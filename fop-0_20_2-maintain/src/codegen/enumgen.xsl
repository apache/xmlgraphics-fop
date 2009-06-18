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
                xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
                extension-element-prefixes="redirect">

<xsl:include href="./propinc.xsl"/>

<xsl:output method="text" />
<xsl:param name="reldir" select="'.'"/>

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
      <redirect:write select="concat('./', $reldir, '/', $classname, '.java')">
package org.apache.fop.fo.properties;

<!-- Handle enumeration values -->
    public interface <xsl:value-of select="$classname"/>
    <xsl:if test="use-generic and $bEnum='true'">
	extends  <xsl:value-of select="use-generic"/>.Enums
    </xsl:if>{
   <xsl:for-each select="enumeration/value">
     public final static int <xsl:value-of select="@const"/> = Constants.<xsl:value-of select="@const"/>;
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
        public final static int <xsl:value-of select="@const"/> =  Constants.<xsl:value-of select="@const"/>;
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
