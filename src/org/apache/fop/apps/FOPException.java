/* 
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the 
 * LICENSE file included with these sources."
 */

package org.apache.fop.apps;

/**
 * Exception thrown when FOP has a problem
 */
public class FOPException extends Exception {

    private Throwable _exception;
    
    /**
     * create a new FOP Exception
     *
     * @param message descriptive message
     */
    public FOPException(String message) {
        super(message);
    }
    public FOPException(Throwable e) {
        super(e.getMessage());
	_exception = e;
    }
    
    public Throwable getException() 
    {
	return _exception;
    }
}
