<?xml version="1.0" encoding="utf-8"?>
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
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  xmlns:svg="http://www.w3.org/2000/svg">

  <xsl:output method="xml" indent="yes"/>

  <xsl:param name="single-family" select="''"/>

  <xsl:template match="fonts">
    <fo:root font-family="sans-serif" font-size="10pt">
      <!-- defines the layout master -->
      <fo:layout-master-set>
        <fo:simple-page-master master-name="A4" page-height="29.7cm" page-width="21cm"
          margin="1.5cm">
          <fo:region-body/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <!-- starts actual layout -->
      <xsl:choose>
        <xsl:when test="string-length($single-family) &gt; 0">
          <xsl:apply-templates select="family[@name = $single-family]"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="bookmarks"/>
          <xsl:call-template name="toc"/>
          <xsl:apply-templates/>
        </xsl:otherwise>
      </xsl:choose>
    </fo:root>
  </xsl:template>

  <xsl:template name="bookmarks">
    <fo:bookmark-tree>
      <fo:bookmark internal-destination="toc">
        <fo:bookmark-title>Table of Contents</fo:bookmark-title>
      </fo:bookmark>
      <xsl:apply-templates mode="bookmark"/>
    </fo:bookmark-tree>
  </xsl:template>

  <xsl:template name="toc">
    <fo:page-sequence master-reference="A4" id="toc">
      <fo:flow flow-name="xsl-region-body">
        <fo:block>
          <fo:block font-size="14pt" font-weight="bold" space-after="1em">FOP Font List</fo:block>
          <fo:block space-after="0.5em">The number of font families: <xsl:value-of select="count(family)"/></fo:block>
        </fo:block>
        <xsl:if test="count(family) > 0">
          <fo:list-block provisional-distance-between-starts="1.6em"
            provisional-label-separation="0.5em">
            <xsl:apply-templates mode="toc"/>
          </fo:list-block>
        </xsl:if>
      </fo:flow>
    </fo:page-sequence>
  </xsl:template>

  <xsl:template match="family" mode="bookmark">
    <fo:bookmark internal-destination="{generate-id()}">
      <fo:bookmark-title>
        <xsl:value-of select="@name"/>
      </fo:bookmark-title>
    </fo:bookmark>
  </xsl:template>
  
  <xsl:template match="family" mode="toc">
    <fo:list-item>
      <fo:list-item-label start-indent="2mm" end-indent="label-end()">
        <fo:block hyphenation-character="&#x2212;" font-family="Symbol">&#x2022;</fo:block>
      </fo:list-item-label>
      <fo:list-item-body start-indent="body-start()">
        <fo:block>
          <fo:basic-link internal-destination="{generate-id()}">
            <xsl:value-of select="@name"/>
          </fo:basic-link>
        </fo:block>
      </fo:list-item-body>
    </fo:list-item>
  </xsl:template>

  <xsl:template match="family">
    <fo:page-sequence master-reference="A4" id="{generate-id()}">
      <fo:flow flow-name="xsl-region-body">
        <fo:block>
          <fo:block font-size="14pt" font-weight="bold" space-after="0.5em" border-bottom="solid 0.5mm">
            <xsl:value-of select="@name"/>
          </fo:block>
          <fo:block>
            <fo:block font-weight="bold">Fonts:</fo:block>
            <xsl:apply-templates/>
          </fo:block>
        </fo:block>
        <xsl:call-template name="weight-sample">
          <xsl:with-param name="font-family" select="@stripped-name"/>
        </xsl:call-template>
      </fo:flow>
    </fo:page-sequence>
  </xsl:template>

  <xsl:template name="weight-sample">
    <xsl:param name="font-family" select="'sans-serif'"/>
    <fo:block border="solid 0.25mm" start-indent="0.25mm" end-indent="0.25mm"
        space-before="0.5em"
        keep-together.within-column="always">
      <fo:block font-size="8pt"
        background-color="black" color="white"
        padding="0mm 1mm" start-indent="1.25mm" end-indent="1.25mm">
        Weight Sample: font-family="<xsl:value-of select="$font-family"/>" font-weight="100..900"</fo:block>
      <fo:block padding="1mm 1mm" start-indent="1.25mm" end-indent="1.25mm">
        <xsl:attribute name="font-family">
          <xsl:value-of select="$font-family"/>
        </xsl:attribute>
        <fo:block font-weight="100">100: The quick brown fox jumps over the lazy dog</fo:block>
        <fo:block font-weight="200">200: The quick brown fox jumps over the lazy dog</fo:block>
        <fo:block font-weight="300">300: The quick brown fox jumps over the lazy dog</fo:block>
        <fo:block font-weight="400">400: The quick brown fox jumps over the lazy dog</fo:block>
        <fo:block font-weight="500">500: The quick brown fox jumps over the lazy dog</fo:block>
        <fo:block font-weight="600">600: The quick brown fox jumps over the lazy dog</fo:block>
        <fo:block font-weight="700">700: The quick brown fox jumps over the lazy dog</fo:block>
        <fo:block font-weight="800">800: The quick brown fox jumps over the lazy dog</fo:block>
        <fo:block font-weight="900">900: The quick brown fox jumps over the lazy dog</fo:block>
        <fo:block>
          <fo:instream-foreign-object>
            <svg xmlns="http://www.w3.org/2000/svg" width="16cm" height="108pt">
              <g font-family="sans-serif" font-weight="bold" font-size="80pt"
                  transform="translate(30, 100) rotate(-15)">
                <text fill="lightgray">SVG</text>
              </g>
              <g font-size="10pt">
                <xsl:attribute name="font-family">
                  '<xsl:value-of select="$font-family"/>'
                </xsl:attribute>
                <text x="0" y="10">
                  <tspan x="0" dy="0" font-weight="100">100: The quick brown fox jumps over the lazy dog</tspan>
                  <tspan x="0" dy="12" font-weight="200">200: The quick brown fox jumps over the lazy dog</tspan>
                  <tspan x="0" dy="12" font-weight="300">300: The quick brown fox jumps over the lazy dog</tspan>
                  <tspan x="0" dy="12" font-weight="400">400: The quick brown fox jumps over the lazy dog</tspan>
                  <tspan x="0" dy="12" font-weight="500">500: The quick brown fox jumps over the lazy dog</tspan>
                  <tspan x="0" dy="12" font-weight="600">600: The quick brown fox jumps over the lazy dog</tspan>
                  <tspan x="0" dy="12" font-weight="700">700: The quick brown fox jumps over the lazy dog</tspan>
                  <tspan x="0" dy="12" font-weight="800">800: The quick brown fox jumps over the lazy dog</tspan>
                  <tspan x="0" dy="12" font-weight="900">900: The quick brown fox jumps over the lazy dog</tspan>
                </text>
              </g>
            </svg>
          </fo:instream-foreign-object>
        </fo:block>
      </fo:block>
    </fo:block>
  </xsl:template>

  <xsl:template match="font">
    <fo:block>Internal key: <xsl:value-of select="@key"/></fo:block>
    <fo:block border="solid 0.25mm" start-indent="0.25mm" end-indent="0.25mm">
      <fo:block font-size="8pt"
          background-color="black" color="white"
          padding="0mm 1mm" start-indent="1.25mm" end-indent="1.25mm">
        Sample:</fo:block>
      <fo:block font-family="{triplets/triplet[1]/@name}"
        font-style="{triplets/triplet[1]/@style}"
        font-weight="{triplets/triplet[1]/@weight}"
        font-size="14pt" padding="1mm" start-indent="1.25mm" end-indent="1.25mm">
        The quick brown fox jumps over the lazy dog.
      </fo:block>
    </fo:block>
    <fo:block start-indent="5mm">
      <fo:block>Accessible by:</fo:block>
      <fo:block start-indent="10mm">
        <xsl:apply-templates select="triplets/triplet"/>
      </fo:block>  
    </fo:block>
  </xsl:template>
  
  <xsl:template match="triplet">
    <fo:block color="gray">
      font-family=<fo:inline color="black">"<xsl:value-of select="@name"/>"</fo:inline>
      font-style=<fo:inline color="black"><xsl:value-of select="@style"/>"</fo:inline>
      font-weight=<fo:inline color="black"><xsl:value-of select="@weight"/>"</fo:inline>
    </fo:block>
  </xsl:template>
</xsl:stylesheet>
