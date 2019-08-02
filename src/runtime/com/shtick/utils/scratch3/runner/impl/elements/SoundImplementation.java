/**
 * 
 */
package com.shtick.utils.scratch3.runner.impl.elements;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.shtick.utils.scratch3.runner.core.elements.Sound;

/**
 * @author sean.cox
 *
 */
public class SoundImplementation implements Sound{
	private String assetId;
	private String name;
	private String dataFormat;
	private String format;
	private long rate;
	private long sampleCount;
	private String md5ext;
	private byte[] soundData;
	
	/**
	 * @param assetId 
	 * @param name 
	 * @param dataFormat 
	 * @param format 
	 * @param sampleCount 
	 * @param rate 
	 * @param md5ext 
	 * @param soundData 
	 */
	public SoundImplementation(String assetId, String name, String dataFormat, String format, long sampleCount, long rate, String md5ext, byte[] soundData) {
		super();
		this.assetId = assetId;
		this.name = name;
		this.dataFormat = dataFormat;
		this.sampleCount = sampleCount;
		this.rate = rate;
		this.format = format;
		this.md5ext = md5ext;
		this.soundData = soundData;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sound#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return null;
	}

	/**
	 * @return the assetId
	 */
	public String getAssetId() {
		return assetId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the dataFormat
	 */
	public String getDataFormat() {
		return dataFormat;
	}

	/**
	 * @return the md5ext
	 */
	public String getMd5ext() {
		return md5ext;
	}

	/**
	 * 
	 * @return The sample count.
	 */
	public long getSampleCount() {
		return sampleCount;
	}

	/**
	 * 
	 * @return The rate
	 */
	public long getRate() {
		return rate;
	}

	/**
	 * 
	 * @return The format.
	 */
	public String getFormat() {
		return format;
	}
	
	/**
	 * 
	 * @return An InoutStream containing the audio file data.
	 */
	public InputStream getSoundData() {
		return new ByteArrayInputStream(soundData);
	}
}
