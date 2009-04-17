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


/**
 * Constants used for configuring PDF output.
 */
public interface PDFConfigurationConstants {

    /** PDF encryption parameter: all parameters as object, datatype: PDFEncryptionParams */
    String ENCRYPTION_PARAMS = "encryption-params";
    /** PDF encryption parameter: user password, datatype: String */
    String USER_PASSWORD = "user-password";
    /** PDF encryption parameter: owner password, datatype: String */
    String OWNER_PASSWORD = "owner-password";
    /** PDF encryption parameter: Forbids printing, datatype: Boolean or "true"/"false" */
    String NO_PRINT = "noprint";
    /** PDF encryption parameter: Forbids copying content, datatype: Boolean or "true"/"false" */
    String NO_COPY_CONTENT = "nocopy";
    /** PDF encryption parameter: Forbids editing content, datatype: Boolean or "true"/"false" */
    String NO_EDIT_CONTENT = "noedit";
    /** PDF encryption parameter: Forbids annotations, datatype: Boolean or "true"/"false" */
    String NO_ANNOTATIONS = "noannotations";
    /** Rendering Options key for the PDF/A mode. */
    String PDF_A_MODE = "pdf-a-mode";
    /** Rendering Options key for the PDF/X mode. */
    String PDF_X_MODE = "pdf-x-mode";
    /** Rendering Options key for the ICC profile for the output intent. */
    String KEY_OUTPUT_PROFILE = "output-profile";
    /**
     * Rendering Options key for disabling the sRGB color space (only possible if no PDF/A or
     * PDF/X profile is active).
     */
    String KEY_DISABLE_SRGB_COLORSPACE = "disable-srgb-colorspace";
}
