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
package org.apache.poi.hwpf.converter;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Picture;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test cases for {@link WordToHtmlConverter}
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
public class TestWordToHtmlConverter extends TestCase
{
    private static void assertContains( String result, final String substring )
    {
        if ( !result.contains( substring ) )
            fail( "Substring \"" + substring
                    + "\" not found in the following string: \"" + result
                    + "\"" );
    }

    private static String getHtmlText( final String sampleFileName )
            throws Exception
    {
        return getHtmlText( sampleFileName, false );
    }

    private static String getHtmlText( final String sampleFileName,
            boolean emulatePictureStorage ) throws Exception
    {
        HWPFDocument hwpfDocument = new HWPFDocument( POIDataSamples
                .getDocumentInstance().openResourceAsStream( sampleFileName ) );

        Document newDocument = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        WordToHtmlConverter wordToHtmlConverter = !emulatePictureStorage ? new WordToHtmlConverter(
                newDocument ) : new WordToHtmlConverter( newDocument )
        {
            @Override
            protected void processImage( Element currentBlock, boolean inlined,
                    Picture picture )
            {
                processImage( currentBlock, inlined, picture, "picture.bin" );
            }
        };
        wordToHtmlConverter.processDocument( hwpfDocument );

        StringWriter stringWriter = new StringWriter();

        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
        transformer.setOutputProperty( OutputKeys.ENCODING, "utf-8" );
        transformer.setOutputProperty( OutputKeys.METHOD, "html" );
        transformer.transform(
                new DOMSource( wordToHtmlConverter.getDocument() ),
                new StreamResult( stringWriter ) );

        String result = stringWriter.toString();
        return result;
    }

    public void testAIOOBTap() throws Exception
    {
        String result = getHtmlText( "AIOOB-Tap.doc" );
        assertContains( result.substring( 0, 6000 ), "<table class=\"t1\">" );
    }

    public void testBug33519() throws Exception
    {
        String result = getHtmlText( "Bug33519.doc" );
        assertContains(
                result,
                "\u041F\u043B\u0430\u043D\u0438\u043D\u0441\u043A\u0438 \u0442\u0443\u0440\u043E\u0432\u0435" );
        assertContains( result,
                "\u042F\u0432\u043E\u0440 \u0410\u0441\u0435\u043D\u043E\u0432" );
    }

    public void testBug46610_2() throws Exception
    {
        String result = getHtmlText( "Bug46610_2.doc" );
        assertContains(
                result,
                "012345678911234567892123456789312345678941234567890123456789112345678921234567893123456789412345678" );
    }

    public void testBug46817() throws Exception
    {
        String result = getHtmlText( "Bug46817.doc" );
        final String substring = "<table class=\"t1\">";
        assertContains( result, substring );
    }

    public void testBug47286() throws Exception
    {
        String result = getHtmlText( "Bug47286.doc" );

        assertFalse( result.contains( "FORMTEXT" ) );

        assertContains( result, "color:#28624f;" );
        assertContains( result, "Passport No and the date of expire" );
        assertContains( result, "mfa.gov.cy" );
    }

    public void testBug48075() throws Exception
    {
        getHtmlText( "Bug48075.doc" );
    }

    public void testDocumentProperties() throws Exception
    {
        String result = getHtmlText( "documentProperties.doc" );

        assertContains( result, "<title>This is document title</title>" );
        assertContains( result,
                "<meta content=\"This is document keywords\" name=\"keywords\">" );
    }

    public void testEmailhyperlink() throws Exception
    {
        String result = getHtmlText( "Bug47286.doc" );
        final String substring = "provisastpet@mfa.gov.cy";
        assertContains( result, substring );
    }

    public void testEndnote() throws Exception
    {
        String result = getHtmlText( "endingnote.doc" );

        assertContains(
                result,
                "<a class=\"a1 endnoteanchor\" href=\"#endnote_1\" name=\"endnote_back_1\">1</a>" );
        assertContains(
                result,
                "<a class=\"a1 endnoteindex\" href=\"#endnote_back_1\" name=\"endnote_1\">1</a> <span" );
        assertContains( result, "Ending note text" );
    }

    public void testEquation() throws Exception
    {
        String result = getHtmlText( "equation.doc" );

        assertContains( result, "<!--Image link to '0.emf' can be here-->" );
    }

    public void testPicture() throws Exception
    {
        String result = getHtmlText( "picture.doc", true );

        // picture
        assertContains( result, "src=\"picture.bin\"" );
        // visible size
        assertContains( result, "width:3.1305554in;height:1.7250001in;" );
        // shift due to crop
        assertContains( result, "left:-0.09375;top:-0.25694445;" );
        // size without crop
        assertContains( result, "width:3.4125in;height:2.325in;" );
    }

    public void testHyperlink() throws Exception
    {
        String result = getHtmlText( "hyperlink.doc" );

        assertContains( result, "<span>Before text; </span><a " );
        assertContains( result,
                "<a href=\"http://testuri.org/\"><span>Hyperlink text</span></a>" );
        assertContains( result, "</a><span>; after text</span>" );
    }

    public void testInnerTable() throws Exception
    {
        getHtmlText( "innertable.doc" );
    }

    public void testTableMerges() throws Exception
    {
        String result = getHtmlText( "table-merges.doc" );
        
        assertContains( result, "<td class=\"td1\" colspan=\"3\">" );
        assertContains( result, "<td class=\"td2\" colspan=\"2\">" );
    }

    public void testO_kurs_doc() throws Exception
    {
        getHtmlText( "o_kurs.doc" );
    }

    public void testPageref() throws Exception
    {
        String result = getHtmlText( "pageref.doc" );

        assertContains( result, "<a href=\"#userref\">" );
        assertContains( result, "<a name=\"userref\">" );
        assertContains( result, "1" );
    }
}
