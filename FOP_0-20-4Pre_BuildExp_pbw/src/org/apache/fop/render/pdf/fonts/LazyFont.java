/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.pdf.fonts;

import org.apache.fop.render.pdf.Font;
import org.apache.fop.layout.FontDescriptor;
import org.apache.fop.pdf.PDFStream;
import java.util.HashMap;

import org.apache.fop.render.pdf.FontReader;

public class LazyFont extends Font implements FontDescriptor {
    
    private String metricsFileName = null;
    private String fontEmbedPath = null;
    private boolean useKerning = false;
    
    private boolean isMetricsLoaded = false;
    private Font realFont = null;
    private FontDescriptor realFontDescriptor = null;
    
    public LazyFont(String fontEmbedPath, String metricsFileName, boolean useKerning){
        this.metricsFileName = metricsFileName;
        this.fontEmbedPath = fontEmbedPath;
        this.useKerning = useKerning;
    }
    
    private void load(){
        if(! isMetricsLoaded){
            isMetricsLoaded = true;
            try{

                // TODO - Possible thread problem here

                FontReader reader = new FontReader(metricsFileName);
                reader.useKerning(useKerning);
                reader.setFontEmbedPath(fontEmbedPath);
                realFont = reader.getFont();
                if(realFont instanceof FontDescriptor){
                    realFontDescriptor = (FontDescriptor) realFont;
                }
                // System.out.println("Metrics " + metricsFileName + " loaded.");
            } catch (Exception ex) {
                //log.error("Failed to read font metrics file "
                //                     + metricsFileName
                //                     + " : " + ex.getMessage());
            }
        }
    }
    
    public Font getRealFont(){
        return realFont;
    }
    
    public boolean isMultiByte() {
        return realFont.isMultiByte();
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
    
    public HashMap getKerningInfo(){
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

