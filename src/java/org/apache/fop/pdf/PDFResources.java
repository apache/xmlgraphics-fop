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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil;

import org.apache.fop.fonts.FontDescriptor;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.base14.Symbol;
import org.apache.fop.fonts.base14.ZapfDingbats;

/**
 * Class representing a /Resources object.
 *
 * /Resources object contain a list of references to the fonts, patterns,
 * shadings, etc.,  for the document.
 */
public class PDFResources extends PDFDictionary {

    /**
     * /Font objects keyed by their internal name
     */
    protected Map<String, PDFDictionary> fonts = new LinkedHashMap<String, PDFDictionary>();

    /**
     * Set of XObjects
     */
    protected Set<PDFXObject> xObjects = new LinkedHashSet<PDFXObject>();
    /** Map of color spaces (key: color space name) */
    protected Map<PDFName, PDFColorSpace> colorSpaces = new LinkedHashMap<PDFName, PDFColorSpace>();

    /** Map of ICC color spaces (key: ICC profile description) */
    protected Map<String, PDFICCBasedColorSpace> iccColorSpaces = new LinkedHashMap<String, PDFICCBasedColorSpace>();

    private PDFResources parent;
    private PDFDictionary fontsObj;
    private Map<String, PDFDictionary> fontsObjDict = new LinkedHashMap<String, PDFDictionary>();

    /** Named properties */
    protected Map<String, PDFReference> properties = new LinkedHashMap<String, PDFReference>();

    protected Set<PDFResourceContext> contexts = new LinkedHashSet<PDFResourceContext>();

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

    public void addContext(PDFResourceContext c) {
        contexts.add(c);
    }

    public void setParentResources(PDFResources p) {
        parent = p;
    }

    public PDFResources getParentResources() {
        return parent;
    }

    /**
     * add font object to resources list.
     *
     * @param font the PDFFont to add
     */
    public void addFont(PDFFont font) {
        addFont(font.getName(), font);
    }

    public void addFont(String name, PDFDictionary font) {
        if (fontsObj != null) {
            fontsObj.put(name, font);
            fontsObjDict.put(name, font);
        } else {
            fonts.put(name, font);
        }
    }

    public void createFontsAsObj() {
        fontsObj = new PDFDictionary();
        getDocument().registerTrailerObject(fontsObj);
        put("Font", fontsObj);
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
            this.iccColorSpaces.put(desc, icc);
        }
    }

    /**
     * Returns a ICCBased color space by profile name.
     * @param desc the name of the color space
     * @return the requested color space or null if it wasn't found
     */
    public PDFICCBasedColorSpace getICCColorSpaceByProfileName(String desc) {
        PDFICCBasedColorSpace cs = this.iccColorSpaces.get(desc);
        return cs;
    }

    /**
     * Returns a color space by name.
     * @param name the name of the color space
     * @return the requested color space or null if it wasn't found
     */
    public PDFColorSpace getColorSpace(PDFName name) {
        PDFColorSpace cs = this.colorSpaces.get(name);
        return cs;
    }

    /**
     * Add a named property.
     *
     * @param name name of property
     * @param property reference to property value
     */
    public void addProperty(String name, PDFReference property) {
        this.properties.put(name, property);
    }

    /**
     * Get a named property.
     *
     * @param name name of property
     */
    public PDFReference getProperty(String name) {
        return this.properties.get(name);
    }

    @Override
    public int output(OutputStream stream) throws IOException {
        populateDictionary();
        return super.output(stream);
    }

    private void populateDictionary() {
        if (parent != null && parent.fontsObj != null) {
            put("Font", parent.fontsObj);
        }
        if (!this.fonts.isEmpty() || (parent != null && !parent.fonts.isEmpty())) {
            PDFDictionary dict = new PDFDictionary(this);
            /* construct PDF dictionary of font object references */
            for (Map.Entry<String, PDFDictionary> entry : fonts.entrySet()) {
                dict.put(entry.getKey(), entry.getValue());
            }
            if (parent != null) {
                for (Map.Entry<String, PDFDictionary> entry : parent.fonts.entrySet()) {
                    dict.put(entry.getKey(), entry.getValue());
                }
                for (Map.Entry<String, PDFDictionary> entry : parent.fontsObjDict.entrySet()) {
                    dict.put(entry.getKey(), entry.getValue());
                }
            }
            put("Font", dict);
        }

        Set<PDFPattern> patterns = new LinkedHashSet<PDFPattern>();
        Set<PDFShading> shadings = new LinkedHashSet<PDFShading>();
        Set<PDFGState> gstates = new LinkedHashSet<PDFGState>();
        for (PDFResourceContext c : contexts) {
            xObjects.addAll(c.getXObjects());
            patterns.addAll(c.getPatterns());
            shadings.addAll(c.getShadings());
            gstates.addAll(c.getGStates());
        }
        if (parent != null) {
            xObjects.addAll(parent.xObjects);
        }

        if (!shadings.isEmpty()) {
            PDFDictionary dict = new PDFDictionary(this);
            for (PDFShading shading : shadings) {
                dict.put(shading.getName(), shading);
            }
            put("Shading", dict);
        }

        if (!patterns.isEmpty()) {
            PDFDictionary dict = new PDFDictionary(this);
            for (PDFPattern pattern : patterns) {
                dict.put(pattern.getName(), pattern);
            }
            put("Pattern", dict);
        }

        PDFArray procset = new PDFArray(this);
        procset.add(new PDFName("PDF"));
        procset.add(new PDFName("ImageB"));
        procset.add(new PDFName("ImageC"));
        procset.add(new PDFName("Text"));
        put("ProcSet", procset);

        if (!xObjects.isEmpty()) {
            PDFDictionary dict = (PDFDictionary) get("XObject");
            if (dict == null) {
                dict = new PDFDictionary(this);
            }
            for (PDFXObject xObject : xObjects) {
                dict.put(xObject.getName().toString(), xObject);
            }
            put("XObject", dict);
        }

        if (!gstates.isEmpty()) {
            PDFDictionary dict = new PDFDictionary(this);
            for (PDFGState gstate : gstates) {
                dict.put(gstate.getName(), gstate);
            }
            put("ExtGState", dict);
        }

        if (!this.colorSpaces.isEmpty()) {
            PDFDictionary dict = new PDFDictionary(this);
            for (PDFColorSpace colorSpace : colorSpaces.values()) {
                dict.put(colorSpace.getName(), colorSpace);
            }
            put("ColorSpace", dict);
        }

        if (!properties.isEmpty()) {
            PDFDictionary dict = new PDFDictionary(this);
            for (String name : properties.keySet()) {
                dict.put(name, properties.get(name));
            }
            put("Properties", dict);
        }
    }

}
