/*
 * $Id: PDFResources.java,v 1.17 2003/03/07 08:25:47 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.pdf;

import org.apache.fop.apps.Document;
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
   public void addFonts(PDFDocument doc, Document fontInfo) {
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
