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
package org.apache.fop.pdf;

// Java
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * class representing a <b>W</b> array for CID fonts.
 */
public class PDFWArray {

    /**
     * the metrics
     */
    private ArrayList entries;

    public PDFWArray() {
        entries = new ArrayList();
    }

    /**
     * add an entry for single starting CID.
     * i.e. in the form "c [w ...]"
     *
     * @param start the starting CID value.
     * @param metrics the metrics array.
     */
    public void addEntry(int start, int[] metrics) {
        entries.add(new Entry(start, metrics));
    }

    /**
     * add an entry for a range of CIDs (/W element on p 213)
     *
     * @param first the first CID in the range
     * @param last the last CID in the range
     * @param width the width for all CIDs in the range
     */
    public void addEntry(int first, int last, int width) {
        entries.add(new int[] {
            first, last, width
        });
    }

    /**
     * add an entry for a range of CIDs (/W2 element on p 210)
     *
     * @param first the first CID in the range
     * @param last the last CID in the range
     * @param width the width for all CIDs in the range
     * @param posX the x component for the vertical position vector
     * @param posY the y component for the vertical position vector
     */
    public void addEntry(int first, int last, int width, int posX, int posY) {
        entries.add(new int[] {
            first, last, width, posX, posY
        });
    }

    public byte[] toPDF() {
        try {
            return toPDFString().getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            return toPDFString().getBytes();
        }       
    }

    public String toPDFString() {
        StringBuffer p = new StringBuffer();
        p.append("[ ");
        int len = entries.size();
        for (int i = 0; i < len; i++) {
            Object entry = entries.get(i);
            if (entry instanceof int[]) {
                int[] line = (int[])entry;
                for (int j = 0; j < line.length; j++) {
                    p.append(line[j]);
                    p.append(" ");
                }
            } else {
                ((Entry)entry).fillInPDF(p);
            }
        }
        p.append("]");
        return p.toString();
    }

    /**
     * inner class for entries in the form "c [w ...]"
     */
    private static class Entry {
        private static final StringBuffer p = new StringBuffer();
        private int start;
        private int[] metrics;
        public Entry(int s, int[] m) {
            start = s;
            metrics = m;
        }

        public void fillInPDF(StringBuffer p) {
            // p.setLength(0);
            p.append(start);
            p.append(" [");
            for (int i = 0; i < metrics.length; i++) {
                p.append(this.metrics[i]);
                p.append(" ");
            }
            p.append("] ");
        }

    }
}
