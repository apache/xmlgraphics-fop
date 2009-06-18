<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
                extension-element-prefixes="redirect">

<xsl:include href="./propinc.xsl"/>

<xsl:output method="text" />

<!-- zap text content -->
<xsl:template match="text()"/>

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
	<xsl:call-template name="hasEnum"/>
   </xsl:variable>
   <xsl:variable name="bSubpropEnum">
	<xsl:call-template name="hasSubpropEnum"/>
   </xsl:variable>

   <xsl:if test="$bEnum='true' or contains($bSubpropEnum, 'true')">
<redirect:write select="concat('@org/apache/fop@/fo/properties/', $classname, '.java')">
package org.apache.fop.fo.properties;

<!-- Handle enumeration values -->
    public interface <xsl:value-of select="$classname"/>
    <xsl:if test="use-generic and $bEnum='true'">
	extends  <xsl:value-of select="use-generic"/>.Enums
    </xsl:if>{
   <xsl:for-each select="enumeration/value">
     public final static int <xsl:value-of select="@const"/> = <xsl:number/>;
</xsl:for-each>
<xsl:if test="contains($bSubpropEnum, 'true')">
    <xsl:call-template name="genSubpropEnum"/>
</xsl:if>
    }
</redirect:write>
   </xsl:if>
</xsl:template>

<xsl:template name="genSubpropEnum">
  <xsl:param name="prop" select="."/>
  <xsl:choose>
    <xsl:when test="$prop/compound/subproperty/enumeration">
      <xsl:for-each select="compound/subproperty[enumeration]">
      public interface <xsl:value-of select="name"/> {
        <xsl:for-each select="enumeration/value">
        public final static int <xsl:value-of select="@const"/> = <xsl:number/>;
        </xsl:for-each>
      }
      </xsl:for-each>
    </xsl:when>
    <xsl:when test="$prop/use-generic">
      <xsl:call-template name="inhspenums">
         <xsl:with-param name="prop" select="key('genericref', $prop/use-generic)"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="$prop/compound/subproperty/use-generic">
	<!-- generate "interface <subprop> extends <gensubprop>.Enums" -->
	<xsl:for-each select="$prop/compound/subproperty[use-generic]">
	  <xsl:variable name="bSpEnum">
	    <xsl:call-template name="hasEnum">
	      <xsl:with-param name="prop"
	   	select="key('genericref', use-generic)"/>
            </xsl:call-template>
	  </xsl:variable>
	  <xsl:if test="$bSpEnum='true'">
	  public interface  <xsl:value-of select="name"/> extends  <xsl:value-of select="use-generic"/>.Enums { }
	  </xsl:if>
	</xsl:for-each>
    </xsl:when>
    <xsl:otherwise>false</xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="inhspenums">
  <xsl:param name="prop"/>
      <xsl:variable name="generic_name">
  <xsl:choose>
    <xsl:when test="$prop/class-name">
      <xsl:value-of select="$prop/class-name"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="makeClassName">
        <xsl:with-param name="propstr" select="$prop/name"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
      </xsl:variable>
	<!-- generate "interface <subprop> extends <genprop>.<subprop>" -->
      <xsl:for-each select="$prop/compound/subproperty[enumeration]">
      <xsl:variable name="spname">
        <xsl:call-template name="makeClassName">
          <xsl:with-param name="propstr" select="name"/>
        </xsl:call-template>
      </xsl:variable>
      public interface <xsl:value-of select="$spname"/> extends <xsl:value-of select="$generic_name"/>.Enums.<xsl:value-of select="$spname"/> {
      }
      </xsl:for-each>
      
    <xsl:if test="$prop/use-generic">
      <xsl:call-template name="inhspenums">
         <xsl:with-param name="prop" select="key('genericref', $prop/use-generic)"/>
      </xsl:call-template>
    </xsl:if>
</xsl:template>

</xsl:stylesheet>
