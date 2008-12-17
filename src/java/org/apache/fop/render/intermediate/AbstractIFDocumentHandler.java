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

package org.apache.fop.render.intermediate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.FOUserAgent;

/**
 * Abstract base class for {@code IFDocumentHandler} implementations.
 */
public abstract class AbstractIFDocumentHandler implements IFDocumentHandler {

    /** logging instance */
    private static Log log = LogFactory.getLog(AbstractIFDocumentHandler.class);

    private FOUserAgent userAgent;

    /**
     * Default constructor.
     */
    public AbstractIFDocumentHandler() {
    }

    /** {@inheritDoc} */
    public void setUserAgent(FOUserAgent ua) {
        if (this.userAgent != null) {
            throw new IllegalStateException("The user agent was already set");
        }
        this.userAgent = ua;
    }

    /** {@inheritDoc} */
    public FOUserAgent getUserAgent() {
        return this.userAgent;
    }

    /** {@inheritDoc} */
    public IFDocumentNavigationHandler getDocumentNavigationHandler() {
        return null; //By default, this is not supported
    }

    /** {@inheritDoc} */
    public void startDocumentHeader() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void endDocumentHeader() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void startDocumentTrailer() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void endDocumentTrailer() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void startPageHeader() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void endPageHeader() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void startPageTrailer() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void endPageTrailer() throws IFException {
        //nop
    }

}
