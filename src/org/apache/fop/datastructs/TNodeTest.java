package org.apache.fop.datastructs;

import java.util.*;

/*
 * TNodeTest.java
 *
 * $Id$
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * A test class for <tt>TNode</tt>.
 */

public class TNodeTest{
    //public TNodeTest (){}
    
    public static void main(String[] args)
	throws Tree.TreeException {
	Tree tree = new Tree();
	TNode root = new TNode(tree, null, "Root");
	TNode child1 = new TNode(tree, root, "1-1");
	TNode child2 = new TNode(tree, root, "1-2");
	TNode child3 = new TNode(tree, root, "1-3");
	TNode child2_1 = new TNode(tree, (TNode)root.getChild(1), "1-2-1");
	TNode child2_2 = new TNode(tree, (TNode)root.getChild(1), "1-2-2");
	TNode child3_1 = new TNode(tree, (TNode)root.getChild(2), "1-3-1");
	TNode child3_2 = new TNode(tree, (TNode)root.getChild(2), "1-3-2");
	TNode child3_3 = new TNode(tree, (TNode)root.getChild(2), "1-3-3");
	TNode child1_1 = new TNode(tree, (TNode)root.getChild(0), "1-1-1");
	System.out.println("Pre-order traversal:root:");
	preorder(root, tree.getModCount());
	System.out.println("Post-order traversal:root:");
	postorder(root, tree.getModCount());
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
	preorder(child2, tree.getModCount());
	System.out.println("Post-order traversal:3:");
	postorder(child3, tree.getModCount());
	System.out.println("Ancestors:3-2");
	ancestors(child3_2, tree.getModCount());

	// Check the copySubTree function
	System.out.println("copySubTree child3 to child2_1");
	child2_1.copySubTree(child3, 0);
	System.out.println("Pre-order traversal:root:");
	preorder(root, tree.getModCount());
	System.out.println("copySubTree child3_3 to root");
	try {
	    root.copySubTree(child3_3, 0);
	} catch (Tree.TreeException e) {
	    System.out.println("Caught TreeException: " + e.getMessage());
	}

	System.out.println("Pre-order traversal:root:");
	preorder(root, tree.getModCount());
	System.out.println("copySubTree child3 to child3_3");
	try {
	    child3_3.copySubTree(child3, 0);
	} catch (Tree.TreeException e) {
	    System.out.println("Caught TreeException: " + e.getMessage());
	}

	System.out.println("Pre-order traversal:root:");
	preorder(root, tree.getModCount());

	// Test the deleteSubTree method
	System.out.println("deleteSubTree child2_1");
	int delcount = child2_1.deleteSubTree();
	System.out.println("# deleted: "+delcount);

	System.out.println("Pre-order traversal:root:");
	preorder(root, tree.getModCount());
	System.out.println("Post-order traversal:root:");
	postorder(root, tree.getModCount());
	// Test for fast-fail
	System.out.println("Setting up PreOrder iterator");
	TNode.PreOrder iterator = root.new PreOrder(tree.getModCount());
	System.out.println("Adding child4");
	TNode child4 = new TNode(tree, root, "1-4");
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
	TNode child3_4 = new TNode(tree, child3, "1-3-3");
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
		child1_1.new Ancestor(tree.getModCount());
	System.out.println("Perturbing root; adding 5");
	TNode child5 = new TNode(tree, root, "1-5");
	try {
	    while (aiterator.hasNext()) {
		TNode next = (TNode) aiterator.next();
		System.out.println((String)next.getContent());
	    }
	} catch (ConcurrentModificationException e) {
	    System.out.println("Comod exception caught");
	}

	System.out.println("Delete all nodes");
	delcount = root.deleteSubTree();
	System.out.println("# deleted: "+delcount);
	System.out.println("Pre-order traversal:root:");
	preorder((TNode)tree.getRoot(), tree.getModCount());
    }

    private static void preorder(TNode node, int age) {
	TNode.PreOrder iterator = node.new PreOrder(age);
	while (iterator.hasNext()) {
	    TNode next = (TNode) iterator.next();
	    System.out.println((String)next.getContent());
	}
    }

    private static void postorder(TNode node, int age) {
	TNode.PostOrder iterator = node.new PostOrder(age);
	while (iterator.hasNext()) {
	    TNode next = (TNode) iterator.next();
	    System.out.println((String)next.getContent());
	}
    }

    private static void ancestors(TNode node, int age) {
	TNode.Ancestor iterator = node.new Ancestor(age);
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
