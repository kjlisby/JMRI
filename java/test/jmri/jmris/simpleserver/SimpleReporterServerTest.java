package jmri.jmris.simpleserver;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Tests for the jmri.jmris.simpleserver.SimpleReporterServer class
 *
 * @author Paul Bender
 */
public class SimpleReporterServerTest {

    @Test
    public void testCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) throws java.io.IOException {
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleReporterServer a = new SimpleReporterServer(input, output);
    }

    @Test
    public void testConnectionCtor() {
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    // null output string drops characters
                    // could be replaced by one that checks for specific outputs
                    @Override
                    public void write(int b) throws java.io.IOException {
                    }
                });
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleReporterServer a = new SimpleReporterServer(jcs);
        assertThat(a).isNotNull();
    }

    @Test
    // test sending a message.
    public void testSendMessage() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleReporterServer a = new SimpleReporterServer(input, output);
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method sendMessageMethod=null;
        try {
          sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
        } catch(java.lang.NoSuchMethodException nsm) {
          Assert.fail("Could not find method sendMessage in SimpleReporterServer class. " );
        }

        // override the default permissions.
        Assert.assertNotNull(sendMessageMethod);
        sendMessageMethod.setAccessible(true);
        try {
           sendMessageMethod.invoke(a,"Hello World");
           assertThat(sb.toString()).isEqualTo("Hello World").withFailMessage("SendMessage Check");
        } catch (java.lang.IllegalAccessException iae) {
           Assert.fail("Could not access method sendMessage in SimpleReporterServer class");
        } catch (java.lang.reflect.InvocationTargetException ite){
          Throwable cause = ite.getCause();
          Assert.fail("sendMessage executon failed reason: " + cause.getMessage());
       }
    }

    @Test
    // test sending a message.
    public void testSendMessageWithConnection() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        jmri.jmris.JmriConnectionScaffold jcs = new jmri.jmris.JmriConnectionScaffold(output);
        SimpleReporterServer a = new SimpleReporterServer(jcs);
        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method sendMessageMethod=null;
        try {
          sendMessageMethod = a.getClass().getDeclaredMethod("sendMessage", String.class);
        } catch(java.lang.NoSuchMethodException nsm) {
          Assert.fail("Could not find method sendMessage in SimpleReporterServer class. " );
        }

        // override the default permissions.
        Assert.assertNotNull(sendMessageMethod);
        sendMessageMethod.setAccessible(true);
        try {
           sendMessageMethod.invoke(a,"Hello World");
           assertThat(jcs.getOutput()).isEqualTo("Hello World").withFailMessage("SendMessage Check");
        } catch (java.lang.IllegalAccessException iae) {
           Assert.fail("Could not access method sendMessage in SimpleReporterServer class");
        } catch (java.lang.reflect.InvocationTargetException ite){
          Throwable cause = ite.getCause();
          Assert.fail("sendMessage executon failed reason: " + cause.getMessage());
       }
    }


    @Test
    // test sending an error message.
    public void testSendErrorStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleReporterServer a = new SimpleReporterServer(input, output);
        try {
            a.sendErrorStatus("IT1");
            assertThat(sb.toString()).isEqualTo("REPORTER ERROR\n").withFailMessage("sendErrorStatus check");
        } catch(java.io.IOException ioe){
            Assert.fail("Exception sending Error Status");
        }
    }

    @Test
    // test sending a Report message.
    public void testSendReport() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleReporterServer a = new SimpleReporterServer(input, output);
        try {
            a.initReporter("IR1");
            a.sendReport("IR1","Hello World");
            assertThat(sb.toString()).isEqualTo("REPORTER IR1 Hello World\n").withFailMessage("sendErrorStatus check");
        } catch(java.io.IOException ioe){
            Assert.fail("Exception sending Error Status");
        }
    }

    @Test
    // test sending an ID tag as a Report message.
    public void testSendIdTagReport() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleReporterServer a = new SimpleReporterServer(input, output);
        try {
            a.initReporter("IR1");
            a.sendReport("IR1",new jmri.implementation.DefaultIdTag("ID1234","Hello World"));
            assertThat(sb.toString()).isEqualTo("REPORTER IR1 Hello World\n").withFailMessage("sendErrorStatus check");
        } catch(java.io.IOException ioe){
            Assert.fail("Exception sending Error Status");
        }
    }

    @Test
    // test sending a null Report message.
    public void testSendNullReport() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleReporterServer a = new SimpleReporterServer(input, output);
        try {
            a.initReporter("IR1");
            a.sendReport("IR1",null);
            // null report, sends back the reporter name only.
            assertThat(sb.toString()).isEqualTo("REPORTER IR1\n").withFailMessage("sendErrorStatus check");
        } catch(java.io.IOException ioe){
            Assert.fail("Exception sending Error Status");
        }
    }

    @Test
    // test parsing a Report message.
    public void testParseStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleReporterServer a = new SimpleReporterServer(input, output);
        try {
            a.parseStatus("REPORTER IR1 Hello World\n\r");
            assertThat(sb.toString()).isEqualTo("REPORTER IR1 Hello World\n").withFailMessage("sendErrorStatus check");
        } catch(jmri.JmriException | java.io.IOException ioe){
            Assert.fail("Exception sending Error Status");
        }
    }

    @Test
    // test parsing a null Report message.
    public void testParseNullStatus() {
        StringBuilder sb = new StringBuilder();
        java.io.DataOutputStream output = new java.io.DataOutputStream(
                new java.io.OutputStream() {
                    @Override
                    public void write(int b) throws java.io.IOException {
                        sb.append((char)b);
                    }
                });
        java.io.DataInputStream input = new java.io.DataInputStream(System.in);
        SimpleReporterServer a = new SimpleReporterServer(input, output);
        try {
            a.parseStatus("REPORTER IR1\n\r");
            assertThat(sb.toString()).isEqualTo("REPORTER IR1\n").withFailMessage("sendErrorStatus check");
        } catch(jmri.JmriException | java.io.IOException ioe){
            Assert.fail("Exception sending Error Status");
        }
    }


    // The minimal setup for log4J
    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initReporterManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
