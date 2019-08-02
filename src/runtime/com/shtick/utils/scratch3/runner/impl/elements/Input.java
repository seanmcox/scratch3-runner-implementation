/**
 * 
 */
package com.shtick.utils.scratch3.runner.impl.elements;

/**
 * @author scox
 *
 */
public class Input {
	private int type;
	private Object value;
	
	/**
	 * 
	 * @param type
	 * @param value if type is 1, then this should be a TypedValueImplementation. Otherwise, it should be a string referencing an evaluable block.
	 */
	public Input(int type, Object value) {
		super();
		this.type = type;
		this.value = value;
	}
	
	/**
	 * 
	 * @return A String for type=1 and TypedValueImplementation otherwise.
	 */
	public Object getRealValue() {
		return value;
	}

	/**
	 * @return the type (1,2, or 3)
	 */
	public int getType() {
		return type;
	}
}
