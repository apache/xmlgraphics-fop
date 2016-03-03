<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  exclude-result-prefixes="fo">

  <xsl:output indent="yes" omit-xml-declaration="yes"/>

  <xsl:template match="/">
    <event>
      <xsl:text>start document</xsl:text>
    </event>
    <xsl:apply-templates/>
    <event>
      <xsl:text>end   document</xsl:text>
    </event>
  </xsl:template>

  <xsl:template match="fo:root">
    <event>start root</event>
    <xsl:apply-templates select="fo:page-sequence"/>
    <event>end   root</event>
  </xsl:template>

  <xsl:template match="fo:*">
    <xsl:call-template name="process.node">
      <xsl:with-param name="id">
        <xsl:apply-templates select="@id"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="process.node">
    <xsl:param name="id" select="''"/>
    <event>
      <xsl:text>start </xsl:text>
      <xsl:value-of select="local-name()"/>
      <xsl:value-of select="$id"/>
    </event>
    <xsl:apply-templates/>
    <event>
      <xsl:text>end   </xsl:text>
      <xsl:value-of select="local-name()"/>
      <xsl:value-of select="$id"/>
    </event>
  </xsl:template>

  <xsl:template match="@id">
    <xsl:text> id="</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>"</xsl:text>
  </xsl:template>

  <xsl:template match="text()"/>

</xsl:stylesheet>
