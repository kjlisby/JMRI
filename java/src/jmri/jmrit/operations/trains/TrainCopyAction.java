// TrainCopyAction.java
package jmri.jmrit.operations.trains;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainCopyFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision: 17977 $
 */
public class TrainCopyAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -8004749161373746580L;

    public TrainCopyAction(String s) {
        super(s);
    }

    Train _train = null;

    public TrainCopyAction(String s, Train train) {
        super(s);
        _train = train;
    }

    TrainCopyFrame f = null;

    public void actionPerformed(ActionEvent e) {
        // create a copy train frame
        if (f == null || !f.isVisible()) {
            f = new TrainCopyFrame(_train);
        }
        f.setExtendedState(Frame.NORMAL);
        f.setVisible(true); // this also brings the frame into focus
    }
}

/* @(#)TrainCopyAction.java */
