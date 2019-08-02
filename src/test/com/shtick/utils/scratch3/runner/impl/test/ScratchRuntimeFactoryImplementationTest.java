/**
 * 
 */
package com.shtick.utils.scratch3.runner.impl.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.shtick.utils.scratch3.runner.impl.ScratchRuntimeFactoryImplementation;

/**
 * @author sean.cox
 *
 */
public class ScratchRuntimeFactoryImplementationTest {
	/**
	 * 
	 */
	@Test
	public void testIsValidFilename() {
		ScratchRuntimeFactoryImplementation scratchRuntimeFactoryImplementation = new ScratchRuntimeFactoryImplementation();
		
		assertTrue(scratchRuntimeFactoryImplementation.isValidFilename("test.sb3"));
		assertFalse(scratchRuntimeFactoryImplementation.isValidFilename("test.sb2"));
		assertFalse(scratchRuntimeFactoryImplementation.isValidFilename("test.zip"));
		assertTrue(scratchRuntimeFactoryImplementation.isValidFilename(".sb3"));
		assertFalse(scratchRuntimeFactoryImplementation.isValidFilename("sb3"));
		assertFalse(scratchRuntimeFactoryImplementation.isValidFilename(null));
	}
}
