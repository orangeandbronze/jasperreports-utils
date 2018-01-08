package com.orangeandbronze.jasperreports.spring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.StringUtils;

import com.orangeandbronze.jasperreports.FillReportCallback;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFillInterruptedException;
import net.sf.jasperreports.engine.fill.JRFiller;
import net.sf.jasperreports.engine.fill.ReportFiller;
import net.sf.jasperreports.engine.query.JRJdbcQueryExecuter;

public class SpringJdbcFillReportCallback<T> implements FillReportCallback {

	private final DataSource dataSource;
	private final String sql;
	private final RowMapper<T> rowMapper;
	private final Object args[];

	public SpringJdbcFillReportCallback(
			DataSource dataSource,
			String sql, RowMapper<T> rowMapper, Object... args) {
		super();
		if (dataSource == null) {
			throw new IllegalArgumentException(
					"DataSource cannot be null");
		}
		if (StringUtils.isEmpty(sql)) {
			throw new IllegalArgumentException(
					"SQL query cannot be null or empty");
		}
		if (rowMapper == null) {
			throw new IllegalArgumentException(
					"RowMapper cannot be null");
		}
		this.dataSource = dataSource;
		this.sql = sql;
		this.rowMapper = rowMapper;
		this.args = args;
	}

	@Override
	public final JasperPrint fillReport(
			ReportFiller reportFiller,
			Map<String, Object> parameters) throws JRException {
		// This try-catch block mimics JRFiller.fill() methods
		// which wrap a JRFillInterruptedException inside
		// a JRException.
		try {
			Connection connection = DataSourceUtils.getConnection(dataSource);
			try {
				return doInConnection(reportFiller, parameters, connection);
			} finally {
				DataSourceUtils.releaseConnection(connection, dataSource);
			}
		} catch (CannotGetJdbcConnectionException cgjce) {
			throw new JRException(cgjce);
		} catch (JRFillInterruptedException e) {
			throw new JRException(
					JRFiller.EXCEPTION_MESSAGE_KEY_THREAD_INTERRUPTED,
					null, e);
		}
	}

	protected JasperPrint doInConnection(
			ReportFiller reportFiller,
			Map<String, Object> parameters,
			Connection connection) throws JRException {
		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			try {
				return doInPreparedStatement(reportFiller, parameters, ps);
			} finally {
				JdbcUtils.closeStatement(ps);
			}
		} catch (SQLException e) {
			throw new JRException(
					JRJdbcQueryExecuter.EXCEPTION_MESSAGE_KEY_QUERY_STATEMENT_PREPARE_ERROR,
					new Object[] { sql }, e);
		}
	}

	protected JasperPrint doInPreparedStatement(
			ReportFiller reportFiller,
			Map<String, Object> parameters,
			PreparedStatement ps) throws JRException {
		try {
			ArgumentPreparedStatementSetter pss =
					new ArgumentPreparedStatementSetter(args);
			pss.setValues(ps);
			ResultSet rs = ps.executeQuery();
			try {
				// This way, one row will be loaded and mapped at a time.
				// The entire data set will *not* be loaded all at once.
				JRDataSource jrDataSource =
						new JRRowMappingDataSource<>(rs, rowMapper);
				return reportFiller.fill(parameters, jrDataSource);
			} finally {
				JdbcUtils.closeResultSet(rs);
				pss.cleanupParameters();
			}
		} catch (SQLException e) {
			throw new JRException(
					JRJdbcQueryExecuter.EXCEPTION_MESSAGE_KEY_QUERY_STATEMENT_EXECUTE_ERROR,
					null, e);
		}
	}

}
