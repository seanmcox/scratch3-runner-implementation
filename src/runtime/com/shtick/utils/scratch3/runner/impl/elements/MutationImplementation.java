/**
 * 
 */
package com.shtick.utils.scratch3.runner.impl.elements;

import java.util.List;

import com.shtick.utils.scratch3.runner.core.elements.Mutation;

/**
 * @author scox
 *
 */
public class MutationImplementation implements Mutation{
	private String tagName;
	/**
	 * Not sure what this is for.
	 */
	private List<Object> children;
	private String proccode;
	private String argumentids;
	private String argumentnames;
	private String argumentdefaults;
	private boolean warp;
	
	/**
	 * 
	 * @param tagName
	 * @param children
	 * @param proccode
	 * @param argumentids
	 * @param argumentnames
	 * @param argumentdefaults
	 * @param warp
	 */
	public MutationImplementation(String tagName, List<Object> children, String proccode, String argumentids,
			String argumentnames, String argumentdefaults, boolean warp) {
		this.tagName = tagName;
		this.children = children;
		this.proccode = proccode;
		this.argumentids = argumentids;
		this.argumentnames = argumentnames;
		this.argumentdefaults = argumentdefaults;
		this.warp = warp;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch3.runner.core.elements.Mutation#getTagName()
	 */
	@Override
	public String getTagName() {
		return tagName;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch3.runner.core.elements.Mutation#getChildren()
	 */
	@Override
	public Object[] getChildren() {
		return children.toArray();
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch3.runner.core.elements.Mutation#getProccode()
	 */
	@Override
	public String getProccode() {
		return proccode;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch3.runner.core.elements.Mutation#getArgumentIds()
	 */
	@Override
	public String getArgumentIds() {
		return argumentids;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch3.runner.core.elements.Mutation#getArgumentNames()
	 */
	@Override
	public String getArgumentNames() {
		return argumentnames;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch3.runner.core.elements.Mutation#getArgumentDefaults()
	 */
	@Override
	public String getArgumentDefaults() {
		return argumentdefaults;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch3.runner.core.elements.Mutation#getWarp()
	 */
	@Override
	public Boolean getWarp() {
		return warp;
	}
}
