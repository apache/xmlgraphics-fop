<!--
$Id$
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


