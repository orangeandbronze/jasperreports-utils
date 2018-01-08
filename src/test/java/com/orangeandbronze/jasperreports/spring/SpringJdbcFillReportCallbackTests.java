package com.orangeandbronze.jasperreports.spring;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;

import net.sf.jasperreports.engine.fill.ReportFiller;

public class SpringJdbcFillReportCallbackTests {

	private DataSource dataSource;
	private RowMapper<?> rowMapper;

	@Before
	public void setUp() throws Exception {
		dataSource = mock(DataSource.class);
		rowMapper = mock(RowMapper.class);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void executeSqlQuery() throws Exception {
		String sql = "SELECT * FROM persons";

		SpringJdbcFillReportCallback<?> callback =
				new SpringJdbcFillReportCallback<>(
						dataSource, sql, rowMapper);
		
		Connection connection = mock(Connection.class);
		when(dataSource.getConnection()).thenReturn(connection);
		PreparedStatement ps = mock(PreparedStatement.class);
		when(connection.prepareStatement(sql)).thenReturn(ps);
		when(ps.executeQuery()).thenReturn(mock(ResultSet.class));

		ReportFiller reportFiller = mock(ReportFiller.class);
		Map<String, Object> parameters = new HashMap<>();
		callback.fillReport(reportFiller, parameters);
		
		verify(connection).prepareStatement(eq(sql));
	}

	@Test
	public void setQueryParameters() throws Exception {
		String sql = "SELECT * FROM persons WHERE first_name LIKE ? AND age > ?";

		SpringJdbcFillReportCallback<?> callback =
				new SpringJdbcFillReportCallback<>(
						dataSource, sql, rowMapper, "J%", 18);
		
		Connection connection = mock(Connection.class);
		when(dataSource.getConnection()).thenReturn(connection);
		PreparedStatement ps = mock(PreparedStatement.class);
		when(connection.prepareStatement(sql)).thenReturn(ps);
		when(ps.executeQuery()).thenReturn(mock(ResultSet.class));

		ReportFiller reportFiller = mock(ReportFiller.class);
		Map<String, Object> parameters = new HashMap<>();
		callback.fillReport(reportFiller, parameters);

		verify(ps, times(1)).setString(eq(1), eq("J%"));
		verify(ps, times(1)).setObject(eq(2), eq(18));
	}

	@Test
	public void fillReport() throws Exception {
		String sql = "SELECT * FROM persons";

		SpringJdbcFillReportCallback<?> callback =
				new SpringJdbcFillReportCallback<>(
						dataSource, sql, rowMapper);
		
		Connection connection = mock(Connection.class);
		when(dataSource.getConnection()).thenReturn(connection);
		PreparedStatement ps = mock(PreparedStatement.class);
		when(connection.prepareStatement(sql)).thenReturn(ps);
		when(ps.executeQuery()).thenReturn(mock(ResultSet.class));

		ReportFiller reportFiller = mock(ReportFiller.class);
		Map<String, Object> parameters = new HashMap<>();
		callback.fillReport(reportFiller, parameters);

		verify(reportFiller).fill(
				same(parameters),
				notNull(JRRowMappingDataSource.class));
	}

}
