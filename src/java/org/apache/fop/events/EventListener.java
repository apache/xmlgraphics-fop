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

/**
 * This interface is implemented by clients who want to listen for events.
 */
public interface EventListener extends java.util.EventListener {

    /**
     * This method is called for each event that is generated. With the event's ID it is possible
     * to react to certain events. Events can also simply be recorded and presented to a user.
     * It is possible to throw an (unchecked) exception if the processing needs to be aborted
     * because some special event occured. This way the client can configure the behaviour of
     * the observed application.
     * @param event the event
     */
    void processEvent(Event event);
    
}
