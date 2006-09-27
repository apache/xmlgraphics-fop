See http://issues.apache.org/bugzilla/show_bug.cgi?id=35948 for more details about
the work being done on integrating foray-font in FOP.

Currently, to run the version found in the foray-font branch:

1) 	Compile from Eclipse, there are still many compilation errors which break
 		the ant build.
 		
2)	Run from Eclipse, for the same reasons.
 		Using the following parameters should work:
 		
 		   ./axsl-temp-stuff/fop-foray-test.fo /tmp/output.pdf

3)	Create some symbolic links in /tmp/foray-links, as some filenames are hardcoded.

    Here are my current links, adapt to your environment:
    
		/tmp/foray-links/axsl -> /YOUR_BASE/axsl/trunk/axsl
		/tmp/foray-links/axsl-temp-stuff -> /YOUR_BASE/apache/fop/branches/foray-font/axsl-temp-stuff
		/tmp/foray-links/config/glb12.ttf -> /YOUR_BASE/apache/fop/branches/foray-font/test/resources/fonts/glb12.ttf
		/tmp/foray-links/foray-font/resource -> /YOUR_BASE/foray/trunk/foray/foray-font/resource
		
    