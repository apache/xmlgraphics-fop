/*
 * $Id$
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
package org.apache.fop.render.pdf.fonts;

import org.apache.fop.render.pdf.Font;
import org.apache.fop.layout.FontDescriptor;
import org.apache.fop.pdf.PDFStream;
import org.apache.fop.messaging.MessageHandler;

import java.util.Map;
import java.net.URL;

import org.apache.fop.render.pdf.FontReader;

public class LazyFont extends Font implements FontDescriptor {

    private URL metricsFile = null;
    private URL fontEmbedPath = null;
    private boolean useKerning = false;

    private boolean isMetricsLoaded = false;
    private Font realFont = null;
    private FontDescriptor realFontDescriptor = null;

    public LazyFont(URL fontEmbedPath, URL metricsFile, boolean useKerning){
        this.metricsFile = metricsFile;
        this.fontEmbedPath = fontEmbedPath;
        this.useKerning = useKerning;
    }

    private void load(){
        if (!isMetricsLoaded) {
            isMetricsLoaded = true;
            try{
                FontReader reader = new FontReader(metricsFile);
                reader.useKerning(useKerning);
                reader.setFontEmbedPath(fontEmbedPath);
                realFont = reader.getFont();
                if(realFont instanceof FontDescriptor){
                    realFontDescriptor = (FontDescriptor) realFont;
                }
                // System.out.println("Metrics " + metricsFileName + " loaded.");
            } catch (Exception ex) {
                MessageHandler.error("Failed to read font metrics file "
                                     + metricsFile.toExternalForm()
                                     + ": " + ex.getMessage());
            }
        }
    }

    public Font getRealFont(){
        return realFont;
    }

    // Font
    public String encoding(){
        load();
        return realFont.encoding();
    }

    public String fontName(){
        load();
        return realFont.fontName();
    }

    public byte getSubType(){
        load();
        return realFont.getSubType();
    }

    public char mapChar(char c){
        load();
        return realFont.mapChar(c);
    }

    // FontMetrics
    public int getAscender(int size){
        load();
        return realFont.getAscender(size);
    }

    public int getCapHeight(int size){
        load();
        return realFont.getCapHeight(size);
    }

    public int getDescender(int size){
        load();
        return realFont.getDescender(size);
    }

    public int getXHeight(int size){
        load();
        return realFont.getXHeight(size);
    }

    public int getFirstChar(){
        load();
        return realFont.getFirstChar();
    }

    public int getLastChar(){
        load();
        return realFont.getLastChar();
    }

    public int width(int i, int size){
        load();
        return realFont.width(i, size);
    }

    public int[] getWidths(int size){
        load();
        return realFont.getWidths(size);
    }

    // FontDescriptor
    public int getCapHeight(){
        load();
        return realFontDescriptor.getCapHeight();
    }

    public int getDescender(){
        load();
        return realFontDescriptor.getDescender();
    }

    public int getAscender(){
        load();
        return realFontDescriptor.getAscender();
    }

    public int getFlags(){
        load();
        return realFontDescriptor.getFlags();
    }

    public int[] getFontBBox(){
        load();
        return realFontDescriptor.getFontBBox();
    }

    public int getItalicAngle(){
        load();
        return realFontDescriptor.getItalicAngle();
    }

    public int getStemV(){
        load();
        return realFontDescriptor.getStemV();
    }

    public boolean hasKerningInfo(){
        load();
        return realFontDescriptor.hasKerningInfo();
    }

    public Map getKerningInfo(){
        load();
        return realFontDescriptor.getKerningInfo();
    }

    public boolean isEmbeddable(){
        load();
        return realFontDescriptor.isEmbeddable();
    }

    public PDFStream getFontFile(int objNum){
        load();
        return realFontDescriptor.getFontFile(objNum);
    }
}

