package com.shtick.utils.scratch3.runner.impl;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.Manager;
import javax.media.Player;
import javax.media.Time;
import javax.media.protocol.DataSource;
import javax.media.protocol.InputSourceStream;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.shtick.util.tokenizers.json.NumberToken;
import com.shtick.utils.data.json.JSONDecoder;
import com.shtick.utils.data.json.JSONNumberDecoder;
import com.shtick.utils.scratch3.runner.core.FeatureSet;
import com.shtick.utils.scratch3.runner.core.InvalidScriptDefinitionException;
import com.shtick.utils.scratch3.runner.core.Opcode;
import com.shtick.utils.scratch3.runner.core.OpcodeHat;
import com.shtick.utils.scratch3.runner.core.ScriptTupleRunner;
import com.shtick.utils.scratch3.runner.core.SoundMonitor;
import com.shtick.utils.scratch3.runner.core.elements.RenderableChild;
import com.shtick.utils.scratch3.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch3.runner.core.elements.Script;
import com.shtick.utils.scratch3.runner.core.elements.StageMonitor;
import com.shtick.utils.scratch3.runner.core.elements.Target;
import com.shtick.utils.scratch3.ScratchFile;
import com.shtick.utils.scratch3.ScratchImageRenderer;
import com.shtick.utils.scratch3.runner.common.BubbleImage;
import com.shtick.utils.scratch3.runner.common.ClipSoundMonitor;
import com.shtick.utils.scratch3.runner.common.JMFSoundMonitor;
import com.shtick.utils.scratch3.runner.common.ScratchRuntimeCommon;
import com.shtick.utils.scratch3.runner.common.ThreadTaskQueue;
import com.shtick.utils.scratch3.runner.common.ui.StagePanel;
import com.shtick.utils.scratch3.runner.impl.elements.BlockImplementation;
import com.shtick.utils.scratch3.runner.impl.elements.BroadcastImplementation;
import com.shtick.utils.scratch3.runner.impl.elements.CostumeImplementation;
import com.shtick.utils.scratch3.runner.impl.elements.Input;
import com.shtick.utils.scratch3.runner.impl.elements.ListImplementation;
import com.shtick.utils.scratch3.runner.impl.elements.MutationImplementation;
import com.shtick.utils.scratch3.runner.impl.elements.ScriptImplementation;
import com.shtick.utils.scratch3.runner.impl.elements.SoundImplementation;
import com.shtick.utils.scratch3.runner.impl.elements.StageMonitorImplementation;
import com.shtick.utils.scratch3.runner.impl.elements.TargetImplementation;
import com.shtick.utils.scratch3.runner.impl.elements.TypedValueImplementation;
import com.shtick.utils.scratch3.runner.impl.elements.VariableImplementation;

/**
 * @author sean.cox
 *
 */
public class ScratchRuntimeImplementation extends ScratchRuntimeCommon {
	private static final String RESOURCE_PROJECT_FILE = "project.json";
	private final ThreadTaskQueue TASK_QUEUE = new ThreadTaskQueue();
	private final ScriptThread SCRIPT_TUPLE_THREAD = new ScriptThread(this);
	
	/**
	 * Map of named (non-clone) sprites.
	 */
	private Map<String, TargetImplementation> targetsByName;
	private TargetImplementation stage;
	private Collection<TargetImplementation> targets;
	private LinkedList<StageMonitorImplementation> stageMonitors = new LinkedList<>();
	/**
	 * Object can be a Variable, Broadcast, Block, or List.
	 */
	private Map<String, Object> objectsByIdentifier;
	private Map<String, Object> meta;

	private int stageWidth;
	private int stageHeight;
	private boolean repaintNeeded = true;
	
	// Mouse properties
	private final Object MOUSE_LOCK = new Object();
	private double mouseStageX;
	private double mouseStageY;
	private LinkedList<MouseListener> stageMouseListeners = new LinkedList<>();
	private LinkedList<MouseMotionListener> stageMouseMotionListeners = new LinkedList<>();
	
	private boolean running = false;
	private boolean stopped = false;
	private BubbleImage bubbleImage;
	private ScratchFile scratchFile;
	private HashSet<SoundMonitor> activeSoundMonitors = new HashSet<>(10);
	private HashSet<Integer> pressedKeys = new HashSet<>(10);
	private boolean mouseDown = false;
	private StagePanel stagePanel;
	
	private MouseMotionListener primaryStageMouseMotionListener = new MouseMotionListener() {
		@Override
		public synchronized void mouseMoved(MouseEvent e) {
			final MouseEvent stageEvent = getStageMouseEventAndProcess(e);
			SwingUtilities.invokeLater(()->{
				synchronized(stageMouseMotionListeners) {
					for(MouseMotionListener listener:stageMouseMotionListeners)
						listener.mouseMoved(stageEvent);
				}
			});
		}
		
		@Override
		public synchronized void mouseDragged(MouseEvent e) {
			final MouseEvent stageEvent = getStageMouseEventAndProcess(e);
			SwingUtilities.invokeLater(()->{
				synchronized(stageMouseMotionListeners) {
					for(MouseMotionListener listener:stageMouseMotionListeners)
						listener.mouseDragged(stageEvent);
				}
			});
		}
		
		private MouseEvent getStageMouseEventAndProcess(MouseEvent e) {
			synchronized(MOUSE_LOCK) {
				mouseStageX = e.getX()*stageWidth/stagePanel.getWidth()-stageWidth/2;
				mouseStageY = -e.getY()*stageHeight/stagePanel.getHeight()+stageHeight/2;

				return new MouseEvent((Component)e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), (int)mouseStageX, (int)mouseStageY, e.getClickCount(), e.isPopupTrigger());
			}
		}
	};
	
	private MouseListener primaryStageMouseListener = new MouseListener() {
		
		@Override
		public void mouseReleased(MouseEvent e) {
			final MouseEvent stageEvent = getStageMouseEventAndProcess(e);
			if(e.getButton()==MouseEvent.BUTTON1)
				mouseDown = false;
			synchronized(stageMouseListeners) {
				for(MouseListener listener:stageMouseListeners)
					listener.mouseReleased(stageEvent);
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			final MouseEvent stageEvent = getStageMouseEventAndProcess(e);
			if(e.getButton()==MouseEvent.BUTTON1)
				mouseDown = true;
			synchronized(stageMouseListeners) {
				for(MouseListener listener:stageMouseListeners)
					listener.mousePressed(stageEvent);
			}
			stagePanel.grabFocus();
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
			final MouseEvent stageEvent = getStageMouseEventAndProcess(e);
			synchronized(stageMouseListeners) {
				for(MouseListener listener:stageMouseListeners)
					listener.mouseExited(stageEvent);
			}
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			final MouseEvent stageEvent = getStageMouseEventAndProcess(e);
			synchronized(stageMouseListeners) {
				for(MouseListener listener:stageMouseListeners)
					listener.mouseEntered(stageEvent);
			}
			stagePanel.grabFocus();
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			final MouseEvent stageEvent = getStageMouseEventAndProcess(e);
			if(e.getButton()!=MouseEvent.BUTTON1)
				return;
			
			synchronized(stageMouseListeners) {
				for(MouseListener listener:stageMouseListeners)
					listener.mouseClicked(stageEvent);
			}
			stagePanel.grabFocus();
		}
		
		private MouseEvent getStageMouseEventAndProcess(MouseEvent e) {
			synchronized(MOUSE_LOCK) {
				mouseStageX = e.getX()*stageWidth/stagePanel.getWidth()-stageWidth/2;
				mouseStageY = -e.getY()*stageHeight/stagePanel.getHeight()+stageHeight/2;

				return new MouseEvent((Component)e.getSource(), e.getID(), e.getWhen(), e.getModifiers(), (int)mouseStageX, (int)mouseStageY, e.getClickCount(), e.isPopupTrigger());
			}
		}
	};
	private KeyListener primaryStageKeyListener = new KeyListener() {
		
		@Override
		public void keyTyped(KeyEvent e) {}
		
		@Override
		public void keyReleased(KeyEvent e) {
			synchronized(pressedKeys) {
				pressedKeys.remove(getKey(e));
			}
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			synchronized(pressedKeys) {
				pressedKeys.add(getKey(e));
			}
		}
		
		private int getKey(KeyEvent e) {
			if(((e.getKeyChar()>='a')&&(e.getKeyChar()<='z'))||((e.getKeyChar()>='A')&&(e.getKeyChar()<='Z'))||((e.getKeyChar()>='0')&&(e.getKeyChar()<='9')))
				return e.getKeyChar();
			return e.getKeyCode();
		}
	};
	
	/**
	 * @param projectFile 
	 * @param stageWidth 
	 * @param stageHeight 
	 * @param featureSet 
	 * @throws IOException 
	 * 
	 */
	public ScratchRuntimeImplementation(File projectFile, int stageWidth, int stageHeight, FeatureSet featureSet) throws IOException{
		super(featureSet);
		this.stageWidth = stageWidth;
		this.stageHeight = stageHeight;
		loadProject(projectFile);
		
		stagePanel = new StagePanel(this);
	}
	
	/**
	 * 
	 */
	public void start(){
		if(running)
			return;
		running = true;
		stagePanel.addMouseMotionListener(primaryStageMouseMotionListener);
		stagePanel.addMouseListener(primaryStageMouseListener);
		stagePanel.addKeyListener(primaryStageKeyListener);

		greenFlagClicked();
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#startScript(com.shtick.utils.scratch.runner.core.elements.ScriptTuple)
	 */
	@Override
	public ScriptTupleRunner startScript(Script script) {
		return SCRIPT_TUPLE_THREAD.startScript((ScriptImplementation)script);
	}

	private void greenFlagClicked() {
		Set<Opcode> opcodes = getFeatureSet().getOpcodes();
		for(Opcode opcode:opcodes) {
			if(!(opcode instanceof OpcodeHat))
				continue;
			OpcodeHat hat = (OpcodeHat)opcode;
			hat.applicationStarted(this);
		}
		SCRIPT_TUPLE_THREAD.start();
	}

	/**
	 * 
	 * @return A ThreadTaskQueue being executed by a Thread that has authority to manage the Scratch script Threads.
	 */
	public ThreadTaskQueue getThreadManagementTaskQueue() {
		return TASK_QUEUE;
	}
	
	/**
	 * 
	 * @return Not really a thread, but an object that manages the thread that runs the scripts.
	 */
	public ScriptThread getScriptTupleThread() {
		return SCRIPT_TUPLE_THREAD;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntimeInterface#getCurrentStage()
	 */
	@Override
	public TargetImplementation getCurrentStage() {
		return stage;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntimeInterface#getScriptContextByName(java.lang.String)
	 */
	@Override
	public ScriptContext getScriptContextByName(String name) {
		Target sprite = targetsByName.get(name);
		if(sprite!=null)
			return sprite;
		return null;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core3.ScratchRuntimeInterface#getAllRenderableChildren()
	 */
	@Override
	public RenderableChild[] getAllRenderableChildren() {
		return stage.getAllRenderableChildren();
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#bringToFront(com.shtick.utils.scratch.runner.core.elements.Sprite)
	 */
	@Override
	public void bringToFront(Target sprite) {
		stage.bringToFront(sprite);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#sendToBack(com.shtick.utils.scratch.runner.core.elements.Sprite)
	 */
	@Override
	public void sendToBack(Target sprite) {
		stage.sendToBack(sprite);	
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#sendBackNLayers(com.shtick.utils.scratch.runner.core.elements.Sprite, int)
	 */
	@Override
	public void sendBackNLayers(Target sprite, int n) {
		stage.sendBackNLayers(sprite, n);
	}

	/**
	 * 
	 * @return An array of all the stage monitors.
	 */
	public StageMonitor[] getStageMonitors() {
		return stageMonitors.toArray(new StageMonitor[stageMonitors.size()]);
	}
	
	/**
	 * 
	 * @param identifier
	 * @return The object corresponding to the given identifier.
	 */
	public Object getObjectByIdentifier(String identifier) {
		return objectsByIdentifier.get(identifier);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#setSpriteBubbleImage(com.shtick.utils.scratch.runner.core.elements.Sprite, java.awt.Image)
	 */
	@Override
	public void setSpriteBubbleImage(Target sprite, Image image) {
		if((sprite==null)||(image==null))
			bubbleImage=null;
		else
			bubbleImage = new BubbleImage(sprite, image);
		repaintStage();
	}
	
	/**
	 * 
	 * @return The current BubbleImage defined.
	 */
	public BubbleImage getBubbleImage() {
		return bubbleImage;
	}

	/**
	 * 
	 */
	@Override
	public void repaintStage() {
		synchronized(stagePanel) {
			repaintNeeded = true;
		}
	}
	
	/**
	 * Called by the ScriptTupleThread.
	 * If repaint was requested, then this triggers the repaint to occur.
	 * 
	 * @return true if repaint called, and false otherwise.
	 */
	protected boolean repaintStageFinal() {
		if((!repaintNeeded)||(JOptionPane.getFrameForComponent(stagePanel)==JOptionPane.getRootFrame()))
			return false;
		synchronized(stagePanel) {
			try {
				SwingUtilities.invokeAndWait(()->{
					stagePanel.paint(stagePanel.getGraphics());
				});
			}
			catch(InvocationTargetException t) {
				t.printStackTrace();
			}
			catch(InterruptedException t) {};
			repaintNeeded = false;
		}
		return true;
	}
	
	/**
	 * Exits the application.
	 */
	public void stop(){
		synchronized(this){
			stagePanel.removeMouseMotionListener(primaryStageMouseMotionListener);
			stagePanel.removeMouseListener(primaryStageMouseListener);
			stagePanel.removeKeyListener(primaryStageKeyListener);
			SCRIPT_TUPLE_THREAD.stopAllScripts();
			stopped=true;
			running = false;
			this.notify();
		}
	}

	@Override
	public boolean isStopped() {
		return stopped;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public JPanel getStagePanel() {
		return stagePanel;
	}

	@Override
	public int getStageWidth() {
		return stageWidth;
	}

	@Override
	public int getStageHeight() {
		return stageHeight;
	}

	@Override
	public Point2D.Double getMouseStagePosition(){
		return new Point2D.Double(mouseStageX, mouseStageY);
	}
	
	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#isMouseDown()
	 */
	@Override
	public boolean isMouseDown() {
		return mouseDown;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#addKeyListener(java.awt.event.KeyListener)
	 */
	@Override
	public void addKeyListener(KeyListener listener) {
		stagePanel.addKeyListener(listener);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#removeKeyListener(java.awt.event.KeyListener)
	 */
	@Override
	public void removeKeyListener(KeyListener listener) {
		stagePanel.removeKeyListener(listener);
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#addStageMouseListener(com.shtick.utils.scratch.runner.core.StageMouseListener)
	 */
	@Override
	public void addStageMouseListener(MouseListener listener) {
		synchronized(stageMouseListeners) {
			stageMouseListeners.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#removeStageMouseListener(com.shtick.utils.scratch.runner.core.StageMouseListener)
	 */
	@Override
	public void removeStageMouseListener(MouseListener listener) {
		synchronized(stageMouseListeners) {
			stageMouseListeners.remove(listener);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#addStageMouseMotionListener(java.awt.event.MouseMotionListener)
	 */
	@Override
	public void addStageMouseMotionListener(MouseMotionListener listener) {
		synchronized(stageMouseMotionListeners) {
			stageMouseMotionListeners.add(listener);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#removeStageMouseMotionListener(java.awt.event.MouseMotionListener)
	 */
	@Override
	public void removeStageMouseMotionListener(MouseMotionListener listener) {
		synchronized(stageMouseMotionListeners) {
			stageMouseMotionListeners.remove(listener);
		}
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#isKeyPressed(java.lang.String)
	 */
	@Override
	public boolean isKeyPressed(String keyID) {
		synchronized(pressedKeys) {
			int keyCode;
			switch(keyID) {
			case "any":
				return pressedKeys.size()>0;
			case "up arrow":
				keyCode = KeyEvent.VK_UP;
				break;
			case "down arrow":
				keyCode = KeyEvent.VK_DOWN;
				break;
			case "left arrow":
				keyCode = KeyEvent.VK_LEFT;
				break;
			case "right arrow":
				keyCode = KeyEvent.VK_RIGHT;
				break;
			case "space":
				keyCode = KeyEvent.VK_SPACE;
				break;
			case "enter":
				keyCode = KeyEvent.VK_ENTER;
				break;
			default:
				if(keyID.length()!=1)
					throw new IllegalArgumentException("Invalid key code: "+keyID);
				keyCode = keyID.charAt(0);
				break;
			}
			return pressedKeys.contains(keyCode);
		}
	}

	/**
	 * 
	 * @param sound 
	 * @param volume 
	 * @return The Clip
	 * @throws IOException
	 * @throws UnsupportedAudioFileException
	 * @throws LineUnavailableException
	 */
	public SoundMonitor playSound(SoundImplementation sound, double volume) throws IOException, UnsupportedAudioFileException, LineUnavailableException{
	    SoundMonitor retval=null;
	    LinkedList<Throwable> errors = new LinkedList<Throwable>();

	    InputStream in = sound.getSoundData();
	    try {
		    AudioInputStream stream;
		    AudioFormat format;
		    DataLine.Info info;
		    Clip clip;
    		stream = AudioSystem.getAudioInputStream(sound.getSoundData());
		    format = stream.getFormat();
		    info = new DataLine.Info(Clip.class, format);
		    synchronized(activeSoundMonitors) {
			    clip = (Clip) AudioSystem.getLine(info);
			    clip.addLineListener(new LineListener() {
					@Override
					public void update(LineEvent event) {
						if((event.getType()==LineEvent.Type.STOP)||(event.getType()==LineEvent.Type.CLOSE)) {
							synchronized(activeSoundMonitors) {
								activeSoundMonitors.remove(clip);
								if(event.getType()==LineEvent.Type.STOP) {
									clip.close();
								}
							}
						}
					}
				});
			    clip.open(stream);
			    retval = new ClipSoundMonitor(clip);
		    }
	    }
	    catch(UnsupportedAudioFileException t) {
	    	// Save error to report just in case of total failure.
	    	errors.add(t);
	    }
	    if(retval == null) {
	    	in.close();
	    }
		InputSourceStream iss = new InputSourceStream(sound.getSoundData(), null);
	    if(retval==null) {
    		DataSource ds=new PullDataSource() {
				
				@Override
				public void stop() throws IOException {
				}
				
				@Override
				public void start() throws IOException {
				}
				
				@Override
				public Time getDuration() {
					return null;
				}
				
				@Override
				public Object[] getControls() {
					return null;
				}
				
				@Override
				public Object getControl(String arg0) {
					return null;
				}
				
				@Override
				public String getContentType() {
					return null;
				}
				
				@Override
				public void disconnect() {
					try {
						iss.close();
					}
					catch(IOException t) {
					}
				}
				
				@Override
				public void connect() throws IOException {}
				
				@Override
				public PullSourceStream[] getStreams() {
					return new PullSourceStream[] {iss};
				}
			};
			try {
	    		Player player = Manager.createPlayer(ds);
	    		retval = new JMFSoundMonitor(player);
			}
			catch(/*NoPlayerException|*/Throwable t) {
		    	// Save error to report just in case of total failure.
		    	errors.add(t);
			}
	    }
	    if(retval == null) {
	    	iss.close();
	    }
	    if(retval==null) {
	    	for(Throwable error:errors) {
	    		error.printStackTrace();
	    	}
	    	return null;
	    }
	    if(volume>100)
		    retval.setVolume(100);
	    else if(volume<0)
		    retval.setVolume(0);
	    else
	    	retval.setVolume(volume);
	    activeSoundMonitors.add(retval);
	    return retval;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#stopAllSounds()
	 */
	@Override
	public void stopAllSounds() {
	    synchronized(activeSoundMonitors) {
    		for(SoundMonitor soundMonitor:activeSoundMonitors)
    			soundMonitor.stop();
    		activeSoundMonitors.clear();
	    }
	}

	/**
	 * 
	 * @return A Graphics2D object that will let you paint on the background using a coordinate system similar to scratch's, but with the y-axis reversed. (ie. centered on the middle, with x increasing to the right and y increasing to the bottom. The width and the height are the stage width and height.)
	 */
	public Graphics2D getPenLayerGraphics() {
		return stagePanel.getPenLayerGraphics();
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#clearPenLayer()
	 */
	@Override
	public void clearPenLayer() {
		stagePanel.clearPenLayer();
		repaintStage();
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#addComponent(java.awt.Component, int, int, int, int)
	 */
	@Override
	public void addComponent(Component component, int x, int y, int width, int height) {
		Runnable runnable = ()->{
			stagePanel.addComponent(component, x, y, width, height);
		};
		if(SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		}
		else {
			try {
				SwingUtilities.invokeAndWait(runnable);
			}
			catch(InvocationTargetException|InterruptedException t) {
				t.printStackTrace();
			}
		}
		repaintStage();			
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.ScratchRuntime#removeComponent(java.awt.Component)
	 */
	@Override
	public void removeComponent(Component component) {
		Runnable runnable = ()->{
			stagePanel.removeComponent(component);
			repaintStage();
		};
		if(SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		}
		else {
			try {
				SwingUtilities.invokeAndWait(runnable);
			}
			catch(InvocationTargetException|InterruptedException t) {
				t.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param clone
	 */
	public void addClone(Target clone) {
		if(!clone.isClone())
			throw new IllegalStateException();
		stage.addChild(clone);
		repaintStage();
	}
	
	/**
	 * 
	 * @param clone Removes the sprite from the list of sprites, but only if it is a clone.
	 */
	public void deleteClone(Target clone) {
		if(!clone.isClone())
			throw new IllegalStateException();
		stage.removeChild(clone);
		repaintStage();
	}

	protected void loadProject(File file) throws IOException{
		scratchFile = new ScratchFile(file);
		Map<String,Object> jsonMap;
		try(InputStream in = scratchFile.getResource(RESOURCE_PROJECT_FILE);){
			jsonMap = JSONDecoder.decode(in, new JSONNumberDecoder() {
			
				@Override
				public Object decodeNumber(NumberToken token) {
					if((token.getFractionalPart()==null)&&(token.getExponentialPart()==null))
						return Long.parseLong((token.isNegative()?"-":"")+token.getWholePart().getText());
					return Double.valueOf(token.toString());
				}
			});
		}
		parseProjectMap(jsonMap, scratchFile);
	}

	private void parseProjectMap(Map<String,Object> jsonMap, ScratchFile scratchFile) throws IOException{
		objectsByIdentifier = new HashMap<>();
		if(jsonMap.get("targets")!=null) {
			Object targetsJson = jsonMap.get("targets");
			if(!(targetsJson instanceof java.util.List<?>))
				throw new IOException("targets not encoded as list");
			parseTargets((java.util.List<Object>)targetsJson);
		}
		else {
			targetsByName = new HashMap<>();
		}
		if(jsonMap.get("monitors")!=null) {
			Object monitorsJson = jsonMap.get("monitors");
			if(!(monitorsJson instanceof java.util.List<?>))
				throw new IOException("monitors not encoded as list");
			parseMonitors((java.util.List<Object>)monitorsJson);
		}
		else {
			stageMonitors = new LinkedList<>();
		}
		if(jsonMap.get("extensions")!=null) {
			Object extensionsJson = jsonMap.get("extensions");
			if(!(extensionsJson instanceof java.util.List<?>))
				throw new IOException("extensions not encoded as list");
			parseExtensions((java.util.List<Object>)extensionsJson);
		}
		else {
			
		}
		
		if(jsonMap.get("meta")!=null) {
			Object metaJson = jsonMap.get("meta");
			if(!(metaJson instanceof Map<?,?>))
				throw new IOException("meta not encoded as Map");
			parseMeta((Map<String,Object>)metaJson);
		}
		else {
			this.meta = new HashMap<>();
		}
	}

	private void parseTargets(java.util.List<Object> targets) throws IOException{
		this.targetsByName =  new HashMap<>(targets.size());
		for(Object targetObject:targets) {
			if(!(targetObject instanceof Map<?,?>))
				throw new IOException("target not encoded as Map");
			TargetImplementation target = parseTarget((Map<String,Object>)targetObject);
			targetsByName.put(target.getObjName(),target);
			if(target.isStage())
				stage = target;
		}
		
		for(TargetImplementation target:targetsByName.values()) {
			if(target.isStage())
				continue;
			stage.addChild(target);
		}
	}

	private void parseMonitors(java.util.List<Object> monitors) throws IOException{
		this.stageMonitors =  new LinkedList<>();
		for(Object monitorObject:monitors) {
			StageMonitorImplementation monitor = parseMonitor(monitorObject);
			stageMonitors.add(monitor);
		}
	}
	
	private StageMonitorImplementation parseMonitor(Object object) throws IOException{
		if(!(object instanceof Map<?,?>))
			throw new IOException("Stage monitor not encoded as map");
		java.util.Map<String,Object> map = (java.util.Map<String,Object>)object;
		try {
			Map<String,Object> paramsMap = (Map<String,Object>)map.get("params");
			Map<String,Object> params = new HashMap<>(paramsMap.size());
			for(String name:paramsMap.keySet()) {
				params.put(name,paramsMap.get(name));
			}

			StageMonitorImplementation retval = new StageMonitorImplementation(
					(String)map.get("id"), (String)map.get("mode"), (String)map.get("opcode"), params, (String)map.get("spriteName"),
					((Number)map.get("width")).longValue(), ((Number)map.get("height")).longValue(), ((Number)map.get("x")).doubleValue(), ((Number)map.get("y")).doubleValue(),
					(Boolean)map.get("visible"),
					map.containsKey("min")?((Number)map.get("min")).doubleValue():0,
					map.containsKey("max")?((Number)map.get("max")).doubleValue():0,
					map.containsKey("isDiscrete")?(Boolean)map.get("isDiscrete"):false,
					this);
			return retval;
		}
		catch(Throwable t) {
			throw t;
		}
	}

	private void parseExtensions(java.util.List<Object> extensions) throws IOException{
		// TODO Investigate this. Is this something we want to implement?
	}

	private void parseMeta(Map<String,Object> meta) throws IOException{
		if(meta==null) {
			meta = new HashMap<>();
		}
		this.meta = meta;
	}

	private TargetImplementation parseTarget(Map<String,Object> target) throws IOException{
		boolean isStage = target.containsKey("isStage")?((Boolean)target.get("isStage")).booleanValue():false;
		String name = target.containsKey("name")?target.get("name").toString():null;
		long currentCostume = target.containsKey("currentCostume")?((Number)target.get("currentCostume")).longValue():0;
		double volume = target.containsKey("volume")?((Number)target.get("volume")).doubleValue():100;
		long layerOrder = target.containsKey("layerOrder")?((Number)target.get("layerOrder")).longValue():0;
		long tempo = target.containsKey("tempo")?((Number)target.get("tempo")).longValue():60;
		long videoTransparency = target.containsKey("videoTransparency")?((Number)target.get("videoTransparency")).longValue():50;
		String videoState = target.containsKey("videoState")?target.get("videoState").toString():null;
		String textToSpeechLanguage = (target.get("textToSpeechLanguage")!=null)?target.get("textToSpeechLanguage").toString():null;
		boolean visible = target.containsKey("visible")?((Boolean)target.get("visible")).booleanValue():true;
		double x = target.containsKey("x")?((Number)target.get("x")).doubleValue():0;
		double y = target.containsKey("y")?((Number)target.get("y")).doubleValue():0;
		long size = (target.get("size")!=null)?((Number)target.get("size")).longValue():100;
		double direction = target.containsKey("direction")?((Number)target.get("direction")).doubleValue():0;
		boolean draggable = target.containsKey("draggable")?((Boolean)target.get("draggable")).booleanValue():false;
		String rotationStyle = target.containsKey("rotationStyle")?target.get("rotationStyle").toString():"all around";

		Map<String,Object> variablesMap = target.containsKey("variables")?(Map<String,Object>)target.get("variables"):new HashMap<>();
		ArrayList<VariableImplementation> variables = new ArrayList<>(variablesMap.size());
		for(String id:variablesMap.keySet()) {
			java.util.List<Object> varVal = (java.util.List<Object>)variablesMap.get(id);
			VariableImplementation variableImplementation = new VariableImplementation(id, varVal.get(1), varVal.get(0).toString());
			variables.add(variableImplementation);
			objectsByIdentifier.put(id, variableImplementation);
		}

		java.util.List<Object> costumesList = target.containsKey("costumes")?(java.util.List<Object>)target.get("costumes"):new LinkedList<>();
		ArrayList<CostumeImplementation> costumes = new ArrayList<>(costumesList.size());
		for(Object costume:costumesList) {
			costumes.add(parseCostume(costume, scratchFile));
		}

		java.util.List<Object> soundsList = target.containsKey("sounds")?(java.util.List<Object>)target.get("sounds"):new LinkedList<>();
		ArrayList<SoundImplementation> sounds = new ArrayList<>(soundsList.size());
		for(Object sound:soundsList) {
			sounds.add(parseSound(sound, scratchFile));
		}
		
		Map<String,Object> blocksMap = target.containsKey("blocks")?(Map<String,Object>)target.get("blocks"):new HashMap<>();
		Map<String,BlockImplementation> blocks = new HashMap<>(blocksMap.size());
		for(String id:blocksMap.keySet()) {
			BlockImplementation blockImplementation = parseBlock(id, blocksMap.get(id));
			blocks.put(id, blockImplementation);
			objectsByIdentifier.put(id, blockImplementation);
		}
		
		Map<String,Object> broadcastsMap = target.containsKey("broadcasts")?(Map<String,Object>)target.get("broadcasts"):new HashMap<>();
		Map<String,BroadcastImplementation> broadcasts = new HashMap<>(broadcastsMap.size());
		for(String id:broadcastsMap.keySet()) {
			BroadcastImplementation broadcastImplementation = new BroadcastImplementation(id, (String)broadcastsMap.get(id));
			broadcasts.put(id, broadcastImplementation);
			objectsByIdentifier.put(id, broadcastImplementation);
		}
		
		Map<String,Object> listsMap = target.containsKey("lists")?(Map<String,Object>)target.get("lists"):new HashMap<>();
		Map<String,ListImplementation> listsByName = new HashMap<>(listsMap.size());
		for(String id:listsMap.keySet()) {
			ListImplementation listImplementation = parseList(listsMap.get(id));
			listsByName.put(listImplementation.getListName(), listImplementation);
			objectsByIdentifier.put(id, listImplementation);
		}
		
		try {
			return new TargetImplementation(
					isStage, name, variables, listsByName, broadcasts,
					blocks, null, currentCostume, costumes.toArray(new CostumeImplementation[costumes.size()]), sounds.toArray(new SoundImplementation[sounds.size()]), volume,
					layerOrder, tempo, videoTransparency, videoState, textToSpeechLanguage,
					visible, x, y, size, direction, draggable, rotationStyle, this);
		}
		catch(InvalidScriptDefinitionException t) {
			throw new IOException(t);
		}
	}
	
	private static BlockImplementation parseBlock(String id, Object object) throws IOException{
		if(!(object instanceof java.util.Map<?,?>))
			throw new IOException("sound not encoded as map.");
		java.util.Map<String,Object> map = (java.util.Map<String,Object>)object;

		String opcode = (String)map.get("opcode");
		String next = (String)map.get("next");
		String parent = (String)map.get("parent");
		boolean shadow = ((Boolean)map.get("shadow")).booleanValue();
		boolean topLevel = ((Boolean)map.get("topLevel")).booleanValue();
		double x = map.containsKey("x")?((Number)map.get("x")).doubleValue():-1;
		double y = map.containsKey("y")?((Number)map.get("y")).doubleValue():-1;
		MutationImplementation mutation = map.containsKey("mutation")?parseMutation(map.get("mutation")):null;

		Map<String,Object> inputsMap = (Map<String,Object>)map.get("inputs");
		Map<String,Input> inputs = new HashMap<>(inputsMap.size());
		for(String inputName:inputsMap.keySet())
			inputs.put(inputName,parseInput(inputsMap.get(inputName)));

		Map<String,Object> fieldsMap = (Map<String,Object>)map.get("fields");
		Map<String,String[]> fields = new HashMap<>(fieldsMap.size());
		for(String fieldName:fieldsMap.keySet()) {
			fields.put(fieldName,parseBlockField(fieldsMap.get(fieldName)));
		}
		
		return new BlockImplementation(id, opcode, next, parent, inputs, fields, shadow, topLevel, x, y, mutation);
	}
	
	private static MutationImplementation parseMutation(Object object) throws IOException{
		if(!(object instanceof java.util.Map<?,?>))
			throw new IOException("mutation not encoded as map.");
		java.util.Map<String,Object> map = (java.util.Map<String,Object>)object;
		String tagName = (String)map.get("tagName");
		List<Object> children = (List<Object>)map.get("children");
		String proccode = (String)map.get("proccode");
		String argumentids = (String)map.get("argumentids");
		String argumentnames = (String)map.get("argumentnames");
		String argumentdefaults = (String)map.get("argumentdefaults");
		Object warpObject = map.get("warp");
		if(!(warpObject instanceof Boolean)) {
			warpObject = Boolean.parseBoolean(warpObject.toString());
		}
		boolean warp = (Boolean)warpObject;
		
		return new MutationImplementation(tagName, children, proccode, argumentids, argumentnames, argumentdefaults, warp);
	}
	
	private static String[] parseBlockField(Object object) throws IOException{
		if(!(object instanceof java.util.List<?>))
			throw new IOException("Block field not encoded as list/array.");
		java.util.List<Object> list = (java.util.List<Object>)object;
		return list.toArray(new String[list.size()]);
	}
	
	private static Input parseInput(Object object) throws IOException{
		if(!(object instanceof java.util.List<?>))
			throw new IOException("Input not encoded as list/array.");
		java.util.List<Object> list = (java.util.List<Object>)object;
		int type = ((Number)list.remove(0)).intValue();
		Object value=list.remove(0);
		if(value instanceof java.util.List<?>)
			value = parseTypedValue(value);
		else if(value instanceof String)
			value = new TypedValueImplementation(type, new Object[]{value}) ;
		else
			throw new IOException("Typed value not encoded as list/array or string: "+object.getClass().getCanonicalName());
		return new Input(type, value);
	}
	
	private static TypedValueImplementation parseTypedValue(Object object) throws IOException{
		java.util.List<Object> list = (java.util.List<Object>)object;
		Number type = (Number)list.remove(0);
		return new TypedValueImplementation(type.intValue(), list.toArray());
	}
	
	private static SoundImplementation parseSound(Object object, ScratchFile scratchFile) throws IOException{
		if(!(object instanceof java.util.Map<?,?>))
			throw new IOException("sound not encoded as map.");
		java.util.Map<String,Object> map = (java.util.Map<String,Object>)object;

		String assetId = (String)map.get("assetId");
		String name = (String)map.get("name");
		String dataFormat = (String)map.get("dataFormat");
		String format = (String)map.get("format");
		long rate = (Long)map.get("rate");
		long sampleCount = (Long)map.get("sampleCount");
		String md5ext = (String)map.get("md5ext");
		
		byte[] data;
		{ // TODO If dropping Java 8 compatibility. In Java 9 there s a readAllBytes function that would be nice to use.
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int l;
			try(InputStream in = scratchFile.getResource(md5ext)){
				l = in.read(buffer);
				while(l>=0) {
					if(l>0)
						out.write(buffer, 0, l);
					l = in.read(buffer);
				}
			}
			data = out.toByteArray();
		}
		
		return new SoundImplementation(assetId, name, dataFormat, format, sampleCount, rate, md5ext, data);
	}
	
	private static CostumeImplementation parseCostume(Object object, ScratchFile scratchFile) throws IOException{
		if(!(object instanceof java.util.Map<?,?>))
			throw new IOException("costume not encoded as map.");
		java.util.Map<String,Object> map = (java.util.Map<String,Object>)object;
		
		// Get the image for this costume.
		String assetId = (String)map.get("assetId");
		String name = (String)map.get("name");
		String md5ext = (String)map.get("md5ext");
		String dataFormat = (String)map.get("dataFormat");
		BufferedImage image = ScratchImageRenderer.renderImageResource(md5ext, scratchFile);

		return new CostumeImplementation(assetId, name, md5ext, dataFormat, map.containsKey("bitmapResolution")?((Long)map.get("bitmapResolution")).intValue():1, ((Long)map.get("rotationCenterX")).intValue(), ((Long)map.get("rotationCenterY")).intValue(), image);
	}

    /**
     * 
     * @param object
     * @param listConsolidation A list of already known lists to consolidated with redundantly defined lists.
     * @return The ListImplementation.
     *         If a list with the same name is already defined in listConsolidation, then the value in listConsolidation is used.
     *         Otherwise, the newly parsed ListImplementation is added to listConsolidation.
     * @throws IOException
     */
	private ListImplementation parseList(Object object) throws IOException{
		if(!(object instanceof java.util.List<?>))
			throw new IOException("list not encoded as list");
		java.util.List<?> list = (java.util.List<?>)object;
		if(list.size()!=2)
			throw new IOException("unexpected list encoding length");
		
		String listName = (String)list.get(0);
		List<Object> contents = (List<Object>)list.get(1);

		return new ListImplementation(listName, contents, this);
	}
}
