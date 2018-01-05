package com.orangeandbronze.jasperreports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.FillListener;

public class ReportRunner {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ReportFillerFactory fillerFactory;
	private ReportExporterFactory exporterFactory;
	private FillAndExportOperation fillAndExportOperation;
	private FillReportCallback reportFillerCallback;

	private JasperReport report;
	private Map<String, Object> parameters;

	private CountDownLatch fillSignal;
	
	private List<Throwable> exceptions;

	private ExecutorService pool;

	public ReportRunner() throws Exception {
		initFillerFactory();
		// this.fillAndExportOperation = ...;
		this.parameters = new HashMap<>();
		this.exceptions = new LinkedList<>();
		this.pool = Executors.newSingleThreadExecutor();
	}

	protected void initFillerFactory() throws Exception {
		DefaultReportFillerFactory fillerFactory = new DefaultReportFillerFactory();
		fillerFactory.addFillListener(new FillListener() {
			@Override
			public void pageUpdated(JasperPrint jasperPrint, int pageIndex) {
				// do nothing
			}

			@Override
			public void pageGenerated(JasperPrint jasperPrint, int pageIndex) {
				int page = pageIndex + 1;
				if ((page == 1) || ((page % 10) == 0)) {
					// jasperPrint.getName();
					logger.debug("Generated page: {}", page);
				}
				if (fillSignal != null && fillSignal.getCount() > 0) {
					fillSignal.countDown();
					if (fillSignal.getCount() == 0) {
						// Increase chances of being interrupted at this point
						Thread.yield();
					}
				}
			}
		});
		this.fillerFactory = fillerFactory;
	}

	public CountDownLatch signalAfterFillingPage(int pageNumber) {
		fillSignal = new CountDownLatch(pageNumber);
		return fillSignal;
	}
	
	public void runReport(File outputFile) throws Exception {
		OutputStream output = new FileOutputStream(outputFile);
		try {
			fillAndExportOperation.fillAndExportReport(
					report, parameters, reportFillerCallback, output);
		} catch (Exception e) {
			logger.debug("Exception encountered: {}", e.getMessage());
			logger.trace("", e);
			getExceptions().add(e);
			throw e;
		} finally {
			output.close();
		}
	}

	public Future<File> runReportAsync(File outputFile) throws Exception {
		return pool.submit(() -> {
			runReport(outputFile);
			return outputFile;
		});
	}

	public void runReport() throws Exception {
		File outputFile = File.createTempFile("test", ".pdf");
		try {
			runReport(outputFile);
		} finally {
			outputFile.delete();
		}
	}

	public Future<?> runReportAsync() throws Exception {
		return pool.submit(() -> {
			runReport();
			return null;
		});
	}

	public ReportFillerFactory getFillerFactory() {
		return fillerFactory;
	}

	public void setFillerFactory(ReportFillerFactory fillerFactory) {
		this.fillerFactory = fillerFactory;
	}

	public ReportExporterFactory getExporterFactory() {
		return exporterFactory;
	}

	public void setExporterFactory(ReportExporterFactory exporterFactory) {
		this.exporterFactory = exporterFactory;
	}

	public FillAndExportOperation getFillAndExportOperation() {
		return fillAndExportOperation;
	}

	public void setFillAndExportOperation(FillAndExportOperation fillAndExportOperation) {
		this.fillAndExportOperation = fillAndExportOperation;
	}

	public FillReportCallback getReportFillerCallback() {
		return reportFillerCallback;
	}

	public void setReportFillerCallback(FillReportCallback reportFillerCallback) {
		this.reportFillerCallback = reportFillerCallback;
	}

	public JasperReport getReport() {
		return report;
	}

	public void setReport(JasperReport report) {
		this.report = report;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public List<Throwable> getExceptions() {
		return exceptions;
	}

	public ExecutorService getExecutorService() {
		return pool;
	}
	
	public void close() {
		pool.shutdown();
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
					logger.error("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

}
