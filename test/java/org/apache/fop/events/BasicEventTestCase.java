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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.fop.events.model.EventSeverity;
import org.junit.Test;

public class BasicEventTestCase {

    @Test
    public void testBasics() throws Exception {

        MyEventListener listener = new MyEventListener();

        EventBroadcaster broadcaster = new DefaultEventBroadcaster();
        broadcaster.addEventListener(listener);
        assertTrue(broadcaster.hasEventListeners());

        Event ev = new Event(this, "123", EventSeverity.INFO,
                Event.paramsBuilder()
                    .param("reason", "I'm tired")
                    .param("blah", new Integer(23))
                    .build());
        broadcaster.broadcastEvent(ev);

        ev = listener.event;
        assertNotNull(ev);
        assertEquals("123", listener.event.getEventID());
        assertEquals(EventSeverity.INFO, listener.event.getSeverity());
        assertEquals("I'm tired", ev.getParam("reason"));
        assertEquals(new Integer(23), ev.getParam("blah"));

        broadcaster.removeEventListener(listener);
        assertFalse(broadcaster.hasEventListeners());

        //Just check that there are no NPEs
        broadcaster.broadcastEvent(ev);
    }

    @Test
    public void testEventProducer() throws Exception {
        MyEventListener listener = new MyEventListener();

        EventBroadcaster broadcaster = new DefaultEventBroadcaster();
        broadcaster.addEventListener(listener);
        assertTrue(broadcaster.hasEventListeners());


        TestEventProducer producer = TestEventProducer.Provider.get(broadcaster);
        producer.complain(this, "I'm tired", 23);

        Event ev = listener.event;
        assertNotNull(ev);
        assertEquals("org.apache.fop.events.TestEventProducer.complain",
                listener.event.getEventID());
        assertEquals(EventSeverity.WARN, listener.event.getSeverity());
        assertEquals("I'm tired", ev.getParam("reason"));
        assertEquals(new Integer(23), ev.getParam("blah"));

        broadcaster.removeEventListener(listener);
        assertFalse(broadcaster.hasEventListeners());

        //Just check that there are no NPEs
        broadcaster.broadcastEvent(ev);
    }

    private class MyEventListener implements EventListener {

        private Event event;

        public void processEvent(Event event) {
            if (this.event != null) {
                fail("Multiple events received");
            }
            this.event = event;
        }
    }

}
