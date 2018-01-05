package com.orangeandbronze.jasperreports;

import java.io.OutputStream;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;

public class PdfReportExporterFactory extends AbstractReportExporterFactory {

	@SuppressWarnings("rawtypes")
	@Override
	public Exporter createExporter(
			JasperPrint filledReport, OutputStream output) throws JRException {
		JRPdfExporter exporter = new JRPdfExporter(jasperReportsContext);
		exporter.setExporterInput(new SimpleExporterInput(filledReport));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(output));

		if (progressMonitor != null) {
			SimplePdfReportConfiguration configuration =
					new SimplePdfReportConfiguration();
			configuration.setProgressMonitor(progressMonitor);
			exporter.setConfiguration(configuration);
		}

		return exporter;
	}

}
