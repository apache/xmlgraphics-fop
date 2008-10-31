package org.apache.fop;

import java.util.Collection;

/**
 * No copy constructor for java.util.Stack so extended and implemented one.
 */
class StateStack extends java.util.Stack {

    private static final long serialVersionUID = 4897178211223823041L;

    /**
     * Default constructor
     */
    public StateStack() {
        super();
    }

    /**
     * Copy constructor
     *
     * @param c initial contents of stack
     */
    public StateStack(Collection c) {
        elementCount = c.size();
        // 10% for growth
        elementData = new Object[
                      (int)Math.min((elementCount * 110L) / 100, Integer.MAX_VALUE)];
        c.toArray(elementData);
    }
}