/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.Map;
import java.util.HashMap;
import org.xml.sax.Attributes;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.CommonAbsolutePosition;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonHyphenation;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.PropertyMaker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class containing the collection of properties for a given FObj.
 */
public class PropertyList extends HashMap {

    // writing-mode values
    private byte[] writingModeTable = null;

    // writing-mode index
    private int writingMode;

    private static boolean[] inheritableProperty;

    // absolute directions and dimensions
    /** constant for direction "left" */
    public static final int LEFT = 0;
    /** constant for direction "right" */
    public static final int RIGHT = 1;
    /** constant for direction "top" */
    public static final int TOP = 2;
    /** constant for direction "bottom" */
    public static final int BOTTOM = 3;
    /** constant for dimension "height" */
    public static final int HEIGHT = 4;
    /** constant for dimension "width" */
    public static final int WIDTH = 5;

    // directions relative to writing-mode
    /** constant for direction "start" */
    public static final int START = 0;
    /** constant for direction "end" */
    public static final int END = 1;
    /** constant for direction "before" */
    public static final int BEFORE = 2;
    /** constant for direction "after" */
    public static final int AFTER = 3;
    /** constant for dimension "block-progression-dimension" */
    public static final int BLOCKPROGDIM = 4;
    /** constant for dimension "inline-progression-dimension" */
    public static final int INLINEPROGDIM = 5;

    private static final String[] ABS_WM_NAMES = new String[] {
        "left", "right", "top", "bottom", "height", "width"
    };

    private static final String[] REL_WM_NAMES = new String[] {
        "start", "end", "before", "after", "block-progression-dimension",
        "inline-progression-dimension"
    };

    private static final HashMap WRITING_MODE_TABLES = new HashMap(4);
    {
        WRITING_MODE_TABLES.put(new Integer(Constants.WritingMode.LR_TB),    /* lr-tb */
        new byte[] {
            START, END, BEFORE, AFTER, BLOCKPROGDIM, INLINEPROGDIM
        });
        WRITING_MODE_TABLES.put(new Integer(Constants.WritingMode.RL_TB),    /* rl-tb */
        new byte[] {
            END, START, BEFORE, AFTER, BLOCKPROGDIM, INLINEPROGDIM
        });
        WRITING_MODE_TABLES.put(new Integer(Constants.WritingMode.TB_RL),    /* tb-rl */
        new byte[] {
            AFTER, BEFORE, START, END, INLINEPROGDIM, BLOCKPROGDIM
        });
    }

    private PropertyList parentPropertyList = null;
    private String namespace = "";
    private FObj fobj = null;

    private Log log = LogFactory.getLog(PropertyList.class);

    /**
     * Cache for properties looked up via maker.findProperty
     * with bTryInherit == true
     */
    private Map cache = new HashMap();

    /**
     * Basic constructor.
     * @param parentPropertyList the PropertyList belonging to the new objects
     * parent
     * @param space name of namespace
     */
    public PropertyList(FObj fObjToAttach, PropertyList parentPropertyList,
        String space) {
        this.fobj = fObjToAttach;
        this.parentPropertyList = parentPropertyList;
        this.namespace = space;
    }

    /**
     * @return the FObj object to which this propertyList is attached
     */
    public FObj getFObj() {
        return this.fobj;
    }

    /**
     * @return the FObj object attached to the parentPropetyList
     */
    public FObj getParentFObj() {
        if (parentPropertyList != null) {
            return parentPropertyList.getFObj();
        } else {
            return null;
        }
    }

    /**
     * @return the FObj object attached to the parentPropetyList
     */
    public PropertyList getParentPropertyList() {
        return parentPropertyList;
    }

    /**
     * @return the namespace of this element
     */
    public String getNameSpace() {
        return namespace;
    }

    /**
     * Return the value explicitly specified on this FO.
     * @param propertyName The name of the property whose value is desired.
     * It may be a compound name, such as space-before.optimum.
     * @return The value if the property is explicitly set or set by
     * a shorthand property, otherwise null.
     */
    public Property getExplicitOrShorthand(int propId) {
        /* Handle request for one part of a compound property */
        Property p = getExplicitBaseProp(propId & Constants.PROPERTY_MASK);
        if (p == null) {
            p = getShorthand(propId & Constants.PROPERTY_MASK);
        }
        if (p != null && (propId & Constants.COMPOUND_MASK) != 0) {
            return getSubpropValue(p, propId);
        }
        return p;
    }

    /**
     * Return the value explicitly specified on this FO.
     * @param propertyName The name of the property whose value is desired.
     * It may be a compound name, such as space-before.optimum.
     * @return The value if the property is explicitly set, otherwise null.
     */
    public Property getExplicit(int propId) {
        String propertyName = FOPropertyMapping.getPropertyName(propId);

        /* Handle request for one part of a compound property */
        if ((propId & Constants.COMPOUND_MASK) != 0) {
            Property p = getExplicitBaseProp(propId & Constants.PROPERTY_MASK);
            if (p != null) {
                return getSubpropValue(p, propId);
            } else {
                return null;
            }
        }
        return (Property) super.get(propertyName);
    }

    /**
     * Return the value explicitly specified on this FO.
     * @param propertyName The name of the base property whose value is desired.
     * @return The value if the property is explicitly set, otherwise null.
     */
    public Property getExplicitBaseProp(int propId) {
        String propertyName = FOPropertyMapping.getPropertyName(propId);
        return (Property) super.get(propertyName);
    }

    /**
     * Return the value of this property inherited by this FO.
     * Implements the inherited-property-value function.
     * The property must be inheritable!
     * @param propID The ID of the property whose value is desired.
     * @return The inherited value, otherwise null.
     */
    public Property getInherited(int propId) {

        if (parentPropertyList != null
                && isInherited(propId)) {
            return parentPropertyList.get(propId);
        } else {
            // return the "initial" value
            try {
                return makeProperty(propId);
            } catch (org.apache.fop.apps.FOPException e) {
                //log.error("Exception in getInherited(): property="
                //                       + propertyName + " : " + e);
            }
        }
        return null;    // Exception in makeProperty!
    }

    /**
     * Return the property on the current FlowObject. If it isn't set explicitly,
     * this will try to compute it based on other properties, or if it is
     * inheritable, to return the inherited value. If all else fails, it returns
     * the default value.
     * @param propId The Constants ID of the property whose value is desired.
     * @return the Property corresponding to that name
     */
    public Property get(int propId) {
        return get(propId, true, true);
    }

    /**
     * Return the property on the current FlowObject. Depending on the passed flags,
     * this will try to compute it based on other properties, or if it is
     * inheritable, to return the inherited value. If all else fails, it returns
     * the default value.
     */
    private Property get(int propId, boolean bTryInherit,
                         boolean bTryDefault) {

        PropertyMaker propertyMaker = findMaker(propId & Constants.PROPERTY_MASK);
        try {
            return propertyMaker.get(propId & Constants.COMPOUND_MASK, this,
                                     bTryInherit, bTryDefault);
        } catch (FOPException exc) {
            fobj.getLogger().error("Error during property processing", exc);
        }
        return null;
    }

    /**
     * Wrapper around PropertyMaker.findProperty using the cache;
     * use this method only if bTryInherit == true.
     * The propertyMaker parameter is there
     * to avoid repeated lookup of the maker
     * in an alternating sequence of calls
     * between findProperty and maker.findProperty.
     * This would not be valid for FO elements
     * which have their own list of property makers,
     * see findMaker(propId).
     * @param propId the ID of the property
     * @param propertyMaker the maker of the property
     * @return the cached property value
     */
    public Property findProperty (int propId, PropertyMaker propertyMaker) 
        throws FOPException {
        Property p;
        if (isInCache(propId)) {
            p = getFromCache(propId);
        } else {
            p = propertyMaker.findProperty(this, true);
            addToCache(propId, p);
        }
        return p;
    }

    /**
     * Add a property value to the cache.
     * The cached value may be null,
     * meaning that no property value has been specified by the user
     * on this FO element or, in the case of inheritable properties,
     * on an ancester FO element.
     * @param propId the ID of the property
     * @param prop the property value being cached
     */
    private void addToCache(int propId, Property prop) {
        String propertyName = FOPropertyMapping.getPropertyName(propId);
        log.trace("PropertyList.addToCache: "
                  + propertyName + ", " + getFObj().getName());
        cache.put(new Integer(propId), prop);
    }

    /**
     * Check whether a property is in the cache.
     * The presence of a key for a property
     * means that a value for this property has been cached.
     * @return whether a property is in the cache
     */
    public boolean isInCache(int propId) {
        // Uncomment one or the other to use/not use the cache
        return cache.containsKey(new Integer(propId));
        // return false;
    }

    /**
     * Retrieve a property from the cache
     * @param propId the ID of the property
     * @return the cached property value
     */
    public Property getFromCache(int propId) {
        Property prop;
        String propertyName = FOPropertyMapping.getPropertyName(propId);
        prop = (Property) cache.get(new Integer(propId));
        log.trace("PropertyList.getFromCache: "
                  + propertyName + ", " + getFObj().getName());
        return prop;
    }

    /**
     * Return the "nearest" specified value for the given property.
     * Implements the from-nearest-specified-value function.
     * @param propertyName The name of the property whose value is desired.
     * @return The computed value if the property is explicitly set on some
     * ancestor of the current FO, else the initial value.
     */
    public Property getNearestSpecified(int propId) {
        Property p = null;

        for (PropertyList plist = this; p == null && plist != null;
                plist = plist.parentPropertyList) {
            p = plist.getExplicit(propId);
        }

        if (p == null) {
            // If no explicit setting found, return initial (default) value.
            try {
                p = makeProperty(propId);
            } catch (FOPException e) {
                //log.error("Exception in getNearestSpecified(): property="
                //                       + propertyName + " : " + e);
            }
        }
        return p;
    }

    /**
     * Return the value of this property on the parent of this FO.
     * Implements the from-parent function.
     * @param propId The Constants ID of the property whose value is desired.
     * @return The computed value on the parent or the initial value if this
     * FO is the root or is in a different namespace from its parent.
     */
    public Property getFromParent(int propId) {

        if (parentPropertyList != null) {
            return parentPropertyList.get(propId);
        } else {
            try {
                return makeProperty(propId);
            } catch (org.apache.fop.apps.FOPException e) {
                //log.error("Exception in getFromParent(): property="
                //                       + propertyName + " : " + e);
            }
        }
        return null;    // Exception in makeProperty!
    }

    /**
     * Set the writing mode traits for the FO with this property list.
     * @param writingMode the writing-mode property to be set for this object
     */
    public void setWritingMode(int writingMode) {
        this.writingMode = writingMode;
        this.writingModeTable = (byte[])WRITING_MODE_TABLES.get(new Integer(writingMode));
    }

    /**
     * Return the "writing-mode" property value. 
     * @return The "writing-mode" property value.
     */
    public int getWritingMode() {
        return writingMode;
    }

    /**
     * Uses the stored writingMode.
     * @param absdir an absolute direction (top, bottom, left, right)
     * @return the corresponding writing model relative direction name
     * for the flow object.
     */
    public int getWritingMode(int lrtb, int rltb, int tbrl) {
        switch (writingMode) {
            case Constants.WritingMode.LR_TB: return lrtb;
            case Constants.WritingMode.RL_TB: return rltb;
            case Constants.WritingMode.TB_RL: return tbrl;
        }
        return -1;
    }


    /**
     * Uses the stored writingMode.
     * @param relativeWritingMode relative direction (start, end, before, after)
     * @return the corresponding absolute direction name for the flow object.
     */
    public String getAbsoluteWritingMode(int relativeWritingMode) {
        if (writingModeTable != null) {
            for (int i = 0; i < writingModeTable.length; i++) {
                if (writingModeTable[i] == relativeWritingMode) {
                    return ABS_WM_NAMES[i];
                }
            }
        }
        return "";
    }

    /**
     *
     * @param attributes Collection of attributes passed to us from the parser.
     * @throws FOPException If an error occurs while building the PropertyList
     */
    public void addAttributesToList(Attributes attributes) {
            /*
             * If font-size is set on this FO, must set it first, since
             * other attributes specified in terms of "ems" depend on it.
             */
            /** @todo When we do "shorthand" properties, must handle the
             *  "font" property as well to see if font-size is set.
             */
            String attributeName = "font-size";
            String attributeValue = attributes.getValue(attributeName);
            convertAttributeToProperty(attributes, attributeName, 
                attributeValue);
    
            for (int i = 0; i < attributes.getLength(); i++) {
                attributeName = attributes.getQName(i);
                attributeValue = attributes.getValue(i);
                convertAttributeToProperty(attributes, attributeName, 
                    attributeValue);
            }
    }

    /**
     *
     * @param attributes Collection of attributes
     * @param attributeName Attribute name to convert
     * @param attributeValue Attribute value to assign to property
     */
    private void convertAttributeToProperty(Attributes attributes,
                                            String attributeName,
                                            String attributeValue) {
                                                
        PropertyMaker propertyMaker = null;
        FObj parentFO = fobj.findNearestAncestorFObj();
        
        /* Handle "compound" properties, ex. space-before.minimum */
        String basePropertyName = findBasePropertyName(attributeName);
        String subPropertyName = findSubPropertyName(attributeName);

        int propId = FOPropertyMapping.getPropertyId(basePropertyName);

        propertyMaker = findMaker(propId);
        if (propertyMaker == null) {
            handleInvalidProperty(attributeName);
            return;
        }
        if (attributeValue == null) {
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
                if (super.get(basePropertyName) != null) {
                    return;
                }
                prop = propertyMaker.make(this, attributeValue, parentFO);
            } else { // e.g. "leader-length.maximum"
                Property baseProperty = findBaseProperty(attributes,
                        parentFO, basePropertyName, propertyMaker);
                int subpropId = FOPropertyMapping.getSubPropertyId(subPropertyName);
                prop = propertyMaker.make(baseProperty, subpropId,
                        this, attributeValue, parentFO);
            }
            if (prop != null) {
                put(basePropertyName, prop);
            }
        } catch (FOPException e) {
            /**@todo log this exception */
            // log.error(e.getMessage());
        }
    }

    private Property findBaseProperty(Attributes attributes,
                                      FObj parentFO,
                                      String basePropName,
                                      PropertyMaker propertyMaker)
            throws FOPException {

        /* If the baseProperty has already been created, return it
         * e.g. <fo:leader xxxx="120pt" xxxx.maximum="200pt"... />
         */

        Property baseProperty = (Property) super.get(basePropName);

        if (baseProperty != null) {
            return baseProperty;
        }

        /* Otherwise If it is specified later in this list of Attributes, create it now
         * e.g. <fo:leader xxxx.maximum="200pt" xxxx="200pt"... />
         */
        String basePropertyValue = attributes.getValue(basePropName);
        
        if (basePropertyValue != null && propertyMaker != null) {
            baseProperty = propertyMaker.make(this, basePropertyValue,
                                              parentFO);
            return baseProperty;
        }
        
        return null;  // could not find base property
    }

    private void handleInvalidProperty(String attributeName) {
        if (!attributeName.startsWith("xmlns")) {
            //log.error("property '"
            //                       + attributeName + "' ignored");
        }
    }

    /**
     * Finds the first or base part (up to any period) of an attribute name.
     * For example, if input is "space-before.minimum", should return
     * "space-before".
     * @param attributeName String to be atomized
     * @return the base portion of the attribute
     */
    private static String findBasePropertyName(String attributeName) {
        int sepCharIndex = attributeName.indexOf('.');
        String basePropName = attributeName;
        if (sepCharIndex > -1) {
            basePropName = attributeName.substring(0, sepCharIndex);
        }
        return basePropName;
    }

    /**
     * Finds the second or sub part (portion past any period) of an attribute
     * name. For example, if input is "space-before.minimum", should return
     * "minimum".
     * @param attributeName String to be atomized
     * @return the sub portion of the attribute
     */
    private static String findSubPropertyName(String attributeName) {
        int sepCharIndex = attributeName.indexOf('.');
        String subPropName = null;
        if (sepCharIndex > -1) {
            subPropName = attributeName.substring(sepCharIndex + 1);
        }
        return subPropName;
    }

    /**
     * @param propId ID of property
     * @param p a Property object
     * @return the sub-property
     */
    private Property getSubpropValue(Property p, int propId) {

        PropertyMaker maker = findMaker(propId & Constants.PROPERTY_MASK);

        if (maker != null) {
            return maker.getSubprop(p, propId & Constants.COMPOUND_MASK);
        } else {
            return null;
        }
    }

    /**
     * @param propId ID of property
     * @return new Property object
     */
    private Property getShorthand(int propId) {
        PropertyMaker propertyMaker = findMaker(propId);
        
        if (propertyMaker != null) {
            return propertyMaker.getShorthand(this);
        } else {
            //log.error("no Maker for " + propertyName);
            return null;
        }
    }

    /**
     * @param propID ID of property
     * @return new Property object
     * @throws FOPException for errors in the input
     */
    private Property makeProperty(int propId) throws FOPException {

        Property p = null;
        PropertyMaker propertyMaker = findMaker(propId);

        if (propertyMaker != null) {
            p = propertyMaker.make(this);
        } else {
            //log.error("property " + propertyName
            //                       + " ignored");
        }
        return p;
    }

    /**
     * @param propId ID of property
     * @return isInherited value from the requested Property.Maker
     */
    private boolean isInherited(int propId) {
        if (inheritableProperty == null) {
            inheritableProperty = new boolean[Constants.PROPERTY_COUNT + 1];
            PropertyMaker maker = null;
            for (int prop = 1; prop <= Constants.PROPERTY_COUNT; prop++) {
                maker = findMaker(prop);
                inheritableProperty[prop] = (maker != null && maker.isInherited());
            }    
        }

        return inheritableProperty[propId];
    }    

    /**
     * @param propId Id of property
     * @return the Property.Maker for this property
     */
    private PropertyMaker findMaker(int propId) {

        if (propId < 1 || propId > Constants.PROPERTY_COUNT) {
            return null;
        } else {
            return FObj.propertyListTable[propId];
        }
    }

    /**
     * Constructs a BorderAndPadding object.
     * @return a BorderAndPadding object
     */
    public CommonBorderPaddingBackground getBorderPaddingBackgroundProps() {
        return new CommonBorderPaddingBackground(this);
    }
    

    
    /**
     * Constructs a HyphenationProps objects.
     * @return a HyphenationProps object
     */
    public CommonHyphenation getHyphenationProps() {
        return new CommonHyphenation();
    }
    
    /**
     * Constructs a MarginProps objects.
     * @return a MarginProps object
     */
    public CommonMarginBlock getMarginBlockProps() {
        return new CommonMarginBlock();
    }
    
    /**
     * Constructs a MarginInlineProps objects.
     * @return a MarginInlineProps object
     */
    public CommonMarginInline getMarginInlineProps() {
        return new CommonMarginInline();
    }
    
    /**
     * Constructs a AccessibilityProps objects. 
     * @return a AccessibilityProps object
     */
    public CommonAccessibility getAccessibilityProps() {
        return new CommonAccessibility();
    }

    /**
     * Constructs a AuralProps objects.
     * @return a AuralProps object
     */
    public CommonAural getAuralProps() {
        CommonAural props = new CommonAural();
        return props;
    }

    /**
     * Constructs a RelativePositionProps objects.
     * @return a RelativePositionProps object
     */
    public CommonRelativePosition getRelativePositionProps() {
        return new CommonRelativePosition();
    }
    
    /**
     * Constructs a AbsolutePositionProps objects.
     * @return a AbsolutePositionProps object
     */
    public CommonAbsolutePosition getAbsolutePositionProps() {
        return new CommonAbsolutePosition();
    }    
    

    /**
     * Constructs a CommonFont object. 
     * @return A CommonFont object
     */
    public CommonFont getFontProps() {
        return new CommonFont(this);
    }
}

