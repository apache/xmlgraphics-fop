

package org.apache.fop.render.pdf.fonts;

import org.apache.fop.render.pdf.Font;
import org.apache.fop.layout.FontDescriptor;
import org.apache.fop.fonts.Glyphs;
import org.apache.fop.pdf.PDFStream;
import org.apache.fop.pdf.PDFTTFStream;
import org.apache.fop.pdf.PDFT1Stream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.Hashtable;

/**
 * Generic SingleByte font
 */
public class SingleByteFont extends Font implements FontDescriptor {
    public String fontName = null;
    public String encoding = "WinAnsiEncoding";
    
    public int capHeight = 0;
    public int xHeight = 0;
    public int ascender = 0;
    public int descender = 0;
    public int[] fontBBox = {0, 0, 0, 0};
    
    public String embedFileName = null;
    public String embedResourceName = null;
    public PDFStream embeddedFont=null;
    
    public int firstChar = 0;
    public int lastChar = 255;
    public int flags = 4;
    public int stemV = 0;
    public int italicAngle = 0;
    public int missingWidth = 0;
    
    public Hashtable kerning=new Hashtable();
    public boolean useKerning = true;
    
    public int width[] = null;
    public byte subType = 0;
    
    public final boolean hasKerningInfo() {
        return (useKerning & kerning.isEmpty());
    }
    
    public final java.util.Hashtable getKerningInfo() {return kerning;}

    public byte getSubType() {
        return subType;
    }

    public int getAvgWidth() {return -1;}
    public int getMinWidth() {return -1;}
    public int getMaxWidth() {return -1;}
    public int getleading() {return -1;}
    public int getStemH() {return 0;}
    public int getMissingWidth() {return missingWidth;}

    public String getCharEncoding() {return encoding;}

    public boolean isEmbeddable() {
        return (embedFileName==null && embedResourceName==null) ? false : true;
    }

    
    public PDFStream getFontFile(int i) {
        InputStream instream=null;
        
        // Get file first
        if (embedFileName!=null)
        try {
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
        byte[] file = new byte[128000];
        int fsize = 0;

        try {
          int l = instream.read(file, 0, 128000);
          fsize += l;
      
          if (l==128000) {
                 // More to read - needs to extend
             byte[] tmpbuf;
         
             while (l > 0) {
                 tmpbuf = new byte[file.length + 64000];
                 System.arraycopy(file, 0, tmpbuf, 0, file.length);
                 l=instream.read(tmpbuf, file.length, 64000);
                 fsize += l;
                 file = tmpbuf;
            
                 if (l < 64000) // whole file read. No need to loop again
                    l=0;
             }
          }

          if (subType == org.apache.fop.pdf.PDFFont.TYPE1) {
              embeddedFont=new PDFT1Stream(i, fsize);
              ((PDFT1Stream)embeddedFont).setData(file, fsize);
          } else {
              embeddedFont=new PDFTTFStream(i, fsize);
              ((PDFTTFStream)embeddedFont).setData(file, fsize);
          }
          
          embeddedFont.addFilter("flate");
          embeddedFont.addFilter("ascii-85");
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
            //return firstChar;
    }

    public int getLastChar() {
        return lastChar;
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
}

