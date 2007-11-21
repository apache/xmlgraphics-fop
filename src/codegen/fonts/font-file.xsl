<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!-- $Id$ -->
<!-- This file writes the class files for the fonts (Courier.java,
     Helvetica.java etc.).  It uses the information in the font
     description files (Courier.xml, Helvetica.xml) to do this.  In these
     font description files each character is referenced by its adobe
     glyph name:
        <char name="A" width="667"/>
     To resolve this name and to find the code for this character it looks
     up the adobe name in the file encodings.xml and extracts the appropriate
     code. -->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="text"/>

  <xsl:param name="encoding" select="/font-metrics/encoding"/>
  <xsl:variable name="glyphs" select="document('encodings.xml')/encoding-set/encoding[@id=$encoding]/glyph"/>

  <xsl:template match="font-metrics">
package org.apache.fop.fonts.base14;

<xsl:if test="count(kerning) &gt; 0">
import java.util.Map;
</xsl:if>
import java.util.Set;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.CodePointMapping;

public class <xsl:value-of select="class-name"/> extends Typeface {
    private final static String fontName = "<xsl:value-of select="font-name"/>";
    private final static String fullName = "<xsl:value-of select="full-name"/>";
    private final static Set familyNames;
    private final static String encoding = "<xsl:value-of select="$encoding"/>";
    private final static int capHeight = <xsl:value-of select="cap-height"/>;
    private final static int xHeight = <xsl:value-of select="x-height"/>;
    private final static int ascender = <xsl:value-of select="ascender"/>;
    private final static int descender = <xsl:value-of select="descender"/>;
    private final static int firstChar = <xsl:value-of select="first-char"/>;
    private final static int lastChar = <xsl:value-of select="last-char"/>;
    private final static int[] width;
    private final CodePointMapping mapping =
        CodePointMapping.getMapping("<xsl:value-of select="$encoding"/>");
<xsl:if test="count(kerning) &gt; 0">
    private final static Map kerning;
</xsl:if>

    private boolean enableKerning = false;

    static {
        width = new int[256];
        <xsl:apply-templates select="widths"/>
<xsl:if test="count(kerning) &gt; 0">
        kerning = new java.util.HashMap();
        Integer first, second;
        Map pairs;
        <xsl:apply-templates select="kerning"/>
</xsl:if>
        familyNames = new java.util.HashSet();
        familyNames.add("<xsl:value-of select="family-name"/>");
    }

    public <xsl:value-of select="class-name"/>() {
        this(false);
    }

    public <xsl:value-of select="class-name"/>(boolean enableKerning) {
        this.enableKerning = enableKerning;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getFontName() {
        return fontName;
    }

    public String getEmbedFontName() {
        return getFontName();
    }

    public String getFullName() {
        return fullName;
    }

    public Set getFamilyNames() {
        return familyNames;
    }

    public FontType getFontType() {
        return FontType.TYPE1;
    }

    public int getAscender(int size) {
        return size * ascender;
    }

    public int getCapHeight(int size) {
        return size * capHeight;
    }

    public int getDescender(int size) {
        return size * descender;
    }

    public int getXHeight(int size) {
        return size * xHeight;
    }

    public int getFirstChar() {
        return firstChar;
    }

    public int getLastChar() {
        return lastChar;
    }

    public int getWidth(int i,int size) {
        return size * width[i];
    }

    public int[] getWidths() {
        int[] arr = new int[getLastChar() - getFirstChar() + 1];
        System.arraycopy(width, getFirstChar(), arr, 0, getLastChar() - getFirstChar() + 1);
        return arr;
    }

<xsl:choose>
  <xsl:when test="count(kerning) &gt; 0">
    public boolean hasKerningInfo() {
        return enableKerning;
    }

    public java.util.Map getKerningInfo() {
        return kerning;
    }
  </xsl:when>
  <xsl:otherwise>
    public boolean hasKerningInfo() {
        return false;
    }

    public java.util.Map getKerningInfo() {
        return java.util.Collections.EMPTY_MAP;
    }
  </xsl:otherwise>
</xsl:choose>

    public char mapChar(char c) {
        notifyMapOperation();
        char d = mapping.mapChar(c);
        if (d != 0) {
            return d;
        } else {
            return '#';
        }
    }

    public boolean hasChar(char c) {
        return (mapping.mapChar(c) > 0);
    }

}
  </xsl:template>

  <xsl:template match="widths/char"><xsl:variable name="char-name" select="@name"/><xsl:variable name="char-num" select="$glyphs[@name = $char-name]/@codepoint"/><xsl:if test="$char-num!=''">        width[0x<xsl:value-of select="$char-num"/>] = <xsl:value-of select="@width"/>;</xsl:if></xsl:template>
  
  <xsl:template match="kerning">
        first = new Integer(<xsl:value-of select="@kpx1"/>);
        pairs = (Map)kerning.get(first);
        if (pairs == null) {
            pairs = new java.util.HashMap();
            kerning.put(first, pairs);
        }
        <xsl:apply-templates select="pair"/>
  </xsl:template>
  
  <xsl:template match="pair">
        second = new Integer(<xsl:value-of select="@kpx2"/>);
        pairs.put(second, new Integer(<xsl:value-of select="@kern"/>));
  </xsl:template>
</xsl:stylesheet>

