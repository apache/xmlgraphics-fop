

package org.apache.fop.render.pdf.fonts;

import org.apache.fop.render.pdf.Font;
import org.apache.fop.layout.FontDescriptor;
import org.apache.fop.fonts.Glyphs;
import org.apache.fop.pdf.PDFStream;
import org.apache.fop.pdf.PDFWArray;
import org.apache.fop.pdf.PDFCIDFont;
import org.apache.fop.render.pdf.CIDFont;
import org.apache.fop.render.pdf.CMap;
import org.apache.fop.pdf.PDFTTFStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.BufferedInputStream;
import java.util.Hashtable;

/**
 * Generic MultiByte (CID) font
 */
public class MultiByteFont extends CIDFont implements FontDescriptor {
    public String fontName = null;
    public String encoding = "Identity-H";
    
    public int capHeight = 0;
    public int xHeight = 0;
    public int ascender = 0;
    public int descender = 0;
    public int[] fontBBox = {0, 0, 0, 0};
    
    public String embedFileName = null;
    public String embedResourceName = null;
    public PDFTTFStream embeddedFont=null;
    
    public int flags = 4;
    public int stemV = 0;
    public int italicAngle = 0;
    public int missingWidth = 0;
    public int defaultWidth = 0;
    public byte cidType = PDFCIDFont.CID_TYPE2;
    
    public Hashtable kerning=new Hashtable();
    public boolean useKerning = true;
    
    public PDFWArray warray=new PDFWArray();
    public int width[] = null;

    public BFEntry[] bfentries = null;
    

    public MultiByteFont() {}
    
    public final boolean hasKerningInfo() {
        return (useKerning & kerning.isEmpty());
    }
    public final java.util.Hashtable getKerningInfo() {return kerning;}

    public byte getSubType() {
        return org.apache.fop.pdf.PDFFont.TYPE0;
    }

    public String getLang() {return null;}
    public String getPanose() {return null;}
    public int getAvgWidth() {return -1;}
    public int getMinWidth() {return -1;}
    public int getMaxWidth() {return -1;}
    public int getleading() {return -1;}
    public int getStemH() {return 0;}
    public int getMissingWidth() {return missingWidth;}
    public int getDefaultWidth() {return defaultWidth;}
    public String getRegistry() {return "Adobe";}
    public String getOrdering() {return "UCS";}
    public int getSupplement() {return 0;}
    public byte getCidType() {return cidType;}
    public String getCidBaseFont() {return fontName;}
    public String getCharEncoding() {return "Identity-H";}

    public PDFWArray getWidths() {
        return warray;
    }

    public boolean isEmbeddable() {
        return (embedFileName==null && embedResourceName==null) ? false : true;
    }

    
    public PDFStream getFontFile(int i) {
        InputStream instream=null;

        int iniSize = 256000;
        int incSize = 128000;
        // Get file first
        if (embedFileName!=null)
        try {
            File ef = new File(embedFileName);
            iniSize = (int)ef.length()+1;
            incSize = (int)ef.length()/10;
           instream=new FileInputStream(embedFileName);
        } catch (Exception e) {
           System.out.println("Failed to embed fontfile: "+embedFileName);
        }
   
        // Get resource
        if (instream==null && embedResourceName!=null)
        try {
           instream=new BufferedInputStream(this.getClass().getResourceAsStream(embedResourceName));
        } catch (Exception e) {
           System.out.println("Failed to embed fontresource: "+embedResourceName);
        }
        
        if (instream==null)
            return (PDFStream)null;
        
        // Read fontdata
        byte[] file = new byte[iniSize];
        int fsize = 0;

        try {
          int l = instream.read(file, 0, iniSize);
          fsize += l;
      
          if (l==iniSize) {
                 // More to read - needs to extend
             byte[] tmpbuf;
         
             while (l > 0) {
                 tmpbuf = new byte[file.length + incSize];
                 System.arraycopy(file, 0, tmpbuf, 0, file.length);
                 l=instream.read(tmpbuf, file.length, incSize);
                 fsize += l;
                 file = tmpbuf;
            
                 if (l < incSize) // whole file read. No need to loop again
                    l=0;
             }
          }

              // Only TrueType CID fonts are supported now
          embeddedFont=new PDFTTFStream(i, fsize);
          embeddedFont.addFilter("flate");
          embeddedFont.addFilter("ascii-85");
          embeddedFont.setData(file, fsize);
          instream.close();
        } catch (Exception e) {}  

        return (PDFStream) embeddedFont;
    }
    
    public String encoding() {
        return encoding;
    }
    
    public String fontName() {
        return fontName;
    }

    public int getAscender() {return ascender;}
    public int getDescender() {return descender;}
    public int getCapHeight() {return capHeight;}

    public int getAscender(int size) {
        return size * ascender;
    }

    public int getCapHeight(int size) {
        return size * capHeight;
    }

    public int getDescender(int size) {
        return size * descender;
    }

    public int getXHeight(int size) {
        return size * xHeight;
    }

    public int getFlags() {
        return flags;
    }

    public int[] getFontBBox() {
        return fontBBox;
    }

    public int getItalicAngle() {
        return italicAngle;
    }

    public int getStemV() {
        return stemV;
    }

    public int getFirstChar() {
        return 0;
    }

    public int getLastChar() {
        return 255;
    }

    public int width(int i, int size) {
        return size * width[i];
    }

    public int[] getWidths(int size) {
        int[] arr = new int[width.length];
        System.arraycopy(width, 0, arr, 0, width.length-1);
        for( int i = 0; i < arr.length; i++) arr[i] *= size;
        return arr;
    }

    public char mapChar(char c) {
        int idx = (int)c;
        int retIdx = 0;

        for (int i = 0; (i < bfentries.length) && retIdx == 0; i++) {

                /*
                  System.out.println("us: "+bfentries[i].unicodeStart +
                  " ue: "+bfentries[i].unicodeEnd+
                  " gi: "+bfentries[i].glyphStartIndex);
                */
            if (bfentries[i].unicodeStart <= idx &&
                bfentries[i].unicodeEnd >= idx) {
                retIdx=bfentries[i].glyphStartIndex + idx -
                    bfentries[i].unicodeStart;
            }
        }

            //System.out.println("Map: "+ c + " (" + idx + ") = " + retIdx);
        return (char)retIdx;
    }
}

