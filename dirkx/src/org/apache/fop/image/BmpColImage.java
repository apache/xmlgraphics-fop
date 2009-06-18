/* modified by JKT to integrate into 0.12.0 */

//Title:        BoBoGi FOP
//Version:      
//Copyright:    Copyright (c) 1999
//Author:       Sergio Botti
//Company:      Dibe Elsag
//Description:  xml to pdf converter

package org.apache.xml.fop.image;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class BmpColImage implements FopImage {
  int X;
  int Y;
  int width;
  int height;
  int pixelwidth;
  int pixelheight;
  String ref;
  boolean color=true;
  int bitperpixel;
  int[] imagemap;
  int imagestart;
  /*
    Costructor read the header of the bmp file to get the size
    and the other data
    SB
  */
public BmpColImage(String href,int x,int y,int w,int h)
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
	int[] temp = new int[nextfourdiv(this.pixelwidth*3)*(this.pixelheight)];
	try{
	    FileInputStream file=new FileInputStream(this.ref);
	    boolean eof=false;
	    int count=0;
	    file.skip((long)this.imagestart);
	    while (!eof) {
		int input = file.read();
		if (input==-1) {
		    eof=true;
		} else {
		    temp[count++]=input;
		}
	    }
	    file.close();
	} catch (IOException e) {
	    System.err.println("Image not found");
	}

	int[] map =new int[this.pixelheight*this.pixelwidth*3];
	int k=0;

	for (int y=0;y<this.pixelheight;y++) {
		for (int x=0;x<(this.pixelwidth);x++) {
		    map[k++]=temp[y*nextfourdiv(this.pixelwidth*3)+x*3+2];
		    map[k++]=temp[y*nextfourdiv(this.pixelwidth*3)+x*3+1];
		    map[k++]=temp[y*nextfourdiv(this.pixelwidth*3)+x*3];
		    
		    //map[k++]=temp[y*nextfourdiv(this.pixelwidth*3)+x];
		}
        }
	return map;
    }


    public boolean getcolor(){return true;}
    public int getbitperpixel() {return this.bitperpixel;} 

    //
    private int nextfourdiv(int number) {
	int n = number;
	while((n%4)!=0) {
	    n++;
	}
	return n;
    }

}
