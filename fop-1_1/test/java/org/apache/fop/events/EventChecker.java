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

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Class that checks that an expected event is produced, and only this one.
 */
class EventChecker implements EventListener {

    private final String expectedEventID;

    private final Map<String, Object> expectedParams;

    private boolean eventReceived;

    EventChecker(String expectedEventID, Map<String, Object> expectedParams) {
        this.expectedEventID = expectedEventID;
        this.expectedParams = expectedParams;
    }

    public void processEvent(Event event) {
        // Always create the message to make sure there is no error in the formatting process
        String id = event.getEventID();
        if (id.equals(expectedEventID)) {
            eventReceived = true;
            for (Map.Entry<String, Object> param : expectedParams.entrySet()) {
                assertEquals(event.getParam(param.getKey()), param.getValue());
            }
        }
    }

    void end() {
        if (!eventReceived) {
            fail("Did not received expected event: " + expectedEventID);
        }
    }
}
