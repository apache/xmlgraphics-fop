/*
 * $Id: PropertyList.java,v 1.20 2003/03/05 21:48:01 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fo;

// Java
import java.util.HashMap;
import org.xml.sax.Attributes;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.Property.Maker;
import org.apache.fop.fo.properties.FOPropertyMapping;
import org.apache.fop.fo.properties.WritingMode;


/**
 * Class containing the collection of properties for a given FObj.
 */
public class PropertyList extends HashMap {

    // writing-mode values
    private byte[] wmtable = null;
    private int writingMode;

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

    private static final String[] ABS_NAMES = new String[] {
        "left", "right", "top", "bottom", "height", "width"
    };

    private static final String[] REL_NAMES = new String[] {
        "start", "end", "before", "after", "block-progression-dimension",
        "inline-progression-dimension"
    };

    private static final HashMap WRITING_MODE_TABLES = new HashMap(4);
    {
        WRITING_MODE_TABLES.put(new Integer(WritingMode.LR_TB),    /* lr-tb */
        new byte[] {
            START, END, BEFORE, AFTER, BLOCKPROGDIM, INLINEPROGDIM
        });
        WRITING_MODE_TABLES.put(new Integer(WritingMode.RL_TB),    /* rl-tb */
        new byte[] {
            END, START, BEFORE, AFTER, BLOCKPROGDIM, INLINEPROGDIM
        });
        WRITING_MODE_TABLES.put(new Integer(WritingMode.TB_RL),    /* tb-rl */
        new byte[] {
            AFTER, BEFORE, START, END, INLINEPROGDIM, BLOCKPROGDIM
        });
    }

    private PropertyList parentPropertyList = null;
    private String namespace = "";
    private String elementName = "";
    private FObj fobj = null;

    /**
     * Basic constructor.
     * @param parentPropertyList the PropertyList belonging to the new objects
     * parent
     * @param space name of namespace
     * @param elementName name of element
     */
    public PropertyList(FObj fObjToAttach, PropertyList parentPropertyList,
        String space, String elementName) {
        this.fobj = fObjToAttach;
        this.parentPropertyList = parentPropertyList;
        this.namespace = space;
        this.elementName = elementName;
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
     * @return the namespace of this element
     */
    public String getNameSpace() {
        return namespace;
    }

    /**
     * @return element name for this
     */
    public String getElement() {
        return elementName;
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

        Property p = findProperty(propId & Constants.PROPERTY_MASK, 
                                    bTryInherit);
        if (p == null && bTryDefault) {    // default value for this FO!
            try {
                p = makeProperty(propId & Constants.PROPERTY_MASK);
            } catch (FOPException e) {
                // don't know what to do here
            }
        }

        // if value is inherit then get computed value from
        // parent
        if (p != null && "inherit".equals(p.getSpecifiedValue())) {
            if (this.parentPropertyList != null) {
                p = parentPropertyList.get(propId, true, false);
            }
        }

        if ((propId & Constants.COMPOUND_MASK) != 0 && p != null) {
            return getSubpropValue(p, propId);
        } else {
            return p;
        }
    }

    /*
     * If the property is a relative property with a corresponding absolute
     * value specified, the absolute value is used. This is also true of
     * the inheritance priority (I think...)
     * If the property is an "absolute" property and it isn't specified, then
     * we try to compute it from the corresponding relative property: this
     * happens in computeProperty.
     */
    private Property findProperty(int propId, boolean bTryInherit) {
        Property p = null;

        if (isCorrespondingForced(propId)) {
            p = computeProperty(propId);
        } else {
            p = getExplicitBaseProp(propId);
            if (p == null) {
                p = this.computeProperty(propId);
            }
            if (p == null) {    // check for shorthand specification
                p = getShorthand(propId);
            }
            if (p == null && bTryInherit) {    
                // else inherit (if has parent and is inheritable)
                if (this.parentPropertyList != null
                        && isInherited(propId)) {
                    p = parentPropertyList.findProperty(propId, true);
                }
            }
        }
        return p;
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
     * Uses the stored writingMode.
     * @param absdir an absolute direction (top, bottom, left, right)
     * @return the corresponding writing model relative direction name
     * for the flow object.
     */
    public int wmMap(int lrtb, int rltb, int tbrl) {
        switch (writingMode) {
        case WritingMode.LR_TB: return lrtb;
        case WritingMode.RL_TB: return lrtb;
        case WritingMode.TB_RL: return lrtb;
        }
        return -1;
    }

    /**
     * Uses the stored writingMode.
     * @param absdir an absolute direction (top, bottom, left, right)
     * @return the corresponding writing model relative direction name
     * for the flow object.
     */
    public String wmAbsToRel(int absdir) {
        if (wmtable != null) {
            return REL_NAMES[wmtable[absdir]];
        } else {
            return "";
        }
    }

    /**
     * Uses the stored writingMode.
     * @param reldir a writing mode relative direction (start, end, before, after)
     * @return the corresponding absolute direction name for the flow object.
     */
    public String wmRelToAbs(int reldir) {
        if (wmtable != null) {
            for (int i = 0; i < wmtable.length; i++) {
                if (wmtable[i] == reldir) {
                    return ABS_NAMES[i];
                }
            }
        }
        return "";
    }

    /**
     * Set the writing mode traits for the FO with this property list.
     * @param writingMode the writing-mode property to be set for this object
     */
    public void setWritingMode(int writingMode) {
        this.writingMode = writingMode;
        this.wmtable = (byte[])WRITING_MODE_TABLES.get(new Integer(writingMode));
    }

    /**
     *
     * @param attributes Collection of attributes passed to us from the parser.
     * @throws FOPException If an error occurs while building the PropertyList
     */
    public void addAttributesToList(Attributes attributes) 
        throws FOPException {
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
                                                
        Property.Maker propertyMaker = null;
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
                                      Maker propertyMaker)
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

        Property.Maker maker = findMaker(propId & Constants.PROPERTY_MASK);

        if (maker != null) {
            return maker.getSubpropValue(p, propId & Constants.COMPOUND_MASK);
        } else {
            return null;
        }
    }

    /**
     * @param propId ID of property
     * @return value from the appropriate Property.Maker
     */
    private boolean isCorrespondingForced(int propId) {
                                             
        Property.Maker propertyMaker = findMaker(propId);
        
        if (propertyMaker != null) {
            return propertyMaker.isCorrespondingForced(this);
        } else {
            //log.error("no Maker for " + propertyName);
        }
        return false;
    }

    /**
     * @param propId ID of property
     * @return new Property object
     */
    private Property getShorthand(int propId) {
        Property.Maker propertyMaker = findMaker(propId);
        
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
        Property.Maker propertyMaker = findMaker(propId);

        if (propertyMaker != null) {
            p = propertyMaker.make(this);
        } else {
            //log.error("property " + propertyName
            //                       + " ignored");
        }
        return p;
    }

    /**
     * @param propID ID of property
     * @return the requested Property object
     */
    private Property computeProperty(int propId) {

        Property p = null;
        Property.Maker propertyMaker = findMaker(propId);

        if (propertyMaker != null) {
            try {
                p = propertyMaker.compute(this);
            } catch (FOPException e) {
                //log.error("exception occurred while computing"
                //                       + " value of property '"
                //                       + propertyName + "': "
                //                       + e.getMessage());
            }
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
        boolean b = true;

        Property.Maker propertyMaker = findMaker(propId);
        if (propertyMaker != null) {
            b = propertyMaker.isInherited();
        }
        
        return b;
    }    

    /**
     * @param propId Id of property
     * @return the Property.Maker for this property
     */
    private Property.Maker findMaker(int propId) {

        if (propId < 1 || propId > Constants.PROPERTY_COUNT) {
            return null;
        } else {
            return FObj.propertyListTable[propId];
        }
    }
    
}

