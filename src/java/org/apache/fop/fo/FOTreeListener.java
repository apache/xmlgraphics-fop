/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo;

//java
import java.util.EventListener;

//sax
import org.xml.sax.SAXException;

//fop
import org.apache.fop.apps.FOPException;

/**
 * An interface for classes that need to handle FOTreeEvent objects as they
 * are fired.
 * The key benefit to using this interface is that the implementation can handle
 * PageSequence objects at a higher level, rather than dealing directly with
 * a lower-level SAX event.
 * @see FOTreeEvent
 */

public interface FOTreeListener extends EventListener {

    /**
     * Method for handling a completed PageSequence object.
     * @param e the FOTreeEvent that has been fired
     * @throws FOPException for invalid input
     */
    void foPageSequenceComplete (FOTreeEvent e) throws FOPException;

    /**
     * Method for handling the end of the document.
     * @param e the FOTreeEvent that has been fired
     * @throws SAXException for parsing error
     */
    void foDocumentComplete (FOTreeEvent e) throws SAXException;

}

