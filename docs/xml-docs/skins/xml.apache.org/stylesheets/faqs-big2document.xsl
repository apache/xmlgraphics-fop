<?xml version="1.0"?>

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0">

  <xsl:import href="copyover.xsl"/>
  <xsl:output indent="yes"/>
  <xsl:strip-space elements="*"/>
  
  <xsl:template match="faqs">
    <document>
      <header>
        <title><xsl:value-of select="@title"/></title>
        <type>FAQ</type>
      </header>
      <body>
        <s1 title="Questions">
          <xsl:apply-templates select="faqsection" mode="index"/>
        </s1>
        <s1 title="Answers">
          <xsl:apply-templates/>
        </s1>
      </body>
    </document>  
  </xsl:template>
  
  <xsl:template match="faq" mode="index">
    <li>
      <jump>
        <xsl:attribute name="anchor">faq-<xsl:number level="any"/></xsl:attribute>
        <xsl:value-of select="question"/>
      </jump>
    </li>
  </xsl:template>
  
  <xsl:template match="faqsection" mode="index">
    <s2 title="{@title}">
      <xsl:apply-templates select="faq" mode="index"/>
    </s2>
  </xsl:template>
  
  <xsl:template match="faqsection">
    <s2 title="{@title}">
      <xsl:apply-templates/>
    </s2>
  </xsl:template>
  
  <xsl:template match="faq">
    <anchor>
      <xsl:attribute name="id">faq-<xsl:number level="any"/></xsl:attribute>
    </anchor>
    <s3 title="{question}">
      <xsl:apply-templates/>
    </s3>
  </xsl:template>
  
  <xsl:template match="question">
    <!-- ignored since already used -->
  </xsl:template>
  
  <xsl:template match="answer">
    <xsl:apply-templates/>
  </xsl:template>
  
</xsl:stylesheet>