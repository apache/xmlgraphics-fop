<?xml version="1.0" encoding="UTF-8"?>

<!-- $Id$ -->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format">

<xsl:preserve-space elements="*"/>

<xsl:template match="/compliance">
	<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

<!--

		<fo:layout-master-set>
			<fo:simple-page-master master-name="simple"
										page-height="29.7cm"
										page-width="21cm"
										margin-top="1.5cm"
										margin-bottom="1.5cm"
										margin-left="2.5cm"
										margin-right="2.5cm">
				<fo:region-body margin-top="1.5cm"/>
				<fo:region-before extent="1.5cm"/>
				<fo:region-after extent="1.5cm"/>
			</fo:simple-page-master>
		</fo:layout-master-set>
		<fo:page-sequence master-reference="simple"
			    font-family="serif"
			    font-size="11pt">
			<fo:static-content flow-name="xsl-region-before">
				<fo:block text-align="end"
							font-size="10pt"
							font-family="serif"
							line-height="14pt" >
					XSL-FO Compliance - p. <fo:page-number/>
				</fo:block>
			</fo:static-content>
			<fo:flow flow-name="xsl-region-body">
        <xsl:apply-templates select="head"/>
        <xsl:apply-templates select="body"/>
			</fo:flow>
		</fo:page-sequence>
-->
	</fo:root>
</xsl:template>
<!--

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
  <xsl:apply-templates select="level-1"/>
</xsl:template>

<xsl:template match="level-1">
  <fo:block
      font-size="16pt"
      text-align="left">
    <xsl:value-of select="@name"/>
  </fo:block>
  <xsl:apply-templates select="explanatory"/>
  <fo:table width="16mm">
    <fo:table-header>
      <fo:table-row>
        <fo:table-cell number-rows-spanned="2">
          <xsl:value-of select="@compliance-item-desc"/>
        </fo:table-cell>
        <fo:table-cell number-cols-spanned="3">
          Support
        </fo:table-cell>
        <fo:table-cell number-rows-spanned="2">
          Comments
        </fo:table-cell>
      </fo:table-row>
      <fo:table-row>
        <fo:table-cell>
          <xsl:value-of select="/compliance/body/standard/@compliance-level-1-desc"/>
        </fo:table-cell>
        <fo:table-cell>
          <xsl:value-of select="/compliance/body/standard/@compliance-level-2-desc"/>
        </fo:table-cell>
        <fo:table-cell>
          <xsl:value-of select="/compliance/body/standard/@compliance-level-3-desc"/>
        </fo:table-cell>
      </fo:table-row>
    </fo:table-header>
    <xsl:apply-templates select="level-2"/>
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
    <fo:table-cell number-cols-spanned="5">
      <xsl:value-of select="@name"/>
    </fo:table-cell>
  </fo:table-row>
  <xsl:apply-templates select="level-3"/>
</xsl:template>

<xsl:template match="level-3">
  <fo:table-row>
    <fo:table-cell>
      <xsl:value-of select="@name"/>
    </fo:table-cell>
    <fo:table-cell>
      <xsl:attribute name="class">
        <xsl:choose>
          <xsl:when test="@comply='yes'">
            <xsl:text>yes</xsl:text>
          </xsl:when>
          <xsl:when test="@comply='no'">
            <xsl:choose>
              <xsl:when test="@compliance-level > 1">
                <xsl:text>yes</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>no</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="@comply='partial'">
            <xsl:choose>
              <xsl:when test="@compliance-level > 1">
                <xsl:text>yes</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>partial</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text></xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:choose>
        <xsl:when test="@compliance-level=1">
          <xsl:value-of select="@comply"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>.</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </fo:table-cell>
    <fo:table-cell>
      <xsl:attribute name="class">
        <xsl:choose>
          <xsl:when test="@comply='yes'">
            <xsl:text>yes</xsl:text>
          </xsl:when>
          <xsl:when test="@comply='no'">
            <xsl:choose>
              <xsl:when test="@compliance-level > 2">
                <xsl:text>yes</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>no</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="@comply='partial'">
            <xsl:choose>
              <xsl:when test="@compliance-level > 2">
                <xsl:text>yes</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>partial</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text></xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:choose>
        <xsl:when test="@compliance-level=2">
          <xsl:value-of select="@comply"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>.</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </fo:table-cell>
    <fo:table-cell>
      <xsl:attribute name="class">
        <xsl:choose>
          <xsl:when test="@comply='yes'">
            <xsl:text>yes</xsl:text>
          </xsl:when>
          <xsl:when test="@comply='no'">
            <xsl:choose>
              <xsl:when test="@compliance-level > 3">
                <xsl:text>yes</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>no</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="@comply='partial'">
            <xsl:choose>
              <xsl:when test="@compliance-level > 3">
                <xsl:text>yes</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>partial</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text></xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:choose>
        <xsl:when test="@compliance-level=3">
          <xsl:value-of select="@comply"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>.</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </fo:table-cell>
    <xsl:choose>
      <xsl:when test="count(comment) > 0">
        <fo:table-cell align="left">
          <fo:list>
            <xsl:for-each select="comment">
              <fo:list-item>
                <xsl:value-of select="."/>
             </fo:list-item>
           </xsl:for-each>
          </fo:list>
        </fo:table-cell>
      </xsl:when>
      <xsl:otherwise>
        <fo:table-cell align="center">
          <xsl:text>.</xsl:text>
        </fo:table-cell>
      </xsl:otherwise>
    </xsl:choose>
  </fo:table-row>
</xsl:template>

-->

</xsl:stylesheet>

<!-- Last Line of $RCSfile$ -->