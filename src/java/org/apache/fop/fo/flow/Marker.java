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

import java.util.Collections;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTreeBuilderContext;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.PropertyListMaker;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.Property;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_marker">
 * <code>fo:marker<code></a> object.
 */
public class Marker extends FObjMixed {
    // The value of properties relevant for fo:marker.
    private String markerClassName;
    // End of property values

    private PropertyListMaker savePropertyListMaker;
    private Map descendantPropertyLists = new java.util.HashMap();

    /**
     * Create a marker fo.
     * 
     * @param parent the parent {@link FONode}
     */
    public Marker(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        if (findAncestor(FO_FLOW) < 0) {
            invalidChildError(locator, getParent().getName(), FO_URI, getName(), 
                "rule.markerDescendantOfFlow");
        }
        
        markerClassName = pList.get(PR_MARKER_CLASS_NAME).getString();
        
        if (markerClassName == null || markerClassName.equals("")) {
            missingPropertyError("marker-class-name");
        }        
    }
    
    /**
     * Retrieve the property list of the given {@link FONode} 
     * descendant
     * 
     * @param foNode the {@link FONode} whose property list is requested
     * @return the {@link MarkerPropertyList} for the given node
     */
    protected MarkerPropertyList getPropertyListFor(FONode foNode) {
        return (MarkerPropertyList) 
            descendantPropertyLists.get(foNode);
    }
    
    /** {@inheritDoc} */
    protected void startOfNode() {
        FOTreeBuilderContext builderContext = getBuilderContext(); 
        // Push a new property list maker which will make MarkerPropertyLists.
        savePropertyListMaker = builderContext.getPropertyListMaker();
        builderContext.setPropertyListMaker(new PropertyListMaker() {
            public PropertyList make(FObj fobj, PropertyList parentPropertyList) {
                PropertyList pList = new MarkerPropertyList(fobj, parentPropertyList);
                descendantPropertyLists.put(fobj, pList);
                return pList;
            }
        });
    }
    
    /** {@inheritDoc} */
    protected void endOfNode() throws FOPException {
        super.endOfNode();
        // Pop the MarkerPropertyList maker.
        getBuilderContext().setPropertyListMaker(savePropertyListMaker);
        savePropertyListMaker = null;
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: (#PCDATA|%inline;|%block;)*
     * <br><i>Additionally: "An fo:marker may contain any formatting objects that
     * are permitted as a replacement of any fo:retrieve-marker that retrieves
     * the fo:marker's children."</i>
     * @todo implement "additional" constraint, possibly within fo:retrieve-marker
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
            throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (!isBlockOrInlineItem(nsURI, localName)) {
                invalidChildError(loc, nsURI, localName);
            }
        }
    }
    
    /** {@inheritDoc} */
    protected boolean inMarker() {
        return true;
    }
    
    /** @return the "marker-class-name" property */
    public String getMarkerClassName() {
        return markerClassName;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "marker";
    }
    
    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_MARKER}
     */
    public int getNameId() {
        return FO_MARKER;
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(" {").append(getMarkerClassName()).append("}");
        return sb.toString();
    }

    /**
     * An implementation of {@link PropertyList} which only stores the explicitly
     * specified properties/attributes as bundles of name-value-namespace
     * strings
     */
    protected class MarkerPropertyList extends PropertyList 
            implements Attributes {
                
        /** the array of attributes **/
        private MarkerAttribute[] attribs;
        
        /**
         * Overriding default constructor
         * 
         * @param fobj  the {@link FObj} to attach
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
         * Override that doesn't convert the attributes to {@link Property}
         * instances, but simply stores the attributes for later processing.
         * 
         * {@inheritDoc}
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
                    MarkerAttribute.getInstance(namespace, qname, name, value);
            }
        }
        
        /** Null implementation; not used by this type of {@link PropertyList} */
        public void putExplicit(int propId, Property value) {
            //nop
        }

        /** Null implementation; not used by this type of {@link PropertyList} */
        public Property getExplicit(int propId) {
            return null;
        }

        /** {@inheritDoc} */
        public int getLength() {
            if (attribs == null) {
                return 0;
            } else {
                return attribs.length;
            }
        }

        /** {@inheritDoc} */
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

        /** {@inheritDoc} */
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

        /** {@inheritDoc} */
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

        /** Default implementation; not used */
        public String getType(int index) {
            return "CDATA";
        }

        /** {@inheritDoc} */
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

        /** {@inheritDoc} */
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

        /** {@inheritDoc} */
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

        /** Default implementation; not used */
        public String getType(String name, String namespace) {
            return "CDATA";
        }

        /** Default implementation; not used */
        public String getType(String qname) {
            return "CDATA";
        }

        /** {@inheritDoc} */
        public String getValue(String name, String namespace) {
            int index = getIndex(name, namespace);
            if (index > 0) {
                return getValue(index);
            }
            return null;
        }

        /** {@inheritDoc} */
        public String getValue(String qname) {
            int index = getIndex(qname);
            if (index > 0) {
                return getValue(index);
            }
            return null;
        }
    }
    
    /** Convenience inner class */
    private static final class MarkerAttribute {
        
        private static Map attributeCache = 
            Collections.synchronizedMap(new java.util.WeakHashMap());

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
        private MarkerAttribute(String namespace, String qname, 
                                    String name, String value) {
            this.namespace = namespace;
            this.qname = qname;
            this.name = (name == null ? qname : name);
            this.value = value;
        }
        
        /**
         * Convenience method, reduces the number
         * of distinct MarkerAttribute instances
         *
         * @param namespace the attribute namespace
         * @param qname the fully qualified name of the attribute
         * @param name  the attribute name
         * @param value the attribute value
         * @return the single MarkerAttribute instance corresponding to 
         *          the name/value-pair
         */
        private static MarkerAttribute getInstance(
                                            String namespace, String qname,
                                            String name, String value) {
            MarkerAttribute newInstance = 
                new MarkerAttribute(namespace, qname, name, value);
            if (attributeCache.containsKey(newInstance)) {
                return (MarkerAttribute) attributeCache.get(newInstance);
            } else {
                attributeCache.put(newInstance, newInstance);
                return newInstance;
            }
        }
        
        /** {@inheritDoc} */
        public boolean equals(Object o) {
            if (o instanceof MarkerAttribute) {
                MarkerAttribute attr = (MarkerAttribute) o;
                return ((attr.namespace == this.namespace)
                            || (attr.namespace != null
                                    && attr.namespace.equals(this.namespace)))
                    && ((attr.qname == this.qname)
                            || (attr.qname != null
                                    && attr.qname.equals(this.qname)))
                    && ((attr.name == this.name)
                            || (attr.name != null
                                    && attr.name.equals(this.name)))
                    && ((attr.value == this.value)
                            || (attr.value != null
                                    && attr.value.equals(this.value)));
            } else {
                return false;
            }
        }
    }
}
