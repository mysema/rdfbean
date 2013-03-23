/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.virtuoso.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.query.types.EntityPath;
import com.mysema.rdfbean.domains.UserDepartmentCompanyDomain;
import com.mysema.rdfbean.domains.UserDepartmentCompanyDomain.Company;
import com.mysema.rdfbean.domains.UserDepartmentCompanyDomain.Department;
import com.mysema.rdfbean.domains.UserDepartmentCompanyDomain.User;
import com.mysema.rdfbean.object.BeanQuery;
import com.mysema.rdfbean.testutil.SessionConfig;
import com.mysema.rdfbean.virtuoso.AbstractConnectionTest;

@SessionConfig({ User.class, Department.class, Company.class })
public class JoinsTest extends AbstractConnectionTest implements UserDepartmentCompanyDomain {

    private User u = Alias.alias(User.class);

    private User sample;

    @Before
    public void setUp() {
        sample = new User();
        sample.userName = "Bobby";
        session.save(sample);
    }

    @Test
    public void ResultSetAssertions() {
        assertEquals(1l, from($(u)).count());
        assertEquals(1l, from($(u)).list($(u.getUserName())).size());

        // where 1
        assertEquals(1l, from($(u)).where($(u).eq(sample)).count());
        assertEquals(0l, from($(u)).where($(u).ne(sample)).count());
        assertEquals(1, from($(u)).where($(u).eq(sample)).list($(u)).size());
        assertEquals(0, from($(u)).where($(u).ne(sample)).list($(u)).size());

        // where 2
        assertEquals(1l, from($(u)).where($(u.getUserName()).eq("Bobby")).count());
        assertEquals(0l, from($(u)).where($(u.getUserName()).ne("Bobby")).count());
        assertEquals(1, from($(u)).where($(u.getUserName()).eq("Bobby")).list($(u)).size());
        assertEquals(0, from($(u)).where($(u.getUserName()).ne("Bobby")).list($(u)).size());
    }

    private BeanQuery from(EntityPath<?> entity) {
        return session.from(entity);
    }

}
