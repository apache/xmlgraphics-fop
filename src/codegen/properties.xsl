<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
                extension-element-prefixes="redirect">
<xsl:output method="text" />

<xsl:template match="property" priority="-1">
<xsl:variable name="classname" select="class-name"/>
<redirect:write select="concat('@org/apache/fop@/fo/properties/', $classname, '.java')">
package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.*;
import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;

public class <xsl:value-of select="class-name"/> extends Property {

  public static class Maker extends Property.Maker {
    public boolean isInherited() { return <xsl:value-of select="inherited"/>; }

    public Property make(PropertyList propertyList, String value) throws FOPException {
<xsl:choose>
<xsl:when test="make">
      <xsl:variable name="datatype" select="datatype"/>
      <xsl:value-of select="$datatype"/> v;
<xsl:if test="make/to-double">
      double d = toDouble(value);
</xsl:if>
<xsl:for-each select="make/if">
      if (value.equals("<xsl:value-of select="@match"/>")) {
        v = new <xsl:value-of select="$datatype"/>(<xsl:value-of select="."/>);
      }
</xsl:for-each>
<xsl:for-each select="make/else-if-number">
      else if (!Double.isNaN(d)) {
        v = new <xsl:value-of select="$datatype"/>( <xsl:value-of select="."/>);
      }
</xsl:for-each>
      else {
        v = new <xsl:value-of select="datatype"/>(<xsl:value-of select="make/else"/>);
      }
      return new <xsl:value-of select="class-name"/>(propertyList, v);
</xsl:when>
<xsl:otherwise>
      return new <xsl:value-of select="class-name"/>(propertyList, new <xsl:value-of select="datatype"/>(value));
</xsl:otherwise>
</xsl:choose>
    }

    public Property make(PropertyList propertyList) throws FOPException {
      return make(propertyList, "<xsl:value-of select="default"/>");
    }
  }

  public static Property.Maker maker() {
    return new <xsl:value-of select="class-name"/>.Maker();
  }

  private <xsl:value-of select="datatype"/> value;

  public <xsl:value-of select="class-name"/>(PropertyList propertyList, <xsl:value-of select="datatype"/> explicitValue) {
    this.propertyList = propertyList;
    this.value = explicitValue;
  }

  public <xsl:value-of select="datatype"/> get<xsl:value-of select="datatype"/>() {
    return this.value;
  }

}
</redirect:write>
</xsl:template>

<xsl:template match="property[datatype/enumeration]">
<xsl:variable name="classname" select="class-name"/>
<redirect:write select="concat('@org/apache/fop@/fo/properties/', $classname, '.java')">
package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.*;
import org.apache.fop.fo.*;
import org.apache.fop.apps.FOPException;

public class <xsl:value-of select="class-name"/> extends Property {
<xsl:for-each select="datatype/enumeration/value">
  public final static int <xsl:value-of select="@const"/> = <xsl:number/>;</xsl:for-each>

  public static class Maker extends Property.Maker {
    public boolean isInherited() { return <xsl:value-of select="inherited"/>; }

    public Property make(PropertyList propertyList, String value) throws FOPException {
      int v;
      <xsl:for-each select="datatype/enumeration/value">
      if (value.equals("<xsl:value-of select="."/>")) { v = <xsl:value-of select="@const"/>; }
      else</xsl:for-each>
      {
        System.err.println("WARNING: Unknown value for <xsl:value-of select="name"/>: " + value);
        return make(propertyList, "<xsl:value-of select="default"/>");
      }
      return new <xsl:value-of select="class-name"/>(propertyList, v);
    }

    public Property make(PropertyList propertyList) throws FOPException {
      return make(propertyList, "<xsl:value-of select="default"/>");
    }
    <xsl:if test="derive">
    public Property compute(PropertyList propertyList) {
      Property computedProperty = null;
      Property correspondingProperty = propertyList.get("<xsl:value-of select="derive/@from"/>");
      if (correspondingProperty != null) {
        int correspondingValue = correspondingProperty.getEnum();
        <xsl:for-each select="derive/if">
        if (correspondingValue == <xsl:value-of select="@match"/>)
          computedProperty = new <xsl:value-of select="$classname"/>(propertyList, <xsl:value-of select="."/>);
        else</xsl:for-each>
        ;
      }
      return computedProperty;
    }
    </xsl:if>
  }

  public static Property.Maker maker() {
    return new <xsl:value-of select="class-name"/>.Maker();
  }

  private int value;

  public <xsl:value-of select="class-name"/>(PropertyList propertyList, int explicitValue) {
    this.propertyList = propertyList;
    this.value = explicitValue;
  }

  public int getEnum() {
    return this.value;
  }

}
</redirect:write>
</xsl:template>
</xsl:stylesheet>


