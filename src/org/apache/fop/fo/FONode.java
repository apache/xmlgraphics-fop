package org.apache.fop.fo;

import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FOAttributes;
import org.apache.fop.fo.FObjects;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.PropertyParser;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.PropertyTriplet;
import org.apache.fop.datastructs.Tree;
import org.apache.fop.datatypes.indirect.Inherit;
import org.apache.fop.apps.FOPException;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.xml.SyncedXmlEventsBuffer;
import org.apache.fop.xml.XMLNamespaces;

import org.xml.sax.Attributes;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

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

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * Constants for the set of attributes of interest with this FONode
     */
    public static final int
        NONE = 0
       ,ROOT = 1
     ,LAYOUT = 2
    ,PAGESEQ = 3
       ,FLOW = 4
     ,STATIC = 5
     ,MARKER = 6
        ;
    /** The <tt>FOTree</tt> of which this node is a member. */
    protected FOTree foTree;
    /** The <tt>XMLEvent</tt> which triggered this node. */
    protected XMLEvent event;
    /** The buffer from which parser events are drawn. */
    protected SyncedXmlEventsBuffer xmlevents;
    /** The namespaces object associated with <i>xmlevents</i>. */
    protected XMLNamespaces namespaces;
    /** The FO type. */
    public final int type;
    /** The array of property value stacks */
    protected LinkedList[] propertyStacks;
    /** The attributes defined on this node. */
    public FOAttributes foAttributes;
    /** The properties defined on this node. */
    public HashMap foProperties = null;
    /** The property expression parser in the FOTree. */
    protected PropertyParser exprParser;
    /** Ancestor reference area of this FONode. */
    protected FONode ancestorRefArea = null;

    /**
     * @param foTree an <tt>FOTree</tt> to which this node belongs
     * @param type the fo type of this FONode.
     * @param parent an <tt>FONode</tt>, the parent node of this node in
     * <i>foTree</i>
     * @param event the <tt>XMLEvent</tt> that triggered the creation of this
     * @param attrSet the set of attributes relevant at this point in the
     * tree.
     * node
     */
    public FONode
        (FOTree foTree, int type, FONode parent, XMLEvent event, int attrSet)
        throws Tree.TreeException, FOPException, PropertyException
    {
        foTree.super(parent);
        this.foTree = foTree;
        this.type = type;
        this.event = event;
        xmlevents = foTree.xmlevents;
        namespaces = xmlevents.getNamespaces();
        propertyStacks = foTree.propertyStacks;
        exprParser = foTree.exprParser;
        foAttributes = new FOAttributes(event, this);
        if ( ! (attrSet == MARKER)) {
            processProperties();
        }
    }

    private void processProperties() throws PropertyException {
        // Process the FOAttributes - parse and stack the values
        // Build a HashMap of the properties defined on this node
        foProperties = foAttributes.getFoAttrMap();
        PropertyValue props;
        for (int prop = 1; prop <= PropNames.LAST_PROPERTY_INDEX; prop++) {
            String value = foAttributes.getFoAttrValue(prop);
            if (value != null) { // property is defined in the attributes
                props = handleAttrValue(prop, value);
            }
        }
    }

    private PropertyValue handleAttrValue(int property, String attrValue)
        throws PropertyException
    {
        // parse the expression
        exprParser.resetParser();
        foTree.args[0] = foTree;
        foTree.args[1] = exprParser.parse(property, attrValue);
        try {
            return (PropertyValue)
                    (((Method)PropertyConsts.refineParsingMethods
                      .get(property))
                     .invoke(null, foTree.args));
        } catch (IllegalAccessException e) {
            throw new PropertyException (e.getMessage());
        } catch (InvocationTargetException e) {
            throw new PropertyException (e.getMessage());
        }
    }

    /**
     * Get the parent's <tt>PropertyTriplet</tt> for the given property.
     * @param property - the property of interest.
     * @return the <tt>PropertyTriplet</tt> of the parent node.
     */
    public PropertyTriplet getParentTriplet(int property) {
        PropertyTriplet triplet = null;
        LinkedList stack = foTree.propertyStacks[property];
        int size = stack.size();
        int next = size;
        // There must be at least one
        triplet = (PropertyTriplet)(stack.get(--next));
        // Following equality can't be the case for initial values,
        // as their stackedBy will be null.
        if (triplet.getStackedBy() == this) {
            triplet = (PropertyTriplet)(stack.get(--next));
        }
        return triplet;
    }

    /**
     * Get the <tt>PropertyTriplet</tt> of the nearest ancestor with a
     * specified value for the given property.
     * @param property - the property of interest.
     * @return the nearest specified <tt>PropertyTriplet</tt>.
     */
    public PropertyTriplet getNearestSpecifiedTriplet(int property)
        throws PropertyException
    {
        PropertyTriplet triplet = null;
        PropertyValue value = null;
        Iterator stackp = foTree.propertyStacks[property].iterator();
        while (stackp.hasNext()) {
            triplet = (PropertyTriplet)(stackp.next());
            // Following equality can't be the case for initial values,
            // as their stackedBy will be null.
            if (triplet.getStackedBy() == this) continue;
            if ((value = triplet.getSpecified()) != null) break;
        }
        if (value == null)
            throw new PropertyException
                    ("No specified value in stack for " + property + ": "
                      + PropNames.getPropertyName(property));
        return triplet;
    }

    /**
     * Get the computed value from nearest ancestor with a specified value.
     * @param property - the index of both target and source properties.
     * @return - the computed value corresponding to the nearest specified
     * value (which may be the initial value) if it exists.  If no computed
     * value is available, return an <tt>Inherit</tt> object with a reference
     * to the PropertyTriplet.
     */
    public PropertyValue fromNearestSpecified(int property)
                throws PropertyException
    {
        return fromNearestSpecified(property, property);
    }

    /**
     * Get the computed value from nearest ancestor with a specified value.
     * @param property - the index of the target property.
     * @param sourceProperty - the index of the source property.
     * @return - the computed value corresponding to the nearest specified
     * value (which may be the initial value) if it exists.  If no computed
     * value is available, return an <tt>Inherit</tt> object with a reference
     * to the PropertyTriplet.
     */
    public PropertyValue fromNearestSpecified
                                        (int property, int sourceProperty)
                throws PropertyException
    {
        PropertyValue value;
        PropertyTriplet triplet = getNearestSpecifiedTriplet(sourceProperty);
        if ((value = triplet.getComputed()) == null) {
            // No computed value is available.  Use an IndirectValue
            Inherit inherit = new Inherit(property, sourceProperty);
            inherit.setInheritedTriplet(triplet);
            return inherit;
        }
        return value;
    }

    /**
     * Get the computed value from the parent FO of the source property.
     * @param property - the index of both target and source properties.
     * @return - the computed value from the parent FO node, if it exists.
     * If not, get the computed initial value.  If no computed
     * value is available, return an <tt>Inherit</tt> object with a reference
     * to the PropertyTriplet.
     */
    public PropertyValue fromParent(int property)
                throws PropertyException
    {
        return fromParent(property, property);
    }

    /**
     * Get the computed value from the parent FO of the source property.
     * @param property - the index of the target property.
     * @param sourceProperty - the index of the source property.
     * @return - the computed value from the parent FO node, if it exists.
     * If not, get the computed initial value.  If no computed
     * value is available, return an <tt>Inherit</tt> object with a reference
     * to the PropertyTriplet.
     */
    public PropertyValue fromParent(int property, int sourceProperty)
                throws PropertyException
    {
        PropertyTriplet triplet = null;
        PropertyValue value = null;
        triplet = getParentTriplet(sourceProperty);
        if ((value = triplet.getComputed()) == null) {
            // No computed value is available.  Use an IndirectValue
            Inherit inherit = new Inherit(property, sourceProperty);
            inherit.setInheritedTriplet(triplet);
            return inherit;
        }
        return value;
    }
    
}// FONode
