package ${package}.services;

import static ${package}.domain.QUser.*;

import org.apache.tapestry5.ioc.annotations.Inject;

import com.example.app.domain.User;
import com.mysema.rdfbean.object.SessionFactory;

import ${package}.domain.User;

public class UserDAOImpl extends AbstractDAO<User> implements UserDAO {

    public UserDAOImpl(@Inject SessionFactory sessionFactory) {
        super(sessionFactory, user);
    }

    @Override
    public User getByUsername(String username) {
        return getSession().from(user).where(user.username.eq(username)).uniqueResult(user);
    }

}
