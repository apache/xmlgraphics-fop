/*
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2004 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 * Software Foundation, please see <http://www.apache.org/>.
 * 
 * $Id$
 */

package org.apache.fop.fo.flow;

// FOP
import java.util.Arrays;
import java.util.BitSet;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.FoXmlEvent;
import org.apache.fop.xml.XmlEvent;
import org.apache.fop.xml.XmlEventReader;
import org.apache.fop.xml.XmlEventsArrayBuffer;

/**
 * Implements the fo:marker flow object.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 */
public class FoMarker extends FONode {

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
    
    /**
     * The marker-class-name for this fo:marker
     */
    public final String markerClassName;
    
    /**
     * The buffer which will hold the contents of the fo:marker subtree
     */
    private XmlEventsArrayBuffer buffer;
    
    static {
        // Collect the sets of properties that apply
        BitSet propsets = new BitSet();

        // Map these properties into sparsePropsSet
        // sparsePropsSet is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        sparsePropsMap = new int[PropNames.LAST_PROPERTY_INDEX + 1];
        Arrays.fill(sparsePropsMap, -1);
        numProps = 1;
        sparseIndices = new int[] { PropNames.MARKER_CLASS_NAME };
        sparsePropsMap[PropNames.MARKER_CLASS_NAME] = 0;
    }

    /**
     * Construct an fo:marker node, and buffer the contents for later parsing.
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XmlEvent</tt> that triggered the creation of
     * this node
     * @param stateFlags - passed down from the parent.  Includes the
     * attribute set information.
     */
    public FoMarker
            (FOTree foTree, FONode parent, FoXmlEvent event, int stateFlags)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.MARKER, parent, event,
                          stateFlags, sparsePropsMap, sparseIndices);
        if ((stateFlags & FONode.FLOW) == 0)
            throw new FOPException
                    ("fo:marker must be descendent of fo:flow.");
        NCName ncName;
            try {
            ncName = (NCName)(getPropertyValue(PropNames.MARKER_CLASS_NAME));
        } catch (PropertyException e) {
            throw new FOPException("Cannot find marker-class-name in fo:marker", e);
        } catch (ClassCastException e) {
            throw new FOPException("Wrong PropertyValue type in fo:marker", e);
        }
        markerClassName = ncName.getNCName();

        // all but the marker-class-name property are irrelevant on fo:marker
        makeSparsePropsSet();
        
        // Collect the contents of fo:marker for future processing
        buffer = new XmlEventsArrayBuffer(namespaces);
        XmlEvent ev = xmlevents.getEndElement(
                buffer, XmlEventReader.RETAIN_EV, event);
        // The original START_ELEMENT event is still known to the parent
        // node, which will arrange to relinquish it.  Relinquish
        // the just-returned END_ELEMENT event
        namespaces.relinquishEvent(ev);
        
    }
    
    /**
     * @return the marker subtree buffer
     */
    public XmlEventsArrayBuffer getEventBuffer() {
        return buffer;
    }

}
