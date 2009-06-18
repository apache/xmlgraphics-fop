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
<!-- This stylesheet renders the disabled testcass in XHTML. -->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xhtml="http://www.w3.org/1999/xhtml">

  <xsl:template match="/">
    <xhtml:html>
      <xhtml:head>
        <xhtml:title>FOP Disabled Testcases</xhtml:title>
      </xhtml:head>
      <xhtml:body>
        <xsl:apply-templates/>
      </xhtml:body>
    </xhtml:html>
  </xsl:template>

  <xsl:template match="disabled-testcases">
    <xhtml:h3>
      <xhtml:a href="#fileindex">Index on filename</xhtml:a>
    </xhtml:h3>
    <xhtml:h1>FOP Disabled Testcases</xhtml:h1>
    <xhtml:ul>
      <xsl:apply-templates>
        <xsl:sort select="name"/>
      </xsl:apply-templates>
    </xhtml:ul>

    <xhtml:h2>
      <xhtml:a name="fileindex">FOP Disabled Testcases, index on filename</xhtml:a>
    </xhtml:h2>
    <xhtml:ul>
      <xsl:apply-templates mode="toc">
        <xsl:sort select="file"/>
      </xsl:apply-templates>
	</xhtml:ul>
  </xsl:template>

  <xsl:template match="testcase" mode="toc">
    <xhtml:li>
      <xhtml:a href="#{generate-id()}">
        <xsl:apply-templates select="file"/>
      </xhtml:a>
      <!--
      <xsl:text> (</xsl:text>
      <xhtml:a href="standard-testcases/{file}">
        <xsl:apply-templates select="file"/>
      </xhtml:a>
      <xsl:text>)</xsl:text>
-->
    </xhtml:li>
  </xsl:template>

  <xsl:template match="testcase">
    <xhtml:li>
      <xhtml:a name="{generate-id()}">
        <xsl:apply-templates select="name"/>
      </xhtml:a>
      <xsl:text> (</xsl:text>
      <xhtml:a href="standard-testcases/{file}">
        <xsl:apply-templates select="file"/>
      </xhtml:a>
      <xsl:text>)</xsl:text>
      <xhtml:ul>
        <xsl:apply-templates select="description"/>
        <xsl:apply-templates select="reference"/>
      </xhtml:ul>
    </xhtml:li>
  </xsl:template>

  <xsl:template match="description">
    <xhtml:li>
      <xsl:apply-templates/>
    </xhtml:li>
  </xsl:template>

  <xsl:template match="reference">
    <xhtml:li>
      <xsl:text>Reference: </xsl:text>
      <xhtml:a href="{.}">
        <xhtml:tt>
          <xsl:apply-templates/>
        </xhtml:tt>
      </xhtml:a>
    </xhtml:li>
  </xsl:template>

</xsl:stylesheet>
