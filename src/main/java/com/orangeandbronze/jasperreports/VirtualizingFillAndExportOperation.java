package com.orangeandbronze.jasperreports;

import java.io.OutputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRVirtualizer;
import net.sf.jasperreports.engine.JasperReport;

/**
 * Decorates a {@link FillAndExportOperation} with a {@link JRVirtualizer}
 * to prevent out-of-memory errors when generating large reports. This defaults to
 * using a {@link SwapFileVirtualizerFactory}.
 * 
 * @see #setReportVirtualizerFactory(ReportVirtualizerFactory)
 */
public class VirtualizingFillAndExportOperation implements FillAndExportOperation {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final FillAndExportOperation delegate;
	private ReportVirtualizerFactory reportVirtualizerFactory =
			new SwapFileVirtualizerFactory();

	public VirtualizingFillAndExportOperation(FillAndExportOperation delegate) {
		if (delegate == null) {
			throw new IllegalArgumentException("Delegate cannot be null");
		}
		this.delegate = delegate;
	}

	public ReportVirtualizerFactory getReportVirtualizerFactory() {
		return this.reportVirtualizerFactory;
	}

	public void setReportVirtualizerFactory(
			ReportVirtualizerFactory reportVirtualizerFactory) {
		this.reportVirtualizerFactory = reportVirtualizerFactory;
	}

	protected JRVirtualizer addVirtualizerIfNone(Map<String, Object> parameters) {
		JRVirtualizer virtualizer = null;
		if (!parameters.containsKey(JRParameter.REPORT_VIRTUALIZER)) {
			virtualizer = reportVirtualizerFactory.getVirtualizer();
			if (virtualizer != null) {
				parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
			}
		}
		return virtualizer;
	}

	protected void cleanUpVirtualizer(
			JRVirtualizer virtualizer, Map<String, Object> parameters) {
		if (virtualizer != null) {
			// Only clean-up the one we added
			logger.debug("Cleaning up virualizer");
			virtualizer.cleanup();
			logger.debug("Done cleaning up virualizer");
			// Remove virtualizer if it was added
			parameters.remove(JRParameter.REPORT_VIRTUALIZER);
		}
	}

	@Override
	public final void fillAndExportReport(
			JasperReport report,
			Map<String, Object> parameters,
			FillReportCallback callback,
			OutputStream output) throws JRException {
		JRVirtualizer virtualizer = addVirtualizerIfNone(parameters);
		try {
			delegate.fillAndExportReport(
					report, parameters, callback, output);
		} finally {
			// Clean-up *after* report is exported
			cleanUpVirtualizer(virtualizer, parameters);
		}
	}

}
