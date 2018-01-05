package com.orangeandbronze.jasperreports.samples;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * For best results, this needs to be run with limited memory (i.e.
 * <code>-Xmx16m</code>). This test uses a {@link RepeatFirstBeanDataSource}
 * that repeats until {@link #ELEMENTS} or {@link #SECONDS} are reached.
 * <p>
 * See <a href=
 * "http://jasperreports.sourceforge.net/sample.reference/virtualizer/index.html">Virtualizer
 * Sample - JasperReports</a>
 * </p>
 */
public class SampleLargeBeanCollectionTests {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final long ELEMENTS = 15000;
	private final int SECONDS = 15;

	private PdfReportRunner reportRunner;

	@Before
	public void setUp() throws Exception {
		reportRunner = new PdfReportRunner();
		reportRunner.setReport(createJasperReport());
		reportRunner.setFillAndExportOperation(
				new FillAndExportTemplate(
						reportRunner.getFillerFactory(),
						reportRunner.getExporterFactory()));
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
	public void goesOutOfMemory() throws Exception {
		// Use a FillAndExportOperation *without* a JRVirtualizer
		try {
			reportRunner.runReport();
			fail("Should have run out of memory");
		} catch (OutOfMemoryError e) {
			// pass!
			logger.debug("Expected to run out of memory: {}",
					e.getMessage());
		}
	}

	@Test
	public void preventOutOfMemory() throws Exception {
		// Use a FillAndExportOperation *with* JRVirtualizer
		reportRunner.setFillAndExportOperation(
				new VirtualizingFillAndExportOperation(
						reportRunner.getFillAndExportOperation()));
		reportRunner.runReport(); // should NOT run out of memory
	}

}
