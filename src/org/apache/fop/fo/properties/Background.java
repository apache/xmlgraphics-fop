package org.apache.fop.fo.properties;

import org.apache.fop.fo.properties.*;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertyConsts;
import org.apache.fop.fo.ShorthandPropSets;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PropertyValueList;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.messaging.MessageHandler;

import java.util.ListIterator;
import java.util.Iterator;

public class Background extends Property  {
    public static final int dataTypes = SHORTHAND | INHERIT;
    public static final int traitMapping = SHORTHAND_MAP;
    public static final int initialValueType = NOTYPE_IT;
    public static final int inherited = NO;

    /**
     * 'value' is a PropertyValueList or an individual PropertyValue.
     * If 'value' is a PropertyValueList, it must contain a single
     * PropertyValueList, which in turn contains the individual elements.
     *
     * 'value' can contain a parsed Inherit value or, in any order;
     * background-color
     *     a parsed ColorType value, or an NCName containing one of
     *     the standard colors
     * background-image
     *     a parsed UriType value, or a parsed None value
     * background-repeat
     *     a parsed NCName containing a repeat enumeration token
     * background-attachment
     *     a parsed NCName containing 'scroll' or 'fixed'
     * background-position
     *     one or two parsed Length or Percentage values, or
     *     one or two parsed NCNames containing enumeration tokens
     *
     * <p>The value(s) provided, if valid, are converted into a list
     * containing the expansion of the shorthand.  Any subset of the
     * elements may be present, from minimum of one.  The elements
     * which are present will always occur in the following order:
     *
     *   a BackgroundColor ColorType or Inherit value
     *   a BackgroundImage UriType, None or Inherit value
     *   a BackgroundRepeat EnumType or Inherit value
     *   a BackgroundAttachment EnumType or Inherit value
     *   a BackgroundPositionHorizontal Numeric or Inherit value
     *   a BackgroundPositionVertical Numeric or Inherit value
     *
     * @param propindex - the <tt>int</tt> property index.
     * @param foNode - the <tt>FONode</tt> on which this expression is being
     * parsed.
     * @value - the <tt>PropertyValue</tt> passed from
     * <i>PropertyParser.parse()</i>.
     * @return the refined <tt>PropertyValue</tt>.
     * @throws <tt>PropertyException</tt>.
     */
    public /*static*/ PropertyValue refineParsing
                        (int propindex, FONode foNode, PropertyValue value)
		    throws PropertyException
    {
	if ( ! (value instanceof PropertyValueList)) {
	    return processValue(propindex, foNode, value);
	} else {
	    return processList(propindex,
                    foNode, spaceSeparatedList((PropertyValueList)value));
	}
    }

    private /*static*/ PropertyValueList processValue
	(int propindex, FONode foNode, PropertyValue value)
            throws PropertyException
    {
	// Can be Inherit, ColorType, UriType, None, Numeric, or an
	// NCName (i.e. enum token)
	int type = value.getType();
	if (type == PropertyValue.INHERIT ||
		type == PropertyValue.FROM_PARENT ||
		    type == PropertyValue.FROM_NEAREST_SPECIFIED)
	{
	    // Copy the value to each member of the shorthand expansion
	    return refineExpansionList(PropNames.BACKGROUND, foNode,
                                ShorthandPropSets.expandAndCopySHand(value));
	} else  {
	    // Make a list and pass to processList
	    PropertyValueList tmpList
		    = new PropertyValueList(propindex);
		    //= new PropertyValueList(value.getProperty());
	    tmpList.add(value);
	    return processList(propindex, foNode, tmpList);
	}
    }

    private /*static*/ PropertyValueList processList
                    (int property, FONode foNode, PropertyValueList value)
		    throws PropertyException
    {
	//int property = value.getProperty();
	PropertyValue color= null,
			image = null,
			repeat = null,
			attachment = null,
			position = null;

	PropertyValueList newlist = new PropertyValueList(property);
	// This is a list
	if (value.size() == 0)
	    throw new PropertyException
			    ("Empty list for Background");
	ListIterator elements = ((PropertyValueList)value).listIterator();

	scanning_elements: while (elements.hasNext()) {
	    PropertyValue pval = (PropertyValue)(elements.next());
	    int type = pval.getType();
	    switch (type) {
	    case PropertyValue.COLOR_TYPE:
		if (color != null) MessageHandler.log("background: " +
			    "duplicate color overrides previous color");
		color = pval;
		continue scanning_elements;

	    case PropertyValue.URI_TYPE:
		if (image != null) MessageHandler.log("background: " +
		    "duplicate image uri overrides previous image spec");
		image = pval;
		continue scanning_elements;

	    case PropertyValue.NONE:
		if (image != null) MessageHandler.log("background: " +
		    "duplicate image spec overrides previous image spec");
		image = pval;
		continue scanning_elements;

	    case PropertyValue.NUMERIC: {
		// Must be one of the position values
		// send it to BackgroundPosition.complex for processing
		// If it is followed by another Numeric, form a list from
		// the pair, else form a list from this element only
		PropertyValueList posnList = new PropertyValueList
					(PropNames.BACKGROUND_POSITION);
		PropertyValue tmpval = null;
		// Is it followed by another Numeric?
		if (elements.hasNext()) {
		    if ((tmpval = (PropertyValue)(elements.next()))
				instanceof Numeric) {
			posnList.add(pval);
			posnList.add(tmpval);
		    } else {
			// Not a following Numeric, so restore the list
			// cursor
			Object tmpo = elements.previous();
			tmpval = null;
		    }
		}
		// Now send one or two Numerics to BackgroundPosition
		if (position != null)
			MessageHandler.log("background: duplicate" +
			"position overrides previous position");
		if (tmpval == null)
                    position = PropertyConsts.pconsts.refineParsing
                                (PropNames.BACKGROUND_POSITION,
                                                foNode, pval, IS_NESTED);
		else { // 2 elements
		    // make a space-separated list
		    PropertyValueList ssList = new PropertyValueList
					(PropNames.BACKGROUND_POSITION);
		    ssList.add(posnList);
                    position = PropertyConsts.pconsts.refineParsing
                                (PropNames.BACKGROUND_POSITION,
                                                foNode, ssList, IS_NESTED);
		}
		continue scanning_elements;
	    }  // end of case NUMERIC

	    case PropertyValue.NCNAME: {
		// NCName can be:
		//  a standard color name
		//  a background attachment mode
		//  one or two position indicators
		String ncname = ((NCName)pval).getNCName();
		ColorType colorval = null;
		try {
		    colorval = new ColorType
				    (PropNames.BACKGROUND_COLOR, ncname);
		} catch (PropertyException e) {};
		if (colorval != null) {
		    if (color != null) MessageHandler.log("background: " +
			    "duplicate color overrides previous color");
		    color = colorval;
		    continue scanning_elements;
		}

		// Is it an attachment mode?
		EnumType enum = null;
		try {
		    enum = new EnumType
			    (PropNames.BACKGROUND_ATTACHMENT, ncname);
		} catch (PropertyException e) {};
		if (enum != null) {
		    if (attachment != null)
			    MessageHandler.log("background: duplicate" +
			    "attachment overrides previous attachment");
		    attachment = enum;
		    continue scanning_elements;
		}

		// Must be a position indicator
		// send it to BackgroundPosition.complex for processing
		// If it is followed by another NCName, form a list from
		// the pair, else send this element only

		// This is made messy by the syntax of the Background
		// shorthand.  A following NCName need not be a second
		// position indicator.  So we have to test this element
		// and the following element individually.
		PropertyValueList posnList = new PropertyValueList
					(PropNames.BACKGROUND_POSITION);
		PropertyValue tmpval = null;
		// Is the current NCName a position token?
		boolean pos1ok = false, pos2ok = false;
		try {
		    PropertyConsts.pconsts.getEnumIndex
                                    (PropNames.BACKGROUND_POSITION, ncname);
		    pos1ok = true;
		    if (elements.hasNext()) {
			tmpval = (PropertyValue)(elements.next());
			if (tmpval instanceof NCName) {
			    String ncname2 = ((NCName)tmpval).getString();
                            PropertyConsts.pconsts.getEnumIndex
                                    (PropNames.BACKGROUND_POSITION, ncname2);
			    pos2ok = true;
			} else {
			    // Restore the listIterator cursor
			    Object tmpo = elements.previous();
			}
		    }
		} catch (PropertyException e) {};

		if (pos1ok) {
		    if (position != null)
			    MessageHandler.log("background: duplicate" +
			    "position overrides previous position");
		    // Is it followed by another position NCName?
		    if (pos2ok) {
			posnList.add(pval);
			posnList.add(tmpval);
			// Now send two NCNames to BackgroundPosition
			// as a space-separated list
			PropertyValueList ssList = new PropertyValueList
					(PropNames.BACKGROUND_POSITION);
			ssList.add(posnList);
			position = PropertyConsts.pconsts.refineParsing
                                (PropNames.BACKGROUND_POSITION,
                                                foNode, ssList, IS_NESTED);
		    } else { // one only
		    // Now send one NCName to BackgroundPosition
			position = PropertyConsts.pconsts.refineParsing
                                (PropNames.BACKGROUND_POSITION,
                                                    foNode, pval, IS_NESTED);
		    }
		    continue scanning_elements;
		}
		throw new PropertyException
		    ("Unknown NCName value for background: " + ncname);
	    } // end of case NCNAME

	    default:
		throw new PropertyException
		    ("Invalid " + pval.getClass().getName() +
			" property value for background");
	    }  // end of switch
	}

	// Now construct the list of PropertyValues with their
	// associated property indices, as expanded from the
	// Background shorthand.  Note that the position value is a list
	// containing the expansion of the BackgroundPosition shorthand.

	if (color != null) {
	    color.setProperty(PropNames.BACKGROUND_COLOR);
	    newlist.add(color);
	}
	if (image != null) {
	    image.setProperty(PropNames.BACKGROUND_IMAGE);
	    newlist.add(image);
	}
	if (repeat != null) {
	    repeat.setProperty(PropNames.BACKGROUND_REPEAT);
	    newlist.add(repeat);
	}
	if (attachment != null) {
	    attachment.setProperty(PropNames.BACKGROUND_ATTACHMENT);
	    newlist.add(attachment);
	}
	if (position != null) {
	    // position must have two elements
	    Iterator positions = ((PropertyValueList)position).iterator();
	    newlist.add(positions.next());
	    newlist.add(positions.next());
	}
	return newlist;
    }

}

