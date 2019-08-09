/**
 * 
 */
package com.shtick.utils.scratch3.runner.impl.elements;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.shtick.utils.scratch3.runner.core.InvalidScriptDefinitionException;
import com.shtick.utils.scratch3.runner.core.Opcode;
import com.shtick.utils.scratch3.runner.core.OpcodeHat;
import com.shtick.utils.scratch3.runner.core.SoundMonitor;
import com.shtick.utils.scratch3.runner.core.ValueListener;
import com.shtick.utils.scratch3.runner.core.elements.Broadcast;
import com.shtick.utils.scratch3.runner.core.elements.Costume;
import com.shtick.utils.scratch3.runner.core.elements.List;
import com.shtick.utils.scratch3.runner.core.elements.RenderableChild;
import com.shtick.utils.scratch3.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch3.runner.core.elements.Target;
import com.shtick.utils.scratch3.runner.core.elements.Variable;
import com.shtick.utils.scratch3.runner.impl.ScratchRuntimeImplementation;
import com.shtick.utils.scratch3.runner.impl.ScriptThread;
import com.shtick.utils.scratch3.runner.impl.elements.CostumeImplementation;
import com.shtick.utils.scratch3.runner.impl.elements.SoundImplementation;
import com.shtick.utils.scratch3.runner.impl.elements.VariableImplementation;
import com.shtick.utils.scratch3.runner.impl.elements.CostumeImplementation.ImageAndArea;

/**
 * @author scox
 *
 */
public class TargetImplementation implements Target{
	private boolean isStage;
	private String name;
	private Map<String,ListImplementation> listsByName;
	private Map<String,BroadcastImplementation> broadcastsByIDs;
	private Map<String,BroadcastImplementation> broadcastsByName;
	private ScriptCollection scriptCollection;
	private Object comments;
	private SoundImplementation[] sounds;
	private Map<String,SoundImplementation> soundsByName;
	private CostumeImplementation[] costumes;
	private long currentCostumeIndex;
	private double volume;
	private long layerOrder;
	
	// Stage only
	private long tempo;
	private long videoTransparency;
	private String videoState;
	private String textToSpeechLanguage;
	
	// Non-stage only
	private boolean visible;
	private double x;
	private double y;
	private double size;
	private double direction;
	private boolean draggable;
	private String rotationStyle;
	
	private final Object LOCK = new Object();
	private ScratchRuntimeImplementation runtime;
	private Map<String,VariableImplementation> variablesByName;
	private HashMap<String,LinkedList<ValueListener>> varListeners = new HashMap<>();
	private LinkedList<SoundMonitor> activeSoundMonitors = new LinkedList<>();
	
	private TargetImplementation cloneOf;
	private HashSet<TargetImplementation> clones = new HashSet<>();
	private LinkedList<RenderableChild> children;
	private HashMap<String,Double> effects = new HashMap<>();

	/**
	 * 
	 * @param isStage
	 * @param name
	 * @param variables
	 * @param listsByName
	 * @param broadcastsByIDs
	 * @param blocks
	 * @param comments
	 * @param currentCostume
	 * @param costumes
	 * @param sounds
	 * @param volume
	 * @param layerOrder
	 * @param tempo
	 * @param videoTransparency
	 * @param videoState
	 * @param textToSpeechLanguage
	 * @param visible
	 * @param x
	 * @param y
	 * @param size
	 * @param direction
	 * @param draggable
	 * @param rotationStyle
	 * @param runtime
	 * @throws InvalidScriptDefinitionException 
	 */
	public TargetImplementation(boolean isStage, String name, java.util.List<VariableImplementation> variables, Map<String,ListImplementation> listsByName, Map<String,BroadcastImplementation> broadcastsByIDs,
			Map<String, BlockImplementation> blocks, Object comments, long currentCostume, CostumeImplementation[] costumes, SoundImplementation[] sounds, double volume,
			long layerOrder, long tempo, long videoTransparency, String videoState, String textToSpeechLanguage,
			boolean visible, double x, double y, double size, double direction, boolean draggable, String rotationStyle, ScratchRuntimeImplementation runtime) throws InvalidScriptDefinitionException{
		this.isStage = isStage;
		this.name = name;
		if(variables==null) {
			variablesByName = new HashMap<>();
		}
		else {
			variablesByName = new HashMap<>(variables.size());
			for(VariableImplementation variable:variables)
				variablesByName.put(variable.getName(), variable);
		}
		this.listsByName = listsByName;
		this.broadcastsByIDs = broadcastsByIDs;
		this.broadcastsByName = new HashMap<>(broadcastsByIDs.size());
		for(BroadcastImplementation broadcast:broadcastsByIDs.values())
			broadcastsByName.put(broadcast.getName(), broadcast);
		this.runtime = runtime;
		this.scriptCollection = new ScriptCollection(blocks,this,runtime);
		this.comments = comments;
		this.currentCostumeIndex = currentCostume;
		this.costumes = costumes;
		this.sounds = sounds;
		soundsByName = new HashMap<>(sounds.length);
		for(SoundImplementation sound:sounds)
			soundsByName.put(sound.getIdentifier(), sound);
		this.volume = volume;
		this.layerOrder = layerOrder;
		this.tempo = tempo;
		this.videoTransparency = videoTransparency;
		this.videoState = videoState;
		this.textToSpeechLanguage = textToSpeechLanguage;
		this.visible = visible;
		this.x = x;
		this.y = y;
		this.size = size;
		this.direction = direction;
		this.draggable = draggable;
		this.rotationStyle = rotationStyle;
		if(isStage) {
			this.children = new LinkedList<>();
		}
		else {
			this.children = null;
			registerWithCostumeImagePool();
		}
	}

	/**
	 * 
	 * @param isStage
	 * @param name
	 * @param variables
	 * @param lists
	 * @param broadcastsByIDs
	 * @param scriptCollection
	 * @param comments
	 * @param currentCostume
	 * @param costumes
	 * @param sounds
	 * @param volume
	 * @param layerOrder
	 * @param tempo
	 * @param videoTransparency
	 * @param videoState
	 * @param textToSpeechLanguage
	 * @param visible
	 * @param x
	 * @param y
	 * @param size
	 * @param direction
	 * @param draggable
	 * @param rotationStyle
	 * @param runtime
	 */
	private TargetImplementation(boolean isStage, String name, java.util.List<VariableImplementation> variables, Map<String,ListImplementation> lists, Map<String,BroadcastImplementation> broadcastsByIDs,
			ScriptCollection scriptCollection, Object comments, long currentCostume, CostumeImplementation[] costumes, SoundImplementation[] sounds, double volume,
			long layerOrder, long tempo, long videoTransparency, String videoState, String textToSpeechLanguage,
			boolean visible, double x, double y, double size, double direction, boolean draggable, String rotationStyle, ScratchRuntimeImplementation runtime) {
		this.isStage = isStage;
		this.name = name;
		if(variables==null) {
			variablesByName = new HashMap<>();
		}
		else {
			variablesByName = new HashMap<>(variables.size());
			for(VariableImplementation variable:variables)
				variablesByName.put(variable.getName(), variable);
		}
		this.listsByName = lists;
		this.broadcastsByIDs = broadcastsByIDs;
		for(BroadcastImplementation broadcast:broadcastsByIDs.values())
			broadcastsByName.put(broadcast.getName(), broadcast);
		this.runtime = runtime;
		this.scriptCollection = scriptCollection.clone(this);
		this.comments = comments;
		this.currentCostumeIndex = currentCostume;
		this.costumes = costumes;
		this.sounds = sounds;
		soundsByName = new HashMap<>(sounds.length);
		for(SoundImplementation sound:sounds)
			soundsByName.put(sound.getIdentifier(), sound);
		this.volume = volume;
		this.layerOrder = layerOrder;
		this.tempo = tempo;
		this.videoTransparency = videoTransparency;
		this.videoState = videoState;
		this.textToSpeechLanguage = textToSpeechLanguage;
		this.visible = visible;
		this.x = x;
		this.y = y;
		this.size = size;
		this.direction = direction;
		this.draggable = draggable;
		this.rotationStyle = rotationStyle;
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#getContextObject()
	 */
	@Override
	public ScriptContext getContextObject() {
		if(isStage) {
			return null;
		}
		return runtime.getCurrentStage();
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#getObjName()
	 */
	@Override
	public String getObjName() {
		return name;
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#getContextListByName(java.lang.String)
	 */
	@Override
	public List getContextListByName(String name) {
		if(listsByName.containsKey(name))
			return listsByName.get(name);
		if(isStage)
			return null;
		return runtime.getCurrentStage().getContextListByName(name);
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#getContextPropertyValueByName(java.lang.String)
	 */
	@Override
	public Object getContextPropertyValueByName(String name) throws IllegalArgumentException {
		switch(name) {
		case "background#":
			return currentCostumeIndex+1; // Translate from 0-index to 1-index
		case "volume":
			return volume;
		}
		return getContextVariableValueByName(name);
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#playSoundByName(java.lang.String)
	 */
	@Override
	public SoundMonitor playSoundByName(String soundName) {
		if(!soundsByName.containsKey(soundName))
			return null;
		SoundImplementation sound = soundsByName.get(soundName);
		return playSound(sound);
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#playSoundByIndex(int)
	 */
	@Override
	public SoundMonitor playSoundByIndex(int index) {
		if(index<0)
			return null;
		if(index>=sounds.length)
			return null;
		SoundImplementation sound = sounds[index];
		return playSound(sound);
	}

	private SoundMonitor playSound(SoundImplementation sound) {
		try {
			final SoundMonitor monitor = runtime.playSound(sound,volume);
			if(monitor!=null) {
				activeSoundMonitors.add(monitor);
				monitor.addCloseListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						activeSoundMonitors.remove(monitor);
					}
				});
				if(monitor.isDone())
					activeSoundMonitors.remove(monitor);
				return monitor;
			}
		}
		catch(Throwable t) {
			t.printStackTrace();
		}
		return new SoundMonitor() {
			
			@Override
			public boolean isDone() {
				return true;
			}

			@Override
			public void addCloseListener(ActionListener listener) {}

			@Override
			public void removeCloseListener(ActionListener listener) {}

			@Override
			public void setVolume(double volume) {}

			@Override
			public void stop() {}
		};
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#setVolume(double)
	 */
	@Override
	public void setVolume(double volume) {
		this.volume = volume;
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#getVolume()
	 */
	@Override
	public double getVolume() {
		return volume;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.RenderableChild#isVisible()
	 */
	@Override
	public boolean isVisible() {
		return visible;
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.RenderableChild#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Target#getCurrentCostume()
	 */
	@Override
	public Costume getCurrentCostume() {
		return costumes[(int)currentCostumeIndex];
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Target#getCurrentCostumeIndex()
	 */
	@Override
	public int getCurrentCostumeIndex() {
		return (int)currentCostumeIndex;
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Target#setCurrentCostumeIndex(int)
	 */
	@Override
	public boolean setCurrentCostumeIndex(int i) {
		if(currentCostumeIndex == i)
			return true;
		if((i<0)||(i>=costumes.length))
			return false;
		currentCostumeIndex = i;
		registerWithCostumeImagePool();
		runtime.repaintStage();
		return true;
	}
	

	@Override
	public boolean setCostumeByName(String name) {
		if(name==null)
			return false;
		for(int i=0;i<costumes.length;i++) {
			if(name.equals(costumes[i].getCostumeName())) {
				if(i==currentCostumeIndex)
					return true;
				costumes[(int)currentCostumeIndex].unregisterSprite(this);
				currentCostumeIndex = i;
				registerWithCostumeImagePool();
				runtime.repaintStage();
				return true;
			}
		}
		return false;
	}
	
	private void registerWithCostumeImagePool() {
		switch(getRotationStyle()) {
		case Target.ROTATION_STYLE_NORMAL:
			costumes[(int)currentCostumeIndex].registerSprite(this, size,((getDirection()-90+360)%360)*Math.PI/180,false);
			break;
		case Target.ROTATION_STYLE_LEFT_RIGHT:
			costumes[(int)currentCostumeIndex].registerSprite(this, size,0,getDirection()<0);
			break;
		default:
			costumes[(int)currentCostumeIndex].registerSprite(this, size,0,false);
			break;
		}
	}

	/**
	 * 
	 * @return An object containing the appropriate image, center, and bounds information for the identified sprite.
	 */
	public ImageAndArea getScaledAndRotatedImage() {
		return costumes[(int)currentCostumeIndex].getScaledAndRotatedImage(this);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Target#gotoXY(double, double)
	 */
	@Override
	public void gotoXY(double scratchX, double scratchY) {
		x = scratchX;
		y = scratchY;
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Target#getScratchX()
	 */
	@Override
	public double getScratchX() {
		return x;
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Target#setScratchX(double)
	 */
	@Override
	public void setScratchX(double scratchX) {
		x = scratchX;
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Target#getScratchY()
	 */
	@Override
	public double getScratchY() {
		return y;
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Target#setScratchY(double)
	 */
	@Override
	public void setScratchY(double scratchY) {
		y = scratchY;
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Target#getScale()
	 */
	@Override
	public double getSize() {
		return size;
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Target#setScale(double)
	 */
	@Override
	public void setSize(double size) {
		if(this.size == size)
			return;
		this.size = size;
		registerWithCostumeImagePool();
		runtime.repaintStage();
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Target#getDirection()
	 */
	@Override
	public double getDirection() {
		return direction;
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Target#setDirection(double)
	 */
	@Override
	public void setDirection(double direction) {
		direction%=360;
		if(direction>180)
			direction-=360;
		else if(direction<-180)
			direction+=360;
		if(this.direction == direction)
			return;
		this.direction = direction;
		registerWithCostumeImagePool();
		runtime.repaintStage();
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Target#getRotationStyle()
	 */
	@Override
	public String getRotationStyle() {
		return rotationStyle;
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Target#setRotationStyle(java.lang.String)
	 */
	@Override
	public void setRotationStyle(String rotationStyle) {
		if(this.rotationStyle.equals(rotationStyle))
			return;
		this.rotationStyle = rotationStyle;
		registerWithCostumeImagePool();
		runtime.repaintStage();
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Target#isClone()
	 */
	@Override
	public boolean isClone() {
		return cloneOf != null;
	}


	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#stopScripts()
	 */
	@Override
	public void stopScripts() {
		if(isStage()) {
			runtime.stopAllSounds();
			synchronized(children) {
				for(Object child:children) {
					if(!(child instanceof TargetImplementation))
						continue;
					((TargetImplementation)child).stopScripts();
				}
			}
		}
		ScriptThread scriptTupleThread = runtime.getScriptTupleThread();
		scriptTupleThread.stopScriptsByContext(this);
	}


	/**
	 * @return the isStage
	 */
	public boolean isStage() {
		return isStage;
	}


	@Override
	public Object getContextVariableValueByName(String name) throws IllegalArgumentException {
		if(variablesByName.containsKey(name))
			return variablesByName.get(name).getValue();
		if(isStage)
			return null;
		return runtime.getCurrentStage().getContextVariableValueByName(name);
	}

	@Override
	public void setContextVariableValueByName(String name, Object value) throws IllegalArgumentException {
		synchronized(LOCK) {
			if(variablesByName.containsKey(name)) {
				VariableImplementation variable = variablesByName.get(name);
				final Object old = variable.getValue();
				variable.setValue(value);
				if(old.equals(value))
					return;
				SwingUtilities.invokeLater(()->{
					synchronized(LOCK) {
						LinkedList<ValueListener> valueListeners = varListeners.get(name);
						if(valueListeners == null)
							return;
						for(ValueListener listener:valueListeners)
							listener.valueUpdated(old, value);
					}
				});
				return;
			}
			if(!isStage)
				return;
			try {
				runtime.getCurrentStage().setContextVariableValueByName(name, value);
			}
			catch(IllegalArgumentException t) {
				throw new IllegalArgumentException("No veriable with the name, "+name+", can be found on the sprite, "+this.name+".", t);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core3.elements.ScriptContext#getContextVariableByName(java.lang.String)
	 */
	@Override
	public Variable getContextVariableByName(String name) {
		if(variablesByName.containsKey(name))
			return variablesByName.get(name);
		if(isStage)
			return null;
		return runtime.getCurrentStage().getContextVariableByName(name);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core3.elements.ScriptContext#getContextBroadcastByName(java.lang.String)
	 */
	@Override
	public Broadcast getContextBroadcastByName(String name) {
		if(broadcastsByName.containsKey(name))
			return broadcastsByName.get(name);
		if(isStage)
			return null;
		return runtime.getCurrentStage().getContextBroadcastByName(name);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#addVariableListener(java.lang.String, com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void addVariableListener(String var, ValueListener listener) {
		synchronized(LOCK) {
			if(!variablesByName.containsKey(var)) {
				if(!isStage)
					runtime.getCurrentStage().addVariableListener(var, listener);
				return;
			}
			if(!varListeners.containsKey(var))
				varListeners.put(var,new LinkedList<>());
			varListeners.get(var).add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#removeVariableListener(java.lang.String, com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void removeVariableListener(String var, ValueListener listener) {
		synchronized(LOCK) {
			if(!varListeners.containsKey(var)) {
				if(!isStage)
					runtime.getCurrentStage().addVariableListener(var, listener);
				return;
			}
			varListeners.get(var).remove(listener);
			if(varListeners.get(var).isEmpty())
				varListeners.remove(var);
		}
	}
	
	/**
	 * 
	 * @param child
	 */
	public void addChild(RenderableChild child) {
		synchronized(children) {
			if((child instanceof Target)&&((Target)child).isClone()) {
				Target cloneParent = ((Target)child).getCloneParent();
				int i = children.indexOf(cloneParent);
				children.add(i, child);
			}
			else {
				children.add(child);
			}
		}
	}
	
	/**
	 * 
	 * @param child
	 * @return true if the child existed and was removed, and false otherwise.
	 */
	public boolean removeChild(RenderableChild child) {
		synchronized(children) {
			return children.remove(child);
		}
	}
	
	/**
	 * 
	 * @return An array of RenderableChild objects.
	 */
	public RenderableChild[] getAllRenderableChildren() {
		return children.toArray(new RenderableChild[children.size()]);
	}
	
	/**
	 * 
	 * @param target
	 * @return true if the target was found and false if not found.
	 */
	public boolean bringToFront(Target target) {
		if(!children.contains(target))
			return false;
		children.remove(target);
		children.add(target);
		return true;
	}
	
	/**
	 * 
	 * @param target
	 * @return true if the target was found and false if not found.
	 */
	public boolean sendToBack(Target target) {
		if(!children.contains(target))
			return false;
		children.remove(target);
		children.addFirst(target);
		return true;
	}

	/**
	 * 
	 * @param target
	 * @param n
	 * @return true if the target was found and false if not found.
	 */
	public boolean sendBackNLayers(Target target, int n) {
		int i = children.indexOf(target);
		if(i<0)
			return false;
		i-=n;
		children.remove(target);
		children.add(Math.min(Math.max(0, i),children.size()), target);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#createClone()
	 */
	@Override
	public void createClone() {
		if(isStage)
			return;
		synchronized(LOCK) {
			synchronized(this.listsByName) {
				ArrayList<VariableImplementation> variables = new ArrayList<>(variablesByName.size());
				for(VariableImplementation variable:variablesByName.values())
					variables.add(new VariableImplementation(variable.getID(), variable.getValue(),variable.getName()));
	
				HashMap<String, ListImplementation> listsByName = new HashMap<>(this.listsByName.size());
				for(String name:this.listsByName.keySet()) {
					try {
						listsByName.put(name,  (ListImplementation)this.listsByName.get(name).clone());
					}
					catch(CloneNotSupportedException t) {
						throw new RuntimeException(t);
					}
				}
	
				TargetImplementation clone = new TargetImplementation(
						false,
						name,
						variables,
						listsByName,
						broadcastsByIDs,
						scriptCollection,
						comments,
						currentCostumeIndex,
						costumes,
						sounds,
						volume,
						layerOrder,
						tempo,
						videoTransparency,
						videoState,
						textToSpeechLanguage,
						visible,
						x,
						y,
						size,
						direction,
						draggable,
						rotationStyle,
						runtime);
				
				// Clone properties that aren't part of an initial definition in a project file
				clone.cloneOf = this;
				
				runtime.addClone(clone);
				clones.add(clone);
				for(ScriptImplementation script:clone.scriptCollection.getScripts()) {
					if("control_start_as_clone".equals(script.getHead().getOpcode()))
						runtime.startScript(script);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core3.elements.Target#getCloneParent()
	 */
	@Override
	public Target getCloneParent() {
		return cloneOf;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core3.elements.Target#deleteClone()
	 */
	@Override
	public void deleteClone() {
		if(!isClone()) {
			System.err.println("WARNING: deleteClone() called on non-clone.");
			return;
		}
		// Set clone scripts.
		stopScripts();
		costumes[(int)currentCostumeIndex].unregisterSprite(this);
		for(ScriptImplementation script:scriptCollection.getScripts()) {
			BlockImplementation maybeHat = script.getHead();
			String opcode = maybeHat.getOpcode();
			Opcode opcodeImplementation = runtime.getFeatureSet().getOpcode(opcode);
			if((opcodeImplementation != null)&&(opcodeImplementation instanceof OpcodeHat)) {
				((OpcodeHat)opcodeImplementation).unregisterListeningScript(script, maybeHat.getArgMap(),maybeHat.getMutation());
			}
		}
		cloneOf.clones.remove(this);
		runtime.deleteClone(this);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core3.elements.Target#getClones()
	 */
	@Override
	public Set<Target> getClones() {
		HashSet<Target> retval = new HashSet<>(clones);
		for(TargetImplementation clone:clones)
			retval.addAll(clone.getClones());
		return retval;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core3.elements.Target#getCostumeCount()
	 */
	@Override
	public int getCostumeCount() {
		return costumes.length;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core3.elements.Target#getEffect(java.lang.String)
	 */
	@Override
	public double getEffect(String name) {
		return effects.containsKey(name)?effects.get(name):0;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core3.elements.Target#setEffect(java.lang.String, double)
	 */
	@Override
	public void setEffect(String name, double value) {
		effects.put(name, value);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core3.elements.Target#clearEffects()
	 */
	@Override
	public void clearEffects() {
		effects.clear();
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<String,Double> getEffects() {
		return effects;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core3.elements.Target#getShape()
	 */
	@Override
	public Area getShape() {
		if(!visible)
			return new Area();
		ImageAndArea imageAndArea = costumes[(int)currentCostumeIndex]
				.getScaledAndRotatedImage(this);
		if(imageAndArea==null)
			return new Area();
		return (Area)imageAndArea
				.getCostumeArea()
				.clone(); // Don't give the original to external entities. (Might have to simply depend on external entities to play nice if this becomes an important performance issue.)
	}
}
