package fr.mathis.tourhanoipro;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.games.Games;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

import fr.mathis.tourhanoipro.adapter.TutoPagerAdapter;
import fr.mathis.tourhanoipro.views.CustomPagerIndicator;

public class TutoActivity extends ActionBarActivity implements OnPageChangeListener, ConnectionCallbacks, OnConnectionFailedListener {

	ViewPager pager;
	TextView tvStep;
	CustomPagerIndicator cpi;
	View vSwypeIndicator;
	View vBottomContainer;
	View vBottomSeparator;
	ObjectAnimator alphaAnimator;
	GoogleApiClient mGoogleApiClient;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_tuto);

		getSupportActionBar().hide();

		if (getIntent().getBooleanExtra("connectClient", false)) {
			mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Games.API).addScope(Games.SCOPE_GAMES).build();
		}

		pager = (ViewPager) findViewById(R.id.tuto_pager);
		tvStep = (TextView) findViewById(R.id.tv_step);
		vSwypeIndicator = findViewById(R.id.v_swype_indicator);
		vBottomContainer = findViewById(R.id.rl_indicator_container);
		vBottomSeparator = findViewById(R.id.ll_indicator_separator);

		TutoPagerAdapter pagerAdapter = new TutoPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(pagerAdapter);
		pager.setOnPageChangeListener(this);
		pager.setOffscreenPageLimit(5);

		tvStep.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		cpi = (CustomPagerIndicator) findViewById(R.id.cpi_pager_indicator);

		alphaAnimator = ObjectAnimator.ofFloat(vSwypeIndicator, "alpha", 0.0f, 0.75f);
		alphaAnimator.setDuration(1000);
		alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
		alphaAnimator.setRepeatMode(ValueAnimator.REVERSE);
		alphaAnimator.start();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@SuppressLint("NewApi")
	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

		cpi.updateBounds(position, TutoPagerAdapter.NB_STEPS, positionOffset);

		if (position == 1) {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				vBottomContainer.setTranslationX(-positionOffsetPixels);
				vBottomSeparator.setTranslationX(-positionOffsetPixels);
			} else {
				ObjectAnimator anim1 = ObjectAnimator.ofFloat(vBottomContainer, "translationX", 0, -positionOffsetPixels);
				ObjectAnimator anim2 = ObjectAnimator.ofFloat(vBottomSeparator, "translationX", 0, -positionOffsetPixels);
				anim1.start();
				anim2.start();
			}
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onPageSelected(int pos) {
		if (alphaAnimator != null)
			alphaAnimator.cancel();

		if (pos + 1 == TutoPagerAdapter.NB_STEPS) {
			tvStep.setText(R.string.s52);

			Timer t = new Timer();
			t.schedule(new TimerTask() {

				@Override
				public void run() {
					runOnUiThread(new Runnable() {
						public void run() {
							finish();
							overridePendingTransition(0, 0);
						}
					});

				}
			}, 200);

		} else {
			tvStep.setText(R.string.s51);
		}
	}

	public void unloackHack(int id) {
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
			Games.Achievements.unlock(mGoogleApiClient, getString(id));
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {

	}

	@Override
	public void onConnected(Bundle arg0) {

	}

	@Override
	public void onConnectionSuspended(int arg0) {

	}

	@Override
	protected void onStart() {
		super.onStart();

		if (mGoogleApiClient != null && !mGoogleApiClient.isConnecting())
			mGoogleApiClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mGoogleApiClient != null)
			mGoogleApiClient.disconnect();
	}
}
