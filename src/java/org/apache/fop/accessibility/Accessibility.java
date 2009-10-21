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

package org.apache.fop.accessibility;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.helpers.DefaultHandler;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;

/**
 * Helper class for FOP's accessibility features.
 */
public final class Accessibility {

    /** Constant string for the rendering options key to enable accessibility features. */
    public static final String ACCESSIBILITY = "accessibility";

    // TODO what if the default factory is not a SAXTransformerFactory?
    private static SAXTransformerFactory tfactory
            = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

    private static Templates addPtrTemplates;

    private static Templates reduceFOTreeTemplates;

    private Accessibility() { }

    /**
     * Decorates the given handler so the structure tree used for accessibility
     * features can be branched off the main content stream.
     * @param handler the handler to decorate
     * @param userAgent the user agent
     * @return the decorated handler
     * @throws FOPException if an error occurs setting up the decoration
     */
    public static DefaultHandler decorateDefaultHandler(DefaultHandler handler,
            FOUserAgent userAgent) throws FOPException {
        try {
            setupTemplates();
            TransformerHandler addPtr = tfactory.newTransformerHandler(addPtrTemplates);
            Transformer reduceFOTree = reduceFOTreeTemplates.newTransformer();
            return new AccessibilityPreprocessor(addPtr, reduceFOTree, userAgent, handler);
        } catch (TransformerConfigurationException e) {
            throw new FOPException(e);
        }
    }

    private static synchronized void setupTemplates() throws TransformerConfigurationException {
        if (addPtrTemplates == null) {
            addPtrTemplates = loadTemplates("addPtr.xsl");
        }
        if (reduceFOTreeTemplates == null) {
            reduceFOTreeTemplates = loadTemplates("reduceFOTree.xsl");
        }
    }

    private static Templates loadTemplates(String source) throws TransformerConfigurationException {
        Source src = new StreamSource(Accessibility.class.getResource(source).toExternalForm());
        return tfactory.newTemplates(src);
    }

}
