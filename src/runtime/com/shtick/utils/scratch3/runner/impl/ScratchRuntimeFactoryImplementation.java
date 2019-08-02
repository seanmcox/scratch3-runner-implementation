/**
 * 
 */
package com.shtick.utils.scratch3.runner.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.shtick.utils.scratch3.runner.core.FeatureLibrary;
import com.shtick.utils.scratch3.runner.core.FeatureSet;
import com.shtick.utils.scratch3.runner.core.FeatureSetGenerator;
import com.shtick.utils.scratch3.runner.core.ScratchRuntime;
import com.shtick.utils.scratch3.runner.core.ScratchRuntimeFactory;
import com.shtick.utils.scratch3.runner.common.AmalgamatedFeatureSet;

/**
 * @author scox
 *
 */
public class ScratchRuntimeFactoryImplementation implements ScratchRuntimeFactory {

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntimeFactory#isValidFilename(java.lang.String)
	 */
	@Override
	public boolean isValidFilename(String filename) {
		if(filename == null)
			return false;
		return filename.matches(".*\\.sb3");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntimeFactory#createScratchRuntime(java.io.File)
	 */
	@Override
	public ScratchRuntime createScratchRuntime(File projectFile) throws IOException{
		return createScratchRuntime(projectFile, 480, 360);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntimeFactory#createScratchRuntime(java.io.File, int, int)
	 */
	@Override
	public ScratchRuntime createScratchRuntime(File projectFile, int stageWidth, int stageHeight) throws IOException{
		Collection<FeatureSetGenerator> generators = FeatureLibrary.getFeatureSetGenerators();
		FeatureSet[] featureSets = new FeatureSet[generators.size()];
		int i = 0;
		for(FeatureSetGenerator generator:generators){
			featureSets[i] = generator.generateFeatureSet();
			i++;
		}
		return createScratchRuntime(projectFile, stageWidth, stageHeight, new AmalgamatedFeatureSet(featureSets));
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntimeFactory#createScratchRuntime(java.io.File, int, int, com.shtick.utils.scratch.runner.core.OpcodeRegistry, com.shtick.utils.scratch.runner.core.GraphicEffectRegistry, com.shtick.utils.scratch.runner.core.StageMonitorCommandRegistry)
	 */
	@Override
	public ScratchRuntime createScratchRuntime(File projectFile, int stageWidth, int stageHeight,
			FeatureSet featureSet) throws IOException {
		return new ScratchRuntimeImplementation(projectFile, stageWidth, stageHeight, featureSet);
	}
}
