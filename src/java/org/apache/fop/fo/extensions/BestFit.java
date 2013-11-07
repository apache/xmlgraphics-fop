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

package org.apache.fop.fo.extensions;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.flow.MultiCaseHandler;
import org.apache.fop.fo.flow.MultiSwitch;
import org.apache.fop.layoutmgr.Alternative.FittingStrategy;

public class BestFit extends FObj implements MultiCaseHandler {

    public static final String OBJECT_NAME = "best-fit";
    private FittingStrategy strategy;

    public BestFit(FONode parent) {
        super(parent);
    }

    public void setFittingStrategy(String strategyName) {
        strategy = FittingStrategy.make(strategyName);
        if (strategy == null) {
            strategy = FittingStrategy.FIRST_FIT;
            if (log.isWarnEnabled()) {
                log.warn("Unrecognized strategy name => " + strategyName + ". Using default strategy (first-fit");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateChildNode(Locator loc, String nsURI, String localName)
            throws ValidationException {
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

    public void filter(MultiSwitch multiSwitch) throws FOPException {
        // Modifying the FO tree is not advised...
//        FONodeIterator nodeIter = multiSwitch.getChildNodes();
//        while (nodeIter.hasNext()) {
//            FONode childNode = (FONode) nodeIter.next();
//            this.addChildNode(childNode);
//        }
//        multiSwitch.clearChildNodes();
    }

    public FittingStrategy getStrategy() {
        return strategy;
    }

}

