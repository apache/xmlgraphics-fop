package org.apache.fop.fo;

import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FOAttributes;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FOPropertySets;
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
import org.apache.fop.messaging.MessageHandler;

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
    /** BitSet of properties for which have been specified on this node. */
    private BitSet specifiedProps =
                                new BitSet(PropNames.LAST_PROPERTY_INDEX + 1);
    /** The <i>attrSet</i> argument. */
    public final int attrSet;
    /** The <tt>ROBitSet</tt> of the <i>attrSet</i> argument. */
    protected ROBitSet attrBitSet;
    /** The <tt>ROBitSet</tt> of inherited properties for the
        <i>attrSet</i> argument. */
    protected ROBitSet inheritedBitSet;
    /** The <tt>ROBitSet</tt> of non-inherited prperties for the
        <i>attrSet</i> argument. */
    protected ROBitSet nonInheritedBitSet;
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
        attrBitSet = FOPropertySets.getAttrROBitSet(attrSet);
        inheritedBitSet = FOPropertySets.getInheritedROBitSet(attrSet);
        nonInheritedBitSet = FOPropertySets.getNonInheritedROBitSet(attrSet);
        xmlevents = foTree.xmlevents;
        namespaces = xmlevents.getNamespaces();
        exprParser = foTree.exprParser;
        propertySet = new PropertyValue[PropNames.LAST_PROPERTY_INDEX + 1];
        foAttributes = new FOAttributes(event, this);
        if ( ! (attrSet == FOPropertySets.MARKER_SET)) {
            processAttributes();
        }
        // Set up the remaining properties.
        for (int prop = inheritedBitSet.nextSetBit(0);
                 prop >= 0;
                 prop = inheritedBitSet.nextSetBit(++prop)) {
            System.out.println("...Setting inherited prop " + prop
                               + " " + PropNames.getPropertyName(prop));
            propertySet[prop] = fromParent(prop);  
        }         

        for (int prop = nonInheritedBitSet.nextSetBit(0);
                 prop >= 0;
                 prop = inheritedBitSet.nextSetBit(++prop))
        {
            propertySet[prop] = foTree.getInitialValue(prop);
        }
        
    }

    private void processAttributes() throws FOPException, PropertyException {
        // Process the FOAttributes - parse and stack the values
        // Build a HashMap of the properties defined on this node
        foProperties = foAttributes.getFoAttrMap();
        numAttrs = foProperties.size();
        if (numAttrs > 0) {
            foKeys = foAttributes.getFoAttrKeys();
        }
        for (int propx = 0; propx < numAttrs; propx++) {
            PropertyValue props;
            int ptype;
            int property;
            int prop = foKeys[propx].intValue();
            if ( ! attrBitSet.get(prop)) {
                MessageHandler.log("Ignoring "
                                   + PropNames.getPropertyName(prop)
                                   + " on "
                                   + FObjectNames.getFOName(type)
                                   + " for attribute set "
                                   + FOPropertySets.getAttrSetName(attrSet)
                                   + ".");
                continue;
            }
            String attrValue = foAttributes.getFoAttrValue(prop);
            props = handleAttrValue(prop, attrValue);
            ptype = props.getType();
            if (ptype != PropertyValue.LIST) { 
                property = props.getProperty();
                stackValue(props);
                // Handle corresponding properties here
                // Update the propertySet
                propertySet[props.getProperty()] = props;
            } else { // a list
                PropertyValue value;
                Iterator propvals = ((PropertyValueList)props).iterator();
                while (propvals.hasNext()) {
                    value = (PropertyValue)(propvals.next());
                    property = value.getProperty();
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
        specifiedProps.set(property);
    }

    private void unstackValues() throws PropertyException {
        for (int prop = specifiedProps.nextSetBit(0);
             prop >=0;
             prop = specifiedProps.nextSetBit(++prop)
             ) {
            PropertyValue value = foTree.popPropertyValue(prop);
            if (value.getStackedBy() != this)
                throw new PropertyException
                        ("Unstacked property not stacked by this node.");
        }
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
     * Get the adjusted value from the parent FO of the source property.
     * @see #fromParent(init,int)
     * @see #getPropertyValue(int)
     * @param property - the index of both target and source properties.
     * @return - the adjusted value from the parent FO node, if it exists.
     * If not, get the adjusted initial value.
     */
    public PropertyValue fromParent(int property)
                throws PropertyException
    {
        return fromParent(property, property);
    }

    /**
     * Get the adjusted <tt>PropertyValue</tt> for the given source property
     * on the parent <tt>FONode</tt>. If this node is not the root,
     * call the <i>getPropertyValue</i> method in the parent node, adjust that
     * that value, and return the adjusted value. Do not set the current
     * value of the property on this node.
     * <p>If this is the root node, return the adjusted initial value for the
     * property.  Do not set the current value of the property on this node.
     * <p>The <b>adjusted value</b> is either the value itself, or, if the
     * value is an unresolved relative length, an <tt>IndirectValue</tt>
     * referring to that unresolved length.
     * Cf. {@link #getPropertyValue(int)}.
     * @param property - the index of the target property.
     * @param sourceProperty - the index of the source property.
     * @return - the computed value from the parent FO node, if it exists.
     * If not, get the adjusted initial value.
     */
    public PropertyValue fromParent(int property, int sourceProperty)
                throws PropertyException
    {
        if (parent != null)
            return IndirectValue.adjustedPropertyValue
                                    (parent.getPropertyValue(sourceProperty));
        else // root
            return IndirectValue.adjustedPropertyValue
                                    (foTree.getInitialValue(sourceProperty));
    }

    public PropertyValue currentFontSize() throws PropertyException {
        return getPropertyValue(PropNames.FONT_SIZE);
    }


    /**
     * Get the adjusted <tt>PropertyValue</tt> for the given property index.
     * <pre>
     * If the property has a value in the node, return that adjusted value.
     * If not, and
     *     if this node is not the root,
     *                     and the property is an inherited property,
     *         call this method in the parent node,
     *         adjust that that value,
     *         set this node's value to the adjusted value,
     *         and return the adjusted value.
     *     else this node is the root, or the property is not inherited
     *         get the adjusted initial value of the property
     *         set the property value in this node to that value,
     *         and return that value.
     * <pre>
     * <p>The <b>adjusted value</b> is either the value itself, or, if the
     * value is an unresolved relative length, an <tt>IndirectValue</tt>
     * referring to that unresolved length.
     * @param index - the property index.
     * @return a <tt>PropertyValue</tt> containing the adjusted property
     * value for the indexed property.
     */
    public PropertyValue getPropertyValue(int property)
                throws PropertyException
    {
        PropertyValue pval;
        if ((pval = propertySet[property]) != null) 
            return IndirectValue.adjustedPropertyValue(pval);
        if (parent != null && PropertyConsts.inheritedProps.get(property))
            return (propertySet[property] =
                               IndirectValue.adjustedPropertyValue
                                        (parent.getPropertyValue(property)));
        else // root
            return (propertySet[property] =
                        IndirectValue.adjustedPropertyValue
                                        (foTree.getInitialValue(property)));
    }

}// FONode
