package jmri.jmrit.operations.setup.backup;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LoadDemoActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        LoadDemoAction t = new LoadDemoAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(LoadDemoActionTest.class);

}
