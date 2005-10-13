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
 
package org.apache.fop.render.pcl;

import java.io.IOException;
import java.io.OutputStream;

public class PCLStream {
    
    private OutputStream out = null;
    private boolean doOutput = true;

    public PCLStream(OutputStream os) {
        out = os;
    }

    public void add(String str) {
        if (!doOutput) {
            return;
        }

        byte buff[] = new byte[str.length()];
        int countr;
        int len = str.length();
        for (countr = 0; countr < len; countr++) {
            buff[countr] = (byte)str.charAt(countr);
        }
        try {
            out.write(buff);
        } catch (IOException e) {
            // e.printStackTrace();
            // e.printStackTrace(System.out);
            throw new RuntimeException(e.toString());
        }
    }

    public void setDoOutput(boolean doout) {
        doOutput = doout;
    }

}
