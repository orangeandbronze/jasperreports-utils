/**
 * <h2>Sample Use Cases</h2>
 *
 * For a report that uses SQL query, use
 * {@link com.orangeandbronze.jasperreports.FillAndExportTemplate
 * FillAndExportTemplate} with a
 * {@link com.orangeandbronze.jasperreports.JdbcFillReportCallback
 * JdbcReportFillerCallback}.
 * 
 * <pre>
 * // Set-up
 * ReportFillerFactory fillerFactory = ...;
 * ReportExporterFactory exporterFactory = ...;
 * java.sql.DataSource dataSource = ...;
 *
 * FillAndExportOperation reportGenerator =
 *     new VirtualizingFillAndExportOperation(
 *         new FillAndExportTemplate(
 *             fillerFactory, exporterFactory));
 * 
 * // Use
 * Map&lt;String, Object&gt; parameters = ...;
 * JasperReport jasperReport = ...; // uses SQL query
 * OutputStream out = ...;
 * try {
 *     reportGenerator.fillAndExportReport(
 *         jasperReport, parameters,
 *         <b>new JdbcFillReportCallback(dataSource)</b>, out);
 * } finally {
 *     out.close();
 * }
 * </pre>
 * <p>
 * For a report that uses {@link net.sf.jasperreports.engine.JRDataSource
 * JRDataSource}, use
 * {@link com.orangeandbronze.jasperreports.FillAndExportTemplate
 * FillAndExportTemplate} with a
 * {@link com.orangeandbronze.jasperreports.JRDataSourceFillReportCallback
 * JRDataSourceReportFillerCallback}.
 * </p>
 * 
 * <pre>
 * // Set-up
 * ReportFillerFactory fillerFactory = ...;
 * ReportExporterFactory exporterFactory = ...;
 *
 * FillAndExportOperation reportGenerator =
 *     new VirtualizingFillAndExportOperation(
 *         new FillAndExportTemplate(
 *             fillerFactory, exporterFactory));
 * 
 * // Use
 * JRDataSource jrDataSource = ...;
 * Map&lt;String, Object&gt; parameters = ...;
 * JasperReport jasperReport = ...; // uses a JRDataSource
 * OutputStream out = ...;
 * try {
 *     reportGenerator.fillAndExportReport(
 *         jasperReport, parameters,
 *         <b>new JRDataSourceFillReportCallback(jrDataSource)</b>, out);
 * } finally {
 *     out.close();
 * }
 * </pre>
 * 
 * @see com.orangeandbronze.jasperreports.FillAndExportTemplate
 * @see com.orangeandbronze.jasperreports.VirtualizingFillAndExportOperation
 * @see com.orangeandbronze.jasperreports.JdbcFillReportCallback
 * @see com.orangeandbronze.jasperreports.JRDataSourceFillReportCallback
 */
package com.orangeandbronze.jasperreports;