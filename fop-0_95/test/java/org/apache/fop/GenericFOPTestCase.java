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

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Date;

import javax.xml.parsers.SAXParserFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
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

    // configure fopFactory as desired
    private FopFactory fopFactory = FopFactory.newInstance();
    
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

    /** @see junit.framework.TestCase#setUp() */
    protected void setUp() throws Exception {
        parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
    }

    public void testSimple() throws Exception {
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

    private String digestToString(byte[] value) {
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
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        foUserAgent.setCreationDate(new Date(10000));
        MessageDigest outDigest = MessageDigest.getInstance("MD5");
        DigestOutputStream out =
            new DigestOutputStream(new ByteArrayOutputStream(), outDigest);
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
        InputSource source = new InputSource(new StringReader(fo));
        DigestFilter filter = new DigestFilter("MD5");
        filter.setParent(parserFactory.newSAXParser().getXMLReader());
        filter.setContentHandler(fop.getDefaultHandler());
        filter.parse(source);
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
