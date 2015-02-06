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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.fop.pdf.PDFAMode;
import org.apache.fop.pdf.PDFEncryptionParams;
import org.apache.fop.pdf.PDFXMode;
import org.apache.fop.pdf.Version;

import static org.apache.fop.render.pdf.PDFRendererOption.DISABLE_SRGB_COLORSPACE;
import static org.apache.fop.render.pdf.PDFRendererOption.FILTER_LIST;
import static org.apache.fop.render.pdf.PDFRendererOption.LINEARIZATION;
import static org.apache.fop.render.pdf.PDFRendererOption.MERGE_FONTS;
import static org.apache.fop.render.pdf.PDFRendererOption.OUTPUT_PROFILE;
import static org.apache.fop.render.pdf.PDFRendererOption.PDF_A_MODE;
import static org.apache.fop.render.pdf.PDFRendererOption.PDF_X_MODE;
import static org.apache.fop.render.pdf.PDFRendererOption.VERSION;

/**
 * The renderer options configuration data.
 */
public final class PDFRendererOptionsConfig {

    static final PDFRendererOptionsConfig DEFAULT;

    static {
        final EnumMap<PDFRendererOption, Object> props
        = new EnumMap<PDFRendererOption, Object>(PDFRendererOption.class);
        for (PDFRendererOption option : PDFRendererOption.values()) {
            props.put(option, option.getDefaultValue());
        }
        DEFAULT = new PDFRendererOptionsConfig(props, null);
    }

    private final Map<PDFRendererOption, Object> properties
            = new EnumMap<PDFRendererOption, Object>(PDFRendererOption.class);

    private final PDFEncryptionParams encryptionConfig;

    PDFRendererOptionsConfig(Map<PDFRendererOption, Object> props,
            PDFEncryptionParams encryptionParams) {
        properties.putAll(props);
        this.encryptionConfig = copyPDFEncryptionParams(encryptionParams);
    }

    private static PDFEncryptionParams copyPDFEncryptionParams(PDFEncryptionParams source) {
        return source == null ? null : new PDFEncryptionParams(source);
    }

    PDFRendererOptionsConfig merge(PDFRendererOptionsConfig config) {
        if (config == null) {
            return this;
        } else {
            return new PDFRendererOptionsConfig(merge(this.properties, config.properties),
                    config.getEncryptionParameters() == null
                            ? copyPDFEncryptionParams(this.encryptionConfig)
                            : copyPDFEncryptionParams(config.getEncryptionParameters()));
        }
    }

    private static Map<PDFRendererOption, Object> merge(
            Map<PDFRendererOption, Object> first, Map<PDFRendererOption, Object> second) {
        final EnumMap<PDFRendererOption, Object> merged
                = new EnumMap<PDFRendererOption, Object>(PDFRendererOption.class);
        for (PDFRendererOption option : PDFRendererOption.values()) {
            Object value = second.get(option);
            if (value != null) {
                merged.put(option, value);
            } else {
                merged.put(option, first.get(option));
            }
        }
        return merged;
    }

    public Map<String, List<String>> getFilterMap() {
        return (Map<String, List<String>>) properties.get(FILTER_LIST);
    }

    public PDFAMode getPDFAMode() {
        return (PDFAMode) properties.get(PDF_A_MODE);
    }

    public PDFXMode getPDFXMode() {
        return (PDFXMode) properties.get(PDF_X_MODE);
    }

    public PDFEncryptionParams getEncryptionParameters() {
        return encryptionConfig;
    }

    public URI getOutputProfileURI() {
        return (URI) properties.get(OUTPUT_PROFILE);
    }

    public Boolean getDisableSRGBColorSpace() {
        return (Boolean) properties.get(DISABLE_SRGB_COLORSPACE);
    }

    public Version getPDFVersion() {
        return (Version) properties.get(VERSION);
    }

    public Boolean getMergeFontsEnabled() {
        return (Boolean)properties.get(MERGE_FONTS);
    }

    public Boolean getLinearizationEnabled() {
        return (Boolean)properties.get(LINEARIZATION);
    }
}
