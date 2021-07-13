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

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FOPropertyMapping;
import org.apache.fop.fo.StaticPropertyList;
import org.apache.fop.fo.expr.NCnameProperty;
import org.apache.fop.fo.expr.PropertyException;

public class GenericShorthandParserTestCase {
    @Test
    public void testPropertyValidation() throws PropertyException {
        StaticPropertyList propertyList = new StaticPropertyList(null, null);
        ListProperty listProperty = new ListProperty(new NCnameProperty("thin"));
        listProperty.addProperty(new NCnameProperty("solid"));
        EnumProperty.Maker maker = new EnumProperty.Maker(0);
        maker.addEnum("solid", EnumProperty.getInstance(0, "solid"));
        new GenericShorthandParser().convertValueForProperty(0, listProperty, maker, propertyList);
        Assert.assertTrue(propertyList.getUnknownPropertyValues().isEmpty());
    }

    @Test
    public void testPropertyValidationJustThin() throws PropertyException {
        StaticPropertyList propertyList = new StaticPropertyList(null, null);
        ListProperty listProperty = new ListProperty(new NCnameProperty("thin"));
        propertyList.putExplicit(Constants.PR_BORDER_LEFT, listProperty);
        PropertyMaker borderLeftStyle = FOPropertyMapping.getGenericMappings()[Constants.PR_BORDER_LEFT_STYLE];
        borderLeftStyle.getShorthand(propertyList);
        Assert.assertTrue(propertyList.getUnknownPropertyValues().isEmpty());
    }
}
