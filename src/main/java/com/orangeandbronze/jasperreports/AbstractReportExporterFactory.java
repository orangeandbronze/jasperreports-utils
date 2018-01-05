package com.orangeandbronze.jasperreports;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.export.JRExportProgressMonitor;

public abstract class AbstractReportExporterFactory implements ReportExporterFactory {

	protected JasperReportsContext jasperReportsContext =
			DefaultJasperReportsContext.getInstance();
	protected JRExportProgressMonitor progressMonitor;

	public AbstractReportExporterFactory() {
		super();
	}

	public JasperReportsContext getJasperReportsContext() {
		return jasperReportsContext;
	}

	public void setJasperReportsContext(JasperReportsContext jasperReportsContext) {
		this.jasperReportsContext = jasperReportsContext;
	}

	public JRExportProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	public void setProgressMonitor(JRExportProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
	}

}