/**
 * 
 */
package com.shtick.utils.scratch3.runner.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.shtick.utils.scratch3.runner.core.ScriptTupleRunner;
import com.shtick.utils.scratch3.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch3.runner.impl.elements.ScriptImplementation;

/**
 * Inspired by the AWT Event Dispatch Thread.
 * 
 * @author sean.cox
 *
 */
public class ScriptThread {
	private static final int DEFAULT_FRAME_DELAY_MS = 1000/60;
	// TODO Use the total number of scripts to determine a good number to use for starting size of this HashMap.
	private HashMap<ScriptImplementation,ScriptRunnable> taskQueue = new HashMap<>();
	private HashMap<ScriptImplementation,ScriptRunnable> scriptsToStart = new HashMap<>();
	private HashSet<ScriptImplementation> scriptsToStop = new HashSet<>();
	private HashSet<ScriptContext> contextsToStop = new HashSet<>();
	private Thread mainThread = new Thread(new MainProcess(), "Scratch Execution Thread");
	private boolean stopFlagged = false;
	private ScriptRunnable currentlyRunning;
	private ScratchRuntimeImplementation runtime;
	
	/**
	 * 
	 * @param runtime
	 */
	public ScriptThread(ScratchRuntimeImplementation runtime) {
		this.runtime = runtime;
	}
	
	/**
	 * Starts the script execution thread if it is not already running.
	 */
	public void start() {
		if(!mainThread.isAlive()) {
			mainThread.setDaemon(true);
			mainThread.start();
		}
	}
	
	/**
	 * 
	 * @return true if the current Thread is the mainThread for this TaskQueue.
	 */
	public boolean isQueueThread() {
		return Thread.currentThread()==mainThread;
	}
	
	/**
	 * 
	 * @param script
	 * @return Adds the given scriptTuple to the list of scripts to run.
	 */
	public ScriptTupleRunner startScript(ScriptImplementation script) {
		synchronized(scriptsToStart) {
			// A script added here will be terminated if it is already running. Eliminate the ambiguity of intention by overriding any current stop order.
			scriptsToStop.remove(script);
			ScriptRunnable runnable = new ScriptRunnable(script, runtime);
			scriptsToStart.put(script,runnable);
			return runnable.getScriptTupleRunner();
		}
	}
	
	/**
	 * Stops the specified script. (The entire call stack is aborted.)
	 * 
	 * @param script
	 */
	public void stopScript(ScriptImplementation script) {
		synchronized(scriptsToStart) {
			// A script set to start/restart will no longer be expected to start.
			scriptsToStart.remove(script);
			scriptsToStop.add(script);
			ScriptRunnable runnable = taskQueue.get(script);
			if(runnable!=null)
				runnable.flagStop(true);
		}
	}
	
	/**
	 * Stops all currently running scripts.
	 */
	public void stopAllScripts() {
		stopFlagged = true;
		synchronized(scriptsToStart) {
			scriptsToStart.clear();
			scriptsToStop.clear();
		}
	}
	
	/**
	 * Stops all scripts running under the given ScriptContext.
	 * 
	 * @param context A ScriptContext
	 */
	public void stopScriptsByContext(ScriptContext context) {
		synchronized(scriptsToStart) {
			contextsToStop.add(context);
			for(ScriptImplementation script:taskQueue.keySet()) {
				if(script.getContext()==context) {
					taskQueue.get(script).flagStop(true);
				}
			}
		}
	}
	
	/**
	 * @return the currentlyRunning
	 */
	public ScriptRunnable getCurrentlyRunning() {
		return currentlyRunning;
	}

	private class MainProcess implements Runnable{

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			long frameStartTime, delay;
			frameStartTime = System.currentTimeMillis();
			while(true) {
				synchronized(scriptsToStart) {
					// A script set to start/restart will no longer be expected to start.
					for(ScriptImplementation scriptToStop:scriptsToStart.keySet())
						taskQueue.remove(scriptToStop);
					for(ScriptImplementation scriptToStart:scriptsToStart.keySet())
						taskQueue.put(scriptToStart,scriptsToStart.get(scriptToStart));
					for(ScriptImplementation script:taskQueue.keySet())
						if(contextsToStop.contains(script.getContext()))
							scriptsToStop.add(script);
					for(ScriptImplementation scriptToStop:scriptsToStop)
						taskQueue.remove(scriptToStop);
					scriptsToStart.clear();
					scriptsToStop.clear();
					contextsToStop.clear();
				}
				
				Set<ScriptImplementation> keySet = taskQueue.keySet();
				Iterator<ScriptImplementation> iter = keySet.iterator();
				while(iter.hasNext()&&!stopFlagged) {
					ScriptImplementation key = iter.next();
					currentlyRunning = taskQueue.get(key);
					if(contextsToStop.contains(key.getContext())) {
						iter.remove();
						continue;
					}
					// The ScriptTupleRunnable should run until it yields, and then continue when called again.
					currentlyRunning.run();
					// If the runnable is done, then remove it from the taskQueue.
					if(currentlyRunning.isFinished())
						iter.remove();
				}
				if(stopFlagged) {
					taskQueue.clear();
					stopFlagged = false;
				}
				// Trigger painting if painting is needed.
				if(runtime.repaintStageFinal()) {
					delay = System.currentTimeMillis() - frameStartTime;
					if(delay<DEFAULT_FRAME_DELAY_MS) {
						// Current painting mechanism to only flag that painting should be triggered.
						// See: https://en.scratch-wiki.info/wiki/Single_Frame#Effects_of_Turbo_Speed
						try {
							Thread.sleep(DEFAULT_FRAME_DELAY_MS-delay);
						}
						catch(InterruptedException t) {
						}
					}
					frameStartTime = System.currentTimeMillis();
				}
			}
		}
	}
}
