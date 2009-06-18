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
/* modified by JKT to integrate into 0.12.0 */

//Title:        BoBoGi FOP
//Version:      
//Copyright:    Copyright (c) 1999
//Author:       Sergio Botti
//Company:      Dibe Elsag
//Description:  xml to pdf converter

package org.apache.fop.image;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class BmpBwImage implements FopImage {
    int X;
    int Y;
    int width;
    int height;
    int pixelwidth;
    int pixelheight;
    String ref;
    boolean color;
    int bitperpixel;
    int[] imagemap;
    int imagestart;
    /*
      Costructor read the header of the bmp file to get the size
      and the other data
      SB
    */
    public BmpBwImage(String href,int x,int y,int w,int h)
    {
	this.ref=href;
	this.X=x;
	this.Y=y;
	
	int wpos=18;
	int hpos=22; //offset positioning for w and height in  bmp files
	int [] headermap = new int[54];
	try{
	    FileInputStream file=new FileInputStream(ref);
	    boolean eof=false;
	    int count=0;
	    while ((!eof) && (count<54) ) {
		int input =file.read();
		if (input==-1)
		    eof=true;
		else
		    headermap[count++]=input;
	    }
	    file.close();
	}catch (IOException e) {System.err.println("Image not found");}
	// gets h & w from headermap
	this.pixelwidth = headermap[wpos]+headermap[wpos+1]*256+headermap[wpos+2]*256*256+headermap[wpos+3]*256*256*256;
	this.pixelheight = headermap[hpos]+headermap[hpos+1]*256+headermap[hpos+2]*256*256+headermap[hpos+3]*256*256*256;
	if (w==0)
	    this.width=this.pixelwidth*1000;
	else
	    this.width=w;
	if (h==0)
	    this.height=this.pixelheight*1000;
	else
	    this.height=h;
	
	this.imagestart =headermap[10]+headermap[11]*256+headermap[12]*256*256+headermap[13]*256*256*256;
	this.bitperpixel=headermap[28];
    }
    
    public String gethref() {		return this.ref;	}
    public int getWidth() {		return this.width;	}
    public int getHeight() {		return this.height;	}
    public int getpixelwidth() {	return this.pixelwidth;	}
    public int getpixelheight() { return this.pixelheight; }
    public int getX(){ return this.X; }
    public int getY(){ return this.Y; }

    public int[] getimagemap(){
	int input;
	int[] temp = new int[nextfourdiv(this.pixelwidth)*(this.pixelheight)];
	try {
	    FileInputStream file = new FileInputStream(this.ref);
	    int count = 0;
	    file.skip((long) this.imagestart);
	    while ((input = file.read()) != -1) {
		temp[count++] = input;
	    }
	    file.close();
	} catch (IOException e) {
	    System.err.println("Image not found");
	}
	int[] map = new int[this.pixelheight * this.pixelwidth];
	int k = 0;
	for (int y = 0; y < this.pixelheight; y++) {
	    for (int x = 0; x < this.pixelwidth; x++)
		map[k++] = temp[y * nextfourdiv(this.pixelwidth) + x];
	}
	return map;
    }

    public boolean getcolor(){return false;}
    public int getbitperpixel() {return this.bitperpixel;} 
    
    private int nextfourdiv(int number) {
	return ((number/4)+1)*4;
    }
    
}
