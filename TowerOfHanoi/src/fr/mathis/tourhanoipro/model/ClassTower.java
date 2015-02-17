package fr.mathis.tourhanoipro.model;

import java.io.Serializable;
import java.util.ArrayList;

public class ClassTower implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4534387356994615332L;
	ArrayList<ClassCircle> circles;
	int id;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ClassTower ()
	{
		
	}

	public ArrayList<ClassCircle> getCircles() {
		return circles;
	}

	public void setCircles(ArrayList<ClassCircle> circles) {
		this.circles = circles;
	}
}
