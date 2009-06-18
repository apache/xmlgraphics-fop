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
 
 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
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

package org.apache.fop.fo;

import org.apache.fop.fo.properties.*;

import java.util.Hashtable;

public class StandardPropertyListMapping implements PropertyListMapping {

    public void addToBuilder(FOTreeBuilder builder) {

	String uri = "http://www.w3.org/1999/XSL/Format";
	Hashtable propertyTable = new Hashtable();

	propertyTable.put("end-indent",EndIndent.maker());
	propertyTable.put("master-name",MasterName.maker());
	propertyTable.put("page-master-first",PageMasterFirst.maker());
	propertyTable.put("page-master-repeating",PageMasterRepeating.maker());
	propertyTable.put("page-master-odd",PageMasterOdd.maker());
	propertyTable.put("page-master-even",PageMasterEven.maker());
	propertyTable.put("margin-top",MarginTop.maker());
	propertyTable.put("margin-bottom",MarginBottom.maker());
	propertyTable.put("margin-left",MarginLeft.maker());
	propertyTable.put("margin-right",MarginRight.maker());
	propertyTable.put("extent",Extent.maker());
	propertyTable.put("page-width",PageWidth.maker());
	propertyTable.put("page-height",PageHeight.maker());
	propertyTable.put("flow-name",FlowName.maker());
	propertyTable.put("font-family",FontFamily.maker());
	propertyTable.put("font-style",FontStyle.maker());
	propertyTable.put("font-weight",FontWeight.maker());
	propertyTable.put("font-size",FontSize.maker());
	propertyTable.put("line-height",LineHeight.maker());
	propertyTable.put("text-align",TextAlign.maker());
	propertyTable.put("text-align-last",TextAlignLast.maker());
	propertyTable.put("space-before.optimum",SpaceBeforeOptimum.maker());
	propertyTable.put("space-after.optimum",SpaceAfterOptimum.maker());
	propertyTable.put("start-indent",StartIndent.maker());
	propertyTable.put("end-indent",EndIndent.maker());
	propertyTable.put("provisional-distance-between-starts",ProvisionalDistanceBetweenStarts.maker());
	propertyTable.put("provisional-label-separation",ProvisionalLabelSeparation.maker());
	propertyTable.put("rule-thickness",RuleThickness.maker());
	propertyTable.put("color",Color.maker());
	propertyTable.put("wrap-option",WrapOption.maker());
	propertyTable.put("white-space-treatment",WhiteSpaceTreatment.maker());
	propertyTable.put("break-before",BreakBefore.maker());
	propertyTable.put("break-after",BreakAfter.maker());
	propertyTable.put("text-indent",TextIndent.maker());
	propertyTable.put("src",Src.maker());
	propertyTable.put("column-width",ColumnWidth.maker());
	propertyTable.put("keep-with-next",KeepWithNext.maker());
	propertyTable.put("background-color",BackgroundColor.maker());
	propertyTable.put("padding-top",PaddingTop.maker());
	propertyTable.put("padding-bottom",PaddingBottom.maker());
	propertyTable.put("padding-left",PaddingLeft.maker());
	propertyTable.put("padding-right",PaddingRight.maker());
	propertyTable.put("external-destination",ExternalDestination.maker());
        propertyTable.put("internal-destination",InternalDestination.maker());

	propertyTable.put("border-after-color",BorderAfterColor.maker());
	propertyTable.put("border-after-style",BorderAfterStyle.maker());
	propertyTable.put("border-after-width",BorderAfterWidth.maker());
	propertyTable.put("border-before-color",BorderBeforeColor.maker());
	propertyTable.put("border-before-style",BorderBeforeStyle.maker());
	propertyTable.put("border-before-width",BorderBeforeWidth.maker());
	propertyTable.put("border-bottom",BorderBottom.maker());
	propertyTable.put("border-bottom-color",BorderBottomColor.maker());
	propertyTable.put("border-bottom-style",BorderBottomStyle.maker());
	propertyTable.put("border-bottom-width",BorderBottomWidth.maker());
	propertyTable.put("border-color",BorderColor.maker());
	propertyTable.put("border-end-color",BorderEndColor.maker());
	propertyTable.put("border-end-style",BorderEndStyle.maker());
	propertyTable.put("border-end-width",BorderEndWidth.maker());
	propertyTable.put("border-left",BorderLeft.maker());
	propertyTable.put("border-left-color",BorderLeftColor.maker());
	propertyTable.put("border-left-style",BorderLeftStyle.maker());
	propertyTable.put("border-left-width",BorderLeftWidth.maker());
	propertyTable.put("border-right",BorderRight.maker());
	propertyTable.put("border-right-color",BorderRightColor.maker());
	propertyTable.put("border-right-style",BorderRightStyle.maker());
	propertyTable.put("border-right-width",BorderRightWidth.maker());
	propertyTable.put("border-start-color",BorderStartColor.maker());
	propertyTable.put("border-start-color",BorderStartColor.maker());
	propertyTable.put("border-start-width",BorderStartWidth.maker());
	propertyTable.put("border-style",BorderStyle.maker());
	propertyTable.put("border-top",BorderTop.maker());
	propertyTable.put("border-top-color",BorderTopColor.maker());
	propertyTable.put("border-top-style",BorderTopStyle.maker());
	propertyTable.put("border-top-style",BorderTopStyle.maker());
	propertyTable.put("border-width",BorderWidth.maker());
	propertyTable.put("bottom",Bottom.maker());
	propertyTable.put("height",Height.maker());
	propertyTable.put("left",Left.maker());
	propertyTable.put("padding",Padding.maker());
	propertyTable.put("padding-after",PaddingAfter.maker());
	propertyTable.put("padding-before",PaddingBefore.maker());
	propertyTable.put("padding-end",PaddingEnd.maker());
	propertyTable.put("padding-start",PaddingStart.maker());
	propertyTable.put("position",Position.maker());
	propertyTable.put("right",Right.maker());
	propertyTable.put("top",Top.maker());
	propertyTable.put("width",Width.maker());
	propertyTable.put("initial-page-number",InitialPageNumber.maker());
	propertyTable.put("ref-id",RefId.maker());  // used by page-number-citation
	propertyTable.put("id",Id.maker());			// attribute for objects, used by page-number-citation
	propertyTable.put("maximum-repeats",MaximumRepeats.maker());
	propertyTable.put("page-position",PagePosition.maker());
	propertyTable.put("odd-or-even",OddOrEven.maker());
	propertyTable.put("blank-or-not-blank",BlankOrNotBlank.maker());
	propertyTable.put("content-width",ContentWidth.maker());
	propertyTable.put("content-height",ContentHeight.maker());
	propertyTable.put("leader-pattern",LeaderPattern.maker());
	propertyTable.put("leader-length",LeaderLength.maker());
  propertyTable.put("rule-style",RuleStyle.maker());
	builder.addPropertyList(uri, propertyTable); 
    }
}
