/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

public class Fop {
    public static void main(String[] args) {
        System.out.println("CVS FOP DOES NOT WORK - DO NOT EXPECT ANYTHING");
        CommandLineOptions options = null;

        try {
            options = new CommandLineOptions(args);
            Starter starter = options.getStarter();
            starter.run();
        } catch (FOPException e) {
            if("null".equals(e.getMessage())) {
                System.err.println("NullPointerException");
            } else {
                System.err.println("" + e.getMessage());
            }
            if (options != null && options.isDebugMode().booleanValue()) {
                e.printStackTrace();
            } else {
                System.err.println("Turn on debugging for more information");
            }
        } catch (java.io.FileNotFoundException e) {
            System.err.println("" + e.getMessage());
            if (options != null && options.isDebugMode().booleanValue()) {
                e.printStackTrace();
            } else {
                System.err.println("Turn on debugging for more information");
            }
        }
    }

}

