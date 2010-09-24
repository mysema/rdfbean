package ${package}.services;

import static ${package}.domain.QUser.*;

import org.apache.tapestry5.ioc.annotations.Inject;

import com.mysema.rdfbean.object.SessionFactory;

import ${package}.domain.User;

public class UserServiceImpl extends AbstractService implements UserService {

    public UserServiceImpl(@Inject SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public User getByUsername(String username) {
        return getSession().from(user).where(user.username.eq(username)).uniqueResult(user);
    }

}
