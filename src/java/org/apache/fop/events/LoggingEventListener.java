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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.events.model.EventSeverity;

/**
 * EventListener implementation that redirects events to Commons Logging. The events are
 * converted to localized messages.
 */
public class LoggingEventListener implements EventListener {

    /** Default logger instance */
    private static Log defaultLog = LogFactory.getLog(LoggingEventListener.class);
    
    private Log log;
    private boolean skipFatal;
    
    public LoggingEventListener() {
        this(defaultLog);
    }
    
    public LoggingEventListener(Log log) {
        this(log, true);
    }
    
    public LoggingEventListener(Log log, boolean skipFatal) {
        this.log = log;
        this.skipFatal = skipFatal;
    }
    
    public Log getLog() {
        return this.log;
    }
    
    /** {@inheritDoc} */
    public void processEvent(Event event) {
        String msg = EventFormatter.format(event);
        EventSeverity severity = event.getSeverity();
        if (severity == EventSeverity.INFO) {
            log.info(msg);
        } else if (severity == EventSeverity.WARN) {
            log.warn(msg);
        } else if (severity == EventSeverity.ERROR) {
            log.error(msg);
        } else if (severity == EventSeverity.FATAL) {
            if (!skipFatal) {
                log.fatal(msg);
            }
        } else {
            assert false;
        }
    }

}
