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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * This class represents a simple number object. It also contains contains some
 * utility methods for outputing numbers to PDF.
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

    // Static cache. Possible concurrency implications. See comment in doubleOut(double, int).
    private static DecimalFormat[] decimalFormatCache = new DecimalFormat[17];

    private static final String BASE_FORMAT = "0.################";

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
        if (dec < 0 || dec >= decimalFormatCache.length) {
            throw new IllegalArgumentException("Parameter dec must be between 1 and "
                    + (decimalFormatCache.length + 1));
        }
        if (decimalFormatCache[dec] == null) {
            //We don't care about the rare case where a DecimalFormat might be replaced in
            //a multi-threaded environment, so we don't synchronize the access to the static
            //array (mainly for performance reasons). After all, the DecimalFormat instances
            //read-only objects so it doesn't matter which instance is used as long as one
            //is available.
            String s = "0";
            if (dec > 0) {
                s = BASE_FORMAT.substring(0, dec + 2);
            }
            DecimalFormat df = new DecimalFormat(s, new DecimalFormatSymbols(Locale.US));
            decimalFormatCache[dec] = df;
        }
        return decimalFormatCache[dec].format(doubleDown);
    }

    /**
     * {@inheritDoc}
     */
    protected String toPDFString() {
        if (getNumber() == null) {
            throw new IllegalArgumentException(
                "The number of this PDFNumber must not be empty");
        }
        StringBuffer sb = new StringBuffer(64);
        if (hasObjectNumber()) {
            sb.append(getObjectID());
        }
        sb.append(getNumber().toString());
        if (hasObjectNumber()) {
            sb.append("\nendobj\n");
        }
        return sb.toString();
    }

}

