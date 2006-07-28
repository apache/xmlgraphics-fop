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

package org.apache.fop.visual;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Redirects the content coming in through an InputStream using a separate thread to a
 * RedirectorLineHandler instance. The default text encoding is used.
 */
public class StreamRedirector implements Runnable {

    private InputStream in;
    private RedirectorLineHandler handler;
    private Exception exception;

    /**
     * @param in the InputStream to read the content from
     * @param handler the handler that receives all the lines
     */
    public StreamRedirector(InputStream in, RedirectorLineHandler handler) {
        this.in = in;
        this.handler = handler;
    }
    
    /**
     * @return true if the run() method was terminated by an exception.
     */
    public boolean hasFailed() {
        return (this.exception != null);
    }
    
    /**
     * @return the exception if the run() method was terminated by an exception, or null
     */
    public Exception getException() {
        return this.exception;
    }

    /** @see java.lang.Runnable#run() */
    public void run() {
        this.exception = null;
        try {
            Reader inr = new java.io.InputStreamReader(in);
            BufferedReader br = new BufferedReader(inr);
            if (handler != null) {
                handler.notifyStart();
            }
            String line = null;
            while ((line = br.readLine()) != null) {
                if (handler != null) {
                    handler.handleLine(line);
                }
            }
            if (handler != null) {
                handler.notifyStart();
            }
        } catch (IOException ioe) {
            this.exception = ioe;
        }
    }
}