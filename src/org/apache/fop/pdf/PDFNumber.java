/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

public class PDFNumber {

    public static String doubleOut(Double doubleDown) {
        return doubleOut(doubleDown.doubleValue());
    }

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
            String doubleString = Double.toString(doubleDown);
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
            String doubleString = Double.toString(doubleDown);
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

}

