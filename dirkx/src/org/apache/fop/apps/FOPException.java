package org.apache.xml.fop.apps;

/**
 * Exception thrown when FOP has a problem
 */
public class FOPException extends Exception {

    /**
     * create a new FOP Exception
     *
     * @param message descriptive message
     */
    public FOPException(String message) {
	super(message);
    }
}
