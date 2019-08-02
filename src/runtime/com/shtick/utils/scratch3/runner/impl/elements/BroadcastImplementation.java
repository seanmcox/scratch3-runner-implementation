package com.shtick.utils.scratch3.runner.impl.elements;

import com.shtick.utils.scratch3.runner.core.elements.Broadcast;

/**
 * 
 * @author sean.cox
 *
 */
public class BroadcastImplementation implements Broadcast{
	private String id;
	private String name;
	
	/**
	 * @param id
	 * @param name
	 */
	public BroadcastImplementation(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}
}
