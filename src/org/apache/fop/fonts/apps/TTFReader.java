/* -- $Id$ --

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
package org.apache.fop.fonts.apps;

import java.io.*;
import org.w3c.dom.*;
import org.apache.xerces.dom.*;
import org.apache.xml.serialize.*;
import org.apache.xalan.xslt.*;
import org.apache.fop.fonts.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
/**
 * A tool which reads TTF files and generates
 * XML font metrics file for use in FOP.
 *
 */
public class TTFReader {

    private boolean invokedStandalone = false;

    public TTFReader() {
    }


       /**
        * Parse commandline arguments. put options in the Hashtable and return
        * arguments in the String array
        * the arguments: -fn Perpetua,Bold -cn PerpetuaBold per.ttf Perpetua.xml
        * returns a String[] with the per.ttf and Perpetua.xml. The hash
        * will have the (key, value) pairs: (-fn, Perpetua) and (-cn, PerpetuaBold)
        */
   private static String[] parseArguments(Hashtable options, String[] args) {
      Vector arguments=new Vector();
      for (int i=0; i < args.length; i++) {
         if (args[i].startsWith("-")) {
             if ((i+1) < args.length && !args[i+1].startsWith("-")) {
                 options.put(args[i], args[i+1]);
                 i++;
             } else {
               options.put(args[i], "");
             }
         } else {
             arguments.addElement(args[i]);
         }
      }

      String[] argStrings=new String[arguments.size()];
      arguments.copyInto(argStrings);
      return argStrings;
   }


   private final static void displayUsage() {
      System.out.println(" java org.apache.fop.fonts.apps.TTFReader [options] fontfile.ttf xmlfile.xml\n");
      System.out.println(" where options can be:\n");
      System.out.println("-enc cid");
      System.out.println("     With this option you create a CID keyed font.");
      System.out.println("     If you're going to use characters outside the");
      System.out.println("     pdfencoding range (almost the same as iso-8889-1)");
      System.out.println("     you must add this option.");
      System.out.println("-ttcname <fontname>");
      System.out.println("     If you're reading data from a TrueType Collection");
      System.out.println("     (.ttc file) you must specify which font from the");
      System.out.println("     collection you will read metrics from. If you read");
      System.out.println("     from a .ttc file without this option, the fontnames");
      System.out.println("      will be listed for you.");
      System.out.println(" -fn <fontname>\n");
      System.out.println("     default is to use the fontname in the .ttf file, but\n"+
                         "     you can override that name to make sure that the\n");
      System.out.println("     embedded font is used (if you're embedding fonts)\n");
      System.out.println("     instead of installed fonts when viewing documents with Acrobat Reader.\n");
   }
      
      
    /**
     * The main method for the PFM reader tool.
     * 
     * @param  args Command-line arguments: [options] fontfile.ttf xmlfile.xml
     * where options can be:
     * -fn <fontname>
     *     default is to use the fontname in the .ttf file, but you can override
     *     that name to make sure that the embedded font is used instead of installed
     *     fonts when viewing documents with Acrobat Reader.
     * -cn <classname>
     *     default is to use the fontname
     * -ef <path to the truetype fontfile>
     *     will add the possibility to embed the font. When running fop, fop will look
     *     for this file to embed it
     * -er <path to truetype fontfile relative to org/apache/fop/render/pdf/fonts>
     *     you can also include the fontfile in the fop.jar file when building fop.
     *     You can use both -ef and -er. The file specified in -ef will be searched first,
     *     then the -er file.
     */
    public static void main(String[] args) {
       String embFile=null;
       String embResource=null;
       String className=null;
       String fontName=null;
       String ttcName=null;
       boolean isCid=false;
       
       Hashtable options=new Hashtable();
       String[] arguments=parseArguments(options, args);
       
       TTFReader app = new TTFReader();
       app.invokedStandalone = true;
       
       System.out.println("TTF Reader v1.1");
       System.out.println();

       if (options.get("-enc") != null) {
           String enc = (String)options.get("-enc");
           if ("cid".equals(enc))
               isCid=true;
       }
           
       if (options.get("-ttcname") != null) 
           ttcName=(String)options.get("-ttcname");
           
       if (options.get("-ef") != null)
          embFile=(String)options.get("-ef");
       
       if (options.get("-er") != null)
          embResource=(String)options.get("-er");
       
       if (options.get("-fn") != null)
          fontName=(String)options.get("-fn");
       
       if (options.get("-cn") != null)
          className=(String)options.get("-cn");

       if (arguments.length != 2 ||
           options.get("-h") != null ||
           options.get("-help") != null ||
           options.get("--help") != null)
          displayUsage();
       else {
          TTFFile ttf = app.loadTTF(arguments[0], ttcName);
          if (ttf != null) {
             org.w3c.dom.Document doc = app.constructFontXML(ttf,
                                                             fontName,
                                                             className,
                                                             embResource,
                                                             embFile,
                                                             isCid,
                                                             ttcName);
             
             if (doc != null) {
                app.writeFontXML(doc, arguments[1]);
             }
             
             if (ttf.isEmbeddable())
                 System.out.println("This font contains no embedding license restrictions");
             else
                 System.out.println("** Note: This font contains license retrictions for\n"+
                                    "         embedding. This font can't be embedded.");
             
          }
       }
    }
   
       /**
        * Read a TTF file and returns it as an object.
        *
        * @param   filename The filename of the PFM file.
        * @return  The TTF as an object.
        */
   public TTFFile loadTTF(String fileName, String fontName) {
      TTFFile ttfFile=new TTFFile();
      try {
         System.out.println("Reading " + fileName + "...");
         System.out.println();

         FontFileReader reader = new FontFileReader(fileName);
         ttfFile.readFont(reader, fontName);
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
      return ttfFile;
    }


    /**
     * Writes the generated DOM Document to a file.
     * 
     * @param   doc The DOM Document to save.
     * @param   target The target filename for the XML file.
     */
    public void writeFontXML(org.w3c.dom.Document doc, String target) {
        System.out.println("Writing xml font file " + target + "...");
        System.out.println();

        try {
            OutputFormat format = new OutputFormat(doc);     //Serialize DOM
            FileWriter out = new FileWriter(target);         //Writer will be a String
            XMLSerializer serial = new XMLSerializer(out, format);
            serial.asDOMSerializer();                        // As a DOM Serializer

            serial.serialize(doc.getDocumentElement());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates the font metrics file from the TTF/TTC file.
     * 
     * @param   ttf The PFM file to generate the font metrics from.
     * @return  The DOM document representing the font metrics file.
     */
    public org.w3c.dom.Document constructFontXML(TTFFile ttf, String fontName,
                                                 String className, String resource,
                                                 String file, boolean isCid,
                                                 String ttcName) {
        System.out.println("Creating xml font file...");
        System.out.println();

        Document doc = new DocumentImpl();
        Element root = doc.createElement("font-metrics");
        doc.appendChild(root);
        if (isCid)
            root.setAttribute("type", "TYPE0");
        else
            root.setAttribute("type", "TRUETYPE");
        
        Element el = doc.createElement("font-name");
        root.appendChild(el);

            // Note that the PostScript name usually is something like
            // "Perpetua-Bold", but the TrueType spec says that in the ttf file
            // it should be "Perpetua,Bold".

        String s = stripWhiteSpace(ttf.getPostscriptName());

        if (fontName != null)
           el.appendChild(doc.createTextNode(stripWhiteSpace(fontName)));
        else
            el.appendChild(doc.createTextNode(s));

        el = doc.createElement("embed");
        root.appendChild(el);
        if (file != null && ttf.isEmbeddable())
            el.setAttribute("file", file);
        if (resource != null && ttf.isEmbeddable())
            el.setAttribute("class", resource);

        el = doc.createElement("cap-height");
        root.appendChild(el);
        Integer value = new Integer(ttf.getCapHeight());
        el.appendChild(doc.createTextNode(value.toString()));

        el = doc.createElement("x-height");
        root.appendChild(el);
        value = new Integer(ttf.getXHeight());
        el.appendChild(doc.createTextNode(value.toString()));
        
        el = doc.createElement("ascender");
        root.appendChild(el);
        value = new Integer(ttf.getLowerCaseAscent());
        el.appendChild(doc.createTextNode(value.toString()));
        
        el = doc.createElement("descender");
        root.appendChild(el);
        value = new Integer(ttf.getLowerCaseDescent());
        el.appendChild(doc.createTextNode(value.toString()));

        Element bbox = doc.createElement("bbox");
        root.appendChild(bbox);
        int[] bb = ttf.getFontBBox();
        String[] names = {"left","bottom","right","top"};
        for (int i=0; i<4; i++) {
            el = doc.createElement(names[i]);
            bbox.appendChild(el);
            value = new Integer(bb[i]);
            el.appendChild(doc.createTextNode(value.toString()));
        }

        el = doc.createElement("flags");
        root.appendChild(el);
        value = new Integer(ttf.getFlags());
        el.appendChild(doc.createTextNode(value.toString()));
        
        el = doc.createElement("stemv");
        root.appendChild(el);
        value = new Integer(ttf.getStemV());
        el.appendChild(doc.createTextNode(value.toString()));
        
        el = doc.createElement("italicangle");
        root.appendChild(el);
        value = new Integer(ttf.getItalicAngle());
        el.appendChild(doc.createTextNode(value.toString()));

        if (ttcName != null) {
            el = doc.createElement("ttc-name");
            root.appendChild(el);
            el.appendChild(doc.createTextNode(ttcName));
        }
        
        el = doc.createElement("subtype");
        root.appendChild(el);
        
            // Fill in extras for CID keyed fonts
        if (isCid) {
            el.appendChild(doc.createTextNode("TYPE0"));

            Element mel = doc.createElement("multibyte-extras");
            root.appendChild(mel);
            
            el = doc.createElement("cid-type");
            mel.appendChild(el);
            el.appendChild(doc.createTextNode("CIDFontType2"));

            el = doc.createElement("default-width");
            mel.appendChild(el);
            el.appendChild(doc.createTextNode("0"));

            el = doc.createElement("bfranges");
            mel.appendChild(el);
            for (Enumeration e=ttf.getCMaps().elements(); e.hasMoreElements();) {
                TTFCmapEntry ce = (TTFCmapEntry)e.nextElement();
                Element el2=doc.createElement("bf");
                el.appendChild(el2);
                el2.setAttribute("us", Integer.toString(ce.unicodeStart));
                el2.setAttribute("ue", Integer.toString(ce.unicodeEnd));
                el2.setAttribute("gi", Integer.toString(ce.glyphStartIndex));
            }
            
            el = doc.createElement("cid-widths");
            el.setAttribute("start-index", "0");
            mel.appendChild(el);

            int[] wx = ttf.getWidths();
            for (int i = 0; i < wx.length; i++) {
                Element wxel=doc.createElement("wx");
                wxel.setAttribute("w", Integer.toString(wx[i]));
                el.appendChild(wxel);
            }
        } else {
                // Fill in extras for singlebyte fonts
            el.appendChild(doc.createTextNode("TRUETYPE"));

            Element sel=doc.createElement("singlebyte-extras");
            root.appendChild(sel);
            
            el = doc.createElement("encoding");
            sel.appendChild(el);
            el.appendChild(doc.createTextNode(ttf.getCharSetName()));

            el = doc.createElement("first-char");
            sel.appendChild(el);
            value = new Integer(ttf.getFirstChar());
            el.appendChild(doc.createTextNode(value.toString()));
            
            el = doc.createElement("last-char");
            sel.appendChild(el);
            value = new Integer(ttf.getLastChar());
            el.appendChild(doc.createTextNode(value.toString()));
            
            Element widths = doc.createElement("widths");
            sel.appendChild(widths);
            
            for (short i = ttf.getFirstChar(); i < ttf.getLastChar(); i++) {
                el = doc.createElement("char");
                widths.appendChild(el);
                el.setAttribute("idx", Integer.toString(i));
                el.setAttribute("wdt", Integer.toString(ttf.getCharWidth(i)));
            }
        }
        
            // Get kerning
        Enumeration enum;
        if (isCid)
            enum=ttf.getKerning().keys();
        else
            enum=ttf.getAnsiKerning().keys();
        
        while (enum.hasMoreElements()) {
            Integer kpx1=(Integer)enum.nextElement();

            el=doc.createElement("kerning");
            el.setAttribute("kpx1", kpx1.toString());
            root.appendChild(el);
            Element el2=null;
            
            Hashtable h2;
            if (isCid)
                h2 = (Hashtable)ttf.getKerning().get(kpx1);
            else
                h2 = (Hashtable)ttf.getAnsiKerning().get(kpx1);
            
            for (Enumeration enum2=h2.keys(); enum2.hasMoreElements(); ) {
                Integer kpx2=(Integer)enum2.nextElement();
                if (isCid || kpx2.intValue() < 256) {
                    el2=doc.createElement("pair");
                    el2.setAttribute("kpx2", kpx2.toString());
                    Integer val=(Integer)h2.get(kpx2);
                    el2.setAttribute("kern", val.toString());
                    el.appendChild(el2);
                }
            }
        }
        
        return doc;
    }
        
    
    private String stripWhiteSpace(String s) {
        char[] ch = new char[s.length()];
        s.getChars(0, s.length(), ch, 0);
        StringBuffer stb = new StringBuffer();
        for (int i = 0; i < ch.length; i++)
            if (ch[i] != ' ' && ch[i] != '\r' &&
                ch[i] != '\n' && ch[i] != '\t')
                stb.append(ch[i]);

        return stb.toString();
    }
        
   private String escapeString(String str) {
      StringBuffer esc=new StringBuffer();
      
      for (int i=0; i < str.length(); i++) {
         if (str.charAt(i)=='\\')
            esc.append("\\\\");
         else
            esc.append(str.charAt(i));
      }

      return esc.toString();
   }
}

