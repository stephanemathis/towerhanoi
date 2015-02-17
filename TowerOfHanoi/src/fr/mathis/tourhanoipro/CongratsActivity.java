package fr.mathis.tourhanoipro;

import java.text.DecimalFormat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class CongratsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
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
		if (time < 20000)
			df = new DecimalFormat("###################.0");
		else {
			df = new DecimalFormat("###################");
		}
		tvSecondes.setText("" + df.format(((float) secondes / 1000.0f)));
		tvMinutes.setText("" + (minutes < 10 ? "0" + minutes : minutes));

		if (time < 60000) {
			findViewById(R.id.ll_minutes).setVisibility(View.GONE);
			((TextView) findViewById(R.id.tv_secondes_desc)).setText(getString(R.string.s47));
		}

		TextView tvPerfect = (TextView) findViewById(R.id.tv_perfect);
		TextView tvTotalMovements = (TextView) findViewById(R.id.tv_totalMovements);

		if (userMovements == totalMovements) {
			tvPerfect.setVisibility(View.VISIBLE);
			tvTotalMovements.setVisibility(View.GONE);
		} else {
			tvPerfect.setVisibility(View.GONE);
			tvTotalMovements.setVisibility(View.VISIBLE);
			tvTotalMovements.setText(getString(R.string.s45).replace(":m", totalMovements + ""));
		}

		TextView tvTowerSize = (TextView) findViewById(R.id.tv_towerSize);
		tvTowerSize.setText(getString(R.string.s44).replace(":tower", "" + (int) (Math.log(totalMovements + 1) / Math.log(2))));

	}

}
