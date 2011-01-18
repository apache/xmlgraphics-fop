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

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.fop.fonts.FontDescriptor;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.base14.Symbol;
import org.apache.fop.fonts.base14.ZapfDingbats;
import org.apache.fop.util.ColorProfileUtil;

/**
 * Class representing a /Resources object.
 *
 * /Resources object contain a list of references to the fonts for the
 * document
 */
public class PDFResources extends PDFDictionary {

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

    /** Map of color spaces (key: color space name) */
    protected Map colorSpaces = new HashMap();

    /** Map of ICC color spaces (key: ICC profile description) */
    protected Map iccColorSpaces = new HashMap();

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
        Map<String, Typeface> usedFonts = fontInfo.getUsedFonts();
        for (String f : usedFonts.keySet()) {
            Typeface font = usedFonts.get(f);

            //Check if the font actually had any mapping operations. If not, it is an indication
            //that it has never actually been used and therefore doesn't have to be embedded.
            if (font.hadMappingOperations()) {
                FontDescriptor desc = null;
                if (font instanceof FontDescriptor) {
                    desc = (FontDescriptor)font;
                }
                String encoding = font.getEncodingName();
                if (font instanceof Symbol || font instanceof ZapfDingbats) {
                    encoding = null; //Symbolic fonts shouldn't specify an encoding value in PDF
                }
                addFont(doc.getFactory().makeFont(
                    f, font.getEmbedFontName(), encoding, font, desc));
            }
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
     * Add a ColorSpace dictionary to the resources.
     * @param colorSpace the color space
     */
    public void addColorSpace(PDFColorSpace colorSpace) {
        this.colorSpaces.put(new PDFName(colorSpace.getName()), colorSpace);
        if (colorSpace instanceof PDFICCBasedColorSpace) {
            PDFICCBasedColorSpace icc = (PDFICCBasedColorSpace)colorSpace;
            String desc = ColorProfileUtil.getICCProfileDescription(
                    icc.getICCStream().getICCProfile());
            this.iccColorSpaces.put(desc, colorSpace);
        }
    }

    /**
     * Returns a ICCBased color space by profile name.
     * @param desc the name of the color space
     * @return the requested color space or null if it wasn't found
     */
    public PDFICCBasedColorSpace getICCColorSpaceByProfileName(String desc) {
        PDFICCBasedColorSpace cs = (PDFICCBasedColorSpace)this.iccColorSpaces.get(desc);
        return cs;
    }

    /**
     * Returns a color space by name.
     * @param name the name of the color space
     * @return the requested color space or null if it wasn't found
     */
    public PDFColorSpace getColorSpace(PDFName name) {
        PDFColorSpace cs = (PDFColorSpace)this.colorSpaces.get(name);
        return cs;
    }

    /** {@inheritDoc} */
    protected int output(OutputStream stream) throws IOException {
        populateDictionary();
        return super.output(stream);
    }

    private void populateDictionary() {
        if (!this.fonts.isEmpty()) {
            PDFDictionary dict = new PDFDictionary(this);
            /* construct PDF dictionary of font object references */
            Iterator fontIterator = this.fonts.keySet().iterator();
            while (fontIterator.hasNext()) {
                String fontName = (String)fontIterator.next();
                dict.put(fontName, (PDFFont)this.fonts.get(fontName));
            }
            put("Font", dict);
        }

        if (!this.shadings.isEmpty()) {
            PDFDictionary dict = new PDFDictionary(this);
            for (Iterator iter = shadings.iterator(); iter.hasNext();) {
                PDFShading currentShading = (PDFShading)iter.next();
                dict.put(currentShading.getName(), currentShading);
            }
            put("Shading", dict);
        }

        if (!this.patterns.isEmpty()) {
            PDFDictionary dict = new PDFDictionary(this);
            for (Iterator iter = patterns.iterator(); iter.hasNext();) {
                PDFPattern currentPattern = (PDFPattern)iter.next();
                dict.put(currentPattern.getName(), currentPattern);
            }
            put("Pattern", dict);
        }

        PDFArray procset = new PDFArray(this);
        procset.add(new PDFName("PDF"));
        procset.add(new PDFName("ImageB"));
        procset.add(new PDFName("ImageC"));
        procset.add(new PDFName("Text"));
        put("ProcSet", procset);

        if (this.xObjects != null && !this.xObjects.isEmpty()) {
            PDFDictionary dict = new PDFDictionary(this);
            for (Iterator iter = xObjects.iterator(); iter.hasNext();) {
                PDFXObject xobj = (PDFXObject)iter.next();
                dict.put(xobj.getName().toString(), xobj);
            }
            put("XObject", dict);
        }

        if (!this.gstates.isEmpty()) {
            PDFDictionary dict = new PDFDictionary(this);
            for (Iterator iter = gstates.iterator(); iter.hasNext();) {
                PDFGState gs = (PDFGState)iter.next();
                dict.put(gs.getName(), gs);
            }
            put("ExtGState", dict);
        }

        if (!this.colorSpaces.isEmpty()) {
            PDFDictionary dict = new PDFDictionary(this);
            for (Iterator iter = colorSpaces.values().iterator(); iter.hasNext();) {
                PDFColorSpace colorSpace = (PDFColorSpace)iter.next();
                dict.put(colorSpace.getName(), colorSpace);
            }
            put("ColorSpace", dict);
        }
    }

}
