<transform xmlns="http://www.w3.org/1999/XSL/Transform"
 xmlns:xt="http://www.jclark.com/xt" element-prefixes="xt"
 version="1.0">
<template match="property" priority="-1">
<variable name="classname" select="class-name"/>
<xt:document method="text" href="org/apache/xml/fop/fo/properties/{$classname}.java">
package org.apache.xml.fop.fo.properties;

import org.apache.xml.fop.datatypes.*;
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.apps.FOPException;

public class <value-of select="class-name"/> extends Property {

  public static class Maker extends Property.Maker {
    public boolean isInherited() { return <value-of select="inherited"/>; }

    public Property make(PropertyList propertyList, String value) throws FOPException {
<choose>
<when test="make">
      <variable name="datatype" select="datatype"/>
      <value-of select="$datatype"/> v;
<if test="make/to-double">
      double d = toDouble(value);
</if>
<for-each select="make/if">
      if (value.equals("<value-of select="@match"/>")) {
        v = new <value-of select="$datatype"/>(<value-of select="."/>);
      }
</for-each>
<for-each select="make/else-if-number">
      else if (!Double.isNaN(d)) {
        v = new <value-of select="$datatype"/>( <value-of select="."/>);
      }
</for-each>
      else {
        v = new <value-of select="datatype"/>(<value-of select="make/else"/>);
      }
      return new <value-of select="class-name"/>(propertyList, v);
</when>
<otherwise>
      return new <value-of select="class-name"/>(propertyList, new <value-of select="datatype"/>(value));
</otherwise>
</choose>
    }

    public Property make(PropertyList propertyList) throws FOPException {
      return make(propertyList, "<value-of select="default"/>");
    }
  }

  public static Property.Maker maker() {
    return new <value-of select="class-name"/>.Maker();
  }

  private <value-of select="datatype"/> value;

  public <value-of select="class-name"/>(PropertyList propertyList, <value-of select="datatype"/> explicitValue) {
    this.propertyList = propertyList;
    this.value = explicitValue;
  }

  public <value-of select="datatype"/> get<value-of select="datatype"/>() {
    return this.value;
  }

}
</xt:document>
</template>

<template match="property[datatype/enumeration]">
<variable name="classname" select="class-name"/>
<xt:document method="text" href="org/apache/xml/fop/fo/properties/{$classname}.java">
package org.apache.xml.fop.fo.properties;

import org.apache.xml.fop.datatypes.*;
import org.apache.xml.fop.fo.*;
import org.apache.xml.fop.apps.FOPException;

public class <value-of select="class-name"/> extends Property {
<for-each select="datatype/enumeration/value">
  public final static int <value-of select="@const"/> = <number/>;</for-each>

  public static class Maker extends Property.Maker {
    public boolean isInherited() { return <value-of select="inherited"/>; }

    public Property make(PropertyList propertyList, String value) throws FOPException {
      int v;
      <for-each select="datatype/enumeration/value">
      if (value.equals("<value-of select="."/>")) { v = <value-of select="@const"/>; }
      else</for-each>
      {
        System.err.println("WARNING: Unknown value for <value-of select="name"/>: " + value);
        return make(propertyList, "<value-of select="default"/>");
      }
      return new <value-of select="class-name"/>(propertyList, v);
    }

    public Property make(PropertyList propertyList) throws FOPException {
      return make(propertyList, "<value-of select="default"/>");
    }
    <if test="derive">
    public Property compute(PropertyList propertyList) {
      Property computedProperty = null;
      Property correspondingProperty = propertyList.get("<value-of select="derive/@from"/>");
      if (correspondingProperty != null) {
        int correspondingValue = correspondingProperty.getEnum();
        <for-each select="derive/if">
        if (correspondingValue == <value-of select="@match"/>)
          computedProperty = new <value-of select="$classname"/>(propertyList, <value-of select="."/>);
        else</for-each>
        ;
      }
      return computedProperty;
    }
    </if>
  }

  public static Property.Maker maker() {
    return new <value-of select="class-name"/>.Maker();
  }

  private int value;

  public <value-of select="class-name"/>(PropertyList propertyList, int explicitValue) {
    this.propertyList = propertyList;
    this.value = explicitValue;
  }

  public int getEnum() {
    return this.value;
  }

}
</xt:document>
</template>
</transform>
