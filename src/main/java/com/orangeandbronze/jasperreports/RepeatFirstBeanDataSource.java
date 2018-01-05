package com.orangeandbronze.jasperreports;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

/**
 * A {@link JRAbstractBeanDataSource JasperReports datasource} wrapper/decorator
 * that returns an unlimited number of elements by repeating the first element
 * returned from the delegate datasource.
 * <p>
 * This is useful when subjecting a report to large amounts of data and test if
 * it runs out of memory. A typical test would create one element and have this
 * repeat it for a certain number of times, or until a time limit is reached.
 * </p>
 * <pre>
 * final SECONDS = 60;
 * final ELEMENTS = 1000000; // 1,000,000
 * Object bean = ...; // bean expected by report
 * 
 * // Fill/populate report with a million elements,
 * // or for up to 60 seconds, whichever comes first.
 * ... = JasperFillManager.fillReport(report, parameters,
 *             new TimeLimitedDataSource(
 *                 new CountLimitedDataSource(
 *                     new RepeatFirstBeanDataSource(
 *                         new JRBeanCollectionDataSource(Arrays.asList(bean))),
 *                 ELEMENTS), SECONDS));
 * </pre>
 *
 * @see CountLimitedDataSource
 * @see TimeLimitedDataSource
 */
public class RepeatFirstBeanDataSource extends JRAbstractBeanDataSource {

	private final JRAbstractBeanDataSource delegateDataSource;
	private boolean firstCallMade;

	public RepeatFirstBeanDataSource(JRAbstractBeanDataSource delegateDataSource) {
		super(true);
		if (delegateDataSource == null) {
			throw new IllegalArgumentException(
					"Delegate JRAbstractBeanDataSource cannot be null");
		}
		this.delegateDataSource = delegateDataSource;
		this.firstCallMade = false;
	}

	@Override
	public void moveFirst() throws JRException {
		delegateDataSource.moveFirst();
	}

	@Override
	public boolean next() throws JRException {
		if (firstCallMade) {
			// Keep repeating the first row
			return true;
		} else {
			// Allow the *first* call (to pass through)
			firstCallMade = true;
			return delegateDataSource.next();
		}
	}

	@Override
	public Object getFieldValue(JRField jrField) throws JRException {
		return delegateDataSource.getFieldValue(jrField);
	}

}
