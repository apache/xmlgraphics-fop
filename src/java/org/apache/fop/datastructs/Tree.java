/*
   Copyright 2002-2004 The Apache Software Foundation.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 * $Id$
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
     * The root node of this tree.
     */
    protected Node root = null;

    public Tree() {}

    public Tree(Node subtree) {
        root = subtree;
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
