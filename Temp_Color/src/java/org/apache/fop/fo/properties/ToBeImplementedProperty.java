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

package org.apache.fop.fo.properties;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;

/**
 * A special property for representing an as yet unimplemented property.
 */
public class ToBeImplementedProperty extends Property {

    /**
     * A to be implemented property maker instance.
     */
    public static class Maker extends PropertyMaker {

        /**
         * Instantiate a to be implemented property maker instance.
         * @param propId a property id
         */
        public Maker(int propId) {
            super(propId);
        }

        /** {@inheritDoc} */
        public Property convertProperty(Property p,
                                        PropertyList propertyList, FObj fo) {
            if (p instanceof ToBeImplementedProperty) {
                return p;
            }

            ToBeImplementedProperty val
                = new ToBeImplementedProperty(getPropId());
            return val;
        }
    }

    /**
     * Constructor
     * @param propId id of Property
     */
    public ToBeImplementedProperty(int propId) {

        //XXX: (mjg@recalldesign.com) This is a bit of a kluge, perhaps an
        //UnimplementedPropertyException or something similar should
        //get thrown here instead.

//         Log log = Hierarchy.getDefaultHierarchy().getLoggerFor("fop");
//         log.warn("property - \"" + propName
//                                + "\" is not implemented yet.");
    }
}

