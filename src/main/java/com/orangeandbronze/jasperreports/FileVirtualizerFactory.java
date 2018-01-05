package com.orangeandbronze.jasperreports;

import net.sf.jasperreports.engine.JRVirtualizer;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;

/**
 * Creates {@link JRFileVirtualizer} instances.
 *
 * @see JRFileVirtualizer
 */
public class FileVirtualizerFactory implements ReportVirtualizerFactory {

	public static final int DEFAULT_MAX_SIZE = 25;
	public static final String DEFAULT_DIRECTORY =
			System.getProperty("java.io.tmpdir");

	/** Number of pages at which the virtualizer should kick on */
	private int maxSize;
	/** Directory to save the tempoary files */
	private String directory;

	public FileVirtualizerFactory() {
		this.maxSize = DEFAULT_MAX_SIZE;
		this.directory = DEFAULT_DIRECTORY;
	}

	@Override
	public JRVirtualizer getVirtualizer() {
		return new JRFileVirtualizer(maxSize, directory);
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

}
