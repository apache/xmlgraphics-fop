/*
 * $Id$
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 */

package org.apache.fop.datastructs;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.ListIterator;

/*
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
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
 * <p>See <tt>Tree</tt> for the tree-wide support methods and fields.</p>
 */

public class Node implements Cloneable {

    private Tree tree;
    private Node parent;
    private ArrayList children;     // ArrayList of Node
    /** Creation size of the <i>children</i> <tt>ArrayList</tt>. */
    private static final int FAMILYSIZE = 4;

    /**
     * No argument constructor.
     *
     * Assumes that this node is the root, and so will throw a
     * <tt>TreeException</tt> when the root node in the enclosing
     * <tt>Tree</tt> object is non-null.
     */

    public Node(Tree tree)
        throws TreeException {
        if (tree.getRoot() != null) {
            throw new TreeException(
                    "No arg constructor invalid when root exists");
        }
        this.tree = tree;
        parent = null;
        tree.setRoot(this);
        //children = new ArrayList();
    }

    /**
     * @param tree the <tt>Tree</tt> to which this Node belongs.
     * @param parent Node which is the parent of this Node.  if this is
     *               null, the generated Node is assumed to be the root
     *               node.  If the Tree root node is already set, throws
     *               a <tt>TreeException</tt>.
     * @param index  int index of child in parent.
     */

    public Node(Tree tree, Node parent, int index)
        throws TreeException, IndexOutOfBoundsException {
        this.tree = tree;
        //children = new ArrayList();
        if (parent == null) {
            if (tree.root != null) {
                throw new TreeException("Null Node constructor "
                                        + "invalid when root exists");
            }
            this.parent = null;
            tree.setRoot(this);
        }
        else {
            // The parent must be a node in the current tree
            if (parent.getTree() != tree) {
                throw new TreeException("Parent not in same tree");
            }
            this.parent = parent;
            // connect me to my parent
            parent.addChild(index, this);
        }
    }

    /**
     * @param tree the <tt>Tree</tt> to which this Node belongs.
     * @param parent Node which is the parent of this Node.  if this is
     *               null, the generated Node is assumed to be the root
     *               node.  If the Tree root node is already set, throws
     *               a <tt>TreeException</tt>.
     */

    public Node(Tree tree, Node parent)
        throws TreeException, IndexOutOfBoundsException {
        this.tree = tree;
        //children = new ArrayList();
        if (parent == null) {
            if (tree.getRoot() != null) {
                throw new TreeException("Null Node constructor "
                                        + "invalid when root exists");
            }
            this.parent = null;
            tree.setRoot(this);
        }
        else {
            // The parent must be a node in the current tree
            if (parent.getTree() != tree) {
                throw new TreeException("Parent not in same tree");
            }
            this.parent = parent;
            // connect me to my parent
            parent.addChild(this);
        }
    }


    /**
     * Appends a child <tt>Node</tt> to this node.  Synchronized on the
     * containing <tt>Tree</tt> object.
     *
     * Calls the <tt>modified</tt> method of the containing Tree to
     * maintain the value of <tt>modCount</tt>.
     *
     * @param child  Node to be added.
     */

    public void addChild(Node child) {
        synchronized (tree) {
            if (children == null)
                children = new ArrayList(FAMILYSIZE);
            children.add((Object) child);
            tree.modified();
        }
    }

    /**
     * Adds a child <tt>Node</tt> in this node at a specified index
     * position.
     * Synchronized on the containing <tt>Tree</tt> object.
     *
     * Calls the <tt>modified</tt> method of the containing Tree to
     * maintain the value of <tt>modCount</tt>.
     *
     * @param index  int position of new child
     * @param child  Node to be added.
     */

    public void addChild(int index, Node child)
        throws IndexOutOfBoundsException {
        synchronized (tree) {
            if (children == null)
                children = new ArrayList(FAMILYSIZE);
            children.add(index, (Object) child);
            tree.modified();
        }
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
        throws TreeException, ConcurrentModificationException {
        copyCheckSubTree(subtree, index, true);
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
        throws TreeException, ConcurrentModificationException {
        synchronized (tree) {
            Node newNode = null;
            if (checkLoops) {
                checkLoops = false;
                if (subtree == this) {
                    throw new TreeException
                            ("Copying subtree onto itself.");
                }

                // Check that subtree is not an ancestor of this.
                Ancestor ancestors =
                        new Ancestor(tree.getModCount());
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
            tree.modified();
        }
    }


    /**
     * Removes the child <tt>Node</tt> at the specified index in the
     * ArrayList.  Synchronized on the enclosing <tt>Tree</tt> object.
     *
     * Calls the <tt>modified</tt> method of the containing Tree to
     * maintain the value of <tt>modCount</tt>.
     *
     * @param index  The int index of the child to be removed.
     * @return the node removed.
     */

    public Node removeChildAtIndex(int index) {
        synchronized (tree) {
            Node tmpNode = (Node) children.remove(index);
            tree.modified();
            return tmpNode;
        }
    }

    /**
     * Removes the specified child <tt>Node</tt> from the children
     * ArrayList.  Synchronized on the enclosing <tt>Tree</tt> object.
     *
     * Implemented by calling <tt>removeChildAtIndex()</tt>.  Relies
     * on that method to call the <tt>modified</tt> method of the
     * containing Tree to maintain the value of <tt>modCount</tt>.
     *
     * @param child  The child node to be removed.
     * @return the node removed.
     */

    public Node removeChild(Node child)
        throws NoSuchElementException {
        synchronized (tree) {
            int index = children.indexOf((Object) child);
            if (index == -1) {
                throw new NoSuchElementException();
            }
            Node tmpNode = removeChildAtIndex(index);
            // Note - no call to tree.modified() here -
            // done in removeChildAtindex()
            return tmpNode;
        }
    }

    /**
     * Deletes the entire subtree rooted on <tt>this</tt> from the 
     * <tt>Tree</tt>.  The Tree is
     * traversed in PostOrder, and each Node is removed in PostOrder.
     * @return int count of Nodes deleted.
     */
    public int deleteSubTree() {
        synchronized (tree) {
            int count = delete(this);
            tree.modified();
            return count;
        }
    }

    /**
     * N.B. this private method must only be called from the deleteSubTree
     * method, which is synchronized.  In itself, it is not synchronized.
     * @param subtree Node at the root of the subtree to be deleted.
     * @return int count of Nodes deleted.
     */
    private int delete(Node subtree) {
        int count = 0;

        while (subtree.numChildren() > 0) {
            //System.out.println("# children "+subtree.numChildren());

            count += delete((Node)subtree.children.get(0));
        }
        // Delete this node
        // nullify the parent reference
        if (subtree.getTree().getRoot() != subtree) {
            // Not the root node - remove from parent
            subtree.getParent().removeChild(subtree);
            subtree.unsetParent();
        } else {
            subtree.getTree().unsetRoot();
        } // end of else
        return ++count;
    }

    public Tree getTree() {
        return tree;
    }

    public Node getParent() {
        return (Node) parent;
    }

    public void unsetParent() {
        parent = null;
    }

    public Node getChild(int index) {
        return (Node) children.get(index);
    }

    public Iterator nodeChildren() {
        return children.iterator();
    }

    public int numChildren() {
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
     *
     * The iterator is <i>fast-fail</i>.  If any modifications occur to
     * the tree as a whole during the lifetime of the iterator, a
     * subsequent call to next() will throw a
     * ConcurrentModificationException.  See the discussion of
     * fast-fail iterators in <tt>AbstractList</tt>.
     *
     * The <tt>modCount</tt> field used to maintain information about
     * structural modifcations is maintained for all nodes in the
     * containing Tree instance.
     */

    class PreOrder implements Iterator {
        private boolean selfNotReturned = true;
        private int nextChildIndex = 0;     // N.B. this must be kept as
        // the index of the active child until that child is exhausted.
        // At the start of proceedings, it may already point past the
        // end of the (empty) child vector

        private int age;
        private PreOrder nextChildIterator;

        /**
         * Constructor
         *
         * @param age  the current value of the modCount field in the
         * <tt>Tree</tt> instance which includes this class instance.
         */
        public PreOrder(int age) {
            this.age = age;
            hasNext();  // A call to set up the initial iterators
            // so that a call to next() without a preceding call to
            // hasNext() will behave sanely
        }

        public boolean hasNext() {
            // synchronize this against possible changes to the tree
            synchronized (tree) {
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
                if (nextChildIndex < children.size()) {
                    return nextChildIterator.hasNext(); 
                }
                else { // no kiddies
                    return false;
                }
            }
        }

        public Object next()
            throws NoSuchElementException,
                   ConcurrentModificationException {
            synchronized (tree) {
                // synchronize the whole against changes to the tree

                // Check for ConcurrentModification
                if (! tree.modCountEqualTo(age)) {
                    throw new ConcurrentModificationException();
                }

                if (! hasNext()) {
                    throw new NoSuchElementException();
                }
                if (selfNotReturned) {
                    selfNotReturned = false;
                    if (nextChildIndex < children.size()) {
                        // We have children - create an iterator
                        // for the first one
                        nextChildIterator =
                                ((Node)
                                 (children.get(nextChildIndex))).new
                                PreOrder(age);
                    }
                    // else do nothing;
                    // the nextChildIndex test in hasNext()
                    // will prevent us from getting into trouble
                    return Node.this;
                }
                else { // self has been returned
                    // there must be a next available, or we would not have
                    // come this far
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
                    Object tempNode = nextChildIterator.next();
                    // Check for exhaustion of the child
                    if (! nextChildIterator.hasNext()) {
                        // child iterator empty - another child?
                        if (++nextChildIndex < children.size()) {
                            nextChildIterator =
                                    ((Node)
                                     (children.get(nextChildIndex))).new
                                    PreOrder(age);
                        }
                        else {
                            // nullify the iterator
                            nextChildIterator = null;
                        }
                    }
                    return (Node) tempNode;
                }
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
     *
     * The iterator is <i>fast-fail</i>.  iI any modifications occur to
     * the tree as a whole during the lifetime of the iterator, a
     * subsequent call to next() will throw a
     * ConcurrentModificationException.  See the discussion of
     * fast-fail iterators in <tt>AbstractList</tt>.
     *
     * The <tt>modCount</tt> field used to maintain information about
     * structural modifcations is maintained for all nodes in the
     * containing Tree instance.
     */

    class PostOrder implements Iterator {
        private boolean selfReturned = false;
        private int nextChildIndex = 0;     // N.B. this must be kept as
        // the index of the active child until that child is exhausted.
        // At the start of proceedings, it may already point past the
        // end of the (empty) child vector

        private int age;
        private PostOrder nextChildIterator;

        /**
         * Constructor
         *
         * @param age  the current value of the modCount field in the
         * <tt>Tree</tt> instance which includes this class instance.
         */
        public PostOrder(int age) {
            this.age = age;
            hasNext();  // A call to set up the initial iterators
            // so that a call to next() without a preceding call to
            // hasNext() will behave sanely
        }

        public boolean hasNext() {
            // Synchronize this against changes in the tree
            synchronized (tree) {
                // self is always the last to go
                if (selfReturned) { // nothing left
                    return false;
                }

                // Check first for children, and set up an iterator if so
                if (nextChildIndex < children.size()) {
                    if (nextChildIterator == null) {
                        nextChildIterator =
                                ((Node)
                                (children.get(nextChildIndex))).new
                                PostOrder(age);
                    }
                    // else an iterator exists.
                    // Assume that the next() method
                    // will keep the iterator current
                } // end of Any children?

                return true;
            }
        }

        public Object next()
            throws NoSuchElementException,
                   ConcurrentModificationException {
            synchronized (tree) {
                // synchronize the whole against changes to the tree

                // Check for ConcurrentModification
                if (! tree.modCountEqualTo(age)) {
                    throw new ConcurrentModificationException();
                }

                if (! hasNext()) {
                    throw new NoSuchElementException();
                }
                // Are there any children?
                if (nextChildIndex < children.size()) {
                    // There will be an active iterator.  Is it empty?
                    if (nextChildIterator.hasNext()) {
                        // children remain
                        Object tempNode = nextChildIterator.next();
                        // now check for exhaustion of the iterator
                        if (! nextChildIterator.hasNext()) {
                            if (++nextChildIndex < children.size()) {
                                nextChildIterator =
                                        ((Node)
                                        (children.get(nextChildIndex))).new
                                        PostOrder(age);
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
     * parent back to tge root Node of the containing <tt>Tree</tt>.
     *
     * The iterator is <i>fast-fail</i>.  If any modifications occur to
     * the tree as a whole during the lifetime of the iterator, a
     * subsequent call to next() will throw a
     * ConcurrentModificationException.  See the discussion of
     * fast-fail iterators in <tt>AbstractList</tt>.
     *
     * The <tt>modCount</tt> field used to maintain information about
     * structural modifcations is maintained for all nodes in the
     * containing Tree instance.
     */

    class Ancestor implements Iterator {
        private Node nextAncestor;
        private int age;

        /**
         * Constructor
         *
         * @param age  the current value of the modCount field in the
         * <tt>Tree</tt> instance which includes this class instance.
         */

        public Ancestor(int age) {
            this.age = age;
            nextAncestor = Node.this.parent;
        }

        public boolean hasNext() {
            return nextAncestor != null;
        }

        public Object next()
            throws NoSuchElementException,
                   ConcurrentModificationException {
            synchronized (tree) {
                // The tree is a
                // potentially dymanic structure, which could be
                // undergoing modification as this method is being
                // executed, and it is possible that the Comod exception
                // could be set to trigger while this call is in process.
                if (! tree.modCountEqualTo(age)) {
                    throw new ConcurrentModificationException();
                }
                Node tmpNode = nextAncestor;
                nextAncestor = tmpNode.parent;
                return tmpNode;
            }
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
     *
     * The iterator is <i>fail-fast</i>.  if any modification occur to
     * the tree as a whole during the lifetime of the iterator, a
     * subsequent call to next() will throw a
     * ConcurrentModificationException.  See the discussion of
     * fast-fail iterators in <tt>AbstractList</tt>.
     *
     * The fail-fast ListIterator in ArrayList is the underlying
     * mechanism for both the listIterator and the fail-fast
     * behaviour.
     */

    class FollowingSibling implements ListIterator {

        private ListIterator listIterator;
        private ArrayList rootDummy = new ArrayList();
        // An empty ArrayList for the root listIterator
        // hasNext() will always return false

        public FollowingSibling() {
            synchronized (tree) {
                // Set up iterator on the parent's arrayList of children
                Node refNode = Node.this.parent;
                if (refNode != null) {
                    // Not the root node; siblings may exist
                    // Set up iterator on the parent's children ArrayList
                    ArrayList siblings = refNode.children;
                    int index = siblings.indexOf((Object) Node.this);
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
            // Any CoMod exception will be thrown by the listIterator
            // provided with ArrayList.  It does not throw such exceptions
            // on calls to hasNext();
            synchronized (tree) {
                return listIterator.hasNext();
            }
        }

        public Object next()
            throws NoSuchElementException,
                   ConcurrentModificationException {
            synchronized (tree) {
                // N.B. synchronization here is still on the Tree
                // rather than on the Node containing the children
                // ArryList of interest.  Other ArrayList operations
                // throughout the Tree are synchronized on the Tree object
                // itself, so exceptions cannot be made for these more
                // directly Nodal operations.
                return listIterator.next();
            }
        }

        public int nextIndex() {
            synchronized (tree) {
                return listIterator.nextIndex();
            }
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
     *
     * The iterator is <i>fail-fast</i>.  if any modification occur to
     * the tree as a whole during the lifetime of the iterator, a
     * subsequent call to previous() will throw a
     * ConcurrentModificationException.  See the discussion of
     * fast-fail iterators in <tt>AbstractList</tt>.
     *
     * The fail-fast ListIterator in ArrayList is the underlying
     * mechanism for both the listIterator and the fail-fast
     * behaviour.
     */

    class PrecedingSibling implements ListIterator {

        private ListIterator listIterator;
        private ArrayList rootDummy = new ArrayList();
        // An empty ArrayList for the root listIterator
        // hasNext() will always return false

        public PrecedingSibling() {
            synchronized (tree) {
                // Set up iterator on the parent's arrayList of children
                Node refNode = Node.this.parent;
                if (refNode != null) {
                    // Not the root node; siblings may exist
                    // Set up iterator on the parent's children ArrayList
                    ArrayList siblings = refNode.children;
                    int index = siblings.indexOf((Object) Node.this);
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
            // Any CoMod exception will be thrown by the listIterator
            // provided with ArrayList.  It does not throw such exceptions
            // on calls to hasNext();
            synchronized (tree) {
                return listIterator.hasPrevious();
            }
        }

        public Object previous()
            throws NoSuchElementException,
                   ConcurrentModificationException {
            synchronized (tree) {
                // N.B. synchronization here is still on the Tree
                // rather than on the Node containing the children
                // ArryList of interest.  Other ArrayList operations
                // throughout the Tree are synchronized on the Tree object
                // itself, so exceptions cannot be made for these more
                // directly Nodal operations.
                return listIterator.previous();
            }
        }

        public int previousIndex() {
            synchronized (tree) {
                return listIterator.previousIndex();
            }
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
