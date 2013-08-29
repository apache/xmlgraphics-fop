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

package org.apache.fop.fo.extensions;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.layoutmgr.AlternativeManager.FittingStrategy;

/**
 *  A class that holds a set of <fox:alternative-block> blocks where each one is examined,
 *  and the one that best matches the fitting strategy is selected.
 *  The selected alternative should have an occupied BPD that is less than
 *  the remaining BPD of the current page.
 */
public class BestFit extends FObj {

    public static final String OBJECT_NAME = "best-fit";
    private FittingStrategy strategy = FittingStrategy.FIRST_FIT;

    public BestFit(FONode parent) {
        super(parent);

    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        String strategyName = pList.get(PR_X_FITTING_STRATEGY).getString();
        if (strategyName.equals("first-fit"))
            strategy = FittingStrategy.FIRST_FIT;
        else if (strategyName.equals("smallest-fit"))
            strategy = FittingStrategy.SMALLEST_FIT;
        else if (strategyName.equals("biggest-fit"))
            strategy = FittingStrategy.BIGGEST_FIT;
        else {
            log.warn("Unrecognized strategy name => " + strategyName + ". Using default strategy (first-fit");
            strategy = FittingStrategy.FIRST_FIT;
        }

    }

    public void processNode(String elementName, Locator locator,
            Attributes attlist, PropertyList pList) throws FOPException {
        if (log.isDebugEnabled()) {
            log.debug("org.apache.fop.fo.extensions.BestFit: " + elementName
                    + (locator != null ? " at " + getLocatorString(locator) : ""));
        }
        pList.addAttributesToList(attlist);
        bind(pList);
    }

    public void startOfNode() throws FOPException {
        if (log.isDebugEnabled())
            log.debug("BestFit.startOfNode()");
    }

    public void endOfNode() throws FOPException {
        if (log.isDebugEnabled())
            log.debug("BestFit.endOfNode()");
    }

    /**
     * {@inheritDoc}
     * Content model: (fox:alternative-block)+
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
            throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (!"alternative-block".equals(localName)) {
                invalidChildError(loc, FO_URI, localName);
            }
        }
    }

    @Override
    public String getLocalName() {
        return OBJECT_NAME;
    }

    @Override
    public String getNormalNamespacePrefix() {
        return ExtensionElementMapping.STANDARD_PREFIX;
    }

    public String getNamespaceURI() {
        return ExtensionElementMapping.URI;
    }

    public FittingStrategy getStrategy() {
        return strategy;
    }

}
