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

import org.apache.fop.fo.Constants;
import java.util.BitSet;
import java.util.ArrayList;

public class PropertySets {
    private static short[][] mapping = null;

    private Element[] elements = new Element[Constants.ELEMENT_COUNT+1];
    private BitSet block_elems = new BitSet();
    private BitSet inline_elems = new BitSet();

</xsl:text>
  <xsl:apply-templates select="common" mode="decl"/>
<xsl:text>
    public void initializeElements() {
        block_elems.set(Constants.FO_BLOCK);
        block_elems.set(Constants.FO_BLOCK_CONTAINER);
        block_elems.set(Constants.FO_TABLE_AND_CAPTION);
        block_elems.set(Constants.FO_TABLE);
        block_elems.set(Constants.FO_LIST_BLOCK);

        inline_elems.set(Constants.FO_BIDI_OVERRIDE);
        inline_elems.set(Constants.FO_CHARACTER);
        inline_elems.set(Constants.FO_EXTERNAL_GRAPHIC);
        inline_elems.set(Constants.FO_INSTREAM_FOREIGN_OBJECT);
        inline_elems.set(Constants.FO_INLINE);
        inline_elems.set(Constants.FO_INLINE_CONTAINER);
        inline_elems.set(Constants.FO_LEADER);
        inline_elems.set(Constants.FO_PAGE_NUMBER);
        inline_elems.set(Constants.FO_PAGE_NUMBER_CITATION);
        inline_elems.set(Constants.FO_BASIC_LINK);
        inline_elems.set(Constants.FO_MULTI_TOGGLE);
    }

    public void initializeCommon() {
</xsl:text>
<xsl:apply-templates select="common"/>
<xsl:text>
    }

    public void initialize() {
        // define the fo: elements
        for (int i = 1; i &lt; elements.length; i++) {
            elements[i] = new Element(i);
        }

        // populate the elements with properties and content elements.
        Element elem;
</xsl:text>

<xsl:apply-templates select="//element"/>
<xsl:text>

        // Merge the attributes from the children into the parent.
        for (boolean dirty = true; dirty; ) {
            dirty = false;
            for (int i = 1; i &lt; elements.length; i++) {
                dirty = dirty || elements[i].merge();
            }
        }
        // Calculate the sparse indices for each element.
        for (int i = 1; i &lt; elements.length; i++) {
            mapping[i] = makeSparseIndices(elements[i].valid);
        }
    }

    /**
     * Turn a BitSet into an array of shorts with the first element
     * on the array the number of set bits in the BitSet.
     */
    private static short[] makeSparseIndices(BitSet set) {
        short[] indices = new short[Constants.PROPERTY_COUNT+1];
        int j = 1;
        for (int i = 0; i &lt; Constants.PROPERTY_COUNT+1; i++) {
            if (set.get(i)) {
                indices[i] = (short) j++;
            }
        }
        indices[0] = (short)j;
        return indices;
    }


    public static short[] getPropertySet(int elementId) {
        if (mapping == null) {
            mapping = new short[Constants.ELEMENT_COUNT+1][];
            PropertySets ps = new PropertySets();
            ps.initializeElements();
            ps.initializeCommon();
            ps.initialize();
        }
        return mapping[elementId];
    }

    /**
     * An object that represent the properties and contents of a fo element
     */
    class Element {
        BitSet relevant = new BitSet();
        BitSet valid = new BitSet();
        int elementId;
        ArrayList children;

        Element(int elementId) {
            this.elementId = elementId;
        }

        /**
         * Add a single property to the element.
         */
        public void addProperty(int propId) {
            relevant.set(propId);
            valid.set(propId);
        }

        /**
         * Add a set of properties to the element.
         */
        public void addProperties(BitSet properties) {
            relevant.or(properties);
            valid.or(properties);
        }

        /**
         * Add a single fo element as a content child.
         */
        public void addContent(int elementId) {
            if (children == null) {
                children = new ArrayList();
            }
            children.add(elements[elementId]);
        }

        /**
         * Add a set of fo elements as content children.
         */
        public void addContent(BitSet elements) {
            for (int i = 0; i &lt; elements.size(); i++) {
                if (elements.get(i)) {
                    addContent(i);
                }
            }
        }

        /**
         * Merge the properties from the children into the set of valid
         * properties. Return true if at least one property could be added.
         */
        public boolean merge() {
            if (children == null) {
                return false;
            }
            boolean dirty = false;
            for (int i = 0; i &lt; children.size(); i++) {
                Element child = (Element) children.get(i);
                BitSet childValid = child.valid;
                int n = childValid.length();
                for (int j = 0; j &lt; n; j++) {
                    if (childValid.get(j) &amp;&amp; !valid.get(j)) {
                        dirty = true;
                        valid.set(j);
                    }
                }
            }
            return dirty;
        }
    }
}
</xsl:text>
</xsl:template>

<xsl:template match="common" mode="decl">
  <xsl:variable name="name" select="name"/>
  <xsl:text>    BitSet </xsl:text><xsl:value-of select="$name"/>
  <xsl:text> = new BitSet();
</xsl:text>
</xsl:template>

<xsl:template match="common">
  <xsl:variable name="name" select="name"/>
  <xsl:apply-templates select="property">
    <xsl:with-param name="setname" select="$name"/>
  </xsl:apply-templates>
  <xsl:text>
</xsl:text>
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
  <xsl:variable name="name">
    <xsl:call-template name="makeEnumConstant">
      <xsl:with-param name="propstr" select="name" />
    </xsl:call-template>
  </xsl:variable>
  <xsl:text>        elem = elements[Constants.FO_</xsl:text>
  <xsl:value-of select="$name"/>
  <xsl:text>];
</xsl:text>
  <xsl:apply-templates select="common-ref | property"/>
  <xsl:apply-templates select="content"/>
  <xsl:text>
</xsl:text>
</xsl:template>


<xsl:template match="element/common-ref">
  <xsl:param name="setname"/>

  <xsl:text>        elem.addProperties(</xsl:text>
  <xsl:value-of select="."/>);
</xsl:template>

<xsl:template match="element/property">
  <xsl:param name="setname"/>

  <xsl:text>        elem.addProperty(Constants.PR_</xsl:text>
  <xsl:call-template name="makeEnumConstant">
    <xsl:with-param name="propstr" select="." />
  </xsl:call-template>);
</xsl:template>

<xsl:template match="element/content">
  <xsl:variable name="name">
    <xsl:text>Constants.FO_</xsl:text>
    <xsl:call-template name="makeEnumConstant">
      <xsl:with-param name="propstr" select="." />
    </xsl:call-template>
  </xsl:variable>

  <xsl:choose>
    <xsl:when test=". = '%block;'">
       <xsl:text>        elem.addContent(block_elems);
</xsl:text>
    </xsl:when>
    <xsl:when test=". = '%inline;'">
       <xsl:text>        elem.addContent(inline_elems);
</xsl:text>
    </xsl:when>
    <xsl:otherwise>
       <xsl:text>        elem.addContent(</xsl:text>
      <xsl:value-of select="$name"/>
      <xsl:text>);
</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<xsl:template match="text()"/>

</xsl:stylesheet>

