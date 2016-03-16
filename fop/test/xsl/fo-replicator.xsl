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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:param name="repeats" select="2"/>
  <xsl:template match="@*|node()">
    <xsl:param name="run"/>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()">
        <xsl:with-param name="run" select="$run"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  <xsl:template match="fo:root">
    <fo:root>
      <xsl:apply-templates select="@*|fo:layout-master-set|fo:declarations">
        <xsl:with-param name="run" select="0"/>
      </xsl:apply-templates>
      <xsl:call-template name="repeat">
        <xsl:with-param name="n" select="$repeats"/>
        <xsl:with-param name="what" select="fo:page-sequence"/>
      </xsl:call-template>
    </fo:root>
  </xsl:template>
  <xsl:template name="repeat">
    <xsl:param name="n"/>
    <xsl:param name="what"/>
    <xsl:if test="number($n) > 0">
      <xsl:apply-templates select="$what">
        <xsl:with-param name="run" select="$n"/>
      </xsl:apply-templates>
      <xsl:call-template name="repeat">
        <xsl:with-param name="n" select="number($n) - 1"/>
        <xsl:with-param name="what" select="$what"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  <xsl:template match="fo:*/@id">
    <xsl:param name="run"/>
    <xsl:attribute name="id"><xsl:value-of select="."/>-<xsl:value-of select="$run"/></xsl:attribute>
  </xsl:template>
  <xsl:template match="fo:*/@ref-id">
    <xsl:param name="run"/>
    <xsl:attribute name="ref-id"><xsl:value-of select="."/>-<xsl:value-of select="$run"/></xsl:attribute>
  </xsl:template>
  <xsl:template match="fo:*/@internal-destination">
    <xsl:param name="run"/>
    <xsl:attribute name="internal-destination"><xsl:value-of select="."/>-<xsl:value-of select="$run"/></xsl:attribute>
  </xsl:template>
  <xsl:template match="fo:page-number-citation">
    <xsl:param name="run"/>
    <fo:inline><xsl:value-of select="@ref-id"/></fo:inline>
  </xsl:template>
  <xsl:template match="fo:retrieve-marker|fo:marker">
    <xsl:param name="run"/>
  </xsl:template>
</xsl:stylesheet>
