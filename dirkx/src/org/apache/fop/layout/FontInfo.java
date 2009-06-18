package org.apache.xml.fop.layout;

import java.util.Hashtable;
import java.util.Enumeration;

import org.apache.xml.fop.apps.FOPException;

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
        System.err.println("WARNING: defaulted font to any,normal,normal");
      }
      System.err.println("WARNING: unknown font "+family+" so defaulted font to any");
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
