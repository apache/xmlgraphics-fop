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
import java.awt.image.BufferedImage;
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
      props.list(System.out);
      GraphicsEnvironment gEnv =
          GraphicsEnvironment.getLocalGraphicsEnvironment();
      Font[] fonts = gEnv.getAllFonts();
      String[] families = gEnv.getAvailableFontFamilyNames();
      BufferedImage pageSpread =
          new BufferedImage(20*72, 12*72, BufferedImage.TYPE_INT_RGB);
      //Graphics2D g2D = fontImage.createGraphics();
      Graphics2D g2D = pageSpread.createGraphics();
      FontRenderContext frc = g2D.getFontRenderContext();
  }

  public static void main(String[] args) {
      FontTest test = new FontTest();
      test.setupFontInfo();
  }
}
