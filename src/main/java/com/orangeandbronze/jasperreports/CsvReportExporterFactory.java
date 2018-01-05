package com.orangeandbronze.jasperreports;

import java.io.OutputStream;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.SimpleCsvReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;

public class CsvReportExporterFactory extends AbstractReportExporterFactory {

	@SuppressWarnings("rawtypes")
	@Override
	public Exporter createExporter(
			JasperPrint filledReport, OutputStream output) throws JRException {
		JRCsvExporter exporter = new JRCsvExporter(jasperReportsContext);
		exporter.setExporterInput(new SimpleExporterInput(filledReport));
		exporter.setExporterOutput(new SimpleWriterExporterOutput(output));

		if (progressMonitor != null) {
			SimpleCsvReportConfiguration configuration =
					new SimpleCsvReportConfiguration();
			configuration.setProgressMonitor(progressMonitor);
			exporter.setConfiguration(configuration);
		}

		return exporter;
	}

}
