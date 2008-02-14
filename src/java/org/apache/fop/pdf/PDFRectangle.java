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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * class representing a rectangle
 *
 * Rectangles are specified on page 183 of the PDF 1.3 spec.
 */
public class PDFRectangle implements PDFWritable {

    /**
     * lower left x coordinate
     */
    protected int llx;

    /**
     * lower left y coordinate
     */
    protected int lly;

    /**
     * upper right x coordinate
     */
    protected int urx;

    /**
     * upper right y coordinate
     */
    protected int ury;

    /**
     * create a rectangle giving the four separate values
     *
     * @param llx  lower left x coordinate
     * @param lly  lower left y coordinate
     * @param urx  upper right x coordinate
     * @param ury  upper right y coordinate
     */
    public PDFRectangle(int llx, int lly, int urx, int ury) {
        this.llx = llx;
        this.lly = lly;
        this.urx = urx;
        this.ury = ury;
    }

    /**
     * create a rectangle giving an array of four values
     *
     * @param array values in the order llx, lly, urx, ury
     */
    public PDFRectangle(int[] array) {
        this.llx = array[0];
        this.lly = array[1];
        this.urx = array[2];
        this.ury = array[3];
    }

    private String format() {
        return "[" + llx + " " + lly + " " + urx + " " + ury + "]";
    }

    /** {@inheritDoc} */
    public String toString() {
        return "PDFRectangle" + format();
    }

    /** {@inheritDoc} */
    public void outputInline(OutputStream out, Writer writer) throws IOException {
        writer.write(format());
    }
}
