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

import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.FontDescriptor;

// Java
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
    protected Map fonts = new HashMap();

    /**
     * Set of XObjects
     */
    protected Set xObjects = new HashSet();

    /**
     * Set of patterns
     */
    protected Set patterns = new HashSet();

    /**
     * Set of shadings
     */
    protected Set shadings = new HashSet();

    /**
     * Set of ExtGStates
     */
    protected Set gstates = new HashSet();

    /**
     * create a /Resources object.
     *
     * @param objnum the object's number
     */
    public PDFResources(int objnum) {
        /* generic creation of object */
        super();
        setObjectNumber(objnum);
    }

    /**
     * add font object to resources list.
     *
     * @param font the PDFFont to add
     */
    public void addFont(PDFFont font) {
        this.fonts.put(font.getName(), font);
    }

    /**
     * Add the fonts in the font info to this PDF document's Font Resources.
     * 
     * @param doc PDF document to add fonts to
     * @param fontInfo font info object to get font information from
     */
   public void addFonts(PDFDocument doc, FontInfo fontInfo) {
        Map fonts = fontInfo.getUsedFonts();
        Iterator e = fonts.keySet().iterator();
        while (e.hasNext()) {
            String f = (String)e.next();
            Typeface font = (Typeface)fonts.get(f);
            FontDescriptor desc = null;
            if (font instanceof FontDescriptor) {
                desc = (FontDescriptor)font;
            }
            addFont(doc.getFactory().makeFont(
                f, font.getFontName(), font.getEncoding(), font, desc));
        }
    }

    /**
     * Add a PDFGState to the resources.
     *
     * @param gs the PDFGState to add
     */
    public void addGState(PDFGState gs) {
        this.gstates.add(gs);
    }

    /**
     * Add a Shading to the resources.
     *
     * @param theShading the shading to add
     */
    public void addShading(PDFShading theShading) {
        this.shadings.add(theShading);
    }

    /**
     * Add the pattern to the resources.
     *
     * @param thePattern the pattern to add
     */
    public void addPattern(PDFPattern thePattern) {
        this.patterns.add(thePattern);
    }

    /**
     * Add an XObject to the resources.
     *
     * @param xObject the XObject to add
     */
    public void addXObject(PDFXObject xObject) {
        this.xObjects.add(xObject);
    }

    /**
     * represent the object in PDF
     * This adds the references to all the objects in the current
     * resource context.
     *
     * @return the PDF
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        StringBuffer p = new StringBuffer(128);
        p.append(getObjectID() + "<<\n");
        if (!this.fonts.isEmpty()) {
            p.append("/Font <<\n");

            /* construct PDF dictionary of font object references */
            Iterator fontIterator = this.fonts.keySet().iterator();
            while (fontIterator.hasNext()) {
                String fontName = (String)fontIterator.next();
                p.append("  /" + fontName + " "
                         + ((PDFFont)this.fonts.get(fontName)).referencePDF()
                         + "\n");
            }

            p.append(">>\n");
        }

        PDFShading currentShading = null;
        if (!this.shadings.isEmpty()) {
            p.append("/Shading <<\n");

            for (Iterator iter = shadings.iterator(); iter.hasNext();) {
                currentShading = (PDFShading)iter.next();
                p.append("  /" + currentShading.getName() + " "
                         + currentShading.referencePDF() + " ");    // \n ??????
            }

            p.append(">>\n");
        }
        // "free" the memory. Sorta.
        currentShading = null;

        PDFPattern currentPattern = null;
        if (!this.patterns.isEmpty()) {
            p.append("/Pattern <<\n");

            for (Iterator iter = patterns.iterator(); iter.hasNext();) {
                currentPattern = (PDFPattern)iter.next();
                p.append("  /" + currentPattern.getName() + " "
                         + currentPattern.referencePDF() + " ");
            }

            p.append(">>\n");
        }
        // "free" the memory. Sorta.
        currentPattern = null;

        p.append("/ProcSet [ /PDF /ImageB /ImageC /Text ]\n");

        if (this.xObjects != null && !this.xObjects.isEmpty()) {
            p = p.append("/XObject <<\n");
            for (Iterator iter = xObjects.iterator(); iter.hasNext();) {
                PDFXObject xobj = (PDFXObject)iter.next();
                p = p.append("  /Im" + xobj.getXNumber() + " "
                             + xobj.referencePDF()
                             + "\n");
            }
            p = p.append(">>\n");
        }

        if (!this.gstates.isEmpty()) {
            p = p.append("/ExtGState <<\n");
            for (Iterator iter = gstates.iterator(); iter.hasNext();) {
                PDFGState gs = (PDFGState)iter.next();
                p = p.append("  /" + gs.getName() + " "
                             + gs.referencePDF()
                             + "\n");
            }
            p = p.append(">>\n");
        }

        p = p.append(">>\nendobj\n");

        return p.toString();
    }

}
