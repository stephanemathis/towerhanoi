package fr.mathis.tourhanoipro.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import fr.mathis.tourhanoipro.R;

public class FixRecyclerViewScrollbarFrameLayout extends FrameLayout {

	public FixRecyclerViewScrollbarFrameLayout(Context context) {
		super(context);

		init();
	}

	public FixRecyclerViewScrollbarFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	private void init() {
		RecyclerView verticalRecyclerView = (RecyclerView) LayoutInflater.from(getContext()).inflate(R.layout.fix_recyclerview_scrollbar, (ViewGroup) getParent(), false);
		verticalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
		addView(verticalRecyclerView);
	}

}