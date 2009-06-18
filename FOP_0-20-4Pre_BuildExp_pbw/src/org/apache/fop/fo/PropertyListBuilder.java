/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

import org.apache.fop.fo.properties.*;
import org.apache.fop.svg.*;
import org.apache.fop.datatypes.*;

import org.apache.fop.apps.FOPException;

import org.xml.sax.Attributes;

import java.util.HashMap;

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

    public PropertyList makeList(String ns, String elementName, Attributes attributes,
                                 PropertyList parentPropertyList,
                                 FObj parentFO) throws FOPException {
        String space = "http://www.w3.org/TR/1999/XSL/Format";
        if (ns != null) {
            space = ns;
        }

        PropertyList par = null;
        if (parentPropertyList != null
                && space.equals(parentPropertyList.getNameSpace())) {
            par = parentPropertyList;
        }
        PropertyList p = new PropertyList(par, space,
                                          elementName);
        p.setBuilder(this);
        HashMap table;
        table = (HashMap)elementTable.get(elementName);

        /* Store names of properties already set. */
        StringBuffer propsDone = new StringBuffer(256);
        propsDone.append(' ');

        /*
         * If font-size is set on this FO, must set it first, since
         * other attributes specified in terms of "ems" depend on it.
         * When we do "shorthand" properties, must handle the "font"
         * property as well to see if font-size is set.
         */
        String fontsizeval = attributes.getValue(FONTSIZEATTR);
        if (fontsizeval != null) {
            Property.Maker propertyMaker = findMaker(table, FONTSIZEATTR);
            if (propertyMaker != null) {
                try {
                    p.put(FONTSIZEATTR,
                          propertyMaker.make(p, fontsizeval, parentFO));
                } catch (FOPException e) {}
            }
            // Put in the "done" list even if error or no Maker.
            propsDone.append(FONTSIZEATTR + ' ');
        }

        for (int i = 0; i < attributes.getLength(); i++) {
            String attributeName = attributes.getQName(i);
            /* Handle "compound" properties, ex. space-before.minimum */
            int sepchar = attributeName.indexOf('.');
            String propName = attributeName;
            String subpropName = null;
            Property propVal = null;
            if (sepchar > -1) {
                propName = attributeName.substring(0, sepchar);
                subpropName = attributeName.substring(sepchar + 1);
            } else if (propsDone.toString().indexOf(' ' + propName + ' ')
                       != -1) {
                // Already processed this property (base property
                // for a property with sub-components or font-size)
                continue;
            }

            Property.Maker propertyMaker = findMaker(table, propName);

            if (propertyMaker != null) {
                try {
                    if (subpropName != null) {
                        Property baseProp = p.getExplicitBaseProp(propName);
                        if (baseProp == null) {
                            // See if it is specified later in this list
                            String baseValue = attributes.getValue(propName);
                            if (baseValue != null) {
                                baseProp = propertyMaker.make(p, baseValue,
                                                              parentFO);
                                propsDone.append(propName + ' ');
                            }
                            // else baseProp = propertyMaker.makeCompound(p, parentFO);
                        }
                        propVal = propertyMaker.make(baseProp, subpropName,
                                                     p,
                                                     attributes.getValue(i),
                                                     parentFO);
                    } else {
                        propVal = propertyMaker.make(p,
                                                     attributes.getValue(i),
                                                     parentFO);
                    }
                    if (propVal != null) {
                        p.put(propName, propVal);
                    }
                } catch (FOPException e) { /* Do other props. */
                    //log.error(e.getMessage());
                }
            } else {
                if (!attributeName.startsWith("xmlns")) {
                    //log.error("property '"
                    //                       + attributeName + "' ignored");
                }
            }
        }

        return p;
    }

    public Property getSubpropValue(String space, String element,
                                    String propertyName, Property p,
                                    String subpropName) {
        Property.Maker maker = findMaker(space, element, propertyName);
        if (maker != null) {
            return maker.getSubpropValue(p, subpropName);
        } else
            return null;
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
