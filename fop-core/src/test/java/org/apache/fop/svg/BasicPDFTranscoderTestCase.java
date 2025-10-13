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

package org.apache.fop.svg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfigurationBuilder;

/**
 * Basic runtime test for the PDF transcoder. It is used to verify that
 * nothing obvious is broken after compiling.
 */
public class BasicPDFTranscoderTestCase extends AbstractBasicTranscoderTest {

    @Override
    protected Transcoder createTranscoder() {
        return new PDFTranscoder();
    }

    @Test
    public void testFontAutoDetect() {
        //Create transcoder
        PDFTranscoder transcoder = (PDFTranscoder) createTranscoder();

        Configuration effectiveConfiguration = transcoder.getEffectiveConfiguration();
        Configuration autoDetectConf = effectiveConfiguration.getChild("fonts").getChild("auto-detect");

        assertEquals("The auto-detect conf must be added to it's parent (fonts) "
                        + "before the parent (fonts) is added to cfg",
                "DefaultConfiguration",
                autoDetectConf.getClass().getSimpleName());
    }

    @Test
    public void testFontSubstitution() throws ConfigurationException, IOException, TranscoderException {

        PDFTranscoder transcoder = (PDFTranscoder) createTranscoder();

        DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
        String cfgFragment =
            "<pdf-renderer>"
                + "<fonts>"
                    + "<substitutions>"
                        + "<substitution>"
                            + "<from font-family=\"Helvetica\"/>"
                            + "<to font-family=\"Courier\"/>"
                        + "</substitution>"
                    + "</substitutions>"
                + "</fonts>"
            + "</pdf-renderer>";
        Configuration cfg = cfgBuilder.build(new ByteArrayInputStream(cfgFragment.getBytes()));
        transcoder.configure(cfg);

        String svgFragment = "<svg xml:space=\"preserve\" x=\"-1.70458in\" y=\"0.198315in\" "
                + "width=\"2.6622in\" height=\"1.89672in\""
                + "     viewBox=\"-4330 0 6762 4818\" xmlns=\"http://www.w3.org/2000/svg\">"
                + "  <text x=\"-3653\" y=\"841\" style=\"fill:#1F1A17;font-size:639;font-family:Helvetica\">H</text>"
                + "</svg>";
        TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(svgFragment.getBytes()));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(os);

        try {
            transcoder.transcode(input, output);
        } finally {
            os.close();
        }

        try (PDDocument pdfDocument = Loader.loadPDF(os.toByteArray())) {
            FontExtractor fontExtractor = new FontExtractor();
            fontExtractor.getText(pdfDocument);
            assertEquals("Courier", fontExtractor.getFontUsage().get("H"));
        }
    }

    static class FontExtractor extends PDFTextStripper {

        private Map<String, String> fontUsage = new HashMap<>();

        @Override
        protected void processTextPosition(TextPosition text) {
            String fontName = text.getFont().getName();
            fontUsage.put(text.toString(), fontName);
            super.processTextPosition(text);
        }

        public Map<String, String> getFontUsage() {
            return fontUsage;
        }
    }

}
