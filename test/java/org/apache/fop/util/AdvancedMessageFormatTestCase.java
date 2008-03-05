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

import org.xml.sax.helpers.LocatorImpl;

import org.apache.fop.events.model.EventSeverity;
import org.apache.fop.util.text.AdvancedMessageFormat;

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

        pattern = "Multi-conditional: [case1: {var1}|case2: {var2}|case3: {var3}]";
        format = new AdvancedMessageFormat(pattern);
        
        params = new java.util.HashMap();
        msg = format.format(params);
        assertEquals("Multi-conditional: ", msg);
        
        params.put("var3", "value3");
        msg = format.format(params);
        assertEquals("Multi-conditional: case3: value3", msg);
        params.put("var1", "value1");
        msg = format.format(params);
        assertEquals("Multi-conditional: case1: value1", msg);
    }
    
    public void testObjectFormatting() throws Exception {
        String msg;
        AdvancedMessageFormat format;
        
        String pattern
            = "Here's a Locator: {locator}";
        format = new AdvancedMessageFormat(pattern);

        Map params = new java.util.HashMap();
        LocatorImpl loc = new LocatorImpl();
        loc.setColumnNumber(7);
        loc.setLineNumber(12);
        params.put("locator", loc);
        
        msg = format.format(params);
        assertEquals("Here\'s a Locator: 12:7", msg);
    }
    
    public void testIfFormatting() throws Exception {
        String msg;
        AdvancedMessageFormat format;
        
        format = new AdvancedMessageFormat("You are{isBad,if, not} nice!");

        Map params = new java.util.HashMap();

        params.put("isBad", Boolean.FALSE);
        msg = format.format(params);
        assertEquals("You are nice!", msg);

        params.put("isBad", Boolean.TRUE);
        msg = format.format(params);
        assertEquals("You are not nice!", msg);

        format = new AdvancedMessageFormat("You are{isGood,if, very, not so} nice!");

        params = new java.util.HashMap();

        msg = format.format(params); //isGood is missing
        assertEquals("You are not so nice!", msg);

        params.put("isGood", Boolean.FALSE);
        msg = format.format(params);
        assertEquals("You are not so nice!", msg);

        params.put("isGood", Boolean.TRUE);
        msg = format.format(params);
        assertEquals("You are very nice!", msg);

        format = new AdvancedMessageFormat("You are{isGood,if, very\\, very} nice!");

        params = new java.util.HashMap();

        msg = format.format(params); //isGood is missing
        assertEquals("You are nice!", msg);

        params.put("isGood", Boolean.FALSE);
        msg = format.format(params);
        assertEquals("You are nice!", msg);

        params.put("isGood", Boolean.TRUE);
        msg = format.format(params);
        assertEquals("You are very, very nice!", msg);
    }
    
    public void testEqualsFormatting() throws Exception {
        String msg;
        AdvancedMessageFormat format;
        
        format = new AdvancedMessageFormat(
                "Error{severity,equals,EventSeverity:FATAL,,\nSome explanation!}");

        Map params = new java.util.HashMap();

        params.put("severity", EventSeverity.FATAL);
        msg = format.format(params);
        assertEquals("Error", msg);

        params.put("severity", EventSeverity.WARN);
        msg = format.format(params);
        assertEquals("Error\nSome explanation!", msg);
    }
}
