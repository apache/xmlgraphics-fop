/*
 *
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created on 1/02/2004
 * $Id$
 */
package org.apache.fop.fo;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Map;

import org.apache.fop.apps.FOPException;
import org.apache.fop.area.Area;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.FontFamilySet;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.TextDecorations;
import org.apache.fop.datatypes.TextDecorator;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.flow.FoMarker;
import org.apache.fop.fo.flow.FoPageSequence;
import org.apache.fop.fo.properties.FontFamily;
import org.apache.fop.fo.properties.TextDecoration;
import org.apache.fop.fonts.FontException;
import org.apache.fop.xml.FoXmlEvent;
import org.apache.fop.xml.XmlEvent;


/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class FOPageSeqNode extends FONode {

    /** The <code>FoPageSequence</code> ancestor of this node. */
    protected final FoPageSequence pageSequence;
    /**
     * Comment for <code>childContext</code>
     */
    protected Area currentArea = null;
    protected Area layoutContext = null;
    protected ArrayList generated = null;
    /** Decorations applicable to generated text. See 7.16.4 "text-decoration"
     * in the Recommendation.*/
    protected TextDecorations decorations = null;
    /**
     * Gets the current <code>TextDecorations</code> object
     * @return the decorations
     */
    public TextDecorations getDecorations() {
        return decorations;
    }
    
    /**
     * @param foTree the FO tree to which this node is added
     * @param type of FO node
     * @param pageSequence the ancestor <code>page-sequence</code>
     * @param parent node
     * @param event that triggered the creation of this node
     * @param stateFlags the set of states relevant at this point in the
     * tree.  Includes the state information necessary to select an attribute
     * set for this node.
     * @param sparsePropsMap maps the property indices
     * to their offsets in the set of properties applicable to this node
     * @param sparseIndices holds the set of property
     * indices applicable to this node, in ascending order.
     * <code>sparsePropsMap</code> maps property indices to a position in this
     * array.  Together they provide a sparse array facility for this node's
     * properties.
     * @throws TreeException
     * @throws FOPException
     * @throws PropertyException
     */
    public FOPageSeqNode(
        FOTree foTree,
        int type,
        FoPageSequence pageSequence,
        FONode parent,
        XmlEvent event,
        int stateFlags,
        int[] sparsePropsMap,
        int[] sparseIndices)
        throws TreeException, FOPException, PropertyException {
        super(
            foTree,
            type,
            parent,
            event,
            stateFlags,
            sparsePropsMap,
            sparseIndices);
        if (pageSequence.type != FObjectNames.PAGE_SEQUENCE) {
            throw new RuntimeException(
                    "FOPageSeqNode constructor expects FoPageSequence; got " +
                    nodeType());
        }
        this.pageSequence = pageSequence;
        decorations = processDecorations();
        layoutContext = getLayoutContext();
    }
    
    
    /**
     * Constructor for the immediate children of a page-sequence, whose
     * parent page-sequence is not an FOPageSeqNode.
     * 
     * @param foTree the FO tree to which this node is added
     * @param type of FO node
     * @param pageSequence the ancestor <code>page-sequence</code>
     * @param event that triggered the creation of this node
     * @param stateFlags the set of states relevant at this point in the
     * tree.  Includes the state information necessary to select an attribute
     * set for this node.
     * @param sparsePropsMap maps the property indices
     * to their offsets in the set of properties applicable to this node
     * @param sparseIndices holds the set of property
     * indices applicable to this node, in ascending order.
     * <code>sparsePropsMap</code> maps property indices to a position in this
     * array.  Together they provide a sparse array facility for this node's
     * properties.
     * @throws TreeException
     * @throws FOPException
     * @throws PropertyException
     */
    public FOPageSeqNode(
            FOTree foTree,
            int type,
            FoPageSequence pageSequence,
            XmlEvent event,
            int stateFlags,
            int[] sparsePropsMap,
            int[] sparseIndices)
    throws TreeException, FOPException, PropertyException {
        this(
                foTree,
                type,
                pageSequence,
                pageSequence,
                event,
                stateFlags,
                sparsePropsMap,
                sparseIndices);
    }
    
    public FoPageSequence getPageSequence() {
        return pageSequence;
    }

    /**
     * Default text decorations processing.  This method must be overridden
     * by <code>FOPageSeqNode</code>s which do <i>not</i> have text descendants.
     * What this means I am not sure.  Does that include block-containers and
     * inline-containers?
     * 
     * @return the modified <code>TextDecorations</code> object
     */
    protected TextDecorations processDecorations() throws PropertyException {
        TextDecorator decorator = getDefinedDecorator();
        if (parent instanceof FOPageSeqNode) {
            TextDecorations decorations = ((FOPageSeqNode)parent).decorations;
        } else {
            decorations = new TextDecorations(
                    PropNames.TEXT_DECORATION, TextDecoration.NO_DECORATION);
        }
        if (decorator.onMask != TextDecoration.NULL_DECORATION) {
            // A new decorator was defined on this node.  Modify the existing
            // decorations with the new decorator
            decorations.maskDecorations(decorator);
            decorations.setColor(getColor());
        }
        return decorations;
    }

    /**
     * Get the current text decorator.
     * @return a <tt>TextDecorator</tt> containing the current text decorator
     * value.  If a <code>TextDecorator</code> has not been set on this
     * node, the initial value will be returned, whic is a decorator with both
     * <code>onMask</code> and <code>offMask</code> of
     * <code>NULL_DECORATION</code>.
     * @exception PropertyException if the <code>PropertyValue</code> returned
     * is not a <code>TextDecorator</code>.
     */
    public TextDecorator getDefinedDecorator() throws PropertyException {
        PropertyValue pv = getPropertyValue(PropNames.TEXT_DECORATION);
        if (! (pv.getType() == PropertyValue.TEXT_DECORATOR)) {
            throw new PropertyException(
                    "TextDecorator not returned from text-decoration");
        }
        return (TextDecorator)pv;
    }

    public ColorType getColor() throws PropertyException {
        PropertyValue pv = getPropertyValue(PropNames.COLOR);
        if (! (pv.getType() == PropertyValue.COLOR_TYPE)) {
            throw new PropertyException(
                    "ColorType not returned from color");
        }
        return (ColorType)pv;
    }

    /**
     * Gets the fo:marker elements (if any) defined in the this node.  Any
     * fo:marker events found are relinquished.
     * @return the number of markers found
     * @throws FOPException
     */
    public int getMarkers() throws FOPException {
        XmlEvent ev;
        try {
            while ((ev = xmlevents.expectStartElement
                    (FObjectNames.MARKER, XmlEvent.DISCARD_W_SPACE))
            != null) {
                new FoMarker(getFOTree(), pageSequence, this,
                        (FoXmlEvent)ev, stateFlags);
                numMarkers++;
                // Relinquish the original event
                namespaces.relinquishEvent(ev);
            }
        } catch (TreeException e) {
            throw new FOPException(e);
        } catch (FOPException e) {
            throw new FOPException(e);
        }
        return numMarkers;
    }

    /* (non-Javadoc)
     * @see org.apache.fop.fo.FONode#getReferenceRectangle()
     */
    public Area getReferenceRectangle() throws FOPException {
        throw new FOPException("Called from FOPageSeqNode");
    }

    /**
     * @return
     * @throws FOPException
     */
    public Area getLayoutContext() throws FOPException {
        // The default layout context is provided by the parent
            return ((FONode)parent).getChildrensLayoutContext();
    }

    /**
     * Gets the layout context for the children of this <code>FONode</code>.
     * Subclasses with special requirements must override this method.
     * The default context is the current area generated by the node.
     */
    public Area getChildrensLayoutContext()
            throws FOPException {
        return currentArea;
    }

    /**
     * Clone the current <i>TextDecorations</i> property.
     * @return a <tt>TextDecorations</tt> object containing the current
     * text decorations
     * @exception PropertyException if current text decorations are not
     * defined, or are not expressed as <tt>TextDecorations</tt>.
     */
    public TextDecorations cloneCurrentTextDecorations()
            throws PropertyException
    {
        PropertyValue textdec = getPropertyValue(PropNames.TEXT_DECORATION);
        if (textdec.getType() != PropertyValue.TEXT_DECORATIONS)
            throw new PropertyException
                ("text-decoration value is not a TextDecorations object.");
        try {
            return (TextDecorations)(textdec.clone());
        } catch (CloneNotSupportedException e) {
            throw new PropertyException("Clone not supported.");
        }
    }

    /**
     * Gets the current <code>FontFamilySet</code> property
     * @return
     * @throws PropertyException
     */
    private FontFamilySet getFontSet() throws PropertyException {
        PropertyValue fontSet = getPropertyValue(PropNames.FONT_FAMILY);
        if (fontSet.getType() != PropertyValue.FONT_FAMILY) {
            throw new PropertyException(
                    "font-family value is not a FontFamilySet object.");
        }
        // TODO make the FO tree property values immutable objects wherever
        // possible
        return (FontFamilySet)fontSet;
    }

    /**
     * Constructs a set of attributes for defining a font, fron the current
     * set of font-specifying properties
     * @return
     * @throws PropertyException
     * @throws FontException
     */
    public Map getFontAttributes() throws PropertyException, FontException {
        int pvtype;
        Numeric fontSize = null;
        PropertyValue pv = getPropertyValue(PropNames.FONT_STYLE);
        if (pv.getType() != PropertyValue.ENUM) {
            throw new PropertyException("font-style not resolved");
        }
        int style = ((EnumType)pv).getEnumValue();
        pv = getPropertyValue(PropNames.FONT_WEIGHT);
        pvtype = pv.getType();
        int weight = 0;
        if (pvtype == PropertyValue.ENUM) {
            weight = ((EnumType)pv).getEnumValue();
        } else if (pvtype == PropertyValue.INTEGER) {
            throw new PropertyException(
                    "Integer values not supported for font-weight");
        } else {
            throw new PropertyException("font-weight not resolved");
        }
        int variant = 0;
        pv = getPropertyValue(PropNames.FONT_VARIANT);
        if (pv.getType() !=  PropertyValue.ENUM) {
            throw new PropertyException("font-variant not resolved");
        }
        int stretch = 0;
        pv = getPropertyValue(PropNames.FONT_STRETCH);
        if (pv.getType() !=  PropertyValue.ENUM) {
            throw new PropertyException("font-stretch not resolved");
        }
        float size = 0;
        fontSize = currentFontSize();
        if (fontSize.isLength()) {
            size = (float)(fontSize.asDouble());
        } else {
            throw new PropertyException("font-size is not a length");
        }
        return fontData.makeFontAttributes(
                null, style, variant, weight, stretch, size);
    }

    /**
     * Gets a font matching the current set of font specifiers
     * @return the font
     * @throws FontException
     * @throws PropertyException
     */
    public Font getFopFont() throws FontException, PropertyException {
        Map fontAttributes = getFontAttributes();
        return getFopFont(fontAttributes);
    }


    /**
     * Gets a font matching the set of font attributes in the <code>Map</code>
     * provided
     * @param attributes the map
     * @return the font
     * @throws FontException
     * @throws PropertyException
     */
    public Font getFopFont(Map attributes)
    throws FontException, PropertyException {
        // Get the current font specifiers
        FontFamilySet fontSet = getFontSet();
        FontFamilySet.Traverser fonts = fontSet.new Traverser();
        while (fonts.hasNext()) {
            // Handling generics?
            String nextFontFamily = fonts.next();
            attributes.put(TextAttribute.FAMILY, nextFontFamily);
            if (FontFamily.isGeneric(nextFontFamily)) {
                Font font = fontData.getGenericFont(attributes);
            } else {
                Font font = fontData.getFont(attributes, 0);
            }
        }
        throw new FontException("No matching font found");
    }
}
