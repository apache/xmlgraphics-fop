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

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * A maker which creates 'letter-spacing' and 'word-spacing' properties.
 * These two properties properties are standard space properties with
 * additinal support for the 'normal' enum value.
 */

public class SpacingPropertyMaker extends SpaceProperty.Maker {
    /**
     * Create a maker for [letter|word]-spacing.
     * @param propId the id for property.
     */
    public SpacingPropertyMaker(int propId) {
        super(propId);
    }

    /**
     * Support for the 'normal' value.
     */
    public Property convertProperty(Property p,
                                       PropertyList propertyList,
                                       FObj fo) throws PropertyException {
        if (p.getEnum() == Constants.EN_NORMAL) {
            return p;
        }
        return super.convertProperty(p, propertyList, fo);
    }
}
