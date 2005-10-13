<?xml version="1.0" encoding="UTF-8"?>
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
<!-- This stylesheet is based on the forrest document2fo.xsl and attempts
     to mimic its style-->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format">

<xsl:import href="../../skins/common/xslt/fo/document2fo.xsl"/>

<xsl:output method="xml"/>

<xsl:preserve-space elements="*"/>

<xsl:variable name="cell-yes" select="'rgb(153, 204, 255)'"/>
<xsl:variable name="cell-no" select="'rgb(255, 153, 204)'"/>
<xsl:variable name="cell-partial" select="'rgb(192, 192, 192)'"/>
<xsl:variable name="cell-normal" select="'transparent'"/>

<xsl:template match="compliance">
<!-- Contents of this template are copied verbatim from Forrest document2fo.xsl
-->
<!--  <xsl:template match="document"> -->
    <fo:title><xsl:value-of select="head/title"/></fo:title>

    <fo:static-content flow-name="first-footer">
      <fo:block
        border-top="0.25pt solid"
        padding-before="6pt"
        text-align="center">
        <xsl:apply-templates select="footer"/>
      </fo:block>
      <fo:block
        text-align="start">
        Page <fo:page-number/>
      </fo:block>
      <xsl:call-template name="info"/>
    </fo:static-content>

    <fo:static-content flow-name="even-header">
      <fo:block
        text-align="end"
        font-style="italic">
        <xsl:value-of select="header/title"/>
      </fo:block>
    </fo:static-content>

    <fo:static-content flow-name="even-footer">
      <fo:block
        border-top="0.25pt solid"
        padding-before="6pt"
        text-align="center">
        <xsl:apply-templates select="footer"/>
      </fo:block>
      <fo:block
        text-align="end">
        Page <fo:page-number/>
      </fo:block>
      <xsl:call-template name="info"/>
    </fo:static-content>

    <fo:static-content flow-name="odd-header">
      <fo:block
        text-align="start"
        font-style="italic">
        <xsl:value-of select="header/title"/>
      </fo:block>
    </fo:static-content>

    <fo:static-content flow-name="odd-footer">
      <fo:block
        border-top="0.25pt solid"
        padding-before="6pt"
        text-align="center">
        <xsl:apply-templates select="footer"/>
      </fo:block>
      <fo:block
        text-align="start">
        Page <fo:page-number/>
      </fo:block>
      <xsl:call-template name="info"/>
    </fo:static-content>

    <fo:flow flow-name="xsl-region-body">
      <fo:block
        padding-before="24pt"
        padding-after="24pt"
        font-size="24pt"
        font-weight="bold"
        id="{generate-id()}">

        <xsl:value-of select="header/title"/>
      </fo:block>

      <fo:block
        text-align="justify"
        padding-before="18pt"
        padding-after="18pt">
        <xsl:apply-templates/>
      </fo:block>
    </fo:flow>
  </xsl:template>
<!-- End of material copied from Forrest document2fo.xsl -->

<xsl:template match="head">
  <fo:block
      font-size="18pt"
      text-align="center">
    <xsl:value-of select="/compliance/head/title"/>
  </fo:block>
</xsl:template>

<xsl:template match="body">
  <xsl:apply-templates select="standard"/>
</xsl:template>

<xsl:template match="standard">
  <fo:block
      font-size="16pt"
      text-align="left">
    <xsl:value-of select="@name"/>
  </fo:block>
  <xsl:apply-templates select="explanatory"/>
<!-- Comment out temporarily - FOP failing on build
  <xsl:apply-templates select="level-1"/>
-->
</xsl:template>

<xsl:template match="level-1">
  <fo:block
      font-size="16pt"
      text-align="left">
    <xsl:value-of select="@name"/>
  </fo:block>
  <xsl:apply-templates select="explanatory"/>
  <fo:table table-layout="fixed" width="6.0in">
    <!-- FIXME: Apache FOP must have column widths specified at present,
         this section can be removed when this limitation is removed from Fop.
         Unfortunately, this means that each column is a fixed width,
         but at least the table displays! -->
    <fo:table-column column-width="1.5in"/>
    <fo:table-column column-width=".5in"/>
    <fo:table-column column-width=".5in"/>
    <fo:table-column column-width=".5in"/>
    <fo:table-column column-width="2.5in"/>

    <fo:table-header>
      <fo:table-row>
        <fo:table-cell number-rows-spanned="2">
          <fo:block>
            <xsl:value-of select="@compliance-item-desc"/>
          </fo:block>
        </fo:table-cell>
        <fo:table-cell number-columns-spanned="3">
          <fo:block>Support</fo:block>
        </fo:table-cell>
        <fo:table-cell number-rows-spanned="2">
          <fo:block>Comments</fo:block>
        </fo:table-cell>
      </fo:table-row>
      <fo:table-row>
        <fo:table-cell>
          <fo:block>
            <xsl:value-of select="/compliance/body/standard/@compliance-level-1-desc"/>
          </fo:block>
        </fo:table-cell>
        <fo:table-cell>
          <fo:block>
            <xsl:value-of select="/compliance/body/standard/@compliance-level-2-desc"/>
          </fo:block>
        </fo:table-cell>
        <fo:table-cell>
          <fo:block>
            <xsl:value-of select="/compliance/body/standard/@compliance-level-3-desc"/>
          </fo:block>
        </fo:table-cell>
      </fo:table-row>
    </fo:table-header>
    <fo:table-body>
    <xsl:apply-templates select="level-2"/>
    </fo:table-body>
  </fo:table>
</xsl:template>

<xsl:template match="explanatory">
  <xsl:for-each select="p">
    <fo:block>
      <xsl:apply-templates/>
    </fo:block>
  </xsl:for-each>
</xsl:template>

<xsl:template match="level-2">
  <fo:table-row>
    <fo:table-cell number-columns-spanned="5" background-color="rgb(255, 204, 102)">
      <fo:block>
        <xsl:value-of select="@name"/>
      </fo:block>
    </fo:table-cell>
  </fo:table-row>
  <xsl:apply-templates select="level-3"/>
</xsl:template>

<xsl:template match="level-3">
  <fo:table-row>
    <fo:table-cell>
      <fo:block>
        <xsl:value-of select="@name"/>
      </fo:block>
    </fo:table-cell>
    <xsl:variable name="cell-attributes-level-1">
      <xsl:choose>
        <xsl:when test="@comply='yes'">
          <xsl:value-of select="$cell-yes"/>
        </xsl:when>
        <xsl:when test="@comply='no'">
          <xsl:choose>
            <xsl:when test="@compliance-level > 1">
              <xsl:value-of select="$cell-yes"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$cell-no"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="@comply='partial'">
          <xsl:choose>
            <xsl:when test="@compliance-level > 1">
              <xsl:value-of select="$cell-yes"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$cell-partial"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$cell-normal"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <fo:table-cell>
      <xsl:attribute name="background-color">
        <xsl:value-of select="$cell-attributes-level-1"/>
      </xsl:attribute>
      <fo:block>
        <xsl:choose>
          <xsl:when test="@compliance-level=1">
            <xsl:value-of select="@comply"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>.</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </fo:block>
    </fo:table-cell>
    <xsl:variable name="cell-attributes-level-2">
      <xsl:choose>
        <xsl:when test="@comply='yes'">
          <xsl:value-of select="$cell-yes"/>
        </xsl:when>
        <xsl:when test="@comply='no'">
          <xsl:choose>
            <xsl:when test="@compliance-level > 2">
              <xsl:value-of select="$cell-yes"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$cell-no"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="@comply='partial'">
          <xsl:choose>
            <xsl:when test="@compliance-level > 2">
              <xsl:value-of select="$cell-yes"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$cell-partial"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$cell-normal"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <fo:table-cell>
      <xsl:attribute name="background-color">
        <xsl:value-of select="$cell-attributes-level-2"/>
      </xsl:attribute>
      <fo:block>
        <xsl:choose>
          <xsl:when test="@compliance-level=2">
            <xsl:value-of select="@comply"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>.</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </fo:block>
    </fo:table-cell>
    <xsl:variable name="cell-attributes-level-3">
      <xsl:choose>
        <xsl:when test="@comply='yes'">
          <xsl:value-of select="$cell-yes"/>
        </xsl:when>
        <xsl:when test="@comply='no'">
          <xsl:choose>
            <xsl:when test="@compliance-level > 3">
              <xsl:value-of select="$cell-yes"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$cell-no"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="@comply='partial'">
          <xsl:choose>
            <xsl:when test="@compliance-level > 3">
              <xsl:value-of select="$cell-yes"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$cell-partial"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$cell-normal"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <fo:table-cell>
      <xsl:attribute name="background-color">
        <xsl:value-of select="$cell-attributes-level-3"/>
      </xsl:attribute>
      <fo:block>
        <xsl:choose>
          <xsl:when test="@compliance-level=3">
            <xsl:value-of select="@comply"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>.</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </fo:block>
    </fo:table-cell>
    <xsl:choose>
      <xsl:when test="count(comment) > 0">
        <fo:table-cell text-align="left">
          <fo:list-block>
            <xsl:for-each select="comment">
              <fo:list-item>
                <fo:list-item-label>
                  <fo:block>*</fo:block>
                </fo:list-item-label>
                <fo:list-item-body>
                  <fo:block>
                    <xsl:value-of select="."/>
                  </fo:block>
                </fo:list-item-body>
             </fo:list-item>
           </xsl:for-each>
          </fo:list-block>
        </fo:table-cell>
      </xsl:when>
      <xsl:otherwise>
        <fo:table-cell text-align="center">
          <fo:block>
            <xsl:text>.</xsl:text>
          </fo:block>
        </fo:table-cell>
      </xsl:otherwise>
    </xsl:choose>
  </fo:table-row>
</xsl:template>

</xsl:stylesheet>

<!-- Last Line of $RCSfile$ -->
