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
public class FileIdSource implements Closeable{
    
    private final ByteBuffer buffer = ByteBuffer.allocate(8);
    
    private final FileChannel fileChannel;
    
    private volatile long lastId = 0l;
    
    private final int granularity;
    
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
            this.fileChannel = new RandomAccessFile(file, "rwd").getChannel();
            this.granularity = granularity;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            if (file.length() > 0l){
                fileChannel.read(buffer, 0l);
                buffer.rewind();
                lastId = buffer.getLong();
                long mod = lastId % granularity; 
                lastId = lastId - mod + granularity;
            }            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized long getNextId() {                
        try {
            ++lastId;            
            if (lastId % granularity == 0l || lastId == 1l){
                buffer.putLong(0, lastId);            
                fileChannel.write(buffer, 0l);
                buffer.rewind();    
            }            
            return lastId;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }        
    }

    public void close() throws IOException{
        fileChannel.close();
    }
}
