<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match ="/">
  <documentation>
    <xsl:copy-of select="document('fop/readme.xml')"/>
    <xsl:copy-of select="document('fop/download.xml')"/>
    <xsl:copy-of select="document('fop/running.xml')"/>
    <xsl:copy-of select="document('fop/implemented.xml')"/>
    <xsl:copy-of select="document('fop/limitations.xml')"/>
    <xsl:copy-of select="document('fop/bugs.xml')"/>
    <xsl:copy-of select="document('fop/examples.xml')"/>
    <xsl:copy-of select="document('fop/configuration.xml')"/>
    <xsl:copy-of select="document('fop/fonts.xml')"/>
    <xsl:copy-of select="document('fop/extensions.xml')"/>
    <xsl:copy-of select="document('fop/compiling.xml')"/>
    <xsl:copy-of select="document('fop/embedding.xml')"/>
    <xsl:copy-of select="document('fop/involved.xml')"/>
    <xsl:copy-of select="document('fop/architecture.xml')"/>
    <xsl:copy-of select="document('fop/faq.xml')"/>
    <xsl:copy-of select="document('fop/resources.xml')"/>
    <xsl:copy-of select="document('fop/license.xml')"/>
  </documentation>
</xsl:template>

</xsl:stylesheet>


