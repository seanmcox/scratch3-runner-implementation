/**
 * 
 */
package com.shtick.utils.scratch3.runner.impl.elements;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.shtick.utils.scratch3.runner.core.elements.TypedValue;

/**
 * @author scox
 *
 */
public class TypedValueImplementation implements TypedValue{
	private int type;
	private List<Object> value;
	
	/**
	 * 
	 * @param type
	 * @param value
	 */
	public TypedValueImplementation(int type, Object[] value) {
		super();
		this.type = type;
		this.value = Collections.unmodifiableList(Arrays.asList(value));
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core3.elements.TypedValue#getType()
	 */
	@Override
	public int getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core3.elements.TypedValue#getValue()
	 */
	@Override
	public List<Object> getValue() {
		return value;
	}
}
