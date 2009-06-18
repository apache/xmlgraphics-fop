package org.apache.xml.fop.layout;

import org.apache.xml.fop.apps.FOPException;

public class FontState {
	
    protected FontInfo fontInfo;
    private String fontName;
    private int fontSize;
    private String fontFamily;
    private String fontStyle;
    private String fontWeight;
    private FontMetric metric;
    
    public FontState(FontInfo fontInfo, String fontFamily, String fontStyle, String fontWeight, int fontSize) throws FOPException {
	this.fontInfo = fontInfo;
	this.fontFamily = fontFamily;
	this.fontStyle = fontStyle;
	this.fontWeight = fontWeight;
	this.fontSize = fontSize;
	this.fontName = fontInfo.fontLookup(fontFamily,fontStyle,fontWeight);
	this.metric = fontInfo.getMetricsFor(fontName);
    }

    public int getAscender() {
	return fontSize * metric.getAscender() / 1000;
    }

    public int getCapHeight() {
	return fontSize * metric.getCapHeight() / 1000;
    }

    public int getDescender() {
	return fontSize * metric.getDescender() / 1000;
    }

    public String getFontName() {
	return this.fontName;
    }

    public int getFontSize() {
	return this.fontSize;
    }

    public String getFontWeight() {
	return this.fontWeight;
    }

    public FontInfo getFontInfo() {
	return this.fontInfo;
    }

    public int getXHeight() {
	return fontSize * metric.getXHeight() / 1000;
    }

    public int width(int charnum) {
	// returns width of given character number in millipoints
	return (fontSize * metric.width(charnum) / 1000);
    }
}
