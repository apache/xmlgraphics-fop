/*
 * FoRepeatablePageMasterAlternatives.java
 * Created: Sun Jan 27 01:19:31 2002
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto: "Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.FOAttributes;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.SyncedCircularBuffer;

/**
 * <i>FoRepeatablePageMasterAlternatives</i> is the class which processes
 * and represents the <i>o:repeatable-page-master-alternatives</i> element.
 * fo:repeatable-page-master-alternatives is the most flexible of the
 * page masters in the layout-master-set.  It is also the only sub-sequence
 * into which all other sub-sequences can be mapped.  Because of this, it
 * is the only sub-sequence type actually used.
 */
public class FoRepeatablePageMasterAlternatives {

    private FOAttributes attributes;
    private LinkedList conditions;
    private int maximumRepeats;
    private int minimumRepeats;

    public FoRepeatablePageMasterAlternatives
        (FOTree foTree, SyncedCircularBuffer xmlevents, XMLEvent event)
        throws FOPException
    {
        
    }

    public void setMinimumRepeats(int min) {
        minimumRepeats = min;
    }

    public void setMaximumRepeats(int max) {
        maximumRepeats = max;
    }

}// FoRepeatablePageMasterAlternatives
