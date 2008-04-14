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
 
package org.apache.fop.svg;

import java.awt.geom.AffineTransform;

import org.apache.fop.apps.FOUserAgent;

/**
 * The SVG user agent. This is an implementation of the Batik SVG user agent.
 */
public class SVGUserAgent extends SimpleSVGUserAgent {
    
    private SVGEventProducer eventProducer;
    private Exception lastException;

    /**
     * Creates a new SVGUserAgent.
     * @param foUserAgent the FO user agent to associate with this SVG user agent
     * @param at the current transform
     */
    public SVGUserAgent(FOUserAgent foUserAgent, AffineTransform at) {
        super(foUserAgent.getSourcePixelUnitToMillimeter(), at);
        this.eventProducer = SVGEventProducer.Provider.get(foUserAgent.getEventBroadcaster());
    }

    /**
     * Creates a new SVGUserAgent.
     * @param foUserAgent the FO user agent to associate with this SVG user agent
     */
    public SVGUserAgent(FOUserAgent foUserAgent) {
        this(foUserAgent, new AffineTransform());
    }
    
    /**
     * Returns the last exception sent to the {@link #displayError(Exception)} method.
     * @return the last exception or null if no exception occurred
     */
    public Exception getLastException() {
        return this.lastException;
    }

    /**
     * Displays an error message.
     * @param message the message to display
     */
    public void displayError(String message) {
        this.eventProducer.error(this, message, null);
    }

    /**
     * Displays an error resulting from the specified Exception.
     * @param ex the exception to display
     */
    public void displayError(Exception ex) {
        this.lastException = ex;
        this.eventProducer.error(this, ex.getLocalizedMessage(), ex);
    }

    /**
     * Displays a message in the User Agent interface.
     * The given message is typically displayed in a status bar.
     * @param message the message to display
     */
    public void displayMessage(String message) {
        this.eventProducer.info(this, message);
    }

    /**
     * Shows an alert dialog box.
     * @param message the message to display
     */
    public void showAlert(String message) {
        this.eventProducer.alert(this, message);
    }

}