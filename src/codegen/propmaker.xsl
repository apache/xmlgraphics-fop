<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
                extension-element-prefixes="redirect">

<xsl:output method="xml" />

<xsl:template match="elements">
<property-list family="SVG">
  <generic-property-list>
    <xsl:apply-templates select="*//attribute"/>
 </generic-property-list>
</property-list>
</xsl:template>

<xsl:template match="*//attribute">
  <property>
	<name><xsl:apply-templates/></name>
    <use-generic ispropclass="true">SVGStringProperty</use-generic>
  </property>
</xsl:template>


</xsl:stylesheet>
