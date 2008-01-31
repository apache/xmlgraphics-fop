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

package org.apache.fop.events;

import junit.framework.TestCase;

import org.apache.fop.events.model.EventSeverity;

/**
 * Tests for EventFormatter.
 */
public class EventFormatterTestCase extends TestCase {

    public void testFormatting() throws Exception {
        Event ev;
        String msg;
        
        ev = new Event(this, "org.apache.fop.fo.FOValidationEventProducer.missingProperty",
                EventSeverity.FATAL, Event.paramsBuilder()
                .param("node", new Object())
                .param("elementName", "fo:external-graphic")
                .param("propertyName", "src")
                .build());
        String template
            = "Element \"${elementName}\" is missing required property \"${propertyName}\"!";
        msg = EventFormatter.format(ev, template);
        assertEquals("Element \"fo:external-graphic\" is missing required property \"src\"!", msg);
    }
    
}
