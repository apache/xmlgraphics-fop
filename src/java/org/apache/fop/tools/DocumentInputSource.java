/*
 * $Id: DocumentInputSource.java,v 1.2.4.3 2003/06/12 18:19:38 pbwest Exp $
 * 
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 */

package org.apache.fop.tools;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

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


