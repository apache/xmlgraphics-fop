<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >

<xsl:key name="genericref" match="property[@type='generic']" use="class-name"/>
<xsl:key name="shorthandref" match="property" use="name"/>

<xsl:template name="capfirst">
  <xsl:param name="str"/>
  <xsl:variable name="lcletters" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="ucletters" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
     <xsl:value-of select="concat(translate(substring($str, 1, 1),
	            $lcletters, $ucletters), substring($str, 2))"/>
</xsl:template>

<xsl:template name="makeClassName">
  <xsl:param name="propstr"/>
  <xsl:choose>
   <xsl:when test="contains($propstr, '-')">
    <xsl:call-template name="capfirst">
      <xsl:with-param name="str" select="substring-before($propstr, '-')"/>
    </xsl:call-template>
    <xsl:call-template name="makeClassName">
      <xsl:with-param name="propstr" select="substring-after($propstr, '-')"/>
    </xsl:call-template>
   </xsl:when>
   <xsl:otherwise>
    <xsl:call-template name="capfirst">
      <xsl:with-param name="str" select="$propstr"/>
    </xsl:call-template>
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- The name of the subclass of Property to be created -->
<xsl:template name="propclass">
  <xsl:param name="prop" select="."/>
  <xsl:choose>
    <xsl:when test="$prop/datatype">
      <xsl:value-of select="$prop/datatype"/><xsl:text>Property</xsl:text>
    </xsl:when>
    <xsl:when test="$prop/use-generic[@ispropclass='true']">
      <xsl:value-of select="$prop/use-generic"/>
    </xsl:when>
    <xsl:when test="$prop/use-generic">
      <!-- If no datatype child, then the prop must use the same datatype as
           its template. -->
	<xsl:call-template name="propclass">
	  <xsl:with-param name="prop"
	   select="key('genericref', $prop/use-generic)"/>
        </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <!-- ERROR -->
      <xsl:message terminate="yes">
	No datatype found for property: <xsl:value-of select="$prop/name"/>
      </xsl:message>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- return a boolean value -->
<xsl:template name="hasEnum">
  <xsl:param name="prop" select="."/>
  <xsl:choose>
    <xsl:when test="$prop/enumeration">true</xsl:when>
    <xsl:when test="$prop/use-generic">
      <!-- If no datatype child, then the prop must use the same datatype as
           its template. -->
	<xsl:call-template name="hasEnum">
	  <xsl:with-param name="prop"
	   select="key('genericref', $prop/use-generic)"/>
        </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>false</xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- return a boolean value -->
<xsl:template name="hasSubpropEnum">
  <xsl:param name="prop" select="."/>
  <xsl:choose>
    <xsl:when test="$prop/compound/subproperty/enumeration">true</xsl:when>
    <xsl:when test="$prop/use-generic">
      <!-- If no datatype child, then the prop must use the same datatype as
           its template. -->
	<xsl:call-template name="hasSubpropEnum">
	  <xsl:with-param name="prop"
	   select="key('genericref', $prop/use-generic)"/>
        </xsl:call-template>
    </xsl:when>
    <xsl:when test="$prop/compound/subproperty/use-generic">
	<xsl:for-each select="$prop/compound/subproperty[use-generic]">
	  <xsl:call-template name="hasEnum">
	    <xsl:with-param name="prop"
	   	select="key('genericref', use-generic)"/>
          </xsl:call-template>
	</xsl:for-each>
    </xsl:when>
    <xsl:otherwise>false</xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
