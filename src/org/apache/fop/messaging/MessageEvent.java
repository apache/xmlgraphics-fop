/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.messaging;

import java.util.EventObject;

/**
 * a container for the text and the type of a message
 * MessageEvents are created by MessageHandler and can be received by any
 * MessageListener, which is added to MessageHandler;
 * @see org.apache.fop.MessageListener MessageListener
 *
 */

public class MessageEvent extends EventObject {
    public static final int LOG = 0;
    public static final int ERROR = 1;
    String message;
    int messageType = MessageEvent.LOG;

    public MessageEvent(Object source) {
        super(source);
        message = (String)source;    // MessageHandler.getMessage()
    }

    /**
     * retrieves the message
     * @return String containing the message
     *
     */
    public String getMessage() {
        return message;
    }

    /**
     * sets the message type
     * @param messageType the type of the message as int in the form of MessageEvent.LOG or MessageEvent.ERROR
     *
     */
    void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    /**
     * returns the type of message as int
     *
     * @return messageType the type of the message as int in the form of MessageEvent.LOG or MessageEvent.ERROR
     */
    public int getMessageType() {
        return messageType;
    }

}

