/*
 *
 * Copyright 2004 The Apache Software Foundation.
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
 * Created on 31/01/2004
 * $Id$
 */
package org.apache.fop.datastructs;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class SyncedNode extends Node implements Cloneable {
    
    protected Object sync;
    
    /**
     * @return the sync
     */
    public Object getSync() {
        synchronized (sync) {
            return sync;
        }
    }
    /**
     * @param sync to set
     */
    public void setSync(Object sync) {
        synchronized (this.sync) {
            this.sync = sync;
        }
    }
    /**
     * @param sync the object on which synchronization for this
     * node will occur
     */
    public SyncedNode(Object sync) {
        super();
        this.sync = sync;
    }

    /**
     * No argument constructor.  Assumes that this node will sync on itself.
     */
    public SyncedNode() {
        super();
        this.sync = this;
    }
    /**
     * @param parent <code>Node</code> of this
     * @param index of this in children of parent
     * @param sync the object on which synchronization for this
     * node will occur
     * @throws IndexOutOfBoundsException
     */
    public SyncedNode(Node parent, int index, Object sync)
        throws IndexOutOfBoundsException {
        super(parent, index);
        this.sync = sync;
    }

    /**
     * @param parent
     * @param sync the object on which synchronization for this
     * node will occur
     * @throws IndexOutOfBoundsException
     */
    public SyncedNode(Node parent, Object sync)
        throws IndexOutOfBoundsException {
        super(parent);
        this.sync = sync;
    }


    /**
     * Appends a child to this node.
     *
     * @param child  Node to be added.
     */

    public void addChild(Node child) {
        synchronized (sync) {
            super.addChild(child);
        }
    }
    
    /**
     * Adds a child <tt>Node</tt> in this node at a specified index
     * position.
     *
     * @param index of position of new child
     * @param child to be added
     */
    public void addChild(int index, Node child)
    throws IndexOutOfBoundsException {
        synchronized (sync) {
            super.addChild(index, child);
        }
    }
    /**
     * Removes the child <tt>Node</tt> at the specified index in the
     * ArrayList.
     *
     * @param index  The int index of the child to be removed.
     * @return the node removed.
     */

    public Node removeChildAtIndex(int index) {
        synchronized (sync) {
            return super.removeChildAtIndex(index);
        }
    }

    /**
     * Removes the specified child <tt>Node</tt> from the children
     * ArrayList.
     *
     * Implemented by calling <tt>removeChildAtIndex()</tt>.
     *
     * @param child  The child node to be removed.
     * @return the node removed.
     */

    public Node removeChild(Node child)
    throws NoSuchElementException {
        synchronized (sync) {
            return super.removeChild(child);
        }
    }

    /**
     * Deletes the entire subtree rooted on <tt>this</tt>.
     * The Tree is traversed in PostOrder, and each
     * encountered <tt>Node</tt> has its <i>Tree</i> reference
     * nullified. The <i>parent</i> field, and the parent's child reference
     * to <tt>this</tt>, are nullified only at the top of the subtree.
     * <p>As a result, any remaining reference to any element in the
     * subtree will keep the whole subtree from being GCed.
     */
    public Node deleteSubTree() {
        synchronized (sync) {
            return super.deleteSubTree();
        }
    }

    /**
     * Deletes <code>this</code> subtree and returns a count of the deleted
     * nodes.  The deletion is effected by cutting the references between
     * <code>this</code> and its parent (if any).  All other relationships
     * within the subtree are maintained.
     * @return the number of deleted nodes
     */
    public int deleteCountSubTree() {
        synchronized (sync) {
            return super.deleteCountSubTree();
        }
    }

    /**
     * Gets the parent of this <tt>Node</tt>.
     * @return the parent <tt>Node</tt>.
     */
    public Node getParent() {
        synchronized (sync) {
            return super.getParent();
        }
    }

    /**
     * Sets the <i>parent</i> field of this node.
     * @param parent the reference to set
     */
    public void setParent(Node parent) {
        synchronized (sync) {
            super.setParent(parent);
        }
    }

    /**
     * Nullifies the parent <tt>Node</tt> of this node.
     */
    public void unsetParent() {
        synchronized (sync) {
            super.unsetParent();
        }
    }

    /**
     * Gets the n'th child of this node.
     * @param n - the <tt>int</tt> index of the child to return.
     * @return the <tt>Node</tt> reference to the n'th child.
     */
    public Node getChild(int n) {
        synchronized (sync) {
            return super.getChild(n);
        }
    }

    /**
     * Gets an <tt>Iterator</tt> over the children of this node.
     * @return the <tt>Iterator</tt>.
     */
    public Iterator nodeChildren() {
        synchronized (sync) {
            return super.nodeChildren();
        }
    }

    /**
     * Gets the number of children of this node.
     * @return the <tt>int</tt> number of children.
     */
    public int numChildren() {
        synchronized (sync) {
            return super.numChildren();
        }
    }
    
    /**
     * Gets the preceding sibling of this <code>Node</code>,
     * or <code>null</code> if none.
     * @return the sibling node
     */
    public Node getPrecedingSibling() {
        if (this.parent == null) return null;
        int thisChild = parent.children.indexOf(this);
        if (thisChild == 0) return null;
        return parent.getChild(--thisChild);
    }
    
    /**
     * Gets the following sibling of this <code>Node</code>,
     * or <code>null</code> if none.
     * @return the sibling node
     */
    public Node getFollowingSibling() {
        if (this.parent == null) return null;
        int thisChild = parent.children.indexOf(this);
        if (++thisChild >= parent.numChildren()) return null;
        return parent.getChild(thisChild);
    }
    
    /**
     * Gets the leaf <code>Node</code> immediately preceding this node in the
     * pre-order tree rooted on the <code>nominalRoot</code>, or, if the
     * nominal root is not encountered, the actual root.
     * Climbs the tree rooted at
     * <code>nominalRoot</code> from <code>this</code>, searching for an
     * ancestor with a branch preceding this.
     * If none is found, there is no preceding leaf node.
     * If one is found, it is descended to the last pre-order node,
     * i.e. the leaf most closely preceding <code>this</code>.
     * @param nominalRoot the root node for the purposes of this operation
     * @return the preceding leaf node or <code>null</code>
     */
    public Node precedingLeaf(Node nominalRoot) {
        synchronized (sync) {
            return super.precedingLeaf(nominalRoot);
        }
    }
    
    /**
     * Gets the leaf <code>Node</code> immediately following this node in the
     * post-order tree rooted on the <code>nominalRoot</code>, or, if the
     * nominal root is not encountered, the actual root.
     * Climbs the tree rooted at
     * <code>nominalRoot</code> from <code>this</code>, searching for an
     * ancestor with a branch following this.
     * If none is found, there is no following leaf node.
     * If one is found, it is descended to the first post-order node,
     * i.e. the leaf most closely following <code>this</code>.
     * @param nominalRoot the root node for the purposes of this operation
     * @return the following leaf node or <code>null</code>
     */
    public Node followingLeaf(Node nominalRoot) {
        synchronized (sync) {
            return super.followingLeaf(nominalRoot);
        }
    }
    
   
    
    /**
     * @author pbw
     * @version $Revision$ $Name$
     */
    public class PreOrder extends Node.PreOrder {

        public PreOrder() {
            super(sync);
        }
        
        public boolean hasNext() {
            synchronized (sync) {
                return super.hasNext();
            }
        }

        public Object next() {
            synchronized (sync) {
                return super.next();
            }
        }
        
    }
    
    /**
     * @author pbw
     * @version $Revision$ $Name$
     */
    public class PostOrder extends Node.PostOrder {

        public PostOrder() {
            super(sync);
        }
        
        public boolean hasNext() {
            synchronized (sync) {
                return super.hasNext();
            }
        }

        public Object next() {
            synchronized (sync) {
                return super.next();
            }
        }
    
    }
    
    /**
     * @author pbw
     * @version $Revision$ $Name$
     */
    public class Ancestor extends Node.Ancestor {

        protected Ancestor() {
            super(sync);
        }

        public Object next() throws NoSuchElementException {
            synchronized (sync) {
                return super.next();
            }
        }
}
    
    /**
     * @author pbw
     * @version $Revision$ $Name$
     */
    public class PrecedingSibling extends Node.PrecedingSibling {

        public PrecedingSibling() {
            super(sync);
        }
        
        public boolean hasPrevious() {
            synchronized (sync) {
                return super.hasPrevious();
            }
        }

        public Object previous() throws NoSuchElementException {
            synchronized (sync) {
                return super.previous();
            }
        }

        public int previousIndex() {
            synchronized (sync) {
                return super.previousIndex();
            }
        }
        
    }
    
    
    /**
     * @author pbw
     * @version $Revision$ $Name$
     */
    public class FollowingSibling extends Node.FollowingSibling {
        
        protected FollowingSibling() {
            super(sync);
        }
        
        public boolean hasNext() {
            synchronized (sync) {
                return super.hasNext();
            }
        }

        public Object next() throws NoSuchElementException {
            synchronized (sync) {
                return super.next();
            }
        }

        public int nextIndex() {
            synchronized (sync) {
                return super.nextIndex();
            }
        }
        
    }

    
}
