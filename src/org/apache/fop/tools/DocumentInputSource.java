/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.tools;

import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * This is an InputSource to be used with DocumentReader.
 *
 * @author Kelly A Campbell
 *
 */

public class DocumentInputSource extends InputSource {
    private Document _document;

    public DocumentInputSource() {
        super();
    }

    public DocumentInputSource(Document document) {
        this();
        _document = document;
    }

    public Document getDocument() {
        return _document;
    }

    public void setDocument(Document document) {
        _document = document;
    }

}


