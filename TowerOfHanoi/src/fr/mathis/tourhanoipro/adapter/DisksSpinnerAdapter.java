package fr.mathis.tourhanoipro.adapter;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import fr.mathis.tourhanoipro.R;
import fr.mathis.tourhanoipro.tools.Tools;

public class DisksSpinnerAdapter extends BaseAdapter {

	LayoutInflater _inflater;
	String[] items;
	int rippleColor;

	public DisksSpinnerAdapter(Activity a) {
		_inflater = LayoutInflater.from(a);
		items = a.getResources().getStringArray(R.array.circles);
		rippleColor = a.getResources().getColor(R.color.transparent_dark_selected);
	}

	@Override
	public int getCount() {
		return items.length;
	}

	@Override
	public Object getItem(int position) {
		return items[position];
	}

	@Override
	public long getItemId(int position) {
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tv;
		if (convertView == null) {
			tv = (TextView) _inflater.inflate(R.layout.spinner_item, parent, false);
		} else
			tv = (TextView) convertView;

		tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
		int padding = Tools.convertDpToPixel(10);
		tv.setPadding(padding, padding, padding, padding);
		tv.setText("" + getItem(position));

		return tv;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		TextView tv;
		if (convertView == null) {
			tv = (TextView) _inflater.inflate(R.layout.spinner_item, parent, false);
		} else
			tv = (TextView) convertView;
		tv.setTypeface(tv.getTypeface(), Typeface.NORMAL);
		int padding = Tools.convertDpToPixel(10);
		tv.setPadding(padding * 2, padding, padding * 2, padding);
		tv.setText("" + getItem(position));

		return tv;
	}
}
