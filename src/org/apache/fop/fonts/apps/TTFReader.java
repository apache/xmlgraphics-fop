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

    static private final String XSL_POSTPROCESS = "TTFPostProcess.xsl";
    static private final String XSL_SORT        = "TTFPostProcessSort.xsl";

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
            i++;
            if (i < args.length)
               options.put(args[i-1], args[i]);
            else
               options.put(args[i-1], "");
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
      System.out.println(" -fn <fontname>\n");
      System.out.println("     default is to use the fontname in the .ttf file, but you can override\n");
      System.out.println("     that name to make sure that the embedded font is used instead of installed\n");
      System.out.println("     fonts when viewing documents with Acrobat Reader.\n");
      System.out.println(" -cn <classname>\n");
      System.out.println("     default is to use the fontname\n");
      System.out.println(" -ef <path to the truetype fontfile>\n");
      System.out.println("     will add the possibility to embed the font. When running fop, fop will look\n");
      System.out.println("     for this file to embed it\n");
      System.out.println(" -er <path to truetype fontfile relative to org/apache/fop/render/pdf/fonts>\n");
      System.out.println("     you can also include the fontfile in the fop.jar file when building fop.\n");
      System.out.println("     You can use both -ef and -er. The file specified in -ef will be searched first,\n");
      System.out.println("     then the -er file.\n");
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
       
       Hashtable options=new Hashtable();
       String[] arguments=parseArguments(options, args);
       
       TTFReader app = new TTFReader();
       app.invokedStandalone = true;
       
       System.out.println("TTF Reader v1.0");
       System.out.println();

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
          TTFFile ttf = app.loadTTF(arguments[0]);
          if (ttf != null) {
             app.preview(ttf);
             
             org.w3c.dom.Document doc = app.constructFontXML(ttf,
                                                             fontName,
                                                             className,
                                                             embResource,
                                                             embFile);
             
             doc = app.postProcessXML(doc);
             if (doc != null) {
                app.writeFontXML(doc, arguments[1]);
             }
          }
       }
    }
   
       /**
        * Read a TTF file and returns it as an object.
        *
        * @param   filename The filename of the PFM file.
        * @return  The PFM as an object.
        */
   public TTFFile loadTTF(String filename) {
      TTFFile ttfFile=new TTFFile();
      try {
         System.out.println("Reading " + filename + "...");
         System.out.println();

         FontFileReader reader = new FontFileReader(filename);
         ttfFile.readFont(reader);
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
      return ttfFile;
    }

    /**
     * Displays a preview of the TTF file on the console.
     * 
     * @param   ttf The TTF file to preview.
     */
    public void preview(TTFFile ttf) {
        PrintStream out = System.out;

        out.print("Font: ");
        out.println(ttf.getWindowsName());
        out.print("Name: ");
        out.println(ttf.getPostscriptName());
        out.print("CharSet: ");
        out.println(ttf.getCharSetName());
        out.print("CapHeight: ");
        out.println(ttf.getCapHeight());
        out.print("XHeight: ");
        out.println(ttf.getXHeight());
        out.print("LowerCaseAscent: ");
        out.println(ttf.getLowerCaseAscent());
        out.print("LowerCaseDescent: ");
        out.println(ttf.getLowerCaseDescent());
        out.print("Having widths for ");
        out.print(ttf.getLastChar()-ttf.getFirstChar());
        out.print(" characters (");
        out.print(ttf.getFirstChar());
        out.print("-");
        out.print(ttf.getLastChar());
        out.println(").");
        out.print("for example: Char ");
        out.print(ttf.getFirstChar());
        out.print(" has a width of ");
        out.println(ttf.getCharWidth(ttf.getFirstChar()));
        out.println();
        if (ttf.isEmbeddable())
           out.println("This font might be embedded");
        else
           out.println("This font might not be embedded");
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
     * Generates the font metrics file from the PFM file.
     * 
     * @param   pfm The PFM file to generate the font metrics from.
     * @return  The DOM document representing the font metrics file.
     */
    public org.w3c.dom.Document constructFontXML(TTFFile ttf, String fontName,
                                                 String className, String resource,
                                                 String file) {
        System.out.println("Creating xml font file...");
        System.out.println();

        Document doc = new DocumentImpl();
        Element root = doc.createElement("font-metrics");
        doc.appendChild(root);

        Element el = doc.createElement("font-name");
        root.appendChild(el);

            // Note that the PostScript name usually is something like
            // "Perpetua-Bold", but the TrueType spec says that in the ttf file
            // it should be "Perpetua,Bold".

        String s = ttf.getPostscriptName();

        if (fontName != null)
           el.appendChild(doc.createTextNode(fontName));
        else
           el.appendChild(doc.createTextNode(s.replace('-', ',')));

        int pos = s.indexOf("-");
        if (pos >= 0) {
           char sb[] = new char[s.length() - 1];
           s.getChars(0, pos, sb, 0);
           s.getChars(pos + 1, s.length(), sb, pos);
           s = new String(sb);
        }
        
        el = doc.createElement("class-name");
        root.appendChild(el);
        if (className != null)
           el.appendChild(doc.createTextNode(className));
        else
           el.appendChild(doc.createTextNode(s));

        el = doc.createElement("embedFile");
        root.appendChild(el);
            //if (file==null || !ttf.isEmbeddable())
        if (file==null)
           el.appendChild(doc.createTextNode("null"));
        else
           el.appendChild(doc.createTextNode("\""+escapeString(file)+"\""));
        
        el = doc.createElement("embedResource");
        root.appendChild(el);
            //if (resource==null || !ttf.isEmbeddable())
        if (resource==null)
           el.appendChild(doc.createTextNode("null"));
        else
           el.appendChild(doc.createTextNode("\""+escapeString(resource)+"\""));

        el = doc.createElement("subtype");
        root.appendChild(el);
        el.appendChild(doc.createTextNode("TRUETYPE"));

        el = doc.createElement("encoding");
        root.appendChild(el);
        el.appendChild(doc.createTextNode(ttf.getCharSetName()+"Encoding"));

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

        el = doc.createElement("first-char");
        root.appendChild(el);
        value = new Integer(ttf.getFirstChar());
        el.appendChild(doc.createTextNode(value.toString()));

        el = doc.createElement("last-char");
        root.appendChild(el);
        value = new Integer(ttf.getLastChar());
        el.appendChild(doc.createTextNode(value.toString()));

        Element widths = doc.createElement("widths");
        root.appendChild(widths);

        for (short i = ttf.getFirstChar(); i < ttf.getLastChar(); i++) {
            el = doc.createElement("char");
            widths.appendChild(el);
                //el.setAttribute("ansichar", "0x00" + Integer.toHexString(i).toUpperCase());
            el.setAttribute("name", "0x00" +
                            Integer.toHexString(i).toUpperCase());
            el.setAttribute("width",
                            new Integer(ttf.getCharWidth(i)).toString());
        }

            // Get kerning
        for (Enumeration enum=ttf.getKerning().keys(); enum.hasMoreElements();) {
           String kpx1=(String)enum.nextElement();
           el=doc.createElement("kerning");
           el.setAttribute("kpx1", kpx1);
           root.appendChild(el);
           Element el2=null;
           
           Hashtable h2=(Hashtable)ttf.getKerning().get(kpx1);
           for (Enumeration enum2=h2.keys(); enum2.hasMoreElements(); ) {
              String kpx2=(String)enum2.nextElement();
              el2=doc.createElement("pair");
              el2.setAttribute("kpx2", kpx2);
              Integer val=(Integer)h2.get(kpx2);
              el2.setAttribute("kern", val.toString());
              el.appendChild(el2);
           }
        }
        return doc;
    }

    /**
     * Modifies the generated font metrics file. First, it processes the
     * character mmappings, then it sorts them.
     * 
     * @param   doc The DOM document representing the font metrics file.
     * @return  A DOM document representing the processed font metrics file.
     */
    public org.w3c.dom.Document postProcessXML(org.w3c.dom.Document doc) {
       if (true)
          return doc;
        try {
            OutputFormat format = new OutputFormat(doc);     //Serialize DOM
            XMLSerializer serial = new XMLSerializer(System.out, format);
            serial.asDOMSerializer();                        // As a DOM Serializer
            serial.serialize(doc.getDocumentElement());
            
            System.out.println("Postprocessing...");
            System.out.println();

           

            InputStream xsl = this.getClass().getResourceAsStream(XSL_POSTPROCESS);
            if (xsl == null) {
                throw new Exception("Resource " + XSL_POSTPROCESS + " not found");
            }
           
            Document targetDoc = new DocumentImpl();
	    org.apache.fop.tools.xslt.XSLTransform.transform(doc, xsl, targetDoc);
	    

            System.out.println("Sorting...");
            System.out.println();

            // Sort the whole thing
            

            xsl = this.getClass().getResourceAsStream(XSL_SORT);
            if (xsl == null) {
                throw new Exception("Resource " + XSL_SORT + " not found");
            }
            

            org.w3c.dom.Document targetDocSorted = new DocumentImpl();

	    org.apache.fop.tools.xslt.XSLTransform.transform(targetDoc, xsl, targetDocSorted);

            return targetDocSorted;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

