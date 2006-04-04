/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

package org.apache.fop.render.ps;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.Typeface;
import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSResource;

/**
 * Utility code for font handling in PostScript.
 */
public class PSFontUtils extends org.apache.xmlgraphics.ps.PSFontUtils {

    /**
     * Generates the PostScript code for the font dictionary.
     * @param gen PostScript generator to use for output
     * @param fontInfo available fonts
     * @return a Map of PSResource instances representing all defined fonts (key: font key)
     * @throws IOException in case of an I/O problem
     */
    public static Map writeFontDict(PSGenerator gen, FontInfo fontInfo) 
                throws IOException {
        gen.commentln("%FOPBeginFontDict");
        gen.writeln("/FOPFonts 100 dict dup begin");

        // write("/gfF1{/Helvetica findfont} bd");
        // write("/gfF3{/Helvetica-Bold findfont} bd");
        Map fonts = fontInfo.getFonts();
        Map fontResources = new java.util.HashMap();
        Iterator iter = fonts.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            Typeface tf = (Typeface)fonts.get(key);
            if (tf instanceof LazyFont) {
                tf = ((LazyFont)tf).getRealFont();
            }
            if (tf == null) {
                //This is to avoid an NPE if a malconfigured font is in the configuration but not
                //used in the document. If it were used, we wouldn't get this far.
                String fallbackKey = fontInfo.getInternalFontKey(Font.DEFAULT_FONT); 
                tf = (Typeface)fonts.get(fallbackKey);
            }
            PSResource fontRes = new PSResource("font", tf.getFontName());
            fontResources.put(key, fontRes);
            boolean embeddedFont = false;
            if (FontType.TYPE1 == tf.getFontType()) {
                if (tf instanceof CustomFont) {
                    CustomFont cf = (CustomFont)tf;
                    InputStream in = getInputStreamOnFont(gen, cf);
                    if (in != null) {
                        gen.writeDSCComment(DSCConstants.BEGIN_RESOURCE, 
                                fontRes);
                        embedType1Font(gen, in);
                        gen.writeDSCComment(DSCConstants.END_RESOURCE);
                        gen.notifyResourceUsage(fontRes, false);
                        embeddedFont = true;
                    }
                }
            }
            if (!embeddedFont) {
                gen.writeDSCComment(DSCConstants.INCLUDE_RESOURCE, fontRes);
                //Resource usage shall be handled by renderer
                //gen.notifyResourceUsage(fontRes, true);
            }
            gen.commentln("%FOPBeginFontKey: " + key);
            gen.writeln("/" + key + " /" + tf.getFontName() + " def");
            gen.commentln("%FOPEndFontKey");
        }
        gen.writeln("end def");
        gen.commentln("%FOPEndFontDict");
        gen.commentln("%FOPBeginFontReencode");
        defineWinAnsiEncoding(gen);
        
        //Rewrite font encodings
        iter = fonts.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String)iter.next();
            Typeface fm = (Typeface)fonts.get(key);
            if (fm instanceof LazyFont && ((LazyFont)fm).getRealFont() == null) {
                continue;
            } else if (null == fm.getEncoding()) {
                //ignore (ZapfDingbats and Symbol run through here
                //TODO: ZapfDingbats and Symbol should get getEncoding() fixed!
            } else if ("WinAnsiEncoding".equals(fm.getEncoding())) {
                gen.writeln("/" + fm.getFontName() + " findfont");
                gen.writeln("dup length dict begin");
                gen.writeln("  {1 index /FID ne {def} {pop pop} ifelse} forall");
                gen.writeln("  /Encoding " + fm.getEncoding() + " def");
                gen.writeln("  currentdict");
                gen.writeln("end");
                gen.writeln("/" + fm.getFontName() + " exch definefont pop");
            } else {
                gen.commentln("%WARNING: Only WinAnsiEncoding is supported. Font '" 
                    + fm.getFontName() + "' asks for: " + fm.getEncoding());
            }
        }
        gen.commentln("%FOPEndFontReencode");
        return fontResources;
    }

    private static InputStream getInputStreamOnFont(PSGenerator gen, CustomFont font) 
                throws IOException {
        if (font.isEmbeddable()) {
            Source source = font.getEmbedFileSource();
            if (source == null && font.getEmbedResourceName() != null) {
                source = new StreamSource(PSFontUtils.class
                        .getResourceAsStream(font.getEmbedResourceName()));
            }
            if (source == null) {
                return null;
            }
            InputStream in = null;
            if (source instanceof StreamSource) {
                in = ((StreamSource) source).getInputStream();
            }
            if (in == null && source.getSystemId() != null) {
                try {
                    in = new java.net.URL(source.getSystemId()).openStream();
                } catch (MalformedURLException e) {
                    new FileNotFoundException(
                            "File not found. URL could not be resolved: "
                                    + e.getMessage());
                }
            }
            if (in == null) {
                return null;
            }
            //Make sure the InputStream is decorated with a BufferedInputStream
            if (!(in instanceof java.io.BufferedInputStream)) {
                in = new java.io.BufferedInputStream(in);
            }
            return in;
        } else {
            return null;
        }
    }

}
