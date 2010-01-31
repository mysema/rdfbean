package com.mysema.rdfbean.load;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.identity.DerbyIdentityService;
import com.mysema.rdfbean.object.identity.IdentityService;
import com.mysema.rdfbean.object.identity.MemoryIdentityService;

/**
 * IdentityServiceTest provides
 *
 * @author tiwe
 * @version $Id$
 */
public class IdentityServiceTest {
    
    private String databaseName = "target/derbydb"; 
    
    @Test
    public void test() throws IOException{
        int iterations = 1000;
        load(MemoryIdentityService.instance(), iterations);       //   16 ms
        load(new DerbyIdentityService(databaseName), iterations); // 1656 ms
    }
    
    private void load(IdentityService identityService, int count){        
        List<BID> ids = new ArrayList<BID>(count);
        for (int i = 0; i < count; i++){
            ids.add(new BID());
        }
        
        UID model = new UID(TEST.NS,"model");
        long s = System.currentTimeMillis();
        for (BID id : ids){
            identityService.getLID(model, id);
        }
        long e = System.currentTimeMillis();
        
        System.out.println(identityService.getClass().getSimpleName());
        System.out.println((e-s)+"ms");
        System.out.println();
    }
}
