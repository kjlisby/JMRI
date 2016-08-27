package jmri.swing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.SortOrder;
import jmri.profile.Profile;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author rhwood
 */
public class JmriJTablePersistenceManagerTest {
    
    public JmriJTablePersistenceManagerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of persist method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testPersist() {
        System.out.println("persist");
        JTable table = null;
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        instance.persist(table);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stopPersisting method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testStopPersisting() {
        System.out.println("stopPersisting");
        JTable table = null;
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        instance.stopPersisting(table);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clearState method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testClearState() {
        System.out.println("clearState");
        JTable table = null;
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        instance.clearState(table);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of cacheState method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testCacheState() {
        System.out.println("cacheState");
        JTable table = null;
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        instance.cacheState(table);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of resetState method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testResetState() {
        System.out.println("resetState");
        JTable table = null;
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        instance.resetState(table);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setPaused method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testSetPaused() {
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        Assert.assertFalse(instance.isPaused());
        instance.setPaused(true);
        Assert.assertTrue(instance.isPaused());
        instance.setPaused(false);
        Assert.assertFalse(instance.isPaused());
    }

    /**
     * Test of isPaused method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testIsPaused() {
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        Assert.assertFalse(instance.isPaused());
        instance.setPaused(true);
        Assert.assertTrue(instance.isPaused());
        instance.setPaused(false);
        Assert.assertFalse(instance.isPaused());
    }

    /**
     * Test of initialize method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testInitialize() throws Exception {
        System.out.println("initialize");
        Profile profile = null;
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        instance.initialize(profile);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of savePreferences method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testSavePreferences() {
        System.out.println("savePreferences");
        Profile profile = null;
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        instance.savePreferences(profile);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProvides method, of class JmriJTablePersistenceManager.
     */
    @Test
    public void testGetProvides() {
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        Set<Class<?>> expected = new HashSet<>();
        expected.add(JTablePersistenceManager.class);
        expected.add(JmriJTablePersistenceManager.class);
        Assert.assertEquals(expected, instance.getProvides());
    }

    /**
     * Test of getPersistedState method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testGetPersistedState() {
        System.out.println("getPersistedState");
        String table = "";
        String column = "";
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        JmriJTablePersistenceManager.TableColumnPreferences expResult = null;
        JmriJTablePersistenceManager.TableColumnPreferences result = instance.getPersistedState(table, column);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setPersistedState method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testSetPersistedState() {
        System.out.println("setPersistedState");
        String table = "";
        String column = "";
        int order = 0;
        int width = 0;
        SortOrder sort = null;
        boolean hidden = false;
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        instance.setPersistedState(table, column, order, width, sort, hidden);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of propertyChange method, of class JmriJTablePersistenceManager.
     */
    @Test
    @Ignore
    public void testPropertyChange() {
        System.out.println("propertyChange");
        PropertyChangeEvent evt = null;
        JmriJTablePersistenceManager instance = new JmriJTablePersistenceManager();
        instance.propertyChange(evt);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}