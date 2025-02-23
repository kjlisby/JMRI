package jmri.jmrix.loconet.lnsvf1;

import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Objects;

/**
 * Supporting class for LocoNet SV Programming Format 1 (LocoIO) messaging.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 * <p>
 * Uses the LOCONETSV1MODE programming mode.
 * <p>
 * Uses LnProgrammer LOCOIO_PEER_CODE_SV_VER1 message format, comparable to DecoderPro LOCONETSV1MODE
 * The DecoderPro decoder definition is recommended for all LocoIO versions. Requires JMRI 4.12 or later.
 *
 * @see jmri.jmrix.loconet.LnOpsModeProgrammer#message(LocoNetMessage)
 *
 * Programming SV's
 * <p>
 * The SV's in a LocoIO hardware module can be programmed using LocoNet OPC_PEER_XFER messages.
 * <p>
 * Commands for setting SV's:
 * <p>
 * PC to LocoIO LocoNet message (OPC_PEER_XFER)
 * <pre><code>
 * Code LOCOIO_SV_READ _or_ LOCOIO_SV_WRITE ----
 * 0xE5 OPC_PEER_XFER
 * 0x10 Message length
 * SRCL 0x50                0x50 // low address of LocoBuffer
 * DSTL LocoIO address
 * DSTH LocoIO sub-address
 * PXCT1
 * D1 LOCOIO_SV_READ _or_   LOCOIO_SV_WRITE // Read/Write command
 * D2 SV number             SV number
 * D3 0x00                  0x00
 * D4 0x00                  Data byte to Write
 * PXCT2
 * D5 0x01                  0x01 // LocoBuffer fixed sub-address
 * D6 0x00                  0x00
 * D7 0x00                  0x00
 * D8 0x00                  0x00
 * CHK Checksum             Checksum
 * </code></pre>
 *
 * LocoIO to PC reply message (OPC_PEER_XFER)
 * <pre><code>
 * Code LOCOIO_SV_READ _or_ LOCOIO_SV_WRITE ----
 * 0xE5 OPC_PEER_XFER
 * 0x10 Message length
 * SRCL LocoIO low address
 * DSTL 0x50                0x50 // address of LocoBuffer
 * DSTH 0x01                0x01 // sub-address of LocoBuffer
 * PXCT1 MSB LocoIO version // High order bit of LocoIO version
 * D1 LNSV1_READ _or_       LNSV1_WRITE // Copy of original Command
 * D2 SV number requested   SV number
 * D3 LSBs LocoIO version   // Lower 7 bits of LocoIO version
 * D4 0x00                  0x00
 * PXCT2 MSB Requested Data // High order bit of requested data
 * D5 LocoIO Sub-address
 * D6 Requested Data        0x00
 * D7 Requested Data + 1    0x00
 * D8 Requested Data + 2    Written Data
 * CHK Checksum             Checksum
 * </code></pre>
 *
 * @author John Plocher 2006, 2007
 * @author B. Milhaupt Copyright (C) 2015
 * @author E. Broerse Copyright (C) 2025
 */
public class LnSv1MessageContents {
    public static final int LNSV1_WRITE = 0x01;
    public static final int LNSV1_READ = 0x02;
    public static final int LNSV1_BROADCAST_ADDRESS = 0x0100; // CHECK
    public static final int LOCOIO_PEER_CODE_SV_VER1 = 0x08;

    private int src_l;
    private int src_h;
    private int sv_cmd;
    private int dst_l;
    private int dst_h;
    private int sv_adr;
    private int vrs;
    private int d4;
    private int d6;
    private int d7;
    private int d8;

    // LocoNet "SV 1 format" helper definitions: length byte value for OPC_PEER_XFER message
    public final static int SV1_LENGTH_ELEMENT_VALUE = 0x10;

    // Helper to calculate LocoIO Sensor address from returned data
    public static int SENSOR_ADR(int a1, int a2) {
        return (((a2 & 0x0f) * 128) + (a1 & 0x7f)) + 1;
    }

    // LocoNet "SV 1 format" helper definitions: indexes into the LocoNet message
    public final static int SV1_LENGTH_ELEMENT_INDEX = 1;
    public final static int SV1_SV_SRC_L_ELEMENT_INDEX = 2;
    public final static int SV1_SV_DST_L_ELEMENT_INDEX = 3;
    public final static int SV1_SV_DST_H_ELEMENT_INDEX = 4;
    public final static int SV1_SVX1_ELEMENT_INDEX = 5;
    public final static int SV1_SV_CMD_ELEMENT_INDEX = 6;
    public final static int SV1_SV_ADR_ELEMENT_INDEX = 7;
    public final static int SV1_SV_VRS_ELEMENT_INDEX = 8;
    public final static int SV1_SVD4_ELEMENT_INDEX = 9;
    public final static int SV1_SVX2_ELEMENT_INDEX = 10;
    public final static int SV1_SV_SRC_H_ELEMENT_INDEX = 11;
    public final static int SV1_SVD6_ELEMENT_INDEX = 12;
    public final static int SV1_SVD7_ELEMENT_INDEX = 13;
    public final static int SV1_SVD8_ELEMENT_INDEX = 14;

    //  helpers for decoding SV format 1 messages (versus other OCP_PEER_XFER messages with length 0x10) TODO
    public final static int SV1_SRC_L_ELEMENT_MASK = 0x7f;
    public final static int SV1_SVX1_ELEMENT_VALIDITY_CHECK_MASK = 0x70;
    public final static int SV1_SVX1_ELEMENT_VALIDITY_CHECK_VALUE = 0x10;
    public final static int SV1_SV_CMD_CMDX7_CHECK_MASK = 0x04;
    public final static int SV1_SV_ADR_SVADRX7_CHECK_MASK = 0x04;
    public final static int SV1_SVX2_ELEMENT_VALIDITY_CHECK_MASK = 0x70;
    public final static int SV1_SVX2_ELEMENT_VALIDITY_CHECK_VALUE = 0x10;
    public final static int SV1_SV_VRS_VRSX7_CHECK_MASK = 0x04;
    public final static int SV1_SV_D4_D4X7_CHECK_MASK = 0x08;
    public final static int SV1_SV_D6_D6X7_CHECK_MASK = 0x01;
    public final static int SV1_SV_D7_D7X7_CHECK_MASK = 0x01;
    public final static int SV1_SV_D8_D8X7_CHECK_MASK = 0x02;

    // helpers for decoding SV_CMD
    public final static int SV_CMD_WRITE_ONE = 0x01;
    public final static int SV_CMD_READ_ONE = 0x02;

    // LocoNet "SV 1 format" helper definitions: SV_CMD "reply" bit
    public final static int SV1_SV_CMD_REPLY_BIT_NUMBER = 0x6;
    public final static int SV1_SV_CMD_REPLY_BIT_MASK = (2^SV1_SV_CMD_REPLY_BIT_NUMBER);

    // LocoNet "SV 1 format" helper definitions for data
    public final static int SV1_SV_DATA_INDEX_SOFTWARE_VERSION = 1;

    /**
     * Create a new LnSV1MessageContents object from a LocoNet message.
     *
     * @param m LocoNet message containing an SV Programming Format 1 message
     * @throws IllegalArgumentException if the LocoNet message is not a valid, supported
     *      SV Programming Format 1 message
     */
    public LnSv1MessageContents(LocoNetMessage m)
            throws IllegalArgumentException {

        log.debug("interpreting a LocoNet message - may be an SV1 message");  // NOI18N
        if (!isSupportedSv1Message(m)) {
            log.debug("interpreting a LocoNet message - is NOT an SV1 message");   // NOI18N
            throw new IllegalArgumentException("LocoNet message is not an SV1 message"); // NOI18N
        }
        src_l = m.getElement(SV1_SV_SRC_L_ELEMENT_INDEX);
        src_h = m.getElement(SV1_SV_SRC_H_ELEMENT_INDEX);
        dst_l = m.getElement(SV1_SV_DST_L_ELEMENT_INDEX);
        dst_h = m.getElement(SV1_SV_DST_H_ELEMENT_INDEX);
        int svx1 = m.getElement(SV1_SVX1_ELEMENT_INDEX);
        int svx2 = m.getElement(SV1_SVX2_ELEMENT_INDEX);

        sv_cmd = m.getElement(SV1_SV_CMD_ELEMENT_INDEX)
                + (((svx1 & SV1_SV_CMD_CMDX7_CHECK_MASK) == SV1_SV_CMD_CMDX7_CHECK_MASK)
                ? 0x80 : 0);

        sv_adr  = m.getElement(SV1_SV_ADR_ELEMENT_INDEX)
                + (((svx1 & SV1_SV_ADR_SVADRX7_CHECK_MASK) == SV1_SV_ADR_SVADRX7_CHECK_MASK)
                ? 0x80 : 0);

        vrs = m.getElement(SV1_SV_VRS_ELEMENT_INDEX)
                + (((svx2 & SV1_SV_VRS_VRSX7_CHECK_MASK) == SV1_SV_VRS_VRSX7_CHECK_MASK)
                ? 0x80 : 0);

        d4 = m.getElement(SV1_SVD4_ELEMENT_INDEX)
                + (((svx2 & SV1_SV_D4_D4X7_CHECK_MASK) == SV1_SV_D4_D4X7_CHECK_MASK)
                ? 0x80 : 0);

        d6 = m.getElement(SV1_SVD6_ELEMENT_INDEX)
                + (((svx2 & SV1_SV_D6_D6X7_CHECK_MASK) == SV1_SV_D6_D6X7_CHECK_MASK)
                ? 0x80 : 0);

        d7 = m.getElement(SV1_SVD7_ELEMENT_INDEX)
                + (((svx2 & SV1_SV_D7_D7X7_CHECK_MASK) == SV1_SV_D7_D7X7_CHECK_MASK)
                ? 0x80 : 0);
        d8 = m.getElement(SV1_SVD8_ELEMENT_INDEX)
                + (((svx2 & SV1_SV_D8_D8X7_CHECK_MASK) == SV1_SV_D8_D8X7_CHECK_MASK)
                ? 0x80 : 0);
    }

    /**
     * Check a LocoNet message to determine if it is a valid SV Programming Format 1
     *      message.
     *
     * @param m  LocoNet message to check
     * @return true if LocoNet message m is a supported SV Programming Format 1
     *      message, else false.
     */
    public static boolean isSupportedSv1Message(LocoNetMessage m) {
        // must be OPC_PEER_XFER opcode
        if (m.getOpCode() != LnConstants.OPC_PEER_XFER) {
            log.debug ("cannot be SV1 message because not OPC_PEER_XFER");  // NOI18N
            return false;
        }

        // length of OPC_PEER_XFER must be 0x10
        if (m.getElement(1) != 0x10) {
            log.debug ("cannot be SV1 message because not length 0x10");  // NOI18N
            return false;
        }

        // "extended command" identifier must be correct.  Check part of the
        // "extended command" identifier
        if ((m.getElement(SV1_SVX1_ELEMENT_INDEX)
                & SV1_SVX1_ELEMENT_VALIDITY_CHECK_MASK)
                != SV1_SVX1_ELEMENT_VALIDITY_CHECK_VALUE) {
            log.debug ("cannot be SV1 message because SVX1 upper nibble wrong");  // NOI18N
            return false;
        }
        // "extended command" identifier must be correct.  Check the rest
        // of the "extended command" identifier
        if ((m.getElement(SV1_SVX2_ELEMENT_INDEX)
                & SV1_SVX2_ELEMENT_VALIDITY_CHECK_MASK)
                != SV1_SVX2_ELEMENT_VALIDITY_CHECK_VALUE) {
            log.debug ("cannot be SV1 message because SVX2 upper nibble wrong");  // NOI18N
            return false;
        }

        // check the <SV_CMD> value
        if (isSupportedSv1Command(m.getElement(SV1_SV_CMD_ELEMENT_INDEX))) {
            log.debug("LocoNet message is a supported SV Format 2 message");
            return true;
        }
        log.debug("LocoNet message is not a supported SV Format 2 message");  // NOI18N
        return false;
    }

    /**
     * Compare reply message against a specific SV Programming Format 1 message type.
     *
     * @param m  LocoNet message to be verified as an SV Programming Format 1 message
     *      with the specified &lt;SV_CMD&gt; value
     * @param svCmd  SV Programming Format 1 command to expect
     * @return true if message is an SV Programming Format 1 message of the specified &lt;SV_CMD&gt;,
     *      else false.
     */
    public static boolean isLnMessageASpecificSv1Command(LocoNetMessage m, Sv1Command svCmd) {
        // must be OPC_PEER_XFER opcode
        if (m.getOpCode() != LnConstants.OPC_PEER_XFER) {
            log.debug ("cannot be SV1 message because not OPC_PEER_XFER");  // NOI18N
            return false;
        }

        // length of OPC_PEER_XFER must be 0x10
        if (m.getElement(1) != 0x10) {
            log.debug ("cannot be SV1 message because not length 0x10");  // NOI18N
            return false;
        }

        // "extended command" identifier must be correct.  Check part of the
        // "extended command" identifier
        if ((m.getElement(SV1_SVX1_ELEMENT_INDEX)
                & SV1_SVX1_ELEMENT_VALIDITY_CHECK_MASK)
                != SV1_SVX1_ELEMENT_VALIDITY_CHECK_VALUE) {
            log.debug ("cannot be SV1 message because SVX1 upper nibble wrong");  // NOI18N
            return false;
        }
        // "extended command" identifier must be correct.  Check the rest
        // of the "extended command" identifier
        if ((m.getElement(SV1_SVX2_ELEMENT_INDEX)
                & SV1_SVX2_ELEMENT_VALIDITY_CHECK_MASK)
                != SV1_SVX2_ELEMENT_VALIDITY_CHECK_VALUE) {
            log.debug ("cannot be SV1 message because SVX2 upper nibble wrong");  // NOI18N
            return false;
        }

        // check the <SV_CMD> value
        if (isSupportedSv1Command(m.getElement(SV1_SV_CMD_ELEMENT_INDEX))) {
            log.debug("LocoNet message is a supported SV Format 1 message");  // NOI18N
            if (Objects.equals(extractMessageType(m), svCmd)) {
                log.debug("LocoNet message is the specified SV Format 1 message");  // NOI18N
                return true;
            }
        }
        log.debug("LocoNet message is not a supported SV Format 1 message");  // NOI18N
        return false;
    }

    /**
     * Interpret a LocoNet message to determine its SV Programming Format 1 &lt;SV_CMD&gt;.
     * If the message is not an SV Programming Format 1 message, returns null.
     *
     * @param m  LocoNet message containing SV Programming Format 1 message
     * @return Sv1Command found in the SV Programming Format 1 message or null if not found
     */
    public static Sv1Command extractMessageType(LocoNetMessage m) {
        if (isSupportedSv1Message(m)) {
            int msgCmd = m.getElement(SV1_SV_CMD_ELEMENT_INDEX);
            for (Sv1Command s: Sv1Command.values()) {
                if (s.getCmd() == msgCmd) {
                    log.debug("LocoNet message has SV1 message command {}", msgCmd);  // NOI18N
                    return s;
                }
            }
        }
        return null;
    }

    /**
     * Interpret the SV Programming Format 1 message into a human-readable string.
     *
     * @return String containing a human-readable version of the SV Programming
     *      Format 1 message
     */
    @Override
    public String toString() {
        Locale l = Locale.getDefault();
        return LnSv1MessageContents.this.toString(l);
    }

    /**
     * Interpret the SV Programming Format 2 message into a human-readable string.
     *
     * @param locale  locale to use for the human-readable string
     * @return String containing a human-readable version of the SV Programming
     *      Format 1 message, in the language specified by the Locale, if the
     *      properties have been translated to that Locale, else in the default
     *      English language.
     */
    public String toString(Locale locale) {
        String returnString;
        log.debug("interpreting an SV1 message - sv_cmd is {}", sv_cmd);  // NOI18N

        switch (sv_cmd) {
            case (SV_CMD_WRITE_ONE):
                returnString = Bundle.getMessage(locale, "SV1_WRITE_ONE_INTERPRETED",
                        src_l,
                        src_h,
                        dst_l,
                        dst_h,
                        sv_adr,
                        d4);
                break;

            case (SV_CMD_READ_ONE):
                returnString = Bundle.getMessage(locale, "SV1_READ_ONE_INTERPRETED",
                        src_l,
                        src_h,
                        dst_l,
                        dst_h,
                        sv_adr,
                        d6,
                        d7,
                        d8);
                break;

//            case (SV_CMD_PROBE_ALL):
//                returnString = Bundle.getMessage(locale, "SV1_PROBE_ALL_INTERPRETED",
//                        src);
//                break;

            default:
                return Bundle.getMessage(locale, "SV1_UNDEFINED_MESSAGE") + "\n";
        }

        log.debug("interpreted: {}", returnString);  // NOI18N
        return returnString + "\n"; // NOI18N
    }

    /**
     *
     * @param possibleCmd  integer to be compared to the command list
     * @return  true if the possibleCmd value is one of the supported SV
     *      Programming Format 1 commands
     */
    public static boolean isSupportedSv1Command(int possibleCmd) {
        switch (possibleCmd) {
            case (SV_CMD_WRITE_ONE):
            case (SV_CMD_READ_ONE):
                return true;
            default:
                return false;
        }
    }

    /**
     * Confirm a message specifies a valid (known) SV Programming Format 1 command.
     *
     * @return true if the SV1 message specifies a valid (known) SV Programming
     *      Format 1 command.
     */
    public boolean isSupportedSv1Command() {
        return isSupportedSv1Command(sv_cmd);
    }

    /**
     *
     * @return true if the SV1 message is a SV1 Read One Reply message
     */
    public boolean isSupportedSv1ReadOneReply() {
        return (sv_cmd == SV_CMD_READ_ONE);
    }

    /**
     * Get the data from a SVs Single Read Reply message. May also be used to
     * return the effective SV value reported in a SV1 Single Write Reply message.
     *
     * @return the {@code <D6>} value from the SV1 message
     */
    public int getSingleReadReportData() {
        return d6;
    }

    // Next 3 message methods copied from LocoIO, Core of LNSV1, still used by jmrit.symbolicprog !

    /**
     * Compose a LocoNet message from the given ingredients for reading
     * the value of one specific SV from a given LocoIO.
     *
     * @param locoIOAddress base address of the LocoIO board to read from
     * @param locoIOSubAddress subAddress of the LocoIO board
     * @param cv the SV index to query
     * @return complete message to send
     */
    public static LocoNetMessage readCV(int locoIOAddress, int locoIOSubAddress, int cv) {
        int[] contents = {LNSV1_READ, cv, 0, 0, locoIOSubAddress, 0, 0, 0};

        return LocoNetMessage.makePeerXfr(
                0x1050, // B'cast locobuffer address
                locoIOAddress,
                contents, // CV and SubAddr to read
                LOCOIO_PEER_CODE_SV_VER1
        );
    }

    /**
     * Compose a LocoNet message from the given ingredients for reading
     * the value of one specific SV from a given LocoIO.
     *
     * @param locoIOAddress base address of the LocoIO board to read from
     * @param locoIOSubAddress subAddress of the LocoIO board
     * @param cv the SV index to change
     * @param data the new value to store in the board's SV
     * @return complete message to send
     */
    public static LocoNetMessage writeCV(int locoIOAddress, int locoIOSubAddress, int cv, int data) {
        int[] contents = {LNSV1_WRITE, cv, 0, data, locoIOSubAddress, 0, 0, 0};

        return LocoNetMessage.makePeerXfr(
                0x1050, // B'cast locobuffer address
                locoIOAddress,
                contents, // CV and SubAddr to read
                LOCOIO_PEER_CODE_SV_VER1
        );
    }

    /**
     * Compose and send a message out onto LocoNet changing the LocoIO hardware board
     * address of all connected LocoIO boards.
     * <p>
     * User is warned that this is a broadcast type operation.
     *
     * @param address the new base address of the LocoIO board to change
     * @param subAddress the new subAddress of the board
     * @param ln the TrafficController to use for sending the message
     */
    public static void programLocoIOAddress(int address, int subAddress, LnTrafficController ln) {
        LocoNetMessage msg;
        msg = writeCV(0x0100, 0, 1, address & 0xFF);
        ln.sendLocoNetMessage(msg);
        if (subAddress != 0) {
            msg = writeCV(0x0100, 0, 2, subAddress);
            ln.sendLocoNetMessage(msg);
        }
    }

    /**
     * Send out a probe of all connected LocoIO units on a given LocoNet connection.
     *
     * @param ln the TrafficController to use for sending the message
     */
    public static void probeLocoIOs(LnTrafficController ln) {
        LocoNetMessage msg;
        msg = readCV(LNSV1_BROADCAST_ADDRESS, 0, 2);
        ln.sendLocoNetMessage(msg);
    }

    /**
     * Create a LocoNet message containing an SV Programming Format 1 message.
     * See Programmer message code in {@link jmri.jmrix.loconet.LnOpsModeProgrammer} loadSV1MessageFormat
     *
     * @param source  source device low address (14 bit, (for &lt;SRC_L&gt; and &lt;SRC_H&gt;)
     * @param destination = SV format 1 destination address (for &lt;DST_L&gt; and &lt;DST_H&gt;)
     * @param command  SV Programming Format 1 command number (for &lt;SV_CMD&gt;)
     * @param svNum  SV Programming Format 1 7-bit SV number
     * @param version  Programming Format 1 7-bit firmware version number, 0 in request, >0 in reply
     * @param d4  SV Programming Format 1 first data value (for &lt;D1&gt;)
     * @param d6  SV Programming Format 1 second data value (for &lt;D2&gt;)
     * @param d7  SV Programming Format 1 third data value (for &lt;D3&gt;)
     * @param d8  SV Programming Format 1 fourth data value (for &lt;D4&gt;)
     * @return LocoNet message for the requested message
     * @throws IllegalArgumentException if command is not a valid SV Programming Format 1 &lt;SV_CMD&gt; value
     */
    public static LocoNetMessage createSv1Message (int source, int destination, int command,
                                                   int svNum, int version, int d4, int d6, int d7, int d8)
            throws IllegalArgumentException {

        if ( ! isSupportedSv1Command(command)) {
            throw new IllegalArgumentException("Command is not a supported SV1 command"); // NOI18N
        }
        LocoNetMessage m = new LocoNetMessage(SV1_LENGTH_ELEMENT_VALUE);
        m.setOpCode(LnConstants.OPC_PEER_XFER);
        m.setElement(SV1_LENGTH_ELEMENT_INDEX, SV1_LENGTH_ELEMENT_VALUE);
        m.setElement(SV1_SV_SRC_L_ELEMENT_INDEX, (source & SV1_SRC_L_ELEMENT_MASK));
        m.setElement(SV1_SV_DST_L_ELEMENT_INDEX, (destination & 0x7f));
        m.setElement(SV1_SV_DST_H_ELEMENT_INDEX, ((destination >> 8) & 0x7f));

        int svx1 = SV1_SVX1_ELEMENT_VALIDITY_CHECK_VALUE;
        // no hi bit for CMD
        svx1 = svx1 + (((svNum & 0x80) == 0x80) ? SV1_SV_ADR_SVADRX7_CHECK_MASK : 0);
        svx1 = svx1 + (((version & 0x80) == 0x80) ? SV1_SV_VRS_VRSX7_CHECK_MASK : 0);
        svx1 = svx1 + (((d4 & 0x80) == 0x80) ? SV1_SV_D4_D4X7_CHECK_MASK : 0);
        m.setElement(SV1_SVX1_ELEMENT_INDEX, svx1);

        m.setElement(SV1_SV_CMD_ELEMENT_INDEX, command);
        m.setElement(SV1_SV_ADR_ELEMENT_INDEX, (svNum & 0x7f));
        m.setElement(SV1_SV_VRS_ELEMENT_INDEX, (version & 0x7f));
        m.setElement(SV1_SVD4_ELEMENT_INDEX, (d4 & 0x7f));

        int svx2 = SV1_SVX2_ELEMENT_VALIDITY_CHECK_VALUE;
        // no hi bit for SRC_H
        svx2 = svx2 + (((d6 & 0x80) == 0x80) ? SV1_SV_D6_D6X7_CHECK_MASK : 0);
        svx2 = svx2 + (((d7 & 0x80) == 0x80) ? SV1_SV_D7_D7X7_CHECK_MASK : 0);
        svx2 = svx2 + (((d8 & 0x80) == 0x80) ? SV1_SV_D8_D8X7_CHECK_MASK : 0);
        m.setElement(SV1_SVX2_ELEMENT_INDEX, svx2);

        m.setElement(SV1_SVD4_ELEMENT_INDEX, (d4 & 0x7f));
        m.setElement(SV1_SVD6_ELEMENT_INDEX, (d6 & 0x7f));
        m.setElement(SV1_SVD7_ELEMENT_INDEX, (d7 & 0x7f));
        m.setElement(SV1_SVD8_ELEMENT_INDEX, (d8 & 0x7f));

        return m;
    }

    public int getDestAddr() {
        //if (sv_cmd != Sv1Command.SV1_PROBE_ALL.sv_cmd) {
        return dst_l + 256*dst_h;
        //}
        //return -1;
    }

    public int getCmd() {
        return sv_cmd;
    }

    public int getSvNum() {
        if ((sv_cmd == LnSv1MessageContents.Sv1Command.SV1_READ_ONE.sv_cmd) ||
                (sv_cmd == LnSv1MessageContents.Sv1Command.SV1_WRITE_ONE.sv_cmd)) {
            return sv_adr;
        }
        return -1;
    }

    public int getSvValue() {
        if ((sv_cmd == LnSv1MessageContents.Sv1Command.SV1_READ_REPLY.sv_cmd) ||
                (sv_cmd == LnSv1MessageContents.Sv1Command.SV1_WRITE_ONE.sv_cmd)) {
            return 1; // mod;
        }
        return -1;
    }

    public int getVersionNum() {
        if ((sv_cmd == LnSv1MessageContents.Sv1Command.SV1_READ_ONE.sv_cmd) ||
                (sv_cmd == LnSv1MessageContents.Sv1Command.SV1_WRITE_ONE.sv_cmd) ||
                (sv_cmd == LnSv1MessageContents.Sv1Command.SV1_READ_REPLY.sv_cmd)) {
            return 2; // art;
        }
        return -1;
    }

    /**
     *
     * @param m  the preceding LocoNet message
     * @param svValues  array containing the SV values; only one value is used
     *          when m contains an SV_QUERY_ONE, else contains 4 values.
     * @return  LocoNet message containing the reply, or null if preceding
     *          message isn't a query
     */
    public static LocoNetMessage createSv1ReadReply(LocoNetMessage m, int[] svValues) {
        if (!isSupportedSv1Message(m)) {
            return null;
        }
        if (m.getElement(3) != Sv1Command.SV1_READ_ONE.sv_cmd) {
            return null;
        }
        LocoNetMessage n = m;
        n.setElement(3, n.getElement(3) + 0x40);
        n.setElement(11, svValues[0] & 0x7F);
        if (n.getElement(3) == Sv1Command.SV1_READ_ONE.sv_cmd) {
            n.setElement(12, 0);
            n.setElement(13, 0);
            n.setElement(14, 0);
            int a = n.getElement(10);
            a &= 0x70;
            if ((svValues[0] & 0xFF) > 0x7f) {
                a |= 1;
            }
            n.setElement(10, a);
            return n;
        }
        n.setElement(12, svValues[1] & 0x7F);
        n.setElement(13, svValues[2] & 0x7F);
        n.setElement(14, svValues[3] & 0x7F);
        int a = n.getElement(10);
        a &= 0x70;
        a |= ((svValues[1] & 0x80) >> 6);
        a |= ((svValues[2] & 0x80) >> 5);
        a |= ((svValues[3] & 0x80) >> 5);
        n.setElement(10, a);
        return n;
    }

    /**
     *
     * @param m  the preceding LocoNet message
     * @param svValue  value of one SV register
     * @return  LocoNet message containing the reply, or null if preceding
     *          message isn't a query
     */
    public static LocoNetMessage createSv1ReadReply(LocoNetMessage m, int svValue) {
        return createSv1ReadReply(m, new int[] {svValue});
    }

    /**
     * Get the d4 value
     * @return d4 element contents
     */
    public int getSv1D4() {
        return d4;
    }

    /**
     * Get the d6 value
     * @return d6 element contents
     */
    public int getSv1D6() {
        return d6;
    }

    /**
     * Get the d7 value
     * @return d7 element contents
     */
    public int getSv1D7() {
        return d7;
    }

    /**
     * Get the d8 value
     * @return d8 element contents
     */
    public int getSv1D8() {
        return d8;
    }

    public static LocoNetMessage createSv1WriteRequest(int deviceAddress, int svNum, int value) {
        return createSv1Message(0x50, deviceAddress,
                Sv1Command.SV1_WRITE_ONE.sv_cmd,
                svNum, 0, value, 0, 0, 0);
    }

    /**
     * Create LocoNet message for another query of an SV of this object.
     *
     * @param deviceAddress  address of the device
     * @param svNum  SV number
     * @return LocoNet message
     */
    public static LocoNetMessage createSv1ReadRequest(int deviceAddress, int svNum) {
        return createSv1Message(0x50, deviceAddress,
                Sv1Command.SV1_READ_ONE.sv_cmd,
                svNum, 0, 0, 0, 0, 0);
    }

    public enum Sv1Command {
        SV1_WRITE_ONE (0x01),
        SV1_READ_ONE (0x02),
        SV1_WRITE_REPLY (0x01),
        SV1_READ_REPLY (0x02);

        private final int sv_cmd;

        Sv1Command(int sv_cmd) {
            this.sv_cmd = sv_cmd;
        }

        int getCmd() {return sv_cmd;}

        public static int getCmd(Sv1Command mt) {
            return mt.getCmd();
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LnSv1MessageContents.class);

}
