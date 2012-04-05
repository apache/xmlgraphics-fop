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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  xmlns:fox="http://xmlgraphics.apache.org/fop/extensions"
  xmlns:foi="http://xmlgraphics.apache.org/fop/internal">

  <xsl:output method="xml" indent="no"/>

  <xsl:template name="copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>


  <!-- Ignore fo:root -->
  <xsl:template match="fo:root">
    <structure-tree-sequence>
      <xsl:apply-templates/>
    </structure-tree-sequence>
  </xsl:template>

  <!-- fo:page-sequence maps to structure-tree -->
  <xsl:template match="fo:page-sequence">
    <structure-tree xmlns="http://xmlgraphics.apache.org/fop/intermediate">
        <xsl:apply-templates/>
    </structure-tree>
  </xsl:template>


  <!-- Declarations and Pagination and Layout Formatting Objects -->
  <xsl:template match="fo:static-content|fo:flow">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <!-- Block-level Formatting Objects -->
  <xsl:template match="fo:block|fo:block-container">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <!-- Inline-level Formatting Objects -->
  <xsl:template match="fo:character|fo:inline|fo:inline-container">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <xsl:template match="fo:external-graphic|fo:instream-foreign-object">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <xsl:template match="fo:page-number|fo:page-number-citation|fo:page-number-citation-last">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <!-- Formatting Objects for Tables -->
  <xsl:template match="fo:table-and-caption|fo:table-caption">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <xsl:template match="fo:table">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="*[name() != 'fo:table-footer']"/>
      <xsl:apply-templates select="fo:table-footer"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="fo:table-header|fo:table-footer|fo:table-body|fo:table-row|fo:table-cell">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <!-- Formatting Objects for Lists -->
  <xsl:template match="fo:list-block|fo:list-item|fo:list-item-label|fo:list-item-body">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <!-- Dynamic Effects: Link and Multi Formatting Objects -->
  <xsl:template match="fo:basic-link">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <!-- Out-of-Line Formatting Objects -->
  <xsl:template match="fo:float|fo:footnote|fo:footnote-body">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <!-- Other Formatting Objects -->
  <xsl:template match="fo:wrapper|fo:marker">
    <xsl:call-template name="copy"/>
  </xsl:template>


  <!-- Discard descendants of fo:leader -->
  <xsl:template match="fo:leader"/>


  <!-- Keep fox:alt-text and role attributes, discard everything else -->
  <xsl:template match="@fox:alt-text|@role">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="@*"/>


  <!-- Discard text nodes... -->
  <xsl:template match="text()"/>

  <!-- ...except those that will result into marked content -->
  <xsl:template match="fo:title/text()
    |fo:block/text()
    |fo:bidi-override/text()
    |fo:inline/text()
    |fo:basic-link/text()
    |fo:wrapper/text()
    |fo:marker/text()">
    <marked-content xmlns="http://xmlgraphics.apache.org/fop/intermediate"/>
  </xsl:template>

</xsl:stylesheet>
