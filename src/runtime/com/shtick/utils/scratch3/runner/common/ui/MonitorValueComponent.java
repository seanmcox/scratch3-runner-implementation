/**
 * 
 */
package com.shtick.utils.scratch3.runner.common.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;

import com.shtick.utils.scratch3.runner.core.StageMonitorCommand;
import com.shtick.utils.scratch3.runner.core.ValueListener;
import com.shtick.utils.scratch3.runner.core.elements.ScriptContext;
import com.shtick.utils.scratch3.runner.common.ScratchRuntimeCommon;

/**
 * @author sean.cox
 *
 */
public class MonitorValueComponent extends JComponent{
	private static final Stroke PANEL_STROKE = new BasicStroke(1.0f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER);
	private static final int VERTICAL_PAD = 1;
	private static final int HORIZONTAL_PAD = 1;
	private int fontHeight;
	private String opcode;
	private String variable;
	private Color backgroundColor = Color.GRAY;
	private Font font;
	private FontMetrics fontMetrics;
	private ScriptContext context;
	private ScratchRuntimeCommon runtime;

	/**
	 * @param opcode 
	 * @param variable 
	 * @param target 
	 * @param backgroundColor 
	 * @param font 
	 * @param fontMetrics 
	 * @param runtime 
	 * 
	 */
	public MonitorValueComponent(String opcode, String variable, String target, Font font, FontMetrics fontMetrics, ScratchRuntimeCommon runtime) {
		this.opcode = opcode;
		this.variable = variable;
		this.font = font;
		this.fontMetrics = fontMetrics;
		this.runtime = runtime;
		fontHeight = fontMetrics.getHeight();
		context = runtime.getScriptContextByName(target);
		setDoubleBuffered(false);

		setPreferredSize(new Dimension(50,fontHeight+VERTICAL_PAD*2));
		StageMonitorCommand monitorCommand = runtime.getFeatureSet().getStageMonitorCommand(opcode);
		if(monitorCommand==null) {
			System.err.println("Error: StageMonitorCommand not found: "+opcode);
			return;
		}
		monitorCommand.addValueListener(new ValueListener() {
			String[] arguments = new String[] {variable};
			@Override
			public void valueUpdated(Object oldValue, Object newValue) {
				repaint();
			}
			
			@Override
			public ScriptContext getScriptContext() {
				return context;
			}
			
			@Override
			public Object[] getArguments() {
				return arguments;
			}
		});
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		Shape panelShape = new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 5, 5);
		g2.setColor(backgroundColor);
		g2.fill(panelShape);
		g2.setColor(Color.WHITE);
		g2.setStroke(PANEL_STROKE);
		g2.draw(panelShape);
		
		g2.setFont(font);
		StageMonitorCommand commandImpl = runtime.getFeatureSet().getStageMonitorCommand(opcode);
		String text;
		if(commandImpl==null)
			text = "Err";
		else
			text = commandImpl.execute(runtime, context, variable);
		int labelWidth = fontMetrics.stringWidth(text);
		g2.drawString(text, (getWidth()-labelWidth)/2, VERTICAL_PAD+fontMetrics.getAscent());
		
		paintChildren(g);
	}

}
