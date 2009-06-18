<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:svg="http://www.w3.org/TR/2000/WD-SVG-20000629/DTD/svg-20000629.dtd">
	<xsl:output method="xml" indent="yes"/>
	<xsl:template match="Document">
		<fo:root>
			<fo:layout-master-set>
				<fo:simple-page-master master-name="one" page-height="29.7cm" page-width="21cm" margin-top="0.5cm" margin-bottom="0.5cm" margin-left="1.5cm" margin-right="1.5cm">
					<fo:region-before extent="1.5cm"/>
					<fo:region-body margin-top="1.5cm" margin-bottom="2cm"/>
					<fo:region-after extent="1.5cm"/>
				</fo:simple-page-master>
				<fo:simple-page-master master-name="front" page-height="29.7cm" page-width="21cm" margin-top="0.5cm" margin-bottom="0.5cm" margin-left="6cm" margin-right="1.5cm">
					<fo:region-before extent="1.5cm"/>
					<fo:region-body margin-top="1.5cm" margin-bottom="2cm"/>
					<fo:region-after extent="1.5cm"/>
				</fo:simple-page-master>
			</fo:layout-master-set>
			<fo:page-sequence master-name="front">
				<fo:flow font-size="14pt" line-height="14pt">
		            <fo:block-container height="20cm" width="6cm" top="2cm" left="-1cm" position="absolute">
						<fo:block>
							<fo:instream-foreign-object>
							    <svg:svg width="20" height="19.5cm">
							        <svg:line style="stroke-width:1.5" x1="10" y1="0" x2="10" y2="20cm"/>
							    </svg:svg>
							</fo:instream-foreign-object>
						</fo:block>
		            </fo:block-container>

		            <fo:block-container height="2cm" width="6cm" top="21.5cm" left="-5.3cm" position="absolute">
						<fo:block>
						</fo:block>
		            </fo:block-container>

					<fo:block font-weight="bold" font-size="26pt" line-height="28pt" space-before.optimum="50pt" space-after.optimum="10pt">
						SVG in FOP
					</fo:block>

					<fo:block font-weight="bold" font-size="22pt" line-height="22pt" space-after.optimum="70pt">
						<xsl:apply-templates select="Title"/>
					</fo:block>
					<fo:block font-weight="bold" font-size="12pt" line-height="12pt" space-after.optimum="70pt">
						<xsl:apply-templates select="Description"/>
					</fo:block>
				</fo:flow>
			</fo:page-sequence>
			<fo:page-sequence master-name="one">
				<fo:static-content flow-name="xsl-region-before">
					<fo:block-container height="1cm" width="6cm" top="0.2cm" left="0cm" position="absolute">
						<fo:block text-align="start" font-size="10pt" font-family="serif" line-height="12pt">
						</fo:block>
						<fo:block text-align="start" font-size="10pt" font-family="serif" line-height="12pt">
						</fo:block>
		            </fo:block-container>
		            <fo:block-container height="2cm" width="6cm" top="-0.1cm" left="5.8cm" position="absolute">
						<fo:block>
						</fo:block>
		            </fo:block-container>
		            <fo:block-container height="1cm" width="2cm" top="0.2cm" left="15cm" position="absolute">
						<fo:block text-align="end" font-size="10pt" font-family="serif" line-height="14pt">
							SVG in FOP
						</fo:block>
		            </fo:block-container>
					<fo:block-container height="1cm" width="17cm" top="1.1cm" left="0cm" position="absolute">
						<fo:leader leader-pattern="rule" space-before.optimum="0pt" space-after.optimum="0pt"/>
		            </fo:block-container>
				</fo:static-content>
				<fo:static-content flow-name="xsl-region-after">
					<fo:block-container height="1cm" width="17cm" top="0cm" left="0cm" position="absolute">
						<fo:leader leader-pattern="rule" space-before.optimum="0pt" space-after.optimum="0pt"/>
		            </fo:block-container>
	    	        <fo:block-container height="2cm" width="6cm" top="0.1cm" left="0cm" position="absolute">
						<fo:block font-size="10pt" font-family="serif" line-height="12pt">
							<xsl:apply-templates select="Title"/>
						</fo:block>
						<fo:block font-size="10pt" font-family="serif" line-height="12pt">
						</fo:block>
	            	</fo:block-container>
			        <fo:block-container height="2cm" width="6cm" top="0.5cm" left="8cm" position="absolute">
						<fo:block font-size="10pt" font-family="serif" line-height="12pt">
							Apache XML
						</fo:block>
		            </fo:block-container>
    		        <fo:block-container height="1cm" width="2cm" top="0.5cm" left="15cm" position="absolute">
						<fo:block text-align="end" font-size="10pt" font-family="serif" line-height="12pt">
							Page <fo:page-number/>
						</fo:block>
            		</fo:block-container>
				</fo:static-content>
				<fo:flow font-size="10pt" line-height="10pt">
					<xsl:apply-templates select="Summary"/>
					<xsl:apply-templates select="ExternalChapter|Chapter"/>
				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>

	<xsl:key name="diagrams" match="Diagram" use="@entry"/>

	<xsl:template match="ExternalDiagram">
		<xsl:variable name="name" select="."/>

		<fo:block text-align="start" font-size="11pt" line-height="11pt">
			<fo:instream-foreign-object>
				<xsl:for-each select="document(@file)/Diagrams/Diagram">
					<xsl:if test="$name=@entry">
						<xsl:apply-templates/>
					</xsl:if>
				</xsl:for-each>
			</fo:instream-foreign-object>
		</fo:block>

		<xsl:if test="@showcode='true'">
    		<fo:block start-indent="-50pt" text-align="start" font-family="monospace" white-space-treatment="preserve" font-size="9pt" line-height="9pt">
<xsl:text disable-output-escaping="yes">&lt;</xsl:text>![CDATA[
	    		<xsl:for-each select="document(@file)/Diagrams/Diagram">
		    		<xsl:if test="$name=@entry">
			    		<xsl:apply-templates/>
				    </xsl:if>
    			</xsl:for-each>
]]<xsl:text disable-output-escaping="yes">&gt;</xsl:text>
	    	</fo:block>
	    </xsl:if>

<!--
		<fo:block font-size="12pt" line-height="12pt">
			<xsl:for-each select="document(@file)">
				<xsl:apply-templates select="key('diagrams', $name)"/>
			</xsl:for-each>
		</fo:block>
-->

	</xsl:template>

	<xsl:template match="ExternalSVG">
		<fo:block text-align="start" font-size="11pt" line-height="11pt">
			<fo:instream-foreign-object>
				<xsl:apply-templates select="document(@file)/svg:svg" mode="svg"/>
			</fo:instream-foreign-object>
		</fo:block>
	</xsl:template>

	<xsl:template match="ExternalChapter">
<!--
<xsl:message>Including File: <xsl:value-of select="@file"/></xsl:message>
-->
		<xsl:for-each select="document(@file)/Document">
			<fo:block break-before="page"/>
			<xsl:apply-templates select="Chapter|ExternalChapter"/>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="Chapter">
		<xsl:if test="not(position()=1)">
			<fo:block break-before="page"/>
		</xsl:if>
		<fo:leader leader-pattern="rule" rule-thickness="1.5pt" space-before.optimum="5pt" space-after.optimum="4pt"/>
		<fo:block font-weight="bold" font-size="18pt" line-height="18pt"><xsl:apply-templates select="Title"/></fo:block>
		<xsl:apply-templates select="ExternalDiagram|ExternalSVG"/>
		<xsl:apply-templates select="Body"/>
		<xsl:apply-templates select="Section"/>
	</xsl:template>

	<xsl:template match="@*|node()" mode="svg">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" mode="svg"/>
		</xsl:copy>
	</xsl:template>

<!-- note: this causes any node not otherwise defined to be copied -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="Summary">
		<fo:block break-before="page" font-size="16pt" line-height="16pt" space-before.optimum="12pt"><xsl:apply-templates select="Title"/></fo:block>

		<xsl:apply-templates select="ExternalDiagram|ExternalSVG"/>

		<xsl:apply-templates select="Body"/>
	</xsl:template>

	<xsl:template match="Section">
		<fo:leader leader-pattern="rule" space-before.optimum="8pt"/>
		<fo:block font-weight="bold" font-size="14pt" line-height="14pt" space-before.optimum="2pt"><xsl:apply-templates select="Title"/></fo:block>

		<xsl:apply-templates select="ExternalDiagram|ExternalSVG"/>

		<xsl:apply-templates select="Body"/>

		<xsl:apply-templates select="SubSection"/>
	</xsl:template>

	<xsl:template match="SubSection">
		<fo:leader leader-pattern="rule" space-before.optimum="6pt"/>
		<fo:block font-weight="bold" font-size="12pt" line-height="12pt" space-before.optimum="2pt"><xsl:apply-templates select="Title"/></fo:block>

		<xsl:apply-templates select="ExternalDiagram|ExternalSVG"/>

		<xsl:apply-templates select="Body"/>
	</xsl:template>

	<xsl:template match="Body">
		<xsl:apply-templates select="p"/>
	</xsl:template>

	<xsl:template match="p">
		<fo:block start-indent="5pt" font-size="10pt" line-height="11pt"  text-align="justify" space-before.optimum="4pt">
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>

	<xsl:template match="Title">
		<xsl:number level="multiple" count="Chapter|Section|SubSection" format="1.1 "/>
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="i">
		<fo:inline-sequence font-style="italic">
			<xsl:apply-templates/>
		</fo:inline-sequence>
	</xsl:template>

	<xsl:template match="b">
		<fo:inline-sequence font-weight="bold">
			<xsl:apply-templates/>
		</fo:inline-sequence>
	</xsl:template>

	<xsl:template match="a">
		<fo:inline-sequence color="blue">
			<xsl:apply-templates/>
		</fo:inline-sequence>
	</xsl:template>

	<xsl:template match="Description">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="index">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="code">
        <fo:block font-family="monospace" text-align="start" white-space-treatment="preserve">
			<xsl:apply-templates/>
        </fo:block>
	</xsl:template>

	<xsl:template match="ul">
      <fo:list-block>
		<xsl:apply-templates/>
      </fo:list-block>
	</xsl:template>

	<xsl:template match="li">
        <fo:list-item>
          <fo:list-item-label>
            <fo:block>&#x2022;</fo:block>
          </fo:list-item-label>
          <fo:list-item-body>
            <fo:block space-after.optimum="4pt">
        		<xsl:apply-templates/>
            </fo:block>
          </fo:list-item-body>
        </fo:list-item>
	</xsl:template>

	<xsl:template match="ol">
      <fo:list-block>
		<xsl:apply-templates/>
      </fo:list-block>
	</xsl:template>

	<xsl:template match="ol/li">
        <fo:list-item>
          <fo:list-item-label>
            <fo:block><xsl:number level="multiple" count="li" format="1. "/></fo:block>
          </fo:list-item-label>
          <fo:list-item-body>
            <fo:block space-after.optimum="4pt">
        		<xsl:apply-templates/>
            </fo:block>
          </fo:list-item-body>
        </fo:list-item>
	</xsl:template>

	<xsl:template match="Appendix//Title" priority="1">
		<xsl:number level="multiple" count="Appendix|Section|SubSection" format="A.1 "/>
		<xsl:apply-templates/>
	</xsl:template>
</xsl:stylesheet>
