<?xml version="1.0"?>
<!--    XSLT stylesheet: QAML -> FO  (QAML = FAQ Markup Language) 
        version: 1.00ß
-->

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
     xmlns:fo="http://www.w3.org/1999/XSL/Format"
     result-ns="fo" indent-result="yes">

   <xsl:output indent="yes"/>

<xsl:template match ="/">
	<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

		<!-- defines page layout -->
		<fo:layout-master-set>

			<fo:simple-page-master page-master-name="first"
										height="29.7cm" 
										width="21cm"
										margin-top="1cm" 
										margin-bottom="2cm" 
										margin-left="2.5cm" 
										margin-right="2.5cm">
				<fo:region-before extent="3cm"/>
				<fo:region-body margin-top="3cm"/>
				<fo:region-after extent="1.5cm"/>
			</fo:simple-page-master>

			<fo:simple-page-master page-master-name="rest"
										height="29.7cm" 
										width="21cm"
										margin-top="1cm" 
										margin-bottom="2cm" 
										margin-left="2.5cm" 
										margin-right="2.5cm">
				<fo:region-before extent="2.5cm"/>
				<fo:region-body margin-top="2.5cm"/>
				<fo:region-after extent="1.5cm"/>
			</fo:simple-page-master>

		</fo:layout-master-set>

		<fo:page-sequence>
			<fo:sequence-specification>
			<fo:sequence-specifier-repeating page-master-first="first" 
														page-master-repeating="rest"/>
			</fo:sequence-specification>

			<fo:static-content flow-name="xsl-before">
				<fo:block text-align="end" 
							font-size="10pt" 
							font-family="serif" 
							line-height="14pt" >
					FOP - p. <fo:page-number/>
				</fo:block>
			</fo:static-content> 

			<fo:flow flow-name="xsl-body">
			<xsl:apply-templates select="faq"/> 
			</fo:flow>

		</fo:page-sequence>
	</fo:root>
</xsl:template>

<!-- don't output head -->
<xsl:template match ="head">
</xsl:template>


<!-- body -->
<xsl:template match ="body">

<!-- insert logo  - doesn't work yet -->
<!--<fo:display-graphic href="logo.bmp"/>-->

<!-- defines text title -->
	<fo:block font-size="18pt" 
				font-family="sans-serif" 
				line-height="24pt"
				space-after.optimum="15pt"
				background-color="blue"
				color="white"
				text-align="centered"
				padding-top="3pt">
		<xsl:value-of select="//head/title"/>
	</fo:block>

<!-- generates table of contents -->

	<fo:block font-size="10pt" 
				font-family="serif" 
				line-height="10pt"
				space-after.optimum="10pt"
				start-indent="15pt">
			<fo:block font-size="10pt" 
						font-family="serif" 
						line-height="10pt"
						space-after.optimum="3pt"
						font-weight="bold">
				Content
			</fo:block>
		<xsl:for-each select="section[@class='level1']/title">
			<fo:block font-size="10pt" 
						font-family="serif" 
						line-height="10pt"
						space-after.optimum="3pt">
				<xsl:number value="position()" format="A"/>) 
				<xsl:value-of select="."/>
			</fo:block>
		</xsl:for-each>
	</fo:block>

<!-- processing of the rest -->
<xsl:apply-templates/>

</xsl:template> 
<!-- end body -->

<!-- title level1, numbering is generated -->
<xsl:template match ="section[@class='level1']/title">
	<fo:block font-size="18pt" 
				font-family="serif" 
				line-height="20pt"
				space-before.optimum="20pt" 
				space-after.optimum="14pt"
				keep-with-next="true">
		<xsl:number count="section[@class='level1']" format="A"/>) 
		<xsl:apply-templates/>
	</fo:block>
</xsl:template>

<!-- title level2 -->
<xsl:template match ="section[@class='level2']/title">
	<fo:block font-size="16pt" 
				font-family="serif" 
				line-height="18pt"
				space-before.optimum="8pt" 
				space-after.optimum="8pt"
				keep-with-next="true">
		<xsl:apply-templates/>
	</fo:block>
</xsl:template>

<!-- title level3 -->
<xsl:template match ="section[@class='level3']/title">
	<fo:block font-size="14pt" 
				font-family="serif" 
				line-height="16pt"
				space-before.optimum="8pt" 
				space-after.optimum="4pt"
				keep-with-next="true">
		<xsl:apply-templates/>
	</fo:block>
</xsl:template>

<!-- section -->
<xsl:template match ="section">
	<xsl:apply-templates/>
</xsl:template>

<!-- question group -->
<xsl:template match ="qna">
	<xsl:apply-templates/>
</xsl:template>

<!-- question  -->
<xsl:template match ="q">
	<fo:block start-indent="0.7cm"
				space-before.optimum="12pt"> 
		<xsl:value-of select="."/>
	</fo:block>
</xsl:template>

<!-- para in answer -->
<xsl:template match ="a/p">
	<fo:block  start-indent="0.7cm"> 
		<xsl:apply-templates/>
	</fo:block>
</xsl:template>

<!-- list -->
<xsl:template match ="div[@class='list']">
	<fo:list-block start-indent="1cm"
						provisional-distance-between-starts="12pt"
						font-family="serif">
		<xsl:apply-templates/>
	</fo:list-block>
</xsl:template>

<!-- list items  -->
<xsl:template match ="div/p[@class='li']">
	<fo:list-item >
		<fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
		<fo:list-item-body>
			<fo:block>
				<xsl:apply-templates/>
			</fo:block>
		</fo:list-item-body>
	</fo:list-item>
</xsl:template>


<!-- code fragments, marked by attribute pre -->
<xsl:template match ="section/p[@class='pre']">
	<fo:block font-size="10pt" 
				font-family="monospace" 
				line-height="12pt"
				space-before.optimum="3pt" 
				space-after.optimum="3pt">
		<xsl:apply-templates/>
	</fo:block>
</xsl:template>

<!-- normal paras without class attribute --> 
<xsl:template match ="section/p[not(@class)]">
	<fo:block space-after.optimum="3pt"
				font-family="serif"> 
		<xsl:apply-templates/>
	</fo:block>
</xsl:template>

<!-- link -->
<xsl:template match ="link">
	<fo:inline-sequence font-style="italic" 
							  font-family="serif">
		<xsl:apply-templates/> 
	</fo:inline-sequence>
	<fo:inline-sequence font-family="serif">
		(<xsl:value-of select="@href"/>)
	</fo:inline-sequence>
</xsl:template>

</xsl:stylesheet>
