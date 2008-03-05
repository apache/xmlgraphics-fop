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

import java.util.Locale;
import java.util.Map;

import org.xml.sax.Locator;

import org.apache.fop.fo.ValidationException;

/**
 * This class is reponsible for converting events into exceptions.
 */
public class EventExceptionManager {

    private static final Map EXCEPTION_FACTORIES = new java.util.HashMap();
    
    static {
        //TODO Replace with dynamic registration if more than two different exceptions are needed.
        EXCEPTION_FACTORIES.put(ValidationException.class.getName(),
                new ValidationExceptionFactory());
    }
    
    /**
     * Converts an event into an exception and throws that.
     * @param event the event to be converted
     * @param exceptionClass the exception class to be thrown
     * @throws Throwable this happens always
     */
    public static void throwException(Event event, String exceptionClass) throws Throwable {
        
        //TODO Localize exceptions!
        //TODO Complain if there's no ExceptionFactory for the given exceptionClass
        
        ExceptionFactory factory = (ExceptionFactory)EXCEPTION_FACTORIES.get(exceptionClass);
        if (factory != null) {
            throw factory.createException(event);
        } else {
            String msg = EventFormatter.format(event);
            throw new RuntimeException(msg);
        }
    }
    
    private interface ExceptionFactory {
        Throwable createException(Event event);
    }
    
    //TODO Move me out of here as I'm FOP-dependent!
    private static class ValidationExceptionFactory implements ExceptionFactory {

        public Throwable createException(Event event) {
            Locator loc = (Locator)event.getParam("loc");
            String msg = EventFormatter.format(event, Locale.ENGLISH);
            ValidationException ex = new ValidationException(msg, loc);
            return ex;
        }
        
    }
}
