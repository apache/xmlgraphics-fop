/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.messaging;


/**
 * A trivial implementation of a MessageListener
 * For further explanation
 * @see MessageListener
 */


public class DefaultMessageListener implements MessageListener {

    /**
     * The method processMessage has to be overwritten to handle the MessageEvents.
     * The message type (information or error) is accessible via event.getMessageType().
     */

    public void processMessage(MessageEvent event) {
        switch (event.getMessageType()) {
        case MessageEvent.ERROR:
            System.err.print("ERROR: " + event.getMessage());
            break;
        case MessageEvent.LOG:
            System.out.print("LOG: " + event.getMessage());
            break;
        default:
            System.out.print("Unknown message type: " + event.getMessage());
        }
    }

}
