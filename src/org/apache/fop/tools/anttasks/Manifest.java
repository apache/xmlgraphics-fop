/* 
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the 
 * LICENSE file included with these sources."
 */

package org.apache.fop.tools.anttasks;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import java.io.*;
import java.text.SimpleDateFormat;
import java.net.InetAddress;
import java.util.Date;
import java.util.Properties;



/**
 * Creates a manifest file for packing into a jar.
 * <P>
 * Attributes are as follows:
 * <dl>
 *  <dt>file</dt> <dd>the manifest file to write out to (required)</dd>
 *  <dt>overwrite</dt> <dd>if set to yes or true, overwrite the given 
 *       manifest file. Default is no</dd>
 *  <dt>version</dt> <dd>manifest version. Defaults to "1.0"</dd>
 *  <dt>spectitle</dt> <dd>the specification title</dd>
 *  <dt>specversion</dt> <dd>the specification version</dd>
 *  <dt>specvendor</dt> <dd>the specification vendor</dd>
 *  <dt>impltitle</dt> <dd>the implementation title</dd>
 *  <dt>implversion</dt> <dd>the implementation version.</dd>
 *  <dt>implvendor</dt> <dd>the implementation vendor</dd>
 *  <dt>mainclass</dt> <dd>the class to run when java -jar is invoked</dd>
 *  <dt>classpath</dt> <dd>the classpath to use when java -jar is invoked</dd>
 *  <dt>createdby</dt> <dd>the string to set the Created-By field to</dd>
 *  <dt>buildid</dt> <dd>A build identifier. Defaults to a build identifier
 *     containing <tt>date + " ("+username+"@"+hostname+" ["+os+" "+version+" "+arch+"]</tt> </dd>
 * </dl>
 *
 * @author Kelly A. Campbell
 */

public class Manifest extends Task 
{
    public static final String MANIFEST_VERSION = "Manifest-Version: ";
    public static final String CREATED_BY = "Created-By: ";
    public static final String REQUIRED_VERSION = "Required-Version: ";
       
    public static final String SPECIFICATION_TITLE = "Specification-Title: ";
    public static final String SPECIFICATION_VERSION = "Specification-Version: ";
    public static final String SPECIFICATION_VENDOR  = "Specification-Vendor: ";
    public static final String IMPL_TITLE   = "Implementation-Title: ";
    public static final String IMPL_VERSION = "Implementation-Version: ";
    public static final String IMPL_VENDOR  = "Implementation-Vendor: ";
    public static final String BUILD_ID     = "Build-ID: ";
    public static final String MAIN_CLASS   = "Main-Class: ";
    public static final String CLASS_PATH   = "Class-Path: ";
    

    private String _manifestVersion = "1.0";
    private String _spectitle;
    private String _specvers;
    private String _specvend;
    private String _impltitle;
    private String _implvers;
    private String _implvend;
    private String _mainclass;
    private String _classpath;
    private String _createdby;
    private String _buildid;
    
    private String _manifestFilename;
    private Boolean _overwrite = Boolean.FALSE;
    
    public void setFile(String s) 
    {
	_manifestFilename = s;
    }
    
    public void setOverwrite(Boolean b) 
    {
	_overwrite = b;
    }
    
    public void setSpectitle(String s) 
    {
	_spectitle = s;
    }
    public void setSpecversion(String s) 
    {
	_specvers = s;
    }
    public void setSpecvendor(String s) 
    {
	_specvend = s;
    }
     public void setImpltitle(String s) 
    {
	_impltitle = s;
    }
    public void setImplversion(String s) 
    {
	_implvers = s;
    }
    public void setImplvendor(String s) 
    {
	_implvend = s;
    }
    public void setMainclass(String s) 
    {
	_mainclass = s;
    }
    public void setClasspath(String s) 
    {
	_classpath = s;
    }
    public void setCreatedby(String s) 
    {
	_createdby = s;
    }
    public void setBuildid(String s) 
    {
	_buildid = s;
    }
    
    /**
     * Main task method which runs this task and creates the manifest file.
     * @exception BuildException if one of the required attributes isn't set
     */
    public void execute ()
	throws BuildException
    {
	//	System.out.println("Executing manifest task");
	
	PrintWriter out;
	try {
	    if (_manifestFilename != null) {
		// open the file for writing
		File f = new File(_manifestFilename);
		if (f.exists()) {
		    if (_overwrite.booleanValue()) {
			f.delete();
		    }
		    else {
			throw new BuildException("Will not overwrite existing file: "+_manifestFilename+". Use overwrite='yes' if you wish to overwrite the file.");
		    }
		}
		System.out.println("creating "+f);
	    
		f.createNewFile();
		out = new PrintWriter(new FileOutputStream(f));
	    
	    }
	    else {
		throw new BuildException("Manifest task requires a 'file' attribute");
	    }
	}
	catch (IOException ex) {
	    throw new BuildException(ex);
	}
	
	// setup the implementation versionn (buildID)
	if (_buildid == null || _buildid.trim().equals("")) {
	    _buildid = createBuildID();
	}
	if (_createdby == null || _createdby.trim().equals("")) {
	    _createdby = getCreator();
	}
	
	print(out, MANIFEST_VERSION, _manifestVersion);
	print(out, CREATED_BY, _createdby);
	
	print(out, SPECIFICATION_TITLE, _spectitle);
	print(out, SPECIFICATION_VERSION, _specvers);
	print(out, SPECIFICATION_VENDOR, _specvend);
	print(out, IMPL_TITLE, _impltitle);
	print(out, IMPL_VERSION, _implvers);
	print(out, IMPL_VENDOR, _implvend);
	print(out, BUILD_ID, _buildid);
	print(out, MAIN_CLASS, _mainclass);
	print(out, CLASS_PATH, _classpath);

	out.flush();
	out.close();
	
    }
      
    protected void print(PrintWriter out, String header, String value) 
    {
	if (value != null && !value.trim().equals("")) {
	    out.println(header+value);
	    //  System.out.println("manifest: "+header+value);
	}
    }
    
    private static String createBuildID() 
    {
	Date d = new Date();
	SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd-HHmmss-z");
	String date = f.format(d);	
	String hostname, username, os, version, arch;
	try {
	    hostname = InetAddress.getLocalHost().getHostName();
	}
	catch (Exception ex) {
	    hostname = "unknown";
	}
	username = System.getProperty("user.name");
	os       = System.getProperty("os.name");
	version  = System.getProperty("os.version");
	arch     = System.getProperty("os.arch");
	String buildid = date + " ("+username+"@"+hostname+" ["+os+" "+version+" "+arch+"])";
	return buildid;
		
    }

    private static String getCreator() 
    {
	try {
	    Properties props = new Properties();
	    InputStream in = org.apache.tools.ant.Main.class.getResourceAsStream("/org/apache/tools/ant/version.txt");
	    if (in != null) {
		props.load(in);
		in.close();
		
		return "Ant "+props.getProperty("VERSION");
	    }
	    else {
		return null;
	    }
	}
	catch (IOException ex) {
	    return null;
	}
    }
    
}
