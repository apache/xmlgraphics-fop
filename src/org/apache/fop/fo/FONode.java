package org.apache.fop.fo;

import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FOAttributes;
import org.apache.fop.fo.FObjects;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.PropertyParser;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datastructs.Tree;
import org.apache.fop.datastructs.ROBitSet;
import org.apache.fop.datatypes.indirect.Inherit;
import org.apache.fop.datatypes.indirect.IndirectValue;
import org.apache.fop.apps.FOPException;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.xml.SyncedXmlEventsBuffer;
import org.apache.fop.xml.XMLNamespaces;

import org.xml.sax.Attributes;

import java.util.BitSet;
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

    /** The <tt>FOTree</tt> of which this node is a member. */
    protected FOTree foTree;
    /** The parent <tt>FONode</tt> of this node. */
    protected FONode parent;
    /** The <tt>XMLEvent</tt> which triggered this node. */
    protected XMLEvent event;
    /** The buffer from which parser events are drawn. */
    protected SyncedXmlEventsBuffer xmlevents;
    /** The namespaces object associated with <i>xmlevents</i>. */
    protected XMLNamespaces namespaces;
    /** The FO type. */
    public final int type;
    /** The attributes defined on this node. */
    public FOAttributes foAttributes;
    /** The unmodifiable map of properties defined on this node. */
    public HashMap foProperties = null;
    /** The sorted keys of <i>foProperties</i>. */
    protected Integer[] foKeys = null;
    /** The size of <i>foKeys</i>. */
    private int numAttrs = 0;
    /** The property expression parser in the FOTree. */
    protected PropertyParser exprParser;
    /** The property set for this node. */
    protected PropertyValue[] propertySet;
    /** BitSet of properties for which specified values have been stacked. */
    private BitSet stackedProps =
                                new BitSet(PropNames.LAST_PROPERTY_INDEX + 1);
    /** The <i>attrSet</i> argument. */
    public final int attrSet;
    /** The <tt>ROBitSet</tt> of the <i>attrSet</i> argument. */
    protected ROBitSet nodeAttrBitSet;
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
        this.parent = parent;
        this.event = event;
        this.attrSet = attrSet;
        xmlevents = foTree.xmlevents;
        namespaces = xmlevents.getNamespaces();
        exprParser = foTree.exprParser;
        propertySet = new PropertyValue[PropNames.LAST_PROPERTY_INDEX + 1];
        foAttributes = new FOAttributes(event, this);
        if ( ! (attrSet == FObjects.MARKER_SET)) {
            processAttributes();
        }
    }

    private void processAttributes() throws PropertyException {
        // Process the FOAttributes - parse and stack the values
        // Build a HashMap of the properties defined on this node
        foProperties = foAttributes.getFoAttrMap();
        numAttrs = foProperties.size();
        if (numAttrs > 0) {
            foKeys = foAttributes.getFoAttrKeys();
        }
        for (int propx = 0; propx < numAttrs; propx++) {
            PropertyValue props;
            int type;
            int prop = foKeys[propx].intValue();
            String attrValue = foAttributes.getFoAttrValue(prop);
            props = handleAttrValue(prop, attrValue);
            type = props.getType();
            if (type != PropertyValue.LIST) { 
                stackValue(props);
                // Handle corresponding properties here
                // Update the propertySet
                propertySet[props.getProperty()] = props;
            } else { // a list
                PropertyValue value;
                Iterator propvals = ((PropertyValueList)props).iterator();
                while (propvals.hasNext()) {
                    value = (PropertyValue)(propvals.next());
                    stackValue(value);
                    // Handle corresponding properties here
                    propertySet[value.getProperty()] = value;
                }
            }
        }
    }

    private PropertyValue handleAttrValue(int property, String attrValue)
        throws PropertyException
    {
        // parse the expression
        exprParser.resetParser();
        foTree.args[0] = this;
        foTree.args[1] = exprParser.parse(this, property, attrValue);
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

    private void stackValue(PropertyValue value) throws PropertyException {
        int property = value.getProperty();
        PropertyValue currentValue = foTree.getCurrentPropertyValue(property);
        if (currentValue.getStackedBy() == this)
            foTree.popPropertyValue(property);
        value.setStackedBy(this);
        foTree.pushPropertyValue(value);
        stackedProps.set(property);
    }

    private void unstackValues() throws PropertyException {
        for (int prop = stackedProps.nextSetBit(0);
             prop >=0;
             prop = stackedProps.nextSetBit(++prop)
             ) {
            PropertyValue value = foTree.popPropertyValue(prop);
            if (value.getStackedBy() != this)
                throw new PropertyException
                        ("Unstacked property not stacked by this node.");
        }
    }

    /**
     * Get the parent's <tt>PropertyValue</tt> for the given property.
     * @param property - the property of interest.
     * @return the <tt>PropertyValue</tt> of the parent node.
     */
    public PropertyValue getParentPropertyValue(int property) {
        return parent.propertySet[property];
    }

    /**
     * Get the <tt>PropertyValue</tt> of the nearest ancestor with a
     * specified value for the given property.
     * @param property - the property of interest.
     * @return the nearest specified <tt>PropertyValue</tt>.
     */
    public PropertyValue getNearestSpecifiedValue(int property)
        throws PropertyException
    {
        PropertyValue value = null;
        ArrayList stack = foTree.propertyStacks[property];
        int stackp = stack.size();
        while (stackp-- > 0) {
            value = (PropertyValue)(stack.get(stackp));
            // Following equality can't be the case for initial values,
            // as their stackedBy will be null.
            if (value.getStackedBy() == this) continue;
            return value;
        }
        throw new PropertyException
                ("No specified value in stack for " + property + ": "
                      + PropNames.getPropertyName(property));
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
        PropertyValue value = getNearestSpecifiedValue(sourceProperty);
        // Determine whether an indirect value is required
        if (IndirectValue.isUnresolved(value)) {
            Inherit inherit = new Inherit(property, sourceProperty);
            inherit.setInheritedValue(value);
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
        PropertyValue value = getParentPropertyValue(sourceProperty);
        if (value == null) {
            // No computed value is available.  Use an IndirectValue
            Inherit inherit = new Inherit(property, sourceProperty);
            inherit.setInheritedValue(value);
            return inherit;
        }
        return value;
    }
    
}// FONode
