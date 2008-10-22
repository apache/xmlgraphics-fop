/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.fop.render.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * The document index defined by the MOD:CA architecture provides functions
 * for indexing the document based on document structure and on
 * application-defined document tags.
 */
public class DocumentIndex extends AbstractStructuredAFPObject {
    /**
     * document index element
     */
    private IndexElement indexElement;

    private List linkLogicalElements;
    
    private List tagLogicalElements;
    
    /**
     * {@inheritDoc}
     */
    protected void writeStart(OutputStream os) throws IOException {
    }

    /**
     * {@inheritDoc}
     */
    protected void writeContent(OutputStream os) throws IOException {
        indexElement.writeToStream(os);
        writeObjects(linkLogicalElements, os);
        writeObjects(tagLogicalElements, os);
    }

    /**
     * {@inheritDoc}
     */
    protected void writeEnd(OutputStream os) throws IOException {
    }    

    
    private class IndexElement extends AbstractAFPObject {

        public void writeToStream(OutputStream os) throws IOException {            
        }
        
    }
}
