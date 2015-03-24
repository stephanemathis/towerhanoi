package fr.mathis.tourhanoipro.model;

import java.io.Serializable;
import java.util.ArrayList;

public class ClassField implements Serializable {

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

	public ClassField() {
		qt = null;
	}

	public QuickTouch getQtCopy() {
		if (this.qt == null)
			return null;
		QuickTouch rqt = new QuickTouch();
		rqt.setHeight(qt.getHeight());
		rqt.setWidth(qt.getWidth());
		rqt.setLeft(qt.getLeft());
		rqt.setTop(qt.getTop());
		return rqt;
	}

	public void setQt(QuickTouch qt) {
		if (qt == null) {
			this.qt = null;
			return;
		}
		this.qt = new QuickTouch();
		this.qt.setHeight(qt.getHeight());
		this.qt.setWidth(qt.getWidth());
		this.qt.setLeft(qt.getLeft());
		this.qt.setTop(qt.getTop());
	}

}
