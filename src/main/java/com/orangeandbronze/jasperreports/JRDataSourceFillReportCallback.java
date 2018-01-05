package com.orangeandbronze.jasperreports;

import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRFillInterruptedException;
import net.sf.jasperreports.engine.fill.JRFiller;
import net.sf.jasperreports.engine.fill.ReportFiller;

public class JRDataSourceFillReportCallback implements FillReportCallback {
	
	private final JRDataSource dataSource;

	public JRDataSourceFillReportCallback(JRDataSource dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException(
					"JRDataSource cannot be null");
		}
		this.dataSource = dataSource;
	}

	@Override
	public JasperPrint fillReport(
			ReportFiller reportFiller,
			Map<String, Object> parameters) throws JRException {
		// This try-catch block mimics JRFiller.fill() methods
		// which wrap a JRFillInterruptedException inside
		// a JRException.
		try {
			// ((BaseReportFiller) reportFiller).getMainDataSet()
			//         .setDatasourceParameterValue(parameters, jrDataSource);
			return reportFiller.fill(parameters, dataSource);
		} catch (JRFillInterruptedException e) {
			throw new JRException(
					JRFiller.EXCEPTION_MESSAGE_KEY_THREAD_INTERRUPTED,
					null, e);
		}
	}

}
