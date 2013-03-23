package com.mysema.rdfbean.query;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import com.mysema.query.types.path.PathBuilder;
import com.mysema.rdfbean.object.Configuration;
import com.mysema.rdfbean.object.ConfigurationBuilder;
import com.mysema.rdfbean.object.Session;
import com.mysema.rdfbean.object.SessionUtil;

public class ConfigurationBuilderTest extends SessionTestBase {

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
    public void test() throws IOException {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addClass(Identifiable.class).addId("id").addProperties();
        builder.addClass(Category.class).addProperties();
        builder.addClass(Product.class).addProperties();
        builder.addClass(Profile.class).addProperties();
        builder.addClass(User.class).addProperties();
        Configuration configuration = builder.build();

        Session session = SessionUtil.openSession(repository, Collections.<Locale> emptySet(), configuration);

        Product product = new Product();
        product.name = "XXX";
        Category category = new Category();
        category.name = "XXX";
        category.products.add(product);
        session.save(product);
        session.save(category);
        session.save(new User());
        session.clear();

        // list
        PathBuilder<Category> _category = new PathBuilder<Category>(Category.class, "category");
        PathBuilder<Identifiable> _identifiable = new PathBuilder<Identifiable>(Identifiable.class, "identifiable");
        PathBuilder<Product> _product = new PathBuilder<Product>(Product.class, "product");
        PathBuilder<User> _user = new PathBuilder<User>(User.class, "user");
        assertEquals(1, session.from(_category).list(_category).size());
        assertEquals(3, session.from(_identifiable).list(_identifiable).size());
        assertEquals(1, session.from(_product).list(_product).size());
        assertEquals(1, session.from(_user).list(_user).size());

        assertEquals("XXX", session.from(_category).list(_category).get(0).name);
        assertEquals("XXX", session.from(_product).list(_product).get(0).name);

        session.close();
    }

}
