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

    /** @see junit.framework.TestCase#setUp() */
    protected void setUp() {
        parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
    }

    private boolean compareDigest(byte[] a, byte[] b) {
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

    private String digestToString(byte[] digest) {
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
        digestFilter.setFeature("http://xml.org/sax/features/namespaces", true);
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
        byte[] lfDigest = runTest("<a>\n</a>");
        byte[] crlfDigest = runTest("<a>\r\n</a>");
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
        byte[] sortDigest = runTest("<a a1='1' a2='2' a3='3'/>");
        byte[] permutationDigest = runTest("<a a2='2' a3='3' a1='1'/>");
        assertTrue(
            "Sort: "
                + digestToString(sortDigest)
                + " permuted: "
                + digestToString(permutationDigest),
            compareDigest(sortDigest, permutationDigest));
        byte[] reverseDigest = runTest("<a a3='3' a2='2' a1='1'/>");
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
        byte[] prefix1Digest = runTest("<a:a xmlns:a='foo'/>");
        byte[] prefix2Digest = runTest("<b:a xmlns:b='foo'/>");
        assertTrue(
            "prefix1: "
                + digestToString(prefix1Digest)
                + " prefix2: "
                + digestToString(prefix2Digest),
            compareDigest(prefix1Digest, prefix2Digest));
    }

}
