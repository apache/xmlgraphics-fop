<!-- $Id$
============================================================================
                   The Apache Software License, Version 1.1
============================================================================

Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

Redistribution and use in source and binary forms, with or without modifica-
tion, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice,
   this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

3. The end-user documentation included with the redistribution, if any, must
   include the following acknowledgment: "This product includes software
   developed by the Apache Software Foundation (http://www.apache.org/)."
   Alternately, this acknowledgment may appear in the software itself, if
   and wherever such third-party acknowledgments normally appear.

4. The names "FOP" and "Apache Software Foundation" must not be used to
   endorse or promote products derived from this software without prior
   written permission. For written permission, please contact
   apache@apache.org.

5. Products derived from this software may not be called "Apache", nor may
   "Apache" appear in their name, without prior written permission of the
   Apache Software Foundation.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
============================================================================

This software consists of voluntary contributions made by many individuals
on behalf of the Apache Software Foundation and was originally created by
James Tauber <jtauber@jtauber.com>. For more information on the Apache
Software Foundation, please see <http://www.apache.org/>.
--> 
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text" />

<xsl:include href="propinc.xsl"/>

<xsl:template match="root">
  <xsl:text>
package org.apache.fop.fo;
import java.util.BitSet;

public class PropertySets {
    public static short[][] mapping = null;

    public static void initialize() {
        mapping = new short[Constants.ELEMENT_COUNT][];
</xsl:text>
  <xsl:apply-templates/>
        <xsl:text>
        boolean loop = true;
        while (loop) {
            loop = false;
</xsl:text>
<xsl:apply-templates mode="content"/>
       }
<xsl:apply-templates mode="mapping"/>
  <xsl:text>
    }

    private static short[] makeSparseIndices(BitSet set) {
        short[] indices = new short[Constants.PROPERTY_COUNT];
        indices[0] = (short) (set.cardinality() + 1);
        int j = 1;
        for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i+1)) {
            indices[i] = (short) j++;
        }
        return indices;
    }

    private static boolean mergeContent(BitSet node, BitSet content,
                                         boolean loop)
    {
        int c = node.cardinality();
        node.or(content);
        if (c == node.cardinality())
            return loop;
        return true;
    }

    public static short[] getPropertySet(int elementId) {
        if (mapping == null)
            initialize();
        return mapping[elementId];
    }
}
  </xsl:text>
</xsl:template>

<xsl:template match="common">
  <xsl:variable name="name" select="name"/>
  <xsl:text>
        BitSet </xsl:text><xsl:value-of select="$name"/>
  <xsl:text> = new BitSet();
</xsl:text>
  <xsl:apply-templates select="property">
    <xsl:with-param name="setname" select="$name"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="common/property">
  <xsl:param name="setname"/>

  <xsl:text>        </xsl:text>
  <xsl:value-of select="../name"/><xsl:text>.set(Constants.PR_</xsl:text>
  <xsl:call-template name="makeEnumConstant">
    <xsl:with-param name="propstr" select="." />
  </xsl:call-template>);
</xsl:template>


<xsl:template match="element">
<!--
  <xsl:text>
  public static final int FO_</xsl:text>
  <xsl:call-template name="makeEnumName">
    <xsl:with-param name="propstr" select="name" />
  </xsl:call-template>
  <xsl:text> = </xsl:text>
  <xsl:value-of select="count(preceding-sibling::element)"/>;
-->

  <xsl:variable name="name">
    <xsl:text>fo_</xsl:text>
    <xsl:value-of select="translate(name, '-', '_')"/>
  </xsl:variable>
  <xsl:text>
        BitSet </xsl:text><xsl:value-of select="$name"/>
  <xsl:text> = new BitSet();
</xsl:text>
  <xsl:apply-templates select="common-ref | property">
    <xsl:with-param name="setname" select="$name"/>
  </xsl:apply-templates>
</xsl:template>


<xsl:template match="element" mode="content">
  <xsl:variable name="name">
    <xsl:text>fo_</xsl:text>
    <xsl:value-of select="translate(name, '-', '_')"/>
  </xsl:variable>

  <xsl:apply-templates select="content">
    <xsl:with-param name="setname" select="$name"/>
  </xsl:apply-templates>
</xsl:template>


<xsl:template match="element/content">
  <xsl:param name="setname"/>

  <xsl:variable name="name">
    <xsl:text>fo_</xsl:text>
    <xsl:value-of select="translate(., '-', '_')"/>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test=". = '%block;'">
            loop = mergeContent(<xsl:value-of select="$setname"/>, fo_block, loop);
            loop = mergeContent(<xsl:value-of select="$setname"/>, fo_block_container, loop);
            loop = mergeContent(<xsl:value-of select="$setname"/>, fo_table_and_caption, loop);
            loop = mergeContent(<xsl:value-of select="$setname"/>, fo_table, loop);
            loop = mergeContent(<xsl:value-of select="$setname"/>, fo_list_block, loop);
    </xsl:when>
    <xsl:when test=". = '%inline;'">
            loop = mergeContent(<xsl:value-of select="$setname"/>, fo_bidi_override, loop);
            loop = mergeContent(<xsl:value-of select="$setname"/>, fo_character, loop);
            loop = mergeContent(<xsl:value-of select="$setname"/>, fo_external_graphic, loop);
            loop = mergeContent(<xsl:value-of select="$setname"/>, fo_instream_foreign_object, loop);
            loop = mergeContent(<xsl:value-of select="$setname"/>, fo_inline, loop);
            loop = mergeContent(<xsl:value-of select="$setname"/>, fo_inline_container, loop);
            loop = mergeContent(<xsl:value-of select="$setname"/>, fo_leader, loop);
            loop = mergeContent(<xsl:value-of select="$setname"/>, fo_page_number, loop);
            loop = mergeContent(<xsl:value-of select="$setname"/>, fo_page_number_citation, loop);
            loop = mergeContent(<xsl:value-of select="$setname"/>, fo_basic_link, loop);
            loop = mergeContent(<xsl:value-of select="$setname"/>, fo_multi_toggle, loop);
    </xsl:when>
    <xsl:otherwise>
            loop = mergeContent(<xsl:value-of select="$setname"/>, <xsl:value-of select="$name"/>, loop);
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="element" mode="mapping">
  <xsl:variable name="name">
    <xsl:text>fo_</xsl:text>
    <xsl:value-of select="translate(name, '-', '_')"/>
  </xsl:variable>
  <xsl:text>        mapping[Constants.</xsl:text>
  <xsl:call-template name="makeEnumConstant">
    <xsl:with-param name="propstr" select="$name" />
  </xsl:call-template>] = makeSparseIndices(<xsl:value-of select="$name"/>);
</xsl:template>

<xsl:template match="element/common-ref">
  <xsl:param name="setname"/>

  <xsl:text>        </xsl:text>
  <xsl:value-of select="$setname"/>.or(<xsl:value-of select="."/>);
</xsl:template>

<xsl:template match="element/property">
  <xsl:param name="setname"/>

  <xsl:text>        </xsl:text>
  <xsl:value-of select="$setname"/><xsl:text>.set(Constants.PR_</xsl:text>
  <xsl:call-template name="makeEnumConstant">
    <xsl:with-param name="propstr" select="." />
  </xsl:call-template>);
</xsl:template>

<xsl:template match="text()"/>
<xsl:template match="text()" mode="content"/>
<xsl:template match="text()" mode="mapping"/>

</xsl:stylesheet>

