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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:include href="./propinc.xsl"/>

<xsl:output method="text" />

<xsl:template match="allprops">
<xsl:variable name="constlist">
  <xsl:for-each select="document(propfile)//generic-property-list
    //enumeration/value">
    <xsl:sort select="@const"/>
  <xsl:value-of select="@const"/>:</xsl:for-each>
</xsl:variable>

<xsl:variable name="propertylist">
  <xsl:for-each select="document(propfile)//generic-property-list//
    property[not(@type = 'generic')]">
    <xsl:sort select="name"/>
    <xsl:text>PR_</xsl:text>
    <xsl:call-template name="makeEnumConstant">
      <xsl:with-param name="propstr" select="name" />
    </xsl:call-template>
    <xsl:text>:</xsl:text>
  </xsl:for-each>
</xsl:variable>

<xsl:variable name="compoundpropertylist">
  <xsl:for-each select="document(propfile)//generic-property-list//
    property/compound/subproperty">
    <xsl:sort select="name"/>
    <xsl:text>CP_</xsl:text>
    <xsl:call-template name="makeEnumConstant">
      <xsl:with-param name="propstr" select="name" />
    </xsl:call-template>
    <xsl:text>:</xsl:text>
  </xsl:for-each>
</xsl:variable>

<xsl:variable name="elementlist">
  <xsl:for-each select="document(elementfile)//element">
    <xsl:sort select="name"/>
    <xsl:text>FO_</xsl:text>
    <xsl:call-template name="makeEnumConstant">
      <xsl:with-param name="propstr" select="name" />
    </xsl:call-template>
    <xsl:text>:</xsl:text>
  </xsl:for-each>
</xsl:variable>

<xsl:text>

package org.apache.fop.fo;

public interface Constants {</xsl:text>

    // element constants
<xsl:call-template name="sortconsts">
  <xsl:with-param name="consts" select="$elementlist"/>
  <xsl:with-param name="counter" select="'ELEMENT'"/>
</xsl:call-template>

    // Masks
    int COMPOUND_SHIFT = 9;
    int PROPERTY_MASK = (1 &lt;&lt; COMPOUND_SHIFT)-1;
    int COMPOUND_MASK = ~PROPERTY_MASK;
    int COMPOUND_COUNT = 11;
    
    // property constants
<xsl:call-template name="sortconsts">
  <xsl:with-param name="consts" select="$propertylist"/>
  <xsl:with-param name="counter" select="'PROPERTY'"/>
</xsl:call-template>

    // compound property constants
<xsl:call-template name="sortconsts">
  <xsl:with-param name="consts" select="$compoundpropertylist"/>
  <xsl:with-param name="suffix" select="' &lt;&lt; COMPOUND_SHIFT'"/>
</xsl:call-template>

    // Enumeration constants
<xsl:call-template name="sortconsts">
  <xsl:with-param name="consts" select="$constlist"/>
</xsl:call-template>

   // Enumeration Interfaces
   
    public interface GenericBooleanInterface {
        int TRUE =  Constants.TRUE;
        int FALSE =  Constants.FALSE;
    }
     
    public interface GenericBorderStyleInterface {
        int NONE =  Constants.NONE;
        int HIDDEN =  Constants.HIDDEN;
        int DOTTED =  Constants.DOTTED;
        int DASHED =  Constants.DASHED;
        int SOLID =  Constants.SOLID;
        int DOUBLE =  Constants.DOUBLE;
        int GROOVE =  Constants.GROOVE;
        int RIDGE =  Constants.RIDGE;
        int INSET =  Constants.INSET;
        int OUTSET =  Constants.OUTSET;
    }
    
    public interface GenericBreakInterface {
        int AUTO =  Constants.AUTO;
        int COLUMN =  Constants.COLUMN;
        int PAGE =  Constants.PAGE;
        int EVEN_PAGE =  Constants.EVEN_PAGE;
        int ODD_PAGE =  Constants.ODD_PAGE;
    }
    
    public interface GenericCondBorderWidthInterface {
        public interface Conditionality {
            int DISCARD = Constants.DISCARD;
            int RETAIN = Constants.RETAIN;
        }
    }
    
    public interface GenericCondPaddingInterface {
        public interface Conditionality {
            int DISCARD = Constants.DISCARD;
            int RETAIN = Constants.RETAIN;
        }
    }
        
    public interface GenericKeepInterface {
        public interface WithinPage {
            int AUTO = Constants.AUTO;
            int ALWAYS = Constants.ALWAYS;
        }
        public interface WithinLine {
            int AUTO = Constants.AUTO;
            int ALWAYS = Constants.ALWAYS;
        }
        public interface WithinColumn {
            int AUTO = Constants.AUTO;
            int ALWAYS = Constants.ALWAYS;
        }
    }
    
    public interface GenericSpaceInterface {
        public interface Precedence {
            int FORCE = Constants.FORCE;
        }
        public interface Conditionality {
            int DISCARD = Constants.DISCARD;
            int RETAIN = Constants.RETAIN;
        }
    }
   
<xsl:apply-templates select = "document(propfile)//property[not(@type='generic')]">
   <xsl:sort select="name"/>
</xsl:apply-templates>

<xsl:text>
}
</xsl:text>
</xsl:template>

<xsl:template match="property">
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
    <!--redirect:write select="concat($classname, '.java')"-->
      <!-- Handle enumeration values -->
      <xsl:text>
    public interface </xsl:text>
      <xsl:value-of select="$classname"/>
      <xsl:if test="use-generic">
        <xsl:text> extends </xsl:text>
        <xsl:value-of select="use-generic"/>
        <xsl:text>Interface</xsl:text>
      </xsl:if>
      <xsl:text> {</xsl:text>
      <xsl:for-each select="enumeration/value">
        <xsl:text>
        int </xsl:text>
        <xsl:value-of select="@const"/>
        <xsl:text> = Constants.</xsl:text>
        <xsl:value-of select="@const"/>
        <xsl:text>;</xsl:text>
      </xsl:for-each>
      <xsl:text> }
</xsl:text>
  </xsl:if>
</xsl:template>

<xsl:template name="sortconsts">
<xsl:param name="consts"/>
<xsl:param name="prevconst"/>
<xsl:param name="num" select="1"/>
<xsl:param name="suffix" select="''"/>
<xsl:param name="counter" select="''"/>
<xsl:variable name="cval" select="substring-before($consts,':')"/>
<xsl:choose>
  <xsl:when test="$consts = ''">
    <xsl:if test="$counter != ''">
      <xsl:text>
    int </xsl:text>
      <xsl:value-of select="$counter"/>_COUNT = <xsl:value-of select="$num - 1"/>;
    </xsl:if>
  </xsl:when>
  <xsl:when test="$cval = $prevconst">
    <xsl:call-template name="sortconsts">
      <xsl:with-param name="consts" select="substring-after($consts,concat($cval, ':'))"/>
      <xsl:with-param name="num" select="$num"/>
      <xsl:with-param name="prevconst" select="$cval"/>
      <xsl:with-param name="counter" select="$counter"/>
      <xsl:with-param name="suffix" select="$suffix"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:otherwise>
    <xsl:text>
    int </xsl:text>
    <xsl:value-of select="$cval"/>
    <xsl:text> = </xsl:text>
    <xsl:value-of select="$num"/>
    <xsl:value-of select="$suffix"/>
    <xsl:text>;</xsl:text>
    <xsl:call-template name="sortconsts">
      <xsl:with-param name="consts" select="substring-after($consts,concat($cval, ':'))"/>
      <xsl:with-param name="num" select="$num + 1"/>
      <xsl:with-param name="prevconst" select="$cval"/>
      <xsl:with-param name="counter" select="$counter"/>
      <xsl:with-param name="suffix" select="$suffix"/>
    </xsl:call-template>
  </xsl:otherwise>
</xsl:choose>
</xsl:template>
</xsl:stylesheet>
