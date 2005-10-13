/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.CopyUtils;
import org.apache.commons.io.EndianUtils;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.Glyphs;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.util.ASCIIHexOutputStream;
import org.apache.fop.util.SubInputStream;

/**
 * Utility code for font handling in PostScript.
 */
public class PSFontUtils {

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
            if (null == fm.getEncoding()) {
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
                System.out.println("Only WinAnsiEncoding is supported. Font '" 
                    + fm.getFontName() + "' asks for: " + fm.getEncoding());
            }
        }
        gen.commentln("%FOPEndFontReencode");
        return fontResources;
    }

    /**
     * This method reads a Type 1 font from a stream and embeds it into a PostScript stream.
     * Note: Only the IBM PC Format as described in section 3.3 of the Adobe Technical Note #5040
     * is supported.
     * @param gen The PostScript generator
     * @param in the InputStream from which to read the Type 1 font
     * @throws IOException in case an I/O problem occurs
     */
    private static void embedType1Font(PSGenerator gen, InputStream in) throws IOException {
        boolean finished = false;
        while (!finished) {
            int segIndicator = in.read();
            if (segIndicator < 0) {
                throw new IOException("Unexpected end-of-file while reading segment indicator");
            } else if (segIndicator != 128) {
                throw new IOException("Expected ASCII 128, found: " + segIndicator);
            }
            int segType = in.read();
            if (segType < 0) {
                throw new IOException("Unexpected end-of-file while reading segment type");
            }
            int dataSegLen = 0;
            switch (segType) {
                case 1: //ASCII
                    dataSegLen = EndianUtils.readSwappedInteger(in);

                    BufferedReader reader = new BufferedReader(
                            new java.io.InputStreamReader(
                                    new SubInputStream(in, dataSegLen), "US-ASCII"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        gen.writeln(line);
                        }
                    break;
                case 2: //binary
                    dataSegLen = EndianUtils.readSwappedInteger(in);

                    SubInputStream sin = new SubInputStream(in, dataSegLen);
                    ASCIIHexOutputStream hexOut = new ASCIIHexOutputStream(gen.getOutputStream());
                    CopyUtils.copy(sin, hexOut);
                    gen.newLine();
                    break;
                case 3: //EOF
                    finished = true;
                    break;
                default: throw new IOException("Unsupported segment type: " + segType);
            }
        }
    }

    private static InputStream getInputStreamOnFont(PSGenerator gen, CustomFont font) 
                throws IOException {
        if (font.isEmbeddable()) {
            Source source = null;
            if (font.getEmbedFileName() != null) {
                source = gen.resolveURI(font.getEmbedFileName());
            }
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

    private static void defineWinAnsiEncoding(PSGenerator gen) throws IOException {
        gen.writeln("/WinAnsiEncoding [");
        for (int i = 0; i < Glyphs.WINANSI_ENCODING.length; i++) {
            if (i > 0) {
                if ((i % 5) == 0) {
                    gen.newLine();
                } else {
                    gen.write(" ");
                }
            }
            final char ch = Glyphs.WINANSI_ENCODING[i];
            final String glyphname = Glyphs.charToGlyphName(ch);
            if ("".equals(glyphname)) {
                gen.write("/" + Glyphs.NOTDEF);
            } else {
                gen.write("/");
                gen.write(glyphname);
            }
        }
        gen.newLine();
        gen.writeln("] def");
    }

    
}
