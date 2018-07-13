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

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;

public class ChangeBarEnd extends ChangeBar {

    /**
     * Constructs a new ChangeBarEnd element.
     *
     * @param parent The parent node
     */
    public ChangeBarEnd(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "change-bar-end";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_CHANGE_BAR_END}
     */
    public int getNameId() {
        return FO_CHANGE_BAR_END;
    }

    /** {@inheritDoc} */
    public void processNode(String elementName, Locator locator,
                            Attributes attlist, PropertyList pList) throws FOPException {
        super.processNode(elementName, locator, attlist, pList);

        // check if we have an element on the stack at all
        ChangeBar changeBarStart = getChangeBarBegin();

        if (changeBarStart == null) {
            getFOValidationEventProducer().changeBarNoBegin(this, getName(), locator);
        } else {
            pop();
        }
    }
}
