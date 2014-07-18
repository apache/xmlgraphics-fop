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

package org.apache.fop;

import org.apache.fop.layoutmgr.ElementListObserver;
import org.apache.fop.logging.LoggingElementListObserver;

/**
 * Handles some standard tasks for debugging.
 */
public final class DebugHelper {

    private DebugHelper() {
    }

    private static boolean elObserversRegistered;

    /**
     * Registers the default element list observers used for debugging.
     */
    public static void registerStandardElementListObservers() {
        if (!elObserversRegistered) {
            ElementListObserver.addObserver(new LoggingElementListObserver());
            elObserversRegistered = true;
        }
    }

}
