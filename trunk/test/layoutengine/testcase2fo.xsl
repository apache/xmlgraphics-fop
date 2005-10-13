<?xml version="1.0" encoding="UTF-8"?>
<!--
  Copyright 2005 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!-- $Id$ -->
<!-- This stylesheet extracts the FO part from the testcase so it can be passed to FOP for layout. -->
<!--
Variable substitution:

For any attribute value that starts with a "##" the stylesheet looks for an element with the variable 
name under /testcase/variables, ex. "##img" looks for /testcase/variables/img and uses its element
value as subsitution value.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:template match="testcase">
    <xsl:apply-templates select="fo/*" mode="copy"/>
  </xsl:template>
  
  <xsl:template match="node()" mode="copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="copy"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@*" mode="copy">
    <xsl:choose>
      <xsl:when test="starts-with(., '##')">
        <!-- variable substitution -->
        <xsl:variable name="nodename" select="name()"/>
        <xsl:variable name="varname" select="substring(., 3)"/>
        <xsl:choose>
          <xsl:when test="boolean(//variables/child::*[local-name() = $varname])">
            <xsl:attribute name="{name(.)}">
              <xsl:value-of select="//variables/child::*[local-name() = $varname]"/>
            </xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <!-- if variable isn't defined, just copy -->
            <xsl:copy>
              <xsl:apply-templates select="node()" mode="copy"/>
            </xsl:copy>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="node()" mode="copy"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
