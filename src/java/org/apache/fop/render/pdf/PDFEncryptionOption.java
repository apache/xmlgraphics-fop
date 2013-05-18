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

package org.apache.fop.render.pdf;

import org.apache.fop.render.RendererConfigOption;

public enum PDFEncryptionOption implements RendererConfigOption {

    /**
     * PDF encryption length parameter: must be a multiple of 8 between 40 and 128,
     * datatype: int, default: 128
     */
    ENCRYPTION_LENGTH("encryption-length", 128),
    /**
     * PDF encryption parameter: Forbids printing to high quality, datatype: Boolean or
     * "true"/"false", default: false
     */
    NO_PRINTHQ("noprinthq", 40),
    /**
     * PDF encryption parameter: Forbids assembling document, datatype: Boolean or
     * "true"/"false", default: false
     */
    NO_ASSEMBLEDOC("noassembledoc", false),
    /**
     * PDF encryption parameter: Forbids extracting text and graphics, datatype: Boolean
     * or "true"/"false", default: false
     */
    NO_ACCESSCONTENT("noaccesscontent", false),
    /**
     * PDF encryption parameter: Forbids filling in existing interactive forms, datatype:
     * Boolean or "true"/"false", default: false
     */
    NO_FILLINFORMS("nofillinforms", false),
    /**
     * PDF encryption parameter: Forbids annotations, datatype: Boolean or "true"/"false",
     * default: false
     */
    NO_ANNOTATIONS("noannotations", false),
    /**
     * PDF encryption parameter: Forbids printing, datatype: Boolean or "true"/"false",
     * default: false
     */
    NO_PRINT("noprint", false),
    /**
     * PDF encryption parameter: Forbids copying content, datatype: Boolean or "true"/"false",
     * default: false
     */
    NO_COPY_CONTENT("nocopy", false),
    /**
     * PDF encryption parameter: Forbids editing content, datatype: Boolean or "true"/"false",
     * default: false
     */
    NO_EDIT_CONTENT("noedit", false),
    /** PDF encryption parameter: user password, datatype: String, default: "" */
    USER_PASSWORD("user-password", ""),
    /** PDF encryption parameter: owner password, datatype: String, default: "" */
    OWNER_PASSWORD("owner-password", ""),
    /**
     * PDF encryption parameter: encrypts Metadata, datatype: Boolean or "true"/"false", default: true
     */
    ENCRYPT_METADATA("encrypt-metadata", true);

    public static final String ENCRYPTION_PARAMS = "encryption-params";

    private final String name;

    private final Object defaultValue;

    private PDFEncryptionOption(String name, Object defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    private PDFEncryptionOption(String name) {
        this(name, null);
    }

    public String getName() {
        return name;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
