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
package org.apache.fop.accessibility.fo;

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.fo.FOEventHandler;

class Event {
    private List<Event> children = new ArrayList<Event>();
    protected FOEventHandler eventHandler;
    protected Event parent;
    protected boolean hasContent;

    public Event(FO2StructureTreeConverter structureTreeConverter) {
        eventHandler = structureTreeConverter.converter;
    }

    public Event(Event parent) {
        this.parent = parent;
    }

    public void run() {
        if (hasContent()) {
            for (Event e : children) {
                e.run();
            }
        }
        children.clear();
    }

    private boolean hasContent() {
        for (Event e : children) {
            if (e.hasContent()) {
                return true;
            }
        }
        return hasContent;
    }

    public void add(Event child) {
        children.add(child);
    }
}
