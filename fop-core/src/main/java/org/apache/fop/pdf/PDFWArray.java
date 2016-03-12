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

package org.apache.fop.pdf;

import java.util.List;

/**
 * Class representing a <b>W</b> array for CID fonts.
 */
public class PDFWArray {

    /**
     * The metrics
     */
    private List entries = new java.util.ArrayList();

    /**
     * Default constructor
     */
    public PDFWArray() {
    }

    /**
     * Convenience constructor
     * @param metrics the metrics array to initially add
     */
    public PDFWArray(int[] metrics) {
        addEntry(0, metrics);
    }

    /**
     * Add an entry for single starting CID.
     * i.e. in the form "c [w ...]"
     *
     * @param start the starting CID value.
     * @param metrics the metrics array.
     */
    public void addEntry(int start, int[] metrics) {
        entries.add(new Entry(start, metrics));
    }

    /**
     * Add an entry for a range of CIDs (/W element on p 213)
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
     * Add an entry for a range of CIDs (/W2 element on p 210)
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

    /**
     * Convert this object to PDF code.
     * @return byte[] the PDF code
     */
    public byte[] toPDF() {
        return PDFDocument.encode(toPDFString());
    }

    /**
     * Convert this object to PDF code.
     * @return String the PDF code
     */
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
     * Inner class for entries in the form "c [w ...]"
     */
    private static class Entry {
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
