package fr.mathis.tourhanoipro.model;

import java.io.Serializable;
import java.util.ArrayList;

public class ClassField implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7738433475066074081L;
	private ArrayList<ClassTower> towers;
	private QuickTouch qt;
	int id;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<ClassTower> getTowers() {
		return towers;
	}

	public void setTowers(ArrayList<ClassTower> towers) {
		this.towers = towers;
	}

	public ClassField()
	{
		qt = null;
	}

	public QuickTouch getQt() {
		return qt;
	}

	public void setQt(QuickTouch qt) {
		this.qt = qt;
	}
	
	
}
