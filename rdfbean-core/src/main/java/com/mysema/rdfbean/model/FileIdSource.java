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

/**
 * FileIdSource provides
 *
 * @author tiwe
 * @version $Id$
 */
public class FileIdSource implements Closeable, IdSource{
    
    private final ByteBuffer buffer = ByteBuffer.allocate(8);
    
    private final File file;
    
    private final FileChannel fileChannel;
    
    private final int granularity;
    
    private volatile long nextId = 1l;
    
    private volatile long maxId = 100l;
    
    public FileIdSource(File file) {
        this(file, 100);
    }
    
    public FileIdSource(File file, int granularity) {
        try {
            if (!file.exists()){
                if (!file.getParentFile().exists()){
                    file.getParentFile().mkdirs();
                }                
                file.createNewFile();
            }            
            this.file = file;
            this.fileChannel = new RandomAccessFile(file, "rwd").getChannel();
            this.granularity = granularity;
            synchronize();      
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void synchronize() throws IOException{
        // TODO : this method needs execlusive access to the file
        
        // get the next id
        if (file.length() > 0l){
            fileChannel.read(buffer, 0l);            
            buffer.rewind();
            nextId = buffer.getLong();            
        }else{
            nextId = 1l;
        }
        maxId = nextId + granularity - 1l;
        
        // set the next id
        buffer.rewind();
        buffer.putLong(0, nextId + granularity);
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
            throw new RuntimeException(e);
        }        
    }

    public void close() throws IOException{
        fileChannel.close();
    }
    
}
