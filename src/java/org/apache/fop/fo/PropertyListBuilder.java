/*
 * $Id: PropertyListBuilder.java,v 1.35 2003/03/05 21:48:02 jeremias Exp $
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
import org.apache.fop.fo.Property.*;

public class PropertyListBuilder {

    /**
     * Name of font-size property attribute to set first.
     */
    private static final String FONTSIZEATTR = "font-size";

    private HashMap propertyListTable;
    private HashMap elementTable;

    public PropertyListBuilder() {
        this.propertyListTable = new HashMap();
        this.elementTable = new HashMap();
    }

    public void addList(HashMap list) {
        propertyListTable.putAll(list);
    }

    public void addElementList(String element, HashMap list) {
        elementTable.put(element, list);
    }

    public Property computeProperty(PropertyList propertyList, String space,
                                    String element, String propertyName) {

        Property p = null;
        Property.Maker propertyMaker = findMaker(space, element,
                                                 propertyName);
        if (propertyMaker != null) {
            try {
                p = propertyMaker.compute(propertyList);
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

    public boolean isInherited(String space, String element,
                               String propertyName) {
        boolean b;

        Property.Maker propertyMaker = findMaker(space, element,
                                                 propertyName);
        if (propertyMaker != null) {
            b = propertyMaker.isInherited();
        } else {
            // log.error("Unknown property " + propertyName);
            b = true;
        }
        return b;
    }

    /**
     *
     * @param nameSpaceURI URI for the namespace of the element to which
     *     the attributes belong.
     * @param elementName Local name for the element to which the attributes
     *     belong.
     * @param attributes Collection of attributes passed to us from the parser.
     * @param fo The FObj to which the attributes need to be attached as
     *     properties.
     * @return PropertyList object containing collection of Properties objects
     *     appropriate for the FObj
     * @throws FOPException
     */
    public PropertyList makeList(String nameSpaceURI, String elementName,
                                 Attributes attributes,
                                 FObj fo) throws FOPException {
        String nameSpaceURIToUse = "http://www.w3.org/TR/1999/XSL/Format";
        if (nameSpaceURI != null) {
            nameSpaceURIToUse = nameSpaceURI;
        }
        FObj parentFO = fo.findNearestAncestorFObj();
        PropertyList parentProperties = null;
        if (parentFO != null) {
            parentProperties = parentFO.getPropertiesForNamespace(nameSpaceURIToUse);
        }

        PropertyList p = new PropertyList(parentProperties, nameSpaceURIToUse,
                                          elementName);
        p.setBuilder(this);
        HashMap validProperties;
        validProperties = (HashMap)elementTable.get(elementName);

        /*
         * If font-size is set on this FO, must set it first, since
         * other attributes specified in terms of "ems" depend on it.
         */
        /** @todo When we do "shorthand" properties, must handle the "font"
         * property as well to see if font-size is set.
         */
        String attributeName = FONTSIZEATTR;
        String attributeValue = attributes.getValue(attributeName);
        convertAttributeToProperty(attributes, attributeName, attributeValue,
                                   validProperties, p, parentFO);

        for (int i = 0; i < attributes.getLength(); i++) {
            attributeName = attributes.getQName(i);
            attributeValue = attributes.getValue(i);
            convertAttributeToProperty(attributes, attributeName, attributeValue,
                                       validProperties, p, parentFO);
        }
        return p;
    }

    /**
     *
     * @param attributes Collection of attributes
     * @param attributeName Attribute name to convert
     * @param attributeValue Attribute value to assign to property
     * @param validProperties Collection of valid properties
     * @param propList PropertyList in which to add the newly created Property
     * @param parentFO Parent FO of the object for which this property is being
     *     built
     */
    private void convertAttributeToProperty(Attributes attributes,
                                            String attributeName,
                                            String attributeValue,
                                            HashMap validProperties,
                                            PropertyList propList,
                                            FObj parentFO) {
        /* Handle "compound" properties, ex. space-before.minimum */
        String basePropertyName = findBasePropertyName(attributeName);
        String subPropertyName = findSubPropertyName(attributeName);

        Property.Maker propertyMaker = findMaker(validProperties, basePropertyName);
        if (propertyMaker == null) {
            handleInvalidProperty(attributeName);
            return;
        }
        if (attributeValue == null) {
            return;
        }
        try {
            Property prop = null;
            if (subPropertyName == null) {
                prop = propertyMaker.make(propList, attributeValue, parentFO);
            }
            else {
                Property baseProperty = findBaseProperty(attributes, propList,
                        parentFO, basePropertyName, propertyMaker);
                prop = propertyMaker.make(baseProperty, subPropertyName,
                        propList, attributeValue, parentFO);
            }
            if (prop != null) {
                propList.put(basePropertyName, prop);
            }
        }
        catch (FOPException e) {
            /**@todo log this exception */
            // log.error(e.getMessage());
        }
    }

    private Property findBaseProperty(Attributes attributes,
                                      PropertyList propertyList,
                                      FObj parentFO,
                                      String basePropName,
                                      Maker propertyMaker)
            throws FOPException {
        // If the baseProperty has already been created, return it
        Property baseProperty = propertyList.getExplicitBaseProp(basePropName);
        if (baseProperty != null) {
            return baseProperty;
        }
        // If it is specified later in this list of Attributes, create it
        String basePropertyValue = attributes.getValue(basePropName);
        if (basePropertyValue != null) {
            baseProperty = propertyMaker.make(propertyList, basePropertyValue,
                    parentFO);
            return baseProperty;
        }
        // Otherwise it is a compound property ??
        // baseProperty = propertyMaker.makeCompound(propertyList, parentFO);
        return baseProperty;
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
    public static String findBasePropertyName(String attributeName) {
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
    public static String findSubPropertyName(String attributeName) {
        int sepCharIndex = attributeName.indexOf('.');
        String subPropName = null;
        if (sepCharIndex > -1) {
            subPropName = attributeName.substring(sepCharIndex + 1);
        }
        return subPropName;
    }

    public Property getSubpropValue(String space, String element,
                                    String propertyName, Property p,
                                    String subpropName) {
        Property.Maker maker = findMaker(space, element, propertyName);
        if (maker != null) {
            return maker.getSubpropValue(p, subpropName);
        } else {
            return null;
        }
    }


    public boolean isCorrespondingForced(PropertyList propertyList,
                                         String space, String element,
                                         String propertyName) {
        Property.Maker propertyMaker = findMaker(space, element,
                                                 propertyName);
        if (propertyMaker != null) {
            return propertyMaker.isCorrespondingForced(propertyList);
        } else {
            //log.error("no Maker for " + propertyName);
        }
        return false;
    }

    public Property getShorthand(PropertyList propertyList, String space,
                                 String element, String propertyName) {
        Property.Maker propertyMaker = findMaker(space, element,
                                                 propertyName);
        if (propertyMaker != null) {
            return propertyMaker.getShorthand(propertyList);
        } else {
            //log.error("no Maker for " + propertyName);
            return null;
        }
    }


    public Property makeProperty(PropertyList propertyList, String space,
                                 String element,
                                 String propertyName) throws FOPException {

        Property p = null;

        Property.Maker propertyMaker = findMaker(space, element,
                                                 propertyName);
        if (propertyMaker != null) {
            p = propertyMaker.make(propertyList);
        } else {
            //log.error("property " + propertyName
            //                       + " ignored");
        }
        return p;
    }

    protected Property.Maker findMaker(String space, String elementName,
                                       String propertyName) {
        return findMaker((HashMap)elementTable.get(elementName),
                         propertyName);
    }

    /**
     * Convenience function to return the Maker for a given property
     * given the HashMap containing properties specific to this element.
     * If table is non-null and
     * @param elemTable Element-specific properties or null if none.
     * @param propertyName Name of property.
     * @return A Maker for this property.
     */
    private Property.Maker findMaker(HashMap elemTable,
                                     String propertyName) {
        Property.Maker propertyMaker = null;
        if (elemTable != null) {
            propertyMaker = (Property.Maker)elemTable.get(propertyName);
        }
        if (propertyMaker == null) {
            propertyMaker =
                (Property.Maker)propertyListTable.get(propertyName);
        }
        return propertyMaker;
    }

}
