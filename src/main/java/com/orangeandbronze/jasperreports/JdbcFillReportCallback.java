package com.orangeandbronze.jasperreports;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFillInterruptedException;
import net.sf.jasperreports.engine.fill.JRFiller;
import net.sf.jasperreports.engine.fill.ReportFiller;

public class JdbcFillReportCallback implements FillReportCallback {

	private final DataSource dataSource;

	public JdbcFillReportCallback(DataSource dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException(
					"DataSource cannot be null");
		}
		this.dataSource = dataSource;
	}

	@Override
	public final JasperPrint fillReport(
			ReportFiller reportFiller,
			Map<String, Object> parameters) throws JRException {
		// This try-catch block mimics JRFiller.fill() methods
		// which wrap a JRFillInterruptedException inside
		// a JRException.
		try {
			try {
				Connection connection = dataSource.getConnection();
				try {
					// ((BaseReportFiller) reportFiller).getMainDataSet()
					//         .setConnectionParameterValue(parameters, connection);
					return reportFiller.fill(parameters, connection);
				} finally {
					connection.close();
				}
			} catch (SQLException e) {
				throw new JRException(e);
			}
		} catch (JRFillInterruptedException e) {
			throw new JRException(
					JRFiller.EXCEPTION_MESSAGE_KEY_THREAD_INTERRUPTED,
					null, e);
		}
	}

}
