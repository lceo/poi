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

package org.apache.poi.hsmf.parsers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.poi.hsmf.exceptions.DirectoryChunkNotFoundException;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.POIFSDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.property.DirectoryProperty;
import org.apache.poi.poifs.property.DocumentProperty;
import org.apache.poi.poifs.storage.BlockWritable;

/**
 * Provides a HashMap with the ability to parse a PIOFS object and provide 
 * an 'easy to access' hashmap structure for the document chunks inside it.
 * 
 * @author Travis Ferguson
 */
public class POIFSChunkParser {
	/**
	 * Constructor 
	 * @param fs
	 * @throws IOException 
	 */
	public POIFSChunkParser(POIFSFileSystem fs) throws IOException {
		this.setFileSystem(fs);
	}


	/**
	 * Set the POIFileSystem object that this object is using.
	 * @param fs
	 * @throws IOException 
	 */
	public void setFileSystem(POIFSFileSystem fs) throws IOException {
		this.fs = fs;
		this.reparseFileSystem();
	}

	/**
	 * Get a reference to the FileSystem object that this object is currently using.
	 * @return
	 */
	public POIFSFileSystem getFileSystem() {
		return this.fs;
	}

	/**
	 * Reparse the FileSystem object, resetting all the chunks stored in this object
	 * @throws IOException 
	 *
	 */
	public void reparseFileSystem() throws IOException {
		// first clear this object of all chunks
		DirectoryEntry root = this.fs.getRoot();
		Iterator iter = root.getEntries();
		
		this.directoryMap = this.processPOIIterator(iter);
	}

	/**
	 * Pull the chunk data that's stored in this object's hashmap out and return it as a HashMap.
	 * @param entryName
	 * @return
	 */
	public Object getChunk(HashMap dirMap, String entryName) {
		if(dirMap == null) return null;
		else {
			return dirMap.get(entryName);
		}
	}
	
	/**
	 * Pull a directory/hashmap out of this hashmap and return it
	 * @param directoryName
	 * @return HashMap containing the chunks stored in the named directoryChunk
	 * @throws DirectoryChunkNotFoundException This is thrown should the directoryMap HashMap on this object be null
	 * or for some reason the directory is not found, is equal to null, or is for some reason not a HashMap/aka Directory Node.
	 */
	public HashMap getDirectoryChunk(String directoryName) throws DirectoryChunkNotFoundException {
		DirectoryChunkNotFoundException excep = new DirectoryChunkNotFoundException(directoryName);
		Object obj = getChunk(this.directoryMap, directoryName);
		if(obj == null || !(obj instanceof HashMap)) throw excep;
		
		return (HashMap)obj;
	}
	
	/**
	 * Pulls a ByteArrayOutputStream from this objects HashMap, this can be used to read a byte array of the contents of the given chunk.
	 * @param directoryMap, chunk
	 * @return
	 * @throws ChunkNotFoundException
	 */
	public Chunk getDocumentNode(HashMap dirNode, Chunk chunk) throws ChunkNotFoundException {
		String entryName = chunk.getEntryName();
		ChunkNotFoundException excep = new ChunkNotFoundException(entryName);
		Object obj = getChunk(dirNode, entryName);
		if(obj == null || !(obj instanceof ByteArrayOutputStream)) throw excep;
		
		chunk.setValue((ByteArrayOutputStream)obj);
		
		return chunk;
	}
	
	/**
	 * Pulls a Chunk out of this objects root Node tree.
	 * @param chunk
	 * @return
	 * @throws ChunkNotFoundException
	 */
	public Chunk getDocumentNode(Chunk chunk) throws ChunkNotFoundException {
		return getDocumentNode(this.directoryMap, chunk);
	}
	
	
	/**
	 * Processes an iterator returned by a POIFS call to getRoot().getEntries()
	 * @param iter
	 * @return
	 * @throws IOException
	 */
	private HashMap processPOIIterator(Iterator iter) throws IOException {
        HashMap currentNode = new HashMap();
        
        while(iter.hasNext()) {
            Object obj = iter.next();
            if(obj instanceof DocumentNode) {
                this.processDocumentNode((DocumentNode)obj, currentNode);
            } else if(obj instanceof DirectoryNode) {
                String blockName = ((DirectoryNode)obj).getName();
                Iterator viewIt = null;
                if( ((DirectoryNode)obj).preferArray()) {
                    Object[] arr = ((DirectoryNode)obj).getViewableArray();
                    ArrayList viewList = new ArrayList(arr.length);

                    for(int i = 0; i < arr.length; i++) {
                        viewList.add(arr[i]);
                    }
                    viewIt = viewList.iterator();
                } else {
                        viewIt = ((DirectoryNode)obj).getViewableIterator();
                }
                //store the next node on the hashmap
                currentNode.put(blockName, processPOIIterator(viewIt));
            } else if(obj instanceof DirectoryProperty) {
            	//don't do anything with the directory property chunk...
            } else {
                    System.err.println("Unknown node: " + obj.toString());
            }
        }
        return currentNode;
	}	

	/**
     * Processes a document node and adds it to the current directory HashMap
     * @param obj 
     * @throws java.io.IOException 
     */
    private void processDocumentNode(DocumentNode obj, HashMap currentObj) throws IOException {
        String blockName = ((DocumentNode)obj).getName();
        
        Iterator viewIt = null;
        if( ((DocumentNode)obj).preferArray()) {
            Object[] arr = ((DocumentNode)obj).getViewableArray();
            ArrayList viewList = new ArrayList(arr.length);

            for(int i = 0; i < arr.length; i++) {
                    viewList.add(arr[i]);
            }
            viewIt = viewList.iterator();
        } else {
                viewIt = ((DocumentNode)obj).getViewableIterator();
        }

        while(viewIt.hasNext()) {
            Object view = viewIt.next();

            if(view instanceof DocumentProperty) {
                    //we don't care about the properties
            } else if(view instanceof POIFSDocument) {
                    //check if our node has blocks or if it can just be read raw.
                    int blockCount = ((POIFSDocument)view).countBlocks();
                    //System.out.println("Block Name: " + blockName);
                    if(blockCount <= 0) {
                    	ByteArrayOutputStream out = new ByteArrayOutputStream();
                        
                        BlockWritable[] bws = ((POIFSDocument)view).getSmallBlocks();
                        for(int i = 0; i < bws.length; i++) {
                                bws[i].writeBlocks(out);
                        }
                        currentObj.put(blockName, out);		
                    } else {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        ((POIFSDocument)view).writeBlocks(out);                    
                        currentObj.put(blockName, out);
                    }
            } else {
                System.err.println("Unknown View Type: " + view.toString());
            }
        }
    }

	/* private instance variables */
	private static final long serialVersionUID = 1L;
	private POIFSFileSystem fs;
	private HashMap directoryMap;
}