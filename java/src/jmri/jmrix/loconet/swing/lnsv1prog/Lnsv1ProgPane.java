package jmri.jmrix.loconet.swing.lnsv1prog;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrix.loconet.*;
import jmri.jmrix.loconet.lnsvf1.LnSv1Device;
import jmri.jmrix.loconet.lnsvf1.LnSv1MessageContents;
import jmri.swing.JTablePersistenceManager;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.Objects;

/**
 * Frame for discovery and display of LocoNet LNSVf1 boards, eg. LocoIO.
 * Derived from lncvprog. Verified with HDL and GCA hardware.
 *
 * Some of the message formats used in this class are Copyright Digitrax
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax.
 *
 * Buttons in table row allow to add roster entry for device, and switch to the
 * DecoderPro ops mode programmer.
 *
 * @author Egbert Broerse Copyright (C) 2021, 2022, 2025
 */
public class Lnsv1ProgPane extends jmri.jmrix.loconet.swing.LnPanel implements LocoNetListener {

    private LocoNetSystemConnectionMemo memo;
    protected JToggleButton allProgButton = new JToggleButton();
    protected JToggleButton modProgButton = new JToggleButton();
    protected JButton readButton = new JButton(Bundle.getMessage("ButtonRead"));
    protected JButton writeButton = new JButton(Bundle.getMessage("ButtonWrite"));
    protected JTextField versionField = new JTextField(4);
    protected JTextField addressField = new JTextField(4);
    protected JTextField cvField = new JTextField(4);
    protected JTextField valueField = new JTextField(4);
    protected JCheckBox rawCheckBox = new JCheckBox(Bundle.getMessage("ButtonShowRaw"));
    protected JTable moduleTable = null;
    protected Lnsv1ProgTableModel moduleTableModel = null;
    public static final int ROW_HEIGHT = (new JButton("X").getPreferredSize().height)*9/10;

    protected JPanel tablePanel = null;
    protected JLabel statusText1 = new JLabel();
    protected JLabel statusText2 = new JLabel();
    protected JLabel sepFieldLabel = new JLabel("/", JLabel.RIGHT);
    protected JLabel addressFieldLabel = new JLabel(Bundle.getMessage("LabelModuleAddress", JLabel.RIGHT));
    protected JLabel cvFieldLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("HeadingCv")), JLabel.RIGHT);
    protected JLabel valueFieldLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("HeadingValue")), JLabel.RIGHT);
    protected JTextArea result = new JTextArea(6,50);
    protected String reply = "";
    protected int art;
    protected int adr = 1;
    protected int cv = 0;
    protected int val;
    boolean writeConfirmed = false;
    private final String rawDataCheck = this.getClass().getName() + ".RawData"; // NOI18N
    private final String dontWarnOnClose = this.getClass().getName() + ".DontWarnOnClose"; // NOI18N
    private UserPreferencesManager pm;
    private transient TableRowSorter<Lnsv1ProgTableModel> sorter;
    private LnSv1DevicesManager lnsv1dm;

    private boolean allProgRunning = false;
    private int moduleProgRunning = -1; // stores module address as int during moduleProgramming session, -1 = no session

    /**
     * Constructor method
     */
    public Lnsv1ProgPane() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.swing.lnsv1prog.LnSv1ProgPane"; // NOI18N
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("MenuItemLnSv1Prog");
    }

    /**
     * Initialize the config window
     */
    @Override
    public void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        // buttons at top, like SE8c pane
        add(initButtonPanel()); // requires presence of memo.
        add(initStatusPanel()); // positioned after ButtonPanel so to keep it simple also delayed
        // creation of table must wait for memo + tc to be available, see initComponents(memo) next

        // only way to get notice of the tool being closed, as a JPanel is silently embedded in some JFrame
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                Component comp = e.getChanged();
                if (comp instanceof JmriJFrame) {
                    JmriJFrame toolFrame = (JmriJFrame) comp;
                    if ((Objects.equals(toolFrame.getTitle(), this.getTitle()) &&
                            !toolFrame.isVisible())) { // it was closed/hidden a moment ago
                        handleCloseEvent();
                        log.debug("Component hidden: {}", comp);
                    }
                }
            }
        });
    }

    @Override
    public synchronized void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);
        this.memo = memo;
        lnsv1dm = memo.getLnSv1DevicesManager();
        pm = InstanceManager.getDefault(UserPreferencesManager.class);
        // connect to the LnTrafficController
        if (memo.getLnTrafficController() == null) {
            log.error("No traffic controller is available");
        } else {
            // add listener
            memo.getLnTrafficController().addLocoNetListener(~0, this);
        }

        // create the data model and its table
        moduleTableModel = new Lnsv1ProgTableModel(this, memo);
        moduleTable = new JTable(moduleTableModel);
        moduleTable.setRowSelectionAllowed(false);
        moduleTable.setPreferredScrollableViewportSize(new Dimension(300, 200));
        moduleTable.setRowHeight(ROW_HEIGHT);
        moduleTable.setDefaultEditor(JButton.class, new ButtonEditor(new JButton()));
        moduleTable.setDefaultRenderer(JButton.class, new ButtonRenderer());
        moduleTable.setRowSelectionAllowed(true);
        moduleTable.getSelectionModel().addListSelectionListener(event -> {
            synchronized (this) {
                if (moduleTable.getSelectedRow() > -1 && moduleTable.getSelectedRow() < moduleTable.getRowCount()) {
                    // print first column value from selected row
                    copyEntry((int) moduleTable.getValueAt(moduleTable.getSelectedRow(), 1), (int) moduleTable.getValueAt(moduleTable.getSelectedRow(), 2));
                }
            }
        });
        // establish row sorting for the table
        sorter = new TableRowSorter<>(moduleTableModel);
        moduleTable.setRowSorter(sorter);
         // establish table physical characteristics persistence
        moduleTable.setName("LNSV Device Management"); // NOI18N
        // Reset and then persist the table's ui state
        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent((tpm) -> {
            synchronized (this) {
                tpm.resetState(moduleTable);
                tpm.persist(moduleTable, true);
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(moduleTable);
        tablePanel = new JPanel();
        Border resultBorder = BorderFactory.createEtchedBorder();
        Border resultTitled = BorderFactory.createTitledBorder(resultBorder, Bundle.getMessage("LnSv1TableTitle"));
        tablePanel.setBorder(resultTitled);
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        // this does not fill the full width, why?
//        JSplitPane holder = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
//                tablePanel, getMonitorPanel());
//        holder.setMinimumSize(new Dimension(1000, 400));
//        holder.setPreferredSize(new Dimension(1000, 400));
//        holder.setDividerSize(8);
//        holder.setOneTouchExpandable(true);
//        add(holder, BorderLayout.LINE_START);
        add(tablePanel);
        add(getMonitorPanel());
        rawCheckBox.setSelected(pm.getSimplePreferenceState(rawDataCheck));
    }

    /*
     * Initialize the LNSV1 Monitor panel.
     */
    protected JPanel getMonitorPanel() {
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));

        JPanel panel31 = new JPanel();
        panel31.setLayout(new BoxLayout(panel31, BoxLayout.Y_AXIS));
        JScrollPane resultScrollPane = new JScrollPane(result);
        panel31.add(resultScrollPane);

        panel31.add(rawCheckBox);
        rawCheckBox.setVisible(true);
        rawCheckBox.setToolTipText(Bundle.getMessage("TooltipShowRaw"));
        panel3.add(panel31);
        Border panel3Border = BorderFactory.createEtchedBorder();
        Border panel3Titled = BorderFactory.createTitledBorder(panel3Border, Bundle.getMessage("LnSv1MonitorTitle"));
        panel3.setBorder(panel3Titled);
        return panel3;
    }

    /*
     * Initialize the Button panel. Requires presence of memo to send and receive.
     */
    protected JPanel initButtonPanel() {
        // Set up buttons and entry fields
        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout());

        JPanel panel41 = new JPanel();
        panel41.setLayout(new BoxLayout(panel41, BoxLayout.PAGE_AXIS));
        allProgButton.setText(allProgRunning ?
                Bundle.getMessage("ButtonStopAllProg") : Bundle.getMessage("ButtonStartAllProg"));
        allProgButton.setToolTipText(Bundle.getMessage("TipAllProgButton"));
        allProgButton.addActionListener(e -> allProgButtonActionPerformed());
        panel41.add(allProgButton);

        modProgButton.setText((moduleProgRunning >= 0) ?
                Bundle.getMessage("ButtonStopModProg") : Bundle.getMessage("ButtonStartModProg"));
        modProgButton.setToolTipText(Bundle.getMessage("TipModuleProgButton"));
        modProgButton.addActionListener(e -> modProgButtonActionPerformed());
        panel41.add(modProgButton);
        panel4.add(panel41);

        JPanel panel42 = new JPanel();
        panel42.setLayout(new BoxLayout(panel42, BoxLayout.PAGE_AXIS));
        JPanel panel421 = new JPanel();
        panel421.add(sepFieldLabel);
        // entry field (decimal)
        panel421.add(versionField);
        panel42.add(panel421);

        JPanel panel422 = new JPanel();
        panel422.add(addressFieldLabel);
        // entry field (decimal) for Module Address
        addressField.setText("1");
        panel422.add(addressField);
        panel42.add(panel422);
        panel4.add(panel42);

        JPanel panel43 = new JPanel();
        Border panel43Border = BorderFactory.createEtchedBorder();
        panel43.setBorder(panel43Border);
        panel43.setLayout(new BoxLayout(panel43, BoxLayout.LINE_AXIS));

        JPanel panel431 = new JPanel();
        panel431.setLayout(new BoxLayout(panel431, BoxLayout.PAGE_AXIS));
        JPanel panel4311 = new JPanel();
        panel4311.add(cvFieldLabel);
        // entry field (decimal) for CV number to read/write
        //cvField.setToolTipText(Bundle.getMessage("TipModuleCvField"));
        cvField.setText("0");
        panel4311.add(cvField);
        panel431.add(panel4311);

        JPanel panel4312 = new JPanel();
        panel4312.add(valueFieldLabel);
        // entry field (decimal) for CV value
        //valueField.setToolTipText(Bundle.getMessage("TipModuleValueField"));
        valueField.setText("1");
        panel4312.add(valueField);
        panel431.add(panel4312);
        panel43.add(panel431);

        JPanel panel432 = new JPanel();
        panel432.setLayout(new BoxLayout(panel432, BoxLayout.PAGE_AXIS));
        panel432.add(readButton);
        readButton.setEnabled(false);
        readButton.addActionListener(e -> readButtonActionPerformed());

        panel432.add(writeButton);
        writeButton.setEnabled(false);
        writeButton.addActionListener(e -> writeButtonActionPerformed());
        panel43.add(panel432);
        panel4.add(panel43);

        return panel4;
    }

    /*
     * Initialize the Status panel.
     */
    protected JPanel initStatusPanel() {
        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.PAGE_AXIS));
        JPanel panel21 = new JPanel();
        panel21.setLayout(new FlowLayout());

        statusText1.setText("   ");
        statusText1.setHorizontalAlignment(JLabel.CENTER);
        panel21.add(statusText1);
        panel2.add(panel21);

        statusText2.setText("   ");
        statusText2.setHorizontalAlignment(JLabel.CENTER);
        panel2.add(statusText2);
        return panel2;
    }

    /**
     * GENERALPROG button.
     */
    public void allProgButtonActionPerformed() {
        if (moduleProgRunning >= 0) {
            statusText1.setText(Bundle.getMessage("FeedBackModProgRunning"));
            return;
        }
        // provide user feedback
        readButton.setEnabled(!allProgRunning);
        writeButton.setEnabled(!allProgRunning);
        log.debug("AllProg pressed, allProgRunning={}", allProgRunning);
        if (allProgRunning) {
            log.debug("Session was running, closing");
            // send LncvAllProgEnd command on LocoNet
            //memo.getLnTrafficController().sendLocoNetMessage(LnSv1MessageContents.createAllProgEndRequest(art));
            statusText1.setText(Bundle.getMessage("FeedBackStopAllProg"));
            allProgButton.setText(Bundle.getMessage("ButtonStartAllProg"));
            versionField.setEditable(true);
            addressField.setEditable(true);
            allProgRunning = false;
            return;
        }
        versionField.setEditable(false);
        addressField.setEditable(false);
        art = -1;
        if (!versionField.getText().isEmpty()) {
            try {
                art = inDomain(versionField.getText(), 9999);
            } catch (NumberFormatException e) {
                // fine, will do broadcast all
            }
        }
        // show dialog to protect unwanted ALL messages
        Object[] dialogBoxButtonOptions = {
                Bundle.getMessage("ButtonProceed"),
                Bundle.getMessage("ButtonCancel")};
        int userReply = JmriJOptionPane.showOptionDialog(this.getParent(),
                Bundle.getMessage("DialogAllLnsv1Warning"),
                Bundle.getMessage("WarningTitle"),
                JmriJOptionPane.DEFAULT_OPTION, JmriJOptionPane.QUESTION_MESSAGE,
                null, dialogBoxButtonOptions, dialogBoxButtonOptions[1]);
        if (userReply != 0 ) { // not array position 0 ButtonProceed
            return;
        }
        statusText1.setText(Bundle.getMessage("FeedBackStartAllProg"));
        // send LncvProgSessionStart command on LocoNet
        //LocoNetMessage m = LnSv1MessageContents.createAllProgStartRequest(art);
        //memo.getLnTrafficController().sendLocoNetMessage(m);
        // stop and inform user
        statusText1.setText(Bundle.getMessage("FeedBackStartAllProg"));
        allProgButton.setText(Bundle.getMessage("ButtonStopAllProg"));
        allProgRunning = true;
        log.debug("AllProgRunning=TRUE, allProgButtonActionPerformed ready");
    }

    // MODULEPROG button
    /**
     * Handle Start/End Module Prog button.
     */
    public void modProgButtonActionPerformed() {
        if (allProgRunning) {
            statusText1.setText(Bundle.getMessage("FeedBackAllProgRunning"));
            return;
        }
        if (versionField.getText().isEmpty()) {
            statusText1.setText(Bundle.getMessage("FeedBackEnterArticle"));
            versionField.setBackground(Color.RED);
            modProgButton.setSelected(false);
            return;
        }
        if (addressField.getText().isEmpty()) {
            statusText1.setText(Bundle.getMessage("FeedBackEnterAddress"));
            addressField.setBackground(Color.RED);
            modProgButton.setSelected(false);
            return;
        }
        // provide user feedback
        versionField.setBackground(Color.WHITE); // reset
        readButton.setEnabled(moduleProgRunning < 0);
        writeButton.setEnabled(moduleProgRunning < 0);
        if (moduleProgRunning >= 0) { // stop prog
            try {
                art = inDomain(versionField.getText(), 9999);
                adr = moduleProgRunning; // use module address that was used to start Modprog
                //memo.getLnTrafficController().sendLocoNetMessage(LnSv1MessageContents.createModProgEndRequest(art, adr));
                statusText1.setText(Bundle.getMessage("FeedBackModProgClosed", adr));
                modProgButton.setText(Bundle.getMessage("ButtonStartModProg"));
                moduleProgRunning = -1;
                versionField.setEditable(true);
                addressField.setEditable(true);
            } catch (NumberFormatException e) {
                statusText1.setText(Bundle.getMessage("FeedBackEnterArticle"));
                modProgButton.setSelected(true);
            }
            return;
        }
        if ((!versionField.getText().isEmpty()) && (!addressField.getText().isEmpty())) {
            try {
                art = inDomain(versionField.getText(), 9999);
                adr = inDomain(addressField.getText(), 65535); // goes in d5-d6 as module address
                //memo.getLnTrafficController().sendLocoNetMessage(LnSv1MessageContents.createModProgStartRequest(art, adr));
                statusText1.setText(Bundle.getMessage("FeedBackModProgOpen", adr));
                modProgButton.setText(Bundle.getMessage("ButtonStopModProg"));
                moduleProgRunning = adr; // store address during modProg, so next line is mostly as UI indication:
                versionField.setEditable(false);
                addressField.setEditable(false); // lock address field to prevent accidentally changing it

            } catch (NumberFormatException e) {
                log.error("invalid entry, must be number");
            }
        }
        // stop and inform user
    }

    // READCV button
    /**
     * Handle Read CV button, assemble LNSV1 read message. Requires presence of memo.
     */
    public void readButtonActionPerformed() {
        String sArt = "65535"; // LncvMessageContents.LNCV_ALL = broadcast replace by probeLocoIOs(LnTrafficController ln)
//        if (moduleProgRunning >= 0) {
//            sArt = versionField.getText();
//            versionField.setBackground(Color.WHITE); // reset
//        }
        if (addressField.getText() != null && cvField.getText() != null) {
            try {
                art = inDomain(sArt, 9999); // limited according to Uhlenbrock info
                adr = inDomain(addressField.getText(), 65535); // used as address for reply
                cv = inDomain(cvField.getText(), 9999); // decimal entry
                // memo.getLnTrafficController().sendLocoNetMessage(LnSv1MessageContents.createSv1ReadRequest(art, adr, cv));

                LnSv1MessageContents.probeLocoIOs(memo.getLnTrafficController());
            } catch (NumberFormatException e) {
                log.error("invalid entry, must be number");
            }
        } else {
            statusText1.setText(Bundle.getMessage("FeedBackEnterArticle"));
            versionField.setBackground(Color.RED);
            return;
        }
        // stop and inform user
        statusText1.setText(Bundle.getMessage("FeedBackRead"));
    }

    // WriteCV button
    /**
     * Handle Write button click, assemble LNCV write message. Requires presence of memo.
     */
    public void writeButtonActionPerformed() {
        String sArt = "65535"; // LncvMessageContents.LNCV_ALL;
        if (moduleProgRunning >= 0) {
            sArt = versionField.getText();
        }
        if ((sArt != null) && (cvField.getText() != null) && (valueField.getText() != null)) {
            versionField.setBackground(Color.WHITE);
            try {
                art = inDomain(sArt, 9999);
                cv = inDomain(cvField.getText(), 9999); // decimal entry
                val = inDomain(valueField.getText(), 65535); // decimal entry
                if (cv == 0 && (val > 65534 || val < 1)) {
                    // reserved general module address, warn in status and abort
                    statusText1.setText(Bundle.getMessage("FeedBackValidAddressRange"));
                    valueField.setBackground(Color.RED);
                    return;
                }
                writeConfirmed = false;
                // TODO memo.getLnTrafficController().sendLocoNetMessage(LnSv1MessageContents.createSv1WriteRequest(art, cv, val));
                valueField.setBackground(Color.ORANGE);
            } catch (NumberFormatException e) {
                log.error("invalid entry, must be number");
            }
        } else {
            statusText1.setText(Bundle.getMessage("FeedBackEnterArticle"));
            versionField.setBackground(Color.RED);
            return;
        }
        // stop and inform user
        statusText1.setText(Bundle.getMessage("FeedBackWrite"));
        // LACK reply will be received separately
        // if (received) {
        //      writeConfirmed = true;
        // }
    }

    private int inDomain(String entry, int max) {
        int n = -1;
        try {
            n = Integer.parseInt(entry);
        } catch (NumberFormatException e) {
            log.error("invalid entry, must be number");
        }
        if ((0 <= n) && (n <= max)) {
            return n;
        } else {
            statusText1.setText(Bundle.getMessage("FeedBackInputOutsideRange"));
            return 0;
        }
    }

    public void copyEntry(int art, int mod) {
        if ((moduleProgRunning < 0) && !allProgRunning) { // protect locked fields while programming
            versionField.setText(art + "");
            addressField.setText(mod + "");
        }
    }

    /**
     * {@inheritDoc}
     * Compare to {@link LnOpsModeProgrammer#message(LocoNetMessage)}
     *
     * @param m a message received and analysed for LNCV characteristics
     */
    @Override
    public synchronized void message(LocoNetMessage m) { // receive a LocoNet message and log it
        // got a LocoNet message, see if it's an LNSV1 response
        log.debug("LnSv1ProgPane heard message {}", m.toMonitorString());
        if (LnSv1MessageContents.isSupportedSv1Message(m)) {
            // raw data, to display
            String raw = (rawCheckBox.isSelected() ? ("[" + m + "] ") : "");
            // format the message text, expect it to provide consistent \n after each line
            String formatted = m.toMonitorString(memo.getSystemPrefix());
            // copy the formatted data
            reply += raw + formatted;
        }
        // or LACK write confirmation response from module?
        if ((m.getOpCode() == LnConstants.OPC_LONG_ACK) &&
                (m.getElement(1) == 0x6D)) { // elem 1 = OPC (matches 0xED), elem 2 = ack1
            writeConfirmed = true;
            if (m.getElement(2) == 0x7f) {
                valueField.setBackground(Color.GREEN);
                reply += Bundle.getMessage("LNSV1_WRITE_CONFIRMED", moduleProgRunning) + "\n";
            } else if (m.getElement(2) == 1) {
                valueField.setBackground(Color.RED);
                reply += Bundle.getMessage("LNSV1_WRITE_CV_NOTSUPPORTED", moduleProgRunning, cv) + "\n";
            } else if (m.getElement(2) == 2) {
                valueField.setBackground(Color.RED);
                reply += Bundle.getMessage("LNSV1_WRITE_CV_READONLY", moduleProgRunning, cv) + "\n";
            } else if (m.getElement(2) == 3) {
                valueField.setBackground(Color.RED);
                reply += Bundle.getMessage("LNSV1_WRITE_CV_OUTOFBOUNDS", moduleProgRunning, val) + "\n";
            }
        }
        if (LnSv1MessageContents.extractMessageType(m) == LnSv1MessageContents.Sv1Command.SV1_WRITE_ONE) {
            reply += Bundle.getMessage("LNSV1_WRITE_MOD_MONITOR", (moduleProgRunning == -1 ? "ALL" : moduleProgRunning)) + "\n";
        }
        if (LnSv1MessageContents.extractMessageType(m) == LnSv1MessageContents.Sv1Command.SV1_READ_ONE) {
            reply += Bundle.getMessage("LNSV1_READ_MOD_MONITOR", (moduleProgRunning == -1 ? "ALL" : moduleProgRunning)) + "\n";
        }
        if (LnSv1MessageContents.extractMessageType(m) == LnSv1MessageContents.Sv1Command.SV1_READ_REPLY) {
            // it's an LNSV1 ReadReply message, decode contents:
            LnSv1MessageContents contents = new LnSv1MessageContents(m);
            int msgArt = contents.getVersionNum();
            int msgAdr = moduleProgRunning;
            int msgCv = contents.getSvNum();
            int msgVal = contents.getSvValue();
            if ((msgCv == 0) || (msgArt == art)) { // trust last used address.
                // to be sure, check against Article (hardware class) number
                msgAdr = msgVal; // if cvNum = 0, this is the LNSV1 module address
            }
            String foundMod = "(LNSV1) " + Bundle.getMessage("LabelArticle") +  art + " "
                    + Bundle.getMessage("LabelAddress") + msgAdr + " "
                    + Bundle.getMessage("LabelCv") + msgCv + " "
                    + Bundle.getMessage("LabelValue")+ msgVal + "\n";
            reply += foundMod;
            log.debug("ReadReply={}", reply);
            // storing a Module in the list using the (first) write reply is handled by loconet.LnSvf1DevicesManager

            // enter returned CV in CVnum field
            cvField.setText(msgCv + "");
            cvField.setBackground(Color.WHITE);
            // enter returned value in Value field
            valueField.setText(msgVal + "");
            valueField.setBackground(Color.WHITE);

            LnSv1Device dev = memo.getLnSv1DevicesManager().getDevice(art, adr);
            if (dev != null) {
                dev.setCvNum(msgCv);
                dev.setCvValue(msgVal);
            }
            memo.getLnSv1DevicesManager().firePropertyChange("DeviceListChanged", true, false);
        }

        if (reply != null) { // we fool allProgFinished (copied from LNSV2 class)
            allProgFinished(null);
        }
    }

    /**
     * AllProg Session callback.
     *
     * @param error feedback from Finish process
     */
    public void allProgFinished(String error) {
        if (error != null) {
             log.error("LNSV1 process finished with error: {}", error);
             statusText2.setText(Bundle.getMessage("FeedBackDiscoverFail"));
        } else {
            synchronized (this) {
                statusText2.setText(Bundle.getMessage("FeedBackDiscoverSuccess", lnsv1dm.getDeviceCount()));
                result.setText(reply);
            }
        }
    }

    /**
     * Give user feedback on closing of any open programming sessions when tool window is closed.
     * @see #dispose() for actual closing of sessions
     */
    public void handleCloseEvent() {
        //log.debug("handleCloseEvent() called in LnSv1ProgPane");
        if (allProgRunning || moduleProgRunning > 0) {
            // adds a Don't remember again checkbox and stores setting in pm
            // show dialog
            if (pm != null && !pm.getSimplePreferenceState(dontWarnOnClose)) {
                final JDialog dialog = new JDialog();
                dialog.setTitle(Bundle.getMessage("ReminderTitle"));
                dialog.setLocationRelativeTo(null);
                dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                JPanel container = new JPanel();
                container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

                JLabel question = new JLabel(Bundle.getMessage("DialogRunningWarning"), JLabel.CENTER);
                question.setAlignmentX(Component.CENTER_ALIGNMENT);
                container.add(question);

                JButton okButton = new JButton(Bundle.getMessage("ButtonOK"));
                JPanel buttons = new JPanel();
                buttons.setAlignmentX(Component.CENTER_ALIGNMENT);
                buttons.add(okButton);
                container.add(buttons);

                final JCheckBox remember = new JCheckBox(Bundle.getMessage("DontRemind"));
                remember.setAlignmentX(Component.CENTER_ALIGNMENT);
                remember.setFont(remember.getFont().deriveFont(10f));
                container.add(remember);

                okButton.addActionListener(e -> {
                    if ((remember.isSelected()) && (pm != null)) {
                        pm.setSimplePreferenceState(dontWarnOnClose, remember.isSelected());
                    }
                    dialog.dispose();
                });


                dialog.getContentPane().add(container);
                dialog.pack();
                dialog.setModal(true);
                dialog.setVisible(true);
            }

            // dispose will take care of actually stopping any open prog session
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (memo != null && memo.getLnTrafficController() != null) {
            // disconnect from the LnTrafficController, normally attached/detached after Discovery completed
            memo.getLnTrafficController().removeLocoNetListener(~0, this);
        }
        // and unwind swing
        if (pm != null) {
            pm.setSimplePreferenceState(rawDataCheck, rawCheckBox.isSelected());
        }
        // prevent closing LNCV tool with programming session left open on module(s).
        if (moduleProgRunning >= 0) {
            modProgButtonActionPerformed();
        }
        if (allProgRunning) {
            allProgButtonActionPerformed();
        }
        super.setVisible(false);

        InstanceManager.getOptionalDefault(JTablePersistenceManager.class).ifPresent((tpm) -> {
            synchronized (this) {
                tpm.stopPersisting(moduleTable);
            }
        });

        super.dispose();
    }

    /**
     * Testing methods.
     *
     * @return text currently in Article field
     */
    protected String getArticleEntry() {
        if (!versionField.isEditable()) {
            return "locked";
        } else {
            return versionField.getText();
        }
    }

    protected String getAddressEntry() {
        if (!addressField.isEditable()) {
            return "locked";
        } else {
            return addressField.getText();
        }
    }

    protected synchronized String getMonitorContents(){
            return reply;
    }

    protected void setCvFields(int cvNum, int cvVal) {
        cvField.setText(""+cvNum);
        if (cvVal > -1) {
            valueField.setText("" + cvVal);
        } else {
            valueField.setText("");
        }
    }

    protected synchronized LnSv1Device getModule(int i) {
        if (lnsv1dm == null) {
            lnsv1dm = memo.getLnSv1DevicesManager();
        }
        log.debug("lncvdm.getDeviceCount()={}", lnsv1dm.getDeviceCount());
        if (i > -1 && i < lnsv1dm.getDeviceCount()) {
            return lnsv1dm.getDeviceList().getDevice(i);
        } else {
            log.debug("getModule({}) failed", i);
            return null;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Lnsv1ProgPane.class);

}
