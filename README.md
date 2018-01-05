# JasperReports Utility Classes

This project contains some utility classes that make JasperReports&reg; less tricky. A typical use would be something like this:

```java
// Set-up
ReportFillerFactory fillerFactory = ...;
ReportExporterFactory exporterFactory = ...;
java.sql.DataSource dataSource = ...;

FillAndExportOperation reportGenerator =
    new VirtualizingFillAndExportOperation( // <--
        new FillAndExportTemplate(fillerFactory, exporterFactory));

// Use
Map&lt;String, Object&gt; parameters = ...;
JasperReport jasperReport = ...; // uses SQL query
OutputStream out = ...;
try {
    reportGenerator.fillAndExportReport(
        jasperReport, parameters,
        new JdbcFillReportCallback(dataSource), out);
} finally {
    out.close();
}
```

Or, if you're using Spring Framework, you can have a configuration that looks something like this:

```java
@Configuration
... class ... {
    @Bean
    FillAndExportOperation fillAndExportOperation() {
        return new VirtualizingFillAndExportOperation( // <--
            new FillAndExportTemplate(
                reportFillerFactory(), pdfExporterFactory()));
    }
    @Bean
    ReportFillerFactory reportFillerFactory() {
        return new DefaultReportFillerFactory();
    }

    @Bean
    ReportExporterFactory pdfExporterFactory() {
        return new PdfReportExporterFactory();
    }

    @Bean
    java.sql.DataSource dataSource() {...}
}
```

And you can inject a [FillAndExportOperation](https://github.com/orangeandbronze/jasperreports-utils/blob/master/src/main/java/com/orangeandbronze/jasperreports/FillAndExportOperation.java) into the objects that need to generate reports using JR.

```java
class ... {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private FillAndExportOperation reportGenerator;
    @Autowired
    private JasperReport jasperReport;
    ...
    public ...() throws JRException {
        Map&lt;String, Object&gt; parameters = ...;
        OutputStream out = ...;
        try {
            reportGenerator.fillAndExportReport(
                jasperReport, parameters,
                new JdbcFillReportCallback(dataSource), out);
        } finally {
            out.close();
        }
    }
}
```

If you're interested to know the tricky parts, read on.

## JasperReports Revisited

When using JasperReports, three (3) steps are usually involved to run a report:

1. Compile report (results to a `JasperReport`)
2. Fill report (i.e. fill report with data) (results to a `JasperPrint`)
3. Export report (e.g. PDF, XLS)

```java
// 1. Load report
JasperReport compiledReport = (JasperReport) JRLoader.loadObject(...);
// 2. Fill report (runs report by loading data)
// Note: This is the step that takes time (and memory)
JasperPrint filledReport = JasperFillManager.fillReport(
		compiledReport, new HashMap<>(), jrDataSource /* or java.sql.Connection */);
// 3. Export report (e.g. to PDF)
JasperExportManager.exportReportToPdfStream(filledReport, pdfOutput);
```

**Which step do you think takes the most time and memory?**

If you answered step #2, you're correct!

Note that step #3 can also take some time. But step #3 is dependent on the number of pages/objects created in step #2. So, if step #2 takes about 10 seconds, step #3 will take another 10 or 15 seconds.

## Running Out Of Memory

Chances are, when you're running your report in your development environment, it does not run out of memory. That's probably because you're only running against small amounts of test data. But with production data (and real operating data), it will consume more memory and even run out of memory. So, *design for that*.

Unfortunately, JasperReports does not use the disk when it runs out of memory. You'll have to explicitly configure it to use a *virtualizer*.

## Looong Runs

If the report will generate a few thousand pages, there's really no simple way to make it faster. The important thing to remember is that it *may* take longer than you think. With small amounts of test data, it will surely run fast (probably just a few seconds). But with production data (and real operating data), it might take a few minutes. So, *design for that*.

Launch the report in a separate thread, store the output to a persistent store (e.g. file), and have the user retrieve it when it's completed.

```java
ExecutorService executor = ...;
Future<?> task = executor.submit(() -> {
    fillAndExportOperation.fillAndExportReport(
        compiledReport, parameters, ..., output);
});
```

## Compile It During Build-Time (Not At Run-Time)

Tip: Compile your `*.jrxml` to `*.jasper` during build (not at run-time). There are some Maven plugins to help with this.

## Test with Bigger Amounts of Data

This project contains some classes to help test reports with bigger amounts of data by simply repeating a given piece of data several times, or repeating until a timeout is reached.

- [RepeatFirstBeanDataSource](https://github.com/orangeandbronze/jasperreports-utils/blob/master/src/main/java/com/orangeandbronze/jasperreports/RepeatFirstBeanDataSource.java) (a `JRDataSource`, not a `java.sql.DataSource`)
	- Usually used with:
		- `TimeLimitedDataSource`
		- `CountLimitedDataSource`
- [RepeatFirstBeanDataSourceProxy]((https://github.com/orangeandbronze/jasperreports-utils/blob/master/src/main/java/com/orangeandbronze/jdbc/RepeatFirstBeanDataSourceProxy.java)) (proxies a `java.sql.DataSource`) - This can be used outside of JasperReports, since it only relies on JDBC.
