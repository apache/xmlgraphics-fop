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
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.FOPropertySets;
import org.apache.fop.fo.PropertySets;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.Tree;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Ints;

import java.util.HashMap;
import java.util.BitSet;

/**
 * Implements the fo:simple-page-master flow object
 */
public class FoRegionEnd extends FoRegionStartEnd {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** The array of applicable properties. Slots in this array are indexed
        by values from the <i>applicableProps</i> <tt>HashMap</tt>.
        It is effectively a sparse array of the set of properties. */
    private PropertyValue[] applicableProps;

    /**
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XMLEvent</tt> that triggered the creation of
     * this node
     */
    public FoRegionEnd(FOTree foTree, FONode parent, FoXMLEvent event)
        throws Tree.TreeException, FOPException
    {
        super(foTree, FObjectNames.REGION_END, parent, event);
        applicableProps = new PropertyValue[numProps];
        FoXMLEvent ev = xmlevents.getEndElement(event);
    }

    /**
     * Get a property value applying to this FO node.
     * @param property - the <tt>int</tt> property index.
     * @return the <tt>PropertyValue</tt> with the indicated property index.
     */
    public PropertyValue getPropertyValue(int property)
            throws PropertyException
    {
        return applicableProps[
            ((Integer)
            (applicablePropsHash.get(Ints.consts.get(property)))).intValue()
        ];
    }
}
