<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt">

<xsl:include href="propinc.xsl"/>

<xsl:output method="text" />


<xsl:template name="genmaker">
  <xsl:param name="prop" select="."/>
  <xsl:param name="htname"/>

  <xsl:variable name="makerclass">
   <xsl:choose>
    <xsl:when test="$prop/use-generic and count($prop/*)=2">
     <xsl:value-of select="$prop/use-generic"/>
    </xsl:when>
    <xsl:when test="$prop/class-name">
     <xsl:value-of select="$prop/class-name"/><xsl:text>Maker</xsl:text>
    </xsl:when>
    <xsl:otherwise> <!-- make from name -->
      <xsl:call-template name="makeClassName">
        <xsl:with-param name="propstr" select="$prop/name"/>
      </xsl:call-template><xsl:text>Maker</xsl:text>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:variable>
<xsl:text>    </xsl:text><xsl:value-of select="$htname"/>.put("<xsl:value-of select="$prop/name"/>", <xsl:value-of select="$makerclass"/>.maker("<xsl:value-of select="$prop/name"/>"));
</xsl:template>


<xsl:template match="text()"/>

<xsl:template match="property-list">
package org.apache.fop.fo.properties;

import java.util.HashMap;
import java.util.Set;
//import org.apache.fop.svg.*;

public class <xsl:value-of select="@family"/>PropertyMapping {

  private static HashMap s_htGeneric = new HashMap();
  private static HashMap s_htElementLists = new HashMap();
  <xsl:for-each select="element-property-list">
  private static HashMap s_ht<xsl:value-of select="localname[1]"/>;</xsl:for-each>

  <xsl:apply-templates/>

  public static HashMap getGenericMappings() {
    return s_htGeneric;
  }

  public static Set getElementMappings() {
    return s_htElementLists.keySet();
  }

  public static HashMap getElementMapping(String elemName) {
    return (HashMap)s_htElementLists.get(elemName);
  }
}
</xsl:template>

<xsl:template match="generic-property-list">
  static {
    // Generate the generic mapping
<xsl:apply-templates>
    <xsl:with-param name="htname" select='"s_htGeneric"'/>
  </xsl:apply-templates>
  }
</xsl:template>

<xsl:template match="element-property-list">
  <xsl:variable name="ename" select="localname[1]"/>
  static {
    s_ht<xsl:value-of select="$ename"/> = new HashMap();
   <xsl:for-each select="localname">
    s_htElementLists.put("<xsl:value-of select='.'/>", s_ht<xsl:value-of select='$ename'/>);
   </xsl:for-each>

<xsl:apply-templates>
    <xsl:with-param name='htname'>s_ht<xsl:value-of select="$ename"/></xsl:with-param>
    </xsl:apply-templates>
  }
</xsl:template>

<xsl:template match="property">
  <xsl:param name="htname"/>
  <xsl:variable name="refname" select="name"/>
  <xsl:choose>
    <xsl:when test="@type='ref'">
      <xsl:call-template name="genmaker">
        <xsl:with-param name="htname" select="$htname"/>
        <xsl:with-param name="prop"
          select='document(concat(@family, "properties.xml"))//property[name=$refname]'/>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="not(@type)">
      <xsl:call-template name="genmaker">
	  <xsl:with-param name="htname" select="$htname"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise/>
  </xsl:choose>
</xsl:template>

<xsl:template match="property[@type='generic']">
  /* PROPCLASS = <xsl:call-template name="propclass"/> */
</xsl:template>

</xsl:stylesheet>


