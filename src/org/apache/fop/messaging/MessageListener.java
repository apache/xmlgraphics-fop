/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.messaging;

/**
 * For situations, where the messages Fop directs to the user have to be handled in some
 * special way, the interface MessageListener and the class MessageEvent are provided.
 * Embedding Fop into a graphical user interface could be such a scenario.<br>
 * Any MessageListener listens for MessageEvents, which contain the user message and
 * also the message type information (progress information or error warning).
 * The class DefaultMessageListener shows an trivial implementation of MessageListener.
 */


public interface MessageListener {

    void processMessage(MessageEvent event);

}

