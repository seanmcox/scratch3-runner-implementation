/**
 * 
 */
package com.shtick.utils.scratch3.runner.impl.elements;

import java.util.Map;

import com.shtick.utils.scratch3.runner.core.elements.StageMonitor;
import com.shtick.utils.scratch3.runner.impl.ScratchRuntimeImplementation;

/**
 * @author sean.cox
 *
 */
public class StageMonitorImplementation implements StageMonitor{
	private String id;
	private String mode;
	private String opcode;
	private Map<String,Object> params;
	private String spriteName;
	private long width;
	private long height;
	private double x;
	private double y;
	private boolean visible;
	private double sliderMin;
	private double sliderMax;
	private boolean isDiscrete;
	private ScratchRuntimeImplementation runtime;
	
	/**
	 * @param id 
	 * @param mode 
	 * @param opcode 
	 * @param params 
	 * @param spriteName 
	 * @param width 
	 * @param height 
	 * @param x 
	 * @param y 
	 * @param visible 
	 * @param sliderMin 
	 * @param sliderMax 
	 * @param isDiscrete 
	 * @param runtime 
	 */
	public StageMonitorImplementation(String id, String mode, String opcode, Map<String,Object> params, String spriteName,
			long width, long height, double x, double y, boolean visible, double sliderMin, double sliderMax, boolean isDiscrete,
			ScratchRuntimeImplementation runtime) {
		super();
		this.id = id;
		this.mode = mode;
		this.opcode = opcode;
		this.params = params;
		this.spriteName = spriteName;
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;
		this.visible = visible;
		this.sliderMin = sliderMin;
		this.sliderMax = sliderMax;
		this.isDiscrete = isDiscrete;
		this.runtime = runtime;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @return the opcode
	 */
	public String getOpcode() {
		return opcode;
	}

	/**
	 * @return the params
	 */
	public Map<String, Object> getParams() {
		return params;
	}

	/**
	 * @return the spriteName
	 */
	public String getSpriteName() {
		return spriteName;
	}

	/**
	 * @return the width
	 */
	public long getWidth() {
		return width;
	}

	/**
	 * @return the height
	 */
	public long getHeight() {
		return height;
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @return the runtime
	 */
	public ScratchRuntimeImplementation getRuntime() {
		return runtime;
	}

	/**
	 * @return 
	 * 
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible 
	 * 
	 */
	public void setVisible(boolean visible) {
		if(this.visible!=visible) {
			this.visible = visible;
			runtime.repaintStage();
		}
	}

	/**
	 * @return the sliderMin
	 */
	public double getSliderMin() {
		return sliderMin;
	}

	/**
	 * @return the sliderMax
	 */
	public double getSliderMax() {
		return sliderMax;
	}

	/**
	 * @return the isDiscrete
	 */
	public boolean isDiscrete() {
		return isDiscrete;
	}
}
