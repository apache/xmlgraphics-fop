/*
 * $Id$
 *
 * 
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 * Software Foundation, please see <http://www.apache.org/>.
 *  
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
    //protected int nodeCount = 0;
    
    /**
     * The root node of this tree.
     */
    protected Node root = null;

    public Tree() {}

    public Tree(Node subtree) {
        subtree.setSubTreeTree(this);
        root = subtree;
    }

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
    /*
    public int size() {
        return nodeCount;
    }
    */

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
