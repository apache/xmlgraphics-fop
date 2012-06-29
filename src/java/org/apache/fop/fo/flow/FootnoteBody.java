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

// XML
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_footnote-body">
 * <code>fo:footnote-body</code></a> object.
 */
public class FootnoteBody extends FObj implements CommonAccessibilityHolder {

    /** {@inheritDoc} */
    private CommonAccessibility commonAccessibility;

    /**
     * Base constructor
     *
     * @param parent FONode that is the parent of this object
     */
    public FootnoteBody(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        commonAccessibility = CommonAccessibility.getInstance(pList);
    }

    /** {@inheritDoc} */
    protected void startOfNode() throws FOPException {
        getFOEventHandler().startFootnoteBody(this);
    }

    /**
     * Make sure the content model is satisfied, if so then tell the
     * {@link org.apache.fop.fo.FOEventHandler} that we are at the
     * end of the footnote-body.
     * {@inheritDoc}
     */
    protected void endOfNode() throws FOPException {
        if (firstChild == null) {
            missingChildElementError("(%block;)+");
        }
        getFOEventHandler().endFootnoteBody(this);
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: (%block;)+
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (!isBlockItem(nsURI, localName)) {
                invalidChildError(loc, nsURI, localName);
            }
        }
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "footnote-body";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_FOOTNOTE_BODY}
     */
    public int getNameId() {
        return FO_FOOTNOTE_BODY;
    }

    /** {@inheritDoc} */
    public CommonAccessibility getCommonAccessibility() {
        return commonAccessibility;
    }

}
