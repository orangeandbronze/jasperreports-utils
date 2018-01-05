package com.orangeandbronze.jasperreports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;

/**
 * Decorates or wraps a {@link JRRewindableDataSource JasperReports datasource}
 * to limit the number of elements returned.
 * <p>
 * This is typically used for testing. For example, to put a count limit to a
 * {@link RepeatFirstBeanDataSource} which returns an unlimited number of
 * elements by repeatedly returning the first element.
 * </p>
 *
 * @see RepeatFirstBeanDataSource
 */
public class CountLimitedDataSource implements JRRewindableDataSource {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final JRRewindableDataSource delegateDataSource;
	private final long countLimit;
	private long count;

	public CountLimitedDataSource(
			JRRewindableDataSource delegateDataSource, long countLimit) {
		if (delegateDataSource == null) {
			throw new IllegalArgumentException(
					"Delegate JRDataSource cannot be null");
		}
		this.delegateDataSource = delegateDataSource;
		this.countLimit = countLimit;
		this.count = 0;
	}

	@Override
	public boolean next() throws JRException {
		if (count >= countLimit) {
			logger.debug("Count limit reached");
			return false;
		}
		count++;
		return delegateDataSource.next();
	}

	@Override
	public Object getFieldValue(JRField jrField) throws JRException {
		return delegateDataSource.getFieldValue(jrField);
	}

	@Override
	public void moveFirst() throws JRException {
		delegateDataSource.moveFirst();
	}

}
