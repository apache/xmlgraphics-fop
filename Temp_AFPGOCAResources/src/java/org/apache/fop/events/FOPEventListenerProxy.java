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

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.events.model.EventSeverity;
import org.apache.fop.fo.FOValidationEventProducer;
import org.apache.fop.layoutmgr.BlockLevelEventProducer;

/**
 * EventListener proxy that inspects all events and adjusts severity levels where necessary.
 * For validation events, it reacts on each event based on the strict validation setting in
 * the user agent.
 * For layout events, it reduces the default severity level if FOP signals that it can recover
 * from the event.
 */
public class FOPEventListenerProxy implements EventListener {

    private static final String FOVALIDATION_EVENT_ID_PREFIX
                = FOValidationEventProducer.class.getName();

    private static final String BLOCK_LEVEL_EVENT_ID_PREFIX
                = BlockLevelEventProducer.class.getName();

    private EventListener delegate;
    private FOUserAgent userAgent;

    /**
     * Main constructor.
     * @param delegate the event listener to delegate events to
     * @param userAgent the FO user agent
     */
    public FOPEventListenerProxy(EventListener delegate, FOUserAgent userAgent) {
        this.delegate = delegate;
        this.userAgent = userAgent;
    }

    /** {@inheritDoc} */
    public synchronized void processEvent(Event event) {
        if (event.getEventID().startsWith(FOVALIDATION_EVENT_ID_PREFIX)) {
            Boolean canRecover = (Boolean)event.getParam("canRecover");
            if (Boolean.TRUE.equals(canRecover) && !userAgent.validateStrictly()) {
                //Reduce severity if FOP can recover
                event.setSeverity(EventSeverity.WARN);
            }
        } else if (event.getEventID().startsWith(BLOCK_LEVEL_EVENT_ID_PREFIX)) {
            Boolean canRecover = (Boolean)event.getParam("canRecover");
            if (Boolean.TRUE.equals(canRecover)) {
                //Reduce severity if FOP can recover
                event.setSeverity(EventSeverity.WARN);
            }
        }
        this.delegate.processEvent(event);
    }

}
