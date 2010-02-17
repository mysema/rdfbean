package com.mysema.rdfbean.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * FileIdSource provides
 *
 * @author tiwe
 * @version $Id$
 */
public class FileIdSource {

    private final RandomAccessFile file;
    
    private long lastId = 0l;
    
    public FileIdSource(File file) {
        try {
            if (!file.exists()){
                if (!file.getParentFile().exists()){
                    file.getParentFile().mkdirs();
                }                
                file.createNewFile();
            }            
            this.file = new RandomAccessFile(file, "rwd");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            if (file.length() > 0l){
                lastId = this.file.readLong();    
            }            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized long getNextId() {                
        try {
            ++lastId;
            file.seek(0l);
            file.writeLong(lastId);
            return lastId;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }        
    }

}
