package ${package}.services;

import org.springframework.transaction.annotation.Transactional;

import com.mysema.rdfbean.dao.Repository;
import ${package}.domain.User;

@Transactional
public interface UserDAO extends Repository<User,String>{

    User getByUsername(String shortName);

}
