/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.util.HashMap;
import java.util.Iterator;

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

    public static final PDFGState DEFAULT;

    static {
        DEFAULT = new PDFGState(0);
        HashMap vals = DEFAULT.values;
        /*vals.put(LW, new Float(1.0));
        vals.put(LC, new Integer(0));
        vals.put(LJ, new Integer(0));
        vals.put(ML, new Float(10.0));
        vals.put(D, "0 []");
        vals.put(RI, "RelativeColorimetric");
        vals.put(OP, Boolean.FALSE);
        vals.put(op, Boolean.FALSE);
        vals.put(OPM, new Integer(1));
        vals.put(Font, "");*/
        vals.put(CA, new Float(1.0));
        vals.put(ca, new Float(1.0));
    }

    HashMap values = new HashMap();

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
        if (fill) {
            values.put(ca, new Float(val));
        } else {
            values.put(CA, new Float(val));
        }
    }

    public void addValues(PDFGState state) {
        values.putAll(state.values);
    }

    public void addValues(HashMap vals) {
        values.putAll(vals);
    }

    /**
     * represent the object in PDF
     *
     * @return the PDF string
     */
    public byte[] toPDF() {
        StringBuffer sb = new StringBuffer(this.number + " " + this.generation
                              + " obj\n<<\n/Type /ExtGState\n");
        appendVal(sb, ca);
        appendVal(sb, CA);

        sb.append(">>\nendobj\n");
        return sb.toString().getBytes();
    }

    private void appendVal(StringBuffer sb, String name) {
        Object val = values.get(name);
        if (val != null) {
            sb.append("/" + name + " " + val + "\n");
        }
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

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PDFGState)) {
            return false;
        }
        HashMap vals1 = values;
        HashMap vals2 = ((PDFGState)obj).values;
        if (vals1.size() != vals2.size()) {
            return false;
        }
        for(Iterator iter = vals1.keySet().iterator(); iter.hasNext();) {
            Object str = iter.next();
            Object obj1 = vals1.get(str);
            if (!obj1.equals(vals2.get(str))) {
                return false;
            }
        }
        return true;
    }
}

