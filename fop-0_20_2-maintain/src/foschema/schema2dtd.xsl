<?xml version="1.0"?>

<xsl:stylesheet saxon:trace="no"
		version="1.1"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:saxon="http://icl.com/saxon"
		xmlns:xs = "http://www.w3.org/2001/XMLSchema"
		xmlns:fo="http://www.w3.org/1999/XSL/Format"
		xmlns:date="http://exslt.org/dates-and-times"
		xmlns:math="http://exslt.org/math"
		extension-element-prefixes="saxon date math"
		exclude-result-prefixes="fo">

<xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/xs:schema">
<xsl:text>
</xsl:text>
<xsl:comment> 
 This DTD has been developed in order to validate XSL FO documents 
 The namespace prefix is xmlns:fo="http://www.w3.org/1999/XSL/Format".
 
 In addition, the functionality implemented or not in FOP has been segregated
 To create an FOP only validating DTD, remove the references marked %\([a-zA-Z_]+\)_Not;
 I certainly have not exhaustively removed all of the properties not implemented in FOP. 
 If you notice an item that is incorrectly included or excluded, please send me a note
 
 The FOP only DTD will not guard against the entering of attribute values not implemented by FOP
 (Such as fo:leader-pattern="use-content")
 See http://xml.apache.org/fop/implemented.html for more detailed restrictions
 
 It has not been well tested. 
 For instance, the length attribute is able to be negative for some elements like margins.
 I have not represented that here.
 I have not added values for the Aural properties
 There are several instances where I've entered %integer_Type; and it should be positive-integer or number
 The DTD trys to handle the text based rules re: fo:markers, fo:float, footer and fo:initial-property-set
 But, allows you to do illegal things if you want because I couldn't figure out how to constrain against the illegal actions.
 
 Please e-mail your comments to cpaussa@myrealbox.com

</xsl:comment><xsl:text>
</xsl:text><xsl:comment> *************************************************************** </xsl:comment><xsl:text>
</xsl:text><xsl:comment> Entity definitions for groups of formatting objects             </xsl:comment><xsl:text>
</xsl:text><xsl:comment> *************************************************************** </xsl:comment><xsl:text>
</xsl:text>
	<xsl:apply-templates select="./xs:simpleType"/>
<xsl:text>
</xsl:text><xsl:comment> *************************************************************** </xsl:comment><xsl:text>
</xsl:text><xsl:comment> Attribute Groups                                                </xsl:comment><xsl:text>
</xsl:text><xsl:comment> *************************************************************** </xsl:comment>
	<xsl:apply-templates select="./xs:attributeGroup"/>
<xsl:text>
</xsl:text><xsl:comment> *************************************************************** </xsl:comment><xsl:text>
</xsl:text><xsl:comment> Element Groups                                                  </xsl:comment><xsl:text>
</xsl:text><xsl:comment> *************************************************************** </xsl:comment><xsl:text>
</xsl:text>
	<xsl:apply-templates select="./xs:group"/>
<xsl:text>
</xsl:text><xsl:comment> *************************************************************** </xsl:comment><xsl:text>
</xsl:text><xsl:comment> Elements                                                        </xsl:comment><xsl:text>
</xsl:text><xsl:comment> *************************************************************** </xsl:comment><xsl:text>
</xsl:text>
	<xsl:apply-templates select="./xs:element"/>
</xsl:template>

<xsl:template match="xs:group">
	<xsl:text disable-output-escaping="yes">
&lt;!ENTITY % </xsl:text><xsl:value-of select="./@name"/><xsl:text> "</xsl:text> 
	<xsl:for-each select="./xs:choice/xs:element">
		<xsl:text>
	</xsl:text>
		<xsl:value-of select="./@ref"/>
		<xsl:if test="position() != last()">
			<xsl:text> |</xsl:text>
		</xsl:if>
	</xsl:for-each>
	<xsl:if test="./xs:choice/xs:group">
		<xsl:variable name="ref1">
			<xsl:call-template name="strip_fo">
				<xsl:with-param name="ref" select="./xs:choice/xs:group[1]/@ref"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="ref2">
			<xsl:call-template name="strip_fo">
				<xsl:with-param name="ref" select="./xs:choice/xs:group[2]/@ref"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:text>
	%</xsl:text>
		<xsl:value-of select="$ref1"/>
		<xsl:text>;</xsl:text>
		<xsl:if test="(/xs:schema/xs:group[@name = $ref1]/xs:choice/xs:element) and (/xs:schema/xs:group[@name = $ref2]/xs:choice/xs:element)">
			<xsl:text>| </xsl:text>
		</xsl:if>
		<xsl:text>
	%</xsl:text>
		<xsl:value-of select="$ref2"/><xsl:text>;</xsl:text>
	</xsl:if>
	<xsl:text disable-output-escaping="yes">
"&gt;</xsl:text>
</xsl:template>

<xsl:template match="xs:attributeGroup">
	<xsl:text disable-output-escaping="yes">
&lt;!ENTITY % </xsl:text><xsl:value-of select="./@name"/><xsl:text> "</xsl:text>
	<xsl:for-each select="./xs:attribute">
		<xsl:variable name="name">
			<xsl:call-template name="strip_fo">
				<xsl:with-param name="ref" select="./@name"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="type">
			<xsl:call-template name="strip_fo">
				<xsl:with-param name="ref" select="./@type"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:text>
	</xsl:text>
		<xsl:value-of select="$name"/>
		<xsl:choose>
			<xsl:when test="$type = 'xs:string'">
				<xsl:text> CDATA</xsl:text>
			</xsl:when>
			<xsl:when test="/xs:schema/xs:simpleType[@name = $type]/xs:restriction/xs:enumeration">
				<!--This item is a directly enumerated type-->
				<xsl:text> (%</xsl:text>
				<xsl:value-of select="$type"/>
				<xsl:text>;)</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text> CDATA</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text> #IMPLIED</xsl:text>
	</xsl:for-each>
	<xsl:for-each select="./xs:attributeGroup">
		<xsl:variable name="ref">
			<xsl:call-template name="strip_fo">
				<xsl:with-param name="ref" select="./@ref"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:text>
	%</xsl:text><xsl:value-of select="$ref"/><xsl:text>;</xsl:text>
	</xsl:for-each>
	<xsl:text disable-output-escaping="yes">
	"&gt;</xsl:text>
</xsl:template>

<xsl:template match="xs:simpleType">
	<xsl:if test="./xs:restriction/xs:enumeration">
		<xsl:text disable-output-escaping="yes">
&lt;!ENTITY % </xsl:text><xsl:value-of select="./@name"/><xsl:text> "</xsl:text>
		<xsl:for-each select="./xs:restriction/xs:enumeration">
			<xsl:value-of select="./@value"/>
			<xsl:if test="position() != last()">
				<xsl:text>|</xsl:text>
			</xsl:if>
		</xsl:for-each>
		<xsl:text disable-output-escaping="yes">"&gt;</xsl:text>
	</xsl:if>
</xsl:template>

<xsl:template match="xs:element">
	<xsl:text disable-output-escaping="yes">&lt;!ELEMENT fo:</xsl:text>
	<xsl:value-of select="./@name"/>
	<xsl:choose>
		<xsl:when test="( not(./xs:complexType/xs:sequence) and not(./xs:complexType/xs:choice) )">
			<xsl:text> EMPTY</xsl:text>
		</xsl:when>
		<xsl:otherwise>
			<xsl:text> (</xsl:text>
			<xsl:if test="./xs:complexType/@mixed = 'true'">
				<xsl:text>#PCDATA|</xsl:text>
			</xsl:if>
			<xsl:for-each select="./xs:complexType/xs:sequence">
				<xsl:for-each select="./xs:element">
					<xsl:value-of select="./@ref"/>
					<xsl:call-template name="addPlus"/>
					<xsl:if test="position() != last()">
						<xsl:text>,</xsl:text>
					</xsl:if>
				</xsl:for-each>
				<xsl:for-each select="./xs:group">
					<xsl:text>%</xsl:text>
					<xsl:variable name="ref">
						<xsl:call-template name="strip_fo">
							<xsl:with-param name="ref" select="./@ref"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:value-of select="$ref"/>
					<xsl:text>;</xsl:text>
					<xsl:call-template name="addPlus"/>
					<xsl:if test="position() != last()">
						<xsl:text>,</xsl:text>
					</xsl:if>
				</xsl:for-each>
			</xsl:for-each>
			<xsl:for-each select="./xs:complexType/xs:choice">
				<xsl:for-each select="./xs:element">
					<xsl:value-of select="./@ref"/>
					<xsl:call-template name="addPlus"/>
					<xsl:if test="position() != last()">
						<xsl:text>|</xsl:text>
					</xsl:if>
				</xsl:for-each>
				<xsl:for-each select="./xs:group">
					<xsl:text>%</xsl:text>
					<xsl:variable name="ref">
						<xsl:call-template name="strip_fo">
							<xsl:with-param name="ref" select="./@ref"/>
						</xsl:call-template>
					</xsl:variable>
					<xsl:value-of select="$ref"/>
					<xsl:text>;</xsl:text>
					<xsl:call-template name="addPlus"/>
					<xsl:if test="position() != last()">
						<xsl:text>|</xsl:text>
					</xsl:if>
				</xsl:for-each>
			</xsl:for-each>
			<xsl:text>)</xsl:text>
		</xsl:otherwise>
	</xsl:choose>
	<xsl:for-each select="./xs:complexType/xs:sequence">
		<xsl:call-template name="addPlus"/>
	</xsl:for-each>
	<xsl:for-each select="./xs:complexType/xs:choice">
		<xsl:call-template name="addPlus"/>
	</xsl:for-each>
	<xsl:text disable-output-escaping="yes">&gt;
&lt;!ATTLIST fo:</xsl:text>
	<xsl:value-of select="./@name"/>
	<xsl:for-each select="./xs:complexType/xs:attribute">
		<xsl:variable name="name">
			<xsl:call-template name="strip_fo">
				<xsl:with-param name="ref" select="./@name"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="type">
			<xsl:call-template name="strip_fo">
				<xsl:with-param name="ref" select="./@type"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:text>
	</xsl:text>
		<xsl:value-of select="$name"/>
		<xsl:choose>
			<xsl:when test="$type = 'xs:string'">
				<xsl:text> CDATA</xsl:text>
			</xsl:when>
			<xsl:when test="/xs:schema/xs:simpleType[@name = $type]/xs:restriction/xs:enumeration">
				<!--This item is a directly enumerated type-->
				<xsl:text> (%</xsl:text>
				<xsl:value-of select="$type"/>
				<xsl:text>;)</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text> CDATA</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="@use = 'required'">
				<xsl:text> #REQUIRED</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text> #IMPLIED</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:for-each>
	<xsl:for-each select="./xs:complexType/xs:attributeGroup">
		<xsl:variable name="ref">
			<xsl:call-template name="strip_fo">
				<xsl:with-param name="ref" select="./@ref"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:text>
	%</xsl:text>
		<xsl:value-of select="$ref"/>
		<xsl:text>;</xsl:text>
	</xsl:for-each>
	<xsl:text disable-output-escaping="yes">
&gt;
</xsl:text>
</xsl:template>

<xsl:template name="addPlus">
	<xsl:choose>
		<xsl:when test="(@minOccurs = 0) and (@maxOccurs = 'unbounded')">
			<xsl:text>*</xsl:text>
		</xsl:when>
		<xsl:when test="(@minOccurs = 0) and ((@maxOccurs = 1) or not(@maxOccurs))">
			<xsl:text>?</xsl:text>
		</xsl:when>
		<xsl:when test="((@minOccurs = 1) or not(@minOccurs)) and (@maxOccurs = 'unbounded')">
			<xsl:text>+</xsl:text>
		</xsl:when>
		<xsl:when test="not(@minOccurs) and not(@maxOccurs)">
		</xsl:when>
		<xsl:otherwise>
			<xsl:text>!error!</xsl:text>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="strip_fo">
	<xsl:param name="ref"/>
	<xsl:choose>
		<xsl:when test="substring($ref,1,3) = 'fo:'">
			<xsl:value-of select="substring($ref,4)"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="$ref"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

</xsl:stylesheet>