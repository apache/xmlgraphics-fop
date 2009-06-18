<?xml version="1.0"?> 
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:fo="http://www.w3.org/1999/XSL/Format">
	
<xsl:output method="html"/>

<xsl:strip-space elements="*"/>

<xsl:template match="/">
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="formattingObjects">
<html>
<head>
<title><xsl:value-of select="title"/></title>
</head>
<body>
<h2><xsl:value-of select="title"/></h2>
<xsl:apply-templates select="usage"/>
<h2>Block-Level Formatting Objects</h2>
<xsl:apply-templates select="category[@class='block-level']"/>
<h2>Inline-Level Formatting Objects</h2>
<xsl:apply-templates select="category[@class='inline-level']"/>
<h2>Other Formatting Objects</h2>
<xsl:apply-templates select="category[@class='other']"/>
<xsl:apply-templates select="notes"/>
</body>
</html>
</xsl:template>

<xsl:template match="usage">
	<xsl:apply-templates select="para"/>
</xsl:template>

<xsl:template match="para">
	<p><xsl:apply-templates/></p>
</xsl:template>

<xsl:template match="em">
	<em><xsl:apply-templates/></em>
</xsl:template>

<xsl:template match="term">
	<b><xsl:apply-templates/></b>
</xsl:template>

<xsl:template match="category">
	<table width="100%" cellpadding="5" cellspacing="1" border="1">
	<tr>
		<th>Name</th><th>Base Class</th><th>Spec Content</th>
		<th>Generated Areas</th><th>Breaks and Keeps</th>
	</tr>
	<xsl:apply-templates select="object"/>
	</table>
</xsl:template>

<xsl:template match="object">
	<tr>
		<td>
		<xsl:value-of select="name"/>
		<xsl:if test="self::node()[@implemented='false']">&#0160;*</xsl:if>
		</td>
		<td><xsl:value-of select="baseClass"/></td>
		<td><xsl:value-of select="specContent"/></td>
		<td><xsl:apply-templates select="generatedAreas"/></td>
		<td><xsl:value-of select="breaksKeeps"/>&#0160;</td>
	</tr>
</xsl:template>

<xsl:template match="generatedAreas">
<xsl:choose>
	<xsl:when test="currentLayout">
	<ul>
	<li>Class: <xsl:value-of select="type/class"/>&#0160;
	Stacking: <xsl:value-of select="type/stacking"/></li>
	<li>Multiplicity: <xsl:value-of select="multiplicity"/></li>
	<li>Layout into: <xsl:value-of select="currentLayout"/>
	<xsl:if test="currentLayout[@intoParentArea='true']">&#0160;(parent)</xsl:if></li>
	<xsl:if test="self::node()[@isReference='true']">
	<li>Reference Area(s)</li>
	</xsl:if>
	</ul>
	</xsl:when>
	<xsl:otherwise>&#0160;</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="notes">
<p><b>Notes:</b></p>
	<ol>
	<xsl:for-each select="note">
	<li><xsl:value-of select="."/></li>
	</xsl:for-each>
	</ol>
</xsl:template>

</xsl:stylesheet>

