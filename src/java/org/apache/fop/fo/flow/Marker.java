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

package org.apache.fop.fo.flow;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.FOPropertyMapping;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.PropertyListMaker;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.Property;

/**
 * Marker formatting object.
 */
public class Marker extends FObjMixed {
    // The value of properties relevant for fo:marker.
    private String markerClassName;
    // End of property values

    private PropertyListMaker savePropertyListMaker;
    private HashMap descendantPropertyLists = new HashMap();

    /**
     * Create a marker fo.
     * @param parent the parent fo node
     */
    public Marker(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        if (findAncestor(FO_FLOW) < 0) {
            invalidChildError(locator, FO_URI, "marker", 
                "An fo:marker is permitted only as the descendant " +
                "of an fo:flow");
        }
        
        markerClassName = pList.get(PR_MARKER_CLASS_NAME).getString();
        
        if (markerClassName == null || markerClassName.equals("")) {
            missingPropertyError("marker-class-name");
        }        
    }
    
    /**
     * retrieve the property list of foNode
     * @param foNode the FO node whose property list is requested
     * @return the MarkerPropertyList of foNode
     */
    protected MarkerPropertyList getPropertyListFor(FONode foNode) {
        return (MarkerPropertyList) 
            descendantPropertyLists.get(foNode);
    }
    
    /** @see org.apache.fop.fo.FONode#startOfNode() */
    protected void startOfNode() {
        FOEventHandler foEventHandler = getFOEventHandler(); 
        // Push a new property list maker which will make MarkerPropertyLists.
        savePropertyListMaker = foEventHandler.getPropertyListMaker();
        foEventHandler.setPropertyListMaker(new PropertyListMaker() {
            public PropertyList make(FObj fobj, PropertyList parentPropertyList) {
                PropertyList pList = new MarkerPropertyList(fobj, parentPropertyList);
                descendantPropertyLists.put(fobj, pList);
                return pList;
            }
        });
    }
    
    /** @see org.apache.fop.fo.FONode#endOfNode() */
    protected void endOfNode() throws FOPException {
        super.endOfNode();
        // Pop the MarkerPropertyList maker.
        getFOEventHandler().setPropertyListMaker(savePropertyListMaker);
        savePropertyListMaker = null;
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: (#PCDATA|%inline;|%block;)*
     * Additionally: "An fo:marker may contain any formatting objects that 
     * are permitted as a replacement of any fo:retrieve-marker that retrieves
     * the fo:marker's children."
     * @todo implement "additional" constraint, possibly within fo:retrieve-marker
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
        if (!isBlockOrInlineItem(nsURI, localName)) {
            invalidChildError(loc, nsURI, localName);
        }
    }
    
    protected boolean inMarker() {
        return true;
    }
    
    /**
     * Return the "marker-class-name" property.
     */
    public String getMarkerClassName() {
        return markerClassName;
    }

    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "marker";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_MARKER;
    }

    /** @see java.lang.Object#toString() */
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(" {").append(getMarkerClassName()).append("}");
        return sb.toString();
    }
    
    /**
     * An implementation of PropertyList which only stores the explicitly
     * specified properties/attributes as bundles of name-value-namespace
     * strings
     */
    protected class MarkerPropertyList extends PropertyList 
            implements Attributes {
        
        protected class MarkerAttribute {
            
            protected String namespace;
            protected String qname;
            protected String name;
            protected String value;
            
            /**
             * Main constructor
             * @param namespace the namespace URI
             * @param qname the qualified name
             * @param name  the name
             * @param value the value
             */
            public MarkerAttribute(String namespace, String qname, 
                    String name, String value) {
                this.namespace = namespace;
                this.qname = qname;
                this.name = (name == null ? qname : name);
                this.value = value;
            }
            
            /**
             * Convenience constructor for FO attributes
             * @param name  the attribute name
             * @param value the attribute value
             */
            public MarkerAttribute(String name, String value) {
                this.namespace = null;
                this.qname = name;
                this.name = name;
                this.value = value;
            }
        }
        
        /** the array of attributes **/
        private MarkerAttribute[] attribs;
        
        /**
         * Overriding default constructor
         * 
         * @param fobj  the FObj to attach
         * @param parentPropertyList    ignored
         */
        public MarkerPropertyList(FObj fobj, PropertyList parentPropertyList) {
            /* ignore parentPropertyList
             * won't be used because the attributes will be stored
             * without resolving
             */
            super(fobj, null);
        }
        
        /**
         * Override that doesn't convert the attributes to Property instances,
         * but simply stores the attributes for later processing;
         * 
         * @see org.apache.fop.fo.PropertyList#addAttributesToList(Attributes)
         */
        public void addAttributesToList(Attributes attributes) 
                    throws ValidationException {
            
            this.attribs = new MarkerAttribute[attributes.getLength()];

            String name;
            String value;
            String namespace;
            String qname;
            
            for (int i = attributes.getLength(); --i >= 0;) {
                namespace = attributes.getURI(i);
                qname = attributes.getQName(i);
                name = attributes.getLocalName(i);
                value = attributes.getValue(i);
                
                this.attribs[i] = 
                    new MarkerAttribute(namespace, qname, name, value);
            }
        }
        
        /**
         * Null implementation; not used by this type of PropertyList
         * @see org.apache.fop.fo.PropertyList#putExplicit(int, Property)
         */
        public void putExplicit(int propId, Property value) {
            //nop
        }

        /**
         * Null implementation; not used by this type of PropertyList
         * @see org.apache.fop.fo.PropertyList#getExplicit(int)
         */
        public Property getExplicit(int propId) {
            return null;
        }

        /**
         * @see org.xml.sax.Attributes#getLength()
         */
        public int getLength() {
            if (attribs == null) {
                return 0;
            } else {
                return attribs.length;
            }
        }

        /**
         * @see org.xml.sax.Attributes#getURI()
         */
        public String getURI(int index) {
            if (attribs != null 
                    && index < attribs.length
                    && index >= 0
                    && attribs[index] != null) {
                return attribs[index].namespace;
            } else {
                return null;
            }
        }

        /**
         * @see org.xml.sax.Attributes#getLocalName()
         */
        public String getLocalName(int index) {
            if (attribs != null 
                    && index < attribs.length
                    && index >= 0
                    && attribs[index] != null) {
                return attribs[index].name;
            } else {
                return null;
            }
        }

        /**
         * @see org.xml.sax.Attributes#getQName()
         */
        public String getQName(int index) {
            if (attribs != null 
                    && index < attribs.length
                    && index >= 0
                    && attribs[index] != null) {
                return attribs[index].qname;
            } else {
                return null;
            }
        }

        /**
         * Default implementation; not used
         * @see org.xml.sax.Attributes#getType()
         */
        public String getType(int index) {
            return "CDATA";
        }

        /**
         * @see org.xml.sax.Attributes#getValue()
         */
        public String getValue(int index) {
            if (attribs != null 
                    && index < attribs.length
                    && index >= 0
                    && attribs[index] != null) {
                return attribs[index].value;
            } else {
                return null;
            }
        }

        /**
         * @see org.xml.sax.Attributes#getIndex()
         */
        public int getIndex(String name, String namespace) {
            int index = -1;
            if (attribs != null && name != null && namespace != null) {
                for (int i = attribs.length; --i >= 0;) {
                    if (attribs[i] != null
                            && namespace.equals(attribs[i].namespace)
                            && name.equals(attribs[i].name)) {
                        break;
                    }
                }
            }
            return index;
        }

        /**
         * @see org.xml.sax.Attributes#getIndex()
         */
        public int getIndex(String qname) {
            int index = -1;
            if (attribs != null && qname != null) {
                for (int i = attribs.length; --i >= 0;) {
                    if (attribs[i] != null 
                            && qname.equals(attribs[i].qname)) {
                        break;
                    }
                }
            }
            return index;
        }

        /**
         * Default implementation; not used
         * @see org.xml.sax.Attributes#getType()
         */
        public String getType(String name, String namespace) {
            return "CDATA";
        }

        /**
         * Default implementation; not used
         * @see org.xml.sax.Attributes#getType()
         */
        public String getType(String qname) {
            return "CDATA";
        }

        /**
         * @see org.xml.sax.Attributes#getValue()
         */
        public String getValue(String name, String namespace) {
            int index = getIndex(name, namespace);
            if (index > 0) {
                return getValue(index);
            }
            return null;
        }

        /**
         * @see org.xml.sax.Attributes#getValue()
         */
        public String getValue(String qname) {
            int index = getIndex(qname);
            if (index > 0) {
                return getValue(index);
            }
            return null;
        }
    }
}