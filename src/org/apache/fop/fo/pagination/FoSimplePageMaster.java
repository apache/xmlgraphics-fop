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
import org.apache.fop.fo.FOAttributes;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.Tree;

/**
 * Implements the fo:simple-page-master flow object
 */
public class FoSimplePageMaster extends FONode {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    private String masterName;

    /**
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XMLEvent</tt> that triggered the creation of
     * this node
     */
    public FoSimplePageMaster(FOTree foTree, FONode parent, XMLEvent event)
        throws Tree.TreeException, FOPException, PropertyException
    {
        super(foTree, FObjectNames.SIMPLE_PAGE_MASTER, parent, event,
              FONode.LAYOUT);
        System.out.println("FOAttributes: " + event);
        try {
            masterName = foAttributes.getFoAttrValue("master-name");
        } catch (PropertyException e) {
            throw new FOPException(e.getMessage());
        }
        // Process regions here
        XMLEvent ev = xmlevents.getEndElement(event);
    }

    /**
     * @return a <tt>String</tt> with the "master-name" attribute value.
     */
    public String getMasterName() {
        return masterName;
    }
}
