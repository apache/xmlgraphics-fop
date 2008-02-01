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

package org.apache.fop.util;

import java.util.Map;

import junit.framework.TestCase;

/**
 * Tests for EventFormatter.
 */
public class AdvancedMessageFormatTestCase extends TestCase {

    public void testFormatting() throws Exception {
        String msg;
        AdvancedMessageFormat format;
        
        String pattern
            = "Element \"{elementName}\" is missing[ required property \"{propertyName}\"]!";
        format = new AdvancedMessageFormat(pattern);

        Map params = new java.util.HashMap();
        params.put("node", new Object());
        params.put("elementName", "fo:external-graphic");
        params.put("propertyName", "src");
        
        msg = format.format(params);
        assertEquals("Element \"fo:external-graphic\" is missing required property \"src\"!", msg);
        
        params.remove("propertyName");
        msg = format.format(params);
        assertEquals("Element \"fo:external-graphic\" is missing!", msg);
        
        pattern
            = "Testing \\{escaped \\[characters\\], now a normal field {elementName}!";
        format = new AdvancedMessageFormat(pattern);
        msg = format.format(params);
        assertEquals("Testing {escaped [characters], now a normal field fo:external-graphic!", msg);
    }
    
}
