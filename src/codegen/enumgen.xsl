<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
                extension-element-prefixes="redirect">

<xsl:include href="./propinc.xsl"/>

<xsl:output method="text" />

<xsl:template match="property[not(@type='generic')]">
 <xsl:variable name="classname">
  <xsl:choose>
    <xsl:when test="class-name">
      <xsl:value-of select="class-name"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="makeClassName">
        <xsl:with-param name="propstr" select="name"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
  </xsl:variable>
   <xsl:variable name="bEnum">
	<xsl:call-template name="isEnum"/>
   </xsl:variable>
   <xsl:if test="$bEnum='true'">
<redirect:write select="concat('@org/apache/fop@/fo/properties/', $classname, '.java')">
package org.apache.fop.fo.properties;

<!-- Handle enumeration values -->
    public interface <xsl:value-of select="$classname"/>
    <xsl:if test="use-generic">
	extends  <xsl:value-of select="use-generic"/>.Enums
    </xsl:if>{
   <xsl:for-each select="enumeration/value">
     public final static int <xsl:value-of select="@const"/> = <xsl:number/>;
</xsl:for-each>
    }
</redirect:write>
   </xsl:if>
</xsl:template>


</xsl:stylesheet>
