/*
 * TNodeTest.java
 *
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
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
package org.apache.fop.datastructs;

import java.util.*;

/**
 * A test class for <tt>TNode</tt>.
 */

public class TNodeTest{
    //public TNodeTest (){}
    
    public static void main(String[] args)
	throws TreeException {
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
	} catch (TreeException e) {
	    System.out.println("Caught TreeException: " + e.getMessage());
	}

	System.out.println("Pre-order traversal:root:");
	preorder(root, tree.getModCount());
	System.out.println("copySubTree child3 to child3_3");
	try {
	    child3_3.copySubTree(child3, 0);
	} catch (TreeException e) {
	    System.out.println("Caught TreeException: " + e.getMessage());
	}

	System.out.println("Pre-order traversal:root:");
	preorder(root, tree.getModCount());

	// Test the cutSubTree method
	System.out.println("cutSubTree child2_1");
	TNode subtree = (TNode)(child2_1.cutSubTree());
        Tree tree2 = new Tree(subtree);
	System.out.println("Pre-order traversal:tree2.getRoot():");
	preorder((TNode)(tree2.getRoot()), tree2.getModCount());
	System.out.println("Post-order traversal:tree2.getRoot():");
	postorder((TNode)(tree2.getRoot()), tree2.getModCount());

	System.out.println("Get the first child of tree 2 root");
        TNode firstChild = (TNode)(tree2.getRoot().getChild(0));
	System.out.println("Cut the first child of tree 2 root");
        subtree = (TNode)(firstChild.cutSubTree());
	System.out.println("Pre-order traversal:tree2.getRoot():");
	preorder((TNode)(tree2.getRoot()), tree2.getModCount());
	System.out.println("Post-order traversal:tree2.getRoot():");
	postorder((TNode)(tree2.getRoot()), tree2.getModCount());
	System.out.println("Insert as first child of child2");
        child2.addSubTree(0, subtree);

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

	System.out.println("Delete child1 nodes");
	int delcount = child1.deleteSubTree();
	System.out.println("# deleted: "+delcount);
	System.out.println("Pre-order traversal:root:");
	preorder((TNode)tree.getRoot(), tree.getModCount());
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
