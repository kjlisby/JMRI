package jmri.managers;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 Tests for the jmri.managers.DefaultUserMessagePreferencesTest class.
 * @author	Bob Jacobsen Copyright 2009
 */
public class DefaultUserMessagePreferencesTest extends TestCase {
    
    public void testSetGet() {
        DefaultUserMessagePreferences d = new DefaultUserMessagePreferences();
        
        Assert.assertTrue(!d.getPreferenceState("one"));
        
        d.setPreferenceState("one", true);
        Assert.assertTrue(d.getPreferenceState("one"));
        Assert.assertTrue(!d.getPreferenceState("two"));
        
        d.setPreferenceState("one", false);
        Assert.assertTrue(!d.getPreferenceState("one"));
        Assert.assertTrue(!d.getPreferenceState("two"));
        
    }
    
    // from here down is testing infrastructure

    public DefaultUserMessagePreferencesTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DefaultUserMessagePreferencesTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DefaultUserMessagePreferencesTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { 
        apps.tests.Log4JFixture.setUp(); 
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultUserMessagePreferencesTest.class.getName());

}
