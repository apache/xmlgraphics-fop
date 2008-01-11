<?xml version="1.0"?>
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
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

  <xsl:template match="disabled-testcases">
   <document>
    <header>
     <title>Disabled Test Cases</title>
    </header>
    <body>
      <xsl:choose>
        <xsl:when test="count(testcase) &gt; 0">
          <xsl:apply-templates select="testcase"/>
        </xsl:when>
        <xsl:otherwise>
          <p>Currently, there are no known issues to list here!</p>
        </xsl:otherwise>
      </xsl:choose>
    </body>
   </document>  
  </xsl:template>

  <xsl:template match="testcase">
    <p>
      <strong><xsl:value-of select="file"/></strong>
      <xsl:if test="boolean(name)"><xsl:text> (</xsl:text><xsl:value-of select="name"/>)</xsl:if>:<br/>
      <xsl:choose>
        <xsl:when test="string-length(description) &gt; 0">
          <xsl:value-of select="description"/>
        </xsl:when>
        <xsl:otherwise><em>TODO: Add missing description in disabled-testcases.xml!</em></xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="reference"/>
    </p>
    <p/>
  </xsl:template>
  
  <xsl:template match="reference">
    <br/>
    See also: <a href="{.}"><xsl:value-of select="."/></a>
  </xsl:template>
  
</xsl:stylesheet>
