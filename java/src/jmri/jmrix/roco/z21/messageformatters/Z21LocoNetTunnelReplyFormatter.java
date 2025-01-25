package jmri.jmrix.roco.z21.messageformatters;

import jmri.jmrix.Message;
import jmri.jmrix.roco.z21.Z21MessageFormatter;
import jmri.jmrix.roco.z21.Z21Reply;

/**
 * Z21 LocoNet Tunnel Reply Formatter.
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Z21LocoNetTunnelReplyFormatter implements Z21MessageFormatter {

    @Override
    public Boolean handlesMessage(Message m) {
        return m instanceof Z21Reply &&
                (((Z21Reply) m).getOpCode() == 0x00A0 ||
                ((Z21Reply) m).getOpCode() == 0x00A1 ||
                ((Z21Reply) m).getOpCode() == 0x00A2 );
    }

    @Override
    public String formatMessage(Message m) {
        if(!handlesMessage(m)){
            throw new IllegalArgumentException("Message is not a Z21 LocoNet Tunnel Reply");
        }
        switch (((Z21Reply) m).getOpCode()) {
            case 0x00A0:
                return Bundle.getMessage("Z21LocoNetRxReply", ((Z21Reply) m).getLocoNetMessage().toMonitorString());
            case 0x00A1:
                return Bundle.getMessage("Z21LocoNetTxReply", ((Z21Reply) m).getLocoNetMessage().toMonitorString());
            default:
                return Bundle.getMessage("Z21LocoNetLanReply", ((Z21Reply) m).getLocoNetMessage().toMonitorString());
        }
    }

}
