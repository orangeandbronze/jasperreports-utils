package com.orangeandbronze.jasperreports.samples;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.orangeandbronze.jasperreports.CountLimitedDataSource;
import com.orangeandbronze.jasperreports.FillAndExportTemplate;
import com.orangeandbronze.jasperreports.JRDataSourceFillReportCallback;
import com.orangeandbronze.jasperreports.PdfReportRunner;
import com.orangeandbronze.jasperreports.RepeatFirstBeanDataSource;
import com.orangeandbronze.jasperreports.TimeLimitedDataSource;
import com.orangeandbronze.jasperreports.VirtualizingFillAndExportOperation;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.fill.JRFillInterruptedException;
import net.sf.jasperreports.export.ExportInterruptedException;

public class InterruptFillAndExportTests {

	private final long ELEMENTS = 3000; // enough to get to page 20
	private final int SECONDS = 10;

	private PdfReportRunner reportRunner;

	@Before
	public void setUp() throws Exception {
		reportRunner = new PdfReportRunner();
		reportRunner.setReport(createJasperReport());
		reportRunner.setFillAndExportOperation(
				new VirtualizingFillAndExportOperation(
						new FillAndExportTemplate(
								reportRunner.getFillerFactory(),
								reportRunner.getExporterFactory())));
		reportRunner.setReportFillerCallback(
				new JRDataSourceFillReportCallback(createJRDataSource()));
	}

	@After
	public void tearDown() throws Exception {
		reportRunner.close();
	}

	protected JasperReport createJasperReport() throws Exception {
		InputStream reportAsStream = this.getClass().getResourceAsStream(
				"report-jrdatasource.jrxml");
		assertNotNull(reportAsStream);
		return JasperCompileManager.compileReport(reportAsStream);
	}

	protected JRDataSource createJRDataSource() throws Exception {
		Object bean = createBean();
		assertNotNull(bean);
		return new TimeLimitedDataSource(
				new CountLimitedDataSource(
					new RepeatFirstBeanDataSource(
						new JRBeanCollectionDataSource(Arrays.asList(bean))),
					ELEMENTS), SECONDS);
	}

	protected Object createBean() throws Exception {
		Person person = new Person();
		person.setId(42);
		person.setFirstName("John");
		person.setLastName("Dough");
		person.setAge(24);
		return person;
	}

	@Test
	public void fillingInterrupted() throws Exception {
		// Signal us after *filling* page 20
		CountDownLatch signal = reportRunner.signalAfterFillingPage(20);
		Future<?> task = reportRunner.runReportAsync();
		// Wait for signal, then cancel report thread
		assertTrue(signal.await(10, TimeUnit.SECONDS));
		assertTrue(task.cancel(true)); // interrupt report thread
		Thread.sleep(1000); // let report thread return
		assertEquals(1, reportRunner.getExceptions().size());
		Throwable exception = reportRunner.getExceptions().get(0);
		assertTrue(
				exception.getCause() instanceof JRFillInterruptedException
				|| exception.getCause() instanceof InterruptedException);
	}

	@Test
	public void exportingInterrupted() throws Exception {
		// Signal us after *exporting* page 20
		CountDownLatch signal = reportRunner.signalAfterExportingPage(20);
		Future<?> task = reportRunner.runReportAsync();
		// Wait for signal, then cancel report thread
		assertTrue(signal.await(20, TimeUnit.SECONDS));
		assertTrue(task.cancel(true)); // interrupt report thread
		Thread.sleep(1000); // let report thread return
		assertEquals(1, reportRunner.getExceptions().size());
		Throwable exception = reportRunner.getExceptions().get(0);
		assertTrue(
				exception instanceof ExportInterruptedException
				|| exception.getCause() instanceof InterruptedException);
	}

}
