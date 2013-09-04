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

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.layoutmgr.Alternative.FittingStrategy;

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
    @Override
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        String strategyName = pList.get(PR_X_FITTING_STRATEGY).getString();
        for (FittingStrategy fs : FittingStrategy.values()) {
            if (fs.getStrategyName().equals(strategyName)) {
                strategy = fs;
                return;
            }
        }
        if (log.isWarnEnabled()) {
            log.warn("Unrecognized strategy name => " + strategyName + ". Using default strategy (first-fit");
        }
    }

    @Override
    public void startOfNode() throws FOPException {
        super.startOfNode();
        if (log.isDebugEnabled()) {
            log.debug("BestFit.startOfNode()");
        }
    }

    @Override
    public void endOfNode() throws FOPException {
        super.endOfNode();
        if (log.isDebugEnabled()) {
            log.debug("BestFit.endOfNode()");
        }
    }

    /**
     * {@inheritDoc}
     * Content model: (fox:alternative-block)+
     */
    @Override
    protected void validateChildNode(Locator loc, String nsURI, String localName)
            throws ValidationException {
        if (FOX_URI.equals(nsURI)) {
            if (!"alternative-block".equals(localName)) {
                invalidChildError(loc, FOX_URI, localName);
            }
        } else {
            invalidChildError(loc, nsURI, localName);
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

    @Override
    public String getNamespaceURI() {
        return ExtensionElementMapping.URI;
    }

    public FittingStrategy getStrategy() {
        return strategy;
    }

}
