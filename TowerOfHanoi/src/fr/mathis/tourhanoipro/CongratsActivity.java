package fr.mathis.tourhanoipro;

import java.text.DecimalFormat;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

public class CongratsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		getWindow().setFlags(LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_congrats);

		int userMovements = getIntent().getExtras().getInt("userMovements");
		int totalMovements = getIntent().getExtras().getInt("totalMovements");
		long time = getIntent().getExtras().getLong("time");

		TextView tvMovements = (TextView) findViewById(R.id.tv_movements);
		tvMovements.setText("" + userMovements);

		TextView tvSecondes = (TextView) findViewById(R.id.tv_secondes);
		TextView tvMinutes = (TextView) findViewById(R.id.tv_minutes);

		int minutes = (int) time / (60000);
		long secondes = time % (60000);

		DecimalFormat df = null;
		if (time < 20000) {
			df = new DecimalFormat("###################.0");
		} else {
			df = new DecimalFormat("###################");
		}

		String secondsText = df.format(((float) secondes / 1000.0f));

		try {
			Integer.parseInt(secondsText.charAt(0) + "");
		} catch (NumberFormatException nfe) {
			secondsText = "0" + secondsText;
		}

		try {
			Integer.parseInt(secondsText.charAt(secondsText.length() - 2) + "");
		} catch (NumberFormatException nfe) {
			if (secondsText.endsWith("0"))
				secondsText = secondsText.substring(0, secondsText.length() - 2);
		}

		tvSecondes.setText(secondsText);
		tvMinutes.setText("" + (minutes < 10 ? "0" + minutes : minutes));

		if (time < 60000) {
			findViewById(R.id.ll_minutes).setVisibility(View.GONE);
			((TextView) findViewById(R.id.tv_secondes_desc)).setText(getString(R.string.s47));
		}

		TextView tvPerfect = (TextView) findViewById(R.id.tv_perfect);

		if (userMovements == totalMovements) {
			tvPerfect.setVisibility(View.VISIBLE);
		} else {
			tvPerfect.setVisibility(View.GONE);
		}

		TextView tvTowerSize = (TextView) findViewById(R.id.tv_towerSize);
		tvTowerSize.setText(getString(R.string.s44).replace(":tower", "" + (int) (Math.log(totalMovements + 1) / Math.log(2))));

		findViewById(R.id.fl_container).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (MotionEvent.ACTION_DOWN == ev.getAction()) {
			Rect dialogBounds = new Rect();
			getWindow().getDecorView().getHitRect(dialogBounds);
			if (!dialogBounds.contains((int) ev.getX(), (int) ev.getY())) {
				finish();
				return false;
			}
		}

		return super.onTouchEvent(ev);
	}
}
