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

<xsl:template match="extfile">
<!--<xsl:message>Do <xsl:value-of select="@href"/></xsl:message>-->
   <xsl:apply-templates select="document(@href)/*"/>
</xsl:template>

<!-- Content of element is code to calculate the base length -->
<xsl:template match="percent-ok">
  <xsl:text>
    /** Return object used to calculate base Length
     * for percent specifications.
     */
    public PercentBase getPercentBase(final FObj fo, final PropertyList propertyList) {</xsl:text>
  <xsl:choose>
    <xsl:when test="@base">
      <xsl:text>
        return new LengthBase(fo, propertyList, LengthBase.</xsl:text>
        <xsl:value-of select="@base"/><xsl:text>);
</xsl:text>
     </xsl:when>
     <xsl:otherwise>
       <!-- I have no idea what's happening here.  Is this supposed to be
            an anonymous class returning the actual base length?
            -->
       <xsl:text>
        return new LengthBase(fo, propertyList, LengthBase.CUSTOM_BASE) {
     public int getBaseLength() {
      return (</xsl:text>
         <xsl:value-of select="."/>
         <xsl:text>);
          }
  });</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
     <xsl:text>
    }</xsl:text>
</xsl:template>

<!-- Look for "auto" length keyword -->
<xsl:template match="auto-ok">
  <xsl:text>
    protected boolean isAutoLengthAllowed() {
        return true;
    }</xsl:text>
</xsl:template>

<xsl:template match="enumeration">
  <xsl:text>
    public Property checkEnumValues(String value) {</xsl:text>
    <xsl:for-each select="value">
      <xsl:call-template name="enumvals">
        <xsl:with-param name="specvals" select="concat(.,' ')"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:text>
        return super.checkEnumValues(value);
    }</xsl:text>
</xsl:template>


<!-- Look for keyword equivalents. Value is the new expression  -->
<xsl:template match="keyword-equiv[1]">
  <xsl:text>
    // Initialize hashtable of keywords
    static HashMap s_htKeywords;
    static {
        s_htKeywords = new HashMap(</xsl:text>
    <xsl:value-of select="count(../keyword-equiv)"/>
    <xsl:text>);</xsl:text>
  <xsl:for-each select="../keyword-equiv">
    <xsl:text>
        s_htKeywords.put("</xsl:text>
      <xsl:value-of select="@match"/>
      <xsl:text>", "</xsl:text>
      <xsl:value-of select="."/>
      <xsl:text>");</xsl:text>
    </xsl:for-each>
    <xsl:text>
    }

    protected String checkValueKeywords(String keyword) {
        String value = (String)s_htKeywords.get(keyword);
        if (value == null) {
            return super.checkValueKeywords(keyword);
        }
        else return value;
    }</xsl:text>
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
<xsl:text>

    // See if other value types are acceptable
    protected Property convertPropertyDatatype(
                  Property p, PropertyList propertyList, FObj fo) {</xsl:text>
  <xsl:for-each select="../datatype-conversion">
    <xsl:variable name="dtc">
      <xsl:choose>
        <xsl:when test="@vartype">
          <xsl:value-of select="@vartype"/>
        </xsl:when><xsl:otherwise>
        <xsl:value-of select="@from-type"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:text>
        </xsl:text>
  <xsl:value-of select="$dtc"/>
  <xsl:text> </xsl:text>
  <xsl:value-of select="@varname"/>
  <xsl:text> = p.get</xsl:text>
  <xsl:value-of select="@from-type"/>
  <xsl:text>();
        if (</xsl:text>
      <xsl:value-of select="@varname"/>
      <xsl:text> != null) {
            return new </xsl:text>
      <xsl:value-of select="$propclass"/><xsl:text>(
                    </xsl:text>
        <xsl:value-of select='normalize-space(.)'/><xsl:text>);
        }</xsl:text>
      </xsl:for-each>
      <xsl:text>
        return super.convertPropertyDatatype(p, propertyList, fo);
    }</xsl:text>
</xsl:template>

<xsl:template match="datatype-conversion[position()>1]"/>

<!-- generate getDefaultForXXX for property components -->
<xsl:template match="default[@subproperty]" priority="2">
  <xsl:variable name="spname">
    <xsl:call-template name="makeClassName">
      <xsl:with-param name="propstr" select="@subproperty"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:text>
    protected String getDefaultFor</xsl:text>
    <xsl:value-of  select='$spname'/>
    <xsl:text>() {
        return "</xsl:text>
    <xsl:value-of  select='.'/>
    <xsl:text>";
    }</xsl:text>
</xsl:template>

<!-- generate default "make" method for non-compound properties -->
<xsl:template match="default[not(../compound)]" priority="1">
  <xsl:if test='not(@contextdep = "true")'>
    <xsl:text>
    private Property m_defaultProp=null;</xsl:text>
  </xsl:if>
  <xsl:text>
    public Property make(PropertyList propertyList) throws FOPException {</xsl:text>
    <xsl:choose>
      <xsl:when test='@contextdep="true"'>
        <xsl:text>
        return make(propertyList, "</xsl:text>
          <xsl:value-of select='.'/>
          <xsl:text>", propertyList.getParentFObj());</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>
        if (m_defaultProp == null) {
            m_defaultProp=make(propertyList, "</xsl:text>
            <xsl:value-of select='.'/>
            <xsl:text>", propertyList.getParentFObj());
        }
        return m_defaultProp;</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text>
    }</xsl:text>
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
      <xsl:value-of select="$eclassname"/>
      <xsl:if test="not(@type='generic')">
        <xsl:text>Maker</xsl:text>
      </xsl:if>
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

    <redirect:write select="concat($classname, '.java')">
      <xsl:text>package org.apache.fop.fo.properties;
      
</xsl:text>
      <xsl:if test=".//keyword-equiv or ./name[.='generic-color']">
        <xsl:text>
import java.util.HashMap;</xsl:text>
      </xsl:if>
      <xsl:if test=
          "((./datatype and
              (not (./datatype[.='String' or .='Enum' or .='List'
                      or .='Length' or .='Character' or .='Number'])
                    or (./datatype[.='Length'] and ./percent-ok)
            ) )
            or .//datatype-conversion)
            and ./name[. !='generic-color']">
        <xsl:text>
import org.apache.fop.datatypes.*;</xsl:text>
      </xsl:if>
      <xsl:text>
import org.apache.fop.fo.*;</xsl:text>
      <xsl:if test="not(
                    (./datatype and ./datatype[. ='List'])
                    or ./class-name[.='GenericCondPadding']
                    or ./name[.='generic-boolean'
                              or .='generic-color'])">
        <xsl:text>
import org.apache.fop.apps.FOPException;</xsl:text>
      </xsl:if>
  <xsl:if test=".//enumeration and @type='generic'">
    <xsl:text>
import org.apache.fop.fo.Constants;</xsl:text>
</xsl:if>
      <xsl:text>

public class </xsl:text>
      <xsl:value-of select="$classname"/>
      <xsl:text> extends </xsl:text>
      <xsl:value-of select="$superclass"/><xsl:value-of select="$enumconst"/>
      <xsl:text> {</xsl:text>

      <!-- If has enumerated values and is a generic class, create a nested
           interface defining the enumeration constants -->
      <xsl:if test=".//enumeration and @type='generic'">
        <xsl:text>
    public interface Enums {</xsl:text>
        <xsl:for-each select="enumeration/value">
          <xsl:text>
        int </xsl:text>
          <xsl:value-of select="@const"/>
          <xsl:text> =  Constants.</xsl:text>
          <xsl:value-of select="@const"/>
          <xsl:text>;</xsl:text>
        </xsl:for-each>
        <xsl:for-each select="compound/subproperty[enumeration]">
          <xsl:variable name="spname">
            <xsl:call-template name="makeClassName">
              <xsl:with-param name="propstr" select="name"/>
            </xsl:call-template>
          </xsl:variable>
          <xsl:text>
        public interface </xsl:text>
          <xsl:value-of select="$spname"/>
          <xsl:text> {</xsl:text>
          <xsl:for-each select="enumeration/value">
            <xsl:text>
            int </xsl:text>
            <xsl:value-of select="@const"/>
            <xsl:text> = Constants.</xsl:text>
            <xsl:value-of select="@const"/>
            <xsl:text>;</xsl:text>
          </xsl:for-each>
          <xsl:text>
        }</xsl:text>
        </xsl:for-each>
        <xsl:text>
    }</xsl:text>
      </xsl:if>


      <!-- Handle enumeration values -->
      <xsl:for-each select="enumeration/value">
        <xsl:text>
    protected final static EnumProperty s_prop</xsl:text>
        <xsl:value-of select="@const"/>
        <xsl:text> = new EnumProperty(</xsl:text>
        <xsl:if test="../../@type='generic'">
          <xsl:text>Enums.</xsl:text>
        </xsl:if>
        <xsl:value-of select="@const"/>
        <xsl:text>);</xsl:text>
      </xsl:for-each>


      <!-- Look for compound properties -->
      <xsl:if test="compound">
        <xsl:variable name="enumclass">
          <xsl:choose>
            <xsl:when test="@type='generic'">
              <xsl:text>Enums</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$eclassname"/>
            </xsl:otherwise>
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
                <xsl:value-of select="datatype"/>
                <xsl:text>Property.Maker</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          
          <xsl:choose>
            <xsl:when test='*[local-name(.)!="name" and local-name(.)!="datatype" and local-name(.)!="use-generic" and local-name(.)!="default"]'>
              <xsl:text>
    static private class SP_</xsl:text>
              <xsl:value-of select="$spname"/>
              <xsl:text>Maker extends </xsl:text>
              <xsl:value-of select="$sp_superclass"/>
              <xsl:if test="enumeration">
                <xsl:text> implements </xsl:text>
                <xsl:value-of select="$enumclass"/>
                <xsl:text>.</xsl:text>
                <xsl:value-of select="$spname"/>
              </xsl:if>
              <xsl:text> {
        SP_</xsl:text>
              <xsl:value-of select="$spname"/>
              <xsl:text>Maker(int sPropName) {
            super(sPropName);
        }</xsl:text>
              <xsl:for-each select="enumeration/value">
                <xsl:text>
        protected final static EnumProperty s_prop</xsl:text>
                <xsl:value-of select="@const"/>
                <xsl:text> = new EnumProperty(</xsl:text>
                <xsl:value-of select="@const"/>
                <xsl:text>);</xsl:text>
              </xsl:for-each>

              <xsl:apply-templates
                select="percent-ok|auto-ok|keyword-equiv|
                        datatype-conversion|enumeration"/>
              <xsl:text>
    }

    final private static Property.Maker s_</xsl:text>
              <xsl:value-of select="$spname"/>
              <xsl:text>Maker =
              new SP_</xsl:text><xsl:value-of select="$spname"/>
              <xsl:text>Maker(</xsl:text>
              <xsl:if test="not(../../@type = 'generic')">
                <xsl:text>Constants.PR_</xsl:text>
                <xsl:call-template name="makeEnumConstant">
                  <xsl:with-param name="propstr" select="../../name"/>
                </xsl:call-template>
                <xsl:text> | </xsl:text>
              </xsl:if>
              <xsl:text>Constants.CP_</xsl:text>
              <xsl:call-template name="makeEnumConstant">
                <xsl:with-param name="propstr" select="name"/>
              </xsl:call-template>
              <xsl:text>);</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>
    final private static Property.Maker s_</xsl:text>
              <xsl:value-of select="$spname"/>
              <xsl:text>Maker =
              new </xsl:text><xsl:value-of select="$sp_superclass"/>
              <xsl:text>(</xsl:text>
              <xsl:if test="not(../../@type = 'generic')">
                <xsl:text>Constants.PR_</xsl:text>
                <xsl:call-template name="makeEnumConstant">
                  <xsl:with-param name="propstr" select="../../name"/>
                </xsl:call-template>
                <xsl:text> | </xsl:text>
              </xsl:if>
              <xsl:text>Constants.CP_</xsl:text>
              <xsl:call-template name="makeEnumConstant">
                <xsl:with-param name="propstr" select="name"/>
              </xsl:call-template>
              <xsl:text>);</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:if>

      <xsl:text>

    static public Property.Maker maker(int propertyId) {
        return new </xsl:text>
      <xsl:value-of select="$classname"/>
      <xsl:text>(propertyId);
    }

    protected </xsl:text>
      <xsl:value-of select="$classname"/>
      <xsl:text>(int propId) {
        super(propId);</xsl:text>
      <xsl:if test="compound">
        <xsl:text>
        m_shorthandMaker= getSubpropMaker(Constants.CP_</xsl:text>
        <xsl:call-template name="makeEnumConstant">
          <xsl:with-param name="propstr" select=
          		'compound/subproperty[@set-by-shorthand="true"]/name'/>
        </xsl:call-template>
        <xsl:text>);</xsl:text>
      </xsl:if>
      <xsl:text>
    }
</xsl:text>

      <xsl:if test="compound">
        <xsl:text>
    Property.Maker m_shorthandMaker;

    public Property checkEnumValues(String value) {
        return m_shorthandMaker.checkEnumValues(value);
    }

    protected boolean isCompoundMaker() {
        return true;
    }

    protected Property.Maker getSubpropMaker(int subpropId) {</xsl:text>
        <xsl:for-each select="compound/subproperty">
          <xsl:variable name="spname">
            <xsl:call-template name="makeClassName">
              <xsl:with-param name="propstr" select="name"/>
            </xsl:call-template>
          </xsl:variable>
          <xsl:text>
        if (subpropId == Constants.CP_</xsl:text>
          <xsl:call-template name="makeEnumConstant">
            <xsl:with-param name="propstr" select="name"/>
          </xsl:call-template>
          <xsl:text>)
            return s_</xsl:text>
          <xsl:value-of select="$spname"/>
          <xsl:text>Maker;</xsl:text>
        </xsl:for-each>
        <xsl:text>
        return super.getSubpropMaker(subpropId);
    }

    protected Property setSubprop(Property baseProp, int subpropId,
                                  Property subProp) {
        </xsl:text>
        <xsl:value-of select="datatype"/>
        <xsl:text> val = baseProp.get</xsl:text>
        <xsl:value-of select="datatype"/>
        <xsl:text>();
        // Do some type checking???
        // Check if one of our subproperties???
        val.setComponent(subpropId, subProp, false);
        return baseProp;
    }

    public Property getSubpropValue(Property baseProp, int subpropId) {
        </xsl:text>
        <xsl:value-of select="datatype"/>
        <xsl:text> val = baseProp.get</xsl:text>
        <xsl:value-of select="datatype"/>
        <xsl:text>();
        return val.getComponent(subpropId);
    }
</xsl:text>
      <xsl:choose>
        <!-- some subproperty default is context dependent;
             don't cache default! -->
        <xsl:when test='.//default[@contextdep="true"]'>
          <xsl:text>
    public Property make(PropertyList propertyList) throws FOPException {
        return makeCompound(propertyList, propertyList.getParentFObj());
    }</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>
    private Property m_defaultProp=null;
    public Property make(PropertyList propertyList) throws FOPException {
        if (m_defaultProp == null) {
            m_defaultProp=makeCompound(
                            propertyList, propertyList.getParentFObj());
        }
        return m_defaultProp;
    }</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>

    protected Property makeCompound(PropertyList pList, FObj fo) throws FOPException {</xsl:text>
      <xsl:value-of select="datatype"/>
      <xsl:text> p = new </xsl:text>
      <xsl:value-of select="datatype"/>
      <xsl:text>();
        Property subProp;</xsl:text>
      <xsl:for-each select="compound/subproperty/name">
        <xsl:variable name="spname">
          <xsl:call-template name="makeClassName">
            <xsl:with-param name="propstr" select="."/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:text>
       // set default for subprop </xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>
       subProp = getSubpropMaker(Constants.CP_</xsl:text>
        <xsl:call-template name="makeEnumConstant">
          <xsl:with-param name="propstr" select="."/>
        </xsl:call-template>
        <xsl:text>).make(pList, getDefaultFor</xsl:text>
        <xsl:value-of select='$spname'/>
        <xsl:text>(), fo);
       p.setComponent(Constants.CP_</xsl:text>
        <xsl:call-template name="makeEnumConstant">
          <xsl:with-param name="propstr" select="."/>
        </xsl:call-template>
        <xsl:text>, subProp, true);</xsl:text>
      </xsl:for-each>
      <xsl:text>
        return new </xsl:text>
      <xsl:value-of select="$propclass"/>
      <xsl:text>(p);
    }
</xsl:text>
      <!-- generate a "getDefaultForXXX" for each subproperty XXX -->
      <xsl:for-each select="compound/subproperty">
        <xsl:variable name="spname">
          <xsl:call-template name="makeClassName">
            <xsl:with-param name="propstr" select="name"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:text>
        protected String getDefaultFor</xsl:text>
        <xsl:value-of  select='$spname'/>
        <xsl:text>() {</xsl:text>
        <xsl:choose>
          <xsl:when test="default">
            <xsl:text>
            return "</xsl:text>
            <xsl:value-of  select='default'/>
            <xsl:text>";</xsl:text>
          </xsl:when>
          <xsl:when test=
            "use-generic and key('genericref', use-generic)/default">
            <xsl:text>
            return "</xsl:text>
            <xsl:value-of select='key(&apos;genericref&apos;,
                                  use-generic)/default'/>
            <xsl:text>";</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>
            return "";</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text>
    }</xsl:text>
      </xsl:for-each>
      <xsl:text>

    /** Set the appropriate components when the "base" property is set. */
    public Property convertProperty(Property p, PropertyList pList,FObj fo)
            throws FOPException {
        if (p instanceof </xsl:text>
      <xsl:value-of select="$propclass"/>
      <xsl:text>) return p;
        if (! (p instanceof EnumProperty)) {
            // delegate to the subprop maker to do conversions
            p = m_shorthandMaker.convertProperty(p,pList,fo);
        }
        if (p != null) {
            Property prop = makeCompound(pList, fo);
      </xsl:text>
      <xsl:value-of select="datatype"/>
      <xsl:text> pval = prop.get</xsl:text>
      <xsl:value-of select="datatype"/>
      <xsl:text>();</xsl:text>
      <xsl:for-each select="compound/subproperty[@set-by-shorthand='true']">
        <xsl:text>
            pval.setComponent(Constants.CP_</xsl:text>
        <xsl:call-template name="makeEnumConstant">
          <xsl:with-param name="propstr" select="name"/>
        </xsl:call-template>
        <xsl:text>, p, false);</xsl:text>
      </xsl:for-each>
      <xsl:text>
            return prop;
        }
        else return null;</xsl:text>
        <!--
             else {
             // throw some kind of exception!
             throw new FOPException("Can't convert value to <xsl:value-of select='$spdt'/> type");
             }
         -->
      <xsl:text>
    }

</xsl:text>
      </xsl:if> <!-- property/compound -->

      <xsl:if test="inherited">
        <xsl:text>
    public boolean isInherited() {
          return </xsl:text>
        <xsl:value-of select="inherited"/>
        <xsl:text>;
    }
</xsl:text>
      </xsl:if>

      <!-- the default implementation returns false -->
      <xsl:if test='inherited/@type="specified"'>
        <xsl:text>
    public boolean inheritsSpecified() {
        return true;
    }
</xsl:text>
      </xsl:if>


      <!-- Currently only works for Enum values -->
      <xsl:if test="derive">
        <xsl:text>
    public Property compute(PropertyList propertyList) {
        Property computedProperty = null;
        Property correspondingProperty = propertyList.get(Constants.PR_</xsl:text>
        <xsl:call-template name="makeEnumConstant">
          <xsl:with-param name="propstr" select="derive/@from"/>
        </xsl:call-template>
        <xsl:text>);
        if (correspondingProperty != null) {
            int correspondingValue = correspondingProperty.getEnum();</xsl:text>
        <xsl:for-each select="derive/if">
          <xsl:text>
            if (correspondingValue == </xsl:text>
          <xsl:value-of select="@match"/>
          <xsl:text>)
                computedProperty = new EnumProperty(</xsl:text>
          <xsl:value-of select="."/>
          <xsl:text>);
            else</xsl:text>
        </xsl:for-each>
        <xsl:text>
                ;
        }
        return computedProperty;
    }
</xsl:text>
      </xsl:if>

      <!-- If any corresponding element at property or subproperty levels -->
      <xsl:if test=".//corresponding">
        <xsl:if test=".//corresponding/@use-if-specified='true'">
          <xsl:text>
    public boolean isCorrespondingForced(PropertyList propertyList) {
        FObj parentFO = propertyList.getParentFObj();</xsl:text>
          <xsl:for-each select=".//corresponding/propval">
            <xsl:text>
        if (propertyList.getExplicit(</xsl:text>
            <xsl:apply-templates select="."/>
            <xsl:text>) != null)
            return true;</xsl:text>
          </xsl:for-each>
          <xsl:text>
        return false;
    }
</xsl:text>
      </xsl:if>
      <xsl:text>

    public Property compute(PropertyList propertyList) throws FOPException {
        FObj parentFO = propertyList.getParentFObj();
        Property p=null;</xsl:text>
      <xsl:choose>
        <xsl:when test="corresponding/propexpr">
          <xsl:text>
        // Make sure the property is set before calculating it!
        if (propertyList.getExplicitOrShorthand(</xsl:text>
          <xsl:apply-templates select="corresponding/propval"/>
          <xsl:text>) == null)
            return p;
        StringBuffer sbExpr=new StringBuffer();
        sbExpr.setLength(0);</xsl:text>
          <xsl:apply-templates select="corresponding/propexpr"/>
          <xsl:text>
        p = make(propertyList, sbExpr.toString(), propertyList.getParentFObj());</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>
        p= propertyList.getExplicitOrShorthand(</xsl:text>
          <xsl:apply-templates select="corresponding/propval"/>
          <xsl:text>);</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>
        if (p != null) {
            p = convertProperty(p, propertyList, parentFO );
        }</xsl:text>
      <xsl:if test="compound">
        <xsl:text>
        else p = makeCompound(propertyList, parentFO);

        Property subprop;</xsl:text>
        <xsl:for-each select="compound/subproperty/corresponding">
          <xsl:choose>
            <xsl:when test="propexpr">
              <xsl:apply-templates select="propexpr"/>
              <xsl:text>
        subprop = getSubpropMaker("</xsl:text>
              <xsl:value-of select='../name'/>
              <xsl:text>").
                  make(propertyList, sbExpr.toString(), parentFO);</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>
        subprop = propertyList.getExplicitOrShorthand(</xsl:text>
          <xsl:apply-templates select="propval"/>
          <xsl:text>);</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text>
        if (subprop != null) {
            setSubprop(p, Constants.CP_</xsl:text>
        <xsl:call-template name="makeEnumConstant">
          <xsl:with-param name="propstr" select="../name"/>
        </xsl:call-template>
        <xsl:text>, subprop);
        }</xsl:text>
      </xsl:for-each>
    </xsl:if>
    <xsl:text>
        return p;
    }
</xsl:text>
</xsl:if>

<!-- If can be specified by any shorthand property -->
<xsl:if test="shorthand">
  <xsl:text>
    public Property getShorthand(PropertyList propertyList) {
        Property p = null;
        ListProperty listprop;</xsl:text>
  <xsl:for-each select="shorthand">
    <xsl:variable name="shprop" select="."/>
    <xsl:text>
        if (p == null) {
            listprop =
                (ListProperty)propertyList.getExplicit(Constants.PR_</xsl:text>
    <xsl:call-template name="makeEnumConstant">
      <xsl:with-param name="propstr" select="$shprop"/>
    </xsl:call-template>
    <xsl:text>);
            if (listprop != null) {
               // Get a parser for the shorthand to set the individual properties
               ShorthandParser shparser =
                      new </xsl:text>
    <xsl:value-of select="key('shorthandref', $shprop)/datatype-parser"/>
    <xsl:text>(listprop);
               p = shparser.getValueForProperty(
                                        getPropId(), this, propertyList);
            }
        }</xsl:text>
  </xsl:for-each>
  <xsl:text>
        return p;
    }
</xsl:text>
</xsl:if>

<xsl:apply-templates select=
      "percent-ok|auto-ok|default|keyword-equiv|datatype-conversion|
            enumeration|extfile"/>
<xsl:text>
}
</xsl:text>
</redirect:write>
</xsl:if> <!-- need to create a class -->
</xsl:template>

<xsl:template match="corresponding/propexpr/propval">
  <xsl:text>
        sbExpr.append("_fop-property-value(");</xsl:text> <!-- Note: interpreted by property parser -->
  <xsl:apply-templates/>
  <xsl:text>
        sbExpr.append(")");</xsl:text>
</xsl:template>


<xsl:template match="propval">
  <xsl:choose>
    <xsl:when test="wmabs2rel[@dir='LEFT']">
      <xsl:call-template name="makeMap">
        <xsl:with-param name="lrtb" select='"START"'/>
        <xsl:with-param name="rltb" select='"END"'/>
        <xsl:with-param name="tbrl" select='"AFTER"'/>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="wmabs2rel[@dir='RIGHT']">
      <xsl:call-template name="makeMap">
        <xsl:with-param name="lrtb" select='"END"'/>
        <xsl:with-param name="rltb" select='"START"'/>
        <xsl:with-param name="tbrl" select='"BEFORE"'/>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="wmabs2rel[@dir='TOP']">
      <xsl:call-template name="makeMap">
        <xsl:with-param name="lrtb" select='"BEFORE"'/>
        <xsl:with-param name="rltb" select='"BEFORE"'/>
        <xsl:with-param name="tbrl" select='"START"'/>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="wmabs2rel[@dir='BOTTOM']">
      <xsl:call-template name="makeMap">
        <xsl:with-param name="lrtb" select='"AFTER"'/>
        <xsl:with-param name="rltb" select='"AFTER"'/>
        <xsl:with-param name="tbrl" select='"END"'/>
      </xsl:call-template>
    </xsl:when>

    <xsl:when test="wmrel2abs[@dir='START']">
      <xsl:call-template name="makeMap">
        <xsl:with-param name="lrtb" select='"LEFT"'/>
        <xsl:with-param name="rltb" select='"RIGHT"'/>
        <xsl:with-param name="tbrl" select='"TOP"'/>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="wmrel2abs[@dir='END']">
      <xsl:call-template name="makeMap">
        <xsl:with-param name="lrtb" select='"RIGHT"'/>
        <xsl:with-param name="rltb" select='"LEFT"'/>
        <xsl:with-param name="tbrl" select='"BOTTOM"'/>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="wmrel2abs[@dir='BEFORE'] or parwmrel2abs[@dir='BEFORE']">
      <xsl:call-template name="makeMap">
        <xsl:with-param name="lrtb" select='"TOP"'/>
        <xsl:with-param name="rltb" select='"TOP"'/>
        <xsl:with-param name="tbrl" select='"RIGHT"'/>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="wmrel2abs[@dir='AFTER'] or parwmrel2abs[@dir='AFTER']">
      <xsl:call-template name="makeMap">
        <xsl:with-param name="lrtb" select='"BOTTOM"'/>
        <xsl:with-param name="rltb" select='"BOTTOM"'/>
        <xsl:with-param name="tbrl" select='"LEFT"'/>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="wmrel2abs[@dir='BLOCKPROGDIM']">
      <xsl:call-template name="makeMap">
        <xsl:with-param name="lrtb" select='"HEIGHT"'/>
        <xsl:with-param name="rltb" select='"HEIGHT"'/>
        <xsl:with-param name="tbrl" select='"WIDTH"'/>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="wmrel2abs[@dir='INLINEPROGDIM']">
      <xsl:call-template name="makeMap">
        <xsl:with-param name="lrtb" select='"WIDTH"'/>
        <xsl:with-param name="rltb" select='"WIDTH"'/>
        <xsl:with-param name="tbrl" select='"HEIGHT"'/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>UNKNOWN <xsl:value-of select="."/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="corresponding//text()">
  <xsl:variable name="tval" select='normalize-space(.)'/>
  <xsl:if test="$tval != ''">
    <xsl:text>
        sbExpr.append("</xsl:text>
    <xsl:value-of select='$tval'/>
    <xsl:text>");</xsl:text>
  </xsl:if>
</xsl:template>

<xsl:template name="makeMap">
  <xsl:param name="lrtb"/>
  <xsl:param name="rltb"/>
  <xsl:param name="tbrl"/>
  <xsl:choose>
    <xsl:when test="parwmrel2abs">
      <xsl:text>parentFO.propertyList</xsl:text>
    </xsl:when>
    <xsl:otherwise>
     <xsl:text>propertyList</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:text>.wmMap(Constants.PR_</xsl:text>
  <xsl:apply-templates mode="x">
    <xsl:with-param name="dir" select='$lrtb'/>
  </xsl:apply-templates>
  <xsl:text>, Constants.PR_</xsl:text>
  <xsl:apply-templates mode="x">
    <xsl:with-param name="dir" select='$rltb'/>
  </xsl:apply-templates>
  <xsl:text>, Constants.PR_</xsl:text>
  <xsl:apply-templates mode="x">
    <xsl:with-param name="dir" select='$tbrl'/>
  </xsl:apply-templates>
  <xsl:text>)</xsl:text>
</xsl:template>

<xsl:template match="corresponding//text()" mode="x">
  <xsl:variable name="tval" select='normalize-space(.)'/>
  <xsl:if test="$tval != ''">
    <xsl:call-template name="makeEnumConstant">
      <xsl:with-param name="propstr" select="$tval"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template match="propval/wmrel2abs">
  <xsl:text>
        sbExpr.append(propertyList.wmRelToAbs(PropertyList.</xsl:text>
  <xsl:value-of select="@dir"/>
  <xsl:text>));</xsl:text>
</xsl:template>

<xsl:template match="propval/parwmrel2abs">
  <xsl:text>
        sbExpr.append(parentFO.propertyList.wmRelToAbs(PropertyList.</xsl:text>
  <xsl:value-of select="@dir"/>
  <xsl:text>));</xsl:text>
</xsl:template>

<xsl:template match="propval/wmabs2rel">
  <xsl:text>
        sbExpr.append(propertyList.wmAbsToRel(PropertyList.</xsl:text>
  <xsl:value-of select="@dir"/>
  <xsl:text>));</xsl:text>
</xsl:template>

<xsl:template match="propval/wmrel2abs" mode="x">
  <xsl:param name="dir"/>
  <xsl:value-of select="$dir"/>
</xsl:template>

<xsl:template match="propval/parwmrel2abs" mode="x">
  <xsl:param name="dir"/>
  <xsl:value-of select="$dir"/>
</xsl:template>

<xsl:template match="propval/wmabs2rel" mode="x">
  <xsl:param name="dir"/>
  <xsl:value-of select="$dir"/>
</xsl:template>

<!-- avoid unwanted output to placeholder file -->
<xsl:template match="localname"/>

<!-- Check that each member of the nodeset dtlist has the same value.
     Print a message if any member of dtlist is different
     from the first member. Return the first member.
 -->
<xsl:template name="check-subprop-datatype">
  <xsl:param name="dtlist"/>
  <xsl:variable name="dt">
    <xsl:value-of select='$dtlist[1]'/>
  </xsl:variable>
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
    <xsl:text>
        if (value.equals("</xsl:text>
    <xsl:value-of select="$oneval"/>
    <xsl:text>")) {
            return s_prop</xsl:text>
    <xsl:value-of select="@const"/>
    <xsl:text>;
        }</xsl:text>
    <xsl:call-template name="enumvals">
      <xsl:with-param name="specvals" select="substring-after($specvals, ' ')"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>
