/*-- $Id$ -- */
/*
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.layout;


import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.CondLength;

public class BorderAndPadding {

  public static final int TOP=0;
  public static final int RIGHT=1;
  public static final int BOTTOM=2;
  public static final int LEFT=3;

  public static class BorderInfo {
    private int mStyle; // Enum for border style
    private CondLength mWidth;
    private ColorType mColor; // Border color

    BorderInfo(int style, CondLength width, ColorType color) {
      mStyle = style;
      mWidth = width;
      mColor = color;
    }
  }

  private BorderInfo[] borderInfo = new BorderInfo[4];
  private CondLength[] padding = new CondLength[4];

  public BorderAndPadding() {
  }

  public void setBorder(int side, int style, CondLength width, ColorType color ) {
    borderInfo[side] = new BorderInfo(style, width, color);
  }

  public void setPadding(int side, CondLength width ) {
    padding[side] = width;
  }

  public int getBorderLeftWidth(boolean bDiscard) {
    return getBorderWidth(LEFT, bDiscard);
  }

  public int getBorderRightWidth(boolean bDiscard) {
    return getBorderWidth(RIGHT, bDiscard);
  }

  public int getBorderTopWidth(boolean bDiscard) {
    return getBorderWidth(TOP, bDiscard);
  }

  public int getBorderBottomWidth(boolean bDiscard) {
    return getBorderWidth(BOTTOM, bDiscard);
  }

  public int getPaddingLeft(boolean bDiscard) {
    return getPadding(LEFT, bDiscard);
  }

  public int getPaddingRight(boolean bDiscard) {
    return getPadding(RIGHT, bDiscard);
  }

  public int getPaddingBottom(boolean bDiscard) {
    return getPadding(BOTTOM, bDiscard);
  }

  public int getPaddingTop(boolean bDiscard) {
    return getPadding(TOP, bDiscard);
  }


  private int getBorderWidth(int side, boolean bDiscard) {
      if ((borderInfo[side] == null) ||
	  (bDiscard && borderInfo[side].mWidth.isDiscard())) {
	return 0;
      }
      else return borderInfo[side].mWidth.mvalue();
  }

  public ColorType getBorderColor(int side) {
    if (borderInfo[side] != null) {
      return borderInfo[side].mColor;
    }
    else return null;
  }

  public int getBorderStyle(int side) {
    if (borderInfo[side] != null) {
      return borderInfo[side].mStyle;
    }
    else return 0;
  }

  private int getPadding(int side, boolean bDiscard) {
      if ((padding[side] == null) ||
	  (bDiscard && padding[side].isDiscard())) {
	return 0;
      }
      else return padding[side].mvalue();
  }
}
