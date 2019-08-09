package com.shtick.utils.scratch3.runner.impl.elements;

import com.shtick.utils.scratch3.runner.core.elements.Variable;

/**
 * 
 * @author sean.cox
 *
 */
public class VariableImplementation implements Variable{
	private String id;
	private String name;
	private Object value;
	
	/**
	 * @param id
	 * @param value
	 * @param name 
	 */
	public VariableImplementation(String id, Object value, String name) {
		super();
		this.id = id;
		this.value = value;
		this.name = name;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public Object getValue() {
		return value;
	}

	/**
	 * @return the displayText
	 */
	public String getName() {
		return name;
	}
	
	@Override
	public void setValue(Object value) {
		this.value = value;
	}
}
