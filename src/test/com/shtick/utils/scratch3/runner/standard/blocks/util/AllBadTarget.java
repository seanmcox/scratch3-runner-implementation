/**
 * 
 */
package com.shtick.utils.scratch3.runner.standard.blocks.util;

import static org.junit.jupiter.api.Assertions.fail;

import java.awt.geom.Area;
import java.util.Set;

import com.shtick.utils.scratch3.runner.core.SoundMonitor;
import com.shtick.utils.scratch3.runner.core.ValueListener;
import com.shtick.utils.scratch3.runner.core.elements.Broadcast;
import com.shtick.utils.scratch3.runner.core.elements.Costume;
import com.shtick.utils.scratch3.runner.core.elements.List;
import com.shtick.utils.scratch3.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch3.runner.core.elements.Target;
import com.shtick.utils.scratch3.runner.core.elements.Variable;

/**
 * @author sean.cox
 *
 */
public class AllBadTarget implements Target {
	@Override
	public void stopScripts() {
		fail("stopScripts called unnecessarily");
	}
	
	@Override
	public SoundMonitor playSoundByName(String soundName) {
		fail("playSoundByName called unnecessarily");
		return null;
	}
	
	@Override
	public SoundMonitor playSoundByIndex(int index) {
		fail("playSoundByIndex called unnecessarily");
		return null;
	}

	@Override
	public void setVolume(double volume) {
		fail("setVolume called unnecessarily");
	}

	@Override
	public double getVolume() {
		fail("getVolume called unnecessarily");
		return 100;
	}
	
	@Override
	public void setContextVariableValueByName(String name, Object value) throws IllegalArgumentException {
		fail("setContextVariableValueByName called unnecessarily");
	}
	
	@Override
	public Object getContextVariableValueByName(String name) throws IllegalArgumentException {
		fail("getContextVariableValueByName called unnecessarily");
		return null;
	}
	
	@Override
	public Object getContextPropertyValueByName(String name) throws IllegalArgumentException {
		fail("getContextPropertyValueByName called unnecessarily");
		return null;
	}
	
	@Override
	public List getContextListByName(String name) {
		fail("getContextListByName called unnecessarily");
		return null;
	}
	
	@Override
	public ScriptContext getContextObject() {
		fail("getContextObject called unnecessarily");
		return this;
	}

	@Override
	public void setVisible(boolean visible) {
		fail("setVisible called unnecessarily");
	}
	
	@Override
	public void setScratchY(double scratchY) {
		fail("setScratchY called unnecessarily");
	}
	
	@Override
	public void setScratchX(double scratchX) {
		fail("setScratchX called unnecessarily");
	}
	
	@Override
	public double getSize() {
		fail("getSize called unnecessarily");
		return 0;
	}

	@Override
	public void setSize(double size) {
		fail("setSize called unnecessarily");
	}

	@Override
	public void setRotationStyle(String rotationStyle) {
		fail("setRotationStyle called unnecessarily");
	}
	
	@Override
	public void setEffect(String name, double value) {
		fail("setEffect called unnecessarily");
	}
	
	@Override
	public void setDirection(double direction) {
		fail("setDirection called unnecessarily");
	}
	
	@Override
	public boolean setCurrentCostumeIndex(int i) {
		fail("setCurrentCostumeIndex called unnecessarily");
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getCurrentCostumeIndex()
	 */
	@Override
	public int getCurrentCostumeIndex() {
		fail("getCurrentCostumeIndex called unnecessarily");
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getCostumeCount()
	 */
	@Override
	public int getCostumeCount() {
		fail("getCostumeCount called unnecessarily");
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#setCostumeByName(java.lang.String)
	 */
	@Override
	public boolean setCostumeByName(String name) {
		fail("setCostumeByName called unnecessarily");
		return false;
	}

	@Override
	public boolean isVisible() {
		fail("isVisible called unnecessarily");
		return true;
	}
	
	@Override
	public boolean isClone() {
		fail("isClone called unnecessarily");
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getCloneParent()
	 */
	@Override
	public Target getCloneParent() {
		fail("getCloneParent called unnecessarily");
		return null;
	}

	@Override
	public void gotoXY(double scratchX, double scratchY) {
		fail("gotoXY called unnecessarily");
	}
	
	@Override
	public double getScratchY() {
		fail("getScratchY called unnecessarily");
		return 0;
	}
	
	@Override
	public double getScratchX() {
		fail("getScratchX called unnecessarily");
		return 0;
	}
	
	@Override
	public String getRotationStyle() {
		fail("getRotationStyle called unnecessarily");
		return "none";
	}
	
	@Override
	public String getObjName() {
		fail("getObjName called unnecessarily");
		return "testObject";
	}
	
	@Override
	public double getEffect(String name) {
		fail("getEffect called unnecessarily");
		return 0;
	}
	
	@Override
	public double getDirection() {
		fail("getDirection called unnecessarily");
		return 90;
	}
	
	@Override
	public Costume getCurrentCostume() {
		fail("getCurrentCostume called unnecessarily");
		return null;
	}
	
	@Override
	public void deleteClone() {
		fail("deleteClone called unnecessarily");
	}
	
	@Override
	public void createClone() {
		fail("createClone called unnecessarily");
	}
	
	@Override
	public void clearEffects() {
		fail("clearEffects called unnecessarily");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.Sprite#getClones()
	 */
	@Override
	public Set<Target> getClones() {
		fail("getClones called unnecessarily");
		return null;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#addVariableListener(java.lang.String, com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void addVariableListener(String var, ValueListener listener) {
		fail("addVariableListener called unnecessarily");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core.elements.ScriptContext#removeVariableListener(java.lang.String, com.shtick.utils.scratch.runner.core.ValueListener)
	 */
	@Override
	public void removeVariableListener(String var, ValueListener listener) {
		fail("removeVariableListener called unnecessarily");
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core3.elements.ScriptContext#getContextVariableByName(java.lang.String)
	 */
	@Override
	public Variable getContextVariableByName(String name) {
		fail("getContextVariableByName called unnecessarily");
		return null;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core3.elements.ScriptContext#getContextBroadcastByName(java.lang.String)
	 */
	@Override
	public Broadcast getContextBroadcastByName(String name) {
		fail("getContextBroadcastByName called unnecessarily");
		return null;
	}

	/* (non-Javadoc)
	 * @see com.shtick.utils.scratch.runner.core3.elements.Target#getShape()
	 */
	@Override
	public Area getShape() {
		fail("getShape called unnecessarily");
		return null;
	}
}
