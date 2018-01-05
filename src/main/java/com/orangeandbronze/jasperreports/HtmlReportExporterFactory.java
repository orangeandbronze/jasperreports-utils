package com.orangeandbronze.jasperreports;

import java.io.OutputStream;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleHtmlReportConfiguration;

public class HtmlReportExporterFactory extends AbstractReportExporterFactory {

	@SuppressWarnings("rawtypes")
	@Override
	public Exporter createExporter(
			JasperPrint filledReport, OutputStream output) throws JRException {
		HtmlExporter exporter = new HtmlExporter(jasperReportsContext);
		exporter.setExporterInput(new SimpleExporterInput(filledReport));
		exporter.setExporterOutput(new SimpleHtmlExporterOutput(output));

		if (progressMonitor != null) {
			SimpleHtmlReportConfiguration configuration =
					new SimpleHtmlReportConfiguration();
			configuration.setProgressMonitor(progressMonitor);
			exporter.setConfiguration(configuration);
		}

		return exporter;
	}

}
