/**
 * 
 */
package com.shtick.utils.scratch3.runner.common;

import com.shtick.utils.scratch3.runner.core.FeatureSet;
import com.shtick.utils.scratch3.runner.core.ScratchRuntime;

/**
 * @author scox
 *
 */
public abstract class ScratchRuntimeCommon implements ScratchRuntime {
	private FeatureSet featureSet;
	
	/**
	 * 
	 * @param featureSet
	 */
	public ScratchRuntimeCommon(FeatureSet featureSet) {
		this.featureSet = featureSet;
	}
	
	/**
	 * @return the stageMonitorCommandRegistry
	 */
	public FeatureSet getFeatureSet() {
		return featureSet;
	}
	
	/**
	 * 
	 * @return The current BubbleImage defined.
	 */
	public abstract BubbleImage getBubbleImage();
	
	/**
	 * Called by the ScriptTupleThread.
	 * If repaint was requested, then this triggers the repaint to occur.
	 * 
	 * @return true if repaint called, and false otherwise.
	 */
	protected abstract boolean repaintStageFinal();
}
