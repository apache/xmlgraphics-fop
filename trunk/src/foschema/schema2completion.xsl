<?xml version="1.0"?>
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
<xsl:stylesheet saxon:trace="no"
    version="1.1"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:saxon="http://icl.com/saxon"
    xmlns:xs = "http://www.w3.org/2001/XMLSchema"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:date="http://exslt.org/dates-and-times"
    xmlns:math="http://exslt.org/math"
    extension-element-prefixes="saxon date math"
    exclude-result-prefixes="fo xs">

<xsl:output method="xml" version="4.0" omit-xml-declaration="yes" indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:variable name="properties" select="document('foproperties.xml')"/>

<xsl:template match="/xs:schema">
  <xsl:apply-templates select="./xs:element"/>
</xsl:template>

<xsl:template match="xs:group">
  <xsl:for-each select="./xs:choice/xs:element">
    <xsl:value-of select="./@ref"/>
    <xsl:text>|</xsl:text>
  </xsl:for-each>
  <!-- Remove any Groups not implemented by FOP -->
  <xsl:for-each select="./xs:choice/xs:group[substring(./@ref,string-length(./@ref) - 3) != '_Not']">
    <xsl:variable name="ref">
      <xsl:call-template name="strip_fo">
        <xsl:with-param name="ref" select="./@ref"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:apply-templates select="/xs:schema/xs:group[@name=$ref]"/>
  </xsl:for-each>
</xsl:template>

<xsl:template match="xs:attributeGroup">
  <xsl:for-each select="./xs:attribute">
    <xsl:apply-templates select="."/>
  </xsl:for-each>
  <!-- Remove any attributeGroups not implemented by FOP -->
  <xsl:for-each select="./xs:attributeGroup[substring(./@ref,string-length(./@ref) - 3) != '_Not']">
    <xsl:variable name="ref">
      <xsl:call-template name="strip_fo">
        <xsl:with-param name="ref" select="./@ref"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:apply-templates select="/xs:schema/xs:attributeGroup[@name = $ref]"/>
  </xsl:for-each>
</xsl:template>

<xsl:template match="xs:simpleType">
  <xsl:variable name="name" select="@name"/>
  <xsl:for-each select="./xs:restriction/xs:enumeration">
    <xsl:value-of select="./@value"/>
    <xsl:text>|</xsl:text>
    <xsl:message>Attribute <xsl:value-of select="./@value"/></xsl:message>
  </xsl:for-each>
  <xsl:variable name="unions" select="./xs:union/@memberTypes"/>
  <xsl:if test="$unions">
    <xsl:call-template name="splitUnion">
      <xsl:with-param name="union" select="$unions"/>
    </xsl:call-template>
  </xsl:if>
    <xsl:for-each select="./xs:annotation/xs:documentation/xs:enumeration">
      <xsl:comment>
        This supplies annotated list values inserted in the schema
        solely for the purpose of supplying examples of valid values for a type.
      </xsl:comment>
      <xsl:value-of select="./@value"/>
      <xsl:text>|</xsl:text>
      <xsl:message>Fake Attribute <xsl:value-of select="./@value"/></xsl:message>
    </xsl:for-each>
</xsl:template>

<xsl:template name="splitUnion">
  <xsl:param name="union"/>
  <xsl:variable name="type">
    <xsl:call-template name="strip_fo">
      <xsl:with-param name="ref">
        <xsl:if test="not(substring-before($union,' '))">
          <xsl:value-of select="$union"/>
        </xsl:if>
        <xsl:value-of select="substring-before($union,' ')"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  <xsl:message>Calling <xsl:value-of select="$type"/></xsl:message>
  <xsl:apply-templates select="/xs:schema/xs:simpleType[@name = $type]"/>
  <xsl:if test="substring-after($union,' ')">
    <xsl:call-template name="splitUnion">
      <xsl:with-param name="union" select="substring-after($union,' ')"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template match="xs:element">
  <xsl:text disable-output-escaping="yes">&lt;element name="fo:</xsl:text>
  <xsl:value-of select="./@name"/>
  <xsl:text>"
content="</xsl:text>
  <xsl:choose>
    <xsl:when test="( not(./xs:complexType/xs:sequence) and not(./xs:complexType/xs:choice) )">
      <xsl:text>EMPTY</xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>(</xsl:text>
      <xsl:if test="./xs:complexType/@mixed = 'true'">
        <xsl:text>#PCDATA|</xsl:text>
      </xsl:if>
      <xsl:for-each select="./xs:complexType/xs:sequence|./xs:complexType/xs:choice">
        <xsl:for-each select="./xs:element">
          <xsl:value-of select="./@ref"/>
          <xsl:text>|</xsl:text>
        </xsl:for-each>
        <xsl:for-each select="./xs:group">
          <xsl:variable name="ref">
            <xsl:call-template name="strip_fo">
              <xsl:with-param name="ref" select="./@ref"/>
            </xsl:call-template>
          </xsl:variable>
          <xsl:apply-templates select="/xs:schema/xs:group[@name=$ref]"/>
        </xsl:for-each>
      </xsl:for-each>
      <xsl:text>)</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:text disable-output-escaping="yes">"&gt;
</xsl:text>
  <xsl:for-each select="./xs:complexType/xs:attribute">
    <xsl:apply-templates select="."/>
  </xsl:for-each>
  <xsl:for-each select="./xs:complexType/xs:attributeGroup">
    <xsl:variable name="ref">
      <xsl:call-template name="strip_fo">
        <xsl:with-param name="ref" select="./@ref"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:apply-templates select="/xs:schema/xs:attributeGroup[@name = $ref]"/>
  </xsl:for-each>
  <xsl:text disable-output-escaping="yes">
&lt;/element&gt;
</xsl:text>
</xsl:template>

<xsl:template match="xs:attribute">
  <xsl:variable name="attributeName" select="./@name"/>
  <xsl:variable name="type">
    <xsl:call-template name="strip_fo">
      <xsl:with-param name="ref" select="./@type"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:message>Processing type = <xsl:value-of select="$type"/></xsl:message>
  <attribute name="{$attributeName}">
    <xsl:attribute name="type">
      <xsl:variable name="content">
        <xsl:choose>
          <xsl:when test="$properties/property-list/generic-property-list/property[name = $attributeName]/enumeration">
            <!-- Get the valid values from FOP -->
            <xsl:for-each select="$properties/property-list/generic-property-list/property[name = $attributeName]/enumeration/value">
              <xsl:value-of select="."/><xsl:text>|</xsl:text>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="/xs:schema/xs:simpleType[@name = $type]"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$content != ''">
          <xsl:text>(</xsl:text>
          <xsl:value-of select="$content"/>
          <xsl:text>)</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>CDATA</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:attribute>
  </attribute>
</xsl:template>

<xsl:template name="strip_fo">
  <xsl:param name="ref"/>
  <xsl:choose>
    <xsl:when test="substring($ref,1,3) = 'fo:'">
      <xsl:value-of select="substring($ref,4)"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$ref"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>