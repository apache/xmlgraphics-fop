/* 
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the 
 * LICENSE file included with these sources."
 */


package org.apache.fop.apps;

public class Fop {
    public static void main (String [] args) {
        CommandLineOptions options = new CommandLineOptions (args);
        Starter starter = options.getStarter();
        starter.run();
    }
}

