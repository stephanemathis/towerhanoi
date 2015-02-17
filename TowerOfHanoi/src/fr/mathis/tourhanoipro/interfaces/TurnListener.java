package fr.mathis.tourhanoipro.interfaces;

import fr.mathis.tourhanoipro.model.ClassField;

public interface TurnListener {
	ClassField field = null;

	public void turnPlayed(int nbCoup, int nbTotal);

	public void gameFinished(int nbCoup, int nbTotal, long miliseconds);
}
