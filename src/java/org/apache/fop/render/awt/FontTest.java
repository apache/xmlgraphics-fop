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
 * Created on 12/04/2004
 * $Id$
 */
package org.apache.fop.render.awt;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Properties;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class FontTest {
    /**
     * 
     */
    public FontTest() {
        super();
        // TODO Auto-generated constructor stub
    }

  public void setupFontInfo() {
      // create a temp Image to test font metrics on
      Properties props = System.getProperties();
      // props.list(System.out);
      GraphicsEnvironment gEnv =
          GraphicsEnvironment.getLocalGraphicsEnvironment();
      Font[] fonts = gEnv.getAllFonts();
      String[] families = gEnv.getAvailableFontFamilyNames();
      BufferedImage pageSpread =
          new BufferedImage(20*72, 12*72, BufferedImage.TYPE_INT_RGB);
      //Graphics2D g2D = fontImage.createGraphics();
      Graphics2D g2D = pageSpread.createGraphics();
      FontRenderContext frc = g2D.getFontRenderContext();
      for (int i = 0; i < families.length; i++) {
          System.out.println(families[i]);
      }
      for (int i = 0; i < fonts.length; i++) {
          Font f = fonts[i];
          System.out.println(f.getFontName());
          System.out.println("\tlogical:" + f.getName());
          System.out.println("\tfamily:" + f.getFamily());
          System.out.println("\tPSName:" + f.getPSName());
          System.out.println("\t" + (f.isPlain() ? "PLAIN  " : "      ")
              + (f.isBold() ? "BOLD  " : "      ")
              + (f.isItalic() ? "ITALIC" : "      "));
          int style = f.getStyle();
          System.out.println("\tStyle:");
          if (style == Font.PLAIN) {
              System.out.println("\t\tPLAIN");
          }
          if ((style & Font.BOLD) != 0) {
              System.out.println("\t\tBOLD");
          }
          if ((style & Font.ITALIC) != 0) {
              System.out.println("\t\tITALIC");             
          }
          Attribute[] textAttrs = f.getAvailableAttributes();
          for (int j = 0; j < textAttrs.length; j++) {
              if (textAttrs[j] instanceof TextAttribute) {
                  TextAttribute attr = (TextAttribute)textAttrs[j];
                  if (attr == TextAttribute.BACKGROUND) {
                      System.out.println("\tBACKGROUND");
                  } else if (attr == TextAttribute.BIDI_EMBEDDING) {
                      System.out.println("\tBIDI_EMBEDDING");
                  } else if (attr == TextAttribute.CHAR_REPLACEMENT) {
                      System.out.println("\tCHAR_REPLACEMENT");
                  } else if (attr == TextAttribute.FAMILY) {
                      System.out.println("\tFAMILY");
                  } else if (attr == TextAttribute.FONT) {
                      System.out.println("\tFONT");
                  } else if (attr == TextAttribute.FOREGROUND) {
                      System.out.println("\tFOREGROUND");
                  } else if (attr == TextAttribute.INPUT_METHOD_HIGHLIGHT) {
                      System.out.println("\tINPUT_METHOD_HIGHLIGHT");
                  } else if (attr == TextAttribute.INPUT_METHOD_UNDERLINE) {
                      System.out.println("\tINPUT_METHOD_UNDERLINE");
                  } else if (attr == TextAttribute.JUSTIFICATION) {
                      System.out.println("\tJUSTIFICATION");
                  } else if (attr == TextAttribute.NUMERIC_SHAPING) {
                      System.out.println("\tNUMERIC_SHAPING");
                  } else if (attr == TextAttribute.POSTURE) {
                      System.out.println("\tPOSTURE");
                  } else if (attr == TextAttribute.RUN_DIRECTION) {
                      System.out.println("\tRUN_DIRECTION");
                  } else if (attr == TextAttribute.SIZE) {
                      System.out.println("\tSIZE");
                  } else if (attr == TextAttribute.STRIKETHROUGH) {
                      System.out.println("\tSTRIKETHROUGH");
                  } else if (attr == TextAttribute.SUPERSCRIPT) {
                      System.out.println("\tSUPERSCRIPT");
                  } else if (attr == TextAttribute.SWAP_COLORS) {
                      System.out.println("\tSWAP_COLORS");
                  } else if (attr == TextAttribute.TRANSFORM) {
                      System.out.println("\tTRANSFORM");
                  } else if (attr == TextAttribute.UNDERLINE) {
                      System.out.println("\tUNDERLINE");
                  } else if (attr == TextAttribute.WEIGHT) {
                      System.out.println("\tWEIGHT");
                  } else if (attr == TextAttribute.WIDTH) {
                      System.out.println("\tWIDTH");
                  }
              } else {
                  Attribute attr = textAttrs[j];
                  if (attr == Attribute.LANGUAGE) {
                      System.out.println("\tLANGUAGE");
                  } else if (attr == Attribute.READING) {
                      System.out.println("\tREADING");
                  } else if (attr == Attribute.INPUT_METHOD_SEGMENT) {
                      System.out.println("\tINPUT_METHOD_SEGMENT");
                  }
              }
          }
      }
  }
  
  public static void main(String[] args) {
      FontTest test = new FontTest();
      test.setupFontInfo();
  }
}
