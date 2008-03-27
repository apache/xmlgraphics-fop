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
 
package org.apache.fop.render.ps.extensions;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.ElementMapping;

/**
 * This class provides the element mapping for the PostScript-specific extensions.
 */
public class PSExtensionElementMapping extends ElementMapping {

    /** Namespace for the extension */
    public static final String NAMESPACE = "http://xmlgraphics.apache.org/fop/postscript"; 

    /** Main constructor */
    public PSExtensionElementMapping() {
        this.namespaceURI = NAMESPACE;
    }

    /** {@inheritDoc} */
    protected void initialize() {
        if (foObjs == null) {
            foObjs = new java.util.HashMap();
            foObjs.put(PSSetupCodeElement.ELEMENT, new PSSetupCodeMaker());
            foObjs.put(PSPageSetupCodeElement.ELEMENT, new PSPageSetupCodeMaker());
            foObjs.put(PSSetPageDeviceElement.ELEMENT, new PSSetPageDeviceMaker());
            foObjs.put(PSCommentBefore.ELEMENT, new PSCommentBeforeMaker());
            foObjs.put(PSCommentAfter.ELEMENT, new PSCommentAfterMaker());
        }
    }

    static class PSSetupCodeMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PSSetupCodeElement(parent);
        }
    }

    static class PSPageSetupCodeMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PSPageSetupCodeElement(parent);
        }
    }

    static class PSSetPageDeviceMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PSSetPageDeviceElement(parent);
        }
    }
    
    static class PSCommentBeforeMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PSCommentBeforeElement(parent);
        }
    }

    static class PSCommentAfterMaker extends ElementMapping.Maker {
        public FONode make(FONode parent) {
            return new PSCommentAfterElement(parent);
        }
    }
}
