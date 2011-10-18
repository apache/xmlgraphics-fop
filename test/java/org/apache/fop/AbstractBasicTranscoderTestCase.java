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

package org.apache.fop;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;

/**
 * Basic runtime test for FOP's transcoders. It is used to verify that
 * nothing obvious is broken after compiling.
 */
public abstract class AbstractBasicTranscoderTestCase extends AbstractFOPTestCase {

    /**
     * Creates the transcoder to test.
     * @return the newly instantiated transcoder
     */
    protected abstract Transcoder createTranscoder();

    /**
     * Runs the PDF transcoder as if it were called by Batik's rasterizer.
     * Without special configuration stuff.
     * @throws Exception if a problem occurs
     */
    @Test
    public void testGenericPDFTranscoder() throws Exception {
        //Create transcoder
        Transcoder transcoder = createTranscoder();

        //Setup input
        File svgFile = new File(getBaseDir(), "test/resources/fop/svg/text.svg");
        InputStream in = new java.io.FileInputStream(svgFile);
        try {
            TranscoderInput input = new TranscoderInput(in);

            //Setup output
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                TranscoderOutput output = new TranscoderOutput(out);

                //Do the transformation
                transcoder.transcode(input, output);
            } finally {
                out.close();
            }
            assertTrue("Some output expected", out.size() > 0);
        } finally {
            in.close();
        }
    }

}
