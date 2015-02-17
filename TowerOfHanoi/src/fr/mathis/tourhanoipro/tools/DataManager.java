package fr.mathis.tourhanoipro.tools;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;

public class DataManager {

	public static ArrayList<String> GetAllSavedGames(Context c) {
		SharedPreferences settings = c.getSharedPreferences("games", 0);

		ArrayList<String> gamesStringList = new ArrayList<String>();
		String games = settings.getString("savedGames", "");

		if (settings.getString("games", "").compareTo("") != 0) {
			ArrayList<String> local = new ArrayList<String>();
			for (String s : settings.getString("games", "").split("_")) {
				if (s != null && s.length() > 0)
					local.add(s);
			}
			SaveAllGames(local, c);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("games", "");
			editor.commit();
			return GetAllSavedGames(c);
		}

		if (Tools.generateMd5(games).compareTo(settings.getString("key", "")) == 0) {
			for (String s : games.split("_")) {
				if (s != null && s.length() > 0)
					gamesStringList.add(s);
			}
		}

		String oldVersionGame = DataManager.GetMemorizedValue("game", c);
		if (oldVersionGame != null && oldVersionGame.length() > 0) {
			gamesStringList.add(0, oldVersionGame);
			DataManager.MemorizeValue("game", "", c);

			SaveAllGames(gamesStringList, c);
		}

		return gamesStringList;
	}

	public static void SaveAllGames(ArrayList<String> games, Context c) {
		String value = "";

		int i = 0;

		for (String s : games) {
			if (s != null && s.length() > 0 && (!s.contains(":n:n") || i == 0))
				value = value + "_" + s;
			i++;
		}
		if (value.length() > 0)
			value = value.substring(1);

		SharedPreferences settings = c.getSharedPreferences("games", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("savedGames", value);
		editor.putString("key", Tools.generateMd5(value));
		editor.commit();
	}

	public static void MemorizeValue(String name, String value, Context c) {
		SharedPreferences settings = c.getSharedPreferences("data", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(name, value);
		editor.commit();
	}

	public static String GetMemorizedValue(String name, Context c) {
		SharedPreferences settings = c.getSharedPreferences("data", 0);
		return settings.getString(name, null);
	}

	public static void MemorizeValue(String name, boolean value, Context c) {
		SharedPreferences settings = c.getSharedPreferences("data", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(name, value);
		editor.commit();
	}

	public static boolean GetMemorizedValueBoolean(String name, Context c) {
		SharedPreferences settings = c.getSharedPreferences("data", 0);
		return settings.getBoolean(name, false);
	}

}
