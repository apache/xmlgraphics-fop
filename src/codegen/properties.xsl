<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
                extension-element-prefixes="redirect">

<xsl:include href="./propinc.xsl"/>

<xsl:output method="text" />


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

<!-- Look for keyword equivalents. Value is the new expression  -->
<xsl:template match="keyword-equiv[1]">
    protected String checkValueKeywords(String value) {
  <xsl:for-each select="../keyword-equiv">
      if (value.equals("<xsl:value-of select="@match"/>")) {
	return new String("<xsl:value-of select="."/>");
      }
  </xsl:for-each>
      return super.checkValueKeywords(value);
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

<redirect:write select="concat('@org/apache/fop@/fo/properties/', $classname, '.java')">
package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.*;
import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.messaging.MessageHandler;

public class <xsl:value-of select="$classname"/> extends  <xsl:value-of select="$superclass"/><xsl:value-of select="$enumconst"/> {

<!-- If has enumerated values and is a generic class, create a nested
  interface defining the enumeration constants -->
<xsl:if test="enumeration/value and @type='generic'">
  public interface Enums {
<xsl:for-each select="enumeration/value">
    int <xsl:value-of select="@const"/> = <xsl:number/>;
</xsl:for-each>
  }
</xsl:if>

<!-- Handle enumeration values -->
<xsl:for-each select="enumeration/value">
  protected final static EnumProperty s_prop<xsl:value-of select="@const"/> = new EnumProperty(<xsl:if test="../../@type='generic'">Enums.</xsl:if><xsl:value-of select="@const"/>);
</xsl:for-each>


<!-- Look for compound properties -->
<xsl:if test="compound">
    <xsl:for-each select="compound/subproperty">
      <xsl:variable name="spname">
	<xsl:call-template name="makeClassName">
          <xsl:with-param name="propstr" select="name"/>
        </xsl:call-template>
      </xsl:variable>

      <xsl:choose>
        <xsl:when test='*[local-name(.)!="name" and local-name(.)!="datatype"]'>
    static private class SP_<xsl:value-of select="$spname"/>Maker 
	extends <xsl:value-of select="datatype"/>Property.Maker {
	   SP_<xsl:value-of select="$spname"/>Maker(String sPropName) {
	     super(sPropName);
           }
	<xsl:apply-templates select="percent-ok|auto-ok|keyword-equiv|datatype-conversion"/>
    }
    final private static Property.Maker s_<xsl:value-of select="$spname"/>Maker =
	new SP_<xsl:value-of select="$spname"/>Maker(
	     "<xsl:value-of select='../../name'/>.<xsl:value-of select='name'/>");
        </xsl:when>
        <xsl:otherwise>
    final private static Property.Maker s_<xsl:value-of select="$spname"/>Maker =
	new <xsl:value-of select="datatype"/>Property.Maker(
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
    }

<xsl:if test="compound">
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
      <xsl:value-of select="datatype"/> val = 
	((<xsl:value-of select="$propclass"/>)baseProp).get<xsl:value-of select="datatype"/>();
    <xsl:for-each select="compound/subproperty">
      <xsl:variable name="spname">
	<xsl:call-template name="makeClassName">
          <xsl:with-param name="propstr" select="name"/>
        </xsl:call-template>
      </xsl:variable>
	if (subpropName.equals("<xsl:value-of select='name'/>"))
	  val.set<xsl:value-of select='$spname'/>(subProp.get<xsl:value-of select='datatype'/>(), false);
	else
    </xsl:for-each>
	  return super.setSubprop(baseProp, subpropName, subProp);
	return baseProp;
    }

    public Property getSubpropValue(Property baseProp, String subpropName) {
      <xsl:value-of select="datatype"/> val = 
	((<xsl:value-of select="$propclass"/>)baseProp).get<xsl:value-of select="datatype"/>();
    <xsl:for-each select="compound/subproperty">
      <xsl:variable name="spname">
	<xsl:call-template name="makeClassName">
          <xsl:with-param name="propstr" select="name"/>
        </xsl:call-template>
      </xsl:variable>
	if (subpropName.equals("<xsl:value-of select='name'/>"))
	  return new <xsl:value-of select='datatype'/>Property(
	val.get<xsl:value-of select='$spname'/>());
    </xsl:for-each>
	 return super.getSubpropValue(baseProp, subpropName);
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
	  p.set<xsl:value-of select='$spname'/>(subProp.get<xsl:value-of select='../datatype'/>(), true);
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
        </xsl:when><xsl:otherwise>
	return "";
	</xsl:otherwise>
      </xsl:choose>
    }
    </xsl:for-each>

    /** Set the appropriate components when the "base" property is set. */
    protected Property convertProperty(Property p, PropertyList pList,FObj fo)
	throws FOPException
    {
<xsl:variable name="spdt">
  <xsl:call-template name="check-subprop-datatype">
    <xsl:with-param name="dtlist"
	 select="compound/subproperty[@set-by-shorthand='true']/datatype"/>
  </xsl:call-template>
</xsl:variable>
<!--
        Property sub= getSubpropMaker("<xsl:value-of select='compound/subproperty[@set-by-shorthand="true"]/name'/>").convertProperty(p,pList,fo);
	if (sub != null) {
          <xsl:value-of select='$spdt'/> spval=sub.get<xsl:value-of select='$spdt'/>();
-->
        <xsl:value-of select='$spdt'/> spval=p.get<xsl:value-of select='$spdt'/>();
	if (spval == null) {
	  // NOTE: must convert to the component datatype, not compound!
	  Property pconv = convertPropertyDatatype(p, pList, fo);
	  if (pconv != null) {
	    spval=pconv.get<xsl:value-of select='$spdt'/>();
          }
        }
	if (spval != null) {
	  Property prop = makeCompound(pList, fo);
	  <xsl:value-of select='datatype'/> pval = prop.get<xsl:value-of select='datatype'/>();
<xsl:for-each select="compound/subproperty[@set-by-shorthand='true']">
      <xsl:variable name="spname"><xsl:call-template name="makeClassName">
          <xsl:with-param name="propstr" select="name"/>
        </xsl:call-template></xsl:variable>
	  pval.set<xsl:value-of select='$spname'/>(spval, false);
</xsl:for-each>
          return prop;
        }
        else {
	  // throw some kind of exception!
	  throw new FOPException("Can't convert value to <xsl:value-of select='$spdt'/> type");
        }
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

<!-- Handle enumerated values -->
<xsl:if test="enumeration/value">
    protected Property findConstant(String value) {
    <xsl:for-each select="enumeration/value">
      if (value.equals("<xsl:value-of select="."/>")) { return s_prop<xsl:value-of select="@const"/>; }
    </xsl:for-each>
	return super.findConstant(value);
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
<xsl:apply-templates select="percent-ok|auto-ok|default|keyword-equiv|datatype-conversion"/>
}
</redirect:write>
</xsl:if> <!-- need to create a class -->
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

</xsl:stylesheet>
