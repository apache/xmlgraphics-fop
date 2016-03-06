/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo;

// Java
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.CommonAbsolutePosition;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonHyphenation;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.CommonTextDecoration;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.PropertyMaker;

/**
 * Class containing the collection of properties for a given FObj.
 */
public abstract class PropertyList {

    private static boolean[] inheritableProperty;

    static {
        inheritableProperty = new boolean[Constants.PROPERTY_COUNT + 1];
        PropertyMaker maker = null;
        for (int prop = 1; prop <= Constants.PROPERTY_COUNT; prop++) {
            maker = findMaker(prop);
            inheritableProperty[prop] = (maker != null && maker.isInherited());
        }
    }

    /** reference to the parent FO's propertyList **/
    protected PropertyList parentPropertyList;
    private FObj fobj;

    private static Log log = LogFactory.getLog(PropertyList.class);

    private final UnknownPropertyHandler unknownPropertyHandler = new UnknownPropertyHandler();

    /**
     * Basic constructor.
     * @param fObjToAttach  the FO this PropertyList should be attached to
     * @param parentPropertyList the PropertyList belonging to the new objects
     * parent
     */
    public PropertyList(FObj fObjToAttach, PropertyList parentPropertyList) {
        this.fobj = fObjToAttach;
        this.parentPropertyList = parentPropertyList;
    }

    /**
     * @return the FObj object to which this propertyList is attached
     */
    public FObj getFObj() {
        return this.fobj;
    }

    /**
     * @return the FObj object attached to the parentPropertyList
     */
    public FObj getParentFObj() {
        if (parentPropertyList != null) {
            return parentPropertyList.getFObj();
        } else {
            return null;
        }
    }

    /**
     * Adds an unknown property value to the property list so that if
     * necessary, a warning can be displayed.
     * @param propertyValue The unknown property value
     * @param output The output of the property to validate
     * @param property The original property containing the full value
     */
    public void validatePropertyValue(String propertyValue, Property output, Property property) {
        unknownPropertyHandler.validatePropertyValue(propertyValue, output, property);
    }

    /**
     * Gets the current list of unknown property values
     * @return The set containing the list of unknown property values
     */
    public Map<String, Property> getUnknownPropertyValues() {
        return unknownPropertyHandler.getUnknownPropertyValues();
    }

    /**
     * @return the FObj object attached to the parentPropetyList
     */
    public PropertyList getParentPropertyList() {
        return parentPropertyList;
    }

    /**
     * Return the value explicitly specified on this FO.
     * @param propId The id of the property whose value is desired.
     * @return The value if the property is explicitly set or set by
     * a shorthand property, otherwise null.
     * @throws PropertyException ...
     */
    public Property getExplicitOrShorthand(int propId) throws PropertyException {
        /* Handle request for one part of a compound property */
        Property p = getExplicit(propId);
        if (p == null) {
            p = getShorthand(propId);
        }
        return p;
    }

    /**
     * A class to handle unknown shorthand property values e.g. border="solit 1pt"
     */
    private static class UnknownPropertyHandler {

        /**
         * A list of unknown properties identified by the value and property in which its featured
         */
        private Map<String, Property> unknownPropertyValues = new HashMap<String, Property>();

        /**
         * A list of known properties which have already been processed
         */
        private Set<Property> knownProperties = new HashSet<Property>();

        void validatePropertyValue(String propertyValue, Property output, Property property) {
            if (!knownProperties.contains(property) && output == null) {
                if (propertyValue != null) {
                    unknownPropertyValues.put(propertyValue, property);
                }
            } else {
                knownProperties.add(property);
            }
        }

        Map<String, Property> getUnknownPropertyValues() {
            return unknownPropertyValues;
        }
    }

    /**
     * Return the value explicitly specified on this FO.
     * @param propId The ID of the property whose value is desired.
     * @return The value if the property is explicitly set, otherwise null.
     */
    public abstract Property getExplicit(int propId);

    /**
     * Set an value defined explicitly on this FO.
     * @param propId The ID of the property to set.
     * @param value The value of the property.
     */
    public abstract void putExplicit(int propId, Property value);

    /**
     * Return the value of this property inherited by this FO.
     * Implements the inherited-property-value function.
     * The property must be inheritable!
     * @param propId The ID of the property whose value is desired.
     * @return The inherited value, otherwise null.
     * @throws PropertyException ...
     */
    public Property getInherited(int propId) throws PropertyException {

        if (isInherited(propId)) {
            return getFromParent(propId);
        } else {
            // return the "initial" value
            return makeProperty(propId);
        }
    }

    /**
     * Return the property on the current FlowObject. If it isn't set explicitly,
     * this will try to compute it based on other properties, or if it is
     * inheritable, to return the inherited value. If all else fails, it returns
     * the default value.
     * @param propId The Constants ID of the property whose value is desired.
     * @return the Property corresponding to that name
     * @throws PropertyException if there is a problem evaluating the property
     */
    public Property get(int propId) throws PropertyException {
        return get(propId, true, true);
    }

    /**
     * Return the property on the current FlowObject. Depending on the passed flags,
     * this will try to compute it based on other properties, or if it is
     * inheritable, to return the inherited value. If all else fails, it returns
     * the default value.
     * @param propId    the property's id
     * @param bTryInherit   true for inherited properties, or when the inherited
     *                      value is needed
     * @param bTryDefault   true when the default value may be used as a last resort
     * @return the property
     * @throws PropertyException if there is a problem evaluating the property
     */
    public Property get(int propId, boolean bTryInherit,
                         boolean bTryDefault) throws PropertyException {

        PropertyMaker propertyMaker = findMaker(propId & Constants.PROPERTY_MASK);
        if (propertyMaker != null) {
            return propertyMaker.get(propId & Constants.COMPOUND_MASK, this,
                                         bTryInherit, bTryDefault);
        }
        return null;
    }

    /**
     * Return the "nearest" specified value for the given property.
     * Implements the from-nearest-specified-value function.
     * @param propId The ID of the property whose value is desired.
     * @return The computed value if the property is explicitly set on some
     * ancestor of the current FO, else the initial value.
     * @throws PropertyException if there an error occurred when getting the property
     */
    public Property getNearestSpecified(int propId) throws PropertyException {
        Property p = null;
        PropertyList pList = parentPropertyList;

        while (pList != null) {
            p = pList.getExplicit(propId);
            if (p != null) {
                return p;
            } else {
                pList = pList.parentPropertyList;
            }
        }

        // If no explicit value found on any of the ancestor-nodes,
        // return initial (default) value.
        return makeProperty(propId);
    }

    /**
     * Return the value of this property on the parent of this FO.
     * Implements the from-parent function.
     * @param propId The Constants ID of the property whose value is desired.
     * @return The computed value on the parent or the initial value if this
     * FO is the root or is in a different namespace from its parent.
     * @throws PropertyException ...
     */
    public Property getFromParent(int propId) throws PropertyException {
        if (parentPropertyList != null) {
            return parentPropertyList.get(propId);
        } else {
            return makeProperty(propId);
        }
    }

    /**
     * Select a writing mode dependent property ID based on value of writing mode property.
     * @param lrtb the property ID to return under lrtb writingmode.
     * @param rltb the property ID to return under rltb writingmode.
     * @param tbrl the property ID to return under tbrl writingmode.
     * @param tblr the property ID to return under tblr writingmode.
     * @return one of the property IDs, depending on the writing mode.
     */
    public int selectFromWritingMode(int lrtb, int rltb, int tbrl, int tblr) {
        int propID;
        try {
            switch (get(Constants.PR_WRITING_MODE).getEnum()) {
            case Constants.EN_LR_TB:
                propID = lrtb;
                break;
            case Constants.EN_RL_TB:
                propID = rltb;
                break;
            case Constants.EN_TB_RL:
                propID = tbrl;
                break;
            case Constants.EN_TB_LR:
                propID = tblr;
                break;
            default:
            propID = -1;
                break;
            }
        } catch (PropertyException e) {
            propID = -1;
        }
        return propID;
    }

    private String addAttributeToList(Attributes attributes,
                                    String attributeName) throws ValidationException {
        String attributeValue = attributes.getValue(attributeName);
        if (attributeValue != null) {
            convertAttributeToProperty(attributes, attributeName, attributeValue);
        }
        return attributeValue;
    }

    /**
     * <p>Adds the attributes, passed in by the parser to the PropertyList.</p>
     * <p>Note that certain attributes are given priority in terms of order of
     * processing due to conversion dependencies, where the order is as follows:</p>
     * <ol>
     * <li>writing-mode</li>
     * <li>column-number</li>
     * <li>number-columns-spanned</li>
     * <li>font</li>
     * <li>font-size</li>
     * <li><emph>all others in order of appearance</emph></li>
     * </ol>
     *
     * @param attributes Collection of attributes passed to us from the parser.
     * @throws ValidationException if there is an attribute that does not
     *          map to a property id (strict validation only)
     */
    public void addAttributesToList(Attributes attributes)
                    throws ValidationException {
        /*
         * Give writing-mode highest conversion priority.
         */
        addAttributeToList(attributes, "writing-mode");

        /*
         * If column-number/number-columns-spanned are specified, then we
         * need them before all others (possible from-table-column() on any
         * other property further in the list...
         */
        addAttributeToList(attributes, "column-number");
        addAttributeToList(attributes, "number-columns-spanned");

        /*
         * If font-size is set on this FO, must set it first, since
         * other attributes specified in terms of "ems" depend on it.
         */
        String checkValue = addAttributeToList(attributes, "font");
        if (checkValue == null || "".equals(checkValue)) {
            /*
             * font shorthand wasn't specified, so still need to process
             * explicit font-size
             */
            addAttributeToList(attributes, "font-size");
        }

        String attributeNS;
        String attributeName;
        String attributeValue;
        FOUserAgent userAgent = getFObj().getUserAgent();
        for (int i = 0; i < attributes.getLength(); i++) {
            /* convert all attributes with the same namespace as the fo element
             * the "xml:lang" and "xml:base" properties are special cases */
            attributeNS = attributes.getURI(i);
            attributeName = attributes.getQName(i);
            attributeValue = attributes.getValue(i);
            if (attributeNS == null || attributeNS.length() == 0
                    || "xml:lang".equals(attributeName)
                    || "xml:base".equals(attributeName)) {
                convertAttributeToProperty(attributes, attributeName, attributeValue);
            } else if (!userAgent.isNamespaceIgnored(attributeNS)) {
                ElementMapping mapping = userAgent.getElementMappingRegistry().getElementMapping(
                        attributeNS);
                QName attr = new QName(attributeNS, attributeName);
                if (mapping != null) {
                    if (mapping.isAttributeProperty(attr)
                            && mapping.getStandardPrefix() != null) {
                        convertAttributeToProperty(attributes,
                                mapping.getStandardPrefix() + ":" + attr.getLocalName(),
                                attributeValue);
                    } else {
                        getFObj().addForeignAttribute(attr, attributeValue);
                    }
                } else {
                    handleInvalidProperty(attr);
                }
            }
        }
    }

    /**
     * Validates a property name.
     * @param propertyName  the property name to check
     * @return true if the base property name and the subproperty name (if any)
     *           can be correctly mapped to an id
     */
    protected boolean isValidPropertyName(String propertyName) {

        int propId = FOPropertyMapping.getPropertyId(
                        findBasePropertyName(propertyName));
        int subpropId = FOPropertyMapping.getSubPropertyId(
                        findSubPropertyName(propertyName));

        return !(propId == -1
                || (subpropId == -1
                && findSubPropertyName(propertyName) != null));
    }

    public Property getPropertyForAttribute(Attributes attributes, String attributeName, String attributeValue)
            throws FOPException {
        if (attributeValue != null) {
            if (attributeName.startsWith("xmlns:") || "xmlns".equals(attributeName)) {
                return null;
            }
            String basePropertyName = findBasePropertyName(attributeName);
            String subPropertyName = findSubPropertyName(attributeName);

            int propId = FOPropertyMapping.getPropertyId(basePropertyName);
            int subpropId = FOPropertyMapping.getSubPropertyId(subPropertyName);

            if (propId == -1 || (subpropId == -1 && subPropertyName != null)) {
                return null;
            }

            return getExplicit(propId);
        }
        return null;
    }

    /**
     *
     * @param attributes Collection of attributes
     * @param attributeName Attribute name to convert
     * @param attributeValue Attribute value to assign to property
     * @throws ValidationException in case the property name is invalid
     *          for the FO namespace
     */
    private void convertAttributeToProperty(Attributes attributes,
                                            String attributeName,
                                            String attributeValue)
                    throws ValidationException {

        if (attributeName.startsWith("xmlns:")
                || "xmlns".equals(attributeName)) {
            /* Ignore namespace declarations if the XML parser/XSLT processor
             * reports them as 'regular' attributes */
            return;
        }

        if (attributeValue != null) {
            /* Handle "compound" properties, ex. space-before.minimum */
            String basePropertyName = findBasePropertyName(attributeName);
            String subPropertyName = findSubPropertyName(attributeName);

            int propId = FOPropertyMapping.getPropertyId(basePropertyName);
            int subpropId = FOPropertyMapping.getSubPropertyId(subPropertyName);

            if (propId == -1
                    || (subpropId == -1 && subPropertyName != null)) {
                handleInvalidProperty(new QName(null, attributeName));
            }
            FObj parentFO = fobj.findNearestAncestorFObj();

            PropertyMaker propertyMaker = findMaker(propId);
            if (propertyMaker == null) {
                log.warn("No PropertyMaker registered for " + attributeName
                        + ". Ignoring property.");
                return;
            }

            try {
                Property prop = null;
                if (subPropertyName == null) { // base attribute only found
                    /* Do nothing if the base property has already been created.
                     * This is e.g. the case when a compound attribute was
                     * specified before the base attribute; in these cases
                     * the base attribute was already created in
                     * findBaseProperty()
                     */
                    if (getExplicit(propId) != null) {
                        return;
                    }
                    prop = propertyMaker.make(this, attributeValue, parentFO);
                } else { // e.g. "leader-length.maximum"
                    Property baseProperty
                        = findBaseProperty(attributes, parentFO, propId,
                                basePropertyName, propertyMaker);
                    prop = propertyMaker.make(baseProperty, subpropId,
                            this, attributeValue, parentFO);
                }
                if (prop != null) {
                    putExplicit(propId, prop);
                }
            } catch (PropertyException e) {
                fobj.getFOValidationEventProducer().invalidPropertyValue(this, fobj.getName(),
                        attributeName, attributeValue, e, fobj.locator);
            }
        }
    }

    private Property findBaseProperty(Attributes attributes,
                                      FObj parentFO,
                                      int propId,
                                      String basePropertyName,
                                      PropertyMaker propertyMaker)
            throws PropertyException {

        /* If the baseProperty has already been created, return it
         * e.g. <fo:leader xxxx="120pt" xxxx.maximum="200pt"... />
         */

        Property baseProperty = getExplicit(propId);

        if (baseProperty != null) {
            return baseProperty;
        }

        /* Otherwise If it is specified later in this list of Attributes, create it now
         * e.g. <fo:leader xxxx.maximum="200pt" xxxx="200pt"... />
         */
        String basePropertyValue = attributes.getValue(basePropertyName);

        if (basePropertyValue != null && propertyMaker != null) {
            baseProperty = propertyMaker.make(this, basePropertyValue,
                                              parentFO);
            return baseProperty;
        }

        return null;  // could not find base property
    }

    /**
     * Handles an invalid property.
     * @param attr the invalid attribute
     * @throws ValidationException if an exception needs to be thrown depending on the
     *                  validation settings
     */
    protected void handleInvalidProperty(QName attr)
                    throws ValidationException {
        if (!attr.getQName().startsWith("xmlns")) {
            fobj.getFOValidationEventProducer().invalidProperty(this, fobj.getName(),
                    attr, true, fobj.locator);
        }
    }

    /**
     * Finds the first or base part (up to any period) of an attribute name.
     * For example, if input is "space-before.minimum", should return
     * "space-before".
     * @param attributeName String to be atomized
     * @return the base portion of the attribute
     */
    protected static String findBasePropertyName(String attributeName) {
        int separatorCharIndex = attributeName.indexOf('.');
        String basePropertyName = attributeName;
        if (separatorCharIndex > -1) {
            basePropertyName = attributeName.substring(0, separatorCharIndex);
        }
        return basePropertyName;
    }

    /**
     * Finds the second or sub part (portion past any period) of an attribute
     * name. For example, if input is "space-before.minimum", should return
     * "minimum".
     * @param attributeName String to be atomized
     * @return the sub portion of the attribute
     */
    protected static String findSubPropertyName(String attributeName) {
        int separatorCharIndex = attributeName.indexOf('.');
        String subpropertyName = null;
        if (separatorCharIndex > -1) {
            subpropertyName = attributeName.substring(separatorCharIndex + 1);
        }
        return subpropertyName;
    }

    /**
     * @param propId ID of property
     * @return new Property object
     * @throws PropertyException if there's a problem while processing the property
     */
    private Property getShorthand(int propId) throws PropertyException {
        PropertyMaker propertyMaker = findMaker(propId);

        if (propertyMaker != null) {
            return propertyMaker.getShorthand(this);
        } else {
            //log.error("no Maker for " + propertyName);
            return null;
        }
    }

    /**
     * @param propId ID of property
     * @return new Property object
     * @throws PropertyException if there's a problem while processing the property
     */
    private Property makeProperty(int propId) throws PropertyException {
        PropertyMaker propertyMaker = findMaker(propId);
        if (propertyMaker != null) {
            return propertyMaker.make(this);
        } else {
            //log.error("property " + propertyName
            //                       + " ignored");
        }
        return null;
    }

    /**
     * @param propId ID of property
     * @return isInherited value from the requested Property.Maker
     */
    private boolean isInherited(int propId) {
        return inheritableProperty[propId];
    }

    /**
     * @param propId Id of property
     * @return the Property.Maker for this property
     */
    private static PropertyMaker findMaker(int propId) {
        if (propId < 1 || propId > Constants.PROPERTY_COUNT) {
            return null;
        } else {
            return FObj.getPropertyMakerFor(propId);
        }
    }

    /**
     * Constructs a BorderAndPadding object.
     * @return a BorderAndPadding object
     * @throws PropertyException if there's a problem while processing the properties
     */
    public CommonBorderPaddingBackground getBorderPaddingBackgroundProps()
                throws PropertyException {
        return CommonBorderPaddingBackground.getInstance(this);
    }

    /**
     * Constructs a CommonHyphenation object.
     * @return the CommonHyphenation object
     * @throws PropertyException if there's a problem while processing the properties
     */
    public CommonHyphenation getHyphenationProps() throws PropertyException {
        return CommonHyphenation.getInstance(this);
    }

    /**
     * Constructs a CommonMarginBlock object.
     * @return the CommonMarginBlock object
     * @throws PropertyException if there's a problem while processing the properties
     */
    public CommonMarginBlock getMarginBlockProps() throws PropertyException {
        return new CommonMarginBlock(this);
    }

    /**
     * Constructs a CommonMarginInline object.
     * @return the CommonMarginInline object
     * @throws PropertyException if there's a problem while processing the properties
     */
    public CommonMarginInline getMarginInlineProps() throws PropertyException {
        return new CommonMarginInline(this);
    }

    /**
     * Constructs a CommonAural object.
     * @return the CommonAural object
     * @throws PropertyException if there's a problem while processing the properties
     */
    public CommonAural getAuralProps() throws PropertyException {
        CommonAural props = new CommonAural(this);
        return props;
    }

    /**
     * Constructs a RelativePositionProps objects.
     * @return a RelativePositionProps object
     * @throws PropertyException if there's a problem while processing the properties
     */
    public CommonRelativePosition getRelativePositionProps() throws PropertyException {
        return new CommonRelativePosition(this);
    }

    /**
     * Constructs a CommonAbsolutePosition object.
     * @return the CommonAbsolutePosition object
     * @throws PropertyException if there's a problem while processing the properties
     */
    public CommonAbsolutePosition getAbsolutePositionProps() throws PropertyException {
        return new CommonAbsolutePosition(this);
    }

    /**
     * Constructs a CommonFont object.
     *
     * @return A CommonFont object
     * @throws PropertyException if there's a problem while processing the properties
     */
    public CommonFont getFontProps() throws PropertyException {
        return CommonFont.getInstance(this);
    }

    /**
     * Constructs a CommonTextDecoration object.
     * @return a CommonTextDecoration object
     * @throws PropertyException if there's a problem while processing the properties
     */
    public CommonTextDecoration getTextDecorationProps() throws PropertyException {
        return CommonTextDecoration.createFromPropertyList(this);
    }
}
