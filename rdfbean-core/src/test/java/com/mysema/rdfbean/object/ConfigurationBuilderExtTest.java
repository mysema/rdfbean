package com.mysema.rdfbean.object;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import com.mysema.query.types.path.PathBuilder;
import com.mysema.rdfbean.model.MiniRepository;

public class ConfigurationBuilderExtTest {
    
    public static class Category extends Identifiable {
        
        String name;
        
        List<Product> products = new ArrayList<Product>();
        
    }
    
    public static class Identifiable {
        
        String id;
        
    }
    
    public static class Product extends Identifiable {

        String name;
        
        double price;
        
        String description;

    }
    
    public enum Profile {
        ADMIN,        
        USER        
    }
    
    public static class User extends Identifiable {
        
        String username;
        
        String password; 
        
        String firstName;
        
        String lastName;
        
        Profile profile = Profile.USER;
        
    }

    @Test
    public void test() throws IOException{
        ConfigurationBuilder builder = new ConfigurationBuilder();        
        builder.addClass(Identifiable.class).addId("id").addProperties();
        builder.addClass(Category.class).addProperties();
        builder.addClass(Product.class).addProperties();
        builder.addClass(Profile.class).addProperties();
        builder.addClass(User.class).addProperties();
        Configuration configuration = builder.build();
        
        Session session = SessionUtil.openSession(new MiniRepository(), Collections.<Locale>emptySet(), configuration);        
        Product product = new Product();
        Category category = new Category();
        category.products.add(product);
        session.save(product);
        session.save(category);
        session.save(new User());
        
        // findInstances
        session.findInstances(Category.class);
        session.findInstances(Identifiable.class);
        session.findInstances(Product.class);
        session.findInstances(User.class);
        
        // list
        PathBuilder<Category> _category = new PathBuilder<Category>(Category.class,"category"); 
        PathBuilder<Identifiable> _identifiable = new PathBuilder<Identifiable>(Identifiable.class,"identifiable");
        PathBuilder<Product> _product = new PathBuilder<Product>(Product.class,"product");
        PathBuilder<User> _user = new PathBuilder<User>(User.class,"user");
        session.from(_category).list(_category);
        session.from(_identifiable).list(_identifiable);
        session.from(_product).list(_product);
        session.from(_user).list(_user);
        
        session.close();
    }
}
