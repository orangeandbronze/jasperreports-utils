package com.orangeandbronze.jasperreports;

import net.sf.jasperreports.engine.JRVirtualizer;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;

public class SwapFileVirtualizerFactory implements ReportVirtualizerFactory {

	public static final int DEFAULT_MAX_SIZE = 25;
	public static final int DEFAULT_BLOCK_SIZE = 4096;
	public static final int DEFAULT_MIN_BLOCK_GROW_COUNT = 100;
	public static final String DEFAULT_DIRECTORY =
			System.getProperty("java.io.tmpdir");

	/** Number of pages at which the virtualizer should kick on */
	private int maxSize;
	/** The directory where the swap file should be created */
	private String directory;
	/** The size of the blocks allocated by the swap file */
	private int blockSize;
	/** The minimum number of blocks by which the swap file grows when full */
	private int minGrowCount;

	public SwapFileVirtualizerFactory() {
		this.maxSize = DEFAULT_MAX_SIZE;
		this.directory = DEFAULT_DIRECTORY;
		this.blockSize = DEFAULT_BLOCK_SIZE;
		this.minGrowCount = DEFAULT_MIN_BLOCK_GROW_COUNT;
	}

	@Override
	public JRVirtualizer getVirtualizer() {
		JRSwapFile swapFile = new JRSwapFile(directory, blockSize, minGrowCount);
		return new JRSwapFileVirtualizer(maxSize, swapFile);
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

	public int getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	public int getMinGrowCount() {
		return minGrowCount;
	}

	public void setMinGrowCount(int minGrowCount) {
		this.minGrowCount = minGrowCount;
	}

}
