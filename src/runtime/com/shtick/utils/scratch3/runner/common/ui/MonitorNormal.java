/**
 * 
 */
package com.shtick.utils.scratch3.runner.common.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;

import javax.swing.JLabel;

import com.shtick.utils.scratch3.runner.common.ScratchRuntimeCommon;
import com.shtick.utils.scratch3.runner.impl.elements.StageMonitorImplementation;

/**
 * @author sean.cox
 *
 */
public class MonitorNormal extends MonitorComponent{
	private static final Stroke PANEL_STROKE = new BasicStroke(1.0f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_MITER);
	private static final Color COLOR_BACKGROUND = new Color(160, 160, 160);
	private static final int VERTICAL_PAD = 2;
	private static final int HORIZONTAL_PAD = 5;
	private int labelWidth;
	private int fontHeight;

	/**
	 * @param monitor 
	 * @param runtime 
	 * 
	 */
	public MonitorNormal(StageMonitorImplementation monitor, ScratchRuntimeCommon runtime) {
		super(monitor);
		Map<String,Object> params = monitor.getParams();
		String labelText = params.containsKey("VARIABLE")?params.get("VARIABLE").toString():"???";
		setLayout(new FlowLayout(FlowLayout.LEFT,HORIZONTAL_PAD,VERTICAL_PAD));
		labelWidth = getFontMetricsNormal().stringWidth(labelText);
		fontHeight = getFontMetricsNormal().getHeight();
		JLabel label = new JLabel(labelText);
		label.setFont(FONT_NORMAL);
		label.setForeground(Color.BLACK);
		add(label);
		add(new MonitorValueComponent(monitor.getOpcode(), labelText, monitor.getSpriteName(), FONT_NORMAL, getFontMetricsNormal(), runtime));
		setDoubleBuffered(false);

		setVisible(true);
		Dimension preferredSize = getPreferredSize();
		setBounds((int)monitor.getX(),(int)monitor.getY(), preferredSize.width, preferredSize.height);
		doLayout();
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		if(!getMonitorObject().isVisible())
			return;
		Graphics2D g2 = (Graphics2D)g;
		Shape panelShape = new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 10, 10);
		g2.setColor(COLOR_BACKGROUND);
		g2.fill(panelShape);
		g2.setColor(Color.GRAY);
		g2.setStroke(PANEL_STROKE);
		g2.draw(panelShape);
		paintChildren(g);
	}

}
