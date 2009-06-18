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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >

<xsl:key name="genericref" match="property[@type='generic']" use="class-name"/>
<xsl:key name="shorthandref" match="property" use="name"/>

<xsl:template name="capfirst">
  <xsl:param name="str"/>
  <xsl:variable name="lcletters" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="ucletters" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
     <xsl:value-of select="concat(translate(substring($str, 1, 1),
	            $lcletters, $ucletters), substring($str, 2))"/>
</xsl:template>

<xsl:template name="makeClassName">
  <xsl:param name="propstr"/>
  <xsl:choose>
   <xsl:when test="contains($propstr, '-')">
    <xsl:call-template name="capfirst">
      <xsl:with-param name="str" select="substring-before($propstr, '-')"/>
    </xsl:call-template>
    <xsl:call-template name="makeClassName">
      <xsl:with-param name="propstr" select="substring-after($propstr, '-')"/>
    </xsl:call-template>
   </xsl:when>
   <xsl:otherwise>
    <xsl:call-template name="capfirst">
      <xsl:with-param name="str" select="$propstr"/>
    </xsl:call-template>
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- The name of the subclass of Property to be created -->
<xsl:template name="propclass">
  <xsl:param name="prop" select="."/>
  <xsl:choose>
    <xsl:when test="$prop/datatype">
      <xsl:value-of select="$prop/datatype"/><xsl:text>Property</xsl:text>
    </xsl:when>
    <xsl:when test="$prop/use-generic[@ispropclass='true']">
      <xsl:value-of select="$prop/use-generic"/>
    </xsl:when>
    <xsl:when test="$prop/use-generic">
      <!-- If no datatype child, then the prop must use the same datatype as
           its template. -->
	<xsl:call-template name="propclass">
	  <xsl:with-param name="prop"
	   select="key('genericref', $prop/use-generic)"/>
        </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <!-- ERROR -->
      <xsl:message terminate="yes">
	No datatype found for property: <xsl:value-of select="$prop/name"/>
      </xsl:message>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- return a boolean value -->
<xsl:template name="hasEnum">
  <xsl:param name="prop" select="."/>
  <xsl:choose>
    <xsl:when test="$prop/enumeration">true</xsl:when>
    <xsl:when test="$prop/use-generic">
      <!-- If no datatype child, then the prop must use the same datatype as
           its template. -->
	<xsl:call-template name="hasEnum">
	  <xsl:with-param name="prop"
	   select="key('genericref', $prop/use-generic)"/>
        </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>false</xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- return a boolean value -->
<xsl:template name="hasSubpropEnum">
  <xsl:param name="prop" select="."/>
  <xsl:choose>
    <xsl:when test="$prop/compound/subproperty/enumeration">true</xsl:when>
    <xsl:when test="$prop/use-generic">
      <!-- If no datatype child, then the prop must use the same datatype as
           its template. -->
	<xsl:call-template name="hasSubpropEnum">
	  <xsl:with-param name="prop"
	   select="key('genericref', $prop/use-generic)"/>
        </xsl:call-template>
    </xsl:when>
    <xsl:when test="$prop/compound/subproperty/use-generic">
	<xsl:for-each select="$prop/compound/subproperty[use-generic]">
	  <xsl:call-template name="hasEnum">
	    <xsl:with-param name="prop"
	   	select="key('genericref', use-generic)"/>
          </xsl:call-template>
	</xsl:for-each>
    </xsl:when>
    <xsl:otherwise>false</xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
