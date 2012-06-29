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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:template match="@*|node()" name="copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="/">
    <fo:root>
      <fo:layout-master-set>
        <fo:simple-page-master master-name="page"
          page-height="500pt" page-width="300pt" margin="20pt">
          <fo:region-body margin-top="20pt"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <xsl:apply-templates select="//fo:page-sequence"/>
    </fo:root>
  </xsl:template>

  <xsl:template match="fo:page-sequence">
    <fo:page-sequence master-reference="page">
      <xsl:apply-templates select="fo:flow"/>
    </fo:page-sequence>
  </xsl:template>

  <xsl:template match="fo:flow">
    <xsl:copy>
      <xsl:apply-templates select="@*[not(starts-with(name(), 'space-before'))]"/>
      <fo:table width="100%" table-layout="fixed">
        <fo:table-footer>
          <fo:table-cell background-color="#F0F0F0">
            <xsl:apply-templates select="@*[starts-with(name(), 'space-before')]"/>
            <xsl:apply-templates select="*"/>
          </fo:table-cell>
        </fo:table-footer>
        <fo:table-body>
          <fo:table-cell>
            <fo:block>The content below is in the table footer.</fo:block>
          </fo:table-cell>
        </fo:table-body>
      </fo:table>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
