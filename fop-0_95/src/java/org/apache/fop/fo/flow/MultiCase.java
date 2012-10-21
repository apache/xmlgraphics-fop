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

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;

/**
 * Class modelling the fo:multi-case object.
 * @todo implement validateChildNode()
 */
public class MultiCase extends FObj {
    // The value of properties relevant for fo:multi-case.
    private int startingState;
    // private ToBeImplementedProperty caseName;
    // private ToBeImplementedProperty caseTitle;
    // Unused but valid items, commented out for performance:
    //     private CommonAccessibility commonAccessibility;
    // End of property values

    static boolean notImplementedWarningGiven = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public MultiCase(FONode parent) {
        super(parent);

        if (!notImplementedWarningGiven) {
            log.warn("fo:multi-case is not yet implemented.");
            notImplementedWarningGiven = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        startingState = pList.get(PR_STARTING_STATE).getEnum();
        // caseName = pList.get(PR_CASE_NAME);
        // caseTitle = pList.get(PR_CASE_TITLE);
    }

    /**
     * Return the "starting-state" property.
     */
    public int getStartingState() {
        return startingState;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "multi-case";
    }

    /**
     * {@inheritDoc}
     */
    public int getNameId() {
        return FO_MULTI_CASE;
    }
}
