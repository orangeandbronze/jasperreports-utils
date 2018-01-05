package com.orangeandbronze.jasperreports;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.fill.CompositeFillListener;
import net.sf.jasperreports.engine.fill.FillListener;
import net.sf.jasperreports.engine.fill.JRFiller;
import net.sf.jasperreports.engine.fill.ReportFiller;

public class DefaultReportFillerFactory implements ReportFillerFactory {

	private JasperReportsContext jasperReportsContext =
			DefaultJasperReportsContext.getInstance();
	private FillListener fillListeners;

	public JasperReportsContext getJasperReportsContext() {
		return jasperReportsContext;
	}

	public void setJasperReportsContext(JasperReportsContext jasperReportsContext) {
		this.jasperReportsContext = jasperReportsContext;
	}

	public FillListener getFillListeners() {
		return fillListeners;
	}

	public void setFillListeners(FillListener... fillListeners) {
		for (FillListener fillListener : fillListeners) {
			addFillListener(fillListener);
		}
	}

	public void addFillListener(FillListener fillListener) {
		this.fillListeners = CompositeFillListener.addListener(
				this.fillListeners, fillListener);
	}

	@Override
	public ReportFiller createFiller(JasperReport report) throws JRException {
		ReportFiller filler = JRFiller.createReportFiller(
				jasperReportsContext, report);
		if (fillListeners != null) {
			filler.addFillListener(fillListeners);
		}
		return filler;
	}

}
