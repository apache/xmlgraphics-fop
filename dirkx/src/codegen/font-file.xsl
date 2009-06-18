<transform xmlns="http://www.w3.org/1999/XSL/Transform"
 xmlns:xt="http://www.jclark.com/xt" extension-element-prefixes="xt"
 version="1.0">
<template match="font-metrics">
<variable name="class-name" select="class-name"/>
<xt:document method="text" href="org/apache/xml/fop/render/pdf/fonts/{$class-name}.java">
package org.apache.xml.fop.render.pdf.fonts;

import org.apache.xml.fop.render.pdf.Font;

public class <value-of select="class-name"/> extends Font {
    private final static String fontName = "<value-of select="font-name"/>";
    private final static String encoding = "<value-of select="encoding"/>";
    private final static int capHeight = <value-of select="cap-height"/>;
    private final static int xHeight = <value-of select="x-height"/>;
    private final static int ascender = <value-of select="ascender"/>;
    private final static int descender = <value-of select="descender"/>;
    private final static int[] width;

    static {
        width = new int[256];
<for-each select="widths/char"><variable name="char-name" select="@name"/><variable name="char-num" select="document('charlist.xml')/font-mappings/map[@adobe-name=$char-name]/@win-ansi"/><if test="$char-num!='-1'">        width[<value-of select="$char-num"/>] = <value-of select="@width"/>;
</if></for-each>
    }

    public String encoding() {
        return encoding;
    }
    
    public String fontName() {
        return fontName;
    }

    public int getAscender() {
	return ascender;
    }

    public int getCapHeight() {
	return capHeight;
    }

    public int getDescender() {
	return descender;
    }

    public int getXHeight() {
	return xHeight;
    }

    public int width(int i) {
        return width[i];
    }
}
</xt:document>
</template>
</transform>
