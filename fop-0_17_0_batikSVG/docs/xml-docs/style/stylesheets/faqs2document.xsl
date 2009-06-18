<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/XSL/Transform/1.0">

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="faqs">
    <s1 title="{@title}">
      <s2 title="Questions">
        <ul>
          <xsl:apply-templates select="faq" mode="index"/>
        </ul>
      </s2>
      <s2 title="Answers">
        <br/>
        <xsl:apply-templates select="faq"/>
      </s2>
    </s1>
  </xsl:template>

  <xsl:template match="faq" mode="index">
    <li>
      <link anchor="faq-{position()}">
        <xsl:if test="string-length(@title)=0">
          <xsl:value-of select="q"/>
        </xsl:if>
        <xsl:if test="string-length(@title)>0">
          <xsl:value-of select="@title"/>
        </xsl:if>
      </link>
    </li>
  </xsl:template>

  <xsl:template match="faq">
    <anchor name="faq-{position()}"/>
    <s3 title="{q}">
      <xsl:apply-templates select="a"/>
    </s3>
  </xsl:template>

  <xsl:template match="a">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>