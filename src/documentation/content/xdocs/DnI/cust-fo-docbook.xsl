<?xml version="1.0" encoding="utf-8"?>

<!--
  * Copyright 2004-2005 The Apache Software Foundation.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  -->

<!-- $Id$ -->

<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:l="http://docbook.sourceforge.net/xmlns/l10n/1.0"
  version="1.0">

  <xsl:import
    href="http://docbook.sourceforge.net/release/xsl/current/fo/docbook.xsl"/>

  <!-- Add other variable definitions here -->
  <xsl:variable name="paper.type" select="'A4'"/>
  <xsl:param name="section.autolabel" select="1"/>
  <xsl:param name="section.label.includes.component.label" select="1"/>
  <xsl:param name="marker.section.level" select="1"/>
  <xsl:param name="component.title.several.objects" select="1"/>
  <xsl:param name="fop.extensions" select="1"/>
  <xsl:param name="draft.mode" select="'no'"/>
  <xsl:param name="draft.watermark.image"/>
  <!-- Double sided does not produce good headers and footers -->
  <xsl:param name="double.sided" select="1"/>
  <!-- Add to the section title properties -->
  <xsl:attribute-set name="section.title.properties">
    <xsl:attribute name="text-align">start</xsl:attribute>
    <xsl:attribute name="hyphenate">false</xsl:attribute>
  </xsl:attribute-set>
  <!-- Reintroduce the attribute set component.title.properties -->
  <xsl:attribute-set name="component.title.properties">
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="space-before.optimum">
      <xsl:value-of select="$body.font.master"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
    <xsl:attribute name="space-before.minimum">
      <xsl:value-of select="$body.font.master * 0.8"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
    <xsl:attribute name="space-before.maximum">
      <xsl:value-of select="$body.font.master * 1.2"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
    <xsl:attribute name="text-align">start</xsl:attribute>
    <xsl:attribute name="hyphenate">false</xsl:attribute>
  </xsl:attribute-set>
  <!-- spacing between label and title -->
  <xsl:attribute-set name="component.title.label.properties">
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="space-after.optimum">
      <xsl:value-of select="$body.font.master"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
    <xsl:attribute name="space-after.minimum">
      <xsl:value-of select="$body.font.master * 0.8"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
    <xsl:attribute name="space-after.maximum">
      <xsl:value-of select="$body.font.master * 1.2"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
    <xsl:attribute name="font-size">
      <xsl:value-of select="$body.font.master * 2.0736"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="book.titlepage.author.properties">
    <xsl:attribute name="font-size">
      <xsl:value-of select="$body.font.master * 1.728"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="book.titlepage.affiliation.properties">
    <xsl:attribute name="space-before">
      <xsl:value-of select="$body.font.master * 1.2"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
    <xsl:attribute name="font-style">
      <xsl:text>italic</xsl:text>
    </xsl:attribute>
    <xsl:attribute name="font-size">
      <xsl:value-of select="$body.font.master * 1.44"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
    <xsl:attribute name="font-weight">
      <xsl:text>normal</xsl:text>
    </xsl:attribute>
  </xsl:attribute-set>
  <!-- No headers and footers on blank pages -->
  <xsl:param name="headers.on.blank.pages" select="0"/>
  <xsl:param name="footers.on.blank.pages" select="0"/>
  <xsl:param name="headers.on.first.pages" select="0"/>
  <xsl:param name="header.left.width"
             select="'proportional-column-width(1)'"/>
  <xsl:param name="header.center.width"
             select="'proportional-column-width(5)'"/>
  <xsl:param name="header.right.width"
             select="'proportional-column-width(1)'"/>
  <!-- No left margin for titles -->
  <xsl:param name="title.margin.left" select="'0pc'"/>

  <!--
     * Similar to mode object.title.markup
     * but call template substitute-markup with the abbreviated title
     -->
  <xsl:template match="*" mode="object.titleabbrev.markup">
    <xsl:param name="allow-anchors" select="0"/>
    <xsl:variable name="template">
      <xsl:apply-templates select="." mode="object.title.template"/>
    </xsl:variable>

    <xsl:call-template name="substitute-markup">
      <xsl:with-param name="allow-anchors" select="$allow-anchors"/>
      <xsl:with-param name="template" select="$template"/>
      <xsl:with-param name="title" select="titleabbrev"/>
    </xsl:call-template>
  </xsl:template>

  <!--
     * Construct titleabbrev with mode object.titleabbrev.markup
     * so that the section label is included
     -->
  <xsl:template match="section/title
                       |simplesect/title
                       |sect1/title
                       |sect2/title
                       |sect3/title
                       |sect4/title
                       |sect5/title"
                       mode="titlepage.mode"
                       priority="2">
    <xsl:variable name="section" select="parent::*"/>
    <fo:block keep-with-next.within-column="always">
      <xsl:variable name="id">
        <xsl:call-template name="object.id">
          <xsl:with-param name="object" select="$section"/>
        </xsl:call-template>
      </xsl:variable>

      <xsl:variable name="level">
        <xsl:call-template name="section.level">
          <xsl:with-param name="node" select="$section"/>
        </xsl:call-template>
      </xsl:variable>

      <xsl:variable name="marker">
        <xsl:choose>
          <xsl:when test="$level &lt;= $marker.section.level">1</xsl:when>
          <xsl:otherwise>0</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="title">
        <xsl:apply-templates select="$section" mode="object.title.markup">
          <xsl:with-param name="allow-anchors" select="1"/>
        </xsl:apply-templates>
      </xsl:variable>

      <xsl:variable name="titleabbrev">
        <xsl:apply-templates select="$section" mode="titleabbrev.markup"/>
      </xsl:variable>

      <!-- Use for running head only if actual titleabbrev element -->
      <xsl:variable name="titleabbrev.elem">
        <xsl:if test="$section/titleabbrev">
          <xsl:apply-templates select="$section" 
                               mode="object.titleabbrev.markup"/>
        </xsl:if>
      </xsl:variable>

      <xsl:if test="$passivetex.extensions != 0">
        <fotex:bookmark xmlns:fotex="http://www.tug.org/fotex" 
                        fotex-bookmark-level="{$level + 2}" 
                        fotex-bookmark-label="{$id}">
          <xsl:value-of select="$titleabbrev"/>
        </fotex:bookmark>
      </xsl:if>

      <xsl:if test="$axf.extensions != 0">
        <xsl:attribute name="axf:outline-level">
          <xsl:value-of select="count(ancestor::*)-1"/>
        </xsl:attribute>
        <xsl:attribute name="axf:outline-expand">false</xsl:attribute>
        <xsl:attribute name="axf:outline-title">
          <xsl:value-of select="$title"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:call-template name="section.heading">
        <xsl:with-param name="level" select="$level"/>
        <xsl:with-param name="title" select="$title"/>
        <xsl:with-param name="marker" select="$marker"/>
        <xsl:with-param name="titleabbrev" select="$titleabbrev.elem"/>
      </xsl:call-template>
    </fo:block>
  </xsl:template>

  <!--
     * Use the attribute set component.title.properties
     * Use mode several.objects.title.markup for actual title
     -->

  <xsl:template name="component.title">
    <xsl:param name="node" select="."/>
    <xsl:param name="pagewide" select="0"/>
    <xsl:variable name="id">
      <xsl:call-template name="object.id">
        <xsl:with-param name="object" select="$node"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="title">
      <xsl:apply-templates select="$node" mode="object.title.markup">
        <xsl:with-param name="allow-anchors" select="1"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:variable name="titleabbrev">
      <xsl:apply-templates select="$node" mode="titleabbrev.markup"/>
    </xsl:variable>

    <xsl:if test="$passivetex.extensions != 0">
      <fotex:bookmark xmlns:fotex="http://www.tug.org/fotex"
                      fotex-bookmark-level="2"
                      fotex-bookmark-label="{$id}">
        <xsl:value-of select="$titleabbrev"/>
      </fotex:bookmark>
    </xsl:if>

    <fo:block xsl:use-attribute-sets="component.title.properties">
      <xsl:if test="$pagewide != 0">
        <!-- Doesn't work to use 'all' here since not a child of fo:flow -->
        <xsl:attribute name="span">inherit</xsl:attribute>
      </xsl:if>
      <xsl:attribute name="hyphenation-character">
        <xsl:call-template name="gentext">
          <xsl:with-param name="key" select="'hyphenation-character'"/>
        </xsl:call-template>
      </xsl:attribute>
      <xsl:attribute name="hyphenation-push-character-count">
        <xsl:call-template name="gentext">
          <xsl:with-param name="key"
                          select="'hyphenation-push-character-count'"/>
        </xsl:call-template>
      </xsl:attribute>
      <xsl:attribute name="hyphenation-remain-character-count">
        <xsl:call-template name="gentext">
          <xsl:with-param name="key"
                          select="'hyphenation-remain-character-count'"/>
        </xsl:call-template>
      </xsl:attribute>
      <xsl:if test="$axf.extensions != 0">
        <xsl:attribute name="axf:outline-level">
          <xsl:value-of select="count($node/ancestor::*)"/>
        </xsl:attribute>
        <xsl:attribute name="axf:outline-expand">false</xsl:attribute>
        <xsl:attribute name="axf:outline-title">
          <xsl:value-of select="$title"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="$component.title.several.objects != 0">
          <xsl:apply-templates select="$node" 
                               mode="several.objects.title.markup"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:copy-of select="$title"/>
        </xsl:otherwise>
      </xsl:choose>
    </fo:block>
  </xsl:template>

  <!-- 
    * Docbook-XSL's templates only allow a string value
    * for the label and title.
    * We want label and title in different fo elements.
    * Mode several.objects.title.markup
    -->
  <xsl:template match="chapter" mode="several.objects.title.markup">

    <fo:block xsl:use-attribute-sets="component.title.label.properties">
      <xsl:call-template name="gentext">
        <xsl:with-param name="key" select="'Chapter'"/>
      </xsl:call-template>
      <xsl:text>&#xA0;</xsl:text>
      <xsl:apply-templates select="." mode="insert.label.markup">
        <xsl:with-param name="label">
          <xsl:apply-templates select="." mode="label.markup"/>
        </xsl:with-param>
      </xsl:apply-templates>
    </fo:block>

    <fo:block>
      <xsl:apply-templates select="." mode="insert.title.markup">
        <xsl:with-param name="title">
          <xsl:apply-templates select="." mode="title.markup">
          </xsl:apply-templates>
        </xsl:with-param>
      </xsl:apply-templates>
    </fo:block>

  </xsl:template>

  <!--
     * Modify the header content:
     * chapter number,
     * section title only on odd pages,
     * section number also with abbreviated titles
     -->
  <xsl:template name="header.content">
    <xsl:param name="pageclass" select="''"/>
    <xsl:param name="sequence" select="''"/>
    <xsl:param name="position" select="''"/>
    <xsl:param name="gentext-key" select="''"/>

    <fo:block>

      <!-- sequence can be odd, even, first, blank -->
      <!-- position can be left, center, right -->
      <xsl:choose>
        <xsl:when test="$sequence = 'blank'">
          <!-- nothing on blank pages -->
        </xsl:when>

        <xsl:when test="$sequence = 'first'">
          <!-- nothing for first pages -->
        </xsl:when>

        <xsl:when test="$position='left' or $position='right'">
          <!-- only draft on the left and right sides -->
          <xsl:call-template name="draft.text"/>
        </xsl:when>

        <xsl:when test="($sequence='odd' or $sequence='even')
                        and $position='center'">
          <xsl:if test="$pageclass != 'titlepage'">
            <xsl:choose>
              <xsl:when test="ancestor::book and ($double.sided != 0)
                              and $sequence='odd'">
                <fo:retrieve-marker retrieve-class-name="section.head.marker"
                                retrieve-position="first-including-carryover"
                                    retrieve-boundary="page-sequence"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates select="." mode="label.markup"/>
                <xsl:text>.&#xA0;</xsl:text>
                <xsl:apply-templates select="." mode="titleabbrev.markup"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
        </xsl:when>

      </xsl:choose>
    </fo:block>
  </xsl:template>

  <!-- Parametrize the widths of the header components -->
  <xsl:template name="header.table">
    <xsl:param name="pageclass" select="''"/>
    <xsl:param name="sequence" select="''"/>
    <xsl:param name="gentext-key" select="''"/>

    <!-- default is a single table style for all headers -->
    <!-- Customize it for different page classes or sequence location -->

    <xsl:choose>
      <xsl:when test="$pageclass = 'index'">
        <xsl:attribute name="margin-left">0pt</xsl:attribute>
      </xsl:when>
    </xsl:choose>

    <xsl:variable name="candidate">
      <fo:table table-layout="fixed" width="100%">
        <xsl:call-template name="head.sep.rule">
          <xsl:with-param name="pageclass" select="$pageclass"/>
          <xsl:with-param name="sequence" select="$sequence"/>
          <xsl:with-param name="gentext-key" select="$gentext-key"/>
        </xsl:call-template>

        <fo:table-column column-number="1"
                         column-width="{$header.left.width}"/>
        <fo:table-column column-number="2"
                         column-width="{$header.center.width}"/>
        <fo:table-column column-number="3"
                         column-width="{$header.right.width}"/>
        <fo:table-body>
          <fo:table-row height="14pt">
            <fo:table-cell text-align="left" display-align="before">
              <xsl:if test="$fop.extensions = 0">
                <xsl:attribute name="relative-align">baseline</xsl:attribute>
              </xsl:if>
              <fo:block>
                <xsl:call-template name="header.content">
                  <xsl:with-param name="pageclass" select="$pageclass"/>
                  <xsl:with-param name="sequence" select="$sequence"/>
                  <xsl:with-param name="position" select="'left'"/>
                  <xsl:with-param name="gentext-key" select="$gentext-key"/>
                </xsl:call-template>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="center" display-align="before">
              <xsl:if test="$fop.extensions = 0">
                <xsl:attribute name="relative-align">baseline</xsl:attribute>
              </xsl:if>
              <fo:block>
                <xsl:call-template name="header.content">
                  <xsl:with-param name="pageclass" select="$pageclass"/>
                  <xsl:with-param name="sequence" select="$sequence"/>
                  <xsl:with-param name="position" select="'center'"/>
                  <xsl:with-param name="gentext-key" select="$gentext-key"/>
                </xsl:call-template>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell text-align="right" display-align="before">
              <xsl:if test="$fop.extensions = 0">
                <xsl:attribute name="relative-align">baseline</xsl:attribute>
              </xsl:if>
              <fo:block>
                <xsl:call-template name="header.content">
                  <xsl:with-param name="pageclass" select="$pageclass"/>
                  <xsl:with-param name="sequence" select="$sequence"/>
                  <xsl:with-param name="position" select="'right'"/>
                  <xsl:with-param name="gentext-key" select="$gentext-key"/>
                </xsl:call-template>
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
        </fo:table-body>
      </fo:table>
    </xsl:variable>

    <!-- Really output a header? -->
    <xsl:choose>
      <xsl:when test="$pageclass = 'titlepage' and $gentext-key = 'book'
                      and $sequence='first'">
        <!-- no, book titlepages have no headers at all -->
      </xsl:when>
      <xsl:when test="($sequence = 'blank' and $headers.on.blank.pages = 0)
                   or ($sequence = 'first' and $headers.on.first.pages = 0)">
        <!-- no output -->
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="$candidate"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- author and affiliation on the titlepage -->
  <xsl:template match="author" mode="titlepage.mode">
    <fo:block xsl:use-attribute-sets="book.titlepage.author.properties">
      <xsl:call-template name="anchor"/>
      <xsl:call-template name="person.name"/>
    </fo:block>
    <xsl:apply-templates select="affiliation" mode="titlepage.mode"/>
  </xsl:template>

  <xsl:template match="affiliation" mode="titlepage.mode">
    <fo:block xsl:use-attribute-sets="book.titlepage.affiliation.properties">
      <xsl:for-each select="*">
        <xsl:if test="position() != 1">
          <xsl:text>, </xsl:text>
        </xsl:if>
        <xsl:apply-templates mode="titlepage.mode"/>
      </xsl:for-each>
    </fo:block>
  </xsl:template>

  <!--
     * I am not sure about the purpose of this template;
     * in FOP it causes an extra page, which is not blank.
     -->
  <xsl:template name="book.titlepage.separator">
    <!--
    <fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format" break-after="page"/>
-->
  </xsl:template>

  <!-- Add revhistory to the verso page -->
  <xsl:template name="book.titlepage.verso">
    <xsl:choose>
      <xsl:when test="bookinfo/title">
        <xsl:apply-templates mode="book.titlepage.verso.auto.mode"
          select="bookinfo/title"/>
      </xsl:when>
      <xsl:when test="title">
        <xsl:apply-templates mode="book.titlepage.verso.auto.mode"
          select="title"/>
      </xsl:when>
    </xsl:choose>

    <xsl:apply-templates mode="book.titlepage.verso.auto.mode"
      select="bookinfo/corpauthor"/>
    <xsl:apply-templates mode="book.titlepage.verso.auto.mode"
      select="bookinfo/authorgroup"/>
    <xsl:apply-templates mode="book.titlepage.verso.auto.mode"
      select="bookinfo/author"/>
    <xsl:apply-templates mode="book.titlepage.verso.auto.mode"
      select="bookinfo/othercredit"/>
    <xsl:apply-templates mode="book.titlepage.verso.auto.mode"
      select="bookinfo/pubdate"/>
    <xsl:apply-templates mode="book.titlepage.verso.auto.mode"
      select="bookinfo/copyright"/>
    <xsl:apply-templates mode="book.titlepage.verso.auto.mode"
      select="bookinfo/abstract"/>
    <xsl:apply-templates mode="book.titlepage.verso.auto.mode"
      select="bookinfo/revhistory"/>
    <xsl:apply-templates mode="book.titlepage.verso.auto.mode"
      select="bookinfo/legalnotice"/>
  </xsl:template>

  <xsl:template match="revhistory" mode="book.titlepage.verso.auto.mode">
    <fo:block xsl:use-attribute-sets="book.titlepage.verso.style
                                      normal.para.spacing">
      <xsl:apply-templates select="." mode="book.titlepage.verso.mode"/>
    </fo:block>
  </xsl:template>
  
</xsl:stylesheet>
