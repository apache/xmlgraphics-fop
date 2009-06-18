/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *  any, must include the following acknowlegement:
 *     "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowlegement may appear in the software itself,
 *  if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *  Foundation" must not be used to endorse or promote products derived
 *  from this software without prior written permission. For written
 *  permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *  nor may "Apache" appear in their names without prior written
 *  permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.fop.tools.xslt;

import java.io.*;
import java.lang.reflect.*;


public class XSLTransform 
{
    
    public static void transform(String xmlSource,
				 String xslURL,
				 String outputFile) 
	throws Exception
    {
	Class[] argTypes = {String.class, String.class, String.class};
	Object[] params = {xmlSource, xslURL, outputFile};
	transform(params, argTypes);
    }

    public static void transform(org.w3c.dom.Document xmlSource,
				 String xslURL,
				 String outputFile) 
	throws Exception
    {
	Class[] argTypes = {org.w3c.dom.Document.class,
			    String.class, String.class};
			
	Object[] params = {xmlSource, xslURL, outputFile};
	transform(params, argTypes);
	
    }
    
    public static void transform(String xmlSource,
				 String xslURL,
				 Writer outputWriter)
	throws Exception
    {
	Class[] argTypes = {String.class, String.class, Writer.class};
	Object[] params = {xmlSource, xslURL, outputWriter};
	transform(params, argTypes);
		
    }
 
    public static void transform(org.w3c.dom.Document xmlSource,
				 InputStream xsl,
				 org.w3c.dom.Document outputDoc)
	throws Exception
    {
	Class[] argTypes = {org.w3c.dom.Document.class, InputStream.class, 
			    org.w3c.dom.Document.class};
	Object[] params = {xmlSource, xsl, outputDoc};
	transform(params, argTypes);
		
    }
    

    private static void transform(Object[] args, Class[] argTypes) 
	throws Exception
    {
	Class transformer = getTransformClass();
	if (transformer != null) {
	    Method transformMethod = getTransformMethod(transformer,argTypes);
	    if (transformMethod != null) {
		try {
		    transformMethod.invoke(null, args);
		}
		catch (InvocationTargetException ex) {
		    ex.printStackTrace();
		}
	    }
	    else {
		throw new Exception("transform method not found");
	    }
	}
	else {
	    throw new Exception("no transformer class found");
	}	
	
    }
    

    private static Class getTransformClass() 
    {
	try {
	    // try trax first
	    Class transformer = Class.forName("javax.xml.transform.Transformer");
	    // ok, make sure we have a liaison to trax
	    transformer = Class.forName("org.apache.fop.tools.xslt.TraxTransform");
	    return transformer;
	    
	}
	catch (ClassNotFoundException ex){
	}
	// otherwise, try regular xalan1
	try {
	    Class transformer = Class.forName("org.apache.xalan.xslt.XSLTProcessor");
	    // get the liaison
	    transformer = Class.forName("org.apache.fop.tools.xslt.Xalan1Transform");
	    return transformer;
	}
	catch (ClassNotFoundException ex){
	}
	return null;
	
    }
    

    private static Method getTransformMethod(Class c, Class[] argTypes) 
    {
	//	System.out.println("transformer class = "+c);
	
	try {
	    //	    Class[] argTypes = new Class[args.length];
	    for (int i=0; i<argTypes.length; i++) {
		//	argTypes[i] = args[i].getClass();
		//System.out.println("arg["+i+"] type = "+argTypes[i]);
		
	    }
	    
	    Method transformer = c.getMethod("transform",argTypes);
	    return transformer;
	    
	}
	catch (NoSuchMethodException ex) {
	    ex.printStackTrace();
	    
	}
	return null;
    }
    
}
