/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

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
        return toPDFString().getBytes();
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
