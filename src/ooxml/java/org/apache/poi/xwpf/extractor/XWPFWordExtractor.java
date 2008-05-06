/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.xwpf.extractor;

import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.xwpf.XWPFDocument;
import org.apache.poi.xwpf.model.XWPFCommentsDecorator;
import org.apache.poi.xwpf.model.XWPFHyperlinkDecorator;
import org.apache.poi.xwpf.model.XWPFParagraphDecorator;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.xmlbeans.XmlException;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;

/**
 * Helper class to extract text from an OOXML Word file
 */
public class XWPFWordExtractor extends POIXMLTextExtractor {
	private XWPFDocument document;
	private boolean fetchHyperlinks = false;
	
	public XWPFWordExtractor(Package container) throws XmlException, OpenXML4JException, IOException {
		this(new XWPFDocument(container));
	}
	public XWPFWordExtractor(XWPFDocument document) {
		super(document);
		this.document = document;
	}

	/**
	 * Should we also fetch the hyperlinks, when fetching 
	 *  the text content? Default is to only output the
	 *  hyperlink label, and not the contents
	 */
	public void setFetchHyperlinks(boolean fetch) {
		fetchHyperlinks = fetch;
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			System.err.println("Use:");
			System.err.println("  HXFWordExtractor <filename.xlsx>");
			System.exit(1);
		}
		POIXMLTextExtractor extractor = 
			new XWPFWordExtractor(POIXMLDocument.openPackage(
					args[0]
			));
		System.out.println(extractor.getText());
	}
	
	public String getText() {
		StringBuffer text = new StringBuffer();
		
			
		Iterator<XWPFParagraph> i = document.getParagraphsIterator();
		while(i.hasNext()) {
			XWPFParagraphDecorator decorator = new XWPFCommentsDecorator(
					new XWPFHyperlinkDecorator(i.next(), null, fetchHyperlinks));
			text.append(decorator.getText()+"\n");
		}
			
		Iterator<XWPFTable> j = document.getTablesIterator();
		while(j.hasNext())
		{
			text.append(j.next().getText()+"\n");
		}
		
		return text.toString();
	}
}