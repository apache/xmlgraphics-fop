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
package org.apache.fop.fonts;
import java.io.*;
import java.util.Hashtable;

public class TTFFile {
   static final byte NTABS = 24;
   static final int NMACGLYPHS = 258;
   static final int MAX_CHAR_CODE = 255;
   static final int ENC_BUF_SIZE = 1024;
   
   boolean is_embeddable=true;
   boolean hasSerifs=true;
   Hashtable dirTabs;
   Hashtable kerningTab;
   
       /** Position inputstream to position indicated
           in the dirtab offset + offset */
   void seek_tab(FontFileReader in, String name, long offset)
      throws IOException {
      TTFDirTabEntry dt=(TTFDirTabEntry)dirTabs.get(name);
      if (dt==null) {
         System.out.println("Dirtab " + name + " not found.");
         return;
      }

      in.seek_set(dt.offset+offset);
   }
   
   int get_ttf_funit(int n) {
      int ret;
      if (n < 0) {
         long rest1=n % upem;
         long storrest=1000*rest1;
         long ledd2=rest1/storrest;
         ret = -((-1000*n)/upem - (int)ledd2);
      } else {
         ret = (n/upem)*1000 + ((n % upem)*1000)/upem;
      }
      
      return ret;
   }

   int upem;
   int ntabs;
   int nhmtx;
   int post_format;
   int loca_format;
   int nglyphs;
   int nmglyphs;
   int names_count;

   

   TTFDirTabEntry dir_tab;
   TTFMtxEntry mtx_tab[];
   int[] mtx_encoded=null;
   boolean reencoded=false;
   String enc_names;

   String fontName="";
   String fullName="";
   String notice="";
   String familyName="";
   String subFamilyName="";
   
   long italicAngle = 0;
   long isFixedPitch = 0;
   int fontBBox1 = 0;
   int fontBBox2 = 0;
   int fontBBox3 = 0;
   int fontBBox4 = 0;
   int capHeight = 0;
   int underlinePosition = 0;
   int underlineThickness = 0;
   int xHeight = 0;
   int ascender = 0;
   int descender = 0;

   public void readFont(FontFileReader in) throws IOException {
      int i, j, k, l, platform_id, encoding_id, language_id;
      long n;
      TTFDirTabEntry[] pd;
      TTFMtxEntry[] pm;
      String[] ps_glyphs_buf;
      
      in.skip(4); // TTF_FIXED_SIZE
      ntabs=in.readTTFUShort();
      in.skip(6); // 3xTTF_USHORT_SIZE;

          // Read Dir tabs
      dirTabs=new Hashtable();
      pd=new TTFDirTabEntry[ntabs];
          //System.out.println("Reading " + ntabs + " dir tables");
      for (i=0; i < ntabs; i++) {
         pd[i]=new TTFDirTabEntry();
         dirTabs.put(pd[i].read(in),
                     pd[i]);
      }

      seek_tab(in, "head", 2*4 + 2*4 + 2);
      upem=in.readTTFUShort();

      in.skip(16);

      fontBBox1=in.readTTFShort();
      fontBBox2=in.readTTFShort();
      fontBBox3=in.readTTFShort();
      fontBBox4=in.readTTFShort();

      in.skip(2+2+2);

      loca_format=in.readTTFShort();

      seek_tab(in, "maxp", 4);
      nglyphs=in.readTTFUShort();
          //System.out.println("nglyphs= " + nglyphs); 
      mtx_tab=new TTFMtxEntry[nglyphs];

      for (i=0; i < nglyphs; i++)
         mtx_tab[i]=new TTFMtxEntry();

      seek_tab(in, "hhea", 4);
      ascender=in.readTTFShort(); // Use sTypoAscender in "OS/2" table?
      descender=in.readTTFShort(); // Use sTypoDescender in "OS/2" table?

      in.skip(2+2+3*2+8*2);
      nhmtx=in.readTTFUShort();
          //System.out.println("nhmtx: " + nhmtx);
      seek_tab(in, "hmtx", 0);
      for (i=0; i < nhmtx; i++) {
         mtx_tab[i].wx=in.readTTFUShort();
         in.skip(2);
      }
          // NB: Her skal det settes mer wx.
      
      seek_tab(in, "post", 0);
      post_format=in.readTTFLong();
      italicAngle=in.readTTFULong();
          //System.out.println("Italic angle: " + italicAngle);
      underlinePosition=in.readTTFShort();
      underlineThickness=in.readTTFShort();
      isFixedPitch=in.readTTFULong();

      in.skip(4*4);

      switch (post_format) {
          case 0x00010000:
                 //System.out.println("Postscript format 1");
             for (i=0; i<Glyphs.mac_glyph_names.length; i++) {
                mtx_tab[i].name=Glyphs.mac_glyph_names[i];
             }
             break;
          case 0x00020000: 
                 //System.out.println("Postscript format 2");
             l = in.readTTFUShort();
             for (i=0; i < l ; i++) {
                mtx_tab[i].index=in.readTTFUShort();
             }
             
             TTFDirTabEntry dirTab=
                (TTFDirTabEntry)dirTabs.get("post");
             if (dirTab==null)
                System.out.println("Can't find table 'post'");

             n=dirTab.length - (in.getCurrentPos() - dirTab.offset);
             ps_glyphs_buf=new String[(int)n];
             int nn=(ps_glyphs_buf.length < nglyphs) ?
                ps_glyphs_buf.length : nglyphs;
                 //System.out.println("Reading " + n + " glyphnames");
             for (i=0; i < nn; i++) {
                ps_glyphs_buf[i]=in.readTTFString(in.readTTFUByte());
             }

             for (i=0; i < l; i++) {
                if (mtx_tab[i].index < NMACGLYPHS) {
                   mtx_tab[i].name = Glyphs.mac_glyph_names[mtx_tab[i].index];
                } else {
                   k = mtx_tab[i].index - NMACGLYPHS ;
                   mtx_tab[i].name=ps_glyphs_buf[k];
                }
             }
             
             break;
          case 0x00030000:
                 //System.out.println("Postscript format 3 - index");
             break;
          default:
                 //System.out.println("Unknown format : " + post_format);
      }

          // Check if font is embeddable
      if (dirTabs.get("OS/2") != null) {
         seek_tab(in, "OS/2", 2*4);
         int fsType=in.readTTFUShort();
         if ((fsType & 2) == 0)
            is_embeddable=false;
         else
            is_embeddable=true;
      } else
         is_embeddable=true;
      
         

      seek_tab(in, "loca", 0);
      for (i=0; i < nglyphs ; i++) {
         mtx_tab[i].offset = (loca_format == 1 ? in.readTTFULong() :
                              (in.readTTFUShort() << 1));
      }

      TTFDirTabEntry dirTab =
         (TTFDirTabEntry)dirTabs.get("glyf");
      for (i=0; i < (nglyphs-1); i++) {
         if (mtx_tab[i].offset != mtx_tab[i+1].offset) {
            in.seek_set(dirTab.offset + mtx_tab[i].offset);
            in.skip(2);
            mtx_tab[i].bbox[0]=in.readTTFShort();
            mtx_tab[i].bbox[1]=in.readTTFShort();
            mtx_tab[i].bbox[2]=in.readTTFShort();
            mtx_tab[i].bbox[3]=in.readTTFShort();
         } else {
            mtx_tab[i].bbox[0]=mtx_tab[0].bbox[0];
            mtx_tab[i].bbox[1]=mtx_tab[0].bbox[1];
            mtx_tab[i].bbox[2]=mtx_tab[0].bbox[2];
            mtx_tab[i].bbox[3]=mtx_tab[0].bbox[3];
         }
      }
      
          //System.out.println("nglyf="+nglyphs+" mtx="+mtx_tab.length);
      
         
      n=((TTFDirTabEntry)dirTabs.get("glyf")).offset;
      for (i=0; i < nglyphs; i++) {
         if ((i+1) >= mtx_tab.length ||
             mtx_tab[i].offset != mtx_tab[i+1].offset) {
            in.seek_set(n+mtx_tab[i].offset);
            in.skip(2);
            mtx_tab[i].bbox[0]=in.readTTFShort();
            mtx_tab[i].bbox[1]=in.readTTFShort();
            mtx_tab[i].bbox[2]=in.readTTFShort();
            mtx_tab[i].bbox[3]=in.readTTFShort();
         } else {
            mtx_tab[i].bbox[0]=mtx_tab[0].bbox[0];
            mtx_tab[i].bbox[1]=mtx_tab[0].bbox[0];
            mtx_tab[i].bbox[2]=mtx_tab[0].bbox[0];
            mtx_tab[i].bbox[3]=mtx_tab[0].bbox[0];
         }
             //System.out.println(mtx_tab[i].toString(this));
      }

      seek_tab(in, "name", 2);
      i = in.getCurrentPos();
      n = in.readTTFUShort();
      j = in.readTTFUShort() + i - 2;
      i += 2*2;

      while (n-- > 0) {
         in.seek_set(i);
         platform_id=in.readTTFUShort();
         encoding_id=in.readTTFUShort();
         language_id=in.readTTFUShort();
             //System.out.println("Platform id: " + language_id);
             //System.out.println("Encoding id: " + language_id);
             //System.out.println("Language id: " + language_id);
         k=in.readTTFUShort();
         l=in.readTTFUShort();

         if ((platform_id==1 && encoding_id==0) &&
             (k==1 || k==2 || k==0 || k==4 || k==6)) {
            in.seek_set(j+in.readTTFUShort());
            String txt = in.readTTFString(l);
            switch (k) {
                case 0: notice=txt; break;
                case 1: familyName=txt; break;
                case 2: subFamilyName=txt;break;
                case 4: fullName=txt; break;
                case 6: fontName=txt; break;
            }
            if (!notice.equals("") && !fullName.equals("") &&
                !fontName.equals("") && !familyName.equals("") &&
                !subFamilyName.equals(""))
               break;
         }
         i+=6*2;
      }
      
      dirTab=
         (TTFDirTabEntry)dirTabs.get("PCLT");
      if (dirTab!=null) {
         in.seek_set(dirTab.offset + 4 + 4 + 2);
         xHeight=in.readTTFUShort();
         in.skip(2*2);
         capHeight=in.readTTFUShort();
         in.skip(2+16+8+6+1+1);
         
         int serifStyle=in.readTTFUByte();
         serifStyle=serifStyle >> 6;
         serifStyle=serifStyle & 3;
         if (serifStyle == 1)
            hasSerifs=false;
         else
            hasSerifs=true;
         
      } else {
             // Approximate capHeight from height of "H"
         for (i=0; i < mtx_tab.length; i++) {
            if ("H".equals(mtx_tab[i].name))
               capHeight=mtx_tab[i].bbox[3]-
                  mtx_tab[i].bbox[1];
         }
      }

          // Read kerning
      kerningTab=new Hashtable();
      dirTab=
         (TTFDirTabEntry)dirTabs.get("kern");
      if (dirTab!=null) {
         seek_tab(in, "kern", 2);
         for (n=in.readTTFUShort(); n>0 ; n--) {
            in.skip(2*2);
            k=in.readTTFUShort();
            if (!((k & 1)!=0) || (k & 2)!=0 || (k & 4)!=0)
               return;
            if ((k >> 8) !=0)
               continue;

            k=in.readTTFUShort();
            in.skip(3 * 2);
            while (k-- > 0) {
               i=in.readTTFUShort();
               j=in.readTTFUShort();
               int kpx=in.readTTFShort();
               if (kpx != 0) {
                  Hashtable adjTab=(Hashtable)kerningTab.get(mtx_tab[i].name);
                  if (adjTab==null)
                     adjTab=new java.util.Hashtable();
                  adjTab.put(mtx_tab[j].name, new Integer((int)get_ttf_funit(kpx)));
                  kerningTab.put(mtx_tab[i].name, adjTab);
               }
            }
         }
             //System.out.println(kerningTab.toString());
      }
   }


   public void printStuff() {
      System.out.println("Font name: " + fontName);
      System.out.println("Full name: " + fullName);
      System.out.println("Family name: " + familyName);
      System.out.println("Subfamily name: " + subFamilyName);
      System.out.println("Notice:    " + notice);
      System.out.println("xHeight:   " + (int)get_ttf_funit(xHeight));
      System.out.println("capheight: " + (int)get_ttf_funit(capHeight));
      
      int italic=(int)(italicAngle>>16);
      System.out.println("Italic: " + italic);
      System.out.print("ItalicAngle: " + (short)(italicAngle/0x10000));
      if ((italicAngle % 0x10000) > 0 )
         System.out.print("."+(short)((italicAngle % 0x10000)*1000)/0x10000);
      System.out.println();
      System.out.println("Ascender:    " + get_ttf_funit(ascender));
      System.out.println("Descender:   " + get_ttf_funit(descender));
      System.out.println("FontBBox:    [" + (int)get_ttf_funit(fontBBox1) +
                         " " + (int)get_ttf_funit(fontBBox2) +
                         " " + (int)get_ttf_funit(fontBBox3) +
                         " " + (int)get_ttf_funit(fontBBox4)+"]");
   }
   
   public static void main(String[] args) {
      try {
         TTFFile ttfFile=new TTFFile();
         FontFileReader reader=
            new FontFileReader(args[0]);

         ttfFile.readFont(reader);
         ttfFile.printStuff();

      } catch (IOException ioe) {
         System.out.println(ioe.toString());
      }
   }

   public String getWindowsName() {
      return new String(familyName+","+subFamilyName);
   }
   public String getPostscriptName() {
      return fontName;
   }
   public String getCharSetName() {
      return "WinAnsi";
   }
   public int getCapHeight() {
      return (int)get_ttf_funit(capHeight);
   }
   public int getXHeight() {
      return (int)get_ttf_funit(xHeight);
   }
   public int getFlags() {
      int flags=32; // Use Adobe Standard charset
      if (italicAngle != 0)
         flags = flags | 64;
      if (isFixedPitch != 0)
         flags = flags | 2;
      if (hasSerifs)
         flags = flags | 1;
      return flags;
   }
   public String getStemV() {
      return "0";
   }
   public String getItalicAngle() {
      String ia=Short.toString((short)(italicAngle/0x10000));
      if ((italicAngle % 0x10000) > 0 )
         ia=ia+("."+Short.toString((short)((short)((italicAngle % 0x10000)*1000)/0x10000)));
      
      return ia;
   }
   public int[] getFontBBox() {
      int[] fbb=new int[4];
      fbb[0]=(int)get_ttf_funit(fontBBox1);
      fbb[1]=(int)get_ttf_funit(fontBBox2);
      fbb[2]=(int)get_ttf_funit(fontBBox3);
      fbb[3]=(int)get_ttf_funit(fontBBox4);
      
      return fbb;
   }
   public int getLowerCaseAscent() {
      return (int)get_ttf_funit(ascender);
   }
   public int getLowerCaseDescent() {
      return (int)get_ttf_funit(descender);
   }
   public short getLastChar() {
      fixWidth();
      return (short)(nmglyphs-1);
   }
   public short getFirstChar() {
      return 0;
   }

   public int getCharWidth(int idx) {
      fixWidth();

      return (int)get_ttf_funit(mtx_encoded[idx]);
   }

   public Hashtable getKerning() {
      return kerningTab;
   }

   public boolean isEmbeddable() {
      return is_embeddable;
   }
   private void fixWidth() {
      if (reencoded)
         return;
      reencoded=true;
          //System.out.println("Reencoding widths");
      nmglyphs=0;
      mtx_encoded=new int[Glyphs.tex8r.length];
      
      Hashtable existingGlyphs=new java.util.Hashtable();
      
      for (int i=0; i < mtx_tab.length; i++) 
         existingGlyphs.put(mtx_tab[i].name, new Integer(mtx_tab[i].wx));

      
      for (int i=0; i < Glyphs.tex8r.length; i++) {
         nmglyphs++;
         Integer wx=(Integer)existingGlyphs.get(Glyphs.tex8r[i]);
         if (wx==null)
            mtx_encoded[i]=0;
         else
            mtx_encoded[i]=wx.intValue();
      }
   }
}

