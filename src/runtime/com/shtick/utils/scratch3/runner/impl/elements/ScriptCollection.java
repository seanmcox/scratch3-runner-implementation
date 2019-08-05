/**
 * 
 */
package com.shtick.utils.scratch3.runner.impl.elements;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

import com.shtick.utils.scratch3.runner.core.InvalidScriptDefinitionException;
import com.shtick.utils.scratch3.runner.core.Opcode;
import com.shtick.utils.scratch3.runner.core.OpcodeHat;
import com.shtick.utils.scratch3.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch3.runner.impl.ScratchRuntimeImplementation;

/**
 * @author scox
 *
 */
public class ScriptCollection {
	private Map<String, BlockImplementation> blocks;
	private java.util.List<ScriptImplementation> scripts;
	
	/**
	 * 
	 * @param blocks
	 * @param context 
	 * @param runtime 
	 * @throws InvalidScriptDefinitionException 
	 */
	public ScriptCollection(Map<String, BlockImplementation> blocks, ScriptContext context, ScratchRuntimeImplementation runtime) throws InvalidScriptDefinitionException{
		this.blocks = blocks;
		scripts = new LinkedList<>();
		for(String key:blocks.keySet()) {
			BlockImplementation block = blocks.get(key);
			if(!block.isTopLevel())
				continue;
			String opcode = block.getOpcode();
			Opcode opcodeImplementation = runtime.getFeatureSet().getOpcode(opcode);
			if(opcodeImplementation==null)
				throw new InvalidScriptDefinitionException("Unimplemented opcode: "+opcode);
			if(opcodeImplementation instanceof OpcodeHat) {
				ScriptImplementation script = new ScriptImplementation(context, blocks, key, runtime);
				scripts.add(script);
			}
		}
	}
	
	/**
	 * Used for cloning. Populating the ScriptCollection handled by clone()
	 */
	private ScriptCollection() {}
	
	/**
	 * 
	 * @return The java.util.List<ScriptImplementation>. Not a copy. Should not be modified.
	 */
	public java.util.List<ScriptImplementation> getScripts() {
		return scripts;
	}
	
	/**
	 * @param context 
	 * @return A copy of this ScriptCollection based on the given ScriptContext
	 * 
	 */
	public ScriptCollection clone(ScriptContext context) {
		ScriptCollection clone = new ScriptCollection();
		clone.blocks = this.blocks;
		clone.scripts = new ArrayList<>(scripts.size());
		for(ScriptImplementation script:scripts)
			clone.scripts.add((ScriptImplementation)script.clone(context));
		return clone;
	}
}
