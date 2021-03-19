package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

/**
 * Definition of objects to handle configuring a layout connection via a SPROG 
 * Generation 5 SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Andrew Crosland 2008
 * @author Andrew Crosland 2019
 */
public class PiSprog3v2ConnectionConfig extends Sprog3PlusConnectionConfig {

    @Override
    public String name() {
        return Bundle.getMessage("PiSprog3v2Title");
    }

}
