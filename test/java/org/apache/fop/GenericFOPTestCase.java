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

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Date;

import javax.xml.parsers.SAXParserFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.fop.apps.Driver;
import org.apache.fop.render.pdf.PDFRenderer;
import org.apache.fop.util.DigestFilter;
import org.xml.sax.InputSource;

/**
 * Framework for simple regression testing.
 * The testcase reads a control XML file which specifies a FO source,
 * a MD5 for the source to help diferentiating failures caused by causal
 * source modification from failures caused by regression, a renderer (only
 * PDF currently supported) and a MD5 for the result.
 *  
 */
public final class GenericFOPTestCase extends TestCase {

    protected SAXParserFactory parserFactory;

    public static Test suite() {
        TestSuite suite = new TestSuite(GenericFOPTestCase.class);
        suite.setName("Fop regression tests");
        return suite;
    }

    /**
     * Constructor for FopTest.
     * @param name the name of the test suite
     */
    public GenericFOPTestCase(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
    }

    public final void testSimple() throws Exception {
        final String digestIn = "17bf13298796065f7775db8707133aeb";
        final String digestOut = "e2761f51152f6663911e567901596707";
        final String fo =
            "<fo:root xmlns:fo='http://www.w3.org/1999/XSL/Format'>"
                + "  <fo:layout-master-set>"
                + "    <fo:simple-page-master master-name='simple'"
                + "       page-height='25cm' page-width='20cm'>"
                + "       <fo:region-body/>"
                + "    </fo:simple-page-master>"
                + "  </fo:layout-master-set>"
                + "  <fo:page-sequence master-reference='simple'>"
                + "     <fo:flow flow-name='xsl-region-body'>"
                + "        <fo:block>This is a blind text.</fo:block>"
                + "     </fo:flow>"
                + "   </fo:page-sequence>"
                + "</fo:root>";
        renderPDF(fo, digestIn, digestOut);
    }

    private String digestToString(byte value[]) {
        StringBuffer buffer = new StringBuffer(2 * value.length);
        for (int i = 0; i < value.length; i++) {
            int val = value[i];
            int hi = (val >> 4) & 0xF;
            int lo = val & 0xF;
            if (hi < 10) {
                buffer.append((char) (hi + 0x30));
            } else {
                buffer.append((char) (hi + 0x61 - 10));
            }
            if (lo < 10) {
                buffer.append((char) (lo + 0x30));
            } else {
                buffer.append((char) (lo + 0x61 - 10));
            }
        }
        return buffer.toString();
    }

    private void renderPDF(String fo, String digestIn, String digestOut)
        throws Exception {
        PDFRenderer renderer = new PDFRenderer();
        renderer.setCreationDate(new Date(10000));
        MessageDigest outDigest = MessageDigest.getInstance("MD5");
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        DigestOutputStream out =
            new DigestOutputStream(new ByteArrayOutputStream(), outDigest);
        Driver driver = new Driver();
        driver.setRenderer(renderer);
        driver.setOutputStream(out);
        InputSource source = new InputSource(new StringReader(fo));
        DigestFilter filter = new DigestFilter("MD5");
        filter.setParent(parserFactory.newSAXParser().getXMLReader());
        driver.render(filter, source);
        String digestInActual = digestToString(filter.getDigestValue());
        if (!digestIn.equals(digestInActual)) {
            fail("input MD5: was " + digestInActual + ", expected " + digestIn);
        }
        String digestOutActual = digestToString(outDigest.digest());
        if (!digestOut.equals(digestOutActual)) {
            fail(
                "output MD5: was "
                    + digestOutActual
                    + ", expected "
                    + digestOut);
        }
    }

}
