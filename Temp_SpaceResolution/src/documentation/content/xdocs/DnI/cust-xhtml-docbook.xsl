<?xml version="1.0" encoding="utf-8"?>

<!--
  * Copyright 2004-2005 The Apache Software Foundation.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  -->

<!-- $Id$ -->

<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml"
  version="1.0">

  <xsl:import
    href="http://docbook.sourceforge.net/release/xsl/current/xhtml/chunk.xsl"/>

  <!-- Use this import if you do not want chunks -->
  <!-- 
  <xsl:import
    href="http://docbook.sourceforge.net/release/xsl/current/xhtml/docbook.xsl"/> 
-->

  <xsl:param name="chunker.output.encoding" select="'UTF-8'"/>
  <xsl:param name="chunk.section.depth" select="2"/>
  <xsl:param name="section.autolabel" select="1"/>
  <xsl:param name="base.dir" select="'DnI-xhtml/'"/>

</xsl:stylesheet>
