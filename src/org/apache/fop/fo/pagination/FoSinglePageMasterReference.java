/*
 * FoSinglePageMasterReference.java
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
import org.apache.fop.datastructs.SyncedXmlEventsBuffer;

public class FoSinglePageMasterReference extends FONode {

    public FoSinglePageMasterReference
        (SyncedCircularBuffer xmlevents, XMLEvent event) throws FOPException {
	super(foTree, FObjectNames.SINGLE_PAGE_MASTER_REFERENCE, parent,
				    event, FOPropertySets.SEQ_MASTER_SET);
    }

    public getMasterReference() {
        return getPropertyValue(PropNames.MASTER_REFERENCE);
    }

}// FoSinglePageMasterReference
