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
package org.apache.fop.fonts;

import java.io.*;
import java.util.Hashtable;

/**
 * This class represents a PFM file (or parts of it) as a Java object.
 *
 * @author  jeremias.maerki@outline.ch
 */
public class PFMFile {

    //Header stuff
    private String  windowsName;
    private String  postscriptName;
    private short   dfItalic;
    private int     dfWeight;
    private short   dfCharSet;
    private short   dfPitchAndFamily;
    private int     dfAvgWidth;
    private int     dfMaxWidth;
    private int     dfMinWidth;
    private short   dfFirstChar;
    private short   dfLastChar;

    //Extension stuff
    //---

    //Extend Text Metrics
    private int     etmCapHeight;
    private int     etmXHeight;
    private int     etmLowerCaseAscent;
    private int     etmLowerCaseDescent;

    //Extent table
    private int[]   extentTable;

   private Hashtable kerningTab;
    public PFMFile() {
       kerningTab=new Hashtable();
    }

    /**
     * Parses a PFM file
     * 
     * @param     inStream The stream from which to read the PFM file.
     */
    public void load(InputStream inStream) throws IOException {
        InputStream bufin = new BufferedInputStream(inStream, 1024);
        bufin.mark(1024);
        PFMInputStream in = new PFMInputStream(bufin);
        int version = in.readShort();
        long filesize = in.readInt();
        bufin.reset();

        byte[] buf = new byte[(int)filesize];
        bufin.read(buf, 0, (int)filesize);

        bufin = new ByteArrayInputStream(buf);
        in = new PFMInputStream(bufin);
        loadHeader(in);
        loadExtension(in);
    }

    /**
     * Parses the header of the PFM file.
     *
     * @param     inStream The stream from which to read the PFM file.
     */
    private void loadHeader(PFMInputStream inStream) throws IOException {
        inStream.skip(80);
        dfItalic = inStream.readByte();
        inStream.skip(2);
        dfWeight = inStream.readShort();
        dfCharSet = inStream.readByte();
        inStream.skip(4);
        dfPitchAndFamily = inStream.readByte();
        dfAvgWidth = inStream.readShort();
        dfMaxWidth = inStream.readShort();
        dfFirstChar = inStream.readByte();
        dfLastChar  = inStream.readByte();
        inStream.skip(8);
        long faceOffset   = inStream.readInt();

        inStream.reset();
        inStream.skip(faceOffset);
        windowsName = inStream.readString();

        inStream.reset();
        inStream.skip(117);
    }

    /**
     * Parses the extension part of the PFM file.
     * 
     * @param     inStream The stream from which to read the PFM file.
     */
    private void loadExtension(PFMInputStream inStream) throws IOException {
        int size = inStream.readShort();
        long extMetricsOffset = inStream.readInt();
        long extentTableOffset = inStream.readInt();
        inStream.skip(4);
        long kernPairOffset = inStream.readInt();
        long kernTrackOffset = inStream.readInt();
        long driverInfoOffset = inStream.readInt();

        if (kernPairOffset > 0) {
           inStream.reset();
           inStream.skip(kernPairOffset);
           loadKernPairs(inStream);
        }
        
        inStream.reset();
        inStream.skip(driverInfoOffset);
        postscriptName = inStream.readString();

        if (extMetricsOffset != 0) {
            inStream.reset();
            inStream.skip(extMetricsOffset);
            loadExtMetrics(inStream);
        }
        if (extentTableOffset != 0) {
            inStream.reset();
            inStream.skip(extentTableOffset);
            loadExtentTable(inStream);
        }

    }

    /**
     * Parses the kernPairs part of the pfm file
     * 
     * @param     inStream The stream from which to read the PFM file.
     */
    private void loadKernPairs(PFMInputStream inStream) throws IOException {
        int i = inStream.readShort();

        
        System.out.println(i + " kerning pairs");
        while (i > 0) {
           int g1 = (int)inStream.readByte();
           i--;
               //System.out.print ("Char no: ("+g1+", ");
           
           int g2 = (int)inStream.readByte();
               //System.out.print (g2+") kern");
           
           int adj = inStream.readShort();
           if (adj > 0x8000)
              adj=-(0x10000-adj);
               //System.out.println (": " + adj);

           String glyph1=Glyphs.tex8r[g1];
           String glyph2=Glyphs.tex8r[g2];

           Hashtable adjTab=(Hashtable)kerningTab.get(new Integer(g1));
           if (adjTab==null)
              adjTab=new Hashtable();
           adjTab.put(new Integer(g2), new Integer(adj));
           kerningTab.put(new Integer(g1), adjTab);
        }
    }

    /**
     * Parses the extended metrics part of the PFM file.
     * 
     * @param     inStream The stream from which to read the PFM file.
     */
    private void loadExtMetrics(PFMInputStream inStream) throws IOException {
        int size = inStream.readShort();
        inStream.skip(12);
        etmCapHeight        = inStream.readShort();
        etmXHeight          = inStream.readShort();
        etmLowerCaseAscent  = inStream.readShort();
        etmLowerCaseDescent = inStream.readShort();
    }

    /**
     * Parses the extent table of the PFM file.
     * 
     * @param     inStream The stream from which to read the PFM file.
     */
    private void loadExtentTable(PFMInputStream inStream) throws IOException {
        extentTable = new int[dfLastChar-dfFirstChar];
        dfMinWidth = dfMaxWidth;
        for (short i = dfFirstChar; i < dfLastChar; i++) {
            extentTable[i-dfFirstChar] = inStream.readShort();
            if (extentTable[i-dfFirstChar] < dfMinWidth) {
                dfMinWidth = extentTable[i-dfFirstChar];
            }
        }
    }

    /**
     * Returns the Windows name of the font.
     * 
     * @return The Windows name.
     */
    public String getWindowsName() {
        return windowsName;
    }

       /**
        * Return the kerning table. The kerning table is a hastable with
        * strings with glyphnames as keys, containing hashtables as value.
        * The value hashtable contain a glyph name string key and an Integer value
        */
   public Hashtable getKerning() {
      return kerningTab;
   }
        
    /**
     * Returns the Postscript name of the font.
     * 
     * @return The Postscript name.
     */
    public String getPostscriptName() {
        return postscriptName;
    }

    /**
     * Returns the charset used for the font.
     * 
     * @return The charset (0=WinAnsi).
     */
    public short getCharSet() {
        return dfCharSet;
    }

    /**
     * Returns the charset of the font as a string.
     * 
     * @return The name of the charset.
     */
    public String getCharSetName() {
        switch (dfCharSet) {
            case 0:     return "WinAnsi";
            case 128:   return "Shift-JIS (Japanese)";
            default:    return "Unknown";
        }
    }

    /**
     * Returns the number of the character that defines
     * the first entry in the widths list.
     * 
     * @return The number of the first character.
     */
    public short getFirstChar() {
        return dfFirstChar;
    }

    /**
     * Returns the number of the character that defines
     * the last entry in the widths list.
     * 
     * @return The number of the last character.
     */
    public short getLastChar() {
        return dfLastChar;
    }

    /**
     * Returns the CapHeight parameter for the font (height of uppercase H).
     * 
     * @return The CapHeight parameter.
     */
    public int getCapHeight() {
        return etmCapHeight;
    }

    /**
     * Returns the XHeight parameter for the font (height of lowercase x).
     * 
     * @return The CapHeight parameter.
     */
    public int getXHeight() {
        return etmXHeight;
    }

    /**
     * Returns the LowerCaseAscent parameter for the font (height of lowercase d).
     * 
     * @return The LowerCaseAscent parameter.
     */
    public int getLowerCaseAscent() {
        return etmLowerCaseAscent;
    }

    /**
     * Returns the LowerCaseDescent parameter for the font (height of lowercase p).
     * 
     * @return The LowerCaseDescent parameter.
     */
    public int getLowerCaseDescent() {
        return etmLowerCaseDescent;
    }

    /**
     * Tells whether the font has proportional character spacing.
     * 
     * @return ex. true for Times, false for Courier.
     */
    public boolean getIsProportional() {
        return ((dfPitchAndFamily & 1) == 1);
    }

    /**
     * Returns the bounding box for the font.
     * Note: this value is just an approximation,
     * it does not really exist in the PFM file.
     * 
     * @return The calculated Font BBox.
     */
    public int[] getFontBBox() {
        int[] bbox = new int[4];

        //Just guessing....
        if (!getIsProportional() && (dfAvgWidth == dfMaxWidth)) {
            bbox[0] = -20;
        } else {
            bbox[0] = -100;
        }
        bbox[1] = -(getLowerCaseDescent() + 5);
        bbox[2] = dfMaxWidth + 10;
        bbox[3] = getLowerCaseAscent() + 5;
        return bbox;
    }

    /**
     * Returns the characteristics flags for the font as
     * needed for a PDF font descriptor (See PDF specs).
     * 
     * @return The characteristics flags.
     */
    public int getFlags() {
        int flags = 0;
        if (!getIsProportional()) { flags |= 1; }
        if ((dfPitchAndFamily & 16) == 16) { flags |= 2; }
        if ((dfPitchAndFamily & 64) == 64) { flags |= 4; }
        if (dfCharSet == 0) { flags |= 6; }
        if (dfItalic != 0) { flags |= 7; }
        return flags;
    }

    /**
     * Returns the width of the dominant vertical stems of the font.
     * Note: this value is just an approximation,
     * it does not really exist in the PFM file.
     * 
     * @return The vertical stem width.
     */
    public int getStemV() {
        //Just guessing....
        if (dfItalic != 0) {
            return (int)Math.round(dfMinWidth * 0.25);
        } else {
            return (int)Math.round(dfMinWidth * 0.6);
        }
    }

    /**
     * Returns the italic angle of the font.
     * Note: this value is just an approximation,
     * it does not really exist in the PFM file.
     * 
     * @return The italic angle.
     */
    public int getItalicAngle() {
        if (dfItalic != 0) {
            return -16; //Just guessing....
        } else {
            return 0;
        }
    }

    /**
     * Returns the width of a character
     * 
     * @param  which The number of the character for which the width is requested.
     * @return The width of a character.
     */
    public int getCharWidth(short which) {
        return extentTable[which-dfFirstChar];
    }
}
