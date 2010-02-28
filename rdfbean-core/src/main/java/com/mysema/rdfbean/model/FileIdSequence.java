/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.rdfbean.model;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FileIdSequence provides
 *
 * @author tiwe
 * @version $Id$
 */
public class FileIdSequence implements Closeable, IdSequence{
    
    private static final Logger logger = LoggerFactory.getLogger(FileIdSequence.class);
    
    private final ByteBuffer buffer = ByteBuffer.allocate(8);
    
    private final File file;
    
    private final FileChannel fileChannel;
    
    private final int cache;
    
    private volatile long nextId = 1l;
    
    private volatile long maxId = 100l;
    
    public FileIdSequence(File file) {
        this(file, 100);
    }
    
    public FileIdSequence(File file, int cache) {
        try {
            if (!file.exists()){
                if (!file.getParentFile().exists()){
                    if (!file.getParentFile().mkdirs()){
                        logger.error("Creation of " + file.getParentFile().getPath() + " failed");
                    }
                }                
                if (!file.createNewFile()){
                    logger.error("Creation of " + file.getPath() + " failed");
                }
            }            
            this.file = file;
            this.fileChannel = new RandomAccessFile(file, "rwd").getChannel();
            this.cache = cache;
            synchronize();      
        } catch (FileNotFoundException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }
    
    private void synchronize() throws IOException{
        // get the next id from file
        if (file.length() > 0l){
            fileChannel.read(buffer, 0l);            
            buffer.rewind();
            nextId = buffer.getLong();            
        }else{
            nextId = 1l;
        }
        maxId = nextId + cache - 1l;
        
        // set the next id to file
        buffer.rewind();
        buffer.putLong(0, nextId + cache);
        fileChannel.write(buffer, 0l);
        buffer.rewind();
        
    }

    /* (non-Javadoc)
     * @see com.mysema.rdfbean.model.IdSource#getNextId()
     */
    public synchronized long getNextId() {                
        try {            
            if (nextId > maxId){                
                synchronize();
            }            
            return nextId++;
        } catch (IOException e) {
            throw new RepositoryException(e);
        }        
    }

    public void close() throws IOException{
        fileChannel.close();
    }
    
}
