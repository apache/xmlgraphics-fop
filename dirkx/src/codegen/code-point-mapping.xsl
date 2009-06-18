<transform xmlns="http://www.w3.org/1999/XSL/Transform"
 xmlns:xt="http://www.jclark.com/xt" extension-element-prefixes="xt"
 version="1.0">
<template match="font-mappings">
<xt:document href="org/apache/xml/fop/render/pdf/CodePointMapping.java">
package org.apache.xml.fop.render.pdf;

public class CodePointMapping {
	public static char[] map;

	static {
		map = new char[65536];
<for-each select="map[@unicode!='-1' and @win-ansi!='-1']">		map[<value-of select="@unicode"/>] = <value-of select="@win-ansi"/>;
</for-each>
	}
}
</xt:document>
</template>
</transform>
