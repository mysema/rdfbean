package com.mysema.rdfbean.virtuoso;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.ID;
import com.mysema.rdfbean.model.LIT;
import com.mysema.rdfbean.model.RDFBeanTransaction;
import com.mysema.rdfbean.model.STMT;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.model.XSD;

@Ignore
public class BulkLoadTest extends AbstractConnectionTest{

    @Test
    public void Load(){
        List<UID> predicates = new ArrayList<UID>(10);
        for (int i = 0; i < 10; i++){
            predicates.add(new UID(TEST.NS, "pred"+i));
        }
        
        
        List<STMT> stmts = new ArrayList<STMT>(14000);
        for (int i = 0; i < 1400; i++){
            ID sub = new BID();  
            stmts.add(new STMT(sub, predicates.get(0), new LIT(UUID.randomUUID().toString())));
            stmts.add(new STMT(sub, predicates.get(1), new LIT("1", XSD.intType)));
            stmts.add(new STMT(sub, predicates.get(2), sub));
            stmts.add(new STMT(sub, predicates.get(3), new BID()));
            stmts.add(new STMT(sub, predicates.get(4), new LIT(UUID.randomUUID().toString())));
            stmts.add(new STMT(sub, predicates.get(5), new LIT("2", XSD.intType)));
            stmts.add(new STMT(sub, predicates.get(6), sub));
            stmts.add(new STMT(sub, predicates.get(7), new BID()));
            stmts.add(new STMT(sub, predicates.get(8), new LIT(UUID.randomUUID().toString())));
            stmts.add(new STMT(sub, predicates.get(9), new LIT("3", XSD.intType)));
        }
        
        toBeRemoved = stmts;
        long start = System.currentTimeMillis();
        RDFBeanTransaction tx = connection.beginTransaction(false, RDFBeanTransaction.TIMEOUT, RDFBeanTransaction.ISOLATION);
        try{
            connection.update(null, stmts);    
            tx.commit();
        }catch(Exception e){
            tx.rollback();
            throw new RuntimeException(e);
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println(duration / 1000);
    }
    
    public static void main(String[] args){
        BulkLoadTest test = new BulkLoadTest();
        try{
            setUpClass();
            test.setUp();
            test.Load();    
        }finally{
            test.tearDown();
            tearDownClass();    
        }                
    }
    
}
