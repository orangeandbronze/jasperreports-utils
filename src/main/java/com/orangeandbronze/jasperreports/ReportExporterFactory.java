package com.orangeandbronze.jasperreports;

import java.io.OutputStream;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.export.Exporter;

public interface ReportExporterFactory {

	@SuppressWarnings("rawtypes")
	Exporter createExporter(
			JasperPrint filledReport, OutputStream output) throws JRException;

}
