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

package org.apache.fop.render.ps;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSResource;

import org.axsl.fontR.Font;
import org.axsl.fontR.FontUse;
import org.axsl.psR.Encoding;

/**
 * Utility code for font handling in PostScript.
 */
public class PSFontUtils extends org.apache.xmlgraphics.ps.PSFontUtils {

    /**
     * Generates the PostScript code for the font dictionary.
     * @param gen PostScript generator to use for output
     * @param fontMap mappings of FontUses to their associated internal names
     * @return a Map of PSResource instances representing all defined fonts (key: font key)
     * @throws IOException in case of an I/O problem
     */
    public static Map writeFontDict(PSGenerator gen, FontMap fontMap) 
                throws IOException {
        gen.commentln("%FOPBeginFontDict");
        gen.writeln("/FOPFonts 100 dict dup begin");

        // write("/gfF1{/Helvetica findfont} bd");
        // write("/gfF3{/Helvetica-Bold findfont} bd");
        Map fontResources = new java.util.HashMap();
        Iterator iter = fontMap.getMappings().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String internalName = (String)entry.getValue();
            FontUse fontUse = (FontUse)entry.getKey();
            PSResource fontRes = new PSResource(PSResource.TYPE_FONT, internalName);
            fontResources.put(internalName, fontRes);
            boolean embeddedFont = false;
            Font font = fontUse.getFont();
            if (font.getFontFormat() == Font.FORMAT_TYPE1
                    && font.isEmbeddable()
                    && !font.isPDFStandardFont()) {
                gen.writeDSCComment(DSCConstants.BEGIN_RESOURCE, fontRes);
                gen.writeByteArr(font.getContentsPostScriptHex(fontMap.getFontConsumer()));
                gen.writeDSCComment(DSCConstants.END_RESOURCE);
                gen.notifyResourceUsage(fontRes, false);
                embeddedFont = true;
            }
            if (!embeddedFont) {
                gen.writeDSCComment(DSCConstants.INCLUDE_RESOURCE, fontRes);
                //Resource usage shall be handled by renderer
                //gen.notifyResourceUsage(fontRes, true);
            }
            gen.commentln("%FOPBeginFontKey: " + internalName);
            gen.writeln("/" + internalName + " /" + font.postscriptName() + " def");
            gen.commentln("%FOPEndFontKey");
        }
        gen.writeln("end def");
        gen.commentln("%FOPEndFontDict");
        gen.commentln("%FOPBeginFontReencode");
        defineWinAnsiEncoding(gen);

        //Rewrite font encodings
        iter = fontMap.getMappings().iterator();
        while (iter.hasNext()) {
            FontUse fontUse = (FontUse)((Map.Entry)iter.next()).getKey();
            Encoding encoding = fontUse.getEncoding();
            if (encoding == null) {
                //ignore (ZapfDingbats and Symbol run through here
                //TODO: ZapfDingbats and Symbol should get getEncoding() fixed!
            } else if (encoding.isPredefinedPS()) {
                gen.writeln("/" + fontUse.postscriptName() + " findfont");
                gen.writeln("dup length dict begin");
                gen.writeln("  {1 index /FID ne {def} {pop pop} ifelse} forall");
                gen.writeln("  /Encoding " + encoding.getName() + " def");
                gen.writeln("  currentdict");
                gen.writeln("end");
                gen.writeln("/" + fontUse.postscriptName() + " exch definefont pop");
            } else {
                gen.commentln("%WARNING: Only WinAnsiEncoding is supported. Font '" 
                    + fontUse.postscriptName() + "' asks for: " + encoding.getName());
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
