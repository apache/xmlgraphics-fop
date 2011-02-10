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

package org.apache.fop.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * SAX filter which produces a digest over the XML elements.
 * Insignificant whitespace as determined by the, comments and
 * processing instructions are not part of the digest.
 * If the filter is namespace aware, the URI and local name for
 * each element is digested, otherwise the QName. Attributes are
 * sorted before the name-value pairs are digested.
 *
 */
public class DigestFilter extends XMLFilterImpl {

    private MessageDigest digest;
    private byte[] value;
    private boolean isNamespaceAware;

    public DigestFilter(String algorithm) throws NoSuchAlgorithmException {
        digest = MessageDigest.getInstance(algorithm);
    }

    public byte[] getDigestValue() {
        return value;
    }

    public String getDigestString() {
        if (value != null) {
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
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] chars, int start, int length)
        throws SAXException {
        digest.update(new String(chars, start, length).getBytes());
        super.characters(chars, start, length);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        value = digest.digest();
        super.endDocument();
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(
        String url,
        String localName,
        String qName,
        Attributes attr)
        throws SAXException {
        Map map = new TreeMap();
        if (isNamespaceAware) {
            digest.update(url.getBytes());
            digest.update(localName.getBytes());
            for (int i = 0; i < attr.getLength(); i++) {
                map.put(
                    attr.getLocalName(i) + attr.getURI(i),
                    attr.getValue(i));
            }
        } else {
            digest.update(qName.getBytes());
            for (int i = 0; i < attr.getLength(); i++) {
                map.put(attr.getQName(i), attr.getValue(i));
            }
        }
        for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry)i.next();
            digest.update(((String)entry.getKey()).getBytes());
            digest.update(((String)entry.getValue()).getBytes());
        }
        super.startElement(url, localName, qName, attr);
    }


    /* (non-Javadoc)
     * @see org.xml.sax.XMLReader#setFeature(java.lang.String, boolean)
     */
    public void setFeature(String feature, boolean value)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        if (feature.equals("http://xml.org/sax/features/namespaces")) {
            isNamespaceAware = value;
        }
        super.setFeature(feature, value);
    }

}
