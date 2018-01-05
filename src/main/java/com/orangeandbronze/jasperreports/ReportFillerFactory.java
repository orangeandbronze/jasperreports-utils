package com.orangeandbronze.jasperreports;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.ReportFiller;

public interface ReportFillerFactory {

	ReportFiller createFiller(JasperReport report) throws JRException;

}
