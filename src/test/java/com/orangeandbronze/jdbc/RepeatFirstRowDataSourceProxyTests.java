package com.orangeandbronze.jdbc;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ClassUtils;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class RepeatFirstRowDataSourceProxyTests {

	@Configuration
	static class Config {
		@Bean
		DataSource dataSource() {
			String thisPackage =
					ClassUtils.classPackageAsResourcePath(this.getClass());
			return new EmbeddedDatabaseBuilder()
					.setType(EmbeddedDatabaseType.HSQL)
					.addScript("classpath:" + thisPackage + "/schema-hsqldb.sql")
					.addScript("classpath:" + thisPackage + "/data.sql")
					.build();
		}
	}

	@Autowired
	private DataSource dataSource;

	@Before
	public void setUp() throws Exception {
		dataSource = new RepeatFirstRowDataSourceProxy(dataSource);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void fromStatement() throws Exception {
		Connection connection = dataSource.getConnection();
		try {
			Statement statement = connection.createStatement();
			try {
				ResultSet rs = statement.executeQuery("SELECT * FROM persons");
				try {
					assertTrue("First row", rs.next());
					Person person = mapRow(rs);
					assertTrue("Second row", rs.next());
					assertEquals(mapRow(rs), person);
					assertTrue("Third row", rs.next());
					assertEquals(mapRow(rs), person);
					assertTrue("Fourth row", rs.next());
					assertEquals(mapRow(rs), person);
					assertTrue("Fifth row", rs.next());
					assertEquals(mapRow(rs), person);
				} finally {
					rs.close();
				}
			} finally {
				statement.close();
			}
		} finally {
			connection.close();
		}
	}

	@Test
	public void fromPreparedStatement() throws Exception {
		Connection connection = dataSource.getConnection();
		try {
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM persons");
			try {
				ResultSet rs = ps.executeQuery();
				try {
					assertTrue("First row", rs.next());
					Person person = mapRow(rs);
					assertTrue("Second row", rs.next());
					assertEquals(mapRow(rs), person);
					assertTrue("Third row", rs.next());
					assertEquals(mapRow(rs), person);
					assertTrue("Fourth row", rs.next());
					assertEquals(mapRow(rs), person);
					assertTrue("Fifth row", rs.next());
					assertEquals(mapRow(rs), person);
				} finally {
					rs.close();
				}
			} finally {
				ps.close();
			}
		} finally {
			connection.close();
		}
	}

	private Person mapRow(ResultSet rs) throws Exception {
		Person person = new Person();
		person.setId(rs.getInt("id"));
		person.setFirstName(rs.getString("first_name"));
		person.setLastName(rs.getString("last_name"));
		person.setAge(rs.getInt("age"));
		return person;
	}

}
