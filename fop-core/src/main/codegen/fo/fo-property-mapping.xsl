<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!-- $Id$ -->
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
  <xsl:variable name="lcletters" select="'abcdefghijklmnopqrstuvwxyz-:'" />
  <xsl:variable name="ucletters" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ__'" />
  <xsl:variable name="enum" select="translate($prop/name, $lcletters, $ucletters)"/>
<xsl:text>    addPropertyName("</xsl:text><xsl:value-of select="$prop/name"/>", PR_<xsl:value-of select="$enum"/>);
<xsl:text>    </xsl:text><xsl:value-of select="$htname"/>[PR_<xsl:value-of select="$enum"/>] =<xsl:value-of select="$makerclass"/>.maker(PR_<xsl:value-of select="$enum"/>);
</xsl:template>


<xsl:template name="genenum">
  <xsl:param name="prop" select="."/>
  <xsl:variable name="lcletters" select="'abcdefghijklmnopqrstuvwxyz-:'" />
  <xsl:variable name="ucletters" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ__'" />
  <xsl:variable name="num" select="count(preceding-sibling::property)"/>
  <xsl:variable name="enum" select="translate($prop/name, $lcletters, $ucletters)"/>
<!--
<xsl:text>  public final static short </xsl:text><xsl:value-of select="$enum"/> = <xsl:value-of select="$num"/>;
-->
</xsl:template>


<xsl:template match="text()"/>
<xsl:template match="text()" mode="enums"/>

<xsl:template match="property-list">
package org.apache.fop.fo.properties;

import java.util.HashMap;
import java.util.Set;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.Property;
//import org.apache.fop.svg.*;

public class <xsl:value-of select="@family"/>PropertyMapping implements Constants {

  private static Property.Maker[] s_htGeneric = new Property.Maker[PROPERTY_COUNT+1];
  /* s_htElementLists not currently used; apparently for specifying element-specific
   * property makers (instead of the default maker for a particular property); see
   * former org.apache.fop.fo.PropertyListBuilder 
   */
  private static HashMap s_htElementLists = new HashMap();
  private static HashMap s_htSubPropNames = new HashMap();
  private static HashMap s_htPropNames = new HashMap();
  private static HashMap s_htPropIds = new HashMap();
  <xsl:for-each select="element-property-list">
  private static HashMap s_ht<xsl:value-of select="localname[1]"/>;</xsl:for-each>

  <xsl:apply-templates/>

  public static Property.Maker[] getGenericMappings() {
    return s_htGeneric;
  }

  public static Set getElementMappings() {
    return s_htElementLists.keySet();
  }

  public static Property.Maker[] getElementMapping(int elemName) {
    return (Property.Maker[])s_htElementLists.get(new Integer(elemName));
  }

  public static int getPropertyId(String name) {
    // check to see if base.compound or just base property
    int sepchar = name.indexOf('.');

    if (sepchar > -1) {
        Integer baseId = (Integer) s_htPropNames.get(name.substring(0, sepchar));
        if (baseId == null) {
            return -1;
        } else {
            int cmpdId = getSubPropertyId(name.substring(sepchar + 1));
            if (cmpdId == -1) {
                return -1;
            } else {
                return baseId.intValue() + cmpdId;
            }
        }
    } else {
        Integer baseId = (Integer) s_htPropNames.get(name);
        if (baseId == null)
            return -1;
        return baseId.intValue();
    }
  }

  public static int getSubPropertyId(String name) {
  	Integer i = (Integer) s_htSubPropNames.get(name);
  	if (i == null)
  		return -1;
    return i.intValue();
  }
  
  // returns a property, compound, or property.compound name
  public static String getPropertyName(int id) {
    if (((id &amp; Constants.COMPOUND_MASK) == 0) 
        || ((id &amp; Constants.PROPERTY_MASK) == 0)) {
        return (String) s_htPropIds.get(new Integer(id));
    } else {
        return (String) s_htPropIds.get(new Integer(
            id &amp; Constants.PROPERTY_MASK)) + "." + s_htPropIds.get(
            new Integer(id &amp; Constants.COMPOUND_MASK));
    }
  }

  static {
    addSubPropertyName("length", CP_LENGTH);
    addSubPropertyName("conditionality", CP_CONDITIONALITY);
    addSubPropertyName("block-progression-direction", CP_BLOCK_PROGRESSION_DIRECTION);
    addSubPropertyName("inline-progression-direction", CP_INLINE_PROGRESSION_DIRECTION);
    addSubPropertyName("within-line", CP_WITHIN_LINE);
    addSubPropertyName("within-column", CP_WITHIN_COLUMN);
    addSubPropertyName("within-page", CP_WITHIN_PAGE);
    addSubPropertyName("minimum", CP_MINIMUM);
    addSubPropertyName("maximum", CP_MAXIMUM);
    addSubPropertyName("optimum", CP_OPTIMUM);
    addSubPropertyName("precedence", CP_PRECEDENCE);
  
  }
  
  public static void addPropertyName(String name, int id) {
    s_htPropNames.put(name, new Integer(id));
    s_htPropIds.put(new Integer(id), name);
  }

  public static void addSubPropertyName(String name, int id) {
    s_htSubPropNames.put(name, new Integer(id));
    s_htPropIds.put(new Integer(id), name);
  }
}
</xsl:template>

<xsl:template match="generic-property-list">
  <xsl:apply-templates mode="enums"/>
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

<xsl:template match="property[@type='generic']" mode="enums">
  /* PROPCLASS = <xsl:call-template name="propclass"/> */
</xsl:template>

<xsl:template match="property" mode="enums">
  <xsl:param name="htname"/>
  <xsl:variable name="refname" select="name"/>
  <xsl:choose>
    <xsl:when test="@type='ref'">
      <xsl:call-template name="genenum">
        <xsl:with-param name="htname" select="$htname"/>
        <xsl:with-param name="prop"
          select='document(concat(@family, "properties.xml"))//property[name=$refname]'/>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="not(@type)">
      <xsl:call-template name="genenum">
    <xsl:with-param name="htname" select="$htname"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise/>
  </xsl:choose>
</xsl:template>

<xsl:template match="subproperty" mode="enums">
  <xsl:call-template name="genenum"/>
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


