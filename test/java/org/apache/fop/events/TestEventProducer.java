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

public interface TestEventProducer extends EventProducer {

    /**
     * Complain about something.
     * @param source the event source
     * @param reason the reason for the complaint
     * @param blah the complaint
     * @event.severity WARN
     */
    void complain(Object source, String reason, int blah);

    /**
     * Express joy about something.
     * @param source the event source
     * @param what the cause for the joy
     * @event.severity INFO
     */
    void enjoy(Object source, String what);

    public class Provider {

        public static TestEventProducer get(EventBroadcaster broadcaster) {
            return (TestEventProducer)broadcaster.getEventProducerFor(TestEventProducer.class);
        }
    }

}
