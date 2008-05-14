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
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"/>
  <xsl:variable name='glyphlists'
                select="document('glyphlist.xml')/glyphlist-set"/>

  <xsl:template match="encoding-set"> /*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.fop.fonts;

import java.util.Map;
import java.util.Collections;

public class CodePointMapping extends AbstractCodePointMapping {

<xsl:apply-templates mode="constant"/>

    public CodePointMapping(String name, int[] table) {
        super(name, table);
    }

    public CodePointMapping(String name, int[] table, String[] charNameMap) {
        super(name, table, charNameMap);
    }

    private static Map mappings;
    static {
        mappings = Collections.synchronizedMap(new java.util.HashMap());
    }

    public static CodePointMapping getMapping(String encoding) {
        CodePointMapping mapping = (CodePointMapping) mappings.get(encoding);
        if (mapping != null) {
            return mapping;
        } <xsl:apply-templates mode="get"/>
        throw new UnsupportedOperationException("Unknown encoding: " + encoding);
    }
<xsl:apply-templates mode="table"/>
<xsl:apply-templates select="encoding" mode="names"/>
}
  </xsl:template>

  <xsl:template match="encoding" mode="constant">    public static final String <xsl:value-of select="@constant"/> = "<xsl:value-of select="@id"/>";</xsl:template>
  
  <xsl:template match="encoding" mode="get">
        else if (encoding.equals(<xsl:value-of select="@constant"/>)) {
    mapping = new CodePointMapping(<xsl:value-of select="@constant"/>, enc<xsl:value-of select="@id"/>, names<xsl:value-of select="@id"/>);
            mappings.put(<xsl:value-of select="@constant"/>, mapping);
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
  
  <xsl:template match="encoding" mode="names">
    private static final String[] names<xsl:value-of select="@id"/>
    = {
<xsl:call-template name="charname">
  <xsl:with-param name="idx" select="0"/>
</xsl:call-template>
        };
  </xsl:template>
  
  <xsl:template name="charname">
    <xsl:param name="idx"/>
    <xsl:variable name="idxHEXraw">
      <xsl:call-template name="toHex">
        <xsl:with-param name="decimalNumber" select="$idx"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="idxHEX">
      <xsl:call-template name="padnumber">
        <xsl:with-param name="num" select="$idxHEXraw"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="idxhex" select="translate($idxHEX, 'ABCDEF', 'abcdef')"></xsl:variable>
    <!--
    <xsl:value-of select="$idx"/>-<xsl:value-of select="$idxHEXraw"/>-<xsl:value-of select="$idxHEX"/>-<xsl:value-of select="$idxhex"/>
    -->
    <xsl:if test="($idx mod 4) = 0">
      <xsl:text>&#x0D;    /*</xsl:text><xsl:value-of select="$idxHEX"/><xsl:text>*/ </xsl:text>
    </xsl:if>
    <xsl:variable name="v">
      <xsl:value-of select="child::glyph[@codepoint = $idxHEX or @codepoint = $idxhex]/@name"/><!--<xsl:value-of select="glyph[@codepoint = $idxhex]/@name"/>-->
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string-length($v) > 0">
        <xsl:text>"</xsl:text><xsl:value-of select="$v"/><xsl:text>"</xsl:text>
      </xsl:when>
      <xsl:otherwise>null</xsl:otherwise>
    </xsl:choose>
    
    <xsl:if test="$idx &lt; 255">
      <xsl:text>, </xsl:text>
      <xsl:call-template name="charname">
        <xsl:with-param name="idx" select="$idx + 1"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
  <xsl:variable name="hexDigits" select="'0123456789ABCDEF'"/>
  <xsl:template name="toHex">
    <xsl:param name="decimalNumber" />
    <xsl:if test="$decimalNumber >= 16">
      <xsl:call-template name="toHex">
        <xsl:with-param name="decimalNumber" select="floor($decimalNumber div 16)" />
      </xsl:call-template>
    </xsl:if>
    <xsl:value-of select="substring($hexDigits, ($decimalNumber mod 16) + 1, 1)" />
  </xsl:template>
  
  <xsl:template name="padnumber">
    <xsl:param name="num"/>
    <xsl:param name="len" select="2"/>
    <!--
    <xsl:text> </xsl:text><xsl:value-of select="$num"/>/<xsl:value-of select="$len"/>
    -->
    <xsl:choose>
      <xsl:when test="string-length($num) &lt; $len">
        <xsl:call-template name="padnumber">
          <xsl:with-param name="num" select="concat('0',$num)"/>
          <xsl:with-param name="len" select="$len"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="$num"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
