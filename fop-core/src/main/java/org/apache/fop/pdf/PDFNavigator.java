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

package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Navigation Node Dictionary, which we call a 'navigator'.
 * This class is used to for sub-page navigation.
 */
public class PDFNavigator extends PDFIdentifiedDictionary {

    public abstract static class Resolver {
        private boolean resolved;
        private PDFNavigator navigator;
        private Object extension;
        public Resolver(PDFNavigator navigator, Object extension) {
            this.navigator = navigator;
            this.extension = extension;
        }
        public PDFNavigator getNavigator() {
            return navigator;
        }
        public Object getExtension() {
            return extension;
        }
        public void resolve() {
            if (!resolved) {
                performResolution();
                resolved = true;
            }
        }
        protected void performResolution() {
        }
    }

    private Resolver resolver;

    public PDFNavigator(String id) {
        super(id);
        put("Type", new PDFName("NavNode"));
    }

    @Override
    public int output(OutputStream stream) throws IOException {
        if (resolver != null) {
            resolver.resolve();
        }
        return super.output(stream);
    }

    public void setResolver(Resolver resolver) {
        this.resolver = resolver;
    }

    public void populate(Object nextAction, Object nextNode, Object prevAction, Object prevNode, Object duration) {
        if (nextAction != null) {
            put("NA", nextAction);
        }
        if (nextNode != null) {
            put("Next", nextNode);
        }
        if (prevAction != null) {
            put("PA", prevAction);
        }
        if (prevNode != null) {
            put("Prev", prevNode);
        }
        if (duration != null) {
            put("Dur", duration);
        }
    }

}

