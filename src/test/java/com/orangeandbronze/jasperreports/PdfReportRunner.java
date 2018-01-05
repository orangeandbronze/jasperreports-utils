package com.orangeandbronze.jasperreports;

import java.util.concurrent.CountDownLatch;

import net.sf.jasperreports.engine.export.JRExportProgressMonitor;

/**
 * Helper class used in tests.
 */
public class PdfReportRunner extends ReportRunner {

	private CountDownLatch exportSignal;
	
	public PdfReportRunner() throws Exception {
		super();
		initExportFactory();
	}

	protected void initExportFactory() {
		PdfReportExporterFactory exporterFactory = new PdfReportExporterFactory();
		exporterFactory.setProgressMonitor(new JRExportProgressMonitor() {
			private int page = 0;
			@Override
			public void afterPageExport() {
				page++;
				if ((page == 1) || ((page % 10) == 0)) {
					logger.debug("Exported page: {}", page);
				}
				if (exportSignal != null && exportSignal.getCount() > 0) {
					exportSignal.countDown();
					if (exportSignal.getCount() == 0) {
						// Increase chances of being interrupted at this point
						Thread.yield();
					}
				}
			}
		});
		setExporterFactory(exporterFactory);
	}

	public CountDownLatch signalAfterExportingPage(int pageNumber) {
		exportSignal = new CountDownLatch(pageNumber);
		return exportSignal;
	}

}
