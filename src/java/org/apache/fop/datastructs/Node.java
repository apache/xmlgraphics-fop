/*
   Copyright 2004 The Apache Software Foundation.

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

    protected Node parent;
    protected ArrayList children;     // ArrayList of Node
    /** Creation size of the <i>children</i> <tt>ArrayList</tt>. */
    private static final int FAMILYSIZE = 4;

    /**
     * No argument constructor.
     *
     * Assumes that this node is the root, and so will throw a
     * <tt>TreeException</tt> when the root node in the enclosing
     * <tt>Tree</tt> object is non-null.
     */

    public Node() {
        parent = null;
    }

    /**
     * @param parent Node which is the parent of this Node.  if this is
     *               null, the generated Node is assumed to be the root
     *               node.  If the Tree root node is already set, throws
     *               a <tt>TreeException</tt>.
     * @param index  int index of child in parent.
     */

    public Node(Node parent, int index)
        throws IndexOutOfBoundsException {
        if (parent == null) {
            this.parent = null;
        }
        else {
            this.parent = parent;
            // connect me to my parent
            parent.addChild(index, this);
        }
    }

    /**
     * @param parent Node which is the parent of this Node.  if this is
     *               null, the generated Node is assumed to be the root
     *               node. 
     */

    public Node(Node parent)
        throws IndexOutOfBoundsException {
        if (parent == null) {
            this.parent = null;
        }
        else {
            this.parent = parent;
            // connect me to my parent
            parent.addChild(this);
        }
    }


    /**
     * Appends a child <tt>Node</tt> to this node.  Synchronized on the
     * containing <tt>Tree</tt> object.
     *
     * Calls the <i>modified</i> method of the containing Tree to
     * maintain the value of <i>modCount</i>.
     *
     * @param child  Node to be added.
     */

    public synchronized void addChild(Node child) {
        if (children == null)
            children = new ArrayList(FAMILYSIZE);
        children.add(child);
    }

    /**
     * Adds a child <tt>Node</tt> in this node at a specified index
     * position.
     * Synchronized on the containing <tt>Tree</tt> object.
     *
     * Calls the <i>modified</i> method of the containing Tree to
     * maintain the value of <i>modCount</i>.
     *
     * @param index  int position of new child
     * @param child  Node to be added.
     */

    public synchronized void addChild(int index, Node child)
    throws IndexOutOfBoundsException {
        if (children == null)
            children = new ArrayList(FAMILYSIZE);
        children.add(index, child);
        
    }

    /**
     * Insert a subtree at a specified index position in the child list.
     * Takes advantage of the synchronization of the actual <i>addChild</i> call.
     */
    public void addSubTree(int index, Node subtree)
        throws IndexOutOfBoundsException
    {
        // The subtree must be traversed, and the tree references set
        // to the Tree of this node.
        subtree.setParent(this);
        addChild(index, subtree);
    }

    /**
     * Add a subtree to the child list.
     * Takes advantage of the synchronization of the actual <i>addChild</i> call.
     */
    public void addSubTree(Node subtree)
        throws IndexOutOfBoundsException
    {
        // The subtree must be traversed, and the tree references set
        // to the Tree of this node.
        subtree.setParent(this);
        addChild(subtree);
    }

    /**
     * Copies a subtree of this tree as a new child of this node.
     * Synchronized on the containing <tt>Tree</tt> object.
     *
     * Calls the <tt>modified</tt> method of the containing Tree to
     * maintain the value of <tt>modCount</tt>.
     *
     * Note that it is illegal to try to copy a subtree to one of
     * its own descendents or itself.  (This restriction could be lifted
     * by creating a new Tree containing the subtree, and defining an
     * attachTree() method to attach one Tree to another.)
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
     * its own descendents or itself.  (This restriction could be lifted
     * by creating a new Tree containing the subtree, and defining an
     * attachTree() method to attach one Tree to another.)
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

    private synchronized void copyCheckSubTree(
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

    public synchronized Node removeChildAtIndex(int index) {
        
            Node tmpNode = (Node) children.remove(index);
            return tmpNode;
        
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

    public synchronized Node removeChild(Node child)
        throws NoSuchElementException {
        
            int index = children.indexOf(child);
            if (index == -1) {
                throw new NoSuchElementException();
            }
            Node tmpNode = removeChildAtIndex(index);
            return tmpNode;
        
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
    public synchronized Node deleteSubTree() {
        if (parent != null) {
            // Not the root node - remove from parent
            parent.removeChild(this);
            unsetParent();
        } // end of else
        return this;
    }

    /**
     * Delete <code>this</code> subtree and return a count of the deleted
     * nodes.  The deletion is effected by cutting the references between
     * <code>this</code> and its parent (if any).  All other relationships
     * within the subtree are maintained.
     * @return the number of deleted nodes
     */
    public synchronized int deleteCountSubTree() {
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
     * Get the parent of this <tt>Node</tt>.
     * @return the parent <tt>Node</tt>.
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Set the <i>parent</i> field of this node.
     * @param parent - the <tt>Node</tt> reference to set.
     */
    public void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * Nullify the parent <tt>Node</tt> of this node.
     */
    public void unsetParent() {
        parent = null;
    }

    /**
     * Get the n'th child of this node.
     * @param n - the <tt>int</tt> index of the child to return.
     * @return the <tt>Node</tt> reference to the n'th child.
     */
    public Node getChild(int n) {
        if (children == null) return null;
        return (Node) children.get(n);
    }

    /**
     * Get an <tt>Iterator</tt> over the children of this node.
     * @return the <tt>Iterator</tt>.
     */
    public Iterator nodeChildren() {
        if (children == null) return null;
        return children.iterator();
    }

    /**
     * Get the number of children of this node.
     * @return the <tt>int</tt> number of children.
     */
    public int numChildren() {
        if (children == null) return 0;
        return children.size();
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
            hasNext();  // A call to set up the initial iterators
            // so that a call to next() without a preceding call to
            // hasNext() will behave sanely
        }
        
        public synchronized boolean hasNext() {
            // synchronize this against possible changes to the tree
            
            if (selfNotReturned) {
                return true;
            }
            // self has been returned - are there any children?
            // if so, we must always have an iterator available
            // even unless it is exhausted.  Assume it is set up this 
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

        public synchronized Object next()
        throws NoSuchElementException {
            
            // synchronize the whole against changes to the tree
            
            if (! hasNext()) {
                throw new NoSuchElementException();
            }
            if (selfNotReturned) {
                selfNotReturned = false;
                if (nextChildIndex < numChildren()) {
                    // We have children - create an iterator
                    // for the first one
                    nextChildIterator =
                        ((Node)
                        (children.get(nextChildIndex))).new
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
                /* ---------------------------------
                if (! nextChildIterator.hasNext()) {
                    // last iterator was exhausted;
                    // if there was another child available, an
                    //iterator would already have been set up.
                    // Every iterator will return at least one node -
                    // the node on which it is defined.
                    // So why did the initial hasNext succeed?
                    throw new NoSuchElementException(
                    "Cannot reach this");
                }
                ----------------------------------- */
                Object tempNode = nextChildIterator.next();
                // Check for exhaustion of the child
                if (! nextChildIterator.hasNext()) {
                    // child iterator empty - another child?
                    if (++nextChildIndex < numChildren()) {
                        nextChildIterator =
                            ((Node)
                            (children.get(nextChildIndex))).new
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
     * child has been returned.  finally, the node itself is returned.
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
            hasNext();  // A call to set up the initial iterators
            // so that a call to next() without a preceding call to
            // hasNext() will behave sanely
        }

        public synchronized boolean hasNext() {
            // Synchronize this against changes in the tree
            // self is always the last to go
            if (selfReturned) { // nothing left
                return false;
            }
            
            // Check first for children, and set up an iterator if so
            if (nextChildIndex < numChildren()) {
                if (nextChildIterator == null) {
                    nextChildIterator =
                        ((Node)
                        (children.get(nextChildIndex))).new
                        PostOrder();
                }
                // else an iterator exists.
                // Assume that the next() method
                // will keep the iterator current
            } // end of Any children?
            
            return true;
        }

        public synchronized Object next()
        throws NoSuchElementException {
            // synchronize the whole against changes to the tree
            if (! hasNext()) {
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
                            nextChildIterator =
                                ((Node)
                                (children.get(nextChildIndex))).new
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
         * Constructor for ancestors iterator.
         */
        public Ancestor() {
            nextAncestor = Node.this.parent;
        }

        public boolean hasNext() {
            return nextAncestor != null;
        }

        public synchronized Object next()
        throws NoSuchElementException {
            // The tree is a
            // potentially dynanic structure, which could be
            // undergoing modification as this method is being
            // executed.
            Node tmpNode = nextAncestor;
            nextAncestor = tmpNode.parent;
            return tmpNode;
            
        }

        public void remove()
            throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }


    /**
     * Class <tt>FollowingSibling</tt> is a member class of <tt>Node</tt>.
     *
     * It implements the <tt>ListIterator</tt> interface, but has reports
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
        private ArrayList rootDummy = new ArrayList();
        // An empty ArrayList for the root listIterator
        // hasNext() will always return false

        public FollowingSibling() {
            synchronized (this) {
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

        public synchronized boolean hasNext() {
            return listIterator.hasNext();
        }

        public synchronized Object next()
        throws NoSuchElementException {
            // N.B. synchronization here is on the Node containing
            // the children ArryList of interest.  Other ArrayList operations
            // throughout the tree are also synchronized on the Node object.
            return listIterator.next();
        }

        public synchronized int nextIndex() {
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
     * It implements the <tt>ListIterator</tt> interface, but has reports
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
        private ArrayList rootDummy = new ArrayList();
        // An empty ArrayList for the root listIterator
        // hasNext() will always return false

        public PrecedingSibling() {
            synchronized (this) {
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

        public synchronized boolean hasPrevious() {
            return listIterator.hasPrevious();
        }

        public synchronized Object previous()
            throws NoSuchElementException {
            // N.B. synchronization here is on the Node containing
            // the children ArryList of interest.  Other ArrayList operations
            // throughout the tree are also synchronized on the Node object.
            return listIterator.previous();
        }

        public synchronized int previousIndex() {
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
