package org.apache.xml.fop.layout;

/**
 * interface for font metric classes
 */
public interface FontMetric {
    int getAscender();
    int getCapHeight();
    int getDescender();
    int getXHeight();

    /**
     * return width (in 1/1000ths of point size) of character at
     * code point i
     */
    public int width(int i);
}
