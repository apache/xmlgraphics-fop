/*
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 * Software Foundation, please see <http://www.apache.org/>.
 * 
 * $Id$
 */

package org.apache.fop.apps;

import java.util.logging.Logger;

public class Fop {

    public static Runtime runtime;
    public static long startTotal;
    public static long startFree;
    public static long startTime;
    public static long startPCi;
    public static long endPCi;
    
    /**
     * The top-level package for FOP
     */
    public static final String fopPackage = "org.apache.fop";
    
    protected static final Logger logger = Logger.getLogger(fopPackage);

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
            logger.warning(e.getMessage());
            if ((bool = Options.isDebugMode()) != null
                    && bool.booleanValue()) {
                e.printStackTrace();
            }
        } catch (java.io.FileNotFoundException e) {
            logger.warning(e.getMessage());
            if ((bool = Options.isDebugMode()) != null
                    && bool.booleanValue()) {
                e.printStackTrace();
            }
        }
    }

    private Fop() {
    }

}

