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

import java.io.InputStream;
import java.util.MissingResourceException;

import javax.xml.XMLConstants;

import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * A resource resolver that returns a cached instance of the XML Schema, that can normally
 * be found at {@linkplain http://www.w3.org/2001/xml.xsd}. This can be used to avoid
 * unnecessary connection to the W3C website.
 */
final class XMLSchemaResolver implements LSResourceResolver {

    private static final String XML_SCHEMA_SYSTEM_ID = "http://www.w3.org/2001/xml.xsd";

    private static final LSInput XML_SCHEMA_INPUT;

    private static final XMLSchemaResolver INSTANCE = new XMLSchemaResolver();

    private XMLSchemaResolver() { }

    static {
        DOMImplementationRegistry registry = getDOMImplementationRegistry();
        DOMImplementationLS impl
                = (DOMImplementationLS) registry.getDOMImplementation("LS 3.0");
        XML_SCHEMA_INPUT = impl.createLSInput();
        InputStream xmlSchema = loadXMLSchema();
        XML_SCHEMA_INPUT.setByteStream(xmlSchema);
    }

    private static DOMImplementationRegistry getDOMImplementationRegistry() {
        try {
            return DOMImplementationRegistry.newInstance();
        } catch (ClassCastException e) {
            throw new ExceptionInInitializerError(e);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        } catch (InstantiationException e) {
            throw new ExceptionInInitializerError(e);
        } catch (IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static InputStream loadXMLSchema() {
        String xmlSchemaResource = "xml.xsd";
        InputStream xmlSchema = XMLSchemaResolver.class.getResourceAsStream(xmlSchemaResource);
        if (xmlSchema == null) {
            throw new MissingResourceException("Schema for XML namespace not found."
                    + " Did you run ant junit-intermediate-format?",
                    XMLSchemaResolver.class.getName(), xmlSchemaResource);
        }
        return xmlSchema;
    }

    public static XMLSchemaResolver getInstance() {
        return INSTANCE;
    }

    public LSInput resolveResource(String type, String namespaceURI, String publicId,
            String systemId, String baseURI) {
        if (XMLConstants.XML_NS_URI.equals(namespaceURI) && XML_SCHEMA_SYSTEM_ID.equals(systemId)) {
            return XML_SCHEMA_INPUT;
        } else {
            return null;
        }
    }

}
