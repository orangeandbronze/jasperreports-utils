package com.orangeandbronze.jasperreports;

import java.io.OutputStream;
import java.util.Map;

import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * A <em>highly opinionated</em> way to generate reports using
 * JasperReports&reg;.
 * <p>
 * A JasperReport goes through three (3) stages in its lifecycle:
 * </p>
 * <ol>
 * <li>Compilation</li>
 * <li>Filling (i.e. fill report with data)</li>
 * <li>Exporting (e.g. export to PDF)</li>
 * </ol>
 * <p>
 * While there is {@link JasperFillManager} and {@link JasperExportManager}
 * which make <i>filling</i> and <i>exporting</i> a report easy, having them
 * separated does not add much value.
 * </p>
 * <p>
 * The <code>fill</code> methods are quite convenient, but lead to poor
 * practices. For example,
 * {@link JasperFillManager#fill(JasperReport, Map, java.sql.Connection)
 * fill(JasperReport, Map, Connection)} accepts a JDBC connection as the source
 * of data for the report. What happens if the connection is not closed? It
 * would be less error prone if we can simply use a {@link DataSource}, and have
 * it get a connection from it, and ensure that it gets closed.
 * </p>
 * <p>
 * Another example is
 * {@link JasperFillManager#fill(JasperReport, Map, JRDataSource)
 * fill(JasperReport, Map, JRDataSource)}. It accepts a {@link JRDataSource}
 * which is perfectly fine. But it has a tendency to make it easy to pass in a
 * {@link JRBeanCollectionDataSource} which can contain thousands of objects and
 * cause an out of memory error with production data.
 * </p>
 * <p>
 * Tip: When filling reports, <em>load one data record/object at
 * a time</em>. <strong>DO NOT</strong> attempt to load an entire collection.
 * </p>
 */
public interface FillAndExportOperation {

	/**
	 * Fills and exports a given report.
	 *
	 * @param report
	 *            the report to run
	 * @param parameters
	 *            the report parameters
	 * @param callback
	 *            called to fill the report
	 * @param output
	 *            the {@code OutputStream} to write the rendered report to. Callers
	 *            must remember to close this output stream since implementations
	 *            will not close it.
	 * @throws JRException
	 *             if an error occurs while filling/exporting the report
	 * @throws NullPointerException
	 *             if any of the arguments are <code>null</code>
	 */
	void fillAndExportReport(
			JasperReport report,
			Map<String,Object> parameters,
			FillReportCallback callback,
			OutputStream output) throws JRException;

}
