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

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.PrefixResolverDefault;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XObject;

import org.apache.fop.intermediate.IFCheck;

/**
 * Simple check that requires an XPath expression to evaluate to true.
 */
public class TrueCheck implements LayoutEngineCheck, IFCheck {

    private String xpath;
    private String failureMessage;
    private PrefixResolver prefixResolver;

    /**
     * Creates a new instance
     * @param xpath XPath statement that needs to be evaluated
     */
    public TrueCheck(String xpath) {
        this.xpath = xpath;
    }

    /**
     * Creates a new instance from a DOM node.
     * @param node DOM node that defines this check
     */
    public TrueCheck(Node node) {
        this.xpath = node.getAttributes().getNamedItem("xpath").getNodeValue();
        Node nd = node.getAttributes().getNamedItem("fail-msg");
        if (nd != null) {
            this.failureMessage = nd.getNodeValue();
        }
        this.prefixResolver = new PrefixResolverDefault(node);
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
        XObject res;
        try {
            res = XPathAPI.eval(doc, xpath, prefixResolver);
        } catch (TransformerException e) {
            throw new RuntimeException("XPath evaluation failed: " + e.getMessage());
        }
        if (!XBoolean.S_TRUE.equals(res)) {
            if (failureMessage != null) {
                throw new RuntimeException(failureMessage);
            } else {
                throw new RuntimeException(
                        "Expected XPath expression to evaluate to 'true', but got '"
                        + res + "' (" + this + ")");
            }
        }

    }

    /** {@inheritDoc} */
    public String toString() {
        return "XPath: " + xpath;
    }

}
