/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 */

package org.apache.fop.fo.pagination;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;

// FOP
import org.apache.fop.fo.FOAttributes;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.xml.XMLNamespaces;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.Properties;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.FOPropertySets;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FONode;
import org.apache.fop.datastructs.Tree;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.NCName;

/**
 * Implements the fo:page-sequence-master flow object.  These Fos are
 * children of fo:layout-master-set FOs.  Their contents are specified by
 * (single-page-master-reference|repeatable-page-master-reference
 *                                |repeatable-page-master-alternatives)+
 * N.B. The FoPageSequenceMaster is a subclass of FONode.
 */
public class FoPageSequenceMaster extends FONode {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    private String masterName;

    private ArrayList subSequenceList = new ArrayList(1);

    public FoPageSequenceMaster(FOTree foTree, FONode parent, XMLEvent event)
        throws Tree.TreeException, FOPException, PropertyException
    {
        super(foTree, FObjectNames.PAGE_SEQUENCE_MASTER, parent, event,
                                              FOPropertySets.SEQ_MASTER_SET);
        // Check that the property has been set
        PropertyValue name = propertySet[PropNames.MASTER_NAME];
        if (name == null)
            throw new PropertyException("master-name property not set");
        if (name.getType() != PropertyValue.NCNAME)
            throw new PropertyException
                                ("master-name property not an NCName.");
        masterName = ((NCName)name).getNCName();
        // Process sequence members here
        LinkedList list = new LinkedList();
        list.add((Object)(new XMLEvent.UriLocalName
          (XMLNamespaces.XSLNSpaceIndex, "single-page-master-reference")));
        list.add((Object)(new XMLEvent.UriLocalName
                          (XMLNamespaces.XSLNSpaceIndex,
                                        "repeatable-page-master-reference")));
        list.add((Object)
                 (new XMLEvent.UriLocalName
                          (XMLNamespaces.XSLNSpaceIndex,
                                   "repeatable-page-master-alternatives")));
        try {
            do {
                XMLEvent ev = xmlevents.expectStartElement(list);
                String localName = ev.getLocalName();
                if (localName.equals("single-page-master-reference")) {
                    System.out.println("Found single-page-master-reference");
                } else if (localName.equals
                           ("repeatable-page-master-reference")) {
                    System.out.println
                            ("Found repeatable-page-master-reference");
                } else if (localName.equals
                           ("repeatable-page-master-alternatives")) {
                    System.out.println
                            ("Found repeatable-page-master-reference");
                } else
                    throw new FOPException
                            ("Aargh! expectStartElement(events, list)");
            } while (true);
        } catch (NoSuchElementException e) {
            // sub-sequence specifiers exhausted
        }
        XMLEvent ev = xmlevents.getEndElement(event);
    }

    /**
     * @return a <tt>String</tt> with the "master-name" attribute value.
     */
    public String getMasterName() {
        return masterName;
    }

}
