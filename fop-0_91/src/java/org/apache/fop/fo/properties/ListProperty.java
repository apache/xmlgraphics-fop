/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

package org.apache.fop.fo.properties;

import java.util.List;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;

/**
 * Superclass for properties that are lists of other properties
 */
public class ListProperty extends Property {

    /**
     * Inner class for creating instances of ListProperty
     */
    public static class Maker extends PropertyMaker {

        /**
         * @param propId ID of the property for which Maker should be created
         */
        public Maker(int propId) {
            super(propId);
        }

        /**
         * @see PropertyMaker#convertProperty
         */
        public Property convertProperty(Property p,
                                        PropertyList propertyList, FObj fo) {
            if (p instanceof ListProperty) {
                return p;
            } else {
                return new ListProperty(p);
            }
        }

    }

    /** Vector containing the list of sub-properties */
    protected List list = new java.util.Vector();

    /**
     * Simple constructor used by subclasses to do some special processing.
     */
    protected ListProperty() {
        //nop
    }
    
    /**
     * @param prop the first Property to be added to the list
     */
    public ListProperty(Property prop) {
        this();
        addProperty(prop);
    }

    /**
     * Add a new property to the list
     * @param prop Property to be added to the list
     */
    public void addProperty(Property prop) {
        list.add(prop);
    }

    /**
     * @return this.list
     */
    public List getList() {
        return list;
    }

    /**
     * @return this.list cast as an Object
     */
    public Object getObject() {
        return list;
    }

}
