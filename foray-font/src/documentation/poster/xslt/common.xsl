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
		xmlns:svg="http://www.w3.org/2000/svg">

  <xsl:template match="@*|node()[namespace-uri() = 'http://www.w3.org/1999/XSL/Format']">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@*|node()[namespace-uri() = 'http://www.w3.org/2000/svg']">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="p">
    <xsl:choose>
      <xsl:when test="ancestor::li and not(preceding-sibling::*)">
        <fo:block
          space-after="4pt">
          <xsl:apply-templates/>
        </fo:block>
      </xsl:when>
      <xsl:otherwise>
        <fo:block
          space-before="4pt"
          space-after="4pt">
          <xsl:apply-templates/>
        </fo:block>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="ol|ul">
    <fo:list-block
      provisional-distance-between-starts="18pt"
      provisional-label-separation="3pt"
      text-align="start">
      <xsl:apply-templates/>
    </fo:list-block>
  </xsl:template>

  <xsl:template match="ol/li">
    <fo:list-item>
      <xsl:if test="not(following-sibling::li[1])">
        <xsl:attribute name="space-after">6pt</xsl:attribute>
      </xsl:if>
      <fo:list-item-label
        end-indent="label-end()">
        <fo:block>
          <xsl:number format="1."/>
        </fo:block>
      </fo:list-item-label>
      <fo:list-item-body
        start-indent="body-start()">
        <fo:block>
          <xsl:apply-templates/>
        </fo:block>
      </fo:list-item-body>
    </fo:list-item>
  </xsl:template>

  <xsl:template match="ul/li">
    <fo:list-item>
      <xsl:if test="not(following-sibling::li[1])">
        <xsl:attribute name="space-after">6pt</xsl:attribute>
      </xsl:if>
      <fo:list-item-label end-indent="label-end()">
        <fo:block>&#x2022;</fo:block>
      </fo:list-item-label>
      <fo:list-item-body start-indent="body-start()">
        <fo:block>
          <xsl:apply-templates/>
        </fo:block>
      </fo:list-item-body>
    </fo:list-item>
  </xsl:template>

	
</xsl:stylesheet>