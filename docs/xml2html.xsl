<?xml version="1.0"?>
<!--    XSLT stylesheet: QAML -> HTML  (QAML = FAQ Markup Language) 
                 author: F. Jannidis <jannidis@.lrz.uni-muenchen.de>
                version: 1.00ß
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html"/> 


<xsl:template match ="/">
    <html>
            <xsl:apply-templates/> 
    </html>
</xsl:template>


<!-- writes the header of the html file with a link to css stylesheet-->
<xsl:template match ="head">
<head>
<link rel="stylesheet" type="text/css" href="qaml-html.css"></link>
<xsl:apply-templates/>
</head>
</xsl:template>

<!-- don't output all of maintain and version-->
<xsl:template match ="maintain">
</xsl:template>
<xsl:template match ="version">
</xsl:template>


<!-- header title -->
<xsl:template match ="head/title">
<title>
            <xsl:apply-templates/> 
</title>
</xsl:template>


<!-- body -->
<xsl:template match ="body">
<body>
<!-- defines text title -->
<h1 class="title"><xsl:value-of select="//head/title"/></h1>

<!-- generates table of contents with links to the different levels-->
<p>
<xsl:for-each select="section[@class='level1']/title">
<xsl:number value="position()" format="A"/>) 
<a href="">
<xsl:attribute name="href"> 
#<xsl:number count="section[@class='level1']"/>
</xsl:attribute>
<xsl:value-of select="."/>
</a>
<br/>
</xsl:for-each>
</p>
<!-- processing of the rest -->
<xsl:apply-templates/>

<!-- writes the footer -->
<hr/>
<p class="contact">
Version: <xsl:value-of select="/faq/head/version"/> - 
Contact: 
<a> 
<xsl:attribute name="href">
mailto:<xsl:value-of select="//head/maintain/email"/>
</xsl:attribute>
<xsl:value-of select="//head/maintain/name"/>
</a> 
</p>
</body>
</xsl:template>

<!-- title level1, numbering is generated -->
<xsl:template match ="section[@class='level1']/title">
<h1>
<a>
<xsl:attribute name="name"> 
<xsl:number count="section[@class='level1']" format="A"/>
</xsl:attribute>
</a>
<xsl:number count="section[@class='level1']"  format="A"/>) 
      <xsl:apply-templates/> 
</h1>
</xsl:template>

<!-- title level2 -->
<xsl:template match ="section[@class='level2']/title">
<h2>
     <xsl:apply-templates/> 
</h2>
</xsl:template>

<!-- title level3 -->
<xsl:template match ="section[@class='level3']/title">
<h3>
     <xsl:apply-templates/> 
</h3>
</xsl:template>

<!-- question -->
<xsl:template match ="q">
<p class="question">
     <xsl:apply-templates/> 
</p>
</xsl:template>

<!-- answer -->
<xsl:template match ="a">
   <xsl:apply-templates/>
</xsl:template>

<!-- para in answer -->
<xsl:template match ="a/p">
<p class="answer">
<xsl:apply-templates/>
</p>
</xsl:template>

<!-- test span -->
<xsl:template match ="span">
<b><xsl:apply-templates/> </b>
</xsl:template>

<!-- list -->
<xsl:template match ="div[@class='list']">
<ul>
<xsl:apply-templates/>
</ul>
</xsl:template>

<!-- list items  -->
<xsl:template match ="div/p[@class='li']">
<li>
<xsl:apply-templates/>
</li>
</xsl:template>


<!-- code fragments, marked by attribute pre -->
<xsl:template match ="section/p[@class='pre']">
<p>
<xsl:attribute name="class"> 
<xsl:value-of select="@class"/>
</xsl:attribute>
<xsl:apply-templates/>
</p>
</xsl:template>


<!-- normal paras without class attribute --> 
<xsl:template match ="section/p[not(@class)]">
<p>
<xsl:apply-templates/>
</p>
</xsl:template>


<!-- links -->
<xsl:template match ="link">
<a>
<xsl:attribute name="href">
<xsl:value-of select="@href"/>
</xsl:attribute>
      <xsl:apply-templates/> 
</a>
</xsl:template>

</xsl:stylesheet>

