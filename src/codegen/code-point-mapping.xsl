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
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"/>
  <xsl:variable name='glyphlists'
                select="document('glyphlist.xml')/glyphlist-set"/>

  <xsl:template match="encoding-set">
package org.apache.fop.render.pdf;

import java.util.Hashtable;

public class CodePointMapping {
    private char[] latin1Map;
    private char[] characters;
    private char[] codepoints;
    private CodePointMapping(int [] table) {
        int nonLatin1 = 0;
        latin1Map = new char[256];
        for(int i = 0; i &lt; table.length; i += 2) {
            if(table[i+1] &lt; 256)
                latin1Map[table[i+1]] = (char) table[i];
            else
                ++nonLatin1;
        }
        characters = new char[nonLatin1];
        codepoints = new char[nonLatin1];
        int top = 0;
        for(int i = 0; i &lt; table.length; i += 2) {
            char c = (char) table[i+1];
            if(c >= 256) {
                ++top;
                for(int j = top - 1; j >= 0; --j) {
                    if(j > 0 &amp;&amp; characters[j-1] >= c) {
                        characters[j] = characters[j-1];
                        codepoints[j] = codepoints[j-1];
                    } else {
                        characters[j] = c;
                        codepoints[j] = (char) table[i];
                        break;
                    }
                }
            }
        }
    }
    public final char mapChar(char c) {
        if(c &lt; 256) {
            return latin1Map[c];
        } else {
            int bot = 0, top = characters.length - 1;
            while(top >= bot) {
                int mid = (bot + top) / 2;
                char mc = characters[mid];

                if(c == mc)
                    return codepoints[mid];
                else if(c &lt; mc)
                    top = mid - 1;
                else
                    bot = mid + 1;
            }
            return 0;
        }
    }

    private static Hashtable mappings;
    static {
        mappings = new Hashtable();
    }
    public static CodePointMapping getMapping(String encoding) {
        CodePointMapping mapping = (CodePointMapping) mappings.get(encoding);
        if(mapping != null) {
            return mapping;
        } <xsl:apply-templates mode="get"/>
        else {
            return null;
        }
    }
<xsl:apply-templates mode="table"/>
}
  </xsl:template>

  <xsl:template match="encoding" mode="get">
        else if(encoding.equals("<xsl:value-of select="@id"/>")) {
            mapping = new CodePointMapping(enc<xsl:value-of select="@id"/>);
            mappings.put("<xsl:value-of select="@id"/>", mapping);
            return mapping;
        }
  </xsl:template>

  <xsl:template match="encoding" mode="table">
    <xsl:variable name="glyphlist-name" select="@glyphlist"/>
    <xsl:variable name="glyphlist"
                  select="$glyphlists/glyphlist[@id=$glyphlist-name]"/>
    private static final int[] enc<xsl:value-of select="@id"/>
        = {<xsl:for-each select="glyph">
  <xsl:variable name="codepoint" select="@codepoint"/>
  <xsl:variable name="name" select="@name"/><xsl:for-each select="$glyphlist/glyph[@name=$name]">
            0x<xsl:value-of select="$codepoint"/>, 0x<xsl:value-of select="@codepoint"/>, // <xsl:value-of select="$name"/>
</xsl:for-each></xsl:for-each>
        };
  </xsl:template>
</xsl:stylesheet>
