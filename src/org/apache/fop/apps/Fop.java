/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

import org.apache.fop.messaging.MessageHandler;

public class Fop {
    public static void main(String[] args) {

        Driver driver;
        Boolean bool = null;

        try {
            Options.configure(args);
            driver = new Driver();
            driver.run();
            System.out.println("Back from driver.run()");
            
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

