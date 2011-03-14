package com.mysema.rdfbean.query;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.query.DefaultQueryMetadata;
import com.mysema.query.QueryMetadata;
import com.mysema.query.support.QueryMixin;
import com.mysema.query.types.PathImpl;
import com.mysema.query.types.path.PathBuilder;
import com.mysema.query.types.template.BooleanTemplate;
import com.mysema.rdfbean.TEST;
import com.mysema.rdfbean.model.BID;
import com.mysema.rdfbean.model.MiniRepository;
import com.mysema.rdfbean.model.QueryLanguage;
import com.mysema.rdfbean.model.RDFConnection;
import com.mysema.rdfbean.model.RDFQueryImpl;
import com.mysema.rdfbean.model.SPARQLVisitor;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.object.BeanSubQuery;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.ConfigurationOntology;
import com.mysema.rdfbean.object.DefaultConfiguration;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionImpl;
import com.mysema.rdfbean.ontology.Ontology;

@SuppressWarnings("unchecked")
public class RDFQueryBuilderTest {

    private QueryMetadata metadata;

    private RDFQueryBuilder builder;

    private final PathBuilder<User> user = new PathBuilder<User>(User.class, "user");

    private final PathBuilder<User> user2 = new PathBuilder<User>(User.class, "user2");

    private QueryMixin query;

    private User createUser(){
        User user = new User();
        user.id = new BID();
        return user;
    }

    @Before
    public void setUp(){
        metadata = new DefaultQueryMetadata();
        RDFConnection connection = new MiniRepository().openConnection();
        Configuration configuration = new DefaultConfiguration(User.class);
        Ontology ontology = new ConfigurationOntology(configuration);
        Session session = new SessionImpl(configuration, new ConfigurationOntology(configuration), connection);
        builder = new RDFQueryBuilder(connection, session, configuration, ontology, metadata);
        query = new QueryMixin(metadata);
    }

    @Test
    public void Between() throws Exception{
        query.from(user);
        query.where(user.getString("firstName").between("A", "D"));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_firstName . FILTER(?user_firstName >= ?_c6 && ?user_firstName <= ?_c7) }");
    }

    @Test
    public void Contains_Key() throws Exception{
        query.from(user);
        query.where(user.getMap("buddiesMapped", String.class, User.class).containsKey("XXX"));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_buddiesMapped . ?user_buddiesMapped ?_c6 ?_c7 }");
    }

    @Test
    public void Contains_Key_Not() throws Exception{
        query.from(user);
        query.where(user.getMap("buddiesMapped", String.class, User.class).containsKey("XXX").not());
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_buddiesMapped . FILTER(!exists {{ ?user_buddiesMapped ?_c6 ?_c7 } }) }");
    }

    @Test
    public void Contains_Value() throws Exception{
        query.from(user);
        query.where(user.getMap("buddiesMapped", String.class, User.class).containsValue(createUser()));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_buddiesMapped . FILTER(?user_buddiesMapped = ?_c6) }");
    }

    @Test
    public void Contains_Value_Not() throws Exception{
        query.from(user);
        query.where(user.getMap("buddiesMapped", String.class, User.class).containsValue(createUser()).not());
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_buddiesMapped . FILTER(!(?user_buddiesMapped = ?_c6)) }");
    }

    @Test
    public void Distinct() throws Exception{
        query.from(user);
        query.distinct();
        assertEquals("SELECT DISTINCT * WHERE { ?user ?_c2 ?_c3 }");
    }

    @Test
    public void Enum_Ordinal(){
        query.from(user);
//        query.orderBy(user.getSimple("gender", User.Gender.class))
        // TODO : use PathBuilder.getEnum
    }

    @Test
    public void Ends_With() throws Exception{
        query.from(user);
        query.where(user.getString("firstName").endsWith("Bob"));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_firstName . FILTER(regex(?user_firstName, 'Bob$')) }");
    }

    @Test(expected=IllegalArgumentException.class)
    public void From_Unknown_Type() throws Exception{
        query.from(new PathImpl(Object.class, "o"));
        assertEquals("SELECT * WHERE { ?user ?_c1 ?_c2 }");
    }

    @Test
    public void Group_By_Having() throws Exception{
        query.from(user);
        query.groupBy(user.getString("firstName"));
        query.having(user.getString("lastName").isNotNull());
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_firstName ; ?_c6 ?user_lastName } GROUP BY ?user_firstName HAVING (bound(?user_lastName))");
    }

    @Test
    public void Id_Eq_Constant() throws Exception{
        query.from(user);
        query.where(user.get("id").eq(new UID(TEST.NS)));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 }");
    }

    @Test
    public void Id_Ne_Constant() throws Exception{
        query.from(user);
        query.where(user.get("id").ne(new UID(TEST.NS)));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 . FILTER(?user != ?_c4) }");
    }

    @Test
    public void In_Entities() throws Exception{
        query.from(user);
        query.where(user.in(createUser(), createUser(), createUser()));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 . FILTER(?user = ?_c4 || ?user = ?_c5 || ?user = ?_c6) }");
    }

    @Test
    public void In_Strings() throws Exception{
        query.from(user);
        query.where(user.getString("firstName").in("Dennis", "Bob"));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_firstName . FILTER(?user_firstName = ?_c6 || ?user_firstName = ?_c7) }");
    }

    @Test
    public void InstanceOf() throws Exception{
        query.from(user);
        query.where(user.instanceOf(User.class));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 , ?_c3 }");
    }

    @Test
    public void IsNull() throws Exception{
        query.from(user);
        query.where(user.isNull());
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 . FILTER(!bound(?user)) }");
    }

    @Test
    public void Limit() throws Exception{
        query.from(user);
        query.limit(4);
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 } LIMIT 4");
    }

    @Test
    public void ListAccess() throws Exception{
        query.from(user);
        query.where(user.getList("buddyList", User.class).get(0).getString("name").eq("XXX"));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_buddyList . ?user_buddyList ?_c6 ?_var_a . ?_var_a ?_c8 ?user_buddyList_0_name }");
    }

    @Test
    public void ListAccess2() throws Exception{
        query.from(user);
        query.where(user.getList("buddyList", User.class).get(1).getString("name").eq("XXX"));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_buddyList . ?user_buddyList ?_c6 ?_var_a . ?_var_a ?_c8 ?_var_b . ?_var_b ?_c10 ?user_buddyList_1_name }");
    }

    @Test
    public void ListEmpty() throws Exception{
        query.from(user);
        query.where(user.getList("buddyList", User.class).eq(Collections.<User>emptyList()));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_buddyList }");
    }

    @Test
    public void Localized_Map() throws Exception{
        query.from(user);
        query.where(user.getMap("names", Locale.class, String.class).get(new Locale("fi")).eq("XXX"));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_names }");
    }

    @Test
    public void Localized_String_eq_Const() throws Exception{
        query.from(user);
        query.where(user.getString("name").eq("XXX"));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_name }");
    }

    @Test
    public void Localized_String_ne_Const() throws Exception{
        query.from(user);
        query.where(user.getString("name").ne("XXX"));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_name . FILTER(?user_name != ?_c6) }");
    }

    @Test
    public void Map_is_Empty() throws Exception{
        query.from(user);
        query.where(user.getMap("buddiesMapped", String.class, User.class).isEmpty());
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 . OPTIONAL {?user ?_c4 ?user_buddiesMapped } FILTER(!bound(?user_buddiesMapped)) }");
    }

    @Test
    public void Map_is_not_Empty() throws Exception{
        query.from(user);
        query.where(user.getMap("buddiesMapped", String.class, User.class).isNotEmpty());
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 . OPTIONAL {?user ?_c4 ?user_buddiesMapped } FILTER(!(!bound(?user_buddiesMapped))) }");
    }

    @Test
    public void Matches() throws Exception{
        query.from(user);
        query.where(user.getString("firstName").matches("Bob.*"));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_firstName . FILTER(regex(?user_firstName, ?_c6)) }");
    }

    @Test
    public void Offset() throws Exception{
        query.from(user);
        query.offset(4);
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 } OFFSET 4");
    }

    @Test
    public void Or() throws Exception{
        query.from(user);
        query.where(user.getString("firstName").eq("X").or(user.getString("firstName").eq("Y")));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 . OPTIONAL {?user ?_c4 ?user_firstName } FILTER(?user_firstName = ?_c6 || ?user_firstName = ?_c7) }");
    }

    @Test
    public void Order_By() throws Exception{
        query.from(user);
        query.orderBy(user.getString("firstName").asc());
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 . OPTIONAL {?user ?_c4 ?user_firstName } } ORDER BY ?user_firstName");
    }

    @Test
    public void Order_By_Desc() throws Exception{
        query.from(user);
        query.orderBy(user.getString("firstName").desc());
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 . OPTIONAL {?user ?_c4 ?user_firstName } } ORDER BY DESC(?user_firstName)");
    }

    @Test
    public void Set_is_Empty() throws Exception{
        query.from(user);
        query.where(user.getSet("buddies", User.class).isEmpty());
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_buddies . FILTER(?user_buddies = ?_c6) }");
    }

    @Test
    public void Set_is_not_Empty() throws Exception{
        query.from(user);
        query.where(user.getSet("buddies", User.class).isNotEmpty());
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_buddies . FILTER(!(?user_buddies = ?_c6)) }");
    }

    @Test
    public void Single_From_is_Preserved() throws Exception {
        query.from(user);
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 }");
    }

    @Test
    public void Single_From_With_Property() throws Exception {
        query.from(user);
        query.where(user.getString("firstName").eq("Bob"));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_firstName }");
    }

    @Test
    public void Starts_With() throws Exception{
        query.from(user);
        query.where(user.getString("firstName").startsWith("Bob"));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_firstName . FILTER(regex(?user_firstName, '^Bob')) }");
    }

    @Test
    public void String_Contains() throws Exception{
        query.from(user);
        query.where(user.getString("firstName").contains("Bob"));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 ; ?_c4 ?user_firstName . FILTER(regex(?user_firstName, '.*Bob.*')) }");
    }

    @Test
    public void SubQuery_Exists() throws Exception{
        query.from(user);
        query.where(new BeanSubQuery().from(user2).where(user2.get("firstName").eq(user.get("firstName"))).exists());
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 . FILTER(exists {{ ?user2 ?_c2 ?_c3 . ?user ?_c5 ?user_firstName . ?user2 ?_c5 ?user_firstName } }) }");
    }

    @Test
    @Ignore // FIXME
    public void Template_Expression() throws Exception {
        query.from(user);
        query.where(BooleanTemplate.create("!bound({0})", user));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 .  }");
    }
    
    @Test
    public void Two_Froms_are_Preserved() throws Exception {
        query.from(user, user2);
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 . ?user2 ?_c2 ?_c3 }");
    }

    @Test
    public void Two_Froms_One_Filter() throws Exception {
        query.from(user, user2);
        query.where(user.eq(user2));
        assertEquals("SELECT * WHERE { ?user ?_c2 ?_c3 . ?user2 ?_c2 ?_c3 . FILTER(?user = ?user2) }");
    }


    private void assertEquals(String query) throws Exception{
        RDFQueryImpl rdfQuery = builder.build(false);
        Method method = RDFQueryImpl.class.getDeclaredMethod("aggregateFilters");
        method.setAccessible(true);
        method.invoke(rdfQuery);

        QueryMetadata metadata = rdfQuery.getMetadata();

        // tuple query
        SPARQLVisitor visitor = new SPARQLVisitor();
        visitor.visit(metadata, QueryLanguage.TUPLE);
        Assert.assertEquals(query, visitor.toString().replaceAll("\\s+", " ").trim());

        // boolean query
        if (!query.contains("DISTINCT")){
            visitor = new SPARQLVisitor();
            visitor.visit(metadata, QueryLanguage.BOOLEAN);
            Assert.assertEquals(query.replaceAll("SELECT . WHERE", "ASK"), visitor.toString().replaceAll("\\s+", " ").trim());
        }

//        for (Map.Entry<Object, String> entry : visitor.getConstantToLabel().entrySet()){
//            System.err.println(entry.getValue() + " = " + entry.getKey());
//        }
//        System.out.println();
    }
}
