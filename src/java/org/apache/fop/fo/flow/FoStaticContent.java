/*
 * $Id$
 * 
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
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 */

package org.apache.fop.fo.flow;

// FOP
import java.util.Arrays;
import java.util.BitSet;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOPageSeqNode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.FoXmlEvent;
import org.apache.fop.xml.XmlEvent;
import org.apache.fop.xml.XmlEventReader;
import org.apache.fop.xml.XmlEventsArrayBuffer;

/**
 * Implements the fo:simple-page-master flow object
 */
public class FoStaticContent extends FOPageSeqNode {

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
     * The flow-name for this fo:static-content
     */
    public final String flowName;
    
    /**
     * The buffer which will hold the contents of the fo:static-content subtree
     */
    private XmlEventsArrayBuffer buffer;
    
    static {
        // Collect the sets of properties that apply
        BitSet propsets = new BitSet();
        propsets.set(PropNames.FLOW_NAME);

        // Map these properties into sparsePropsSet
        // sparsePropsSet is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        sparsePropsMap = new int[PropNames.LAST_PROPERTY_INDEX + 1];
        Arrays.fill(sparsePropsMap, -1);
        numProps = 1;
        sparsePropsMap[PropNames.FLOW_NAME] = 0;
        sparseIndices = new int[] { PropNames.FLOW_NAME };
    }

    /**
     * Construct an fo:static-content node, and buffer the contents for later
* parsing
     * subtree.
     * <p>Content model for fo:static-content: (%block;)+
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XmlEvent</tt> that triggered the creation of
     * this node
     */
    public FoStaticContent(FOTree foTree, FONode parent, FoXmlEvent event)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.STATIC_CONTENT, parent, event,
              FONode.STATIC_SET, sparsePropsMap, sparseIndices);
        NCName ncName;
        try {
            ncName = (NCName)(getPropertyValue(PropNames.FLOW_NAME));
        } catch (PropertyException e) {
            throw new FOPException(
                    "Cannot find marker-class-name in fo:marker", e);
        } catch (ClassCastException e) {
            throw new FOPException("Wrong PropertyValue type in fo:marker", e);
        }
        flowName = ncName.getNCName();
        
        // sparsePropsSet cannot be made for static content, because the full
        // ancestor tree of properties must be available for the later
        // resolution of properties in the static-content subtree and any
        // markers which are later attached to the subtree.
        // makeSparsePropsSet();
        
        // Collect the contents of fo:static-content for future processing
        buffer = new XmlEventsArrayBuffer(namespaces);
        XmlEvent ev = xmlevents.getEndElement(
                buffer, XmlEventReader.RETAIN_EV, event);
        // The original START_ELEMENT event is still known to the parent
        // page-sequence, which will arrange to relinquish it.  Relinquish
        // the just-returned END_ELEMENT event
        namespaces.relinquishEvent(ev);
    }
    
    /**
     * @return the static-content subtree buffer
     */
    public XmlEventsArrayBuffer getEventBuffer() {
        return buffer;
    }

    
}
