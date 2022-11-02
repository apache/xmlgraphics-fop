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

import java.util.HashSet;
import java.util.Set;

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

    private final Set<String> loggedMessages = new HashSet<String>();

    /**
     * Creates an instance logging to the default log category of this class.
     */
    public LoggingEventListener() {
        this(defaultLog);
    }

    /**
     * Creates an instance logging to a given logger. Events with fatal severity level will be
     * skipped.
     * @param log the target logger
     */
    public LoggingEventListener(Log log) {
        this(log, true);
    }

    /**
     * Creates an instance logging to a given logger.
     * @param log the target logger
     * @param skipFatal true if events with fatal severity level should be skipped (i.e. not logged)
     */
    public LoggingEventListener(Log log, boolean skipFatal) {
        this.log = log;
        this.skipFatal = skipFatal;
    }

    /**
     * Returns the target logger for this instance.
     * @return the target logger
     */
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
            // we want to prevent logging of duplicate messages in situations where they are likely
            // to occur; for instance, warning related to layout do not repeat (since line number
            // will be different) and as such we do not try to filter them here; on the other hand,
            // font related warnings are very likely to repeat and we try to filter them out here;
            // the same may happen with missing images (but not implemented yet).
            String eventGroupID = event.getEventGroupID();
            if (eventGroupID.equals("org.apache.fop.fonts.FontEventProducer")) {
                if (!loggedMessages.contains(msg)) {
                    loggedMessages.add(msg);
                    log.warn(msg);
                }
            } else {
                log.warn(msg);
            }
        } else if (severity == EventSeverity.ERROR) {
            if (event.getParam("e") != null) {
                log.error(msg, (Throwable)event.getParam("e"));
            } else {
                log.error(msg);
            }
        } else if (severity == EventSeverity.FATAL) {
            if (!skipFatal) {
                if (event.getParam("e") != null) {
                    log.fatal(msg, (Throwable)event.getParam("e"));
                } else {
                    log.fatal(msg);
                }
            }
        } else {
            assert false;
        }
    }
}
