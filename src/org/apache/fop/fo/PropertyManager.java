/*-- $Id$ -- */
/*
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.fo;

import org.apache.fop.layout.FontState;
import org.apache.fop.layout.FontInfo;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.fo.properties.BreakAfter;
import org.apache.fop.fo.properties.BreakBefore;
import org.apache.fop.fo.properties.Constants;
import org.apache.fop.layout.HyphenationProps;
import org.apache.fop.apps.FOPException;
import java.text.MessageFormat;
import java.text.FieldPosition;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.ColumnArea;

public class PropertyManager {

  private PropertyList properties;
  private FontState fontState=null;
  private BorderAndPadding borderAndPadding = null;
  private HyphenationProps hyphProps = null;

  private String[] saLeft ;
  private String[] saRight;
  private String[] saTop ;
  private String[] saBottom ;

  private static MessageFormat msgColorFmt = new MessageFormat("border-{0}-color");
  private static MessageFormat msgStyleFmt = new MessageFormat("border-{0}-style");
  private static MessageFormat msgWidthFmt = new MessageFormat("border-{0}-width");
  private static MessageFormat msgPaddingFmt = new MessageFormat("padding-{0}");

  public PropertyManager(PropertyList pList) {
    this.properties = pList;
  }

  private void initDirections() {
    saLeft = new String[1];
    saRight = new String[1];
    saTop = new String[1];
    saBottom = new String[1];
      saTop[0] = properties.wmAbsToRel(PropertyList.TOP);
      saBottom[0] = properties.wmAbsToRel(PropertyList.BOTTOM);
      saLeft[0] = properties.wmAbsToRel(PropertyList.LEFT);
      saRight[0] = properties.wmAbsToRel(PropertyList.RIGHT);
  }

  public FontState getFontState(FontInfo fontInfo) throws FOPException {
    if (fontState == null) {
        String fontFamily = properties.get("font-family").getString();
        String fontStyle =properties.get("font-style").getString();
        String fontWeight =properties.get("font-weight").getString();
    // NOTE: this is incomplete. font-size may be specified with
    // various kinds of keywords too
        int fontSize =properties.get("font-size").getLength().mvalue();
        int fontVariant =properties.get("font-variant").getEnum();
	// fontInfo is same for the whole FOP run but set in all FontState
	fontState = new FontState(fontInfo, fontFamily,
                fontStyle, fontWeight, fontSize, fontVariant);
    }
    return fontState ;
  }


  public BorderAndPadding getBorderAndPadding() {
    if (borderAndPadding == null) {
      this.borderAndPadding = new BorderAndPadding();
      initDirections();

      initBorderInfo(BorderAndPadding.TOP, saTop);
      initBorderInfo(BorderAndPadding.BOTTOM, saBottom);
      initBorderInfo(BorderAndPadding.LEFT, saLeft);
      initBorderInfo(BorderAndPadding.RIGHT, saRight);
    }
    return borderAndPadding;
  }

  private void initBorderInfo(int whichSide, String[] saSide) {
    borderAndPadding.setPadding(whichSide,
	properties.get(msgPaddingFmt.format(saSide)).getCondLength());
    // If style = none, force width to 0, don't get Color
    int style = properties.get(msgStyleFmt.format(saSide)).getEnum();
    if (style != Constants.NONE) {
      borderAndPadding.setBorder(whichSide, style,
		properties.get(msgWidthFmt.format(saSide)).getCondLength(),
	        properties.get(msgColorFmt.format(saSide)).getColorType());
    }
  }

  public HyphenationProps getHyphenationProps() {
    if (hyphProps == null) {
      this.hyphProps = new HyphenationProps();
      hyphProps.hyphenate = this.properties.get("hyphenate").getEnum();
      hyphProps.hyphenationChar = this.properties.get("hyphenation-character").getCharacter();
      hyphProps.hyphenationPushCharacterCount = this.properties.get( "hyphenation-push-character-count").getNumber().intValue();
      hyphProps.hyphenationRemainCharacterCount = this.properties.get( "hyphenation-remain-character-count").getNumber().intValue();
      hyphProps.language = this.properties.get("language").getString();
      hyphProps.country = this.properties.get("country").getString();
    }
    return hyphProps;
  }

  public int checkBreakBefore(Area area) {
	  if (!(area instanceof ColumnArea))
		  	return Status.OK;
	  ColumnArea colArea = (ColumnArea)area;
    switch(properties.get("break-before").getEnum()) {
      case BreakBefore.PAGE:
		  // if first ColumnArea, and empty, return OK
		  if (!colArea.hasChildren() && (colArea.getColumnIndex() == 1))
			  return Status.OK;
		  else
              return Status.FORCE_PAGE_BREAK;
      case BreakBefore.ODD_PAGE:
		  // if first ColumnArea, empty, _and_ in odd page,
		  // return OK
		  if (!colArea.hasChildren() && (colArea.getColumnIndex() == 1) &&
			  (colArea.getPage().getNumber() % 2 != 0))
			  return Status.OK;
		  else
              return Status.FORCE_PAGE_BREAK_ODD;
      case BreakBefore.EVEN_PAGE:
		  // if first ColumnArea, empty, _and_ in even page,
		  // return OK
		  if (!colArea.hasChildren() && (colArea.getColumnIndex() == 1) &&
			  (colArea.getPage().getNumber() % 2 == 0))
			  return Status.OK;
		  else
       return Status.FORCE_PAGE_BREAK_EVEN;
      case BreakBefore.COLUMN:
		  // if ColumnArea is empty return OK
		  if (!area.hasChildren())
			  return Status.OK;
		  else		  
              return Status.FORCE_COLUMN_BREAK;
      default:
        return Status.OK;
    }
  }

  public int checkBreakAfter(Area area) {
	  if (!(area instanceof org.apache.fop.layout.ColumnArea))
		  	return Status.OK;
    switch(properties.get("break-after").getEnum()) {
      case BreakAfter.PAGE:
       return Status.FORCE_PAGE_BREAK;
      case BreakAfter.ODD_PAGE:
       return Status.FORCE_PAGE_BREAK_ODD;
      case BreakAfter.EVEN_PAGE:
       return Status.FORCE_PAGE_BREAK_EVEN;
      case BreakAfter.COLUMN:
       return Status.FORCE_COLUMN_BREAK;
      default:
        return Status.OK;
    }
  }

}
