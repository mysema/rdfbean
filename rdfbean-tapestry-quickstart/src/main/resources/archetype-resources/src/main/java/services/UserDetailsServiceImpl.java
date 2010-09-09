package ${package}.services;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

import ${package}.domain.User;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Inject
    private final UserDAO userDAO;

    public UserDetailsServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userDAO.getByUsername(username);
        if (user != null) {
            return new UserDetailsImpl(user.getUsername(), user.getPassword(), user.getProfile()
                    .getAuthorities());
        }
        throw new UsernameNotFoundException("User " + username + " not found");
    }

}
