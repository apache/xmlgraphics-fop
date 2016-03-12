<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!-- $Id$ -->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
                extension-element-prefixes="redirect">

<xsl:import href="elements.xsl"/>

<xsl:output method="xml" />

<xsl:template match="elements">
<property-list>
<xsl:attribute name="family">
<xsl:call-template name="capall"><xsl:with-param name="str" select="$prefixVal"/></xsl:call-template>
</xsl:attribute>
  <generic-property-list>
    <xsl:apply-templates select="*//attribute"/>
 </generic-property-list>
</property-list>
</xsl:template>

<xsl:template match="*//attribute">
  <property>
  <name><xsl:apply-templates/></name>
    <use-generic ispropclass="true">SVGStringProperty</use-generic>
  </property>
</xsl:template>


</xsl:stylesheet>
