package jmri.jmrit.operations.setup.gui;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintOptionActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        PrintOptionAction t = new PrintOptionAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintOptionActionTest.class);

}
