/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
     * Output a double value to a string suitable for PDF.
     *
     * @param doubleDown the double value
     * @return the value as a string
     */
    public static String doubleOut(double doubleDown) {
        StringBuffer p = new StringBuffer();
        if (doubleDown < 0) {
            doubleDown = -doubleDown;
            p.append("-");
        }
        double trouble = doubleDown % 1;

        if (trouble > 0.950) {
            p.append((int)doubleDown + 1);
        } else if (trouble < 0.050) {
            p.append((int)doubleDown);
        } else {
            String doubleString = new String(doubleDown + "");
            int decimal = doubleString.indexOf(".");
            if (decimal != -1) {
                p.append(doubleString.substring(0, decimal));

                if ((doubleString.length() - decimal) > 6) {
                    p.append(doubleString.substring(decimal, decimal + 6));
                } else {
                    p.append(doubleString.substring(decimal));
                }
            } else {
                p.append(doubleString);
            }
        }
        return (p.toString());
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
        StringBuffer p = new StringBuffer();
        if (doubleDown < 0) {
            doubleDown = -doubleDown;
            p.append("-");
        }
        double trouble = doubleDown % 1;

        if (trouble > (1.0 - (5.0 / (Math.pow(10.0, dec))))) {
            p.append((int)doubleDown + 1);
        } else if (trouble < (5.0 / (Math.pow(10.0, dec)))) {
            p.append((int)doubleDown);
        } else {
            String doubleString = new String(doubleDown + "");
            int decimal = doubleString.indexOf(".");
            if (decimal != -1) {
                p.append(doubleString.substring(0, decimal));

                if ((doubleString.length() - decimal) > dec) {
                    p.append(doubleString.substring(decimal, decimal + dec));
                } else {
                    p.append(doubleString.substring(decimal));
                }
            } else {
                p.append(doubleString);
            }
        }
        return (p.toString());
    }

    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    protected String toPDFString() {
        if (getNumber() == null) {
            throw new IllegalArgumentException(
                "The number of this PDFNumber must not be empty");
        }
        StringBuffer sb = new StringBuffer(64);
        sb.append(getObjectID());
        sb.append(getNumber().toString());
        sb.append("\nendobj\n");
        return sb.toString();
    }

}

