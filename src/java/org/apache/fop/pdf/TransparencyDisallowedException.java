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

package org.apache.fop.pdf;

/**
 * The PDF profile being used does not allow transparency.
 */
public class TransparencyDisallowedException extends PDFConformanceException {

    private static final long serialVersionUID = -1653621832449817596L;

    private final Object profile;

    private final String context;

    public TransparencyDisallowedException(Object profile, String context) {
        super(profile + " does not allow the use of transparency."
                + (context == null ? "" : " (" + context + ")"));
        this.profile = profile;
        this.context = context;
    }

    /**
     * Returns the profile that is being used and disallows transparency.
     *
     * @see PDFAMode
     * @see PDFXMode
     */
    public Object getProfile() {
        return profile;
    }

    /**
     * Returns context information to help spotting the problem.
     */
    public String getContext() {
        return context;
    }

}
