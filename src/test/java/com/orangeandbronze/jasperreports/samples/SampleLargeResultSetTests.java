package com.orangeandbronze.jasperreports.samples;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ClassUtils;

import com.orangeandbronze.jasperreports.FillAndExportTemplate;
import com.orangeandbronze.jasperreports.JdbcFillReportCallback;
import com.orangeandbronze.jasperreports.PdfReportRunner;
import com.orangeandbronze.jasperreports.VirtualizingFillAndExportOperation;
import com.orangeandbronze.jdbc.RepeatFirstRowDataSourceProxy;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;

/**
 * For best results, this needs to be run with limited memory (i.e.
 * <code>-Xmx16m</code>). This test uses a {@link RepeatFirstRowDataSourceProxy}
 * that repeats until {@link #ELEMENTS} or {@link #SECONDS} are reached.
 * <p>
 * See <a href=
 * "http://jasperreports.sourceforge.net/sample.reference/virtualizer/index.html">Virtualizer
 * Sample - JasperReports</a>
 * </p>
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
public class SampleLargeResultSetTests {

	@Configuration
	static class Config {
		@Bean
		DataSource dataSource() {
			String thisPackage =
					ClassUtils.classPackageAsResourcePath(this.getClass());
			return new EmbeddedDatabaseBuilder()
					.setType(EmbeddedDatabaseType.HSQL)
					.addScript("classpath:" + thisPackage + "/schema-hsqldb.sql")
					.addScript("classpath:" + thisPackage + "/data.sql")
					.build();
		}
	}

	@Autowired
	private DataSource dataSource;
	
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
				new JdbcFillReportCallback(getDataSourceProxy()));
	}

	@After
	public void tearDown() throws Exception {
		reportRunner.close();
	}

	protected JasperReport createJasperReport() throws Exception {
		InputStream reportAsStream = this.getClass().getResourceAsStream(
				"report-sqlquery.jrxml");
		assertNotNull(reportAsStream);
		return JasperCompileManager.compileReport(reportAsStream);
	}

	protected File createOutputFile() throws Exception {
		return File.createTempFile("output", ".pdf");
		// return new File("output.pdf");
	}

	protected DataSource getDataSourceProxy() throws Exception {
		// Create "proxy"
		DataSource dataSource = getDataSource();
		assertNotNull(dataSource);
		RepeatFirstRowDataSourceProxy proxyDataSource =
				new RepeatFirstRowDataSourceProxy(dataSource);
		proxyDataSource.setCountLimit(ELEMENTS);
		proxyDataSource.setTimeLimitInSeconds(SECONDS);
		return proxyDataSource;
	}

	protected DataSource getDataSource() {
		return this.dataSource;
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
