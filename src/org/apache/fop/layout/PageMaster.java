/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

import java.io.*;

import org.apache.fop.area.PageViewport;

public class PageMaster {

    private PageViewport pageVP ;

    public PageMaster(PageViewport pageVP) {
	this.pageVP = pageVP;
    }


    // Use serialization to make a clone of the master
    public PageViewport makePage() {
	try {
	    System.err.println("PageMaster.makePage");
	    PipedOutputStream outputStream = new PipedOutputStream();
	    PipedInputStream inputStream = new PipedInputStream(outputStream);
	    //System.err.println("PageMaster.makePage made piped streams");

	    ObjectOutputStream objOut =
		new ObjectOutputStream(new BufferedOutputStream(outputStream));
	    /* ObjectInputStream objIn =
	       new ObjectInputStream(new BufferedInputStream(inputStream));*/

	    //System.err.println("PageMaster.makePage: streams made");
	    PageViewport newPageVP = new PageViewport(pageVP.getPage(),
						      pageVP.getViewArea());
	    //System.err.println("PageMaster.makePage: newPageVP made");
	    Thread reader = new Thread(new PageReader(inputStream, newPageVP));
	    //System.err.println("Start serialize");
	    reader.start();

	    //System.err.println("Save page");
	    pageVP.savePage(objOut);
	    objOut.close();
	    //System.err.println("Save page done");
	    reader.join();
	    //System.err.println("join done");

	    // objIn.close();
	    return newPageVP;
	} catch (Exception e) {
	    System.err.println("PageMaster.makePage(): " + e.getMessage());
	    return null;
	}
    }

    static private class PageReader implements Runnable {
	private InputStream is;
	private PageViewport pvp;

	PageReader(InputStream is, PageViewport pvp) {
	    //System.err.println("PageReader object made");
	    this.is = is;
	    this.pvp = pvp;
	}

	public void run() {
	    try {
		//System.err.println("PageReader make ObjectInputStream");
		ObjectInputStream ois = new ObjectInputStream(is);
		//System.err.println("Load page");
		pvp.loadPage(ois);
		//System.err.println("Load page done");
	    } catch (Exception e) {
		System.err.println("Error copying PageViewport: " +
				   e);
	    }
	}
    }

}
