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
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Simple check that requires an XPath expression to evaluate to true.
 */
public class TrueCheck implements LayoutEngineCheck {

    private String xpath;
    
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
    }
    
    /**
     * @see org.apache.fop.layoutengine.LayoutEngineCheck#check(org.w3c.dom.Document)
     */
    public void check(Document doc) {
        XObject res;
        try {
            res = XPathAPI.eval(doc, xpath);
        } catch (TransformerException e) {
            throw new RuntimeException("XPath evaluation failed: " + e.getMessage());
        }
        if (!XBoolean.S_TRUE.equals(res)) {
            throw new RuntimeException(
                    "Expected XPath expression to evaluate to 'true', but got '" 
                    + res + "' (" + this + ")");
        }

    }

    /** @see java.lang.Object#toString() */
    public String toString() {
        return "XPath: " + xpath;
    }
    
}
