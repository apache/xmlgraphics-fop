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
    xmlns:date="http://exslt.org/dates-and-times"
    extension-element-prefixes="date"
    version="1.0">

  <date:date-format lang="en" first-day-in-week="monday"/>

  <xsl:template match="news">
    <document>
      <header>
        <title>News</title>
      </header>
      <body>
        <xsl:apply-templates select="item"/>
      </body>
    </document>  
  </xsl:template>

  <xsl:template match="item">
    <section id="news-{@date}">
      <title>
        <xsl:value-of select="date:day-in-month(@date)"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="date:month-abbreviation(@date)"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="date:year(@date)"/>
        <xsl:text>: </xsl:text>
        <xsl:value-of select="@title"/>
      </title>
      <xsl:copy-of select="."/>
    </section>
 </xsl:template>

</xsl:stylesheet>
