/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

import org.apache.fop.messaging.MessageHandler;

public class Fop {

    public static Runtime runtime;
    public static long startTotal;
    public static long startFree;
    public static long startTime;
    public static long startPCi;
    public static long endPCi;

    public static void main(String[] args) {

	long endtotal, endfree, gctotal, gcfree;
        Driver driver;
        Boolean bool = null;

	runtime = Runtime.getRuntime();
	startTotal = runtime.totalMemory();
	startFree = runtime.freeMemory();
	startTime = System.currentTimeMillis();

        try {
            Options.configure(args);
            driver = new Driver();
            driver.run();
            System.out.println("Back from driver.run()");
	    System.out.println("Elapsed time: " +
				(System.currentTimeMillis() - startTime));
	    endtotal = runtime.totalMemory();
	    endfree = runtime.freeMemory();
	    System.gc();
	    gctotal = runtime.totalMemory();
	    gcfree = runtime.freeMemory();
	    System.out.println("Total memory before run : " + startTotal);
	    System.out.println("Total memory after run  : " + endtotal);
	    System.out.println("Total memory after GC   : " + gctotal);
	    System.out.println("Diff before/after total : "
                                                   + (endtotal - startTotal));
	    System.out.println("Diff before/GC total    : "
                                                   + (gctotal - startTotal));
	    System.out.println("Diff after/GC total     : "
                                                   + (gctotal - endtotal));
	    System.out.println("Free memory before run  : " + startFree);
	    System.out.println("Free memory after run   : " + endfree);
	    System.out.println("Free memory after GC    : " + gcfree);
	    System.out.println("Diff before/after free  : "
                                                   + (endfree - startFree));
	    System.out.println("Diff before/GC free     : "
                                                   + (gcfree - startFree));
	    System.out.println("Diff after/GC free      : "
                                                   + (gcfree - endfree));
	    System.out.println("cg() freed              : "
                                                    + (gcfree - endfree));
	    //System.out.println("PC time     : " + (endPCi - startPCi));
            
        } catch (FOPException e) {
            MessageHandler.errorln("ERROR: " + e.getMessage());
            if ((bool = Options.isDebugMode()) != null
                    && bool.booleanValue()) {
                e.printStackTrace();
            }
        } catch (java.io.FileNotFoundException e) {
            MessageHandler.errorln("ERROR: " + e.getMessage());
            if ((bool = Options.isDebugMode()) != null
                    && bool.booleanValue()) {
                e.printStackTrace();
            }
        }
    }

}

