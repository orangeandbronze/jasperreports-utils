package com.orangeandbronze.jasperreports.spring;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.ResultSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JRDesignField;

public class JRRowMappingDataSourceTests {

	private ResultSet resultSet;
	private RowMapper<Person> rowMapper;
	private JRDataSource jrDataSource;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		resultSet = mock(ResultSet.class);
		rowMapper = mock(RowMapper.class);
		jrDataSource = new JRRowMappingDataSource<>(resultSet, rowMapper);

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void delegatesToResultSet() throws Exception {
		when(resultSet.next()).thenReturn(false);

		assertFalse(jrDataSource.next());
		verify(resultSet).next();
		verifyZeroInteractions(rowMapper);
	}

	@Test
	public void delegatesToRowMapper() throws Exception {
		when(resultSet.next()).thenReturn(true);

		jrDataSource.next();
		verify(resultSet).next();
		verify(rowMapper).mapRow(resultSet, 0);
	}

	@Test
	public void returnFieldValue() throws Exception {
		when(resultSet.next()).thenReturn(true);

		Person person = new Person();
		person.setId(42);
		person.setFirstName("John");
		person.setLastName("Dough");
		person.setAge(24);

		when(rowMapper.mapRow(resultSet, 0)).thenReturn(person);

		jrDataSource.next();

		assertFieldEquals(
				person.getId(), "id", Integer.class, jrDataSource);
		assertFieldEquals(
				person.getFirstName(), "firstName", String.class, jrDataSource);
		assertFieldEquals(
				person.getLastName(), "lastName", String.class, jrDataSource);
		assertFieldEquals(
				person.getAge(), "age", Integer.class, jrDataSource);
	}

	private static void assertFieldEquals(
			Object expected,
			String propertyName,
			Class<?> clazz,
			JRDataSource jrDataSource) throws JRException {
		JRDesignField field = new JRDesignField();
		field.setName(propertyName);
		field.setValueClass(clazz);
		Object actual = jrDataSource.getFieldValue(field);
		assertEquals(expected, actual);
	}

}
