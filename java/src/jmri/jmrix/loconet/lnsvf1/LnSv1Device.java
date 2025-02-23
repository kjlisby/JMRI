package jmri.jmrix.loconet.lnsvf1;

import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.RosterEntry;

/**
 * A class to hold LocoNet LNSVf1 (LocoIO) device identity information.
 * See jmri.jmrix.loconet.lnsvf1.Sv1DiscoverPane
 *
 * @author B. Milhaupt 2020
 * @author Egbert Broerse 2020, 2025
 */
public class LnSv1Device {
    private int deviceAddressLow; // Module address in reply, value of -1 is ignored, LNSV1 default address : 88
    private int deviceAddressHi;
    private int deviceAddress;
    private String deviceName;
    private String rosterEntryName;
    private int swVersion;
    private RosterEntry rosterEntry;
    private DecoderFile decoderFile;
    private int cvNum;
    private int cvValue;

    public LnSv1Device(int addressL, int addressH, int lastCv, int lastVal, String deviceName, String rosterName, int swVersion) {
        this.deviceAddressLow = addressL;
        this.deviceAddressHi = addressH;
        this.deviceAddress = addressL;
        cvNum = lastCv;
        cvValue = lastVal;
        this.deviceName = deviceName;
        this.rosterEntryName = rosterName;
        this.swVersion = swVersion;
    }

    public int getDestAddr() {return deviceAddress;}
    public int getDestAddrLow() {return deviceAddressLow;}
    public int getDestAddrHigh() {return deviceAddressHi;}
    public String getDeviceName() {return deviceName;}
    public String getRosterName() {return rosterEntryName;}
    public int getSwVersion() {return swVersion;}

    /**
     * Set the table view of the device's destination high address.
     * This routine does _not_ program the device's destination address.
     *
     * @param destAddrL device destination low address
     */
    public void setDestAddr(int destAddrL) {this.deviceAddressLow = destAddrL;}
    public void setDestAddrHigh(int destAddrH) {this.deviceAddressHi = destAddrH;}
    public void setDevName(String s) {deviceName = s;}
    public void setRosterName(String s) {rosterEntryName = s;}
    public void setSwVersion(int version) {swVersion = version;}
    public DecoderFile getDecoderFile() {
        return decoderFile;
    }
    public void setDecoderFile(DecoderFile f) {
        decoderFile = f;
    }

    public RosterEntry getRosterEntry() {
        return rosterEntry;
    }
    public void setRosterEntry(RosterEntry e) {
        rosterEntry = e;
        setRosterName(e.getId()); // is a name (String)
    }

    // optional: remember last used CV
    public int getCvNum() {
        return cvNum;
    }
    public void setCvNum(int num) {
        cvNum = num;
    }
    public int getCvValue() {
        return cvValue;
    }
    public void setCvValue(int val) {
        cvValue = val;
    }

    //private final static Logger log = LoggerFactory.getLogger(Lnsv1Device.class);

}
