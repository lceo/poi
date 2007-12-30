        
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

package org.apache.poi.hssf.record.formula;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Tests for {@link ReferencePtg}.
 */
public class TestReferencePtg extends AbstractPtgTestCase
{
    /**
     * Tests reading a file containing this ptg.
     */
    public void testReading() throws Exception
    {
        HSSFWorkbook workbook = loadWorkbook("ReferencePtg.xls");
        HSSFSheet sheet = workbook.getSheetAt(0);

        // First row
        assertEquals("Wrong numeric value for original number", 55.0,
                     sheet.getRow(0).getCell((short) 0).getNumericCellValue(), 0.0);
        assertEquals("Wrong numeric value for referemce", 55.0,
                     sheet.getRow(0).getCell((short) 1).getNumericCellValue(), 0.0);
        assertEquals("Wrong formula string for reference", "A1",
                     sheet.getRow(0).getCell((short) 1).getCellFormula());
        
        // Now moving over the 2**15 boundary
        // (Remember that excel row (n) is poi row (n-1)
        assertEquals("Wrong numeric value for original number", 32767.0,
                sheet.getRow(32766).getCell((short) 0).getNumericCellValue(), 0.0);
        assertEquals("Wrong numeric value for referemce", 32767.0,
                sheet.getRow(32766).getCell((short) 1).getNumericCellValue(), 0.0);
        assertEquals("Wrong formula string for reference", "A32767",
                sheet.getRow(32766).getCell((short) 1).getCellFormula());
        
        assertEquals("Wrong numeric value for original number", 32768.0,
                sheet.getRow(32767).getCell((short) 0).getNumericCellValue(), 0.0);
        assertEquals("Wrong numeric value for referemce", 32768.0,
                sheet.getRow(32767).getCell((short) 1).getNumericCellValue(), 0.0);
        assertEquals("Wrong formula string for reference", "A32768",
                sheet.getRow(32767).getCell((short) 1).getCellFormula());
        
        assertEquals("Wrong numeric value for original number", 32769.0,
                sheet.getRow(32768).getCell((short) 0).getNumericCellValue(), 0.0);
        assertEquals("Wrong numeric value for referemce", 32769.0,
                sheet.getRow(32768).getCell((short) 1).getNumericCellValue(), 0.0);
        assertEquals("Wrong formula string for reference", "A32769",
                sheet.getRow(32768).getCell((short) 1).getCellFormula());
        
        assertEquals("Wrong numeric value for original number", 32770.0,
                sheet.getRow(32769).getCell((short) 0).getNumericCellValue(), 0.0);
        assertEquals("Wrong numeric value for referemce", 32770.0,
                sheet.getRow(32769).getCell((short) 1).getNumericCellValue(), 0.0);
        assertEquals("Wrong formula string for reference", "A32770",
                sheet.getRow(32769).getCell((short) 1).getCellFormula());
    }
}

