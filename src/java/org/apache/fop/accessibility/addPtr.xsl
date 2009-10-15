<?xml version="1.0" encoding="UTF-8"?>
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
  xmlns:foi="http://xmlgraphics.apache.org/fop/internal">

  <xsl:template name="addPtr">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:attribute name="foi:ptr">
        <xsl:value-of select="generate-id()"/>
      </xsl:attribute>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- Block-level Formatting Objects -->
  <xsl:template match="fo:block|fo:block-container">
    <xsl:call-template name="addPtr"/>
  </xsl:template>

  <!-- Inline-level Formatting Objects -->
  <xsl:template match="fo:character|fo:inline|fo:inline-container">
    <xsl:call-template name="addPtr"/>
  </xsl:template>

  <xsl:template match="fo:external-graphic|fo:instream-foreign-object">
    <xsl:call-template name="addPtr"/>
  </xsl:template>

  <xsl:template match="fo:page-number|fo:page-number-citation|fo:page-number-citation-last">
    <xsl:call-template name="addPtr"/>
  </xsl:template>

  <!-- Formatting Objects for Tables -->
  <xsl:template match="fo:table-and-caption|fo:table-caption|fo:table">
    <xsl:call-template name="addPtr"/>
  </xsl:template>

  <xsl:template match="fo:table-header|fo:table-footer|fo:table-body|fo:table-row|fo:table-cell">
    <xsl:call-template name="addPtr"/>
  </xsl:template>

  <!-- Formatting Objects for Lists -->
  <xsl:template match="fo:list-block|fo:list-item|fo:list-item-label|fo:list-item-body">
    <xsl:call-template name="addPtr"/>
  </xsl:template>

  <!-- Dynamic Effects: Link and Multi Formatting Objects -->
  <xsl:template match="fo:basic-link">
    <xsl:call-template name="addPtr"/>
  </xsl:template>

  <!-- Out-of-Line Formatting Objects -->
  <xsl:template match="fo:float|fo:footnote|fo:footnote-body">
    <xsl:call-template name="addPtr"/>
  </xsl:template>

  <!-- Other Formatting Objects -->
  <xsl:template match="fo:wrapper|fo:marker">
    <xsl:call-template name="addPtr"/>
  </xsl:template>


  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
