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

package org.apache.fop.fo.flow;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fo.Constants;

/**
 * A class to register and resolve markers.
 */
public final class Markers {

    // IsAny means either IsFirst or IsLast
    private Map<String, Marker> firstQualifyingIsFirst;
    private Map<String, Marker> firstQualifyingIsAny;
    private Map<String, Marker> lastQualifyingIsFirst;
    private Map<String, Marker> lastQualifyingIsLast;
    private Map<String, Marker> lastQualifyingIsAny;

    private static Log log = LogFactory.getLog(Markers.class);

    /**
     * Registers a marker with the position traits set.
     * Only the required markers are kept.
     * For "first-starting-within-page" it adds the markers
     * that are starting only if the marker class name is not
     * already added.
     * For "first-including-carryover" it adds any starting marker
     * if the marker class name is not already added.
     * For "last-starting-within-page" it adds all marks that
     * are starting, replacing earlier markers.
     * For "last-ending-within-page" it adds all markers that
     * are ending, replacing earlier markers.
     *
     * @param marks a map of markers to register
     * @param starting whether the registration happens at the start (true) or end (false) the the area
     * @param isfirst whether it is the first area of the parent LM
     * @param islast whether it is the last area of the parent LM
     */
    public void register(Map<String, Marker> marks, boolean starting, boolean isfirst, boolean islast) {
        // TODO: find way to put the page number in the log tracing

        if (marks == null) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("--" + marks.keySet() + ": " + (starting ? "starting" : "ending")
                    + (isfirst ? ", first" : "") + (islast ? ", last" : ""));
        }

        if (starting) {
            // at the start of the area, register is-first and any areas
            if (firstQualifyingIsAny == null) {
                firstQualifyingIsAny = new HashMap<String, Marker>();
            }
            if (isfirst) {
                if (firstQualifyingIsFirst == null) {
                    firstQualifyingIsFirst = new HashMap<String, Marker>();
                }
                // first on scope: only put in new values, leave current
                Set<Map.Entry<String, Marker>> entries = marks.entrySet();
                for (Map.Entry<String, Marker> entry : entries) {
                    String key = entry.getKey();
                    Marker marker = entry.getValue();
                    if (!firstQualifyingIsFirst.containsKey(key)) {
                        firstQualifyingIsFirst.put(key, marker);
                        if (log.isTraceEnabled()) {
                            log.trace("Adding marker " + key + " to firstQualifyingIsFirst");
                        }
                    }
                    if (!firstQualifyingIsAny.containsKey(key)) {
                        firstQualifyingIsAny.put(key, marker);
                        if (log.isTraceEnabled()) {
                            log.trace("Adding marker " + key + " to firstQualifyingIsAny");
                        }
                    }
                }
                if (lastQualifyingIsFirst == null) {
                    lastQualifyingIsFirst = new HashMap<String, Marker>();
                }
                // last on scope: replace all
                lastQualifyingIsFirst.putAll(marks);
                if (log.isTraceEnabled()) {
                    log.trace("Adding all markers to LastStart");
                }
            } else {
                // first on scope: only put in new values, leave current
                Set<Map.Entry<String, Marker>> entries = marks.entrySet();
                for (Map.Entry<String, Marker> entry : entries) {
                    String key = entry.getKey();
                    Marker marker = entry.getValue();
                    if (!firstQualifyingIsAny.containsKey(key)) {
                        firstQualifyingIsAny.put(key, marker);
                        if (log.isTraceEnabled()) {
                            log.trace("Adding marker " + key + " to firstQualifyingIsAny");
                        }
                    }
                }
            }
        } else {
            // at the end of the area, register is-last and any areas
            if (islast) {
                if (lastQualifyingIsLast == null) {
                    lastQualifyingIsLast = new HashMap<String, Marker>();
                }
                // last on page: replace all
                lastQualifyingIsLast.putAll(marks);
                if (log.isTraceEnabled()) {
                    log.trace("Adding all markers to lastQualifyingIsLast");
                }
            }
            if (lastQualifyingIsAny == null) {
                lastQualifyingIsAny = new HashMap<String, Marker>();
            }
            // last on page: replace all
            lastQualifyingIsAny.putAll(marks);
            if (log.isTraceEnabled()) {
                log.trace("Adding all markers to lastQualifyingIsAny");
            }
        }
    }

    /**
     * Retrieves the best candidate marker for the given position.
     * @param name the key used to register the marker
     * @param pos the retrieval scope position
     * @return a Marker instance
     */
    public Marker resolve(AbstractRetrieveMarker arm) {
        Marker mark = null;
        int pos = arm.getPosition();
        String name = arm.getRetrieveClassName();
        String posName = arm.getPositionLabel();
        String localName = arm.getLocalName();
        switch (pos) {
        case Constants.EN_FSWP: // retrieve-marker
        case Constants.EN_FIRST_STARTING: // retrieve-table-marker
            if (firstQualifyingIsFirst != null) {
                mark = firstQualifyingIsFirst.get(name);
            }
            if (mark == null && firstQualifyingIsAny != null) {
                mark = firstQualifyingIsAny.get(name);
                posName = "FirstAny after " + posName;
            }
            break;
        case Constants.EN_FIC: // retrieve-marker
        case Constants.EN_FIRST_INCLUDING_CARRYOVER: // retrieve-table-marker
            if (firstQualifyingIsAny != null) {
                mark = firstQualifyingIsAny.get(name);
            }
            break;
        case Constants.EN_LSWP: // retrieve-marker
        case Constants.EN_LAST_STARTING: // retrieve-table-marker
            if (lastQualifyingIsFirst != null) {
                mark = lastQualifyingIsFirst.get(name);
            }
            if (mark == null && lastQualifyingIsAny != null) {
                mark = lastQualifyingIsAny.get(name);
                posName = "LastAny after " + posName;
            }
            break;
        case Constants.EN_LEWP: // retrieve-marker
        case Constants.EN_LAST_ENDING: // retrieve-table-marker
            if (lastQualifyingIsLast != null) {
                mark = lastQualifyingIsLast.get(name);
            }
            if (mark == null && lastQualifyingIsAny != null) {
                mark = lastQualifyingIsAny.get(name);
                posName = "LastAny after " + posName;
            }
            break;
        default:
            throw new RuntimeException("Invalid position attribute in " + localName + ".");
        }
        if (log.isTraceEnabled()) {
            // TODO: find way to put the page number here
            log.trace(localName + ": name[" + name + "]; position [" + posName + "]");
        }
        return mark;
    }

    /** Dumps the current marker data to the logger. */
    public void dump() {
        if (log.isTraceEnabled()) {
            log.trace("FirstAny: " + this.firstQualifyingIsAny);
            log.trace("FirstStart: " + this.firstQualifyingIsFirst);
            log.trace("LastAny: " + this.lastQualifyingIsAny);
            log.trace("LastEnd: " + this.lastQualifyingIsLast);
            log.trace("LastStart: " + this.lastQualifyingIsFirst);
        }
    }

}
