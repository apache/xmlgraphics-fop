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
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"/>
  <xsl:variable name='glyphlists'
                select="document('glyphlist.xml')/glyphlist-set"/>

  <xsl:template match="encoding-set">
package org.apache.fop.fonts;

import java.util.Map;
import java.util.Collections;

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

    private static Map mappings;
    static {
        mappings = Collections.synchronizedMap(new java.util.HashMap());
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
