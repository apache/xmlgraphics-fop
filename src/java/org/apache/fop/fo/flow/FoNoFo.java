/*
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
 * $Id$
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOPageSeqNode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.xml.FoXmlEvent;

/**
 * Implements the fo:no-fo flow object.
 * 
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 */
public class FoNoFo {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** Map of <tt>Integer</tt> indices of <i>sparsePropsSet</i> array.
        It is indexed by the FO index of the FO associated with a given
        position in the <i>sparsePropsSet</i> array.
     */
    //private static final int[] sparsePropsMap;

    /** An <tt>int</tt> array of of the applicable property indices, in
        property index order. */
    //private static final int[] sparseIndices;

    /** The number of applicable properties.  This is the size of the
        <i>sparsePropsSet</i> array. */
    //private static final int numProps;

    /*
    static {
        // Collect the sets of properties that apply
        BitSet propsets = new BitSet();

        // Map these properties into sparsePropsSet
        // sparsePropsSet is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        sparsePropsMap = new HashMap(1);
        Arrays.fill(sparsePropsMap, -1);
        numProps = 1;
        sparseIndices = new int[] { PropNames.NO_PROPERTY };
        sparsePropsMap[PropNames.NO_PROPERTY] = 0;
    }
    */

    /**
     * The non-existent flow object.  If it comes to this, something
     * has gone wrong.  A use may be found for this later.
     * @param foTree the FO tree being built
     * @param pageSequence ancestor of this node
     * @param parent the parent FONode of this node
     * @param event that triggered the creation of
     * this node
     * @param stateFlags - passed down from the parent.  Includes the
     * attribute set information.
     * @throws FOPException, without exception.
     */
    public FoNoFo
            (FOTree foTree, FONode pageSequence, FOPageSeqNode parent,
                    FoXmlEvent event, int stateFlags)
        throws FOPException
    {
        throw new FOPException("No such flow object as fo:no-fo.");
    }

}
