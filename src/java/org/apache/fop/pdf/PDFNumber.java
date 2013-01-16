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

import org.apache.xmlgraphics.util.DoubleFormatUtil;

/**
 * This class represents a simple number object. It also contains contains some
 * utility methods for outputting numbers to PDF.
 */
public class PDFNumber extends PDFObject {

    private Number number;

    /**
     * Returns the number.
     * @return the number
     */
    public Number getNumber() {
        return this.number;
    }

    /**
     * Sets the number.
     * @param number the number
     */
    public void setNumber(Number number) {
        this.number = number;
    }

    /**
     * Output a Double value to a string suitable for PDF.
     *
     * @param doubleDown the Double value
     * @return the value as a string
     */
    public static String doubleOut(Double doubleDown) {
        return doubleOut(doubleDown.doubleValue());
    }

    /**
     * Output a double value to a string suitable for PDF (6 decimal digits).
     *
     * @param doubleDown the double value
     * @return the value as a string
     */
    public static String doubleOut(double doubleDown) {
        return doubleOut(doubleDown, 6);
    }

    /**
     * Output a double value to a string suitable for PDF.
     * In this method it is possible to set the maximum
     * number of decimal places to output.
     *
     * @param doubleDown the Double value
     * @param dec the number of decimal places to output
     * @return the value as a string
     */
    public static String doubleOut(double doubleDown, int dec) {
        if (dec < 0 || dec > 16) {
            throw new IllegalArgumentException("Parameter dec must be between 1 and 16");
        }
        StringBuffer buf = new StringBuffer();
        DoubleFormatUtil.formatDouble(doubleDown, dec, dec, buf);
        return buf.toString();
    }

    /**
     * Append a double value to a string buffer suitable for PDF.
     * In this method it is possible to set the maximum
     * number of decimal places to output.
     *
     * @param doubleDown the Double value
     * @param dec the number of decimal places to output
     * @param buf the string buffer to which double is formatted (appended)
     * @return the string buffer
     */
    public static StringBuffer doubleOut(double doubleDown, int dec, StringBuffer buf) {
        if (dec < 0 || dec > 16) {
            throw new IllegalArgumentException("Parameter dec must be between 1 and 16");
        }
        DoubleFormatUtil.formatDouble(doubleDown, dec, dec, buf);
        return buf;
    }

    /** {@inheritDoc} */
    protected String toPDFString() {
        if (getNumber() == null) {
            throw new IllegalArgumentException(
                "The number of this PDFNumber must not be empty");
        }
        StringBuffer sb = new StringBuffer(64);
        sb.append(doubleOut(getNumber().doubleValue(), 10));
        return sb.toString();
    }

}

