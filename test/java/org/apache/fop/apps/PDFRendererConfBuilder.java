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

package org.apache.fop.apps;

import org.w3c.dom.Element;

import org.apache.fop.apps.FopConfBuilder.RendererConfBuilder;
import org.apache.fop.render.RendererConfigOption;
import org.apache.fop.render.pdf.PDFEncryptionOption;

import static org.apache.fop.render.pdf.PDFEncryptionOption.ENCRYPTION_LENGTH;
import static org.apache.fop.render.pdf.PDFEncryptionOption.ENCRYPTION_PARAMS;
import static org.apache.fop.render.pdf.PDFEncryptionOption.OWNER_PASSWORD;
import static org.apache.fop.render.pdf.PDFEncryptionOption.USER_PASSWORD;
import static org.apache.fop.render.pdf.PDFRendererOption.DISABLE_SRGB_COLORSPACE;
import static org.apache.fop.render.pdf.PDFRendererOption.FILTER_LIST;
import static org.apache.fop.render.pdf.PDFRendererOption.OUTPUT_PROFILE;
import static org.apache.fop.render.pdf.PDFRendererOption.PDF_A_MODE;
import static org.apache.fop.render.pdf.PDFRendererOption.PDF_X_MODE;
import static org.apache.fop.render.pdf.PDFRendererOption.VERSION;

/**
 * A config builder specific to a particular renderer for specific MIME type.
 */
public final class PDFRendererConfBuilder extends RendererConfBuilder {

    private EncryptionParamsBuilder accessConf;

    public PDFRendererConfBuilder() {
        super(MimeConstants.MIME_PDF);
    }

    public EncryptionParamsBuilder startEncryptionParams() {
        accessConf = new EncryptionParamsBuilder();
        return accessConf;
    }

    public PDFRendererConfBuilder endEncryptionParams() {
        accessConf = null;
        return this;
    }

    public PDFRendererConfBuilder createFilterList(String type, String... filters) {
        Element filterListEl = createElement(FILTER_LIST.getName());
        if (type != null) {
            filterListEl.setAttribute("type", type);
        }
        for (String filter : filters) {
            createTextElement("value", filter, filterListEl);
        }
        return this;
    }

    public PDFRendererConfBuilder setPDFAMode(String value) {
        createTextElement(PDF_A_MODE, value);
        return this;
    }

    public PDFRendererConfBuilder setPDFXMode(String value) {
        createTextElement(PDF_X_MODE, value);
        return this;
    }

    public PDFRendererConfBuilder setPDFVersion(String version) {
        createTextElement(VERSION, version);
        return this;
    }

    public PDFRendererConfBuilder setOutputProfile(String profile) {
        createTextElement(OUTPUT_PROFILE, profile);
        return this;
    }

    public PDFRendererConfBuilder disableSRGBColorSpace(boolean disable) {
        createTextElement(DISABLE_SRGB_COLORSPACE, String.valueOf(disable));
        return this;
    }

    public final class EncryptionParamsBuilder {
        private final Element el;

        private EncryptionParamsBuilder() {
            el = createElement(ENCRYPTION_PARAMS);
        }

        public EncryptionParamsBuilder setEncryptionLength(int length) {
            createTextElement(ENCRYPTION_LENGTH, String.valueOf(length));
            return this;
        }

        public EncryptionParamsBuilder setUserPassword(String password) {
            createTextElement(USER_PASSWORD, password);
            return this;
        }

        public EncryptionParamsBuilder setOwnerPassword(String password) {
            createTextElement(OWNER_PASSWORD, password);
            return this;
        }

        public EncryptionParamsBuilder setAllowParam(PDFEncryptionOption option) {
            el.appendChild(createElement(option.getName()));
            return this;
        }

        public PDFRendererConfBuilder endEncryptionParams() {
            return PDFRendererConfBuilder.this.endEncryptionParams();
        }

        private void createTextElement(RendererConfigOption name, String value) {
            PDFRendererConfBuilder.this.createTextElement(name.getName(), value, el);
        }

    }
}
