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

<xsl:output method="text" />

<xsl:variable name="prefixVal">  
<xsl:value-of select="//elements/@prefix"/>  
</xsl:variable>  

<xsl:template name="capfirst">
  <xsl:param name="str"/>
  <xsl:variable name="lcletters" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="ucletters" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
     <xsl:value-of select="concat(translate(substring($str, 1, 1),
	            $lcletters, $ucletters), substring($str, 2))"/>
</xsl:template>

<xsl:template name="capall">
  <xsl:param name="str"/>
  <xsl:variable name="lcletters" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="ucletters" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
     <xsl:value-of select="translate($str,
              $lcletters, $ucletters)"/>
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

<xsl:template match="attributes"><xsl:apply-templates/></xsl:template>

<xsl:template match="includeAttributes">
<xsl:variable name="attr-ref">
  <xsl:value-of select="@ref"/>
</xsl:variable>
<xsl:for-each select="/elements/commonAttributes">
  <xsl:choose>
    <xsl:when test="@ref = $attr-ref">
    <xsl:apply-templates/>
    </xsl:when>
  </xsl:choose>
</xsl:for-each>
</xsl:template>

<xsl:template match="attribute">"<xsl:apply-templates/>"<xsl:if test="not(position()=last())">, </xsl:if></xsl:template>

<xsl:template match="elements">
    <xsl:apply-templates select="element"/>
</xsl:template>

<xsl:template match="tagname">
    <xsl:apply-templates/>
</xsl:template>

<xsl:template match="element">

<xsl:variable name="name">
  <xsl:apply-templates select="tagname"/>
</xsl:variable>

<xsl:variable name="classname">
  <xsl:choose>
    <xsl:when test="class-name">
      <xsl:value-of select="class-name"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="makeClassName">
        <xsl:with-param name="propstr" select="$name"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:variable>

<redirect:write select="concat('./{$prefixVal}/', $classname, '.java')">
package org.apache.fop.<xsl:value-of select="$prefixVal"/>;

import org.apache.fop.fo.*;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;

import org.w3c.dom.Element;

public class <xsl:value-of select="$classname"/> extends <xsl:call-template name="capall"><xsl:with-param name="str" select="$prefixVal"/></xsl:call-template>Obj {

    /**
     * inner class for making <xsl:apply-templates select="tagname"/> objects.
     */
    public static class Maker extends FObj.Maker {

        /**
         * make a <xsl:apply-templates select="tagname"/> object.
         *
         * @param parent the parent formatting object
         * @param propertyList the explicit properties of this object
         *
         * @return the <xsl:apply-templates select="tagname"/> object
         */
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new <xsl:value-of select="$classname"/>(parent, propertyList);
        }
    }

    /**
     * returns the maker for this object.
     *
     * @return the maker for <xsl:apply-templates select="tagname"/> objects
     */
    public static FObj.Maker maker() {
        return new <xsl:value-of select="$classname"/>.Maker();
    }

    /**
     * constructs a <xsl:apply-templates select="tagname"/> object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    protected <xsl:value-of select="$classname"/>(FObj parent, PropertyList propertyList) {
        super(parent, propertyList);
        this.name = "<xsl:value-of select="//@prefix"/>:<xsl:value-of select="$name"/>";
        tagName = "<xsl:value-of select="$name"/>";
        props = new String[] {<xsl:apply-templates select="attributes"/>};
    }

<xsl:if test="@addText">
	protected void addCharacters(char data[], int start, int length) {
		this.children.addElement(new String(data, start, length - start));
	}
</xsl:if>
}
</redirect:write>
</xsl:template>

</xsl:stylesheet>
