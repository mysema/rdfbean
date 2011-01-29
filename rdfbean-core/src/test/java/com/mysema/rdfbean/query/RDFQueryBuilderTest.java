package com.mysema.rdfbean.query;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.support.QueryMixin;
import com.mysema.query.types.PathImpl;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.RDFQueryImpl;
import com.mysema.rdfbean.object.BeanSubQuery;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionImpl;

@SuppressWarnings("unchecked")
public class RDFQueryBuilderTest {
    
    private QueryMetadata metadata;
    
    private RDFQueryBuilder builder;
    
    private PathBuilder<User> user = new PathBuilder<User>(User.class, "user");
    
    private PathBuilder<User> user2 = new PathBuilder<User>(User.class, "user2");
    
    private QueryMixin query;
    
    @Before
    public void setUp(){
        metadata = new DefaultQueryMetadata();
        RDFConnection connection = new MiniRepository().openConnection();
        Configuration configuration = new DefaultConfiguration(User.class);
        Session session = new SessionImpl(configuration, connection);
        builder = new RDFQueryBuilder(connection, session, configuration, metadata);
        query = new QueryMixin(metadata);
    }
        
    @Test
    public void Single_From_is_Preserved() throws Exception {
        query.from(user);
        assertEquals("SELECT WHERE { ?user ?_c1 ?_c2 }");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void From_Unknown_Type() throws Exception{
        query.from(new PathImpl(Object.class, "o"));
        assertEquals("SELECT WHERE { ?user ?_c1 ?_c2 }");
    }
    
    @Test
    public void Two_Froms_are_Preserved() throws Exception {
        query.from(user, user2);        
        assertEquals("SELECT WHERE { ?user ?_c1 ?_c2 . ?user2 ?_c1 ?_c2 }");
    }

    @Test
    public void Two_Froms_One_Filter() throws Exception {
        query.from(user, user2);    
        query.where(user.eq(user2));
        assertEquals("SELECT WHERE { ?user ?_c1 ?_c2 . ?user2 ?_c1 ?_c2 . FILTER(?user = ?user2) }");
    }
    
    @Test
    public void Single_From_With_Property() throws Exception {
        query.from(user);    
        query.where(user.getString("firstName").eq("Bob"));
        assertEquals("SELECT WHERE { ?user ?_c1 ?_c2 ; ?_c3 ?user_firstName . FILTER(?user_firstName = ?_c4) }");
    }
    
    @Test
    @Ignore
    public void Starts_With() throws Exception{
        query.from(user);    
        query.where(user.getString("firstName").startsWith("Bob"));
        assertEquals("SELECT WHERE { ?user ?_c1 ?_c2 ; ?_c3 ?user_firstName . FILTER(regex(?user_firstName, '^Bob')) }");
    }
    
    @Test
    public void Between() throws Exception{
        query.from(user);
        query.where(user.getString("firstName").between("A", "D"));
        assertEquals("SELECT WHERE { ?user ?_c1 ?_c2 ; ?_c3 ?user_firstName . FILTER(?user_firstName >= ?_c4 && ?user_firstName <= ?_c5) }");
    }
    
    @Test
    public void In() throws Exception{
        query.from(user);
        query.where(user.getString("firstName").in("Dennis", "Bob"));
        assertEquals("SELECT WHERE { ?user ?_c1 ?_c2 ; ?_c3 ?user_firstName . FILTER(?user_firstName = ?_c4 || ?user_firstName = ?_c5) }");
    }
    
    @Test
    public void InstanceOf() throws Exception{
        query.from(user);
        query.where(user.instanceOf(User.class));
        assertEquals("SELECT WHERE { ?user ?_c1 ?_c2 , ?_c2 }");
    }
    
    @Test
    public void SubQuery_Exists() throws Exception{
        query.from(user);
        query.where(new BeanSubQuery().from(user2).where(user2.get("firstName").eq(user.get("firstName"))).exists());
        assertEquals("SELECT WHERE { ?user ?_c1 ?_c2 . FILTER(exists({ ?user2 ?_c1 ?_c2 ; ?_c3 ?user2_firstName . ?user ?_c3 ?user_firstName . FILTER(?user2_firstName = ?user_firstName) } )) }");
    }
    
    @Test
    public void Limit() throws Exception{
        query.from(user);
        query.limit(4);
        assertEquals("SELECT WHERE { ?user ?_c1 ?_c2 } LIMIT 4");
    }
    
    @Test
    public void Offset() throws Exception{
        query.from(user);
        query.offset(4);
        assertEquals("SELECT WHERE { ?user ?_c1 ?_c2 } OFFSET 4");
    }
    
    @Test
    public void Order_By() throws Exception{
        query.from(user);
        query.orderBy(user.getString("firstName").asc());
        assertEquals("SELECT WHERE { ?user ?_c1 ?_c2 . OPTIONAL {?user ?_c3 ?user_firstName } } ORDER BY ?user_firstName");
    }
    
    private void assertEquals(String query) throws Exception{
        RDFQueryImpl rdfQuery = builder.build(false);
        Method method = RDFQueryImpl.class.getDeclaredMethod("aggregateFilters");
        method.setAccessible(true);
        method.invoke(rdfQuery);
        Assert.assertEquals(query, rdfQuery.toString().replaceAll("\\s+", " ").trim());
    }

}
