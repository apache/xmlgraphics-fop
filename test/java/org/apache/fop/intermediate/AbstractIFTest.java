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

package org.apache.fop.intermediate;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFSerializer;

/**
 * A common super-class for intermediate format test cases.
 */
abstract class AbstractIFTest extends AbstractIntermediateTest {

    private static final Schema IF_SCHEMA;

    static {
        Schema ifSchema = null;
        try {
            SchemaFactory sFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            sFactory.setErrorHandler(new ErrorHandler() {

                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                public void warning(SAXParseException exception) throws SAXException {
                    throw exception;
                }

            });
            File ifSchemaFile = new File(
                "src/documentation/intermediate-format-ng/fop-intermediate-format-ng.xsd");
            ifSchema = sFactory.newSchema(ifSchemaFile);
        } catch (IllegalArgumentException iae) {
            System.err.println("No suitable SchemaFactory for XML Schema validation found!");
        } catch (SAXException e) {
            throw new ExceptionInInitializerError(e);
        }
        IF_SCHEMA = ifSchema;
    }

    /**
     * Creates a new test case.
     *
     * @param testFile the file containing the document and the tests
     * @throws IOException if an I/O error occurs while loading the test case
     */
    public AbstractIFTest(File testFile) throws IOException {
        super(testFile);
    }

    @Override
    protected String getIntermediateFileExtension() {
        return ".if.xml";
    }

    @Override
    protected Document buildIntermediateDocument(Templates templates) throws Exception {
        Transformer transformer = templates.newTransformer();
        setErrorListener(transformer);

        //Set up XMLRenderer to render to a DOM
        DOMResult domResult = new DOMResult();

        FOUserAgent userAgent = createUserAgent();

        //Create an instance of the target renderer so the XMLRenderer can use its font setup
        IFDocumentHandler targetHandler = userAgent.getRendererFactory().createDocumentHandler(
                userAgent, getTargetMIME());

        //Setup painter
        IFSerializer serializer = new IFSerializer();
        serializer.setContext(new IFContext(userAgent));
        serializer.mimicDocumentHandler(targetHandler);
        serializer.setResult(domResult);

        userAgent.setDocumentHandlerOverride(serializer);

        Fop fop = fopFactory.newFop(userAgent);
        Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(new DOMSource(testDoc), res);

        return (Document) domResult.getNode();
    }

    @Override
    protected void validate(Document doc) throws SAXException, IOException {
        if (IF_SCHEMA == null) {
            return; //skip validation;
        }
        Validator validator = IF_SCHEMA.newValidator();
        validator.setErrorHandler(new ErrorHandler() {

            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }

            public void fatalError(SAXParseException exception) throws SAXException {
                throw exception;
            }

            public void warning(SAXParseException exception) throws SAXException {
                //ignore
            }

        });
        validator.validate(new DOMSource(doc));
    }

}
