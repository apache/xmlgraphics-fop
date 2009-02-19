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
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.helpers.DefaultHandler;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;

/**
 * Utility class for FOP's accessibility features. It provides the stylesheets used for processing
 * the incoming XSL-FO stream and for setting up the transformation.
 */
public class AccessibilityUtil {

    private static SAXTransformerFactory tfactory
        = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

    private static Templates addPtrTemplates;
    private static Templates reduceFOTemplates;

    public static DefaultHandler decorateDefaultHandler(DefaultHandler handler,
            FOUserAgent userAgent) throws FOPException {
        DefaultHandler transformNode = new TransformerNodeEndProcessing(
                getAddPtrTemplates(), handler, userAgent);
        return transformNode;
    }

    /**
     * Returns the addPtr.xsl stylesheet.
     * @return the addPtr.xsl stylesheet
     * @throws FOPException if transform fails
     */
    public static synchronized Templates getAddPtrTemplates() throws FOPException {
        if (addPtrTemplates == null) {
            //Load and cache stylesheet
            Source src = new StreamSource(
                    AccessibilityUtil.class.getResource("addPtr.xsl").toExternalForm());
            try {
                addPtrTemplates = tfactory.newTemplates(src);
            } catch (TransformerConfigurationException e) {
                throw new FOPException(e);
            }
        }
        return addPtrTemplates;
    }

    /**
     * Returns the reduceFOTree.xsl stylesheet
     * @return the reduceFOTree.xsl stylesheet
     * @throws FOPException if an error occurs loading the stylesheet
     */
    public static synchronized Templates getReduceFOTreeTemplates() throws FOPException  {
        if (reduceFOTemplates == null) {
            //Load and cache stylesheet
            Source src = new StreamSource(
                    AccessibilityUtil.class.getResource("reduceFOTree.xsl").toExternalForm());
            try {
                reduceFOTemplates = tfactory.newTemplates(src);
            } catch (TransformerConfigurationException e) {
                throw new FOPException(e);
            }
        }
        return reduceFOTemplates;
    }
 }