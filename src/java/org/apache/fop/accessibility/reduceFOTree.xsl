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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  xmlns:fox="http://xmlgraphics.apache.org/fop/extensions" 
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:foi="http://xmlgraphics.apache.org/fop/internal" 
  version="1.0">
    <xsl:output method="xml" indent="no"/>
    
    <xsl:template match="*">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@foi:ptr"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="@master-reference|@flow-name"></xsl:template>
    
    <xsl:template match="fo:block">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@foi:ptr"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="fo:inline|fo:wrapper|fo:page-number|fo:page-number-citation|fo:page-number-citation-last">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@foi:ptr"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="fo:table-cell|fo:table|fo:table-body|fo:table-footer|fo:table-row|fo:table-header">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@foi:ptr"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="fo:list-block|fo:list-item|fo:list-item-label|fo:list-item-body">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@foi:ptr"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="fo:basic-link|fo:block-container|fo:character|fo:instream-foreign-object|fo:marker">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@foi:ptr"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="fo:external-graphic|fo:instream-foreign-object">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@fox:alt-text|@foi:ptr"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>
    
    <!-- the following nodes are being ignored/filtered -->
    
    <xsl:template match="text()"/>
    
    <xsl:template match="fo:layout-master-set | comment() | processing-instruction() | fo:simple-page-master | fo:table-column | fo:leader | fo:retrieve-marker "/>

    <xsl:template match="svg:svg | svg | fo:inline-container | fo:float | fo:bidi-override"/>
    
</xsl:stylesheet>
