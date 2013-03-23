/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.virtuoso.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.UserDepartmentCompanyDomain;
import com.mysema.rdfbean.domains.UserDepartmentCompanyDomain.Company;
import com.mysema.rdfbean.domains.UserDepartmentCompanyDomain.Department;
import com.mysema.rdfbean.domains.UserDepartmentCompanyDomain.User;
import com.mysema.rdfbean.testutil.SessionConfig;
import com.mysema.rdfbean.virtuoso.AbstractConnectionTest;

@SessionConfig({ User.class, Department.class, Company.class })
public class MultiSourcesTest extends AbstractConnectionTest implements UserDepartmentCompanyDomain {

    @Before
    public void setUp() {
        for (String username : Arrays.asList("X", "X", "Y")) {
            User user = new User();
            user.userName = username;
            session.save(user);
        }
        session.clear();
    }

    @Test
    public void test() {
        User u = Alias.alias(User.class, "u");
        User u2 = Alias.alias(User.class, "u2");
        assertEquals(6, session.from($(u), $(u2)).where($(u).ne($(u2))).count());
        assertEquals(2, session.from($(u), $(u2)).where($(u).ne($(u2)), $(u.getUserName()).eq($(u2.getUserName()))).count());
        assertEquals(2, session.from($(u), $(u2)).where($(u).ne($(u2)), $(u.getUserName()).eq($(u2.getUserName()))).list($(u), $(u2)).size());
    }

}
