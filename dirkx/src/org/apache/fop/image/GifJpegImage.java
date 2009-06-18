/* modified by JKT to integrate into 0.12.0 */

//Title:        BoBoGi FOP
//Version:      
//Copyright:    Copyright (c) 1999
//Author:       Sergio Botti
//Company:      Dibe Elsag
//Description:  xml to pdf converter


package org.apache.xml.fop.image;

import java.util.Hashtable;
import java.net.URL;
import java.io.IOException;
import java.io.PrintWriter;
import java.awt.image.*;
import java.awt.Image;
import java.awt.Toolkit;

public class GifJpegImage implements FopImage {
  int X;
  int Y;
  int width;
  int height;
  int pixelwidth;
  int pixelheight;
  String ref;
  boolean color=true;
  int bitperpixel=8;
  int[] imagemap;
  int[] tempmap;
    /*
    Costructor read the header of the bmp file to get the size
    and the other data
    SB
  */

public GifJpegImage(String href,int x,int y,int w,int h)
{
  this.ref=href;
  this.X=x;
  this.Y=y;
  this.pixelheight=-1;
  this.pixelwidth=-1;
  try {
	    URL url = new URL(href);
	    ImageProducer ip = (ImageProducer)url.getContent();
	    FopImageConsumer consumer = new FopImageConsumer();
	    ip.startProduction(consumer);
	    while ((this.pixelheight = consumer.getHeight())==-1) {}
	    while ((this.pixelwidth = consumer.getWidth())==-1) {}
	    this.tempmap = new int[this.pixelwidth*this.pixelheight];
      //Image img=Toolkit.getDefaultToolkit().getImage("prova.gif");
    //  Image img=Toolkit.getDefaultToolkit().getImage(url);
	    PixelGrabber pg = new PixelGrabber(ip,0,0,this.pixelwidth,this.pixelheight,this.tempmap,0,w);
	    try {
          pg.grabPixels();
      }catch (InterruptedException e) {System.err.println("Image grabbing interrupted");}
	} catch (ClassCastException e) {System.err.println("Image format not supported: " + href);
	} catch (Exception e) {System.err.println("Error loading image " + href + " : " +e);
	}
  if (w==0)
     this.width=this.pixelwidth*1000;
  else
      this.width=w;
  if (h==0)
     this.height=this.pixelheight*1000;
  else
      this.height=h;
}
//
public static class FopImageConsumer implements ImageConsumer {
	int width = -1;
	int height = -1;
	public void imageComplete(int status) {}
	public void setColorModel(ColorModel model) {}
	public void setDimensions(int width, int height) {
	    this.width = width;
	    this.height = height;
	}
	public void setHints(int hintflags) {}
	public void setPixels(int x, int y, int w, int h,ColorModel model, byte[] pixels,int off, int scansize) {}
	public void setPixels(int x, int y, int w, int h,ColorModel model, int[] pixels,int off, int scansize) {}
	public void setProperties(Hashtable props) {}
	public int getWidth() { return this.width; }
	public int getHeight() { return this.height; }
}

//
  public String gethref() {		return this.ref;	}
  public int getWidth() {		return this.width;	}
  public int getHeight() {		return this.height;	}
  public int getpixelwidth() {	return this.pixelwidth;	}
  public int getpixelheight() { return this.pixelheight; }
  public int getX(){ return this.X; }
  public int getY(){ return this.Y; }

  public int[] getimagemap(){
         this.imagemap=new int[this.pixelheight*this.pixelwidth*3];
         int count=0;
         int i;
         for(i=0;i<(this.pixelheight*this.pixelwidth);i++)
                 {
                 int red   = ((this.tempmap[i]>>16) & 0xff);
                 int green = ((this.tempmap[i]>> 8) & 0xff);
                 int blue  = ((this.tempmap[i]    ) & 0xff);
                 this.imagemap[count++]=red;
                 this.imagemap[count++]=green;
                 this.imagemap[count++]=blue;
                 }
  return imagemap;
  }


  public boolean getcolor(){return true;}
  public int getbitperpixel() {return this.bitperpixel;}
  }


