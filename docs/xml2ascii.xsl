<?xml version="1.0"?>
<!--    XSLT stylesheet: QAML -> No Markup  (QAML = FAQ Markup Language) 
                 author: F. Jannidis <jannidis@.lrz.uni-muenchen.de>
                version: 1.00ß
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="text"/> 
<xsl:template match ="/">
            <xsl:apply-templates/> 
</xsl:template>

<!-- writes the header of the html file with a link to css stylesheet-->
<xsl:template match ="head">
<xsl:apply-templates/>
</xsl:template>

<!-- don't output maintain and version-->
<xsl:template match ="maintain">
</xsl:template>
<xsl:template match ="version">
</xsl:template>

<!-- header title -->
<xsl:template match ="head/title">
            <xsl:apply-templates/> 
</xsl:template>

<!-- body -->
<xsl:template match ="body">
<!-- generates table of contents with links to the different levels-->
<xsl:for-each select="section[@class='level1']/title">
<xsl:text>&#10;</xsl:text><xsl:number value="position()"/>) <xsl:value-of select="."/>
</xsl:for-each>

<!-- processing of the rest -->
<xsl:apply-templates/>

<!-- writes the footer -->

Version: <xsl:value-of select="/faq/head/version"/> - 
Contact: <xsl:value-of select="//head/maintain/name"/> (<xsl:value-of select="//head/maintain/email"/>)
</xsl:template>

<!-- title level1, numbering is generated -->
<xsl:template match ="section[@class='level1']/title">
<xsl:number count="section[@class='level1']"/>) <xsl:apply-templates/> 
</xsl:template>

<!-- title level2 -->
<xsl:template match ="section[@class='level2']/title">
     <xsl:apply-templates/> 
</xsl:template>

<!-- title level3 -->
<xsl:template match ="section[@class='level3']/title">
     <xsl:apply-templates/> 
</xsl:template>

<!-- question -->
<xsl:template match ="q">
     <xsl:apply-templates/> 
</xsl:template>

<!-- answer -->
<xsl:template match ="a">
   <xsl:apply-templates/>
</xsl:template>

<!-- para  -->
<xsl:template match ="p">
<xsl:apply-templates/>
</xsl:template>

<!-- test span -->
<xsl:template match ="span">
<xsl:apply-templates/>
</xsl:template>

<!-- links -->
<xsl:template match ="link">
      <xsl:apply-templates/> (<xsl:value-of select="@href"/>)
</xsl:template>

</xsl:stylesheet>


