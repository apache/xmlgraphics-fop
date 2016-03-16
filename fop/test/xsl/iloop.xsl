<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:fo="http://www.w3.org/1999/XSL/Format">
  <xsl:template match="/">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="all" page-height="29.7cm" page-width="21cm" margin-top="1cm" margin-bottom="0.3cm" margin-left="1cm" margin-right="1cm">
          <fo:region-body margin-top="0cm" margin-right="0cm" margin-bottom="1cm" margin-left="0cm" region-name="doesntexist" />

        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="all">

        <fo:flow flow-name="xsl-region-body">
          <fo:block>
            <xsl:apply-templates select="//region[@name='Main Region']" />
          </fo:block>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>

  </xsl:template>


</xsl:stylesheet>