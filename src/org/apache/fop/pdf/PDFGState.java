/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

/**
 * class representing a /ExtGState object.
 *
 */
public class PDFGState extends PDFObject {
    public static final String LW = "lw";
    public static final String LC = "lc";
    public static final String LJ = "lj";
    public static final String ML = "ml";
    public static final String D = "d";
    public static final String RI = "ri";
    public static final String OP = "OP";
    public static final String op = "op";
    public static final String OPM = "opm";
    public static final String Font = "font";
    public static final String BG = "bg";
    public static final String BG2 = "bg2";
    public static final String UCR = "ucr";
    public static final String UCR2 = "ucr2";
    public static final String TR = "tr";
    public static final String TR2 = "tr2";
    public static final String HT = "ht";
    public static final String FL = "fl";
    public static final String SM = "sm";
    public static final String SA = "sa";
    public static final String BM = "bm";
    public static final String SMask = "smask";
    public static final String CA = "CA";
    public static final String ca = "ca";
    public static final String AIS = "ais";
    public static final String TK = "tk";

    float alphaFill = 1;
    float alphaStroke = 1;

    /**
     * create a /ExtGState object.
     *
     * @param number the object's number
     * @param pageReference the pageReference represented by this object
     */
    public PDFGState(int number) {

        /* generic creation of object */
        super(number);

    }

    public String getName() {
        return "GS" + this.number;
    }

    public void setAlpha(float val, boolean fill) {
        if(fill) {
            alphaFill = val;
        } else {
            alphaStroke = val;
        }
    }

    /**
     * represent the object in PDF
     *
     * @return the PDF string
     */
    public byte[] toPDF() {
        StringBuffer sb = new StringBuffer(this.number + " " + this.generation
                              + " obj\n<<\n/Type /ExtGState\n");
        if(alphaFill != 1) {
            sb.append("/ca " + alphaFill + "\n");
        }
        if(alphaStroke != 1) {
            sb.append("/CA " + alphaStroke + "\n");
        }
        sb.append(">>\nendobj\n");
        return sb.toString().getBytes();
    }

    /*
     * example
     * 29 0 obj
     * <<
     * /Type /ExtGState
     * /ca 0.5
     * >>
     * endobj
     */
}
