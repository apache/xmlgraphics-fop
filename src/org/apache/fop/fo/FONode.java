package org.apache.fop.fo;

import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FOAttributes;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FOPropertySets;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.PropertyParser;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.TextDecorations;
import org.apache.fop.datatypes.indirect.Inherit;
import org.apache.fop.datatypes.indirect.IndirectValue;
import org.apache.fop.datastructs.Tree;
import org.apache.fop.datastructs.ROBitSet;
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
        // Do not set up the remaining properties now.
        // These will be developed by inheritance or from the initial values
        // as the property values are referenced.
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
                // Update the propertySet
                propertySet[props.getProperty()] = props;
                specifiedProps.set(property);
                // Handle corresponding properties here
            } else { // a list
                PropertyValue value;
                Iterator propvals = ((PropertyValueList)props).iterator();
                while (propvals.hasNext()) {
                    value = (PropertyValue)(propvals.next());
                    property = value.getProperty();
                    propertySet[value.getProperty()] = value;
                    specifiedProps.set(property);
                    // Handle corresponding properties here
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
            throw new PropertyException (e);
        } catch (InvocationTargetException e) {
            throw new PropertyException (e);
        }
    }

    /**
     * Get the adjusted <tt>PropertyValue</tt> of the property
     * on the nearest ancestor with a specified value for that property.
     * @see #fromNearestSpecified(init,int)
     * @see #getNearestSpecifiedValue(int)
     * @param property - the index of both target and source properties.
     * to the PropertyTriplet.
     * @return - the adjusted value corresponding to the nearest specified
     * value if it exists, else the adjusted initial value.
     */
    public PropertyValue fromNearestSpecified(int property)
                throws PropertyException
    {
        return fromNearestSpecified(property, property);
    }

    /**
     * Get the adjusted <tt>PropertyValue</tt> of the source property
     * on the nearest ancestor with a specified value for that property.
     * <p>If this node is not the root, call the
     * <i>getNearestSpecifiedValue</i> method in the parent node, adjust
     * that value, and return the adjusted value. Do not set the current
     * value of the property on this node.
     * <p>If this is the root node, return the adjusted initial value for the
     * property.  Do not set the current value of the property on this node.
     * <p>The <b>adjusted value</b> is either the value itself, or, if the
     * value is an unresolved relative length, an <tt>IndirectValue</tt>
     * referring to that unresolved length.
     * Cf. {@link #getNearestSpecifiedValue(int)}.
     * @param property - the index of the target property.
     * @param sourceProperty - the index of the source property.
     * @return - the adjusted value corresponding to the nearest specified
     * value if it exists, else the adjusted initial value.
     */
    public PropertyValue fromNearestSpecified
                                        (int property, int sourceProperty)
                throws PropertyException
    {
        if (parent != null)
            return IndirectValue.adjustedPropertyValue
                            (parent.getNearestSpecifiedValue(sourceProperty));
        else // root
            return IndirectValue.adjustedPropertyValue
                                    (foTree.getInitialValue(sourceProperty));
    }

    /**
     * Get the adjusted <tt>PropertyValue</tt> of the property on the nearest
     * ancestor with a specified value for the given property.
     * <p>If a value has been specified on this node, return the adjusted
     * value.
     * <p>Otherwise, if the this node is not the root, return the adjusted
     * value from a recursive call.
     * <p>If this is the root node, return the adjusted initial value for the
     * property.
     * <p>The <b>adjusted value</b> is either the value itself, or, if the
     * value is an unresolved relative length, an <tt>IndirectValue</tt>
     * referring to that unresolved length.
     * @param property - the property of interest.
     * @return the adjusted value of the nearest specified
     * <tt>PropertyValue</tt>.
     */
    public PropertyValue getNearestSpecifiedValue(int property)
                throws PropertyException
    {
        if (specifiedProps.get(property))
            return IndirectValue.adjustedPropertyValue(propertySet[property]);
        if (parent != null)
            return IndirectValue.adjustedPropertyValue
                                (parent.getNearestSpecifiedValue(property));
        else // root
            return IndirectValue.adjustedPropertyValue
                                        (foTree.getInitialValue(property));
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
     * call the <i>getPropertyValue</i> method in the parent node, adjust
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


    /**
     * Clone the adjusted <tt>PropertyValue</tt> for the given property index.
     * <p>The <b>adjusted value</b> is either the value itself, or, if the
     * value is an unresolved relative length, an <tt>IndirectValue</tt>
     * referring to that unresolved length.
     * Cf. {@link #getPropertyValue(int)}.
     * @param index - the property index.
     * @return a <tt>PropertyValue</tt> containing a clone of the adjusted
     * property value for the indexed property.
     */
    public PropertyValue clonePropertyValue(int index)
                throws PropertyException
    {
        PropertyValue tmpval = getPropertyValue(index);
        try {
            return (PropertyValue)(tmpval.clone());
        } catch (CloneNotSupportedException e) {
            throw new PropertyException("Clone not supported.");
        }
    }

    /**
     * Get the current font size.  This is a reference to the
     * <tt>PropertyValue</tt> located.
     * @return a <tt>Numeric</tt> containing the current font size
     * @exception PropertyException if current font size is not defined,
     * or is not expressed as a <tt>Numeric</tt>.
     */
    public Numeric currentFontSize() throws PropertyException {
        PropertyValue fontsize = getPropertyValue(PropNames.FONT_SIZE);
        if ( ! (fontsize.getType() == PropertyValue.NUMERIC
                            && ((Numeric)fontsize).isLength()))
            throw new PropertyException
                    ("font-size value is not a length.");
        return (Numeric)fontsize;
    }

    /**
     * Clone the current font size.
     * @return a <tt>Numeric</tt> containing the current font size
     * @exception PropertyException if current font size is not defined,
     * or is not expressed as a <tt>Numeric</tt>, or if cloning is not
     * supported.
     */
    public Numeric cloneCurrentFontSize() throws PropertyException {
        Numeric tmpval = currentFontSize();
        try {
            return (Numeric)(tmpval.clone());
        } catch (CloneNotSupportedException e) {
            throw new PropertyException(e);
        }
    }

    /**
     * Clone the current <i>TextDecorations</i> property.
     * @return a <tt>TextDecorations</tt> object containing the current
     * text decorations
     * @exception PropertyException if current text decorations are not
     * defined, or are not expressed as <tt>TextDecorations</tt>.
     */
    public TextDecorations cloneCurrentTextDecorations()
            throws PropertyException
    {
        PropertyValue textdec = getPropertyValue(PropNames.TEXT_DECORATION);
        if (textdec.getType() != PropertyValue.TEXT_DECORATIONS)
            throw new PropertyException
                ("text-decoration value is not a TextDecorations object.");
        try {
            return (TextDecorations)(textdec.clone());
        } catch (CloneNotSupportedException e) {
            throw new PropertyException("Clone not supported.");
        }
    }

}// FONode
