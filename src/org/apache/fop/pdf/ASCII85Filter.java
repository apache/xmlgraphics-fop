/*-- $Id$ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */
package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ASCII85Filter extends PDFFilter
{
    private static final char ASCII85_ZERO = 'z';
    private static final char ASCII85_START = '!';
    private static final String ASCII85_EOD   = "~>";
    
    private static final long base85_4 = 85;
    private static final long base85_3 = base85_4 * base85_4;
    private static final long base85_2 = base85_3 * base85_4;
    private static final long base85_1 = base85_2 * base85_4;
    

   
    public String getName() 
    {
	return "/ASCII85Decode";
    }
    
    public String getDecodeParms() 
    {
	return null;
    }

    public byte[] encode(byte[] data) 
    {
	
	StringBuffer buffer = new StringBuffer();
	int i;
	int total = 0;
	int diff = 0;
	
	for (i = 0; i+3 < data.length; i+=4) {
	    byte b1 = data[i];
	    byte b2 = data[i+1];
	    byte b3 = data[i+2];
	    byte b4 = data[i+3];
	    
	    long val = ((b1 << 24) & 0xff000000L) 
		+ ((b2 << 16) & 0xff0000L) 
		+ ((b3 << 8) & 0xff00L) 
		+ (b4 & 0xffL);
	    String conv = convertLong(val);
	    
	    buffer.append(conv);

	    
	}

	
	if (i < data.length) {
	    int n = data.length - i;
	    byte b1,b2,b3,b4;
	    b1 = data[i++];
	    if (i < data.length) {
		b2 = data[i++];
	    }
	    else {
		b2 = 0;
	    }
	    if (i < data.length) {
		b3 = data[i++];
	    }
	    else {
		b3 = 0;
	    }
	    // assert i = data.length
	    b4 = 0;
	   
	    long val = ((b1 << 24) & 0xff000000L) 
		+ ((b2 << 16) & 0xff0000L) 
		+ ((b3 << 8) & 0xff00L) 
		+ (b4 & 0xffL);
	    String converted = convertLong(val);
	    
	    // special rule for handling zeros at the end
	    if (val == 0) {
		converted = "!!!!!";
	    }
	    buffer.append(converted.substring(0,n));
	}
	buffer.append(ASCII85_EOD);
	return buffer.toString().getBytes();
	
    }
    
    private String convertLong(long val) 
    {
	val = val & 0xffffffff;
	if (val < 0) {
	    val = -val;
	}
	
	if (val == 0) {
	    return new Character(ASCII85_ZERO).toString();
	}
	else {
	    byte c1 = (byte)((val / base85_1) & 0xFF);
	    byte c2 = (byte)(((val - (c1 * base85_1)) / base85_2) & 0xFF);
	    byte c3 = (byte)(((val 
		       - (c1 * base85_1) 
		       - (c2 * base85_2) 
		       ) / base85_3) & 0xFF);
	    byte c4 = (byte)(((val 
		       - (c1 * base85_1) 
		       - (c2 * base85_2)
		       - (c3 * base85_3)
		       ) / base85_4) & 0xFF);
	    byte c5 = (byte)(((val 
		       - (c1 * base85_1) 
		       - (c2 * base85_2)
		       - (c3 * base85_3)
		       - (c4 * base85_4))) & 0xFF);
			
	    char[] ret = {(char)(c1+ASCII85_START),
			   (char)(c2+ASCII85_START),
			   (char)(c3+ASCII85_START),
			   (char)(c4+ASCII85_START),
			   (char)(c5+ASCII85_START)};
	    for (int i = 0; i< ret.length; i++) {
		if (ret[i] < 33 || ret[i] > 117) {
		    System.out.println("illegal char value "+new Integer(ret[i]));
		}
	    }
	    
	    return new String(ret);
	    	    
	}	
    }

}
