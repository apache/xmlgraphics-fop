/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.fo.pagination;

import org.apache.fop.fo.pagination.FoSimplePageMaster;
import org.apache.fop.fo.FOAttributes;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.apps.FOPException;
//import org.apache.fop.messaging.MessageHandler;

public class FoConditionalPageMasterReference {

    private FoSimplePageMaster master;
    private FOAttributes attributes;

    private int pagePosition;
    private int oddOrEven;
    private int blankOrNotBlank;

    public FoConditionalPageMasterReference(XMLEvent event)
        throws FOPException
    {
        attributes = new FOAttributes(event);
    }

    public void setMinimumRepeats(int min) {
        minimumRepeats = min;
    }

}
