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

// Java
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

/**
 * class representing a /Resources object.
 *
 * /Resources object contain a list of references to the fonts for the
 * document
 */ 
public class PDFResources extends PDFObject {

    /** /Font objects keyed by their internal name */
    protected Hashtable fonts = new Hashtable();

    protected Vector xObjects=null;
	protected Vector patterns= new Vector();
	protected Vector shadings=new Vector();
    /**
     * create a /Resources object.
     *
     * @param number the object's number
     */
    public PDFResources(int number) {

	/* generic creation of object */
		super(number);
	
    }

    /**
     * add font object to resources list.
     *
     * @param font the PDFFont to add
     */
    public void addFont(PDFFont font) {
	this.fonts.put(font.getName(),font);
    }
	
	public void addShading(PDFShading theShading){
		this.shadings.addElement(theShading);
	}
	
	public void addPattern(PDFPattern thePattern){
		this.patterns.addElement(thePattern);
	}
    public void setXObjects(Vector xObjects) {
	this.xObjects = xObjects;
    }

    /**
     * represent the object in PDF
     *
     * @return the PDF
     */
	public byte[] toPDF() {
		StringBuffer p = new StringBuffer(this.number + " "
						+ this.generation
						+ " obj\n<< \n");
		if(!this.fonts.isEmpty())
		{
			p.append("/Font << ");

			/* construct PDF dictionary of font object references */
			Enumeration fontEnumeration = this.fonts.keys();
			while (fontEnumeration.hasMoreElements()) {
	   		String fontName = (String) fontEnumeration.nextElement();
	   		p.append("/" + fontName + " " 
					+ ((PDFFont) this.fonts.get(fontName)).referencePDF()
					+ " ");  
			}
			
			p.append(">> \n");
		}
		
		PDFShading currentShading = null;
		if(!this.shadings.isEmpty())
		{			
			p.append("/Shading << ");
			
			for(int currentShadingNumber=0;
				currentShadingNumber < this.shadings.size();
				currentShadingNumber++)
			{
				currentShading = ((PDFShading)this.shadings.elementAt(currentShadingNumber));

				p.append("/" + currentShading.getName() + " "
				+ currentShading.referencePDF()
				+ " "); // \n ??????
			}

			p.append(">> \n");
		}
		//"free" the memory. Sorta.
		currentShading = null;
		
		PDFPattern currentPattern=null;
		if(!this.patterns.isEmpty())
		{
			p.append("/Pattern << ");
			
			for(int currentPatternNumber=0;
				currentPatternNumber < this.patterns.size();
				currentPatternNumber++)
			{
				currentPattern = ((PDFPattern)this.patterns.elementAt(currentPatternNumber));

				p.append("/" + currentPattern.getName() + " "
				+ currentPattern.referencePDF()
				+ " ");
			}

			p.append(">> \n");
		}
		//"free" the memory. Sorta.
		currentPattern = null;
		
		p.append("/ProcSet [ /PDF /ImageC /Text ] ");

		if (!this.xObjects.isEmpty())
		{
	   	p = p.append("/XObject <<");
	   	for (int i = 1; i <= this.xObjects.size(); i++) {
			p = p.append("/Im" + i + " " +
			   	((PDFXObject)
			      	this.xObjects.elementAt(i -
						      	1)).referencePDF()
			   	+
			   	" \n");
	   	}
		p = p.append(" >>\n");
		}  

		p = p.append(">> \nendobj\n");

		return p.toString().getBytes();
	}    
}
