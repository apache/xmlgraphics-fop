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
 
package org.apache.fop.render.txt;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Helper class for text streams.
 */
public class TXTStream {
    
    private OutputStream out = null;
    private boolean doOutput = true;

    /**
     * Main constructor.
     * @param os OutputStream to write to
     */
    public TXTStream(OutputStream os) {
        out = os;
    }

    /**
     * Adds a String to the OutputStream
     * @param str String to add
     */
    public void add(String str) {
        if (!doOutput) {
            return;
        }

        try {
            byte buff[] = str.getBytes("UTF-8");
            out.write(buff);
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * Controls whether output is actually written.
     * @param doout true to enable output, false to suppress
     */
    public void setDoOutput(boolean doout) {
        doOutput = doout;
    }

}

