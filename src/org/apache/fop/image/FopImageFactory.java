/* modified by JKT to integrate into 0.12.0 */

//Title:        BoBoGi FOP
//Version:      x
//Copyright:    Copyright (c) 1999
//Author:       Sergio Botti
//Company:      Dibe Elsag
//Description:  xml to pdf converter


package org.apache.xml.fop.image;

import java.io.FileInputStream;
import java.io.IOException;

public class FopImageFactory {

    public static FopImage Make(String ref,int x,int y, int width, int height) {


	int colorpos=28; //offset positioning for w and height in  bmp files
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
	} catch (IOException e) {System.err.println("Image not found");}
	int bpp=headermap[28];
	if (bpp==8) {
	    return (new BmpBwImage(ref,x,y,width,height));
	}  else if (bpp==24) {
	    return  (new BmpColImage(ref,x,y,width,height));
	}
	System.err.println("Unsupported bmp format");
	
	return null;
	
    }
} 
