package com.orangeandbronze.jasperreports.spring;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSource;

public class JRRowMappingDataSource<T> extends JRAbstractBeanDataSource {
	
	private final ResultSet resultSet;
	private final RowMapper<T> rowMapper;

	private int rowNum;
	private T currentBean;

	public JRRowMappingDataSource(ResultSet resultSet, RowMapper<T> rowMapper) {
		super(true);
		if (resultSet == null) {
			throw new IllegalArgumentException(
					"ResultSet cannot be null");
		}
		if (rowMapper == null) {
			throw new IllegalArgumentException(
					"RowMapper cannot be null");
		}
		this.resultSet = resultSet;
		this.rowMapper = rowMapper;
		this.rowNum = 0;
	}

	@Override
	public boolean next() throws JRException {
		try {
			boolean hasNext = resultSet.next();
			if (hasNext) {
				currentBean = rowMapper.mapRow(resultSet, rowNum++);
			}
			return hasNext;
		} catch (SQLException e) {
			throw new JRException(
					JRResultSetDataSource.EXCEPTION_MESSAGE_KEY_RESULT_SET_NEXT_RECORD_NOT_RETRIEVED, 
					null, e);
		}
	}

	@Override
	public Object getFieldValue(JRField field) throws JRException {
		return getFieldValue(currentBean, field);
	}

	@Override
	public void moveFirst() throws JRException {
		// resultSet.first();
		throw new UnsupportedOperationException();
	}

}
