/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

// Java
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * class representing a /Resources object.
 *
 * /Resources object contain a list of references to the fonts for the
 * document
 */
public class PDFResources extends PDFObject {

    /**
     * /Font objects keyed by their internal name
     */
    protected HashMap fonts = new HashMap();

    protected HashSet xObjects = new HashSet();
    protected HashSet patterns = new HashSet();
    protected HashSet shadings = new HashSet();
    protected HashSet gstates = new HashSet();

    /**
     * create a /Resources object.
     *
     * @param number the object's number
     */
    public PDFResources(int number) {
        /* generic creation of object */
        super(number);
    }

    /**
     * add font object to resources list.
     *
     * @param font the PDFFont to add
     */
    public void addFont(PDFFont font) {
        this.fonts.put(font.getName(), font);
    }

    public void addGState(PDFGState gs) {
        this.gstates.add(gs);
    }

    public void addShading(PDFShading theShading) {
        this.shadings.add(theShading);
    }

    public void addPattern(PDFPattern thePattern) {
        this.patterns.add(thePattern);
    }

    public void addXObject(PDFXObject xObject) {
        this.xObjects.add(xObject);
    }

    /**
     * represent the object in PDF
     *
     * @return the PDF
     */
    public byte[] toPDF() {
        StringBuffer p = new StringBuffer(this.number + " " + this.generation
                                          + " obj\n<< \n");
        if (!this.fonts.isEmpty()) {
            p.append("/Font << ");

            /* construct PDF dictionary of font object references */
            Iterator fontIterator = this.fonts.keySet().iterator();
            while (fontIterator.hasNext()) {
                String fontName = (String)fontIterator.next();
                p.append("/" + fontName + " "
                         + ((PDFFont)this.fonts.get(fontName)).referencePDF()
                         + " ");
            }

            p.append(">> \n");
        }

        PDFShading currentShading = null;
        if (!this.shadings.isEmpty()) {
            p.append("/Shading << ");

            for (Iterator iter = shadings.iterator(); iter.hasNext(); ) {
                currentShading = (PDFShading)iter.next();
                p.append("/" + currentShading.getName() + " "
                         + currentShading.referencePDF() + " ");    // \n ??????
            }

            p.append(">>\n");
        }
        // "free" the memory. Sorta.
        currentShading = null;

        PDFPattern currentPattern = null;
        if (!this.patterns.isEmpty()) {
            p.append("/Pattern << ");

            for (Iterator iter = patterns.iterator(); iter.hasNext(); ) {
                currentPattern = (PDFPattern)iter.next();
                p.append("/" + currentPattern.getName() + " "
                         + currentPattern.referencePDF() + " ");
            }

            p.append(">> \n");
        }
        // "free" the memory. Sorta.
        currentPattern = null;

        p.append("/ProcSet [ /PDF /ImageC /Text ]\n");

        if (this.xObjects != null && !this.xObjects.isEmpty()) {
            p = p.append("/XObject <<");
            for (Iterator iter = xObjects.iterator(); iter.hasNext(); ) {
                PDFXObject xobj = (PDFXObject)iter.next();
                p = p.append("/Im" + xobj.getXNumber() + " "
                             + xobj.referencePDF()
                             + "\n");
            }
            p = p.append(" >>\n");
        }

        if (!this.gstates.isEmpty()) {
            p = p.append("/ExtGState <<");
            for (Iterator iter = gstates.iterator(); iter.hasNext(); ) {
                PDFGState gs = (PDFGState)iter.next();
                p = p.append("/" + gs.getName() + " "
                             + gs.referencePDF()
                             + " ");
            }
            p = p.append(">>\n");
        }

        p = p.append(">>\nendobj\n");

        return p.toString().getBytes();
    }

}
