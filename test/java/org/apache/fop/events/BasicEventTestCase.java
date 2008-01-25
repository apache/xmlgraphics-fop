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

public class BasicEventTestCase extends TestCase {

    static {
        //Use local event model
        DefaultEventBroadcaster.loadModel(BasicEventTestCase.class, "test-event-model.xml");
    }
    
    public void testBasics() throws Exception {
        
        MyEventListener listener = new MyEventListener();

        EventBroadcaster broadcaster = new DefaultEventBroadcaster();
        broadcaster.addFopEventListener(listener);
        assertEquals(1, broadcaster.getListenerCount());
        
        FopEvent ev = new FopEvent(this, "123",
                FopEvent.paramsBuilder()
                    .param("reason", "I'm tired")
                    .param("blah", new Integer(23))
                    .build());
        broadcaster.broadcastEvent(ev);
        
        ev = listener.event;
        assertNotNull(ev);
        assertEquals("123", listener.event.getEventID());
        assertEquals("I'm tired", ev.getParam("reason"));
        assertEquals(new Integer(23), ev.getParam("blah"));
        
        broadcaster.removeFopEventListener(listener);
        assertEquals(0, broadcaster.getListenerCount());

        //Just check that there are no NPEs
        broadcaster.broadcastEvent(ev);
    }

    public void testEventProducer() throws Exception {
        MyEventListener listener = new MyEventListener();

        EventBroadcaster broadcaster = new DefaultEventBroadcaster();
        broadcaster.addFopEventListener(listener);
        assertEquals(1, broadcaster.getListenerCount());
        
        TestEventProducer producer = (TestEventProducer)broadcaster.getEventProducerFor(
                TestEventProducer.class);
        producer.complain(this, "I'm tired", 23);
        
        FopEvent ev = listener.event;
        assertNotNull(ev);
        assertEquals("org.apache.fop.events.TestEventProducer.complain",
                listener.event.getEventID());
        assertEquals("I'm tired", ev.getParam("reason"));
        assertEquals(new Integer(23), ev.getParam("blah"));
        
        broadcaster.removeFopEventListener(listener);
        assertEquals(0, broadcaster.getListenerCount());

        //Just check that there are no NPEs
        broadcaster.broadcastEvent(ev);
    }
    
    private class MyEventListener implements FopEventListener {

        private FopEvent event;
        
        public void processEvent(FopEvent event) {
            if (this.event != null) {
                fail("Multiple events received");
            }
            this.event = event;
        }
    }

}
