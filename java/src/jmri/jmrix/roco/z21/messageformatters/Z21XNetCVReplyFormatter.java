package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.lenz.XPressNetMessageFormatter;
import jmri.jmrix.roco.z21.Z21Constants;
import jmri.jmrix.roco.z21.Z21XNetReply;

/**
 * Format Z21XNetReply CV Programming results for display in the XpressNet monitor.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21XNetCVReplyFormatter implements XPressNetMessageFormatter {
    public Z21XNetCVReplyFormatter() {
    }

    public Boolean handlesMessage(jmri.jmrix.Message m) {
        return m instanceof jmri.jmrix.roco.z21.Z21XNetReply &&
                m.getElement(0) == Z21Constants.LAN_X_CV_RESULT_XHEADER &&
                m.getElement(1) == Z21Constants.LAN_X_CV_RESULT_DB0;
    }

    public String formatMessage(jmri.jmrix.Message m) {
        if (!handlesMessage(m)) {
            throw new IllegalArgumentException("Message is not a Z21XNetReply");
        }
        Z21XNetReply r = (Z21XNetReply) m;
        int value = r.getElement(4) & 0xFF;
        int cv = ( (r.getElement(2)&0xFF) << 8) +
                        ( r.getElement(3)& 0xFF ) + 1;
        return Bundle.getMessage("Z21LAN_X_CV_RESULT",cv,value);
    }
}
