<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lxslt="http://xml.apache.org/xslt"
                xmlns:redirect="org.apache.xalan.xslt.extensions.Redirect"
                extension-element-prefixes="redirect">
<xsl:output method="text" />

<xsl:template match="font-mappings">
<!-- <redirect:write file="org/apache/fop/render/pdf/CodePointMapping.java"> -->
package org.apache.fop.render.pdf;

public class CodePointMapping {
        public static char[] map;

        static {
                map = new char[65536];
<xsl:for-each select="map[@unicode!='-1' and @win-ansi!='-1']">             map[<xsl:value-of select="@unicode"/>] = <xsl:value-of select="@win-ansi"/>;
</xsl:for-each>
        }
}
<!-- </redirect:write>-->
</xsl:template>
</xsl:stylesheet>

