/*
 * $Id$
 * 
 *  ============================================================================
 *                    The Apache Software License, Version 1.1
 *  ============================================================================
 *  
 *  Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without modifica-
 *  tion, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of  source code must  retain the above copyright  notice,
 *     this list of conditions and the following disclaimer.
 *  
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  
 *  3. The end-user documentation included with the redistribution, if any, must
 *     include  the following  acknowledgment:  "This product includes  software
 *     developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *     Alternately, this  acknowledgment may  appear in the software itself,  if
 *     and wherever such third-party acknowledgments normally appear.
 *  
 *  4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *     endorse  or promote  products derived  from this  software without  prior
 *     written permission. For written permission, please contact
 *     apache@apache.org.
 *  
 *  5. Products  derived from this software may not  be called "Apache", nor may
 *     "Apache" appear  in their name,  without prior written permission  of the
 *     Apache Software Foundation.
 *  
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 *  APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 *  DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 *  OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 *  ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 *  (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  This software  consists of voluntary contributions made  by many individuals
 *  on  behalf of the Apache Software  Foundation and was  originally created by
 *  James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 *  Software Foundation, please see <http://www.apache.org/>.
 *  
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertySets;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Ints;

import java.util.Arrays;
import java.util.HashMap;
import java.util.BitSet;

/**
 * Implements the fo:multi-properties flow object.
 */
public class FoMultiProperties extends FONode {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** Map of <tt>Integer</tt> indices of <i>sparsePropsSet</i> array.
        It is indexed by the FO index of the FO associated with a given
        position in the <i>sparsePropsSet</i> array. See
        {@link org.apache.fop.fo.FONode#sparsePropsSet FONode.sparsePropsSet}.
     */
    private static final int[] sparsePropsMap;

    /** An <tt>int</tt> array of of the applicable property indices, in
        property index order. */
    private static final int[] sparseIndices;

    /** The number of applicable properties.  This is the size of the
        <i>sparsePropsSet</i> array. */
    private static final int numProps;

    static {
        // Collect the sets of properties that apply
        BitSet propsets = new BitSet();
        propsets.or(PropertySets.accessibilitySet);
        propsets.set(PropNames.ID);

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

    /** The number of multi-property-sets on this FO. */
    private int numSets = 0;

    /**
     * Construct an fo:multi-properties node, and build the
     * fo:multi-properties subtree.
     * <p>Content model for fo:multi-properties: (multi-property-set+,wrapper)
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>FoXMLEvent</tt> that triggered the creation of
     * this node
     * @param stateFlags - passed down from the parent.  Includes the
     * attribute set information.
     */
    public FoMultiProperties
            (FOTree foTree, FONode parent, FoXMLEvent event, int stateFlags)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.MULTI_PROPERTIES, parent, event,
                          stateFlags, sparsePropsMap, sparseIndices);
        FoXMLEvent ev;
        try {
            // Look for one or more multi-property-set
            while ((ev = xmlevents.expectStartElement
                    (FObjectNames.MULTI_PROPERTY_SET,
                                                 XMLEvent.DISCARD_W_SPACE))
                   != null) {
                new FoMultiPropertySet(getFOTree(), this, ev, stateFlags);
                numSets++;
                ev = xmlevents.getEndElement(xmlevents.DISCARD_EV, ev);
                pool.surrenderEvent(ev);
            }

            if (numSets == 0)
                throw new FOPException
                        ("No multi-property-set in multi-properties.");

            // Look for the wrapper
            if ((ev = xmlevents.expectStartElement
                    (FObjectNames.WRAPPER, XMLEvent.DISCARD_W_SPACE))
                   == null)
                throw new FOPException
                        ("No wrapper in multi-properties.");
            new FoWrapper(getFOTree(), this, ev, stateFlags);
            ev = xmlevents.getEndElement(xmlevents.DISCARD_EV, ev);
            pool.surrenderEvent(ev);

            /*
        } catch (NoSuchElementException e) {
            throw new FOPException
                ("Unexpected EOF while processing " + nowProcessing + ".");
            */
        } catch(TreeException e) {
            throw new FOPException("TreeException: " + e.getMessage());
        } catch(PropertyException e) {
            throw new FOPException("PropertyException: " + e.getMessage());
        }

        makeSparsePropsSet();
    }

}
