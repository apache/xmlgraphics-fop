/*
 * $Id$
 * 
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 * 
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
package org.apache.fop.fo.declarations;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.XmlEventReader;
import org.apache.fop.xml.XmlEvent;

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
        position in the <i>sparsePropsSet</i> array.
     */
    private static final int[] sparsePropsMap;

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
        sparsePropsMap = new int[PropNames.LAST_PROPERTY_INDEX + 1];
        Arrays.fill(sparsePropsMap, -1);
        numProps = 0;
        sparseIndices = new int[] {};
    }

    /**
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XmlEvent</tt> that triggered the creation of
     * this node
     */
    public FoDeclarations
        (FOTree foTree, FONode parent, XmlEvent event)
        throws TreeException, FOPException, PropertyException
    {
        super(foTree, FObjectNames.DECLARATIONS, parent, event,
              FONode.DECLARATIONS_SET, sparsePropsMap, sparseIndices);
        try {
            XmlEvent ev =
                xmlevents.expectStartElement
                    (FObjectNames.COLOR_PROFILE, XmlEvent.DISCARD_W_SPACE);
            if (ev == null)
                throw new FOPException
                        ("No fo:color-profile in fo:declarations.");
            new FoColorProfile(foTree, this, ev);
            ev = xmlevents.getEndElement(XmlEventReader.DISCARD_EV, ev);
            namespaces.relinquishEvent(ev);
            do {
                ev = xmlevents.expectStartElement
                    (FObjectNames.COLOR_PROFILE, XmlEvent.DISCARD_W_SPACE);
                if (ev == null) break; // No instance of these elements found
                new FoColorProfile(foTree, this, ev);
                // Flush the master event
                ev = xmlevents.getEndElement(XmlEventReader.DISCARD_EV, ev);
                namespaces.relinquishEvent(ev);
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
