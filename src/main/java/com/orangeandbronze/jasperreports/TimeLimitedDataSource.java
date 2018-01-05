package com.orangeandbronze.jasperreports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;

/**
 * A {@link JRRewindableDataSource JasperReports datasource} wrapper/decorator
 * that ends the wrapped datasource when a time limit is reached.
 * <p>
 * This is typically used for testing. For example, to put a time limit on a
 * wrap/decorate a {@link RepeatFirstBeanDataSource} which returns an unlimited
 * number of elements by repeatedly returning the first element.
 * </p>
 *
 * @see RepeatFirstBeanDataSource
 */
public class TimeLimitedDataSource implements JRRewindableDataSource {

	public static final int DEFAULT_TIME_LIMIT_IN_SECONDS = 60;

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final JRRewindableDataSource delegateDataSource;
	private final int timeLimitInMillis;
	private Long startTimeInMillis;

	public TimeLimitedDataSource(
			JRRewindableDataSource delegateDataSource) {
		this(delegateDataSource, DEFAULT_TIME_LIMIT_IN_SECONDS);
	}

	public TimeLimitedDataSource(
			JRRewindableDataSource delegateDataSource, int timeLimitInSeconds) {
		if (delegateDataSource == null) {
			throw new IllegalArgumentException(
					"Delegate JRDataSource cannot be null");
		}
		if (timeLimitInSeconds <= 0) {
			throw new IllegalArgumentException(
					"Time limit in seconds must be greater than zero");
		}
		this.delegateDataSource = delegateDataSource;
		this.timeLimitInMillis = timeLimitInSeconds * 1000;
	}

	@Override
	public boolean next() throws JRException {
		if (startTimeInMillis == null) {
			startTimeInMillis = System.currentTimeMillis();
		} else {
			boolean limitReached = System.currentTimeMillis() >=
					(startTimeInMillis + timeLimitInMillis);
			if (limitReached) {
				logger.debug("Time limit reached");
				return false;
			}
		}
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
