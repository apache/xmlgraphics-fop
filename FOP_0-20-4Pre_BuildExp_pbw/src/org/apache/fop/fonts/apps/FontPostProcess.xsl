<?xml version="1.0" encoding="UTF-8"?>
<!-- xslt stylesheets belonging to the pfmreader -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml"/>

<xsl:template match="@*|node()">
	<xsl:copy>
		<xsl:apply-templates select="@*|node()"/>
	</xsl:copy>
</xsl:template>

<xsl:template match="widths">
	<xsl:element name="widths">
		<xsl:for-each select="char">
			<xsl:variable name="char-num" select="@ansichar"/>
			<xsl:variable name="char-name" select="document('file:charlist.xml')/font-mappings/map[@win-ansi=$char-num]/@adobe-name"/>
			<xsl:if test="$char-name!=''">
				<xsl:element name="char">
					<xsl:attribute name="name"><xsl:value-of select="$char-name"/></xsl:attribute>
					<xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute>
				</xsl:element>
			</xsl:if>
		</xsl:for-each>
	</xsl:element>
</xsl:template>

</xsl:stylesheet>
