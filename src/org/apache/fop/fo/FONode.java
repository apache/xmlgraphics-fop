package org.apache.fop.fo;

import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FOAttributes;
import org.apache.fop.fo.FObjects;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.PropertyParser;
import org.apache.fop.fo.expr.PropertyValue;
import org.apache.fop.fo.expr.PropertyValueList;
import org.apache.fop.datastructs.Tree;
import org.apache.fop.datastructs.SyncedCircularBuffer;
import org.apache.fop.apps.FOPException;
import org.apache.fop.xml.XMLEvent;

import org.xml.sax.Attributes;

import java.util.LinkedList;
import java.util.ArrayList;

/*
 * FONode.java
 * Created: Sat Nov 10 01:39:37 2001
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * Class for nodes in the FO tree.
 */

public class FONode extends FOTree.Node{

    /**
     * Constants for the set of attributes of interest with this FONode
     */
    public static final int
        NONE = 0
       ,ROOT = 1
     ,LAYOUT = 2
       ,FLOW = 3
        ;

    /** The <tt>FOTree</tt> of which this node is a member. */
    protected FOTree foTree;
    /** The <tt>XMLEvent</tt> which triggered this node. */
    protected XMLEvent event;
    /** The buffer from which parser events are drawn. */
    protected SyncedCircularBuffer xmlevents;
    /** The array of property value stacks */
    protected LinkedList[] propertyStacks;
    /** The attributes defined on this node. */
    public FOAttributes foAttributes;
    /** The property expression parser in the FOTree. */
    protected PropertyParser exprParser;

    /**
     * @param foTree an <tt>FOTree</tt> to which this node belongs
     * @param parent an <tt>FONode</tt>, the parent node of this node in
     * <i>foTree</i>
     * @param event the <tt>XMLEvent</tt> that triggered the creation of this
     * node
     */
    public FONode (FOTree foTree, FONode parent, XMLEvent event, int attrSet)
        throws Tree.TreeException, FOPException, PropertyException
    {
        foTree.super(parent);
        this.foTree = foTree;
        this.event = event;
        xmlevents = foTree.xmlevents;
        propertyStacks = foTree.propertyStacks;
        exprParser = foTree.exprParser;
        foAttributes = new FOAttributes(event);
        // Process the FOAttributes - parse and stack the values
        // font-size is always done first
        String value = foAttributes.getFoAttrValue("font-size");
        if (value != null) { // font-size is defined in the attributes
            // parse the expression
            exprParser.resetParser();
            PropertyValue props =
                    exprParser.parse(PropNames.FONT_SIZE, value);
            // font-size must be a single property value, which may be
            // a percentage
            if (props instanceof PropertyValueList)
                throw new PropertyException
                        ("font-size requires single PropertyValue in " +
                        "PropertyValueList");
            
        }
    }

}// FONode
