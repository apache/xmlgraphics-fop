<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
                extension-element-prefixes="redirect">

<xsl:include href="./propinc.xsl"/>

<xsl:output method="text" />

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
    final private static Property.Maker s_<xsl:value-of select="name"/>Maker =
	new <xsl:value-of select="datatype"/>Property.Maker(
	     "<xsl:value-of select='../../name'/>.<xsl:value-of select='name'/>");
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
	if (subprop.equals("<xsl:value-of select='name'/>"))
	  return s_<xsl:value-of select="name"/>Maker;
    </xsl:for-each>
	return super.getSubpropMaker(subprop);
    }

    protected Property setSubprop(Property baseProp, String subpropName,
	Property subProp) {
      <xsl:value-of select="datatype"/> val = 
	((<xsl:value-of select="$propclass"/>)baseProp).get<xsl:value-of select="datatype"/>();
    <xsl:for-each select="compound/subproperty">
      <xsl:variable name="capname">
	<xsl:call-template name="capfirst">
          <xsl:with-param name="str" select="name"/>
        </xsl:call-template>
      </xsl:variable>
	if (subpropName.equals("<xsl:value-of select='name'/>"))
	  val.set<xsl:value-of select='$capname'/>(subProp.get<xsl:value-of select='datatype'/>());
	else
    </xsl:for-each>
	  return super.setSubprop(baseProp, subpropName, subProp);
	return baseProp;
    }

    public Property getSubpropValue(Property baseProp, String subpropName) {
      <xsl:value-of select="datatype"/> val = 
	((<xsl:value-of select="$propclass"/>)baseProp).get<xsl:value-of select="datatype"/>();
    <xsl:for-each select="compound/subproperty">
      <xsl:variable name="capname">
	<xsl:call-template name="capfirst">
          <xsl:with-param name="str" select="name"/>
        </xsl:call-template>
      </xsl:variable>
	if (subpropName.equals("<xsl:value-of select='name'/>"))
	  return new <xsl:value-of select='datatype'/>Property(
	val.get<xsl:value-of select='$capname'/>());
    </xsl:for-each>
	 return super.getSubpropValue(baseProp, subpropName);
    }
</xsl:if>

<xsl:if test='not(default/@contextdep = "true")'>
    private Property m_defaultProp=null;
</xsl:if>

<xsl:if test="inherited">
   public boolean isInherited() { return <xsl:value-of select="inherited"/>; }
</xsl:if>

<!-- the default implementation returns false -->
<xsl:if test='inherited/@type="specified"'>
   public boolean inheritsSpecified() {
	 return true;
   }
</xsl:if>

<!-- Content of element is code to calculate the base length -->
<xsl:if test="percent-ok">
    static class PropLengthBase extends LengthBase {
	private FObj fo;
	private PropertyList propertyList ;

	public PropLengthBase(FObj fo, PropertyList plist) {
	  this.fo = fo;
	// get from FO????
	  this.propertyList = plist;
	  //this.propertyList = fo.getProperties();
        }

 	public int getBaseLength() {
<xsl:choose>
  <xsl:when test="percent-ok/@base='FONTSIZE'">
	return propertyList.get("font-size").getLength().mvalue();
  </xsl:when>
  <xsl:when test="percent-ok/@base='INH-FONTSIZE'">
	return propertyList.getInherited("font-size").getLength().mvalue();
  </xsl:when>
  <xsl:when test="percent-ok/@base='CONTAINING-BOX'">
	<!-- should probably distinguish width and height... -->
	return fo.getContainingWidth();
  </xsl:when>
  <xsl:otherwise>
	return <xsl:value-of select="percent-ok"/>;
  </xsl:otherwise>
</xsl:choose>
        }
    }

    /** Return instance of internal class to calculate base Length
     * for percent specifications.
     */
    public PercentBase getPercentBase(final FObj fo,
	final PropertyList propertyList) {
	return new PropLengthBase(fo, propertyList);
    }
</xsl:if>

<!-- Look for "auto" length keyword -->
<xsl:if test="auto-ok">
    protected boolean isAutoLengthAllowed() {
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

<!-- Look for keyword equivalents. Value is the new expression  -->
<xsl:if test="keyword-equiv">
    protected String checkValueKeywords(String value) {
  <xsl:for-each select="keyword-equiv">
      if (value.equals("<xsl:value-of select="@match"/>")) {
	return new String("<xsl:value-of select="."/>");
      }
  </xsl:for-each>
      return super.checkValueKeywords(value);
    }
</xsl:if>

<!-- Generate code to convert from other datatypes to property datatype -->
<xsl:if test='datatype-conversion'>
    // See if other value types are acceptable
    protected Property convertPropertyDatatype(Property p,
	PropertyList propertyList, FObj fo) {
      <xsl:for-each select="datatype-conversion">
      {
        <xsl:variable name="dtc">
          <xsl:choose>
	    <xsl:when test="@vartype">
	       <xsl:value-of select="@vartype"/>
	    </xsl:when><xsl:otherwise>
	      <xsl:value-of select="@type"/>
	    </xsl:otherwise>
	  </xsl:choose>
	</xsl:variable>
	<xsl:value-of select="$dtc"/><xsl:text> </xsl:text> <xsl:value-of select="@varname"/> = 
		p.get<xsl:value-of select="@type"/>();
	if (<xsl:value-of select="@varname"/> != null) {
	    return new <xsl:value-of select="$propclass"/>(
		    <xsl:value-of select='.'/>);
        }
      }
      </xsl:for-each>
      return super.convertPropertyDatatype(p, propertyList, fo);
    }
</xsl:if>

<xsl:if test="default">
    public Property make(PropertyList propertyList, boolean bForceNew) throws FOPException {
      <xsl:choose><xsl:when test='default/@contextdep="true"'>
        return make(propertyList, "<xsl:value-of select="default"/>", null);
	</xsl:when><xsl:otherwise>
	if (bForceNew) {
	    // Make a new property instead of using the static default
            return make(propertyList, "<xsl:value-of select="default"/>", null);
        }
        if (m_defaultProp == null)
            m_defaultProp=make(propertyList, "<xsl:value-of select="default"/>", null);
        return m_defaultProp;
	</xsl:otherwise></xsl:choose>
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
}
</redirect:write>
</xsl:if> <!-- need to create a class -->
</xsl:template>

</xsl:stylesheet>


