/*
 * $Id$
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

package org.apache.fop.apps;


/**
 * Exception thrown when FOP has a problem
 */
public class FOPException extends Exception {
    private static final String TAG = "$Name$";
    private static final String REVISION = "$Revision$";

    /**
     * create a new FOP Exception
     * @param message description
     */
    public FOPException(String message) {
        super(message);
    }

    /**
     * create a new FOP Exception
     * @param e incoming Throwable
     */
    public FOPException(Throwable e) {
        super(e);
    }

    /**
     * @param message the description
     * @param e the exception
     */
    public FOPException(String message, Throwable e) {
        super(message, e);
    }

}
