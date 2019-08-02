package com.shtick.utils.scratch3.runner.impl.elements;

import com.shtick.utils.scratch3.runner.core.elements.Variable;

/**
 * 
 * @author sean.cox
 *
 */
public class VariableImplementation implements Variable{
	private String name;
	private String displayText;
	private Object value;
	
	/**
	 * @param name
	 * @param value
	 * @param displayText 
	 */
	public VariableImplementation(String name, Object value, String displayText) {
		super();
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getValue() {
		return value;
	}

	/**
	 * @return the displayText
	 */
	public String getDisplayText() {
		return displayText;
	}
	
	/**
	 * 
	 * @param value
	 */
	public void setValue(Object value) {
		this.value = value;
	}
}
