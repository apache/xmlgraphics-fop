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
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.Properties;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FONode;
import org.apache.fop.datastructs.Tree;

/**
 * Implements the fo:page-sequence-master flow object.  These Fos are
 * children of fo:layout-master-set FOs.  Their contents are specified by
 * (single-page-master-reference|repeatable-page-master-reference
 *                                |repeatable-page-master-alternatives)+
 * N.B. The FoPageSequenceMaster is a subclass of FONode.
 */
public class FoPageSequenceMaster extends FONode {

    private String masterName;

    private ArrayList subSequenceList = new ArrayList(1);

    public FoPageSequenceMaster
        (FOTree foTree, FONode parent, String masterName)
        throws Tree.TreeException, FOPException, PropertyException
    {
        super(foTree, parent, null, FONode.NONE);
        this.masterName = masterName;
    }

    public FoPageSequenceMaster(FOTree foTree, FONode parent, XMLEvent event)
        throws Tree.TreeException, FOPException, PropertyException
    {
        super(foTree, parent, event, FONode.LAYOUT);
        if (event == null) {
            System.out.println("Null event; throwing FOPException");
            throw new FOPException
                    ("Null event passed to FoPageSequenceMaster constructor");
        }
        try {
            masterName = foAttributes.getFoAttrValue("master-name");
        } catch (PropertyException e) {
            throw new FOPException(e.getMessage());
        }
        // Process sequence members here
        LinkedList list = new LinkedList();
        list.add((Object)
                 (new XMLEvent.UriLocalName
                  (XMLEvent.XSLNSpaceIndex, "single-page-master-reference")));
        list.add((Object)
                 (new XMLEvent.UriLocalName
                  (XMLEvent.XSLNSpaceIndex,
                   "repeatable-page-master-reference")));
        list.add((Object)
                 (new XMLEvent.UriLocalName
                  (XMLEvent.XSLNSpaceIndex,
                   "repeatable-page-master-alternatives")));
        try {
            do {
                XMLEvent ev = XMLEvent.expectStartElement(xmlevents, list);
                if (ev.localName.equals("single-page-master-reference")) {
                    System.out.println("Found single-page-master-reference");
                } else if (ev.localName.equals
                           ("repeatable-page-master-reference")) {
                    System.out.println
                            ("Found repeatable-page-master-reference");
                } else if (ev.localName.equals
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
        XMLEvent ev = XMLEvent.getEndElement(xmlevents, event);
    }

    /**
     * @return a <tt>String</tt> with the "master-name" attribute value.
     */
    public String getMasterName() {
        return masterName;
    }

    /**
     * <i>SubSequenceSpecifer</i> objects contain the information about an
     * individual sub-sequence.  They do <i>not</i> extend <tt>FONode</tt>
     * because they are maintained only in the <i>subSequenceList</i> of an
     * <tt>FoPageSequenceMaster</tt> object.
     * <p>When created from <i>fo:single-page-master-reference</i>,
     * <i>fo:repeatable-page-master-reference</i> or
     * <i>fo:repeatable-page-master-alternatives</i> and
     * <i>fo:conditional-page-master-reference</i>s, the attributes on those
     * nodes are merged into the attributes of the parent
     * <tt>FoPageSequenceMaster</tt> object, so that they will be available
     * to any children of this node, both
     * during the parsing of the fo input and during the page construction
     * process.
     * <p>Note that there is some ambiguity about the place of properties
     * defined on the members of the <i>layout-master-set</i> subtree.  Do
     * these properties participate in the properties environment of
     * elements in page-sequence subtrees?
     */
    public class SubSequenceSpecifier {
        public static final int UNBOUNDED = -1;
        private int minRepeats = 1;
        private int maxRepeats = UNBOUNDED;
        private ArrayList conditionals = new ArrayList(1);

        public SubSequenceSpecifier() {
            // Insert the SSS into the parent FoPageSequenceMaster's
            // list
            FoPageSequenceMaster.this.subSequenceList.add(this);
        }

        public SubSequenceSpecifier(int maxRepeats) {
            this();
            minRepeats = 0;
            this.maxRepeats = maxRepeats;
        }

        public SubSequenceSpecifier(int minRepeats, int maxRepeats) {
            this(maxRepeats);
            this.minRepeats = minRepeats;
        }

        /**
         * <i>ConditionalPageMasterReference</i> objects implement the
         * corresponding flow object.  The encode a set of conditions in
         * the indicated master-reference (to a
         * <tt>SimplePageMasterReference</tt>) will be activated.
         */
        public class ConditionalPageMasterReference {
            private String masterReference = "";
            private int pagePosition = Properties.PagePosition.ANY;
            private int oddOrEven = Properties.OddOrEven.ANY;
            private int blankOrNotBlank = Properties.BlankOrNotBlank.ANY;

            public ConditionalPageMasterReference(String masterReference) {
                // Insert into including SubSequenceSpecifier's
                // conditionals list
                SubSequenceSpecifier.this.conditionals.add(this);
                this.masterReference = masterReference;
            }

            public ConditionalPageMasterReference
                (String masterReference, int pagePosition,
                 int oddOrEven, int blankOrNotBlank) {
                this(masterReference);
                this.pagePosition = pagePosition;
                this.oddOrEven = oddOrEven;
                this.blankOrNotBlank = blankOrNotBlank;
            }
        }
    }
}
