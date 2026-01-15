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

import org.apache.fop.intermediate.IFCheck;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Simple check that requires an XPath expression to evaluate to true.
 */
public class IFTrueCheck implements LayoutEngineCheck, IFCheck {

    final TrueCheck trueCheck;

    /**
     * Creates a new instance from a DOM node.
     * @param node DOM node that defines this check
     */
    public IFTrueCheck(final Node node) {
        this.trueCheck = new TrueCheck(node);
    }

    /** {@inheritDoc} */
    public void check(LayoutResult result) {
        trueCheck.doCheck(result.getAreaTree());
    }

    /** {@inheritDoc} */
    public void check(Document intermediate) {
        trueCheck.doCheck(intermediate);
    }


    /** {@inheritDoc} */
    public String toString() {
        return trueCheck.toString();
    }

}
