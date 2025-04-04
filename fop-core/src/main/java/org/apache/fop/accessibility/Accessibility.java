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

package org.apache.fop.accessibility;


/**
 * Helper class for FOP's accessibility features.
 */
public final class Accessibility {

    /** Constant string for the rendering options key to enable accessibility features. */
    public static final String ACCESSIBILITY = "accessibility";

    /** Constant string for the rendering options key to suppress empty tags from structure tree. */
    public static final String KEEP_EMPTY_TAGS = "keep-empty-tags";

    public static final String STATIC_REGION_PER_PAGE = "static-region-per-page";

    /**
     * The value to be set on the 'role' property for the element and its descendants to
     * be considered as artifacts.
     */
    public static final String ROLE_ARTIFACT = "artifact";

    private Accessibility() { }

}
