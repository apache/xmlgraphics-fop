<!-- $Id$
============================================================================
                   The Apache Software License, Version 1.1
============================================================================

Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

Redistribution and use in source and binary forms, with or without modifica-
tion, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

3. The end-user documentation included with the redistribution, if any, must
   include the following acknowledgment: "This product includes software
   developed by the Apache Software Foundation (http://www.apache.org/)."
   Alternately, this acknowledgment may appear in the software itself, if
   and wherever such third-party acknowledgments normally appear.

4. The names "FOP" and "Apache Software Foundation" must not be used to
   endorse or promote products derived from this software without prior
   written permission. For written permission, please contact
   apache@apache.org.

5. Products derived from this software may not be called "Apache", nor may
   "Apache" appear in their name, without prior written permission of the
   Apache Software Foundation.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
============================================================================

This software consists of voluntary contributions made by many individuals
on behalf of the Apache Software Foundation and was originally created by
James Tauber <jtauber@jtauber.com>. For more information on the Apache
Software Foundation, please see <http://www.apache.org/>.
--> 
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

package org.apache.fop.fo.properties;

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

<xsl:text>
}
</xsl:text>
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
