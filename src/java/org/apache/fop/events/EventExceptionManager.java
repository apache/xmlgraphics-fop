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

import java.util.Iterator;
import java.util.Map;

import org.apache.xmlgraphics.util.Service;

/**
 * This class is responsible for converting events into exceptions.
 */
public class EventExceptionManager {

    private static final Map EXCEPTION_FACTORIES = new java.util.HashMap();

    static {
        Iterator iter;
        iter = Service.providers(ExceptionFactory.class, true);
        while (iter.hasNext()) {
            ExceptionFactory factory = (ExceptionFactory)iter.next();
            EXCEPTION_FACTORIES.put(factory.getExceptionClass().getName(), factory);
        }
    }

    /**
     * Converts an event into an exception and throws that. If the exception class is null,
     * a {@link RuntimeException} will be thrown.
     * @param event the event to be converted
     * @param exceptionClass the exception class to be thrown
     * @throws Throwable this happens always
     */
    public static void throwException(Event event, String exceptionClass) throws Throwable {
        if (exceptionClass != null) {
            ExceptionFactory factory = (ExceptionFactory)EXCEPTION_FACTORIES.get(exceptionClass);
            if (factory != null) {
                throw factory.createException(event);
            } else {
                throw new IllegalArgumentException(
                        "No such ExceptionFactory available: " + exceptionClass);
            }
        } else {
            String msg = EventFormatter.format(event);
            //Get original exception as cause if it is given as one of the parameters
            Throwable t = null;
            Iterator iter = event.getParams().values().iterator();
            while (iter.hasNext()) {
                Object o = iter.next();
                if (o instanceof Throwable) {
                    t = (Throwable)o;
                    break;
                }
            }
            if (t != null) {
                throw new RuntimeException(msg, t);
            } else {
                throw new RuntimeException(msg);
            }
        }
    }

    /**
     * This interface is implementation by exception factories that can create exceptions from
     * events.
     */
    public interface ExceptionFactory {

        /**
         * Creates an exception from an event.
         * @param event the event
         * @return the newly created exception
         */
        Throwable createException(Event event);

        /**
         * Returns the {@link Exception} class created by this factory.
         * @return the exception class
         */
        Class getExceptionClass();
    }
}
