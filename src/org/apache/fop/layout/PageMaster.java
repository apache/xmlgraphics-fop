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
package org.apache.fop.layout;

public class PageMaster {

    private int width;
    private int height;
	
    private Region body;
    private Region before;
    private Region after;
    private Region start;
    private Region end;

    public PageMaster(int pageWidth, int pageHeight) {
	this.width = pageWidth;
	this.height = pageHeight;
    }

    public void addAfter(Region region) {
	this.after = region;
    }

    public void addBefore(Region region) {
	this.before = region;
    }

    public void addBody(Region region) {
	this.body = region;
    }

    public void addEnd(Region region) {
	this.end = region;
    }
	
    public void addStart(Region region) {
	this.start = region;
    }

    public int getHeight() {
	return this.height;
    }

    public int getWidth() {
	return this.width;
    }

    public Page makePage(AreaTree areaTree) {
	Page p = new Page(areaTree, this.height, this.width);
	if (this.body != null) {
	    p.addBody(body.makeAreaContainer());
	}
	if (this.before != null) {
	    p.addBefore(before.makeAreaContainer());
	}
	if (this.after != null) {
	    p.addAfter(after.makeAreaContainer());
	}
	if (this.start != null) {
	    p.addStart(start.makeAreaContainer());
	}
	if (this.end != null) {
	    p.addEnd(end.makeAreaContainer());
	}
	return p;
    }
}
