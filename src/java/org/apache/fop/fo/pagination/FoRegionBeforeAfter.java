/*
 * $Id$
 * 
 *
 * Copyright 1999-2003 The Apache Software Foundation.
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
 */

package org.apache.fop.fo.pagination;

// FOP
import java.util.Arrays;
import java.util.BitSet;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertySets;
import org.apache.fop.xml.XmlEvent;

/**
 * Implements the fo:simple-page-master flow object
 */
public class FoRegionBeforeAfter extends FONode {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** Map of <tt>Integer</tt> indices of <i>sparsePropsSet</i> array.
        It is indexed by the FO index of the FO associated with a given
        position in the <i>sparsePropsSet</i> array.
     */
    protected static final int[] sparsePropsMap;

    /** An <tt>int</tt> array of of the applicable property indices, in
        property index order. */
    protected static final int[] sparseIndices;
 
    /** The number of applicable properties.  This is the size of the
        <i>sparsePropsSet</i> array. */
    protected static final int numProps;

    static {
        // Collect the sets of properties that apply
        BitSet propsets = PropertySets.backgroundSetClone();
        propsets.or(PropertySets.borderSet);
        propsets.or(PropertySets.paddingSet);
        propsets.set(PropNames.CLIP);
        propsets.set(PropNames.DISPLAY_ALIGN);
        propsets.set(PropNames.OVERFLOW);
        propsets.set(PropNames.REGION_NAME);
        propsets.set(PropNames.REFERENCE_ORIENTATION);
        propsets.set(PropNames.WRITING_MODE);
        propsets.set(PropNames.EXTENT);
        propsets.set(PropNames.PRECEDENCE);

        // Map these properties into sparsePropsSet
        // sparsePropsSet is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        sparsePropsMap = new int[PropNames.LAST_PROPERTY_INDEX + 1];
        Arrays.fill(sparsePropsMap, -1);
        numProps = propsets.cardinality();
        sparseIndices = new int[numProps];
        int propx = 0;
        for (int next = propsets.nextSetBit(0);
                next >= 0;
                next = propsets.nextSetBit(next + 1)) {
            sparseIndices[propx] = next;
            sparsePropsMap[next] = propx++;
        }
    }

    /**
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XmlEvent</tt> that triggered the creation of
     * this node
     */
    public FoRegionBeforeAfter
                (FOTree foTree, int foType, FONode parent, XmlEvent event)
        throws TreeException, FOPException
    {
        super(foTree, foType, parent, event, FONode.LAYOUT_SET,
                sparsePropsMap, sparseIndices);
    }

}
