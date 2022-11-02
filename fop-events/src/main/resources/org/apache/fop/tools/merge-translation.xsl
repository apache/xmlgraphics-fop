<?xml version="1.0" encoding="UTF-8"?>
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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output indent="yes" method="xml" encoding="UTF-8"/>

  <xsl:param name="generated-url" select="''"/>
  
  <xsl:template match="catalogue">
    <catalogue>
      <xsl:attribute name="xml:lang"><xsl:value-of select="@xml:lang"/></xsl:attribute>
      <xsl:apply-templates/>
      <xsl:if test="$generated-url != ''">
        <xsl:variable name="generated" select="document($generated-url)"/>
        <xsl:call-template name="add-new-messages">
          <xsl:with-param name="existing" select="."/>
          <xsl:with-param name="new" select="$generated/catalogue"/>
        </xsl:call-template>
      </xsl:if>
    </catalogue>
  </xsl:template>
  
  <xsl:template name="add-new-messages">
    <xsl:param name="existing"/>
    <xsl:param name="new"/>
    <xsl:for-each select="$new/message">
      <xsl:variable name="k" select="@key"/>
      <xsl:if test="not(boolean($existing/message[@key = $k]))">
        <xsl:apply-templates select="."></xsl:apply-templates>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>  
  
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
