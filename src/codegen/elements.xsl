<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
                extension-element-prefixes="redirect">

<xsl:output method="text" />

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

<redirect:write select="concat('@org/apache/fop@/svg/', $classname, '.java')">
package org.apache.fop.svg;

import org.apache.fop.fo.*;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;

import org.apache.batik.dom.svg.*;

import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.Element;

public class <xsl:value-of select="$classname"/> extends SVGObj {

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
