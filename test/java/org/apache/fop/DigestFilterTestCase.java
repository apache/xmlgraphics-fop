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

import java.io.IOException;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;

import org.apache.fop.util.DigestFilter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Test case for digesting SAX filter.
 *
 */
public class DigestFilterTestCase extends TestCase {

    private SAXParserFactory parserFactory;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() {
        parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
    }

    private boolean compareDigest(byte a[], byte b[]) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    private String digestToString(byte digest[]) {
        StringBuffer buffer = new StringBuffer(2 * digest.length);
        for (int i = 0; i < digest.length; i++) {
            int val = digest[i];
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

    private byte[] runTest(String input)
        throws
            NoSuchAlgorithmException,
            ParserConfigurationException,
            SAXException,
            IOException {
        XMLReader parser = parserFactory.newSAXParser().getXMLReader();
        DigestFilter digestFilter = new DigestFilter("MD5");
        digestFilter.setParent(parser);
        digestFilter.setFeature("http://xml.org/sax/features/namespaces",true);
        parser.setContentHandler(digestFilter);
        InputSource inputSource = new InputSource(new StringReader(input));
        parser.parse(inputSource);
        return digestFilter.getDigestValue();
    }

    public final void testLineFeed()
        throws
            NoSuchAlgorithmException,
            ParserConfigurationException,
            SAXException,
            IOException {
        byte lfDigest[] = runTest("<a>\n</a>");
        byte crlfDigest[] = runTest("<a>\r\n</a>");
        assertTrue(
            "LF: "
                + digestToString(lfDigest)
                + " CRLF: "
                + digestToString(crlfDigest),
            compareDigest(lfDigest, crlfDigest));
    }

    public final void testAttributeOrder()
        throws
            NoSuchAlgorithmException,
            ParserConfigurationException,
            SAXException,
            IOException {
        byte sortDigest[] = runTest("<a a1='1' a2='2' a3='3'/>");
        byte permutationDigest[] = runTest("<a a2='2' a3='3' a1='1'/>");
        assertTrue(
            "Sort: "
                + digestToString(sortDigest)
                + " permuted: "
                + digestToString(permutationDigest),
            compareDigest(sortDigest, permutationDigest));
        byte reverseDigest[] = runTest("<a a3='3' a2='2' a1='1'/>");
        assertTrue(
            "Sort: "
                + digestToString(sortDigest)
                + " permuted: "
                + digestToString(reverseDigest),
            compareDigest(sortDigest, reverseDigest));
    }

    public final void testNamespacePrefix()
        throws
            NoSuchAlgorithmException,
            ParserConfigurationException,
            SAXException,
            IOException {
        byte prefix1Digest[] = runTest("<a:a xmlns:a='foo'/>");
        byte prefix2Digest[] = runTest("<b:a xmlns:b='foo'/>");
        assertTrue(
            "prefix1: "
                + digestToString(prefix1Digest)
                + " prefix2: "
                + digestToString(prefix2Digest),
            compareDigest(prefix1Digest, prefix2Digest));
    }

}
