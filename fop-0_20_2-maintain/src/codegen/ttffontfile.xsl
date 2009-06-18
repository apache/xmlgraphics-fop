<!--
$Id$
============================================================================
                   The Apache Software License, Version 1.1
============================================================================

Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

Redistribution and use in source and binary forms, with or without modifica-
tion, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

3. The end-user documentation included with the redistribution, if any, must
   include the following acknowledgment: "This product includes software
   developed by the Apache Software Foundation (http://www.apache.org/)."
   Alternately, this acknowledgment may appear in the software itself, if
   and wherever such third-party acknowledgments normally appear.

4. The names "FOP" and "Apache Software Foundation" must not be used to
   endorse or promote products derived from this software without prior
   written permission. For written permission, please contact
   apache@apache.org.

5. Products derived from this software may not be called "Apache", nor may
   "Apache" appear in their name, without prior written permission of the
   Apache Software Foundation.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
============================================================================

This software consists of voluntary contributions made by many individuals
on behalf of the Apache Software Foundation and was originally created by
James Tauber <jtauber@jtauber.com>. For more information on the Apache
Software Foundation, please see <http://www.apache.org/>.
--> 
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
import java.util.Hashtable;

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
    private final static Hashtable kerning=new Hashtable();
    static {
        width = new int[256];
<!--<xsl:for-each select="widths/char"><xsl:variable name="char-name" select="@name"/><xsl:variable name="char-num" select="document('charlist.xml')/font-mappings/map[@adobe-name=$char-name]/@win-ansi"/><xsl:if test="$char-num!='-1'">        width[<xsl:value-of select="$char-num"/>] = <xsl:value-of select="@width"/>;</xsl:if>-->
<xsl:for-each select="widths/char"><xsl:variable name="char-name" select="@name"/>   width[<xsl:value-of select="$char-name"/>] = <xsl:value-of select="@width"/>;
</xsl:for-each>

    Hashtable tmptable;
<xsl:for-each select="kerning">
<xsl:variable name="kpx1-name" select="@kpx1"/>
    tmptable=new Hashtable();<xsl:for-each select="pair"><xsl:variable name="kpx2-name" select="@kpx2"/><xsl:variable name="kern-name" select="@kern"/>
    tmptable.put(Glyphs.glyphToString("<xsl:value-of select="$kpx2-name"/>"), new Integer(<xsl:value-of select="$kern-name"/>));</xsl:for-each>
    kerning.put(Glyphs.glyphToString("<xsl:value-of select="$kpx1-name"/>"), tmptable);
</xsl:for-each>
    }

    public final boolean hasKerningInfo() {return kerning.isEmpty();}
    public final java.util.Hashtable getKerningInfo() {return kerning;}

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


