<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
                extension-element-prefixes="redirect">
<xsl:output method="text" />
<xsl:key name="adobe-char-map" match="/font-metrics/font-mappings/map" use="@adobe-name"/>

<xsl:template match="font-metrics">
<xsl:variable name="class-name" select="class-name"/>
<!--<redirect:write select="concat($class-name, '.java')">-->
<xsl:variable name="cExtends" select="count(extends-name)"/>
<xsl:variable name="cImplements" select="count(implements-name)"/>
<xsl:variable name="cEncoding" select="count(encoding)"/>
<xsl:variable name="cCharEncoding" select="count(char-encoding)"/>
<xsl:variable name="cCMap" select="count(cmap)"/>
<xsl:variable name="cCidType" select="count(cidtype)"/>
<xsl:variable name="cRegistry" select="count(registry)"/>
<xsl:variable name="cOrdering" select="count(ordering)"/>
<xsl:variable name="cSupplement" select="count(supplement)"/>
<xsl:variable name="cWinCharSet" select="count(win-charset)"/>
<xsl:variable name="cDw" select="count(dw)"/>
<xsl:variable name="cWidths" select="count(widths)"/>
<xsl:variable name="cFirstChar" select="count(first-char)"/>
<xsl:variable name="cLastChar" select="count(last-char)"/>
<xsl:variable name="cFlags" select="count(flags)"/>
<xsl:variable name="cFontBBox" select="count(font-bbox)"/>
<xsl:variable name="cStemV" select="count(stemv)"/>
<xsl:variable name="cItalicangle" select="count(italicangle)"/>
<xsl:variable name="cCapHeight" select="count(cap-height)"/>
<xsl:variable name="cAscender" select="count(ascender)"/>
<xsl:variable name="cDescender" select="count(descender)"/>
<xsl:variable name="cMissingWidth" select="count(missing-width)"/>
<xsl:variable name="cStemH" select="count(stemh)"/>
<xsl:variable name="cXHeight" select="count(x-height)"/>
<xsl:variable name="cLeading" select="count(leading)"/>
<xsl:variable name="cMaxWidth" select="count(max-width)"/>
<xsl:variable name="cMinWidth" select="count(min-width)"/>
<xsl:variable name="cAvgWidth" select="count(avg-width)"/>
<xsl:variable name="cPanose" select="count(panose)"/>
package org.apache.fop.render.pdf.fonts.cidfont.jp;

import org.apache.fop.render.pdf.CIDFont;
import org.apache.fop.render.pdf.CMap;
import org.apache.fop.render.pdf.Widths;
import org.apache.fop.layout.OptionalFontDescriptor;

public class <xsl:value-of select="class-name"/> extends <xsl:choose><xsl:when test="$cExtends != 0"><xsl:value-of select="extends-name"/></xsl:when><xsl:otherwise>CIDFont</xsl:otherwise></xsl:choose><xsl:if test="$cImplements != 0"> implements <xsl:value-of select="implements-name"/></xsl:if> {
    // Type0Font
    private final static String fontName = "<xsl:value-of select="font-name"/>";
<xsl:if test="$cEncoding != 0">    private final static String encoding = "<xsl:value-of select="encoding"/>";
</xsl:if>
<xsl:if test="$cCharEncoding != 0">    private final static String charEncoding = "<xsl:value-of select="char-encoding"/>";
</xsl:if>
<xsl:if test="$cCMap != 0">    private final static CMap cmap = new <xsl:value-of select="cmap"/>();
</xsl:if>
    // CIDFont
    private final static String cidBaseFont = "<xsl:value-of select="cid-basefont"/>";
<xsl:if test="$cCidType != 0">    private final static int cidtype = <xsl:value-of select="cidtype"/>;
</xsl:if>
<xsl:if test="$cRegistry != 0">    private final static String registry = "<xsl:value-of select="registry"/>";
</xsl:if>
<xsl:if test="$cOrdering != 0">    private final static String ordering = "<xsl:value-of select="ordering"/>";
</xsl:if>
<xsl:if test="$cSupplement != 0">    private final static int supplement = <xsl:value-of select="supplement"/>;
</xsl:if>
<xsl:if test="$cWinCharSet != 0">    private final static int winCharSet = <xsl:value-of select="win-charset"/>;
</xsl:if>
<xsl:if test="$cDw != 0">    private final static int dw = <xsl:value-of select="dw"/>;
</xsl:if>
<xsl:if test="$cWidths != 0">    private final static Widths widths = new Widths();
    static {<xsl:apply-templates select="widths"/>
    }
</xsl:if>
<xsl:if test="$cFirstChar != 0">    private final static int firstChar = <xsl:value-of select="first-char"/>;
</xsl:if>
<xsl:if test="$cLastChar != 0">    private final static int lastChar = <xsl:value-of select="last-char"/>;
</xsl:if>
    // FontDescriptor
<xsl:if test="$cFlags != 0">    private final static int flags = <xsl:value-of select="flags"/>;
</xsl:if>
<xsl:if test="$cFontBBox != 0"><xsl:apply-templates select="font-bbox"/>
</xsl:if>
<xsl:if test="$cStemV != 0">    private final static int stemv = <xsl:value-of select="stemv"/>;
</xsl:if>
<xsl:if test="$cItalicangle != 0">    private final static int italicangle = <xsl:value-of select="italicangle"/>;
</xsl:if>
<xsl:if test="$cCapHeight != 0">    private final static int capHeight = <xsl:value-of select="cap-height"/>;
</xsl:if>
<xsl:if test="$cAscender != 0">    private final static int ascender = <xsl:value-of select="ascender"/>;
</xsl:if>
<xsl:if test="$cDescender != 0">    private final static int descender = <xsl:value-of select="descender"/>;
</xsl:if>
    // OptionalDescriptor
<xsl:if test="$cMissingWidth != 0">    private final static int missingWidth = <xsl:value-of select="missing-width"/>;
</xsl:if>
<xsl:if test="$cStemH != 0">    private final static int stemh = <xsl:value-of select="stemh"/>;
</xsl:if>
<xsl:if test="$cXHeight != 0">    private final static int xHeight = <xsl:value-of select="x-height"/>;
</xsl:if>
<xsl:if test="$cLeading != 0">    private final static int leading = <xsl:value-of select="leading"/>;
</xsl:if>
<xsl:if test="$cMaxWidth != 0">    private final static int maxWidth = <xsl:value-of select="max-width"/>;
</xsl:if>
<xsl:if test="$cMinWidth != 0">    private final static int minWidth = <xsl:value-of select="min-width"/>;
</xsl:if>
<xsl:if test="$cAvgWidth != 0">    private final static int avgWidth = <xsl:value-of select="avg-width"/>;
</xsl:if>
<xsl:if test="$cPanose != 0">    private final static String panose = "<xsl:value-of select="panose"/>";
</xsl:if>
    public String fontName() {
        return fontName;
    }
<xsl:if test="$cEncoding != 0">
    public String encoding() {
        return encoding;
    }
</xsl:if>
<xsl:if test="$cCharEncoding != 0">
    public String getCharEncoding() {
        return charEncoding;
    }
</xsl:if>
<xsl:if test="$cCMap != 0">
    public CMap getCMap() {
        return cmap;
    }
</xsl:if>
    public String getCidBaseFont() {
        return cidBaseFont;
    }
<xsl:if test="$cCidType != 0">
    public int getCidType() {
        return cidtype;
    }
</xsl:if>
<xsl:if test="$cRegistry != 0">
    public String getRegistry() {
        return registry;
    }
</xsl:if>
<xsl:if test="$cOrdering != 0">
    public String getOrdering() {
        return ordering;
    }
</xsl:if>
<xsl:if test="$cSupplement != 0">
    public int getSupplement() {
        return supplement;
    }
</xsl:if>
<xsl:if test="$cWinCharSet != 0">
    public int getWinCharSet() {
        return winCharSet;
    }
</xsl:if>
<xsl:if test="$cDw != 0">
    public int getDefaultWidth() {
        return dw;
    }
</xsl:if>
<xsl:if test="$cWidths != 0">
    public int width(int i,int size) {
        int width = widths.getWidth(i);
        if ( width &lt; 0 )
            width = (getDefaultWidth() &lt; 0) ? 1000 : dw;
        return width*size;
    }
    public int[] getWidths(int size) { return null; }
    public Widths getWidths() {
        return widths;
    }
</xsl:if>
<xsl:if test="$cFirstChar != 0">
    public int getFirstChar() {
        return firstChar;
    }
</xsl:if>
<xsl:if test="$cLastChar != 0">
    public int getLastChar() {
        return lastChar;
    }
</xsl:if>


<xsl:if test="$cExtends = 0">
    public int getFlags() {
<xsl:choose><xsl:when test="$cFlags != 0">        return flags;
</xsl:when><xsl:otherwise>        return -1;
</xsl:otherwise></xsl:choose>    }

    public int[] getFontBBox() {
<xsl:choose><xsl:when test="$cFontBBox != 0">        return fontBBox;
</xsl:when><xsl:otherwise>        return null;
</xsl:otherwise></xsl:choose>    }

    public int getStemV() {
<xsl:choose><xsl:when test="$cStemV != 0">        return stemv;
</xsl:when><xsl:otherwise>        return -1;
</xsl:otherwise></xsl:choose>    }

    public int getItalicAngle() {
<xsl:choose><xsl:when test="$cItalicangle != 0">        return italicangle;
</xsl:when><xsl:otherwise>        return -1;
</xsl:otherwise></xsl:choose>    }
</xsl:if>

<xsl:if test="$cCapHeight != 0">
    public int getCapHeight(int size) {
        return size*capHeight;
    }
    public int getCapHeight() {
        return capHeight;
    }
</xsl:if>
<xsl:if test="$cAscender != 0">
    public int getAscender(int size) {
        return size*ascender;
    }
    public int getAscender() {
        return ascender;
    }
</xsl:if>
<xsl:if test="$cDescender != 0">
    public int getDescender(int size) {
        return size*descender;
    }
    public int getDescender() {
        return descender;
    }
</xsl:if>

<xsl:if test="$cExtends = 0">
    public int getMissingWidth() {
<xsl:choose><xsl:when test="$cMissingWidth != 0">        return missingWidth;
</xsl:when><xsl:otherwise>        return -1;
</xsl:otherwise></xsl:choose>    }

    public int getStemH() {
<xsl:choose><xsl:when test="$cStemH != 0">        return stemh;
</xsl:when><xsl:otherwise>        return -1;
</xsl:otherwise></xsl:choose>    }
</xsl:if>

<xsl:if test="$cXHeight != 0">
    public int getXHeight(int size) {
        return size*xHeight;
    }
    public int getXHeight() {
        return xHeight;
    }
</xsl:if>

<xsl:if test="$cExtends = 0">
    public int getLeading() {
<xsl:choose><xsl:when test="$cLeading != 0">        return leading;
</xsl:when><xsl:otherwise>        return -1;
</xsl:otherwise></xsl:choose>    }

    public int getMaxWidth() {
<xsl:choose><xsl:when test="$cMaxWidth != 0">        return maxWidth;
</xsl:when><xsl:otherwise>        return -1;
</xsl:otherwise></xsl:choose>    }

    public int getMinWidth() {
<xsl:choose><xsl:when test="$cMinWidth != 0">        return minWidth;
</xsl:when><xsl:otherwise>        return -1;
</xsl:otherwise></xsl:choose>    }

    public int getAvgWidth() {
<xsl:choose><xsl:when test="$cAvgWidth != 0">        return avgWidth;
</xsl:when><xsl:otherwise>        return -1;
</xsl:otherwise></xsl:choose>    }

    public String getPanose() {
<xsl:choose><xsl:when test="$cPanose != 0">        return panose;
</xsl:when><xsl:otherwise>        return null;
</xsl:otherwise></xsl:choose>    }
</xsl:if>

   public boolean hasKerningInfo() { return false; }
   public java.util.Hashtable getKerningInfo() { return null; };
   public boolean isEmbeddable() { return false; }
   public byte getSubType() {return org.apache.fop.pdf.PDFFont.TYPE0;}
   public org.apache.fop.pdf.PDFStream getFontFile(int objNum) { return null; }

}
</xsl:template>

<xsl:template match="widths">
<xsl:variable name="cCidWidths" select="count(cid-widths)"/>
<xsl:variable name="cCidRange" select="count(cid-range)"/>
<xsl:variable name="cCidSingle" select="count(cid-single)"/>
<xsl:if test="$cCidWidths !=0"><xsl:apply-templates select="cid-widths"/></xsl:if>
<xsl:if test="$cCidRange !=0"><xsl:apply-templates select="cid-range"/></xsl:if>
<xsl:if test="$cCidSingle != 0"><xsl:apply-templates select="cid-single"/></xsl:if>
</xsl:template>

<xsl:template match="cid-widths">
<xsl:variable name="cRange" select="count(range)"/>
<xsl:variable name="cSingle" select="count(single)"/>
<xsl:if test="$cRange !=0"><xsl:apply-templates select="range"/></xsl:if>
<xsl:if test="$cSingle != 0"><xsl:apply-templates select="single"/></xsl:if>
</xsl:template>

<xsl:template match="cid-range">
        widths.addElement(<xsl:value-of select="start"/>,<xsl:value-of select="end"/>,<xsl:value-of select="width"/>);</xsl:template>

<xsl:template match="cid-single">
        widths.addElement(<xsl:value-of select="start"/>, new int[] {
<xsl:apply-templates select="width"/>
        });</xsl:template>

<xsl:template match="width">
<xsl:text>            </xsl:text><xsl:value-of select="text()"/>,
</xsl:template>

<xsl:template match="range">
        widths.addWidths(<xsl:value-of select="start"/>,<xsl:value-of select="end"/>,<xsl:value-of select="width"/>);</xsl:template>

<xsl:template match="single">
        widths.addWidths(<xsl:value-of select="start"/>, new int[] {
<xsl:apply-templates select="width"/>
        });</xsl:template>

<xsl:template match="font-bbox">    private final static int[] fontBBox = {
        <xsl:value-of select="llx"/>,
        <xsl:value-of select="lly"/>,
        <xsl:value-of select="urx"/>,
        <xsl:value-of select="ury"/>
    };
</xsl:template>

</xsl:stylesheet>
