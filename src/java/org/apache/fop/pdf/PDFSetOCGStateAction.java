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

public class PDFSetOCGStateAction extends PDFNavigatorAction {

    public abstract static class Resolver {
        private boolean resolved;
        private PDFSetOCGStateAction action;
        private Object extension;
        public Resolver(PDFSetOCGStateAction action, Object extension) {
            this.action = action;
            this.extension = extension;
        }
        public PDFSetOCGStateAction getAction() {
            return action;
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

    public PDFSetOCGStateAction(String id) {
        super(id);
        put("Type", new PDFName("Action"));
        put("S", new PDFName("SetOCGState"));
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

    public void populate(Object state, Object preserveRB, Object nextAction) {
        if (state != null) {
            put("State", state);
        }
        if (preserveRB != null) {
            put("PreserveRB", preserveRB);
        }
        if (nextAction != null) {
            put("Next", nextAction);
        }
    }
}
