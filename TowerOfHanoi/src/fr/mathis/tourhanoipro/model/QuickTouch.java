package fr.mathis.tourhanoipro.model;

import java.io.Serializable;

public class QuickTouch implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5232434486515097479L;
	int top;
	int left;
	int width;
	int height;
	
	public int getTop() {
		return top;
	}

	public void setTop(int top) {
		this.top = top;
	}

	public int getLeft() {
		return left;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public QuickTouch() {
		top = 0;
		left = 0;		
		width = 0;
		height = 0;
	}
	
}
