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
  <xsl:param name="project-name" select="//skinconfig/project-name"/>
  <xsl:param name="project-url" select="//skinconfig/project-url"/>
  <xsl:template match="news">
    <rss version="2.0">
      <channel>
        <title><xsl:value-of select="$project-name"/> News</title>
        <link><xsl:value-of select="$project-url"/></link>
        <description>
          Subproject News for <xsl:value-of select="$project-name"/>
        </description>
        <language>en</language>
        <xsl:apply-templates/>
      </channel>
    </rss>
  </xsl:template>
  <xsl:template match="item">
    <item>
      <title>
        <xsl:value-of select="date:day-in-month(@date)"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="date:month-abbreviation(@date)"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="date:year(@date)"/>
        <xsl:text>: </xsl:text>
        <xsl:value-of select="@title"/>
      </title>
      <guid>news-<xsl:value-of select="@date"/></guid>
      <link><xsl:value-of select="$project-url"/>/index.html#news-<xsl:value-of select="@date"/></link>
      <pubDate><xsl:value-of select="date:format-date(@date, 'EEE, d MMM yyyy HH:mm:ss Z')"/></pubDate>
      <description>
        <xsl:apply-templates/>
      </description>
    </item>
  </xsl:template>
</xsl:stylesheet>
