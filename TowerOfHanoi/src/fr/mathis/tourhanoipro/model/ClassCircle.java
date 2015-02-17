package fr.mathis.tourhanoipro.model;

import java.io.Serializable;

public class ClassCircle implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5922842562269786674L;
	private int id;
	private int color;
	
	public ClassCircle()
	{

	}
	
	public ClassCircle(int id, int color)
	{
		this.id = id;
		this.color = color;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	
}
