package org.apache.fop.fo.declarations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.xml.SyncedFoXmlEventsBuffer;
import org.apache.fop.datastructs.TreeException;

/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * 
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * <tt>FoLayoutMasterSet</tt> is the class which processes the
 * <i>layout-master-set</i> element.  This is the compulsory first element
 * under the <i>root</i> element in an FO document.
 */

public class FoDeclarations extends FONode {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** Map of <tt>Integer</tt> indices of <i>sparsePropsSet</i> array.
        It is indexed by the FO index of the FO associated with a given
        position in the <i>sparsePropsSet</i> array.  See
        {@link org.apache.fop.fo.FONode#sparsePropsSet FONode.sparsePropsSet}.
     */
    private static final HashMap sparsePropsMap;

    /** An <tt>int</tt> array of of the applicable property indices, in
        property index order. */
    private static final int[] sparseIndices;

    /** The number of applicable properties.  This is the size of the
        <i>sparsePropsSet</i> array. */
    private static final int numProps;

    static {
        // applicableProps is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        sparsePropsMap = new HashMap(0);
        numProps = 0;
        sparseIndices = new int[] {};
    }

    /**
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XMLEvent</tt> that triggered the creation of
     * this node
     */
    public FoDeclarations
        (FOTree foTree, FONode parent, FoXMLEvent event)
        throws TreeException, FOPException, PropertyException
    {
        super(foTree, FObjectNames.DECLARATIONS, parent, event,
              FONode.DECLARATIONS_SET, sparsePropsMap, sparseIndices);
        try {
            FoXMLEvent ev =
                xmlevents.expectStartElement
                    (FObjectNames.COLOR_PROFILE, XMLEvent.DISCARD_W_SPACE);
            if (ev == null)
                throw new FOPException
                        ("No fo:color-profile in fo:declarations.");
            new FoColorProfile(foTree, this, ev);
            xmlevents.getEndElement(ev);
            do {
                ev = xmlevents.expectStartElement
                    (FObjectNames.COLOR_PROFILE, XMLEvent.DISCARD_W_SPACE);
                if (ev == null) break; // No instance of these elements found
                new FoColorProfile(foTree, this, ev);
                // Flush the master event
                xmlevents.getEndElement(ev);
            } while (true);
        } catch (NoSuchElementException e) {
            // Unexpected end of file
            throw new FOPException("layout-master-set: unexpected EOF.");
        }
        catch (PropertyException e) {
            throw new FOPException(e);
        }
        catch (TreeException e) {
            throw new FOPException(e);
        }
        // No need to clean up the build tree, because the whole subtree
        // will be deleted.
        // This is problematical: while Node is obliged to belong to a Tree,
        // any remaining references to elements of the subtree will keep the
        // whole subtree from being GCed.
        makeSparsePropsSet();
    }
        
}
