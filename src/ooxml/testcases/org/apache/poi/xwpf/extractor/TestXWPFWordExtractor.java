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

import java.io.File;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.xwpf.XWPFDocument;

import junit.framework.TestCase;

/**
 * Tests for HXFWordExtractor
 */
public class TestXWPFWordExtractor extends TestCase {
	/**
	 * A very simple file
	 */
	private XWPFDocument xmlA;
	private File fileA;
	/**
	 * A fairly complex file
	 */
	private XWPFDocument xmlB;
	private File fileB;

	protected void setUp() throws Exception {
		super.setUp();
		
		fileA = new File(
				System.getProperty("HWPF.testdata.path") +
				File.separator + "sample.docx"
		);
		fileB = new File(
				System.getProperty("HWPF.testdata.path") +
				File.separator + "IllustrativeCases.docx"
		);
		assertTrue(fileA.exists());
		assertTrue(fileB.exists());
		
		xmlA = new XWPFDocument(POIXMLDocument.openPackage(fileA.toString()));
		xmlB = new XWPFDocument(POIXMLDocument.openPackage(fileB.toString()));
	}

	/**
	 * Get text out of the simple file
	 */
	public void testGetSimpleText() throws Exception {
		new XWPFWordExtractor(xmlA);
		new XWPFWordExtractor(POIXMLDocument.openPackage(fileA.toString()));
		
		XWPFWordExtractor extractor = 
			new XWPFWordExtractor(xmlA);
		extractor.getText();
		
		String text = extractor.getText();
		assertTrue(text.length() > 0);
		
		// Check contents
		assertTrue(text.startsWith(
				"Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Nunc at risus vel erat tempus posuere. Aenean non ante. Suspendisse vehicula dolor sit amet odio."
		));
		assertTrue(text.endsWith(
				"Phasellus ultricies mi nec leo. Sed tempus. In sit amet lorem at velit faucibus vestibulum.\n"
		));
		
		// Check number of paragraphs
		int ps = 0;
		char[] t = text.toCharArray();
		for (int i = 0; i < t.length; i++) {
			if(t[i] == '\n') { ps++; }
		}
		assertEquals(3, ps);
	}
	
	/**
	 * Tests getting the text out of a complex file
	 */
	public void testGetComplexText() throws Exception {
		XWPFWordExtractor extractor = 
			new XWPFWordExtractor(xmlB);
		extractor.getText();
		
		String text = extractor.getText();
		assertTrue(text.length() > 0);
		
		char euro = '\u20ac';
		System.err.println("'"+text.substring(text.length() - 20) + "'");
		
		// Check contents
		assertTrue(text.startsWith(
				"  \n(V) ILLUSTRATIVE CASES\n\n"
		));
		assertTrue(text.endsWith(
				"As well as gaining "+euro+"90 from child benefit increases, he will also receive the early childhood supplement of "+euro+"250 per quarter for Vincent for the full four quarters of the year.\n\n\n\n \n\n\n"
		));
		
		// Check number of paragraphs
		int ps = 0;
		char[] t = text.toCharArray();
		for (int i = 0; i < t.length; i++) {
			if(t[i] == '\n') { ps++; }
		}
		assertEquals(79, ps);
	}
}