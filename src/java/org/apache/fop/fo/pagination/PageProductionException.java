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

package org.apache.fop.fo.pagination;

import java.util.Locale;

import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import org.apache.fop.events.Event;
import org.apache.fop.events.EventFormatter;
import org.apache.fop.events.EventExceptionManager.ExceptionFactory;

/**
 * Exception thrown by FOP if there is a problem while producing new pages.
 */
public class PageProductionException extends RuntimeException {

    private static final long serialVersionUID = -5126033718398975158L;

    private String localizedMessage;
    private Locator locator;

    /**
     * Creates a new PageProductionException.
     * @param message the message
     * @param locator the optional locator that points to the error in the source file
     */
    public PageProductionException(String message, Locator locator) {
        super(message);
        setLocator(locator);
    }

    /**
     * Set a location associated with the exception.
     * @param locator the locator holding the location.
     */
    public void setLocator(Locator locator) {
        this.locator = new LocatorImpl(locator);
    }


    /**
     * Returns the locattion associated with the exception.
     * @return the locator or null if the location information is not available
     */
    public Locator getLocator() {
        return this.locator;
    }

    /**
     * Sets the localized message for this exception.
     * @param msg the localized message
     */
    public void setLocalizedMessage(String msg) {
        this.localizedMessage = msg;
    }

    /** {@inheritDoc} */
    public String getLocalizedMessage() {
        if (this.localizedMessage != null) {
            return this.localizedMessage;
        } else {
            return super.getLocalizedMessage();
        }
    }

    /** Exception factory for {@link PageProductionException}. */
    public static class PageProductionExceptionFactory implements ExceptionFactory {

        /** {@inheritDoc} */
        public Throwable createException(Event event) {
            Object obj = event.getParam("loc");
            Locator loc = (obj instanceof Locator ? (Locator)obj : null);
            String msg = EventFormatter.format(event, Locale.ENGLISH);
            PageProductionException ex = new PageProductionException(msg, loc);
            if (!Locale.ENGLISH.equals(Locale.getDefault())) {
                ex.setLocalizedMessage(EventFormatter.format(event));
            }
            return ex;
        }

        /** {@inheritDoc} */
        public Class<PageProductionException> getExceptionClass() {
            return PageProductionException.class;
        }

    }
}
