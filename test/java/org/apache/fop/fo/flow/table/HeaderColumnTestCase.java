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

package org.apache.fop.fo.flow.table;

import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.AttributesImpl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.fo.ElementMappingRegistry;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FONodeMocks;
import org.apache.fop.fo.FOValidationEventProducer;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.StaticPropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.util.XMLUtil;

/**
 * Tests that the fox:header property is correctly parsed and set up at the FO tree level.
 */
public class HeaderColumnTestCase {

    @Test
    public void testWrongValue() throws ValidationException {
        Table parent = createTableParent();
        EventBroadcaster mockEventBroadcaster = FONodeMocks.mockGetEventBroadcaster(
                parent.getFOEventHandler().getUserAgent());
        FOValidationEventProducer eventProducer = mockGetEventProducerFor(mockEventBroadcaster);
        TableColumn column = new TableColumn(parent);
        PropertyList propertyList = new StaticPropertyList(column, null);
        Attributes atts = createScopeAttribute("blah");
        propertyList.addAttributesToList(atts);
        verify(eventProducer).invalidPropertyValue(any(), eq("fo:table-column"),
                eq("fox:header"), eq("blah"), any(PropertyException.class), any(Locator.class));
    }

    @Test
    public void testCorrectValue() throws Exception {
        testCorrectValue(true);
        testCorrectValue(false);
    }

    private void testCorrectValue(boolean expectedValue) throws Exception {
        Table parent = createTableParent();
        FONodeMocks.mockGetColumnNumberManager(parent);
        TableColumn column = new TableColumn(parent, true);
        PropertyList propertyList = new StaticPropertyList(column, null);
        Attributes atts = createScopeAttribute(String.valueOf(expectedValue));
        propertyList.addAttributesToList(atts);
        column.bind(propertyList);
        assertEquals(expectedValue, column.isHeader());
    }

    private Table createTableParent() {
        Table parent = mock(Table.class);
        FOEventHandler mockFOEventHandler = FONodeMocks.mockGetFOEventHandler(parent);
        FOUserAgent mockUserAgent = mockFOEventHandler.getUserAgent();
        mockGetElementMappingRegistry(mockUserAgent);
        return parent;
    }

    private Attributes createScopeAttribute(String value) {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute(ExtensionElementMapping.URI, "header", "fox:header", XMLUtil.CDATA, value);
        return atts;
    }

    private ElementMappingRegistry mockGetElementMappingRegistry(FOUserAgent mockUserAgent) {
        ElementMappingRegistry mockRegistry = mock(ElementMappingRegistry.class);
        when(mockRegistry.getElementMapping(anyString())).thenReturn(new ExtensionElementMapping());
        when(mockUserAgent.getElementMappingRegistry()).thenReturn(mockRegistry);
        return mockRegistry;
    }

    private FOValidationEventProducer mockGetEventProducerFor(EventBroadcaster mockEventBroadcaster) {
        FOValidationEventProducer mockEventProducer = mock(FOValidationEventProducer.class);
        when(mockEventBroadcaster.getEventProducerFor(eq(FOValidationEventProducer.class)))
                .thenReturn(mockEventProducer);
        return mockEventProducer;
    }

}
