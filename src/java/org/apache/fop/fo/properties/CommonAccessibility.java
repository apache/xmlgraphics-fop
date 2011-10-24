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
 * The "role" and "source-document" properties, see Section 7.5 of the XSL-FO 1.1
 * Recommendation.
 */
public final class CommonAccessibility {

    private static final CommonAccessibility DEFAULT_INSTANCE = new CommonAccessibility(null, null);

    private final String sourceDocument;

    private final String role;

    private CommonAccessibility(String sourceDocument, String role) {
        this.sourceDocument = sourceDocument;
        this.role = role;
    }

    /**
     * Returns an instance that matches the values (if any) in the given property list.
     *
     * @param propertyList a list from which to retrieve the accessibility properties
     * @return the corresponding instance
     * @throws PropertyException if a problem occurs while retrieving the properties
     */
    public static CommonAccessibility getInstance(PropertyList propertyList)
            throws PropertyException {
        String sourceDocument = propertyList.get(Constants.PR_SOURCE_DOCUMENT).getString();
        if ("none".equals(sourceDocument)) {
            sourceDocument = null;
        }
        String role = propertyList.get(Constants.PR_ROLE).getString();
        if ("none".equals(role)) {
            role = null;
        }
        if (sourceDocument == null && role == null) {
            return DEFAULT_INSTANCE;
        } else {
            return new CommonAccessibility(sourceDocument, role);
        }
    }

    /**
     * Returns the value of the source-document property.
     *
     * @return the source document, or null if the property was set to "none"
     */
    public String getSourceDocument() {
        return sourceDocument;
    }

    /**
     * Returns the value of the role property.
     *
     * @return the role, or null if the property was set to "none"
     */
    public String getRole() {
        return role;
    }

}
