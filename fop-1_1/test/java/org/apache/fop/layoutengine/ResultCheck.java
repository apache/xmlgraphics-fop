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

import org.w3c.dom.Node;

import org.apache.fop.apps.FormattingResults;

/**
 * Simple check that requires a result property to evaluate to the expected value
 */
public class ResultCheck implements LayoutEngineCheck {

    private String expected;
    private String property;

    /**
     * Creates a new instance from a DOM node.
     * @param node DOM node that defines this check
     */
    public ResultCheck(Node node) {
        this.expected = node.getAttributes().getNamedItem("expected").getNodeValue();
        this.property = node.getAttributes().getNamedItem("property").getNodeValue();
    }

    /** {@inheritDoc} */
    public void check(LayoutResult result) {
        FormattingResults results = result.getResults();
        String actual;
        if (property.equals("pagecount")) {
            actual = Integer.toString(results.getPageCount());
        } else {
            throw new RuntimeException("No such property test: " + property);
        }
        if (!expected.equals(actual)) {
            throw new RuntimeException(
                    "Expected property to evaluate to '" + expected + "', but got '"
                    + actual + "' (" + this + ")");
        }

    }

    @Override
    public String toString() {
        return "Property: " + property;
    }

}
