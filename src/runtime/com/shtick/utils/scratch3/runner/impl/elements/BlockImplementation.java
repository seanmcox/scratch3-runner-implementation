/**
 * 
 */
package com.shtick.utils.scratch3.runner.impl.elements;

import java.util.Map;

import com.shtick.utils.scratch3.runner.core.elements.Block;

/**
 * @author sean.cox
 *
 */
public class BlockImplementation implements Block{
	private String id;
	private String opcode;
	private String next;
	private String parent;
	private Map<String,Input> inputs;
	private Map<String,String[]> fields;
	private boolean shadow;
	private boolean topLevel;
	private Map<String,Object> argMap;
	/**
	 * Optional
	 * Only seems to be used for top-level blocks.
	 */
	private double x;
	/**
	 * Optional
	 * Only seems to be used for top-level blocks.
	 */
	private double y;
	/**
	 * Optional
	 * Defines procedure_prototype-specific attributes
	 */
	private MutationImplementation mutation;
	
	/**
	 * @param id 
	 * @param opcode 
	 * @param next 
	 * @param parent 
	 * @param inputs 
	 * @param fields 
	 * @param shadow 
	 * @param topLevel 
	 * @param x 
	 * @param y 
	 * @param mutation 
	 */
	public BlockImplementation(String id, String opcode, String next, String parent, Map<String,Input> inputs, Map<String,String[]> fields, boolean shadow, boolean topLevel, double x, double y, MutationImplementation mutation) {
		super();
		this.id = id;
		this.opcode = opcode;
		this.next = next;
		this.parent = parent;
		this.inputs = inputs;
		this.fields = fields;
		this.shadow = shadow;
		this.topLevel = topLevel;
		this.x = x;
		this.y = y;
		this.mutation = mutation;
	}
	
	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch3.runner.core.elements.Block#getID()
	 */
	@Override
	public String getID() {
		return id;
	}

	/**
	 * @return the inputs
	 */
	public Map<String, Input> getInputs() {
		return inputs;
	}

	/**
	 * @return the fields
	 */
	public Map<String, String[]> getFields() {
		return fields;
	}

	/**
	 * @return the argMap
	 */
	public Map<String, Object> getArgMap() {
		return argMap;
	}

	/**
	 * @param argMap the argMap to set
	 */
	public void setArgMap(Map<String, Object> argMap) {
		this.argMap = argMap;
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
	 * @return the mutation
	 */
	public MutationImplementation getMutation() {
		return mutation;
	}

	/**
	 * @return the opcode
	 */
	public String getOpcode() {
		return opcode;
	}

	/**
	 * @return the next
	 */
	public String getNext() {
		return next;
	}

	/**
	 * @return the parent
	 */
	public String getParent() {
		return parent;
	}

	/**
	 * @return the shadow
	 */
	public boolean isShadow() {
		return shadow;
	}

	/**
	 * 
	 * @return True if the block is a top level block (has no parent and is not the next block for any other block) and false otherwise.
	 */
	public boolean isTopLevel() {
		return topLevel;
	}
}
