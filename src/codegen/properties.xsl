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

<xsl:template match="extfile">
<!--<xsl:message>Do <xsl:value-of select="@href"/></xsl:message>-->
   <xsl:apply-templates select="document(@href)/*"/>
</xsl:template>

<!-- Content of element is code to calculate the base length -->
<xsl:template match="percent-ok">
    /** Return object used to calculate base Length
     * for percent specifications.
     */
    public PercentBase getPercentBase(final FObj fo, final PropertyList propertyList) {
     <xsl:choose>
       <xsl:when test="@base">
	return new LengthBase(fo, propertyList, LengthBase.<xsl:value-of select="@base"/>);
       </xsl:when>
       <xsl:otherwise>
	return (new LengthBase(fo, propertyList, LengthBase.CUSTOM_BASE ) {
 	  public int getBaseLength() {
	    return (<xsl:value-of select="."/>);
          }
	});
      </xsl:otherwise>
    </xsl:choose>
    }
</xsl:template>

<!-- Look for "auto" length keyword -->
<xsl:template match="auto-ok">
    protected boolean isAutoLengthAllowed() {
      return true;
    }
</xsl:template>

<xsl:template match="enumeration">
    public Property checkEnumValues(String value) {
    <xsl:for-each select="value">
      <xsl:call-template name="enumvals">
          <xsl:with-param name="specvals" select="concat(.,' ')"/>
      </xsl:call-template>
    </xsl:for-each>
	return super.checkEnumValues(value);
    }
</xsl:template>


<!-- Look for keyword equivalents. Value is the new expression  -->
<xsl:template match="keyword-equiv[1]">
    // Initialize hashtable of keywords
    static Hashtable s_htKeywords;
    static {
	s_htKeywords = new Hashtable(<xsl:value-of select="count(../keyword-equiv)"/>);
  <xsl:for-each select="../keyword-equiv">
      	s_htKeywords.put("<xsl:value-of select="@match"/>", "<xsl:value-of select="."/>");
  </xsl:for-each>
    }
    protected String checkValueKeywords(String keyword) {
      String value = (String)s_htKeywords.get(keyword);
      if (value == null) {
	return super.checkValueKeywords(keyword);
      }
      else return value;
    }
</xsl:template>

<xsl:template match="keyword-equiv[position()>1]"/>

<!-- Generate code to convert from other datatypes to property datatype -->
<xsl:template match='datatype-conversion[1]'>
  <xsl:variable name="propclass">
    <xsl:choose>
      <xsl:when test="../compound">
        <xsl:call-template name="propclass">
          <xsl:with-param name="prop"
		select="../compound/subproperty[@set-by-shorthand]"/>
        </xsl:call-template>
      </xsl:when><xsl:otherwise>
        <xsl:call-template name="propclass">
          <xsl:with-param name="prop" select=".."/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
    // See if other value types are acceptable
    protected Property convertPropertyDatatype(Property p,
	PropertyList propertyList, FObj fo) {
      <xsl:for-each select="../datatype-conversion">
      {
        <xsl:variable name="dtc">
          <xsl:choose>
	    <xsl:when test="@vartype">
	       <xsl:value-of select="@vartype"/>
	    </xsl:when><xsl:otherwise>
	      <xsl:value-of select="@from-type"/>
	    </xsl:otherwise>
	  </xsl:choose>
	</xsl:variable>
	<xsl:value-of select="$dtc"/><xsl:text> </xsl:text> <xsl:value-of select="@varname"/> =
		p.get<xsl:value-of select="@from-type"/>();
	if (<xsl:value-of select="@varname"/> != null) {
	    return new <xsl:value-of select="$propclass"/>(
		    <xsl:value-of select='.'/>);
        }
      }
      </xsl:for-each>
      return super.convertPropertyDatatype(p, propertyList, fo);
    }
</xsl:template>

<xsl:template match="datatype-conversion[position()>1]"/>

<!-- generate getDefaultForXXX for property components -->
<xsl:template match="default[@subproperty]" priority="2">
      <xsl:variable name="spname">
	<xsl:call-template name="makeClassName">
	  <xsl:with-param name="propstr" select="@subproperty"/>
	</xsl:call-template>
      </xsl:variable>
    protected String getDefaultFor<xsl:value-of  select='$spname'/>() {
	return "<xsl:value-of  select='.'/>";
    }
</xsl:template>

<!-- generate default "make" method for non-compound properties -->
<xsl:template match="default[not(../compound)]" priority="1">
  <xsl:if test='not(@contextdep = "true")'>
    private Property m_defaultProp=null;
  </xsl:if>
    public Property make(PropertyList propertyList) throws FOPException {
      <xsl:choose><xsl:when test='@contextdep="true"'>
        return make(propertyList, "<xsl:value-of select='.'/>", propertyList.getParentFObj());
	</xsl:when><xsl:otherwise>
        if (m_defaultProp == null) {
            m_defaultProp=make(propertyList, "<xsl:value-of select='.'/>", propertyList.getParentFObj());
	}
        return m_defaultProp;
	</xsl:otherwise></xsl:choose>
    }
</xsl:template>

<xsl:template match="text()"/>

<!-- Ignore properties which reference others. Only for mapping! -->
<xsl:template match="property[@type='ref']"/>

<!-- Only if more explicit rules not matched (ref) -->
<xsl:template match="property">

<!-- Only create a specific class for those properties not based on
     template (generic) property definitions or which extends a
     generic definition.
 -->
<xsl:if test='not(use-generic) or count(*)>2'>

<xsl:variable name="eclassname">
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

<xsl:variable name="classname">
     <xsl:value-of select="$eclassname"/><xsl:if test="not(@type='generic')">Maker</xsl:if>
</xsl:variable>

<!-- The class of the Property object to be created -->
<xsl:variable name="propclass">
  <xsl:call-template name="propclass"/>
</xsl:variable>

<!-- The superclass for this PropertyMaker -->
<xsl:variable name="superclass">
  <xsl:choose>
  <xsl:when test="use-generic[@ispropclass='true']">
    <xsl:value-of select="use-generic"/><xsl:text>.Maker</xsl:text>
  </xsl:when>
  <xsl:when test="use-generic">
    <xsl:value-of select="use-generic"/>
  </xsl:when>
  <xsl:otherwise>
    <xsl:value-of select="datatype"/><xsl:text>Property.Maker</xsl:text>
  </xsl:otherwise>
  </xsl:choose>
</xsl:variable>

<!-- Is this property an Enum or derived from a generic Enum -->
<xsl:variable name="enumconst">
  <xsl:if test="enumeration/value and not(@type='generic')">
  <xsl:text> implements </xsl:text><xsl:value-of select="$eclassname"/></xsl:if>
</xsl:variable>

<redirect:write select="concat('./', $reldir, '/', $classname, '.java')">
package org.apache.fop.fo.properties;

import java.util.Hashtable;
import org.apache.fop.datatypes.*;
import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.messaging.MessageHandler;

public class <xsl:value-of select="$classname"/> extends  <xsl:value-of select="$superclass"/><xsl:value-of select="$enumconst"/> {

<!-- If has enumerated values and is a generic class, create a nested
  interface defining the enumeration constants -->
<xsl:if test=".//enumeration and @type='generic'">
  public interface Enums {
<xsl:for-each select="enumeration/value">
    int <xsl:value-of select="@const"/> =  Constants.<xsl:value-of select="@const"/>;
</xsl:for-each>
<xsl:for-each select="compound/subproperty[enumeration]">
       <xsl:variable name="spname">
	<xsl:call-template name="makeClassName">
          <xsl:with-param name="propstr" select="name"/>
        </xsl:call-template>
      </xsl:variable>
  public interface <xsl:value-of select="$spname"/> {
<xsl:for-each select="enumeration/value">
    int <xsl:value-of select="@const"/> =  Constants.<xsl:value-of select="@const"/>;
</xsl:for-each>
  }
</xsl:for-each>
  }
</xsl:if>


<!-- Handle enumeration values -->
<xsl:for-each select="enumeration/value">
  protected final static EnumProperty s_prop<xsl:value-of select="@const"/> = new EnumProperty(<xsl:if test="../../@type='generic'">Enums.</xsl:if><xsl:value-of select="@const"/>);
</xsl:for-each>


<!-- Look for compound properties -->
<xsl:if test="compound">
      <xsl:variable name="enumclass">
      <xsl:choose>
        <xsl:when test="@type='generic'">Enums</xsl:when>
        <xsl:otherwise><xsl:value-of select="$eclassname"/></xsl:otherwise>
      </xsl:choose>
      </xsl:variable>
    <xsl:for-each select="compound/subproperty">
      <xsl:variable name="spname">
	<xsl:call-template name="makeClassName">
          <xsl:with-param name="propstr" select="name"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="sp_superclass">
        <xsl:choose>
          <xsl:when test="use-generic">
            <xsl:value-of select="use-generic"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="datatype"/><xsl:text>Property.Maker</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:choose>
        <xsl:when test='*[local-name(.)!="name" and local-name(.)!="datatype" and local-name(.)!="use-generic" and local-name(.)!="default"]'>
    static private class SP_<xsl:value-of select="$spname"/>Maker
	extends <xsl:value-of select="$sp_superclass"/>
<xsl:if test="enumeration"> implements <xsl:value-of select="$enumclass"/>.<xsl:value-of select="$spname"/></xsl:if> {
	SP_<xsl:value-of select="$spname"/>Maker(String sPropName) {
	     super(sPropName);
        }
<xsl:for-each select="enumeration/value">
       protected final static EnumProperty s_prop<xsl:value-of select="@const"/> = new EnumProperty(<xsl:value-of select="@const"/>);
</xsl:for-each>

	<xsl:apply-templates select="percent-ok|auto-ok|keyword-equiv|datatype-conversion|enumeration"/>
    }
    final private static Property.Maker s_<xsl:value-of select="$spname"/>Maker =
	new SP_<xsl:value-of select="$spname"/>Maker(
	     "<xsl:value-of select='../../name'/>.<xsl:value-of select='name'/>");
        </xsl:when>
        <xsl:otherwise>
    final private static Property.Maker s_<xsl:value-of select="$spname"/>Maker =
	new <xsl:value-of select="$sp_superclass"/>(
	     "<xsl:value-of select='../../name'/>.<xsl:value-of select='name'/>");
	</xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
</xsl:if>

    static public Property.Maker maker(String propName) {
	return new <xsl:value-of select="$classname"/>(propName);
    }

    protected <xsl:value-of select="$classname"/>(String name) {
	super(name);
<xsl:if test="compound">
        m_shorthandMaker= getSubpropMaker("<xsl:value-of select='compound/subproperty[@set-by-shorthand="true"]/name'/>");
</xsl:if>
    }

<xsl:if test="compound">
    Property.Maker m_shorthandMaker;

    public Property checkEnumValues(String value) {
	return m_shorthandMaker.checkEnumValues(value);
    }

    protected boolean isCompoundMaker() {
      return true;
    }

    protected Property.Maker getSubpropMaker(String subprop) {
    <xsl:for-each select="compound/subproperty">
      <xsl:variable name="spname">
	<xsl:call-template name="makeClassName">
          <xsl:with-param name="propstr" select="name"/>
        </xsl:call-template>
      </xsl:variable>
	if (subprop.equals("<xsl:value-of select='name'/>"))
	  return s_<xsl:value-of select="$spname"/>Maker;
    </xsl:for-each>
	return super.getSubpropMaker(subprop);
    }

    protected Property setSubprop(Property baseProp, String subpropName,
	Property subProp) {
        <xsl:value-of select="datatype"/> val = baseProp.get<xsl:value-of select="datatype"/>();
	// Do some type checking???
	// Check if one of our subproperties???
	val.setComponent(subpropName, subProp, false);
	return baseProp;
    }

    public Property getSubpropValue(Property baseProp, String subpropName) {
      <xsl:value-of select="datatype"/> val = baseProp.get<xsl:value-of select="datatype"/>();
      return val.getComponent(subpropName);
    }
<xsl:choose>
<!-- some subproperty default is context dependent; don't cache default! -->
<xsl:when test='.//default[@contextdep="true"]'>
    public Property make(PropertyList propertyList) throws FOPException {
        return makeCompound(propertyList, propertyList.getParentFObj());
    }
</xsl:when>
<xsl:otherwise>
    private Property m_defaultProp=null;
    public Property make(PropertyList propertyList) throws FOPException {
        if (m_defaultProp == null) {
            m_defaultProp=makeCompound(propertyList, propertyList.getParentFObj());
	}
        return m_defaultProp;
    }
</xsl:otherwise>
</xsl:choose>

    protected Property makeCompound(PropertyList pList, FObj fo) throws FOPException {
	<xsl:value-of select="datatype"/> p = new <xsl:value-of select="datatype"/>();
	Property subProp;
    <xsl:for-each select="compound/subproperty/name">
      <xsl:variable name="spname">
	<xsl:call-template name="makeClassName">
	  <xsl:with-param name="propstr" select="."/>
	</xsl:call-template>
      </xsl:variable>
	 // set default for subprop <xsl:value-of select="."/>
	 subProp = getSubpropMaker("<xsl:value-of select='.'/>").make(pList,
	  getDefaultFor<xsl:value-of select='$spname'/>(), fo);
	  p.setComponent("<xsl:value-of select='.'/>", subProp, true);
    </xsl:for-each>
	return new <xsl:value-of select="$propclass"/>(p);
    }

    <!-- generate a "getDefaultForXXX" for each subproperty XXX -->
    <xsl:for-each select="compound/subproperty">
      <xsl:variable name="spname">
	<xsl:call-template name="makeClassName">
	  <xsl:with-param name="propstr" select="name"/>
	</xsl:call-template>
      </xsl:variable>
    protected String getDefaultFor<xsl:value-of  select='$spname'/>() {
      <xsl:choose><xsl:when test="default">
	return "<xsl:value-of  select='default'/>";
        </xsl:when><xsl:when test="use-generic and key('genericref', use-generic)/default">
	return "<xsl:value-of select='key(&apos;genericref&apos;, use-generic)/default'/>";
        </xsl:when><xsl:otherwise>
	return "";
	</xsl:otherwise>
      </xsl:choose>
    }
    </xsl:for-each>

    /** Set the appropriate components when the "base" property is set. */
    public Property convertProperty(Property p, PropertyList pList,FObj fo)
	throws FOPException
    {
        if (p instanceof <xsl:value-of select="$propclass"/>) return p;
	if (! (p instanceof EnumProperty)) {
	  // delegate to the subprop maker to do conversions
	  p = m_shorthandMaker.convertProperty(p,pList,fo);
        }
	if (p != null) {
	  Property prop = makeCompound(pList, fo);

	  <xsl:value-of select="datatype"/> pval = prop.get<xsl:value-of select="datatype"/>();
<xsl:for-each select="compound/subproperty[@set-by-shorthand='true']">
	  pval.setComponent("<xsl:value-of select='name'/>", p, false);
</xsl:for-each>
          return prop;
        }
	else return null;
<!--
        else {
	  // throw some kind of exception!
	  throw new FOPException("Can't convert value to <xsl:value-of select='$spdt'/> type");
        }
-->
    }

</xsl:if> <!-- property/compound -->

<xsl:if test="inherited">
   public boolean isInherited() { return <xsl:value-of select="inherited"/>; }
</xsl:if>

<!-- the default implementation returns false -->
<xsl:if test='inherited/@type="specified"'>
   public boolean inheritsSpecified() {
	 return true;
   }
</xsl:if>


<!-- Currently only works for Enum values -->
<xsl:if test="derive">
    public Property compute(PropertyList propertyList) {
      Property computedProperty = null;
      Property correspondingProperty = propertyList.get("<xsl:value-of select="derive/@from"/>");
      if (correspondingProperty != null) {
        int correspondingValue = correspondingProperty.getEnum();
        <xsl:for-each select="derive/if">
        if (correspondingValue == <xsl:value-of select="@match"/>)
          computedProperty = new EnumProperty(<xsl:value-of select="."/>);
        else</xsl:for-each>
        ;
      }
      return computedProperty;
    }
</xsl:if>

<!-- If any corresponding element at property or subproperty levels -->
<xsl:if test=".//corresponding">
    <xsl:if test=".//corresponding/@use-if-specified='true'">
    public boolean isCorrespondingForced(PropertyList propertyList) {
      StringBuffer sbExpr=new StringBuffer();
      <xsl:for-each select=".//corresponding/propval">
      sbExpr.setLength(0);
      <xsl:apply-templates select="."/>
      if (propertyList.getExplicit(sbExpr.toString()) != null) return true;
      </xsl:for-each>
      return false;
    }
    </xsl:if>

    public Property compute(PropertyList propertyList) throws FOPException {
      FObj parentFO = propertyList.getParentFObj();
      StringBuffer sbExpr=new StringBuffer();
      Property p=null;
      <xsl:choose><xsl:when test="corresponding/propexpr">
      <xsl:apply-templates select="corresponding/propval"/>
	// Make sure the property is set before calculating it!
      if (propertyList.getExplicitOrShorthand(sbExpr.toString()) == null) return p;
      sbExpr.setLength(0);
      <xsl:apply-templates select="corresponding/propexpr"/>
      p= make(propertyList, sbExpr.toString(), propertyList.getParentFObj());
      </xsl:when><xsl:otherwise>
      <xsl:apply-templates select="corresponding/propval"/>
      p= propertyList.getExplicitOrShorthand(sbExpr.toString());
      </xsl:otherwise></xsl:choose>
      if (p != null) {
          p = convertProperty(p, propertyList, parentFO );
      }
      <xsl:if test="compound">
      else p= makeCompound(propertyList, parentFO);

      Property subprop;
      <xsl:for-each select="compound/subproperty/corresponding">
      sbExpr.setLength(0);
      <xsl:choose><xsl:when test="propexpr">
      <xsl:apply-templates select="propexpr"/>
      subprop= getSubpropMaker("<xsl:value-of select='../name'/>").
      		make(propertyList, sbExpr.toString(), parentFO);
      </xsl:when><xsl:otherwise>
      <xsl:apply-templates select="propval"/>
      subprop= propertyList.getExplicitOrShorthand(sbExpr.toString());
      </xsl:otherwise></xsl:choose>
      if (subprop != null) {
        setSubprop(p, "<xsl:value-of select='../name'/>", subprop);
      }
      </xsl:for-each>
      </xsl:if>
      return p;
    }
</xsl:if>

<!-- If can be specified by any shorthand property -->
<xsl:if test="shorthand">
    public Property getShorthand(PropertyList propertyList) {
      Property p = null;
      ListProperty listprop;
      <xsl:for-each select="shorthand">
	<xsl:variable name="shprop" select="."/>
      if (p == null) {
         listprop = (ListProperty)propertyList.getExplicit("<xsl:value-of select='$shprop'/>");
         if (listprop != null) {
           // Get a parser for the shorthand to set the individual properties
           ShorthandParser shparser = new <xsl:value-of select="key('shorthandref', $shprop)/datatype-parser"/>(listprop);
             p = shparser.getValueForProperty(getPropName(), this, propertyList);
         }
      }
      </xsl:for-each>
      return p;
    }
</xsl:if>

<xsl:apply-templates select="percent-ok|auto-ok|default|keyword-equiv|datatype-conversion|enumeration|extfile"/>
}
</redirect:write>
</xsl:if> <!-- need to create a class -->
</xsl:template>

<xsl:template match="corresponding/propexpr/propval">
   sbExpr.append("_fop-property-value("); <!-- Note: interpreted by property parser -->
   <xsl:apply-templates/>
   sbExpr.append(")");
</xsl:template>


<xsl:template match="corresponding//text()">
   <xsl:variable name="tval" select='normalize-space(.)'/>
   <xsl:if test="$tval != ''">sbExpr.append("<xsl:value-of select='$tval'/>");</xsl:if>
</xsl:template>

<xsl:template match="propval/wmrel2abs">
   sbExpr.append(propertyList.wmRelToAbs(PropertyList.<xsl:value-of select="@dir"/>));
</xsl:template>

<xsl:template match="propval/wmabs2rel">
   sbExpr.append(propertyList.wmAbsToRel(PropertyList.<xsl:value-of select="@dir"/>));
</xsl:template>

<!-- avoid unwanted output to placeholder file -->
<xsl:template match="localname"/>

<!-- Check that each member of the nodeset dtlist has the same value.
     Print a message if any member of dtlist is different
     from the first member. Return the first member.
 -->
<xsl:template name="check-subprop-datatype">
    <xsl:param name="dtlist"/>
    <xsl:variable name="dt"><xsl:value-of select='$dtlist[1]'/></xsl:variable>
    <xsl:for-each select="$dtlist">
	<xsl:if test=". != $dt">
	    <xsl:message>
	        <xsl:text>Conflict between subproperty datatypes: </xsl:text>
		<xsl:value-of select='.'/> != <xsl:value-of select='$dt'/>
	    </xsl:message>
	</xsl:if>
    </xsl:for-each>
    <xsl:value-of select='$dt'/>
</xsl:template>

<!-- If the value of an enumeration constant contains two or more words,
     separated by a blank, map all of these words to the same constant.
-->
<xsl:template name="enumvals">
   <xsl:param name="specvals"/>
   <xsl:if test='string-length($specvals)>0'>
   <xsl:variable name="oneval" select="substring-before($specvals, ' ')"/>
      if (value.equals("<xsl:value-of select="$oneval"/>")) { return s_prop<xsl:value-of select="@const"/>; }
    <xsl:call-template name="enumvals">
       <xsl:with-param name="specvals" select="substring-after($specvals, ' ')"/>
    </xsl:call-template>
    </xsl:if>
</xsl:template>

</xsl:stylesheet>
