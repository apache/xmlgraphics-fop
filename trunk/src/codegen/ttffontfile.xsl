<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!-- $Id$ -->
<!--
This files writes the class files for the Adobe Type 1 fonts.
It uses the information in the font description files (Courier.xml, Helvetica.xml) to this
In these font description files each character is referenced by its adobe name:
      <char name="A" width="667"/>
To resolve this name and to find the code for this character it looks up the adobe name in the
file charlist.xml and extracts the WinAnsi code.
-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
                extension-element-prefixes="redirect">
<xsl:output method="text" />

<xsl:template match="font-metrics">
<xsl:variable name="class-name" select="class-name"/>
<!--<redirect:write select="concat('org/apache/fop/render/pdf/fonts/', $class-name, '.java')">-->
package org.apache.fop.render.pdf.fonts;

import org.apache.fop.render.pdf.Font;
import org.apache.fop.layout.FontDescriptor;
import org.apache.fop.fonts.Glyphs;
import org.apache.fop.pdf.PDFStream;
import org.apache.fop.pdf.PDFTTFStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.HashMap;

public class <xsl:value-of select="class-name"/> extends Font implements FontDescriptor {
    private final static String fontName = "<xsl:value-of select="font-name"/>";
    private final static String encoding = "<xsl:value-of select="encoding"/>";
    private final static int capHeight = <xsl:value-of select="cap-height"/>;
    private final static int xHeight = <xsl:value-of select="x-height"/>;
    private final static int ascender = <xsl:value-of select="ascender"/>;
    private final static int descender = <xsl:value-of select="descender"/>;
    private final static int[] fontBBox = {
        <xsl:value-of select="bbox/left"/>,
        <xsl:value-of select="bbox/bottom"/>,
        <xsl:value-of select="bbox/right"/>,
        <xsl:value-of select="bbox/top"/>
    };

    private final static String embedFileName = <xsl:value-of select="embedFile"/>;
    private final static String embedResourceName = <xsl:value-of select="embedResource"/>;
    private static PDFTTFStream embeddedFont=null;
    private final static int flags = <xsl:value-of select="flags"/>;
    private final static int stemV = <xsl:value-of select="stemv"/>;
    private final static int italicAngle = <xsl:value-of select="italicangle"/>;
    private final static int firstChar = <xsl:value-of select="first-char"/>;
    private final static int lastChar = <xsl:value-of select="last-char"/>;
    private final static int[] width;
    private final static HashMap kerning=new HashMap();
    static {
        width = new int[256];
<!--<xsl:for-each select="widths/char"><xsl:variable name="char-name" select="@name"/><xsl:variable name="char-num" select="document('charlist.xml')/font-mappings/map[@adobe-name=$char-name]/@win-ansi"/><xsl:if test="$char-num!='-1'">        width[<xsl:value-of select="$char-num"/>] = <xsl:value-of select="@width"/>;</xsl:if>-->
<xsl:for-each select="widths/char"><xsl:variable name="char-name" select="@name"/>   width[<xsl:value-of select="$char-name"/>] = <xsl:value-of select="@width"/>;
</xsl:for-each>

    HashMap tmptable;
<xsl:for-each select="kerning">
<xsl:variable name="kpx1-name" select="@kpx1"/>
    tmptable=new HashMap();<xsl:for-each select="pair"><xsl:variable name="kpx2-name" select="@kpx2"/><xsl:variable name="kern-name" select="@kern"/>
    tmptable.put(Glyphs.glyphToString("<xsl:value-of select="$kpx2-name"/>"), new Integer(<xsl:value-of select="$kern-name"/>));</xsl:for-each>
    kerning.put(Glyphs.glyphToString("<xsl:value-of select="$kpx1-name"/>"), tmptable);
</xsl:for-each>
    }

    public final boolean hasKerningInfo() {return kerning.isEmpty();}
    public final java.util.HashMap getKerningInfo() {return kerning;}

    public byte getSubType() {return org.apache.fop.pdf.PDFFont.TRUETYPE;}
    public boolean isEmbeddable() {
        return (embedFileName==null &amp;&amp; embedResourceName==null) ? false : true;
    }
    public PDFStream getFontFile(int i) {
        InputStream instream=null;

        // Get file first
        if (embedFileName!=null)
        try {
           instream=new FileInputStream(embedFileName);
        } catch (Exception e) {
           System.out.println("Failed to embed fontfile: "+embedFileName);
        }

        // Get resource
        if (instream==null &amp;&amp; embedResourceName!=null)
        try {
           instream=new BufferedInputStream(this.getClass().getResourceAsStream(embedResourceName));
        } catch (Exception e) {
           System.out.println("Failed to embed fontresource: "+embedResourceName);
        }

        if (instream==null)
            return (PDFStream)null;

        // Read fontdata
        byte[] file = new byte[128000];
        int fsize = 0;

        try {
          int l = instream.read(file, 0, 128000);
          fsize += l;

          if (l==128000) {
                 // More to read - needs to extend
             byte[] tmpbuf;

             while (l &gt; 0) {
                 tmpbuf = new byte[file.length + 64000];
                 System.arraycopy(file, 0, tmpbuf, 0, file.length);
                 l=instream.read(tmpbuf, file.length, 64000);
                 fsize += l;
                 file = tmpbuf;

                 if (l &lt; 64000) // whole file read. No need to loop again
                    l=0;
             }
          }

          embeddedFont=new PDFTTFStream(i, fsize);
          embeddedFont.addFilter("flate");
          embeddedFont.addFilter("ascii-85");
          embeddedFont.setData(file, fsize);
          instream.close();
        } catch (Exception e) {}

        return (PDFStream) embeddedFont;
    }
    public String encoding() {
        return encoding;
    }

    public String fontName() {
        return fontName;
    }

    public int getAscender() {return ascender;}
    public int getDescender() {return descender;}
    public int getCapHeight() {return capHeight;}

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

    public int getFlags() {
        return flags;
    }

    public int[] getFontBBox() {
        return fontBBox;
    }

    public int getItalicAngle() {
        return italicAngle;
    }

    public int getStemV() {
        return stemV;
    }

    public int getFirstChar() {
        return firstChar;
    }

    public int getLastChar() {
        return lastChar;
    }

    public int width(int i, int size) {
        return size * width[i];
    }

    public int[] getWidths(int size) {
        int[] arr = new int[getLastChar()-getFirstChar()+1];
        System.arraycopy(width, getFirstChar(), arr, 0, getLastChar()-getFirstChar()+1);
        for( int i = 0; i &lt; arr.length; i++) arr[i] *= size;
        return arr;
    }
}
<!--</redirect:write>-->
</xsl:template>
</xsl:stylesheet>


