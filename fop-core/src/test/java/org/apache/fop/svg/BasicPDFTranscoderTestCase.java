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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.apache.batik.transcoder.Transcoder;

import org.apache.fop.configuration.Configuration;

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

}
