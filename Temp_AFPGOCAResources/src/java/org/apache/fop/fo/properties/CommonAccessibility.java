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

package org.apache.fop.fo.properties;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Store all common accessibility properties.
 * See Sec 7.4 of the XSL-FO Standard.
 * Public "structure" allows direct member access.
 */
public class CommonAccessibility {
    /**
     * The "source-doc" property.
     */
    public String sourceDoc = null;

    /**
     * The "role" property.
     */
    public String role = null;

    /**
     * Create a <code>CommonAccessibility</code> object.
     * @param pList The PropertyList with propery values.
     */
    public CommonAccessibility(PropertyList pList) throws PropertyException {
        sourceDoc = pList.get(Constants.PR_SOURCE_DOCUMENT).getString();
        if ("none".equals(sourceDoc)) {
            sourceDoc = null;
        }
        role = pList.get(Constants.PR_ROLE).getString();
        if ("none".equals(role)) {
            role = null;
        }

    }


}
