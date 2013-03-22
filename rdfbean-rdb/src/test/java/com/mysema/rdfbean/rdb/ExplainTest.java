package com.mysema.rdfbean.rdb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.After;
import org.junit.Test;

import com.mysema.query.sql.H2Templates;
import com.mysema.query.sql.SQLQuery;
import com.mysema.query.sql.SQLTemplates;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Predicate;
import com.mysema.rdfbean.model.MemoryIdSequence;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;

public class ExplainTest {

    private SQLTemplates templates = new H2Templates(){{
        setSelect("explain select ");
    }};
    
    private JdbcConnectionPool dataSource;
    
    private Connection connection;
    
    @After
    public void tearDown() throws SQLException{
        if (connection != null){
            connection.close();    
        }        
        dataSource.dispose();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void test() throws SQLException{
        dataSource = JdbcConnectionPool.create("jdbc:h2:target/"+getClass().getSimpleName(), "sa", "");   
        dataSource.setMaxConnections(30);
        Configuration configuration = new DefaultConfiguration();
        RDBRepository repository = new RDBRepository(configuration, dataSource, new H2Templates(), new MemoryIdSequence());
        repository.initialize();
        repository.close();        
        connection = dataSource.getConnection();
        
        System.err.println("EXPLAIN PLANS");
        QStatement stmt = QStatement.statement;
        QSymbol symbol = QSymbol.symbol;
        
        // stmt
        Predicate s = stmt.subject.eq(1l);
        Predicate p = stmt.predicate.eq(1l);
        Predicate o = stmt.object.eq(1l);
        Predicate m = stmt.model.eq(1l);
        Expression[] projection = new Expression[]{stmt.subject, stmt.predicate, stmt.object, stmt.model};
        
        print(query().from(stmt).where(s).getResults(projection));  
        print(query().from(stmt).where(s,p).getResults(projection));    
        print(query().from(stmt).where(s,o).getResults(projection));    
        print(query().from(stmt).where(s,m).getResults(projection));    
        print(query().from(stmt).where(s,p,o).getResults(projection));  
        print(query().from(stmt).where(s,p,m).getResults(projection));  
        print(query().from(stmt).where(s,p,o,m).getResults(projection));
        
        print(query().from(stmt).where(p).getResults(projection));
        print(query().from(stmt).where(p,o).getResults(projection));    
        print(query().from(stmt).where(p,m).getResults(projection));
        print(query().from(stmt).where(p,o,m).getResults(projection));  
        
        print(query().from(stmt).where(o).getResults(projection));
        print(query().from(stmt).where(o,m).getResults(projection));
        
        print(query().from(stmt).where(m).getResults(projection));
        
        // symbol
        print(query().from(symbol).where(symbol.id.eq(1l)).getResults(symbol.lexical));
        print(query().from(symbol).where(symbol.lexical.eq("")).getResults(symbol.id));
        print(query().from(symbol).where(symbol.lexical.eq(""), symbol.datatype.eq(1l)).getResults(symbol.id));
        
    }
    
    private void print(ResultSet results) throws SQLException {
        try{
            while (results.next()){
                System.err.println(results.getString(1));
            }
        }finally{
            System.out.println();
            results.close();            
        }
        
    }

    private SQLQuery query(){
        return new SQLQuery(connection,templates);
    }
    
    
}
