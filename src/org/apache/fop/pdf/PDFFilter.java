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


//Author:       Eric SCHAEFFER, Kelly A. Campbell
//Description:  represent a PDF filter object

package org.apache.fop.pdf;

public abstract class PDFFilter {
    /* These are no longer needed, but are here as a reminder about what 
       filters pdf supports.

	public static final int ASCII_HEX_DECODE = 1;
	public static final int ASCII_85_DECODE = 2;
	public static final int LZW_DECODE = 3;
	public static final int RUN_LENGTH_DECODE = 4;
	public static final int CCITT_FAX_DECODE = 5;
	public static final int DCT_DECODE = 6;
	public static final int FLATE_DECODE = 7;

    */

    /** Marker to know if this filter has already been applied to the data */
    private boolean _applied = false;
    
    public boolean isApplied() 
    {
	return _applied;
    }
    
    /**
     * Set the applied attribute to the given value. This attribute is
     * used to determine if this filter is just a placeholder for the
     * decodeparms and dictionary entries, or if the filter needs to
     * actually encode the data. For example if the raw data is copied
     * out of an image file in it's compressed format, then this
     * should be set to true and the filter options should be set to
     * those which the raw data was encoded with.  
     */
    public void setApplied(boolean b) 
    {
	_applied = b;
    }
    
    
    /** return a PDF string representation of the filter, e.g. /FlateDecode */
    public abstract String getName();
    
    /** return a parameter dictionary for this filter, or null */
    public abstract String getDecodeParms();
     
    /** encode the given data with the filter */
    public abstract byte[] encode(byte[] data);
    
 
    
}
