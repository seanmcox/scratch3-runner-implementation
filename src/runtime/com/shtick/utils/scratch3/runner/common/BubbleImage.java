/**
 * 
 */
package com.shtick.utils.scratch3.runner.common;

import java.awt.Image;

import com.shtick.utils.scratch3.runner.core.elements.Target;

/**
 * @author sean.cox
 *
 */
public class BubbleImage {
	private Target sprite;
	private Image image;
	
	/**
	 * @param sprite
	 * @param image
	 */
	public BubbleImage(Target sprite, Image image) {
		super();
		this.sprite = sprite;
		this.image = image;
	}

	/**
	 * @return the sprite
	 */
	public Target getSprite() {
		return sprite;
	}

	/**
	 * @return the image
	 */
	public Image getImage() {
		return image;
	}
}
