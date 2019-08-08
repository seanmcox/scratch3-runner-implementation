/**
 * 
 */
package com.shtick.utils.scratch3.runner.impl.elements;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.shtick.utils.scratch3.runner.core.InvalidScriptDefinitionException;
import com.shtick.utils.scratch3.runner.core.Opcode;
import com.shtick.utils.scratch3.runner.core.OpcodeControl;
import com.shtick.utils.scratch3.runner.core.OpcodeHat;
import com.shtick.utils.scratch3.runner.core.OpcodeValue;
import com.shtick.utils.scratch3.runner.core.Opcode.DataType;
import com.shtick.utils.scratch3.runner.core.elements.Block;
import com.shtick.utils.scratch3.runner.core.elements.Script;
import com.shtick.utils.scratch3.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch3.runner.core.elements.TypedValue;
import com.shtick.utils.scratch3.runner.core.elements.control.CodeHeadBlock;
import com.shtick.utils.scratch3.runner.core.elements.control.JumpBlock;
import com.shtick.utils.scratch3.runner.core.elements.control.LocalVarBlock;
import com.shtick.utils.scratch3.runner.impl.ScratchRuntimeImplementation;

/**
 * @author sean.cox
 *
 */
public class ScriptImplementation implements Script{
	private ScriptContext context;
	private CloneableData cloneableData;
	private ScratchRuntimeImplementation runtime;

	/**
	 * @param context 
	 * @param blocks 
	 * @param headIdentifier 
	 * @param runtime 
	 * @throws InvalidScriptDefinitionException 
	 */
	public ScriptImplementation(ScriptContext context, Map<String, BlockImplementation> blocks, String headIdentifier, ScratchRuntimeImplementation runtime) throws InvalidScriptDefinitionException{
		super();
		this.context = context;
		this.cloneableData = new CloneableData();
		this.cloneableData.head = blocks.get(headIdentifier);
		this.runtime = runtime;
		Opcode headOpcode = runtime.getFeatureSet().getOpcode(this.cloneableData.head.getOpcode());
		resolveBlocksArgMap(this.cloneableData.head, headOpcode);
		resolveScript(blocks);
		Map<String,Object> registerArgs = this.cloneableData.head.getArgMap();
		if(registerArgs == null) {
			registerArgs = new HashMap<>();
		}
		else {
			registerArgs = new HashMap<>(registerArgs);
		}
		evaluateHeadArgs(registerArgs,context,blocks,runtime);
		((OpcodeHat)headOpcode).registerListeningScript(this, registerArgs, this.cloneableData.head.getMutation());
	}
	
	private static void evaluateHeadArgs(Map<String,Object> args, ScriptContext context, Map<String, BlockImplementation> blocks, ScratchRuntimeImplementation runtime) {
		for(String key:args.keySet()) {
			Object value = args.get(key);
			if(value instanceof TypedValueImplementation) {
				BlockImplementation valueBlock = blocks.get((String)((TypedValueImplementation)value).getValue().get(0));
				OpcodeValue valueOpcode = (OpcodeValue)runtime.getFeatureSet().getOpcode(valueBlock.getOpcode());
				Map<String,Object> arguments = valueBlock.getArgMap();
				if(arguments == null) {
					arguments = new HashMap<>();
				}
				else {
					arguments = new HashMap<>(arguments);
				}
				evaluateHeadArgs(arguments,context,blocks,runtime);
				args.put(key, valueOpcode.execute(runtime, null, context, arguments, valueBlock.getMutation()));
			}
		}
	}
	
	private ScriptImplementation(ScriptContext context, CloneableData cloneableData) {
		this.context = context;
		this.cloneableData = cloneableData;
	}
	
	/**
	 * 
	 * @return The topLevel Block.
	 */
	public BlockImplementation getHead() {
		return this.cloneableData.head;
	}

	@Override
	public Script clone(ScriptContext context) {
		return new ScriptImplementation(context,cloneableData);
	}
	
	/**
	 * 
	 * @return A representation of the script in which all control opcodes have been resolved.
	 */
	public Block[] getResolvedBlockTuples() {
		return cloneableData.resolvedBlocks;
	}

	/**
	 * 
	 * @return The number of local variables needed to run the resolved script.
	 */
	public int getLocalVariableCount() {
		return cloneableData.localVariableCount;
	}

	private void resolveScript(Map<String, BlockImplementation> blocks) throws InvalidScriptDefinitionException {
		LinkedList<Block> resolvedScript = new LinkedList<>();
		try {
			BlockImplementation currentBlock = blocks.get(cloneableData.head.getNext());
			
			int largestLocalVariableIndex = resolveScript(blocks, currentBlock,resolvedScript,0);
			cloneableData.resolvedBlocks = resolvedScript.toArray(new Block[resolvedScript.size()]);
			cloneableData.localVariableCount = largestLocalVariableIndex+1;
		}
		catch(InvalidScriptDefinitionException t) {
			throw new InvalidScriptDefinitionException(t.getMessage()+", hat="+cloneableData.head.getOpcode(),t);
		}
	}

	/**
	 * @param blockTuples
	 * @param resolvedScript
	 * @param startIndex
	 * @param firstAvailableLocalVar The next thread-local variable index available to this block of code.
	 * @return The index of the largest local variable used. (firstAvailableLocalVar-1 if no local variables were used.)
	 * @throws InvalidScriptDefinitionException
	 */
	private int resolveScript(Map<String, BlockImplementation> blocks, BlockImplementation currentBlock, LinkedList<Block> resolvedScript, int firstAvailableLocalVar) throws InvalidScriptDefinitionException {
		LinkedList<JumpBlock> jumpBlockTuples = new LinkedList<>();
		
		// Process block tuples, inflating control blocks.
		while(currentBlock!=null) {
			String opcode = currentBlock.getOpcode();
			Opcode opcodeImplementation = runtime.getFeatureSet().getOpcode(opcode);
			if(opcodeImplementation == null) {
				throw new InvalidScriptDefinitionException("Unrecognized opcode: "+opcode);
			}
			else if(opcodeImplementation instanceof OpcodeControl) {
				resolveBlocksArgMap(currentBlock,opcodeImplementation);
				Block[] resolvedControl = ((OpcodeControl)opcodeImplementation).execute(currentBlock.getArgMap());
				HashMap<Integer,Integer> localVariableMap = new HashMap<>();
				HashMap<Integer,Integer> localJumpMap = new HashMap<>(resolvedControl.length);
				LinkedList<JumpBlock> localJumpsToResolve = new LinkedList<>();
				for(int i=0;i<resolvedControl.length;i++) {
					localJumpMap.put(i, resolvedScript.size());
					if(resolvedControl[i] instanceof LocalVarBlock) {
						int localIndex = ((LocalVarBlock)resolvedControl[i]).getLocalVarIdentifier();
						if(!localVariableMap.containsKey(localIndex)) {
							localVariableMap.put(localIndex, firstAvailableLocalVar);
							firstAvailableLocalVar++;
						}
						((LocalVarBlock)resolvedControl[i]).setLocalVarIdentifier(localVariableMap.get(localIndex));
						resolvedScript.add(resolvedControl[i]);
					}
					else if(resolvedControl[i] instanceof JumpBlock) {
						resolvedScript.add(resolvedControl[i]);
						localJumpsToResolve.add((JumpBlock)resolvedControl[i]);
					}
					else if(resolvedControl[i] instanceof CodeHeadBlock) {
						BlockImplementation codeHead = blocks.get(((CodeHeadBlock)resolvedControl[i]).getBlockID());
						firstAvailableLocalVar = resolveScript(blocks,codeHead,resolvedScript,firstAvailableLocalVar)+1;
					}
					else{
						resolvedScript.add(resolvedControl[i]);
					}
				}
				for(JumpBlock jumpBlock:localJumpsToResolve) {
					if(!localJumpMap.containsKey(jumpBlock.getIndex())) {
						int index = jumpBlock.getIndex();
						if(index<0)
							index = 0;
						if(index>=resolvedControl.length)
							index = resolvedScript.size();
						else
							index = localJumpMap.get(0);
						jumpBlock.setIndex(index);
					}
					else {
						jumpBlock.setIndex(localJumpMap.get(jumpBlock.getIndex()));
					}
				}
			}
			else if(opcodeImplementation instanceof OpcodeHat) {
				throw new InvalidScriptDefinitionException("OpcodeHat found in the middle of a script. Context = "+context.getObjName());
			}
			else if(opcodeImplementation instanceof OpcodeValue) {
				throw new InvalidScriptDefinitionException("Attempted to execute value opcode. Opcode = "+opcode+", Context = "+context.getObjName());
			}
			else {
				resolveBlocksArgMap(currentBlock,opcodeImplementation);
				resolvedScript.add(currentBlock);
			}
			currentBlock = blocks.get(currentBlock.getNext());
		}
		return firstAvailableLocalVar-1;
	}
	
	private void resolveBlocksArgMap(BlockImplementation currentBlock, Opcode opcodeImplementation) {
		Map<String,DataType> types = opcodeImplementation.getArgumentTypes();
		Map<String, Input> inputs = currentBlock.getInputs();
		Map<String, String[]> fields = currentBlock.getFields();
		HashMap<String,Object> argMap = new HashMap<>(fields.size()+inputs.size());
		for(String key:fields.keySet())
			argMap.put(key, fields.get(key)[0]);
		for(String key:inputs.keySet()) {
			Input input = inputs.get(key);
			Object value;
			{
				TypedValueImplementation typedValue = (TypedValueImplementation)input.getRealValue();
				java.util.List<Object> typedValueParts = typedValue.getValue();
				
				if((typedValue.getType()>=1)&&(typedValue.getType()<=3)) {
					value = typedValue;
				}
				else if(typedValue.getType() == TypedValue.TYPE_BROADCAST){
					// TODO Probably I should be getting these items by the identifier, typedValueParts.get(1)
					String name = typedValueParts.get(0).toString();
					value = context.getContextBroadcastByName((String)name);
				}
				else if(typedValue.getType() == TypedValue.TYPE_LIST){
					String name = typedValueParts.get(0).toString();
					value = context.getContextListByName((String)name);
				}
				else if(typedValue.getType() == TypedValue.TYPE_VARIABLE){
					String name = typedValueParts.get(0).toString();
					value = context.getContextVariableByName((String)name);
				}
				else {
					value = typedValueParts.get(0).toString();
					// TODO It would probably be better to convert number/colors/etc up front.
				}
			}
			argMap.put(key, value);
		}
		currentBlock.setArgMap(argMap);
	}

	@Override
	public ScriptContext getContext() {
		return context;
	}
	
	private class CloneableData {
		private BlockImplementation head;
		private Block[] resolvedBlocks;
		private int localVariableCount;
	}
}
