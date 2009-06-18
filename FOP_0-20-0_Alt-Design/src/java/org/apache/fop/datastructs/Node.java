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

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.ListIterator;

/*
 */


/**
 * Class <tt>Node</tt>, with class <tt>Tree</tt>, provides the
 * structure for a general-purpose tree.</p>
 * <pre>
 * Node
 * +-------------------------+
 * |(Node) parent            |
 * +-------------------------+
 * |ArrayList                |
 * |+-----------------------+|
 * ||(Node) child 0         ||
 * |+-----------------------+|
 * |:                       :|
 * |+-----------------------+|
 * ||(Node) child n         ||
 * |+-----------------------+|
 * +-------------------------+
 * </pre>
 * <p><tt>ArrayList</tt> is used for the list of children because the
 * synchronization is performed "manually" within the individual methods,
 *
 * <p>Note that there is no payload carried by the Node. This class must
 * be subclassed to carry any actual node contents.
 *
 * <p>See <tt>Tree</tt> for the tree-wide support methods and fields.
 * 
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class Node implements Cloneable {

    /** The parent of this node.  If null, this is the root node. */
    protected Node parent;
    /** An array of the children of this node. */
    protected ArrayList children;     // ArrayList of Node
    /** Creation size of the <i>children</i> <tt>ArrayList</tt>. */
    private static final int FAMILYSIZE = 4;
    
    /**
     * This immutable empty array is provided as a convenient class-level
     * synchronization object for circumstances where such synchronization
     * is required.
     */
    public static final boolean[] syncArray = new boolean[0];

    /**
     * No argument constructor.
     * Assumes that this node is the root of a new tree.
     */

    public Node() {
        parent = null;
    }
    
    /**
     * Adds a <code>Node</code> as a child at a given index position among
     * its parent's children.
     * @param parent of this Node
     * @param index of child in parent.  If the parent reference
     * is <code>null</code>, an IndexOutOfBoundsException is thrown.
     */

    public Node(Node parent, int index)
    throws IndexOutOfBoundsException {
        if (parent == null) {
            throw new IndexOutOfBoundsException("Null parent");
        }
        else {
            this.parent = parent;
            parent.addChild(index, this);
        }
    }
    
    /**
     * Adds a <code>Node</code> as a child of the given parent.
     * @param parent of this Node.  if this is
     *               null, the generated Node is assumed to be the root
     *               node. 
     */

    public Node(Node parent) {
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
        }
    }


    /**
     * Appends a child to this node.
     *
     * @param child  Node to be added.
     */

    public void addChild(Node child) {
        if (children == null)
            children = new ArrayList(FAMILYSIZE);
        children.add(child);
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
        if (children == null)
            children = new ArrayList(FAMILYSIZE);
        children.add(index, child);
    }

    /**
     * Inserts a subtree at a specified index position in the child list.
     * @param index position of subtree within children
     * @param subtree to insert
     * @throws IndexOutOfBoundsException
     */
    public void addSubTree(int index, Node subtree)
        throws IndexOutOfBoundsException
    {
        subtree.setParent(this);
        addChild(index, subtree);
    }

    /**
     * Adds a subtree to the child list.
     * @param subtree to insert
     * @throws IndexOutOfBoundsException
     */
    public void addSubTree(Node subtree)
        throws IndexOutOfBoundsException
    {
        subtree.setParent(this);
        addChild(subtree);
    }

    /**
     * Copies a subtree of this tree as a new child of this node.
     *
     * Note that it is illegal to try to copy a subtree to one of
     * its own descendents or itself.
     *
     * This is the public entry to copyCheckSubTree.  It will always
     * perform a check for the attempt to copy onto a descendent or
     * self.  It calls copyCheckSubTree.
     *
     * @param subtree Node at the root of the subtree to be added.
     * @param index int index of child position in Node's children
     */

    public void copySubTree(Node subtree, int index)
        throws TreeException {
        copyCheckSubTree(subtree, index, true);
    }

    /**
     * Copies a subtree of this tree as a new child of this node.
     *
     * Note that it is illegal to try to copy a subtree to one of
     * its own descendents or itself.
     *
     * WARNING: this version of the method assumes that <tt>Node</tt>
     * will be subclassed; <tt>Node</tt> has no contents, so for
     * the tree to carry any data the Node must be subclassed.  As a
     * result, this method copies nodes by performing a <tt>clone()</tt>
     * operation on the nodes being copied, rather than issuing a
     * <tt>new Node(..)</tt> call.  It then adjusts the necessary
     * references to position the cloned node under the correct parent.
     * As part of this process, the method must create a new empty
     * <i>children</i> <tt>ArrayList</tt>.  if this is not done,
     * subsequent <tt>addChild()</tt> operations on the node will affect
     * the original <i>children</i> array.
     *
     * This warning applies to the contents of any subclassed
     * <tt>Node</tt>.  All references in the copied subtree will be to
     * the objects from the original subtree.  If this has undesirable
     * effects, the method must be overridden so that the copied subtree
     * can have its references adjusted after the copy.
     *
     * @param subtree Node at the root of the subtree to be added.
     * @param index int index of child position in Node's children
     * @param checkLoops boolean - should the copy been checked for
     *                     loops.  Set this to true on the first
     *                     call.
     */

    private void copyCheckSubTree(
            Node subtree, int index, boolean checkLoops)
        throws TreeException {
            Node newNode = null;
            if (checkLoops) {
                checkLoops = false;
                if (subtree == this) {
                    throw new TreeException
                            ("Copying subtree onto itself.");
                }

                // Check that subtree is not an ancestor of this.
                Ancestor ancestors =
                        new Ancestor();
                while (ancestors.hasNext()) {
                    if ((Node)ancestors.next() == subtree) {
                        throw new TreeException
                                ("Copying subtree onto descendent.");
                    }
                }
            }

            // Clone (shallow copy) the head of the subtree
            try {
                newNode = (Node)subtree.clone();
            } catch (CloneNotSupportedException e) {
                throw new TreeException(
                        "clone() not supported on Node");
            }

            // Attach the clone to this at the indicated child index
            newNode.parent = this;
            this.addChild(index, newNode);
            if (newNode.numChildren() != 0) {
                // Clear the children arrayList
                newNode.children = new ArrayList(newNode.numChildren());
                // Now iterate over the children of the root of the
                // subtree, adding a copy to the newly created Node
                Iterator iterator = subtree.nodeChildren();
                while (iterator.hasNext()) {
                    newNode.copyCheckSubTree((Node)iterator.next(),
                                             newNode.numChildren(),
                                             checkLoops);
                }
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
        return (Node) children.remove(index);
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
        int index = children.indexOf(child);
        if (index == -1) {
            throw new NoSuchElementException();
        }
        return removeChildAtIndex(index);
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
        if (parent != null) {
            // Not the root node - remove from parent
            parent.removeChild(this);
            unsetParent();
        } // end of else
        return this;
    }

    /**
     * Deletes <code>this</code> subtree and returns a count of the deleted
     * nodes.  The deletion is effected by cutting the references between
     * <code>this</code> and its parent (if any).  All other relationships
     * within the subtree are maintained.
     * @return the number of deleted nodes
     */
    public int deleteCountSubTree() {
        int count = deleteCount(this);
        // nullify the parent reference
        if (parent != null) {
            // Not the root node - remove from parent
            parent.removeChild(this);
            unsetParent();
        }
        return count;
    }

    /**
     * N.B. this private method must only be called from the deleteCountSubTree
     * method, which is synchronized.  In itself, it is not
     * synchronized.
     * The internal relationships between the <tt>Node</tt>s are unchanged.
     * @param subtree Node at the root of the subtree to be deleted.
     * @return int count of Nodes deleted.
     */
    private int deleteCount(Node subtree) {
        int count = 0;
        int numkids = subtree.numChildren();

        for (int i = 0; i < numkids; i++) {
            count += deleteCount((Node)subtree.children.get(i));
        }
        return ++count;
    }

    /**
     * Gets the parent of this <tt>Node</tt>.
     * @return the parent <tt>Node</tt>.
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Sets the <i>parent</i> field of this node.
     * @param parent the reference to set
     */
    public void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * Nullifies the parent <tt>Node</tt> of this node.
     */
    public void unsetParent() {
        parent = null;
    }

    /**
     * Gets the n'th child of this node.
     * @param n - the <tt>int</tt> index of the child to return.
     * @return the <tt>Node</tt> reference to the n'th child.
     */
    public Node getChild(int n) {
        if (children == null) return null;
        return (Node) children.get(n);
    }

    /**
     * Gets an <tt>Iterator</tt> over the children of this node.
     * @return the <tt>Iterator</tt>.
     */
    public Iterator nodeChildren() {
        if (children == null) return null;
        return children.iterator();
    }

    /**
     * Gets the number of children of this node.
     * @return the <tt>int</tt> number of children.
     */
    public int numChildren() {
        if (children == null) return 0;
        return children.size();
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
        if (this == nominalRoot || this.parent == null) {
            return null;
        }
        Node sibling = null;
        Node pivot = this.parent;
        while (pivot != nominalRoot) {
            if ((sibling = pivot.getPrecedingSibling()) != null) {
                break;
            }
            pivot = pivot.parent;
        }
        if (pivot == nominalRoot) { // No preceding leaf node
            return null;
        }
        // We have the pivot preceding sibling - now descend the
        // preceding subtree to the last leaf
        int numChildren;
        while ((numChildren = sibling.numChildren()) > 0) {
            sibling = sibling.getChild(--numChildren);
        }
        return sibling;
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
        if (this == nominalRoot || this.parent == null) {
            return null;
        }
        Node sibling = null;
        Node pivot = this.parent;
        while (pivot != nominalRoot) {
            if ((sibling = pivot.getFollowingSibling()) != null) {
                break;
            }
            pivot = pivot.parent;
        }
        if (pivot == nominalRoot) { // No preceding leaf node
            return null;
        }
        // We have the pivot following sibling - now descend the
        // following subtree to the first leaf
        while (sibling.numChildren() > 0) {
            sibling = sibling.getChild(0);
        }
        return sibling;
    }
    
    /**
     * Class <tt>PreOrder</tt> is a member class of <tt>Node</tt>.
     *
     * It implements the <tt>Iterator</tt> interface, excluding the
     * optional <tt>remove</tt> method.  The iterator traverses its
     * containing <tt>Tree</tt> from its containing Node in
     * preorder order.
     *
     * The method is implemented recursively;
     * at each node, a PreOrder object is instantiated to handle the
     * node itself and to trigger the handing of the node's children.
     * The node is returned first, and then for each child, a new
     * PreOrder is instantiated.  That iterator will terminate when the
     * last descendant of that child has been returned.
     */

    public class PreOrder implements Iterator {
        private boolean selfNotReturned = true;
        private int nextChildIndex = 0;     // N.B. this must be kept as
        // the index of the active child until that child is exhausted.
        // At the start of proceedings, it may already point past the
        // end of the (empty) child vector
        
        private PreOrder nextChildIterator;
        
        /**
         * Constructor for pre-order iterator.
         */
        public PreOrder() {
            hasNextNode();  // A call to set up the initial iterators
            // so that a call to next() without a preceding call to
            // hasNext() will behave sanely
        }
        
        /**
         * Synchronized constructor for pre-order iterator.
         */
        public PreOrder(Object sync) {
            synchronized (sync) {
                hasNextNode();  // A call to set up the initial iterators
                // so that a call to next() without a preceding call to
                // hasNext() will behave sanely
            }
        }
        
        public boolean hasNext() {
            return hasNextNode();
        }
        
        private boolean hasNextNode() {
            if (selfNotReturned) {
                return true;
            }
            // self has been returned - are there any children?
            // if so, we must always have an iterator available
            // even if it is exhausted.  Assume it is set up this 
            // way by next().  The iterator has a chance to do this 
            // because self will always be returned first.
            // The test of nextChildIndex must always be made because
            // there may be no children, or the last child may be
            // exhausted, hence no possibility of an
            // iterator on the children of any child.
            if (nextChildIndex < numChildren()) {
                return nextChildIterator.hasNext(); 
            }
            else { // no kiddies
                return false;
            }
        }

        public Object next() {
            if (! hasNextNode()) {
                throw new NoSuchElementException();
            }
            if (selfNotReturned) {
                selfNotReturned = false;
                if (nextChildIndex < numChildren()) {
                    // We have children - create an iterator
                    // for the first one
                    nextChildIterator = (
                            (Node)(children.get(nextChildIndex))).new
                            PreOrder();
                }
                // else do nothing;
                // the nextChildIndex test in hasNext()
                // will prevent us from getting into trouble
                return Node.this;
            }
            else { // self has been returned
                // there must be a next available, or we would not have
                // come this far
                Object tempNode = nextChildIterator.next();
                // Check for exhaustion of the child
                if (! nextChildIterator.hasNext()) {
                    // child iterator empty - another child?
                    if (++nextChildIndex < numChildren()) {
                        nextChildIterator = (
                                (Node)(children.get(nextChildIndex))).new
                                PreOrder();
                    }
                    else {
                        // nullify the iterator
                        nextChildIterator = null;
                    }
                }
                return (Node) tempNode;
            }
        }
        
        public void remove()
            throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }


    /**
     * Class <tt>PostOrder</tt> is a member class of <tt>Node</tt>.
     *
     * It implements the <tt>Iterator</tt> interface, excluding the
     * optional <tt>remove</tt> method.  The iterator traverses its
     * containing <tt>Tree</tt> from its containing Node in
     * postorder order.
     *
     * The method is implemented recursively;
     * at each node, a PostOrder object is instantiated to handle the
     * node itself and to trigger the handing of the node's children.
     * Firstly, for each child a new PostOrder is instantiated.
     * That iterator will terminate when the last descendant of that
     * child has been returned.  Finally, the node itself is returned.
     */

    public class PostOrder implements Iterator {
        private boolean selfReturned = false;
        private int nextChildIndex = 0;     // N.B. this must be kept as
        // the index of the active child until that child is exhausted.
        // At the start of proceedings, it may already point past the
        // end of the (empty) child vector

        private PostOrder nextChildIterator;
        
        /**
         * Constructor for post-order iterator.
         */
        public PostOrder() {
            hasNextNode();  // A call to set up the initial iterators
            // so that a call to next() without a preceding call to
            // hasNext() will behave sanely
        }
        
        /**
         * Synchronized constructor for post-order iterator.
         * @param sync the object on which to synchronize
         */
        public PostOrder(Object sync) {
            synchronized (sync) {
                hasNextNode();  // A call to set up the initial iterators
                // so that a call to next() without a preceding call to
                // hasNext() will behave sanely
            }
        }
        
        public boolean hasNext() {
            return hasNextNode();
        }
        
        private boolean hasNextNode() {
            // self is always the last to go
            if (selfReturned) { // nothing left
                return false;
            }
            // Check first for children, and set up an iterator if so
            if (nextChildIndex < numChildren()) {
                if (nextChildIterator == null) {
                    nextChildIterator = (
                            (Node)(children.get(nextChildIndex))).new
                            PostOrder();
                }
                // else an iterator exists.
                // Assume that the next() method
                // will keep the iterator current
            }
            return true;
        }

        public Object next() throws NoSuchElementException {
            if (! hasNextNode()) {
                throw new NoSuchElementException();
            }
            // Are there any children?
            if (nextChildIndex < numChildren()) {
                // There will be an active iterator.  Is it empty?
                if (nextChildIterator.hasNext()) {
                    // children remain
                    Object tempNode = nextChildIterator.next();
                    // now check for exhaustion of the iterator
                    if (! nextChildIterator.hasNext()) {
                        if (++nextChildIndex < numChildren()) {
                            nextChildIterator = (
                                    (Node)(children.get(nextChildIndex))).new
                                    PostOrder();
                        }
                        // else leave the iterator bumping on empty
                        // next call will return self
                    }
                    // else iterator not exhausted
                    // return the Node
                    return (Node) tempNode;
                }
                // else children exhausted - fall through
            }
            // No children - return self object
            selfReturned = true;
            nextChildIterator = null;
            return Node.this;
        }

        public void remove()
            throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }


    /**
     * Class <tt>Ancestor</tt> is a member class of <tt>Node</tt>.
     *
     * It implements the <tt>Iterator</tt> interface, excluding the
     * optional <tt>remove</tt> method.  The iterator traverses the
     * ancestors of its containing Node from the Node's immediate
     * parent back to the root Node.
     */

    public class Ancestor implements Iterator {
        
        private Node nextAncestor;
        /**
         * If synchronization is require, sync on the containing
         * <code>Node</code>.
         */
        
        /**
         * Constructor for ancestors iterator.
         */
        public Ancestor() {
            nextAncestor = Node.this.parent;
        }
        
        public Ancestor(Object sync) {
            synchronized (sync) {
                nextAncestor = Node.this.parent;
            }
        }

        public boolean hasNext() {
            return nextAncestor != null;
        }

        public Object next() throws NoSuchElementException {
            Node tmpNode = nextAncestor;
            nextAncestor = tmpNode.parent;
            return tmpNode;
        }

        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }


    /**
     * Class <tt>FollowingSibling</tt> is a member class of <tt>Node</tt>.
     *
     * It implements the <tt>ListIterator</tt> interface, but reports
     * UnsupportedOperationException for all methods except
     * <tt>hasNext()</tt>, <tt>next()</tt> and <tt>nextIndex()</tt>.
     * These methods are implemented as synchronized wrappers around the
     * underlying ArrayList methods.
     *
     * The listIterator traverses those children in the parent node's
     * <tt>children</tt> <tt>ArrayList</tt> which follow the subject
     * node's entry in that array, using the <tt>next()</tt> method.
     */

    public class FollowingSibling implements ListIterator {

        private ListIterator listIterator;
        /**
         * An empty ArrayList for the root listIterator.
         * hasNext() will always return false
         */
        private ArrayList rootDummy = new ArrayList(0);
        
        public FollowingSibling() {
            // Set up iterator on the parent's arrayList of children
            Node refNode = Node.this.parent;
            if (refNode != null) {
                // Not the root node; siblings may exist
                // Set up iterator on the parent's children ArrayList
                ArrayList siblings = refNode.children;
                int index = siblings.indexOf(Node.this);
                // if this is invalid, we are in serious trouble
                listIterator = siblings.listIterator(index + 1);
            } // end of if (Node.this.parent != null)
            else {
                // Root node - no siblings
                listIterator = rootDummy.listIterator();
            }
        }
        
        public FollowingSibling(Object sync) {
            synchronized (sync) {
                // Set up iterator on the parent's arrayList of children
                Node refNode = Node.this.parent;
                if (refNode != null) {
                    // Not the root node; siblings may exist
                    // Set up iterator on the parent's children ArrayList
                    ArrayList siblings = refNode.children;
                    int index = siblings.indexOf(Node.this);
                    // if this is invalid, we are in serious trouble
                    listIterator = siblings.listIterator(index + 1);
                } // end of if (Node.this.parent != null)
                else {
                    // Root node - no siblings
                    listIterator = rootDummy.listIterator();
                }
                
            }
        }
        
        public boolean hasNext() {
            return listIterator.hasNext();
        }

        public Object next() throws NoSuchElementException {
            return listIterator.next();
        }

        public int nextIndex() {
            return listIterator.nextIndex();
        }

        public void add(Object o)
            throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        public void remove()
            throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        public boolean hasPrevious()
            throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        public Object previous()
            throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        public int previousIndex()
            throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        public void set(Object o)
            throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Class <tt>PrecedingSibling</tt> is a member class of <tt>Node</tt>.
     *
     * It implements the <tt>ListIterator</tt> interface, but reports
     * UnsupportedOperationException for all methods except
     * <tt>hasPrevious()</tt>, <tt>previous()</tt> and
     * <tt>previousIndex()</tt>.
     * These methods are implemented as synchronized wrappers around the
     * underlying ArrayList methods.
     *
     * The listIterator traverses those children in the parent node's
     * <tt>children</tt> <tt>ArrayList</tt> which precede the subject
     * node's entry in that array, using the <tt>previous()</tt> method.
     * I.e., siblings are produced in reverse sibling order.
     */

    public class PrecedingSibling implements ListIterator {

        private ListIterator listIterator;
        /**
         * An empty ArrayList for the root listIterator.
         * hasNext() will always return false
         */
        private ArrayList rootDummy = new ArrayList(0);
        
        public PrecedingSibling() {
            // Set up iterator on the parent's arrayList of children
            Node refNode = Node.this.parent;
            if (refNode != null) {
                // Not the root node; siblings may exist
                // Set up iterator on the parent's children ArrayList
                ArrayList siblings = refNode.children;
                int index = siblings.indexOf(Node.this);
                // if this is invalid, we are in serious trouble
                listIterator = siblings.listIterator(index);
            } // end of if (Node.this.parent != null)
            else {
                // Root node - no siblings
                listIterator = rootDummy.listIterator();
            }
        }
        
        protected PrecedingSibling(Object sync) {
            synchronized (sync) {
                // Set up iterator on the parent's arrayList of children
                Node refNode = Node.this.parent;
                if (refNode != null) {
                    // Not the root node; siblings may exist
                    // Set up iterator on the parent's children ArrayList
                    ArrayList siblings = refNode.children;
                    int index = siblings.indexOf(Node.this);
                    // if this is invalid, we are in serious trouble
                    listIterator = siblings.listIterator(index);
                } // end of if (Node.this.parent != null)
                else {
                    // Root node - no siblings
                    listIterator = rootDummy.listIterator();
                }
            }
        }
        
        
        public boolean hasPrevious() {
            return listIterator.hasPrevious();
        }

        public Object previous() throws NoSuchElementException {
            return listIterator.previous();
        }

        public int previousIndex() {
            return listIterator.previousIndex();
        }

        public void add(Object o)
            throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        public void remove()
            throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext()
            throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        public Object next()
            throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        public int nextIndex()
            throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        public void set(Object o)
            throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }

}
