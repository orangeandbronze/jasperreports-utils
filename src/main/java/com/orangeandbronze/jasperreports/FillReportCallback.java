package com.orangeandbronze.jasperreports;

import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.ReportFiller;

public interface FillReportCallback {

	JasperPrint fillReport(
			ReportFiller reportFiller,
			Map<String, Object> parameters) throws JRException;

}
