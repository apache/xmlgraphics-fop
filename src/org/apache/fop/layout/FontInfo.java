/*-- $Id$ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */
package org.apache.fop.layout;

import java.util.Hashtable;
import org.apache.fop.messaging.MessageHandler;
import java.util.Enumeration;

import org.apache.fop.apps.FOPException;

public class FontInfo {

  Hashtable triplets; // look up a font-triplet to find a font-name
  Hashtable fonts; // look up a font-name to get a font (that implements FontMetric at least)

  public FontInfo() {
    this.triplets = new Hashtable(); 
    this.fonts = new Hashtable(); 
  }

  public void addFontProperties(String name, String family, String style, String weight) {
    /* add the given family, style and weight as a lookup for the font
       with the given name */

    String key = family + "," + style + "," + weight;
    this.triplets.put(key,name);
  }

  public void addMetrics(String name, FontMetric metrics) {
    // add the given metrics as a font with the given name

    this.fonts.put(name,metrics);
  }

  public String fontLookup(String family, String style, String weight) throws FOPException {
    // given a family, style and weight, return the font name
    int i;

    try {
      i = Integer.parseInt(weight);
    } catch (NumberFormatException e) {
      i = 0;
    }

    if (i > 600)
      weight = "bold";
    else if (i > 0)
      weight = "normal";

    String key = family + "," + style + "," + weight;

    String f = (String)this.triplets.get(key);
    if (f == null) {
      f = (String)this.triplets.get("any," + style + "," + weight);
      if (f == null) {
        f = (String)this.triplets.get("any,normal,normal");
        if (f == null) {
          throw new FOPException("no default font defined by OutputConverter");
        }
        MessageHandler.errorln("WARNING: defaulted font to any,normal,normal");
      }
      MessageHandler.errorln("WARNING: unknown font "+family+" so defaulted font to any");
    }
    return f;
  }

  public Hashtable getFonts() {
    return this.fonts;
  }

  public FontMetric getMetricsFor(String fontName) throws FOPException {
    return (FontMetric)fonts.get(fontName);
  }

  public FontMetric getMetricsFor(String family, String style, String weight) throws FOPException {
    // given a family, style and weight, return the metric

    return (FontMetric)fonts.get(fontLookup(family,style,weight));
  }
}
