/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.Tree;

/**
 * Implements the fo:simple-page-master flow object
 */
public class FoRegionStart extends FoRegionStartEnd {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XMLEvent</tt> that triggered the creation of
     * this node
     */
    public FoRegionStart(FOTree foTree, FONode parent, FoXMLEvent event)
        throws Tree.TreeException, FOPException
    {
        super(foTree, FObjectNames.REGION_START, parent, event);
        makeSparsePropsSet();
    }

}
