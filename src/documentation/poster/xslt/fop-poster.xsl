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
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:fo="http://www.w3.org/1999/XSL/Format"
		xmlns:svg="http://www.w3.org/2000/svg"
		xmlns:exslt="http://exslt.org/common"
        xmlns:xlink="http://www.w3.org/1999/xlink"
		extension-element-prefixes="exslt">

	<xsl:output method="xml" indent="yes"/>
	<xsl:include href="poster.xsl"/>
	<xsl:include href="common.xsl"/>

	<xsl:template match="poster">
		<fo:root font-family="Verdana" font-size="14pt">
			<xsl:call-template name="poster-layout-master-set"/>
            <fo:declarations>
              <x:xmpmeta xmlns:x="adobe:ns:meta/">
                <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                  <rdf:Description rdf:about="" xmlns:xap="http://ns.adobe.com/xap/1.0">
                    <xap:Title>
                      <rdf:Alt>
                        <rdf:li xml:lang="x-default">Apache FOP Poster</rdf:li>
                        <rdf:li xml:lang="de">Apache FOP Plakat</rdf:li>
                      </rdf:Alt>
                    </xap:Title>
                  </rdf:Description>
                  <rdf:Description rdf:about="" xmlns:dc="http://purl.org/dc/elements/1.1/">
                    <dc:creator>Jeremias Märki</dc:creator>
                    <dc:title>
                      <rdf:Alt>
                        <rdf:li xml:lang="x-default">Apache FOP Poster</rdf:li>
                        <rdf:li xml:lang="de">Apache FOP Plakat</rdf:li>
                      </rdf:Alt>
                    </dc:title>
                  </rdf:Description>
                </rdf:RDF>
              </x:xmpmeta>
            </fo:declarations>
			<fo:page-sequence master-reference="A2plusL" language="en" hyphenate="true" text-align="justify">
              <fo:static-content flow-name="xsl-region-before">
                <xsl:call-template name="background"/>
              </fo:static-content>
			  <fo:flow flow-name="xsl-region-body">
                <xsl:apply-templates select="section"/>
			  </fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>
	
	<xsl:template name="background">
	  <fo:block-container absolute-position="fixed" width="700mm" height="500mm">
	    <fo:block line-height="0">
          <fo:instream-foreign-object>
          <svg xmlns="http://www.w3.org/2000/svg" width="700mm"
              height="500mm" viewBox="0 0 840 594">
            <g font-family="Verdana">
              <path
                 d="M 0,55 L 580,55 c 40,0 60,30 100,30 L 840,85 840,575 L 280,575 c -40,0 -60,-50, -100,-50 L 0,525 z"
                 style="fill:lightblue; stroke:none;" />
              <path
                 d="M 0,55 L 580,55 c 40,0 60,30 100,30 L 840,85"
                 style="fill:none; stroke:blue; stroke-width:5;" />
              <path
                 d="M 840,575 L 280,575 c -40,0 -60,-50 -100,-50 L 0,525"
                 style="fill:none; stroke:blue; stroke-width:5;" />
              <image x="680" y="7" width="140" height="60" xlink:href="../svg/fop-logo-reconstructed.svg"/>
              <text x="15" y="35" style="fill:black; font-weight:bold; font-size:17">
                Apache FOP - The leading open source XSL-FO formatter
              </text>
              <text x="685" y="74" style="fill:black; font-size:5.5">
                A product of the Apache XML Graphics Project
              </text>
              <image x="15" y="530" width="160" height="60" xlink:href="../svg/asf-logo.svg"/>
              <text x="360" y="560" style="fill:blue; font-size:12">
                For more details, please visit: <a xlink:href="http://xmlgraphics.apache.org/fop/">http://xmlgraphics.apache.org/fop/</a>
              </text>
              <text style="fill:gray; font-size:5" text-anchor="end">
                <tspan x="830" y="587">
                  Poster generated with Apache FOP and Apache Batik
                </tspan>
                <tspan x="830" y="594">
                  The sources for the poster are available under: <a xlink:href="http://svn.apache.org/repos/asf/xmlgraphics/fop/trunk/src/documentation/poster">http://svn.apache.org/repos/asf/xmlgraphics/fop/trunk/src/documentation/poster</a>
                </tspan>
              </text>
            </g>
          </svg>
          </fo:instream-foreign-object>
	    </fo:block>
	  </fo:block-container>
	</xsl:template>
	
	<xsl:template match="section">
	  <xsl:variable name="section-content" select="exslt:node-set(document(@href, .))"/>
      <fo:block 
          space-before.minimum="2mm" space-before.optimum="5mm" space-before.maximum="30mm" 
          space-before.conditionality="discard" space-after.conditionality="discard"
          space-after.minimum="2mm" space-after.optimum="5mm" space-after.maximum="30mm"
          keep-together.within-column="always">
	  <fo:block-container width="{@width}mm" height="{@height}mm" space-after="-{@height}mm">
	    <fo:block line-height="0">
	        <fo:instream-foreign-object overflow="visible">
	          <xsl:call-template name="section-box">
              <xsl:with-param name="width" select="@width"/>
              <xsl:with-param name="height" select="@height"/>
	            <xsl:with-param name="tab-width" select="$section-content/section/title/@tab-width"/>
	            <xsl:with-param name="title" select="$section-content/section/title"/>
	          </xsl:call-template>
	        </fo:instream-foreign-object>
	    </fo:block>
	  </fo:block-container>
	  <fo:block-container width="{@width}mm - 8mm" height="{@height}mm - 24mm"
	    padding="4mm" padding-top="16mm" padding-bottom="8mm" margin="0pt"
	      display-align="distribute" id="section{position()}">
	    <fo:block>
	      <xsl:apply-templates select="$section-content/section/*[local-name() != 'title']"/>
	    </fo:block>
	  </fo:block-container>
      </fo:block>
	</xsl:template>
	
	<xsl:template name="section-box">
	  <xsl:param name="width" select="200"/>
	  <xsl:param name="height" select="100"/>
	  <xsl:param name="tab-width" select="35"/>
	  <xsl:param name="title" select="'Title'"/>
	  <xsl:variable name="stroke-width" select="2"/>
	  <xsl:variable name="offset" select="$stroke-width div 2"/>
	  <xsl:variable name="w" select="$width - $stroke-width"/>
	  <xsl:variable name="h" select="$height - $stroke-width"/>
      <svg xmlns="http://www.w3.org/2000/svg" width="{$width}mm" height="{$height}mm" viewBox="0 0 {$width} {$height}">
	    <g transform="translate({$offset},{$offset})" font-family="Verdana">
		  <path d="M 0,5 S 0,0 5,0 L {$tab-width},0 C {$tab-width + 10},0 {$tab-width + 10},10 {$tab-width + 20},10 L {$w - 5},10 S {$w},10 {$w},15 L {$w},{$h - 5} S {$w},{$h}, {$w - 5},{$h} L 5,{$h} S 0,{$h} 0,{$h - 5} z"
		    	style="fill:rgb(240,240,255); stroke:blue; stroke-width:{$stroke-width};" />
	       <text x="4" y="9" style="fill:blue; font-size:7">
	         <xsl:value-of select="$title"/>
	       </text>
	    </g>
      </svg>
	</xsl:template>
	
</xsl:stylesheet>