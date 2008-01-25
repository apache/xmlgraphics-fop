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

import java.util.List;

public class DefaultEventBroadcaster implements EventBroadcaster {

    private List listeners = new java.util.ArrayList();
    
    /** {@inheritDoc} */
    public void addFopEventListener(FopEventListener listener) {
        this.listeners.add(listener);
    }

    /** {@inheritDoc} */
    public void removeFopEventListener(FopEventListener listener) {
        this.listeners.remove(listener);
    }

    /** {@inheritDoc} */
    public int getListenerCount() {
        return this.listeners.size();
    }
    
    /** {@inheritDoc} */
    public void broadcastEvent(FopEvent event) {
        for (int i = 0, c = getListenerCount(); i < c; i++) {
            FopEventListener listener = (FopEventListener)this.listeners.get(i);
            listener.processEvent(event);
        }
    }

}
