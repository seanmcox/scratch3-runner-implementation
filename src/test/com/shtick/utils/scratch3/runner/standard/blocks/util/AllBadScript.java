/**
 * 
 */
package com.shtick.utils.scratch3.runner.standard.blocks.util;

import com.shtick.utils.scratch3.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch3.runner.core.elements.Script;

/**
 * @author Sean
 *
 */
public class AllBadScript implements Script{
	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptTuple#getContext()
	 */
	@Override
	public ScriptContext getContext() {
		throw new UnsupportedOperationException("Called getContext when not expected.");
	}

	@Override
	public Script clone(ScriptContext context) {
		throw new UnsupportedOperationException("Called clone when not expected.");
	}

}
