/**
 * 
 */
package com.shtick.utils.scratch3.runner.impl;

import java.util.HashMap;
import java.util.Stack;

import com.shtick.utils.scratch3.runner.core.InvalidScriptDefinitionException;
import com.shtick.utils.scratch3.runner.core.Opcode;
import com.shtick.utils.scratch3.runner.core.Opcode.DataType;
import com.shtick.utils.scratch3.runner.core.OpcodeAction;
import com.shtick.utils.scratch3.runner.core.OpcodeSubaction;
import com.shtick.utils.scratch3.runner.core.OpcodeUtils;
import com.shtick.utils.scratch3.runner.core.OpcodeValue;
import com.shtick.utils.scratch3.runner.core.ScriptTupleRunner;
import com.shtick.utils.scratch3.runner.core.elements.Block;
import com.shtick.utils.scratch3.runner.core.elements.Broadcast;
import com.shtick.utils.scratch3.runner.core.elements.List;
import com.shtick.utils.scratch3.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch3.runner.core.elements.Variable;
import com.shtick.utils.scratch3.runner.core.elements.control.BasicJumpBlock;
import com.shtick.utils.scratch3.runner.core.elements.control.ChangeLocalVarByBlock;
import com.shtick.utils.scratch3.runner.core.elements.control.ControlBlock;
import com.shtick.utils.scratch3.runner.core.elements.control.FalseJumpBlock;
import com.shtick.utils.scratch3.runner.core.elements.control.JumpBlock;
import com.shtick.utils.scratch3.runner.core.elements.control.LocalVarBlock;
import com.shtick.utils.scratch3.runner.core.elements.control.ReadLocalVarBlock;
import com.shtick.utils.scratch3.runner.core.elements.control.SetLocalVarBlock;
import com.shtick.utils.scratch3.runner.core.elements.control.TestBlock;
import com.shtick.utils.scratch3.runner.core.elements.control.TrueJumpBlock;
import com.shtick.utils.scratch3.runner.impl.elements.BlockImplementation;
import com.shtick.utils.scratch3.runner.impl.elements.ScriptImplementation;

/**
 * @author sean.cox
 *
 */
public class ScriptRunnable implements Runnable {
	private ScriptImplementation scriptTuple;
	private Opcode currentOpcode = null;
	private boolean testResult;
	
	private Stack<YieldingScript> callStack = new Stack<>();
	private OpcodeSubaction yieldCheck = null;

	private final Object STOP_LOCK = new Object();
	/**
	 * Used to flag that the currently running script (top of the stack) should be aborted.
	 */
	private boolean stopProcedure = false;
	/**
	 * Used to flag that the entire call stack needs to be aborted, rather than just the currently running script on the top of the stack.
	 */
	private boolean totalStop = false;
	private boolean stopped = false;
	/**
	 * Indicated that the script is actively executing now. (ie. false if yielded or stopped)
	 */
	private boolean running = false;

	private ScriptTupleRunnerImpl scriptRunner;
	private ScratchRuntimeImplementation runtime;

	/**
	 * @param scriptTuple
	 * @param runtime 
	 */
	public ScriptRunnable(ScriptImplementation scriptTuple, ScratchRuntimeImplementation runtime) {
		this.scriptTuple = scriptTuple;
		this.runtime = runtime;
		callStack.push(new YieldingScript(scriptTuple.getContext(), scriptTuple.getResolvedBlockTuples(), scriptTuple.getLocalVariableCount(), false, ""));
		this.scriptRunner = new ScriptTupleRunnerImpl(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			runBlockTuples();
		}
		catch(InvalidScriptDefinitionException t) {
			throw new RuntimeException(t);
		}
	}
	
	/**
	 * 
	 * @return A String describing where the process is currently, with newlines separating stack layers.
	 */
	public String getStackTrace() {
		String retval = scriptTuple.getContext().getObjName();
		int i = 0;
		synchronized(callStack) {
			for(YieldingScript task:callStack) {
				i++;
				retval+="\n"+i+": "+task.description+" "+task.index+"/"+task.blocks.length+" "+((task.index>=task.blocks.length)?"Done":task.blocks[task.index].getOpcode())+" "+task.isAtomic;
			}
		}
		return retval;
	}
	
	/**
	 * 
	 * @param totalStop If true, then the entire call stack is aborted. Otherwise, only the top procedure in the stack aborts and returns.
	 */
	public void flagStop(boolean totalStop) {
		this.totalStop = totalStop;
		stopProcedure=true;
		if(totalStop) {
			new Thread(()->{
				synchronized(STOP_LOCK) {
					while(running) {
						try {
							STOP_LOCK.wait(100);
						}
						catch(InterruptedException t) {}
					}
					if(!stopped) {
						stopped = true;
						STOP_LOCK.notifyAll();
						scriptRunner.runnable = null;
					}
				}
			}).start();
		}
	}
	
	/**
	 * 
	 * @return true if the script being run has completed, and false otherwise.
	 */
	public boolean isFinished() {
		return stopped;
	}
	
	/**
	 * 
	 * @param context
	 * @param blockTuples The results of resolveScript(), so this shouldn't include any ControlBlockTuple elements.
	 * @throws InvalidScriptDefinitionException
	 */
	private void runBlockTuples() throws InvalidScriptDefinitionException{
		if(callStack.size()==0)
			return;
		
		// Handle yieldCheck.
		if(yieldCheck!=null) {
			if(yieldCheck.shouldYield())
				return;
			yieldCheck = null;
		}
		
		try {
			synchronized(STOP_LOCK) {
				running = true;
			}
		
			// Run the next script segment.
			while((callStack.size()>0)&&(!totalStop)) {
				YieldingScript yieldingScript = callStack.peek();
				while((yieldingScript.index<yieldingScript.blocks.length)&&(!stopProcedure)) {
					Block tuple = yieldingScript.blocks[yieldingScript.index];
					if(yieldingScript.debugFlag)
						System.out.println("Index: "+yieldingScript.index+"/"+yieldingScript.blocks.length+" - "+tuple.getOpcode());
					if(tuple instanceof ControlBlock) {
						if(tuple instanceof TestBlock) {
							testResult = (Boolean)getValue(yieldingScript.context,((TestBlock) tuple).getTest(),yieldingScript.localVariables);
							yieldingScript.index++;
							continue;
						}
						else if(tuple instanceof SetLocalVarBlock) {
							yieldingScript.localVariables[((LocalVarBlock)tuple).getLocalVarIdentifier()] = ((SetLocalVarBlock)tuple).getValue();
							yieldingScript.index++;
							continue;
						}
						else if(tuple instanceof ChangeLocalVarByBlock) {
							long change = ((ChangeLocalVarByBlock)tuple).getValue();
							yieldingScript.localVariables[((LocalVarBlock)tuple).getLocalVarIdentifier()] += change;
							yieldingScript.index++;
							continue;
						}
						if(tuple instanceof BasicJumpBlock) {
							if((!yieldingScript.isAtomic)&&(((JumpBlock)tuple).getIndex()<yieldingScript.index)) {
								// If the new index is before the old index, then yield after updating the index, unless this function is atomic.
								yieldingScript.index = ((JumpBlock)tuple).getIndex();
								return;
							}
							yieldingScript.index = ((JumpBlock)tuple).getIndex();
							continue;
						}
						else if(tuple instanceof TrueJumpBlock) {
							if(testResult) {
								if((!yieldingScript.isAtomic)&&(((JumpBlock)tuple).getIndex()<yieldingScript.index)) {
									// If the new index is before the old index, then yield after updating the index, unless this function is atomic.
									yieldingScript.index = ((JumpBlock)tuple).getIndex();
									return;
								}
								yieldingScript.index = ((JumpBlock)tuple).getIndex();
								continue;
							}
						}
						else if(tuple instanceof FalseJumpBlock) {
							if(!testResult) {
								if((!yieldingScript.isAtomic)&&(((JumpBlock)tuple).getIndex()<yieldingScript.index)) {
									// If the new index is before the old index, then yield after updating the index, unless this function is atomic.
									yieldingScript.index = ((JumpBlock)tuple).getIndex();
									return;
								}
								yieldingScript.index = ((JumpBlock)tuple).getIndex();
								continue;
							}
						}
						else {
							throw new InvalidScriptDefinitionException("Unrecognized control block: "+tuple.getClass().getCanonicalName());
						}
						yieldingScript.index++;
						continue;
					}
					// TODO Move type/safety checking below to resolveScript. (Opcode value implementations will need to report a return type.)
					//      The type checking at that point would be less comprehensive, probably, but this seems to be the direction I need to go to improve performance.
					String opcode = tuple.getOpcode();
					java.util.Map<String,Object> arguments = ((BlockImplementation)tuple).getArgMap();
					Opcode opcodeImplementation = runtime.getFeatureSet().getOpcode(opcode);
					currentOpcode = opcodeImplementation;
					java.util.Map<String,DataType> types = opcodeImplementation.getArgumentTypes();
					java.util.Map<String,Object> executableArguments = new HashMap<>(Math.max(arguments.size(),types.size()));
					for(String paramName:types.keySet()) {
						Object executableArgument;
						switch(types.get(paramName)) {
						case BOOLEAN:
							executableArgument = getValue(yieldingScript.context,arguments.get(paramName),yieldingScript.localVariables);
							if(!(executableArgument instanceof Boolean))
								throw new InvalidScriptDefinitionException("Non-tuple provided where tuple expected.");
							break;
						case NUMBER:
							executableArgument = OpcodeUtils.getNumericValue(getValue(yieldingScript.context,arguments.get(paramName),yieldingScript.localVariables));
							break;
						case OBJECT:
							executableArgument = getValue(yieldingScript.context,arguments.get(paramName),yieldingScript.localVariables);
							if(!((executableArgument instanceof Boolean)||(executableArgument instanceof Number)||(executableArgument instanceof String)))
								throw new InvalidScriptDefinitionException("Non-object provided where object expected.");
							break;
						case STRING:
							executableArgument = OpcodeUtils.getStringValue(getValue(yieldingScript.context,arguments.get(paramName),yieldingScript.localVariables));
							break;
						default:
							throw new RuntimeException("Unhandled DataType, "+types.get(paramName).name()+", in method signature for opcode, "+opcode);
						}
						executableArguments.put(paramName, executableArgument);
					}
					OpcodeSubaction subaction = ((OpcodeAction)opcodeImplementation).execute(runtime, scriptRunner, yieldingScript.context, executableArguments);
					yieldingScript.index++;
					if(subaction!=null) {
						switch(subaction.getType()) {
						case YIELD_CHECK:
							if(!yieldingScript.isAtomic) {
								yieldCheck = subaction;
								return;
							}
							break;
						case SUBSCRIPT:
							synchronized(callStack){
								if(!stopProcedure) {
									callStack.push(new YieldingScript(subaction.getSubscript().getContext(), ((ScriptImplementation)subaction.getSubscript()).getResolvedBlockTuples(), ((ScriptImplementation)subaction.getSubscript()).getLocalVariableCount(), subaction.isSubscriptAtomic(), ""));
								}
							}
							if(!yieldingScript.isAtomic) {
								return;
							}
							yieldingScript = callStack.peek();
							break;
						}
					}
				}
				currentOpcode = null;
				synchronized(callStack){
					callStack.pop();
				}
				stopProcedure = false;
				if(callStack.size() > 0) {
					yieldingScript = callStack.peek();
					if(!yieldingScript.isAtomic)
						return;
				}
			}

			synchronized(STOP_LOCK) {
				stopped = true;
				STOP_LOCK.notifyAll();
				scriptRunner.runnable = null;
			}
		}
		finally{
			synchronized(STOP_LOCK) {
				running = false;
			}
		}
	}
	
	private Object getValue(ScriptContext context, Object object, long[] localVariables) throws InvalidScriptDefinitionException {
		if(object instanceof Block)
			return getBlockTupleValue(context, (Block)object, localVariables);
		return object;
	}
	
	private Object getBlockTupleValue(ScriptContext context, Block tuple, long[] localVariables) throws InvalidScriptDefinitionException{
		if(tuple instanceof ReadLocalVarBlock)
			return localVariables[((ReadLocalVarBlock)tuple).getLocalVarIdentifier()];
		java.util.Map<String,Object> arguments = ((BlockImplementation)tuple).getArgMap();
		Opcode opcodeImplementation = getOpcode(tuple);
		if(opcodeImplementation == null)
			throw new InvalidScriptDefinitionException("Unrecognized value resolving opcode: "+tuple.getOpcode());
		if(!(opcodeImplementation instanceof OpcodeValue))
			throw new InvalidScriptDefinitionException("Attempted to evaluate non-value opcode: "+tuple.getOpcode());
		java.util.Map<String,DataType> types = opcodeImplementation.getArgumentTypes();
		if(types.size()!= arguments.size())
			throw new InvalidScriptDefinitionException("Invalid arguments found for opcode, "+tuple.getOpcode());
		java.util.Map<String,Object> executableArguments = new HashMap<>(arguments.size());
		for(String paramName:types.keySet()) {
			DataType type = types.get(paramName);
			if(type == null)
				throw new InvalidScriptDefinitionException("Invalid argument, "+paramName+", found for opcode, "+tuple.getOpcode());
			Object executableArgument;
			if(type==Opcode.DataType.POINTER_META)
				executableArgument = arguments.get(paramName);
			else if(type==Opcode.DataType.POINTER_BROADCAST) {
				executableArgument = arguments.get(paramName);
				if(!(executableArgument instanceof Broadcast)) {
					executableArgument=runtime.getObjectByIdentifier(executableArgument.toString());
					if(!(executableArgument instanceof Broadcast))
						executableArgument = null;
				}
			}
			else if(type==Opcode.DataType.POINTER_LIST) {
				executableArgument = arguments.get(paramName);
				if(!(executableArgument instanceof List)) {
					executableArgument=runtime.getObjectByIdentifier(executableArgument.toString());
					if(!(executableArgument instanceof List))
						executableArgument = null;
				}
			}
			else if(type==Opcode.DataType.POINTER_META) {
				executableArgument = arguments.get(paramName).toString();
			}
			else if(type==Opcode.DataType.POINTER_VARIABLE) {
				executableArgument = arguments.get(paramName);
				if(!(executableArgument instanceof Variable)) {
					executableArgument=runtime.getObjectByIdentifier(executableArgument.toString());
					if(!(executableArgument instanceof Variable))
						executableArgument = null;
				}
			}
			else
				executableArgument = getValue(context,arguments.get(paramName), localVariables);
			switch(type) {
			case BOOLEAN:
				if(!(executableArgument instanceof Boolean))
					throw new InvalidScriptDefinitionException("Non-boolean provided where boolean expected: "+executableArgument);
				break;
			case NUMBER:
				executableArgument = OpcodeUtils.getNumericValue(executableArgument);
				break;
			case OBJECT:
				if(!((executableArgument instanceof Boolean)||(executableArgument instanceof Number)||(executableArgument instanceof String)))
					throw new InvalidScriptDefinitionException("Non-object provided where object expected.");
				break;
			case STRING:
				executableArgument = OpcodeUtils.getStringValue(executableArgument);
				break;
			default:
				throw new RuntimeException("Unhandled DataType, "+type.name()+", in method signature for opcode, "+opcodeImplementation.getOpcode());
			}
			executableArguments.put(paramName, executableArgument);
		}
		Object retval = ((OpcodeValue)opcodeImplementation).execute(runtime, scriptRunner, context, executableArguments, tuple.getMutation());
		return retval;
	}
	
	private Opcode getOpcode(Block blockTuple) {
		return runtime.getFeatureSet().getOpcode(blockTuple.getOpcode());
	}

	/**
	 * 
	 * @return A ScriptTupleRunner object for accessing this Thread.
	 */
	public ScriptTupleRunner getScriptTupleRunner() {
		return scriptRunner;
	}
	
	private static class YieldingScript{
		public ScriptContext context;
		public Block[] blocks;
		public long[] localVariables;
		public int index;
		public boolean isAtomic;
		public String description;
		public boolean debugFlag = false;
		
		public YieldingScript(ScriptContext context, Block[] blocks, int localVarCount, boolean isAtomic, String description) {
			super();
			this.context = context;
			this.blocks = blocks;
			this.isAtomic = isAtomic;
			this.localVariables = new long[localVarCount];
			this.description = description;
			index = 0;
		}
	}
	
	/**
	 * A class for helping to encapsulate the thread. (Not completely doable, since the thread is always accessible as Thread.currentThread())
	 * 
	 * @author sean.cox
	 *
	 */
	private static class ScriptTupleRunnerImpl implements ScriptTupleRunner{
		private ScriptRunnable runnable;
		
		/**
		 * @param runnable
		 */
		public ScriptTupleRunnerImpl(ScriptRunnable runnable) {
			super();
			this.runnable = runnable;
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.ScriptTupleRunner#flagStop()
		 */
		@Override
		public void flagStop() {
			if(runnable!=null)
				runnable.flagStop(false);
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.ScriptTupleRunner#isStopFlagged()
		 */
		@Override
		public boolean isStopFlagged() {
			return (runnable!=null)||runnable.stopProcedure;
		}

		@Override
		public boolean isStopped() {
			return (runnable==null)||runnable.stopped;
		}

		@Override
		public ScriptContext getContext() {
			return runnable.scriptTuple.getContext();
		}

		/* (non-Javadoc)
		 * @see com.shtick.utils.scratch.runner.core.ScriptTupleRunner#getCurrentOpcode()
		 */
		@Override
		public Opcode getCurrentOpcode() {
			return runnable.currentOpcode;
		}

		@Override
		public String getStackTrace() {
			return runnable.getStackTrace();
		}
	}
}
