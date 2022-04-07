package jmri.jmrit.logixng.actions;

import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action triggers a entryExit.
 * <p>
 * This action has the Operation enum, similar to EnableLogix and other actions,
 * despite that's not needed since this action only has one option. But it's
 * here in case someone wants to add more options later.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionEntryExit extends AbstractDigitalAction implements VetoableChangeListener {

    private final LogixNG_SelectNamedBean<DestinationPoints> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, DestinationPoints.class, InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class));
    private NamedBeanAddressing _operationAddressing = NamedBeanAddressing.Direct;
    private Operation _operationDirect = Operation.SetNXPairEnabled;
    private String _operationReference = "";
    private String _operationLocalVariable = "";
    private String _operationFormula = "";
    private ExpressionNode _operationExpressionNode;

    public ActionEntryExit(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionEntryExit copy = new ActionEntryExit(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        copy.setOperationDirect(_operationDirect);
        copy.setOperationAddressing(_operationAddressing);
        copy.setOperationFormula(_operationFormula);
        copy.setOperationLocalVariable(_operationLocalVariable);
        copy.setOperationReference(_operationReference);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<DestinationPoints> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public void setOperationAddressing(NamedBeanAddressing addressing) throws ParserException {
        _operationAddressing = addressing;
        parseLockFormula();
    }

    public NamedBeanAddressing getOperationAddressing() {
        return _operationAddressing;
    }

    public void setOperationDirect(Operation state) {
        _operationDirect = state;
    }

    public Operation getOperationDirect() {
        return _operationDirect;
    }

    public void setOperationReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _operationReference = reference;
    }

    public String getOperationReference() {
        return _operationReference;
    }

    public void setOperationLocalVariable(@Nonnull String localVariable) {
        _operationLocalVariable = localVariable;
    }

    public String getOperationLocalVariable() {
        return _operationLocalVariable;
    }

    public void setOperationFormula(@Nonnull String formula) throws ParserException {
        _operationFormula = formula;
        parseLockFormula();
    }

    public String getLockFormula() {
        return _operationFormula;
    }

    private void parseLockFormula() throws ParserException {
        if (_operationAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _operationExpressionNode = parser.parseExpression(_operationFormula);
        } else {
            _operationExpressionNode = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    private String getNewLock() throws JmriException {

        switch (_operationAddressing) {
            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _operationReference);

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_operationLocalVariable), false);

            case Formula:
                return _operationExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _operationExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : null;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _operationAddressing.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        DestinationPoints entryExit = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (entryExit == null) {
//            log.error("entryExit is null");
            return;
        }

        String name = (_operationAddressing != NamedBeanAddressing.Direct)
                ? getNewLock() : null;

        Operation oper;
        if ((_operationAddressing == NamedBeanAddressing.Direct)) {
            oper = _operationDirect;
        } else {
            oper = Operation.valueOf(name);
        }

        // Variables used in lambda must be effectively final
        Operation theOper = oper;

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            switch (theOper) {
                case SetNXPairEnabled:
                    entryExit.setEnabled(true);
                    break;
                case SetNXPairDisabled:
                    entryExit.setEnabled(false);
                    break;
                case SetNXPairSegment:
                    jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).
                            setSingleSegmentRoute(entryExit.getSystemName());
                    break;
                default:
                    throw new IllegalArgumentException("invalid oper state: " + theOper.name());
            }
        });
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "ActionEntryExit_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        String state;

        switch (_operationAddressing) {
            case Direct:
                state = Bundle.getMessage(locale, "AddressByDirect", _operationDirect._text);
                break;

            case Reference:
                state = Bundle.getMessage(locale, "AddressByReference", _operationReference);
                break;

            case LocalVariable:
                state = Bundle.getMessage(locale, "AddressByLocalVariable", _operationLocalVariable);
                break;

            case Formula:
                state = Bundle.getMessage(locale, "AddressByFormula", _operationFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _operationAddressing.name());
        }

        return Bundle.getMessage(locale, "ActionEntryExit_Long", namedBean, state);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    public enum Operation {
        SetNXPairEnabled(Bundle.getMessage("ActionEntryExit_SetNXPairEnabled")),
        SetNXPairDisabled(Bundle.getMessage("ActionEntryExit_SetNXPairDisabled")),
        SetNXPairSegment(Bundle.getMessage("ActionEntryExit_SetNXPairSegment"));

        private final String _text;

        private Operation(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("getUsageReport :: ActionEntryExit: bean = {}, report = {}", cdl, report);
        NamedBeanHandle<DestinationPoints> handle = _selectNamedBean.getNamedBean();
        if (handle != null && bean.equals(handle.getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionEntryExit.class);

}
