<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<xsl:output method="text" />

<xsl:template match="allprops">
<xsl:variable name="constlist">
    <xsl:for-each select="document(propfile)//enumeration/value">
    <xsl:sort select="@const"/>
    <xsl:value-of select="@const"/>:</xsl:for-each>
</xsl:variable>
package org.apache.fop.fo.properties;

public interface Constants {
  <xsl:call-template name="sortconsts">
     <xsl:with-param name="consts" select="$constlist"/>
  </xsl:call-template>
}
</xsl:template>

<xsl:template name="sortconsts">
<xsl:param name="consts"/>
<xsl:param name="prevconst"/>
<xsl:param name="num" select="1"/>
<xsl:variable name="cval" select="substring-before($consts,':')"/>
<xsl:choose>
 <xsl:when test="$consts = ''"/>
 <xsl:when test="$cval = $prevconst">
  <xsl:call-template name="sortconsts">
     <xsl:with-param name="consts" select="substring-after($consts,concat($cval, ':'))"/>
     <xsl:with-param name="num" select="$num"/>
     <xsl:with-param name="prevconst" select="$cval"/>
  </xsl:call-template>
 </xsl:when>
 <xsl:otherwise>
        public final static int <xsl:value-of select="$cval"/> = <xsl:value-of select="$num"/>;
  <xsl:call-template name="sortconsts">
     <xsl:with-param name="consts" select="substring-after($consts,concat($cval, ':'))"/>
     <xsl:with-param name="num" select="$num + 1"/>
     <xsl:with-param name="prevconst" select="$cval"/>
  </xsl:call-template>
  </xsl:otherwise>
</xsl:choose>
</xsl:template>
</xsl:stylesheet>
