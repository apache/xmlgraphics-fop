/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.fop.layoutengine;

import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Node;

/**
 * Simple check that requires an XPath expression to evaluate to true.
 */
public class EvalCheck implements LayoutEngineCheck {

    private String expected;
    private String xpath;
    
    /**
     * Creates a new instance
     * @param expected expected value
     * @param xpath XPath statement that needs to be evaluated
     */
    public EvalCheck(String expected, String xpath) {
        this.expected = expected;
        this.xpath = xpath;
    }
    
    /**
     * Creates a new instance from a DOM node.
     * @param node DOM node that defines this check
     */
    public EvalCheck(Node node) {
        this.expected = node.getAttributes().getNamedItem("expected").getNodeValue();
        this.xpath = node.getAttributes().getNamedItem("xpath").getNodeValue();
    }
    
    /** @see org.apache.fop.layoutengine.LayoutEngineCheck */
    public void check(LayoutResult result) {
        XObject res;
        try {
            res = XPathAPI.eval(result.getAreaTree(), xpath);
        } catch (TransformerException e) {
            throw new RuntimeException("XPath evaluation failed: " + e.getMessage());
        }
        String actual = res.str(); //Second str() seems to fail. D'oh!
        if (!expected.equals(actual)) {
            throw new RuntimeException(
                    "Expected XPath expression to evaluate to '" + expected + "', but got '" 
                    + actual + "' (" + this + ")");
        }

    }

    /** @see java.lang.Object#toString() */
    public String toString() {
        return "XPath: " + xpath;
    }
    
}
