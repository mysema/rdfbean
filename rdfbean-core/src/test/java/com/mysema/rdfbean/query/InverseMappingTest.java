/*
 * Copyright (c) 2010 Mysema Ltd.
 * All rights reserved.
 *
 */
package com.mysema.rdfbean.query;

import static com.mysema.query.alias.Alias.$;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import com.mysema.query.alias.Alias;
import com.mysema.rdfbean.domains.CompanyDepartmentEmployeeDomain;
import com.mysema.rdfbean.domains.CompanyDepartmentEmployeeDomain.Company;
import com.mysema.rdfbean.domains.CompanyDepartmentEmployeeDomain.Department;
import com.mysema.rdfbean.domains.CompanyDepartmentEmployeeDomain.Employee;
import com.mysema.rdfbean.domains.SongPlaybackMusicStore.MusicStore;
import com.mysema.rdfbean.domains.SongPlaybackMusicStore.Song;
import com.mysema.rdfbean.domains.SongPlaybackMusicStore.SongPlayback;
import com.mysema.rdfbean.model.LID;
import com.mysema.rdfbean.model.MiniConnection;
import com.mysema.rdfbean.model.UID;
import com.mysema.rdfbean.testutil.SessionConfig;

@SessionConfig({ Company.class, Department.class, Employee.class})
public class InverseMappingTest extends SessionTestBase implements CompanyDepartmentEmployeeDomain {

    @Test
    public void test() {
        Company company = new Company();
        Department department = new Department();
        Employee employee = new Employee();
        employee.department = department;
        department.company = company;

        session.save(company);
        session.save(department);
        session.save(employee);
        session.flush();
        session.clear();

        Company c = Alias.alias(Company.class);
        Department d = Alias.alias(Department.class);
        Employee e = Alias.alias(Employee.class);

        // count instances
        assertEquals(1l, session.from($(c)).count());
        assertEquals(1l, session.from($(d)).count());
        assertEquals(1l, session.from($(e)).count());

        // direct
        assertEquals(1l, session.from($(d)).where($(d.getCompany()).eq(company)).count());
        assertEquals(1l, session.from($(d)).where($(d.getCompany()).eq(company)).count());
        assertEquals(1l, session.from($(e)).where($(e.getDepartment()).eq(department)).count());

        // inverse
        assertEquals(1l, session.from($(c)).where($(c.getDepartment()).eq(department)).count());
        assertEquals(1l, session.from($(c)).where($(c.getDepartments()).contains(department)).count());
        assertEquals(1l, session.from($(d)).where($(d.getEmployees()).contains(employee)).count());

    }

    @Test
    @Ignore // inverse properties are not stored
    public void testInverseIDMapping() {
        Song song = new Song();
        MusicStore musicStore = new MusicStore();
        SongPlayback songPlayback = new SongPlayback();

        LID songLID = session.save(song);
        LID musicStoreLID = session.save(musicStore);
        session.flush();
        song = session.get(Song.class, songLID);
        musicStore = session.get(MusicStore.class, musicStoreLID);
        session.clear();

        songPlayback.datetime = new DateTime();
        songPlayback.song = song.id;
        songPlayback.store = musicStore.id;
        session.save(songPlayback);
        session.flush();
        session.clear();

        MiniConnection conn = repository.openConnection();
        assertTrue(conn.exists(null, new UID("http://www.foo.com#musicStore"), null, null, true));
        assertTrue(conn.exists(null, new UID("http://www.foo.com#playback"), null, null, true));
        conn.close();
    }
}
