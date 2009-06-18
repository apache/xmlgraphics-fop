package org.apache.xml.fop.render.pdf;

// FOP
import org.apache.xml.fop.layout.FontMetric;

/**
 * base class for PDF font classes
 */
public abstract class Font implements FontMetric {

    /**
     * get the encoding of the font
     */
    public abstract String encoding();

    /**
     * get the base font name
     */
    public abstract String fontName();
}
