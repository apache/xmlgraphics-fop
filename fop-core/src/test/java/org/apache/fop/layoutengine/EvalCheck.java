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

package org.apache.fop.layoutengine;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.apache.fop.intermediate.IFCheck;
import org.apache.fop.util.XMLConstants;

/**
 * Simple check that requires an XPath expression to evaluate to true.
 */
public class EvalCheck implements LayoutEngineCheck, IFCheck {

    private String expected;
    private String xpath;
    private double tolerance;
    private NamespaceContext ctx;

    /**
     * Creates a new instance from a DOM node.
     * @param node DOM node that defines this check
     */
    public EvalCheck(final Node node) {
        this.expected = node.getAttributes().getNamedItem("expected").getNodeValue();
        this.xpath = node.getAttributes().getNamedItem("xpath").getNodeValue();
        Node nd = node.getAttributes().getNamedItem("tolerance");
        if (nd != null) {
            this.tolerance = Double.parseDouble(nd.getNodeValue());
        }
        ctx = new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                if ("xml".equals(prefix)) {
                    return XMLConstants.XML_NAMESPACE;
                }
                return node.lookupNamespaceURI(prefix);
            }
            public Iterator getPrefixes(String val) {
                return null;
            }
            public String getPrefix(String uri) {
                return null;
            }
        };
    }

    /** {@inheritDoc} */
    public void check(LayoutResult result) {
        doCheck(result.getAreaTree());
    }

    /** {@inheritDoc} */
    public void check(Document intermediate) {
        doCheck(intermediate);
    }

    private void doCheck(Document doc) {
        String actual;
        try {
            XPath xPathAPI = XPathFactory.newInstance().newXPath();
            xPathAPI.setNamespaceContext(ctx);
            XPathExpression expr = xPathAPI.compile(xpath);
            actual = (String) expr.evaluate(doc, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("XPath evaluation failed: " + e.getMessage());
        }
        if (tolerance != 0) {
            double v1 = Double.parseDouble(expected);
            double v2 = Double.parseDouble(actual);
            if (Math.abs(v1 - v2) > tolerance) {
                throw new AssertionError(
                        "Expected XPath expression to evaluate to '" + expected + "', but got '"
                        + actual + "' (" + this + ", outside tolerance)");
            }
        } else {
            if (!expected.equals(actual)) {
                throw new AssertionError(
                        "Expected XPath expression to evaluate to '" + expected + "', but got '"
                        + actual + "' (" + this + ")");
            }
        }
    }

    /** {@inheritDoc} */
    public String toString() {
        return "XPath: " + xpath;
    }

}
