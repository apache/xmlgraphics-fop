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

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.pdf.PDFAMode;
import org.apache.fop.pdf.PDFXMode;
import org.apache.fop.pdf.Version;
import org.apache.fop.render.RendererConfigOption;

public enum PDFRendererOption implements RendererConfigOption {
    FILTER_LIST("filterList", null) {
        @Override
        Object deserialize(String value) {
            throw new UnsupportedOperationException();
        }
    },
    /** Rendering Options key for the PDF/A mode, default: {@link PDFAMode#DISABLED} */
    PDF_A_MODE("pdf-a-mode", PDFAMode.DISABLED) {
        @Override
        PDFAMode deserialize(String value) {
            return PDFAMode.getValueOf(value);
        }
    },
    /** Rendering Options key for the PDF/X mode, default: {@link PDFXMode#DISABLED} */
    PDF_X_MODE("pdf-x-mode", PDFXMode.DISABLED) {
        @Override
        PDFXMode deserialize(String value) {
            return PDFXMode.getValueOf(value);
        }
    },
    /** PDF version entry: specify the version of the PDF document created, datatype: String */
    VERSION("version") {
        @Override
        Version deserialize(String value) {
            return Version.getValueOf(value);
        }
    },
    /**
     * Rendering Options key for disabling the sRGB color space (only possible if no PDF/A or
     * PDF/X profile is active), default: false
     */
    DISABLE_SRGB_COLORSPACE("disable-srgb-colorspace", false) {
        @Override
        Boolean deserialize(String value) {
            return Boolean.valueOf(value);
        }
    },
    MERGE_FONTS("merge-fonts", false) {
        @Override
        Boolean deserialize(String value) {
            return Boolean.valueOf(value);
        }
    },
    LINEARIZATION("linearization", false) {
        @Override
        Boolean deserialize(String value) {
            return Boolean.valueOf(value);
        }
    },
    /** Rendering Options key for the ICC profile for the output intent. */
    OUTPUT_PROFILE("output-profile") {
        @Override
        URI deserialize(String value) {
            try {
                return InternalResourceResolver.cleanURI(value);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private final String name;

    private final Object defaultValue;

    private PDFRendererOption(String name, Object defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    private PDFRendererOption(String name) {
        this(name, null);
    }

    public String getName() {
        return name;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Object parse(Object object) {
        if (object instanceof String) {
            return deserialize((String) object);
        }  else {
            return object;
        }
    }

    abstract Object deserialize(String value);

}
