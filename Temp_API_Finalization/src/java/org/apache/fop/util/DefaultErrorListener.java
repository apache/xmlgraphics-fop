/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.util;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;

/**
 * Standard ErrorListener implementation for in-FOP use. Some Xalan-J versions don't properly
 * re-throw exceptions.
 */
public class DefaultErrorListener implements ErrorListener {

    private Log log;
    
    /**
     * Main constructor
     * @param log the log instance to send log events to
     */
    public DefaultErrorListener(Log log) {
        this.log = log;
    }
    
    /**
     * @see javax.xml.transform.ErrorListener#warning(javax.xml.transform.TransformerException)
     */
    public void warning(TransformerException exc) {
        log.warn(exc.toString());
    }

    /**
     * @see javax.xml.transform.ErrorListener#error(javax.xml.transform.TransformerException)
     */
    public void error(TransformerException exc) throws TransformerException {
        throw exc;
    }

    /**
     * @see javax.xml.transform.ErrorListener#fatalError(javax.xml.transform.TransformerException)
     */
    public void fatalError(TransformerException exc)
            throws TransformerException {
        throw exc;
    }

}