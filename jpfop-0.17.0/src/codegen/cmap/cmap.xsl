<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
                extension-element-prefixes="redirect">
<xsl:output method="text" />

<xsl:template match="character-collection">
package org.apache.fop.render.pdf.cmap;

import org.apache.fop.render.pdf.CMap;

public class <xsl:value-of select="class-name"/> implements CMap {
    public char mapping(char ch) {
        int cp = (int)ch;

<xsl:apply-templates select="codespacerange"/>

<xsl:apply-templates select="notdefrange"/>
<xsl:apply-templates select="cidrange"/>

        return (char)<xsl:value-of select="notdefrange/width"/>;
    }
}
</xsl:template>

<xsl:template match="codespacerange">
<!-- <xsl:param name="cCodeSpaceRange" select="count(range)"/> -->
        if (!(<xsl:apply-templates select="range">
		<xsl:with-param name="cCodeSpaceRange">
			<xsl:value-of select="count(range)"/>
		</xsl:with-param>
		</xsl:apply-templates>
        )) {
            return (char)<xsl:value-of select="../notdefrange/width"/>;
        }
</xsl:template>

<xsl:template match="codespacerange/range">
<xsl:param name="cCodeSpaceRange" select="$cCodeSpaceRange"/>
            (cp &gt;= 0x<xsl:value-of select="start"/> &amp;&amp; 0x<xsl:value-of select="end"/> &gt;= cp) <xsl:if test="position() != $cCodeSpaceRange">||</xsl:if>
</xsl:template>

<xsl:template match="notdefrange">
        if (cp &gt;= 0x<xsl:value-of select="start"/> &amp;&amp; 0x<xsl:value-of select="end"/> &gt;= cp) {
            return (char)<xsl:value-of select="width"/>;
        }
</xsl:template>

<xsl:template match="cidrange">
<!-- <xsl:param name="cCidRange" select="count(range)"/> -->
        if <xsl:apply-templates select="range">
		<xsl:with-param name="cCidRange">
			<xsl:value-of select="count(range)"/>
		</xsl:with-param>
		</xsl:apply-templates>
</xsl:template>

<xsl:template match="cidrange/range">
<xsl:param name="cCidRange" select="$cCidRange"/>
<!-- <xsl:param name="cpStart" select="start"/>
<xsl:param name="cpEnd" select="end"/> -->
<xsl:choose>
	<xsl:when test="start != end">(cp &gt;= 0x<xsl:value-of select="start"/> &amp;&amp; 0x<xsl:value-of select="end"/> &gt;= cp) {
            return (char)(<xsl:value-of select="width"/>+cp-0x<xsl:value-of select="start"/>);
			</xsl:when>
			<xsl:otherwise>(cp == 0x<xsl:value-of select="start"/>) {
            return (char)(<xsl:value-of select="width"/>);</xsl:otherwise></xsl:choose>
        }<xsl:if test="position() != $cCidRange"> else if </xsl:if></xsl:template>

</xsl:stylesheet>
