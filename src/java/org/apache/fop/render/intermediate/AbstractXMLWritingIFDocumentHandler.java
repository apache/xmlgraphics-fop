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

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.ContentHandler;

import org.apache.fop.util.GenerationHelperContentHandler;

/**
 * Abstract base class for XML-writing {@link IFDocumentHandler} implementations.
 */
public abstract class AbstractXMLWritingIFDocumentHandler extends AbstractIFDocumentHandler {

    /**
     * Default SAXTransformerFactory that can be used by subclasses.
     */
    protected SAXTransformerFactory tFactory
        = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

    /** Main SAX ContentHandler to receive the generated SAX events. */
    protected GenerationHelperContentHandler handler;

    /** {@inheritDoc} */
    public void setResult(Result result) throws IFException {
        if (result instanceof SAXResult) {
            SAXResult saxResult = (SAXResult)result;
            this.handler = new GenerationHelperContentHandler(
                    saxResult.getHandler(), getMainNamespace());
        } else {
            this.handler = new GenerationHelperContentHandler(
                    createContentHandler(result), getMainNamespace());
        }
    }

    /**
     * Returns the main namespace used for generated XML content.
     * @return the main namespace
     */
    protected abstract String getMainNamespace();

    /**
     * Creates a ContentHandler for the given JAXP Result instance.
     * @param result the JAXP Result instance
     * @return the requested SAX ContentHandler
     * @throws IFException if an error occurs setting up the output
     */
    protected ContentHandler createContentHandler(Result result) throws IFException {
        try {
            TransformerHandler tHandler = tFactory.newTransformerHandler();
            Transformer transformer = tHandler.getTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            tHandler.setResult(result);
            return tHandler;
        } catch (TransformerConfigurationException tce) {
            throw new IFException(
                    "Error while setting up the serializer for XML output", tce);
        }
    }

}
