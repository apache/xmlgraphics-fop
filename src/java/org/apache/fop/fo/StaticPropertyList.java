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
package org.apache.fop.fo;

import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;

/**
 * A very fast implementation of PropertyList that uses arrays to store
 * the explicit set properties and another array to store cached values.
 */
public class StaticPropertyList extends PropertyList {
    private final Property[] explicit;
    private final Property[] values;
    
    /**
     * Construct a StaticPropertyList. 
     * @param fObjToAttach The FObj object.
     * @param parentPropertyList The parent property list.
     */
    public StaticPropertyList(FObj fObjToAttach, PropertyList parentPropertyList) {
        super(fObjToAttach, parentPropertyList);
        explicit = new Property[Constants.PROPERTY_COUNT + 1];
        values = new Property[Constants.PROPERTY_COUNT + 1];
    }

    /**
     * Return the value explicitly specified on this FO.
     * @param propId The ID of the property whose value is desired.
     * @return The value if the property is explicitly set, otherwise null.
     */
    public Property getExplicit(int propId) {
        return explicit[propId];
    }

    /**
     * Set an value defined explicitly on this FO.
     * @param propId The ID of the property whose value is desired.
     * @param value The value of the property to set.
     */
    public void putExplicit(int propId, Property value) {
        explicit[propId] = value;
        if (values[propId] != null) { // if the cached value is set overwrite it
            values[propId] = value;
        }
    }

    /**
     * Override PropertyList.get() and provides fast caching of previously
     * retrieved property values.
     * @param propId The property ID
     */
    public Property get(int propId, boolean bTryInherit, boolean bTryDefault)
        throws PropertyException 
    {
        Property p = values[propId];
        if (p == null) {
            p = values[propId] = super.get(propId, bTryInherit, bTryDefault);
        }
        return p;
    }
}
