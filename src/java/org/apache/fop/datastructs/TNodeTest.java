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

 */
package org.apache.fop.datastructs;

import java.util.*;

/**
 * A test class for <tt>TNode</tt>.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class TNodeTest{
    //public TNodeTest (){}
    
    public static void main(String[] args)
	throws TreeException {
	Tree tree = new Tree();
	TNode root = new TNode(null, "Root");
	TNode child1 = new TNode(root, "1-1");
	TNode child2 = new TNode(root, "1-2");
	TNode child3 = new TNode(root, "1-3");
	TNode child2_1 = new TNode((TNode)root.getChild(1), "1-2-1");
	TNode child2_2 = new TNode((TNode)root.getChild(1), "1-2-2");
	TNode child3_1 = new TNode((TNode)root.getChild(2), "1-3-1");
	TNode child3_2 = new TNode((TNode)root.getChild(2), "1-3-2");
	TNode child3_3 = new TNode((TNode)root.getChild(2), "1-3-3");
	TNode child1_1 = new TNode((TNode)root.getChild(0), "1-1-1");
	System.out.println("Pre-order traversal:root:");
	preorder(root);
	System.out.println("Post-order traversal:root:");
	postorder(root);
	System.out.println("Preceding siblings 3-2");
	precedingsibling(child3_2);
	System.out.println("Following siblings 3-2");
	followingsibling(child3_2);
	System.out.println("Preceding siblings 2-2");
	precedingsibling(child2_2);
	System.out.println("Following siblings 2-2");
	followingsibling(child2_2);
	System.out.println("Preceding siblings 1");
	precedingsibling(child1);
	System.out.println("Following siblings 1");
	followingsibling(child1);
	System.out.println("Preceding siblings root");
	precedingsibling(root);
	System.out.println("Following siblings root");
	followingsibling(root);
	System.out.println("Pre-order traversal:2:");
	preorder(child2);
	System.out.println("Post-order traversal:3:");
	postorder(child3);
	System.out.println("Ancestors:3-2");
	ancestors(child3_2);

	// Check the copySubTree function
	System.out.println("copySubTree child3 to child2_1");
	child2_1.copySubTree(child3, 0);
	System.out.println("Pre-order traversal:root:");
	preorder(root);
	System.out.println("copySubTree child3_3 to root");
	try {
	    root.copySubTree(child3_3, 0);
	} catch (TreeException e) {
	    System.out.println("Caught TreeException: " + e.getMessage());
	}

	System.out.println("Pre-order traversal:root:");
	preorder(root);
	System.out.println("copySubTree child3 to child3_3");
	try {
	    child3_3.copySubTree(child3, 0);
	} catch (TreeException e) {
	    System.out.println("Caught TreeException: " + e.getMessage());
	}

	System.out.println("Pre-order traversal:root:");
	preorder(root);

	// Test the cutSubTree method
	System.out.println("cutSubTree child2_1");
	TNode subtree = (TNode)(child2_1.deleteSubTree());
        Tree tree2 = new Tree(subtree);
	System.out.println("Pre-order traversal:tree2.getRoot():");
	preorder((TNode)(tree2.getRoot()));
	System.out.println("Post-order traversal:tree2.getRoot():");
	postorder((TNode)(tree2.getRoot()));

	System.out.println("Get the first child of tree 2 root");
        TNode firstChild = (TNode)(tree2.getRoot().getChild(0));
	System.out.println("Cut the first child of tree 2 root");
        subtree = (TNode)(firstChild.deleteSubTree());
	System.out.println("Pre-order traversal:tree2.getRoot():");
	preorder((TNode)(tree2.getRoot()));
	System.out.println("Post-order traversal:tree2.getRoot():");
	postorder((TNode)(tree2.getRoot()));
	System.out.println("Insert as first child of child2");
        child2.addSubTree(0, subtree);

	System.out.println("Pre-order traversal:root:");
	preorder(root);
	System.out.println("Post-order traversal:root:");
	postorder(root);
	// Test for fast-fail
	System.out.println("Setting up PreOrder iterator");
	TNode.PreOrder iterator = root.new PreOrder();
	System.out.println("Adding child4");
	TNode child4 = new TNode(root, "1-4");
	System.out.println("Iterating");
	try {
	    while (iterator.hasNext()) {
		TNode next = (TNode) iterator.next();
		System.out.println((String)next.getContent());
	    }
	} catch (ConcurrentModificationException e) {
	    System.out.println("Comod exception caught");
	} // end of try-catch
	System.out.println("Setting up FollowingSibling listIterator on 3-2");
	TNode.FollowingSibling listiterator =
		child3_2.new FollowingSibling();
	System.out.println("Perturbing child3-2 parent; adding 3-4");
	TNode child3_4 = new TNode(child3, "1-3-3");
	try {
	    while (listiterator.hasNext()) {
		TNode next = (TNode) listiterator.next();
		System.out.println((String)next.getContent());
	    }
	} catch (ConcurrentModificationException e) {
	    System.out.println("Comod exception caught");
	}

	System.out.println("Setting up Ancestor Iterator on 1-1");
	TNode.Ancestor aiterator =
		child1_1.new Ancestor();
	System.out.println("Perturbing root; adding 5");
	TNode child5 = new TNode(root, "1-5");
	try {
	    while (aiterator.hasNext()) {
		TNode next = (TNode) aiterator.next();
		System.out.println((String)next.getContent());
	    }
	} catch (ConcurrentModificationException e) {
	    System.out.println("Comod exception caught");
	}

	System.out.println("Delete child1 nodes");
	int delcount = child1.deleteCountSubTree();
	System.out.println("# deleted: "+delcount);
	System.out.println("Pre-order traversal:root:");
	preorder((TNode)tree.getRoot());
	System.out.println("Delete all nodes");
	delcount = root.deleteCountSubTree();
	System.out.println("# deleted: "+delcount);
	System.out.println("Pre-order traversal:root:");
	preorder((TNode)tree.getRoot());
    }

    private static void preorder(TNode node) {
	TNode.PreOrder iterator = node.new PreOrder();
	while (iterator.hasNext()) {
	    TNode next = (TNode) iterator.next();
	    System.out.println((String)next.getContent());
	}
    }

    private static void postorder(TNode node) {
	TNode.PostOrder iterator = node.new PostOrder();
	while (iterator.hasNext()) {
	    TNode next = (TNode) iterator.next();
	    System.out.println((String)next.getContent());
	}
    }

    private static void ancestors(TNode node) {
	TNode.Ancestor iterator = node.new Ancestor();
	while (iterator.hasNext()) {
	    TNode next = (TNode) iterator.next();
	    System.out.println((String)next.getContent());
	}
    }
	
    private static void followingsibling(TNode node) {
	TNode.FollowingSibling iterator =
		node.new FollowingSibling();
	while (iterator.hasNext()) {
	    TNode next = (TNode) iterator.next();
	    System.out.println((String)next.getContent());
	}
    }

    private static void precedingsibling(TNode node) {
	TNode.PrecedingSibling iterator =
		node.new PrecedingSibling();
	while (iterator.hasPrevious()) {
	    TNode previous = (TNode) iterator.previous();
	    System.out.println((String)previous.getContent());
	}
    }

} // TNodeTest
