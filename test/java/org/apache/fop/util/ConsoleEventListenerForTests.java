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

import org.apache.fop.events.Event;
import org.apache.fop.events.EventFormatter;
import org.apache.fop.events.EventListener;
import org.apache.fop.events.model.EventSeverity;

/** A simple event listener that writes the events to stdout and sterr. */
public class ConsoleEventListenerForTests implements EventListener {

    private String name;
    
    public ConsoleEventListenerForTests(String name) {
        this.name = name;
    }
    
    /** {@inheritDoc} */
    public void processEvent(Event event) {
        if (this.name != null) {
            System.out.println("Test: " + this.name);
            this.name = null;
        }
        String msg = EventFormatter.format(event);
        EventSeverity severity = event.getSeverity();
        if (severity == EventSeverity.INFO) {
            System.out.println("  [INFO ] " + msg);
        } else if (severity == EventSeverity.WARN) {
            System.out.println("  [WARN ] " + msg);
        } else if (severity == EventSeverity.ERROR) {
            System.out.println("  [ERROR] " + msg);
        } else if (severity == EventSeverity.FATAL) {
            System.out.println("  [FATAL] " + msg);
        } else {
            assert false;
        }
    }
}