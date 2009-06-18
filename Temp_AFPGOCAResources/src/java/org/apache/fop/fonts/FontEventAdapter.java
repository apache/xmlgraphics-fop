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

package org.apache.fop.fonts;

import java.util.Map;

import org.apache.fop.events.Event;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.model.EventSeverity;

/**
 * Event listener interface for font-related events. This interface extends FontEventListener
 * and EventProducer for integration into FOP's event subsystem.
 */
public class FontEventAdapter implements FontEventListener {

    private EventBroadcaster eventBroadcaster;

    /**
     * Creates a new FontEventAdapter.
     * @param broadcaster the event broadcaster to send the generated events to
     */
    public FontEventAdapter(EventBroadcaster broadcaster) {
        this.eventBroadcaster = broadcaster;
    }

    /**
     * Returns the event group ID.
     * @return the event group ID
     */
    protected String getEventGroupID() {
        return getClass().getName();
    }

    /** {@inheritDoc} */
    public void fontSubstituted(Object source, FontTriplet requested, FontTriplet effective) {
        Map params = new java.util.HashMap();
        params.put("requested", requested);
        params.put("effective", effective);
        Event ev = new Event(source, getEventGroupID() + ".fontSubstituted",
                EventSeverity.WARN, params);
        this.eventBroadcaster.broadcastEvent(ev);
    }

    /** {@inheritDoc} */
    public void fontLoadingErrorAtAutoDetection(Object source, String fontURL, Exception e) {
        Map params = new java.util.HashMap();
        params.put("fontURL", fontURL);
        params.put("e", e);
        Event ev = new Event(source, getEventGroupID() + ".fontLoadingErrorAtAutoDetection",
                EventSeverity.WARN, params);
        this.eventBroadcaster.broadcastEvent(ev);
    }

    /** {@inheritDoc} */
    public void glyphNotAvailable(Object source, char ch, String fontName) {
        Map params = new java.util.HashMap();
        params.put("ch", new Character(ch));
        params.put("fontName", fontName);
        Event ev = new Event(source, getEventGroupID() + ".glyphNotAvailable",
                EventSeverity.WARN, params);
        this.eventBroadcaster.broadcastEvent(ev);
    }

}
