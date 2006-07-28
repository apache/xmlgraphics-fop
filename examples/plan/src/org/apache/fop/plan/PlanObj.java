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
 
package org.apache.fop.plan;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.XMLObj;

/**
 * Since SVG objects are not layed out then this class checks
 * that this element is not being layed out inside some incorrect
 * element.
 */
public class PlanObj extends XMLObj {

    /**
     * Creates a new Plan object.
     * @param parent the parent formatting object
     */
    public PlanObj(FONode parent) {
        super(parent);
    }

    /** @see org.apache.fop.fo.FONode#getNamespaceURI() */
    public String getNamespaceURI() {
        return PlanElementMapping.NAMESPACE;
    }

    /** @see org.apache.fop.fo.FONode#getNormalNamespacePrefix() */
    public String getNormalNamespacePrefix() {
        return "plan";
    }
}

