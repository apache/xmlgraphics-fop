/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop;

import java.io.File;
import java.io.InputStream;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * Basic runtime test for FOP's transcoders. It is used to verify that 
 * nothing obvious is broken after compiling.
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 */
public abstract class AbstractBasicTranscoderTestCase extends AbstractFOPTestCase {

    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public AbstractBasicTranscoderTestCase(String name) {
        super(name);
    }

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
