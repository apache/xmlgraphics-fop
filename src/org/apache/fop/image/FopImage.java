/* modified by JKT to integrate into 0.12.0 */

//Title:        BoBoGi FOP
//Version:      
//Copyright:    Copyright (c) 1999
//Author:       Sergio Botti
//Company:      Dibe Elsag
//Description:  Part in xml to pdf converter


package org.apache.xml.fop.image;

public interface FopImage {
  public int getpixelwidth();
  public int getpixelheight();
  public int getWidth();
  public int getHeight();
  public int getX();
  public int getY();
  public String gethref();
  public int[] getimagemap();
  public boolean getcolor();
  public int getbitperpixel();
}
