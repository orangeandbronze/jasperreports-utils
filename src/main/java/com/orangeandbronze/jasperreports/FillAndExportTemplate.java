package com.orangeandbronze.jasperreports;

import java.io.OutputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.fill.ReportFiller;
import net.sf.jasperreports.export.Exporter;

/**
 * @see ReportFillerFactory
 * @see ReportExporterFactory
 */
public class FillAndExportTemplate implements FillAndExportOperation {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ReportFillerFactory fillerFactory;
	private final ReportExporterFactory exporterFactory;

	private JasperReportsContext jasperReportsContext =
			DefaultJasperReportsContext.getInstance();

	public FillAndExportTemplate(
			ReportFillerFactory fillerFactory,
			ReportExporterFactory exporterFactory) {
		if (fillerFactory == null) {
			throw new IllegalArgumentException(
					"ReportFillerFactory cannot be null");
		}
		if (exporterFactory == null) {
			throw new IllegalArgumentException(
					"ReportExportFactory cannot be null");
		}
		this.fillerFactory = fillerFactory;
		this.exporterFactory = exporterFactory;
	}

	protected ReportFiller createReportFiller(
			JasperReport report) throws JRException {
		return fillerFactory.createFiller(report);
	}

	@SuppressWarnings("rawtypes")
	protected Exporter createReportExporter(
			JasperPrint filledReport, OutputStream output) throws JRException {
		return exporterFactory.createExporter(filledReport, output);
	}

	public JasperReportsContext getJasperReportsContext() {
		return jasperReportsContext;
	}

	public void setJasperReportsContext(JasperReportsContext jasperReportsContext) {
		this.jasperReportsContext = jasperReportsContext;
	}

	@SuppressWarnings("rawtypes")
	public final void fillAndExportReport(
			JasperReport report,
			Map<String, Object> parameters,
			FillReportCallback callback,
			OutputStream output) throws JRException {
		// 1a. Create ReportFiller
		ReportFiller reportFiller = createReportFiller(report);
		// 1b. Fill report
		logger.debug("Filling report");
		JasperPrint filledReport = callback.fillReport(reportFiller, parameters);
		logger.debug("Done filling report");

		// 2a. Create Exporter
		Exporter exporter = createReportExporter(filledReport, output);
		// 2b. Export report
		logger.debug("Exporting report");
		exporter.exportReport();
		logger.debug("Done exporting report");
	}

}
