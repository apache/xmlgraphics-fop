/*
 * $Id$
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 */

package org.apache.fop.datastructs;


/**
 * A generalised tree class.
 *
 * <p>The <tt>Tree</tt> class is analogous to one of the <tt>Collection</tt>
 * classes.  It provides a bag with a certain structure into which objects
 * may be collected for manipulation.
 *
 * <p>The outer class, Tree, is the level at which are defined those fields
 * and methods which are provided for the manipulation of the tree as a
 * whole.  The tree is actually comprised of a collection of <tt>Node</tt>
 * elements.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class Tree {

    /**
     * The number of times the tree has been <i>structurally modified</i>.
     * See the discussion of the <tt>modCount</tt> field in
     * <tt>AbstractList</tt>.
     */
    protected int modCount = 0;

    /**
     * Count of the nodes in this tree.
     */
    protected int nodeCount = 0;
    
    /**
     * The root node of this tree.
     */
    protected Node root = null;

    public Tree() {}

    public int modified() {
        // In the Tree class, this function updates the modCount
        // N.B. This method is always called from within a synchronized
        // method.
        synchronized (this) {
            return ++modCount;
        }
    }

    /**
     * Get the value of the <i>modCount</i> field, used to warn of concurrent
     * modification of the tree during certain unsynchronized operations.
     * @return - the <tt>int</tt> <i>modCount</i>.
     */
    public int getModCount() {
        synchronized (this) {
            return modCount;
        }
    }

    /**
     * Test the <i>modCount</i> field value.
     * @param value - the value to test against <i>modCount</i>.
     * @return <tt>boolean</tt> test result.
     */
    public boolean modCountEqualTo(int value) {
        synchronized (this) {
            return value == modCount;
        }
    }

    /**
     * Get the number of nodes in the tree.
     * @return the number of nodes.
     */
    public int size() {
        return nodeCount;
    }

    /**
     * Is the tree empty?
     * @return <tt>boolean</tt> answer to the question.  Tests whether the
     * root node is <tt>null</tt>.
     */
    public boolean isEmpty() {
        return root == null;
    }

    /**
     * Set the <i>root</i> field.
     * @param root the <tt>Node</tt> which is to be the root of the tree.
     */
    public void setRoot(Node root) {
        this.root = root;
    }

    /**
     * Get the root node of the tree.
     * @return the root <tt>Node</tt>.
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Clear the <i>root</i> field.  I.e., empty the tree.
     */
    public void unsetRoot() {
        root = null;
    }

}
