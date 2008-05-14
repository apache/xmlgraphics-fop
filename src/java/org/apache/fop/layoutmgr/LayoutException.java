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

package org.apache.fop.layoutmgr;

import java.util.Locale;

import org.apache.fop.events.Event;
import org.apache.fop.events.EventFormatter;
import org.apache.fop.events.EventExceptionManager.ExceptionFactory;

/**
 * Exception thrown by FOP if an unrecoverable layout error occurs. An example: An area overflows
 * a viewport that has overflow="error-if-overflow".
 * 
 * @todo Discuss if this should become a checked exception.
 */
public class LayoutException extends RuntimeException {

    private static final long serialVersionUID = 5157080040923740433L;
    
    private String localizedMessage;
    private LayoutManager layoutManager;

    /**
     * Constructs a new layout exception with the specified detail message.
     * @param message the detail message.
     */
    public LayoutException(String message) {
        this(message, null);
    }

    /**
     * Constructs a new layout exception with the specified detail message.
     * @param message the detail message
     * @param lm the layout manager that throws the exception
     */
    public LayoutException(String message, LayoutManager lm) {
        super(message);
        this.layoutManager = lm;
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

    /**
     * Returns the layout manager that detected the problem.
     * @return the layout manager (or null)
     */
    public LayoutManager getLayoutManager() {
        return this.layoutManager;
    }
    
    /** Exception factory for {@link LayoutException}. */
    public static class LayoutExceptionFactory implements ExceptionFactory {

        /** {@inheritDoc} */
        public Throwable createException(Event event) {
            Object source = event.getSource();
            LayoutManager lm = (source instanceof LayoutManager) ? (LayoutManager)source : null;
            String msg = EventFormatter.format(event, Locale.ENGLISH);
            LayoutException ex = new LayoutException(msg, lm);
            if (!Locale.ENGLISH.equals(Locale.getDefault())) {
                ex.setLocalizedMessage(EventFormatter.format(event));
            }
            return ex;
        }
        
        /** {@inheritDoc} */
        public Class getExceptionClass() {
            return LayoutException.class;
        }
        
    }    
}
