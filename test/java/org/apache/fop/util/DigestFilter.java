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
    private byte value[];
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
        if(feature.equals("http://xml.org/sax/features/namespaces")) {
            isNamespaceAware = value;
        }
        super.setFeature(feature, value);
    }

}
