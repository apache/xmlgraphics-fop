/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

// Java
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

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
    protected Hashtable fonts = new Hashtable();

    protected Vector xObjects = null;
    protected Vector patterns = new Vector();
    protected Vector shadings = new Vector();

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

    public void addShading(PDFShading theShading) {
        this.shadings.addElement(theShading);
    }

    public void addPattern(PDFPattern thePattern) {
        this.patterns.addElement(thePattern);
    }

    public void setXObjects(Vector xObjects) {
        this.xObjects = xObjects;
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
            Enumeration fontEnumeration = this.fonts.keys();
            while (fontEnumeration.hasMoreElements()) {
                String fontName = (String)fontEnumeration.nextElement();
                p.append("/" + fontName + " "
                         + ((PDFFont)this.fonts.get(fontName)).referencePDF()
                         + " ");
            }

            p.append(">> \n");
        }

        PDFShading currentShading = null;
        if (!this.shadings.isEmpty()) {
            p.append("/Shading << ");

            for (int currentShadingNumber = 0;
                    currentShadingNumber < this.shadings.size();
                    currentShadingNumber++) {
                currentShading =
                    ((PDFShading)this.shadings.elementAt(currentShadingNumber));

                p.append("/" + currentShading.getName() + " "
                         + currentShading.referencePDF() + " ");    // \n ??????
            }

            p.append(">> \n");
        }
        // "free" the memory. Sorta.
        currentShading = null;

        PDFPattern currentPattern = null;
        if (!this.patterns.isEmpty()) {
            p.append("/Pattern << ");

            for (int currentPatternNumber = 0;
                    currentPatternNumber < this.patterns.size();
                    currentPatternNumber++) {
                currentPattern =
                    ((PDFPattern)this.patterns.elementAt(currentPatternNumber));

                p.append("/" + currentPattern.getName() + " "
                         + currentPattern.referencePDF() + " ");
            }

            p.append(">> \n");
        }
        // "free" the memory. Sorta.
        currentPattern = null;

        p.append("/ProcSet [ /PDF /ImageC /Text ] ");

        if (!this.xObjects.isEmpty()) {
            p = p.append("/XObject <<");
            for (int i = 1; i <= this.xObjects.size(); i++) {
                p = p.append("/Im" + i + " "
                             + ((PDFXObject)this.xObjects.elementAt(i - 1)).referencePDF()
                             + " \n");
            }
            p = p.append(" >>\n");
        }

        p = p.append(">> \nendobj\n");

        return p.toString().getBytes();
    }

}
