<?xml version="1.0" encoding="UTF-8"?>

<!-- $Id$ -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:preserve-space elements="*"/>

<xsl:template match="/compliance">
    <document>
  <xsl:apply-templates select="head"/>
  <xsl:apply-templates select="body"/>
    </document>
</xsl:template>

<xsl:template match="head">
      <header>
  <title>
  <xsl:value-of select="/compliance/head/title"/>
  </title>
      </header>
</xsl:template>

<xsl:template match="body">
  <body>
  <xsl:apply-templates select="standard"/>
  </body>
</xsl:template>

<xsl:template match="standard">
  <section>
  <title><xsl:value-of select="@name"/></title>
  <xsl:apply-templates select="explanatory"/>
  <xsl:apply-templates select="level-1"/>
  </section>
</xsl:template>

<xsl:template match="level-1">
  <title><xsl:value-of select="@name"/></title>
  <xsl:apply-templates select="explanatory"/>
  <table>
  <tr>
    <th rowspan="2">
      <p><xsl:value-of select="@compliance-item-desc"/></p>
    </th>
    <th colspan="3">
      Support
    </th>
    <th rowspan="2">
      Comments
    </th>
  </tr>
  <tr>
    <th>
      <xsl:value-of select="/compliance/body/standard/@compliance-level-1-desc"/>
    </th>
    <th>
      <xsl:value-of select="/compliance/body/standard/@compliance-level-2-desc"/>
    </th>
    <th>
      <xsl:value-of select="/compliance/body/standard/@compliance-level-3-desc"/>
    </th>
  </tr>
  <xsl:apply-templates select="level-2"/>
  </table>
</xsl:template>

<xsl:template match="explanatory">
  <xsl:for-each select="p">
    <p>
      <xsl:apply-templates/>
    </p>
  </xsl:for-each>
</xsl:template>

<xsl:template match="level-2">
  <tr>
    <td colspan="5" class="category">
    <xsl:value-of select="@name"/>
    </td>
  </tr>
  <xsl:apply-templates select="level-3"/>
</xsl:template>

<xsl:template match="level-3">
  <tr>
    <td>
      <xsl:value-of select="@name"/>
    </td>
    <td>
      <xsl:attribute name="class">
        <xsl:choose>
          <xsl:when test="@comply='yes'">
            <xsl:text>yes</xsl:text>
          </xsl:when>
          <xsl:when test="@comply='no'">
            <xsl:choose>
              <xsl:when test="@compliance-level > 1">
                <xsl:text>yes</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>no</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="@comply='partial'">
            <xsl:choose>
              <xsl:when test="@compliance-level > 1">
                <xsl:text>yes</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>partial</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text></xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:choose>
        <xsl:when test="@compliance-level=1">
          <xsl:value-of select="@comply"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>.</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </td>
    <td>
      <xsl:attribute name="class">
        <xsl:choose>
          <xsl:when test="@comply='yes'">
            <xsl:text>yes</xsl:text>
          </xsl:when>
          <xsl:when test="@comply='no'">
            <xsl:choose>
              <xsl:when test="@compliance-level > 2">
                <xsl:text>yes</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>no</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="@comply='partial'">
            <xsl:choose>
              <xsl:when test="@compliance-level > 2">
                <xsl:text>yes</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>partial</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text></xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:choose>
        <xsl:when test="@compliance-level=2">
          <xsl:value-of select="@comply"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>.</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </td>
    <td>
      <xsl:attribute name="class">
        <xsl:choose>
          <xsl:when test="@comply='yes'">
            <xsl:text>yes</xsl:text>
          </xsl:when>
          <xsl:when test="@comply='no'">
            <xsl:choose>
              <xsl:when test="@compliance-level > 3">
                <xsl:text>yes</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>no</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:when test="@comply='partial'">
            <xsl:choose>
              <xsl:when test="@compliance-level > 3">
                <xsl:text>yes</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>partial</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text></xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:choose>
        <xsl:when test="@compliance-level=3">
          <xsl:value-of select="@comply"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>.</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </td>
    <xsl:choose>
      <xsl:when test="count(comment) > 0">
        <td align="left">
          <ul>
            <xsl:for-each select="comment">
              <li>
                <xsl:value-of select="."/>
             </li>
           </xsl:for-each>
          </ul>
        </td>
      </xsl:when>
      <xsl:otherwise>
        <td align="center">
          <xsl:text>.</xsl:text>
        </td>
      </xsl:otherwise>
    </xsl:choose>
  </tr>
</xsl:template>

</xsl:stylesheet>

