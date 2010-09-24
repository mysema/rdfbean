package ${package}.services;

import org.springframework.transaction.annotation.Transactional;

import ${package}.domain.User;

@Transactional
public interface UserService {

    User getByUsername(String shortName);

}
