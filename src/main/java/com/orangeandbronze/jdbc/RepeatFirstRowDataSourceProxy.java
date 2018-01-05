package com.orangeandbronze.jdbc;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;

public class RepeatFirstRowDataSourceProxy implements DataSource {

	protected final org.slf4j.Logger logger =
			LoggerFactory.getLogger(this.getClass());

	private final DataSource targetDataSource;
	private Integer timeLimitInSeconds = null;
	private int timeLimitInMillis;
	private Long countLimit = null;

	public RepeatFirstRowDataSourceProxy(DataSource targetDataSource) {
		if (targetDataSource == null) {
			throw new IllegalArgumentException(
					"Target datasource cannot be null");
		}
		this.targetDataSource = targetDataSource;
	}

	public void setTimeLimitInSeconds(Integer seconds) {
		this.timeLimitInSeconds = seconds;
		if (seconds != null) {
			this.timeLimitInMillis = seconds * 1000;
		}
	}

	public void setCountLimit(Long count) {
		this.countLimit = count;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return targetDataSource.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		targetDataSource.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		targetDataSource.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return targetDataSource.getLoginTimeout();
	}

	@Override
	public Logger getParentLogger()
			throws SQLFeatureNotSupportedException {
		return targetDataSource.getParentLogger();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return targetDataSource.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return targetDataSource.isWrapperFor(iface);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return proxyConnection(targetDataSource.getConnection());
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return proxyConnection(
				targetDataSource.getConnection(username, password));
	}

	private Connection proxyConnection(Connection targetConnection) {
		return (Connection) Proxy.newProxyInstance(
				Connection.class.getClassLoader(),
				new Class<?>[] { Connection.class },
				new ConnectionInvocationHandler(targetConnection));
	}

	private class ConnectionInvocationHandler implements InvocationHandler {

		private final Connection targetConnection;

		public ConnectionInvocationHandler(
				Connection targetConnection) {
			if (targetConnection == null) {
				throw new IllegalArgumentException(
						"Target connection cannot be null");
			}
			this.targetConnection = targetConnection;
		}

		@Override
		public Object invoke(
				Object proxy, Method method, Object[] args) throws Throwable {
			try {
				Object returnValue = method.invoke(targetConnection, args);
				if (Statement.class.isAssignableFrom(method.getReturnType())) {
					logger.debug("Proxying Statement returned from [{}]", method.getName());
					Statement targetStatement = (Statement) returnValue;
					returnValue = Proxy.newProxyInstance(
							ConnectionInvocationHandler.class.getClassLoader(),
							new Class<?>[] { method.getReturnType() },
							new StatementInvocationHandler(targetStatement));
				}
				return returnValue;
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}
	}

	private class StatementInvocationHandler implements InvocationHandler {

		private final Statement targetStatement;
		
		public StatementInvocationHandler(Statement targetStatement) {
			if (targetStatement == null) {
				throw new IllegalArgumentException(
						"Target statement cannot be null");
			}
			this.targetStatement = targetStatement;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try {
				Object returnValue = method.invoke(targetStatement, args);
				if ("executeQuery".equals(method.getName())) {
					logger.debug("Proxying ResultSet returned from [{}]", method.getName());
					ResultSet targetResultSet = (ResultSet) returnValue;
					InvocationHandler handler =
							new RepeatFirstRowResultSetInvocationHandler(targetResultSet);
					if (countLimit != null) {
						handler = new CountLimitedInvocationHandler(handler);
					}
					if (timeLimitInSeconds != null) {
						handler = new TimeLimitedInvocationHandler(handler);
					}
					returnValue = Proxy.newProxyInstance(
							StatementInvocationHandler.class.getClassLoader(),
							new Class<?>[] { ResultSet.class },
							handler);
				}
				return returnValue;
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}
	}

	private class RepeatFirstRowResultSetInvocationHandler implements InvocationHandler {

		private final ResultSet targetResultSet;
		private boolean firstCallMade;

		public RepeatFirstRowResultSetInvocationHandler(
				ResultSet targetResultSet) {
			if (targetResultSet == null) {
				throw new IllegalArgumentException(
						"Target resultset cannot be null");
			}
			this.targetResultSet = targetResultSet;
			this.firstCallMade = false;
		}

		@Override
		public Object invoke(
				Object proxy, Method method, Object[] args) throws Throwable {
			if ("next".equals(method.getName())) {
				if (firstCallMade) {
					// Keep repeating the first row
					logger.trace("Repeating first row");
					return true;
				} else {
					// Allow the *first* call (to pass through)
					firstCallMade = true;
				}
			}
			logger.trace("Invoking underlying method [{}]", method.getName());
			try {
				return method.invoke(targetResultSet, args);
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}

	}

	private class CountLimitedInvocationHandler implements InvocationHandler {

		private final InvocationHandler delegateHandler;
		private int count = 0;

		public CountLimitedInvocationHandler(
				InvocationHandler delegateHandler) {
			if (delegateHandler == null) {
				throw new IllegalArgumentException(
						"Delegate handler cannot be null");
			}
			this.delegateHandler = delegateHandler;
		}

		@Override
		public Object invoke(
				Object proxy, Method method, Object[] args) throws Throwable {
			if ("next".equals(method.getName())) {
				if (count > countLimit) {
					logger.debug("Count limit reached");
					return false;
				}
				count++;
			}
			return delegateHandler.invoke(proxy, method, args);
		}

	}

	private class TimeLimitedInvocationHandler implements InvocationHandler {

		private final InvocationHandler delegateHandler;
		private Long startTimeInMillis;

		public TimeLimitedInvocationHandler(
				InvocationHandler delegateHandler) {
			if (delegateHandler == null) {
				throw new IllegalArgumentException(
						"Delegate handler cannot be null");
			}
			this.delegateHandler = delegateHandler;
			this.startTimeInMillis = null;
		}

		@Override
		public Object invoke(
				Object proxy, Method method, Object[] args) throws Throwable {
			if ("next".equals(method.getName())) {
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
			}
			return delegateHandler.invoke(proxy, method, args);
		}

	}

}
