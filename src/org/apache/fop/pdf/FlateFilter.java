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
import java.util.zip.DeflaterOutputStream;

/**
 * A filter to deflate a stream. Note that the attributes for
 * prediction, colors, bitsPerComponent, and columns are not supported
 * when this filter is used to handle the data compression. They are
 * only valid for externally encoded data such as that from a graphics
 * file. 
 */
public class FlateFilter extends PDFFilter {

    public static final int PREDICTION_NONE = 1;
    public static final int PREDICTION_TIFF2 = 2;
    public static final int PREDICTION_PNG_NONE = 10;
    public static final int PREDICTION_PNG_SUB  = 11;
    public static final int PREDICTION_PNG_UP   = 12;
    public static final int PREDICTION_PNG_AVG  = 13;
    public static final int PREDICTION_PNG_PAETH= 14;
    public static final int PREDICTION_PNG_OPT  = 15;
   
    
    private int _predictor = PREDICTION_NONE;
    private int _colors;
    private int _bitsPerComponent;
    private int _columns;
    
    public String getName() 
    {
	return "/FlateDecode";
    }
    
    public String getDecodeParms() 
    {
	if (_predictor > PREDICTION_NONE) {
	    StringBuffer sb = new StringBuffer();
	    sb.append("<< /Predictor ");
	    sb.append(_predictor);
	    if (_colors > 0) {
		sb.append(" /Colors "+_colors);
	    }
	    if (_bitsPerComponent > 0) {
		sb.append(" /BitsPerComponent "+_bitsPerComponent);
	    }
	    if (_columns > 0) {
		sb.append(" /Columns "+_columns);
	    }
	    sb.append(" >> ");
	    return sb.toString();
	}
	return null;
    }
    

    /**
     * Encode the given data and return it. Note: a side effect of
     * this method is that it resets the prediction to the default
     * because these attributes are not supported. So the DecodeParms
     * should be retrieved after calling this method.  
     */
    public byte[] encode(byte[] data) 
    {
	ByteArrayOutputStream outArrayStream = new ByteArrayOutputStream();
 	_predictor = PREDICTION_NONE;
	try {
 	    DeflaterOutputStream compressedStream = 
		new DeflaterOutputStream(outArrayStream);
	    compressedStream.write(data, 0, data.length);
 	    compressedStream.flush();
 	    compressedStream.close();
 	}
 	catch (IOException e) {
 	    org.apache.fop.messaging.MessageHandler.error("Fatal error: "+
							  e.getMessage());
 	    e.printStackTrace();
 	}
 	
 	return outArrayStream.toByteArray();
    }
    
    public void setPredictor(int predictor)
	throws PDFFilterException
    {
	_predictor = predictor;
	
    }

    public int getPredictor() 
    {
	return _predictor;
    }
    
    
    public void setColors(int colors)
	throws PDFFilterException
    {
	if (_predictor != PREDICTION_NONE) {
	    _colors = colors;
	}
	else {
	    throw new PDFFilterException
		("Prediction must not be PREDICTION_NONE in order to set Colors");
	}
    }

    public int getColors() 
    {
	return _colors;
    }
    

    public void setBitsPerComponent(int bits) 
	throws PDFFilterException
    {
	if (_predictor != PREDICTION_NONE) {
	    _bitsPerComponent = bits;
	}
    	else {
	    throw new PDFFilterException
		("Prediction must not be PREDICTION_NONE in order to set bitsPerComponent");
	}
    }

    public int getBitsPerComponent() 
    {
	return _bitsPerComponent;
    }
    

    public void setColumns(int columns)
	throws PDFFilterException
    {
	if (_predictor != PREDICTION_NONE) {
	    _columns = columns;
	}
	else {
	    throw new PDFFilterException
		("Prediction must not be PREDICTION_NONE in order to set Columns");
	}
    }

    public int getColumns() 
    {
	return _columns;
    }
    

}
