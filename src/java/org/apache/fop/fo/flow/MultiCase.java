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

package org.apache.fop.fo.flow;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_multi-case">
 * <code>fo:multi-case</code></a> object.
 * TODO implement validateChildNode()
 */
public class MultiCase extends FObj {

    // FO multi-case properties
    private int startingState;
    private String caseName;
    private String caseTitle;
    // Unused but valid items, commented out for performance:
    //     private CommonAccessibility commonAccessibility;
    // End of property values

    /**
     * Base constructor
     *
     * @param parent {@link FONode} that is the parent of this object
     */
    public MultiCase(FONode parent) {
        super(parent);
    }

    @Override
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        startingState = pList.get(PR_STARTING_STATE).getEnum();
        caseName = pList.get(PR_CASE_NAME).getString();
        caseTitle = pList.get(PR_CASE_TITLE).getString();
    }

    /**
     * Content Model: (#PCDATA|%inline;|%block)*
     */
    @Override
    protected void validateChildNode(Locator loc, String nsURI, String localName)
            throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (!isBlockOrInlineItem(nsURI, localName) || "marker".equals(localName)) {
                invalidChildError(loc, nsURI, localName);
            }
            if (!"multi-toggle".equals(localName)) {
                // Validate against parent of fo:multi-switch
                FONode.validateChildNode(getParent().getParent(), loc, nsURI, localName);
            }
        }
    }

    @Override
    public void endOfNode() throws FOPException {
        if (firstChild == null) {
            missingChildElementError("(#PCDATA|%inline;|%block)*");
        }
    }

    /** @return the "starting-state" property */
    public int getStartingState() {
        return startingState;
    }

    @Override
    public String getLocalName() {
        return "multi-case";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_MULTI_CASE}
     */
    @Override
    public int getNameId() {
        return FO_MULTI_CASE;
    }

    public String getCaseName() {
        return caseName;
    }

    public String getCaseTitle() {
        return caseTitle;
    }

}
