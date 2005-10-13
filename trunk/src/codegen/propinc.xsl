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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >

<xsl:key name="genericref" match="property[@type='generic']" use="class-name"/>
<xsl:key name="shorthandref" match="property" use="name"/>

<xsl:template name="capfirst">
  <xsl:param name="str"/>
  <xsl:variable name="lcletters" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="ucletters" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
     <xsl:value-of select="concat(translate(substring($str, 1, 1),
              $lcletters, $ucletters), substring($str, 2))"/>
</xsl:template>

<xsl:template name="makeClassName">
  <xsl:param name="propstr"/>
  <xsl:choose>
   <xsl:when test="contains($propstr, '-')">
    <xsl:call-template name="capfirst">
      <xsl:with-param name="str" select="substring-before($propstr, '-')"/>
    </xsl:call-template>
    <xsl:call-template name="makeClassName">
      <xsl:with-param name="propstr" select="substring-after($propstr, '-')"/>
    </xsl:call-template>
   </xsl:when>
   <xsl:otherwise>
    <xsl:call-template name="capfirst">
      <xsl:with-param name="str" select="$propstr"/>
    </xsl:call-template>
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Generate enumeration constants for FO's, Properties, etc. -->
<xsl:template name="makeEnumConstant">
  <xsl:param name="propstr"/>
  <xsl:variable name="lcletters" select="'abcdefghijklmnopqrstuvwxyz-:'" />
  <xsl:variable name="ucletters" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ__'" />
  <xsl:value-of select="translate($propstr, $lcletters, $ucletters)"/>
</xsl:template>

<!-- The name of the subclass of Property to be created -->
<xsl:template name="propclass">
  <xsl:param name="prop" select="."/>
  <xsl:choose>
    <xsl:when test="$prop/datatype">
      <xsl:value-of select="$prop/datatype"/><xsl:text>Property</xsl:text>
    </xsl:when>
    <xsl:when test="$prop/use-generic[@ispropclass='true']">
      <xsl:value-of select="$prop/use-generic"/>
    </xsl:when>
    <xsl:when test="$prop/use-generic">
      <!-- If no datatype child, then the prop must use the same datatype as
           its template. -->
  <xsl:call-template name="propclass">
    <xsl:with-param name="prop"
     select="key('genericref', $prop/use-generic)"/>
        </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <!-- ERROR -->
      <xsl:message terminate="yes">
  No datatype found for property: <xsl:value-of select="$prop/name"/>
      </xsl:message>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- return a boolean value -->
<xsl:template name="hasEnum">
  <xsl:param name="prop" select="."/>
  <xsl:choose>
    <xsl:when test="$prop/enumeration">true</xsl:when>
    <xsl:when test="$prop/use-generic">
      <!-- If no datatype child, then the prop must use the same datatype as
           its template. -->
  <xsl:call-template name="hasEnum">
    <xsl:with-param name="prop"
     select="key('genericref', $prop/use-generic)"/>
        </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>false</xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- return a boolean value -->
<xsl:template name="hasSubpropEnum">
  <xsl:param name="prop" select="."/>
  <xsl:choose>
    <xsl:when test="$prop/compound/subproperty/enumeration">true</xsl:when>
    <xsl:when test="$prop/use-generic">
      <!-- If no datatype child, then the prop must use the same datatype as
           its template. -->
  <xsl:call-template name="hasSubpropEnum">
    <xsl:with-param name="prop"
     select="key('genericref', $prop/use-generic)"/>
        </xsl:call-template>
    </xsl:when>
    <xsl:when test="$prop/compound/subproperty/use-generic">
  <xsl:for-each select="$prop/compound/subproperty[use-generic]">
    <xsl:call-template name="hasEnum">
      <xsl:with-param name="prop"
       select="key('genericref', use-generic)"/>
          </xsl:call-template>
  </xsl:for-each>
    </xsl:when>
    <xsl:otherwise>false</xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
