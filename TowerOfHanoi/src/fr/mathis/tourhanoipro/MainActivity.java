package fr.mathis.tourhanoipro;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;

import fr.mathis.tourhanoipro.adapter.DisksSpinnerAdapter;
import fr.mathis.tourhanoipro.interfaces.HelpListener;
import fr.mathis.tourhanoipro.interfaces.QuickTouchListener;
import fr.mathis.tourhanoipro.interfaces.TurnListener;
import fr.mathis.tourhanoipro.listener.SwipeToDismissTouchListener;
import fr.mathis.tourhanoipro.tools.DataManager;
import fr.mathis.tourhanoipro.tools.Tools;
import fr.mathis.tourhanoipro.views.GameView;

public class MainActivity extends ActionBarActivity implements TurnListener, ConnectionCallbacks, OnConnectionFailedListener, QuickTouchListener {

	static final int DRAWER_NEW_GAME = 1;
	static final int DRAWER_ACHIEVEMENTS = 2;
	static final int DRAWER_LEADERBOARDS = 3;
	static final int DRAWER_TUTO = 4;
	static final int DRAWER_RESTORE_GAME = 5;

	static final int MENU_QUICK_TOUCH_ENABLE = 1;
	static final int MENU_DECONNECT = 2;
	static final int MENU_RIGHT_DRAWER = 3;
	static final int MENU_QUICK_TOUCH_MODIFY = 4;
	static final int MENU_QUICK_TOUCH_REMOVE = 5;

	static final int POPUP_MENU_PLAY = 0;
	static final int POPUP_MENU_DELETE = 1;

	static final int RESULT_RESOLVER = 4;
	static final int RESULT_CONGRATS = 0;
	static final int RESULT_ACHIVEMENTS = 1;
	static final int RESULT_LEADERBOARDS = 2;
	static final int RESULT_TUTORIAL = 5;

	static final int RC_SIGN_IN = 9001;

	private boolean mResolvingConnectionFailure = false;
	private boolean mAutoStartSignInFlow = true;
	private boolean mSignInClicked = false;

	DrawerLayout mDrawerLayout;
	ActionBarDrawerToggle mDrawerToggle;
	View leftDrawer;
	View rightDrawer;
	boolean isSlideLock = false;
	GameView currentGame;
	GameView animateGame;

	MenuItem menuItemDeconnection;
	MenuItem menuItemSmallTouchEnable;
	MenuItem menuItemSmallTouchModify;
	MenuItem menuItemSmallTouchRemove;
	MenuItem openRightDrawer;
	boolean showMenu = true;
	String actionBarSubtitle = null;
	int currentGameIndex = 0;
	ArrayList<String> allGames;
	RecyclerView rvSavedGames;
	GameAdapter adapter;
	boolean wantstoShowHelp;

	boolean spinnerCreatedShouldAvoidFirstItemSelection = true;

	int pendingDrawerAction = -1;
	Object pendingDrawerActionData;

	GoogleApiClient mGoogleApiClient;
	boolean tutorialShownConnectionAfterResume = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.AppBaseTheme);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		int error = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (error == ConnectionResult.SUCCESS)
			mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Games.API).addScope(Games.SCOPE_GAMES).build();

		currentGame = (GameView) findViewById(R.id.gameView1);
		animateGame = (GameView) findViewById(R.id.gameView2);
		allGames = DataManager.GetAllSavedGames(getApplicationContext());

		isSlideLock = DataManager.GetMemorizedValueBoolean("lock", getApplicationContext());

		initDrawer();
		initEvents();
		initSpinner();
		initListView();

		getWindow().setBackgroundDrawable(null);

		if (!DataManager.GetMemorizedValueBoolean("showTutoFirstTime2", getApplicationContext())) {
			tutorialShownConnectionAfterResume = true;
			DataManager.MemorizeValue("showTutoFirstTime2", true, getApplicationContext());
			showTutorial(false);
		}

		showClosedIcon();
	}

	private void initListView() {
		rvSavedGames = (RecyclerView) findViewById(R.id.rvSavedGames);
		rvSavedGames.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		rvSavedGames.setHasFixedSize(true);
		rvSavedGames.setVerticalScrollBarEnabled(false);
		adapter = new GameAdapter(LayoutInflater.from(this));
		rvSavedGames.setAdapter(adapter);

		SwipeToDismissTouchListener swipeToDismissTouchListener = new SwipeToDismissTouchListener(rvSavedGames, new SwipeToDismissTouchListener.DismissCallbacks() {
			@Override
			public SwipeToDismissTouchListener.SwipeDirection canDismiss(int position) {
				if (position == 0 || !adapter.showAllSavedGames)
					return SwipeToDismissTouchListener.SwipeDirection.NONE;
				return SwipeToDismissTouchListener.SwipeDirection.LEFT;
			}

			@Override
			public void onDismiss(RecyclerView view, List<SwipeToDismissTouchListener.PendingDismissData> dismissData) {
				for (SwipeToDismissTouchListener.PendingDismissData data : dismissData) {
					deleteGameBySwypeOrMenu(data.position);
				}
			}
		});
		rvSavedGames.addOnItemTouchListener(swipeToDismissTouchListener);
	}

	private void initDrawer() {
		getSupportActionBar().setDisplayShowTitleEnabled(true);

		if (findViewById(R.id.drawer_layout) != null) {
			mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			leftDrawer = findViewById(R.id.left_drawer);
			rightDrawer = findViewById(R.id.right_drawer);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
				@SuppressLint("NewApi")
				public void onDrawerClosed(View view) {
					if (pendingDrawerAction >= 0) {
						if (pendingDrawerAction == DRAWER_NEW_GAME) {
							restartGame();
						} else if (pendingDrawerAction == DRAWER_TUTO) {
							showTutorial(true);
							DataManager.MemorizeValue("helpCompleted", false, getApplicationContext());
							DataManager.MemorizeValue("shownRightDrawerAfterFirstNewGame", false, getApplicationContext());
						} else if (pendingDrawerAction == DRAWER_ACHIEVEMENTS) {
							if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
								startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), RESULT_ACHIVEMENTS);
							}
						} else if (pendingDrawerAction == DRAWER_LEADERBOARDS) {
							if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
								startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient), RESULT_LEADERBOARDS);
							}
						} else if (pendingDrawerAction == DRAWER_RESTORE_GAME && pendingDrawerActionData instanceof Integer) {
							Integer index = (Integer) pendingDrawerActionData;

							String sOldGame = currentGame.saveGameAsString();

							String newGame = allGames.get(index);
							allGames.remove(Integer.parseInt(index.toString()));
							allGames.add(0, newGame);
							currentGame.launchGame(allGames.get(currentGameIndex));
							adapter.notifyDataSetChanged();

							animateGame.launchGame(sOldGame);
							animateGame.setQt(currentGame.getQt());
							animateGame.setVisibility(View.VISIBLE);
							ObjectAnimator anim = ObjectAnimator.ofFloat(currentGame, "alpha", 0.0f, 1.0f);
							anim.setDuration(300);
							anim.addListener(new AnimatorListener() {

								@Override
								public void onAnimationStart(Animator animation) {

								}

								@Override
								public void onAnimationRepeat(Animator animation) {

								}

								@Override
								public void onAnimationEnd(Animator animation) {
									animateGame.setVisibility(View.GONE);
								}

								@Override
								public void onAnimationCancel(Animator animation) {

								}
							});
							anim.start();
						}
						pendingDrawerAction = -1;
						pendingDrawerActionData = null;
					}

					rvSavedGames.scrollToPosition(currentGameIndex);

					showClosedIcon();
				}

				public void onDrawerOpened(View drawerView) {
					if (drawerView == rightDrawer) {
						DataManager.MemorizeValue("shownRightDrawerAfterFirstNewGame", true, getApplicationContext());
					}
					showOpenedIcon();
				}

				@Override
				public void onDrawerSlide(View drawerView, float slideOffset) {
					if (slideOffset < 0.1)
						currentGame.cleanTouch();
					if (drawerView == leftDrawer) {
						super.onDrawerSlide(drawerView, slideOffset);
					}
				}
			};
			mDrawerLayout.setDrawerListener(mDrawerToggle);
		}
	}

	private void initEvents() {
		findViewById(R.id.btn_newGame).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pendingDrawerAction = DRAWER_NEW_GAME;
				closeDrawer();
			}
		});

		findViewById(R.id.btn_tutorial).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pendingDrawerAction = DRAWER_TUTO;
				closeDrawer();
			}
		});

		findViewById(R.id.sign_in_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (mGoogleApiClient == null) {
					int error = GooglePlayServicesUtil.isGooglePlayServicesAvailable(MainActivity.this);
					if (error == ConnectionResult.SUCCESS)
						mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this).addConnectionCallbacks(MainActivity.this).addOnConnectionFailedListener(MainActivity.this).addApi(Games.API).addScope(Games.SCOPE_GAMES).build();
				}

				if (mGoogleApiClient != null)
					mGoogleApiClient.connect();

				mSignInClicked = true;
			}
		});

		findViewById(R.id.btn_achievement).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pendingDrawerAction = DRAWER_ACHIEVEMENTS;
				closeDrawer();
			}
		});

		findViewById(R.id.btn_leaderboard).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pendingDrawerAction = DRAWER_LEADERBOARDS;
				closeDrawer();
			}
		});

		if (findViewById(R.id.iv_drawerLock) != null) {

			ImageView ivDrawerLock = (ImageView) findViewById(R.id.iv_drawerLock);

			ivDrawerLock.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					isSlideLock = !isSlideLock;
					DataManager.MemorizeValue("lock", isSlideLock, getApplicationContext());
					((ImageView) v).setImageResource(isSlideLock ? R.drawable.ic_drawer_unlocked : R.drawable.ic_drawer_locked);

					if (isSlideLock && mDrawerLayout != null)
						mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
					if (mDrawerLayout != null)
						mDrawerLayout.closeDrawers();
					supportInvalidateOptionsMenu();
				}
			});

			ivDrawerLock.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					Toast.makeText(getApplicationContext(), R.string.s71, Toast.LENGTH_SHORT).show();

					return true;
				}
			});

			ivDrawerLock.setImageResource(isSlideLock ? R.drawable.ic_drawer_unlocked : R.drawable.ic_drawer_locked);
		}

		currentGame.setTurnListener(this);
		currentGame.setQuickTouchListener(this);
	}

	private void initSpinner() {
		Spinner spinner = (Spinner) findViewById(R.id.spinnerCircles);
		DisksSpinnerAdapter choices = new DisksSpinnerAdapter(this);
		spinner.setAdapter(choices);

		int currentNumberOfCircle = -1;
		int spinnerPosition = -1;

		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
		currentNumberOfCircle = Integer.parseInt(mgr.getString("nbCircles", "5"));

		String[] circles = getResources().getStringArray(R.array.circles);
		for (int i = 0; i < circles.length; i++) {
			if (currentNumberOfCircle == Integer.parseInt(circles[i])) {
				spinnerPosition = i;
				break;
			}
		}

		spinner.setSelection(spinnerPosition);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (!spinnerCreatedShouldAvoidFirstItemSelection) {
					String[] circles = getResources().getStringArray(R.array.circles);
					int nbOfDisks = Integer.parseInt(circles[arg2]);

					SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
					SharedPreferences.Editor editor = mgr.edit();
					editor.putString("nbCircles", nbOfDisks + "");
					editor.commit();

					pendingDrawerAction = DRAWER_NEW_GAME;
					closeDrawer();
				} else {
					spinnerCreatedShouldAvoidFirstItemSelection = false;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
	}

	@SuppressLint("NewApi")
	private void initHelpPopup(boolean show) {
		wantstoShowHelp = false;
		if (show) {
			if (!DataManager.GetMemorizedValueBoolean("helpCompleted", getApplicationContext())) {

				final View stepContainer = findViewById(R.id.rl_help);
				final View step0 = findViewById(R.id.ll_help_step0);
				final View step1 = findViewById(R.id.ll_help_step1);
				final View step2 = findViewById(R.id.ll_help_step2);

				stepContainer.setVisibility(View.VISIBLE);
				step0.setVisibility(View.VISIBLE);
				step1.setVisibility(View.GONE);
				step2.setVisibility(View.GONE);

				float dpWidth = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().density;
				if (dpWidth > 400)
					stepContainer.getLayoutParams().width = Tools.convertDpToPixel(300);
				else
					findViewById(R.id.rl_help).getLayoutParams().width = -1;

				ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(stepContainer, "alpha", 0.0f, 1.0f);
				alphaAnimator.setDuration(500);
				alphaAnimator.start();

				step0.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						wantstoShowHelp = true;
						ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(stepContainer, "alpha", 1.0f, 0.0f);
						alphaAnimator.setDuration(500);
						alphaAnimator.addListener(new AnimatorListener() {

							@Override
							public void onAnimationStart(Animator animation) {
							}

							@Override
							public void onAnimationRepeat(Animator animation) {
							}

							@Override
							public void onAnimationEnd(Animator animation) {
								step0.setVisibility(View.GONE);
								step1.setVisibility(View.VISIBLE);
								step2.setVisibility(View.GONE);
								ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(stepContainer, "alpha", 0.0f, 1.0f);
								alphaAnimator.setDuration(500);
								alphaAnimator.start();
								MainActivity.this.currentGame.setDrawHelpLine(true);
							}

							@Override
							public void onAnimationCancel(Animator animation) {
							}
						});
						alphaAnimator.start();

					}
				});

				currentGame.setHelpListener(new HelpListener() {

					@Override
					public void stepPassed(int step) {

						if (step == 0 && wantstoShowHelp) {
							ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(stepContainer, "alpha", 1.0f, 0.0f);
							alphaAnimator.setDuration(500);
							alphaAnimator.addListener(new AnimatorListener() {

								@Override
								public void onAnimationStart(Animator animation) {
								}

								@Override
								public void onAnimationRepeat(Animator animation) {
								}

								@Override
								public void onAnimationEnd(Animator animation) {
									step0.setVisibility(View.GONE);
									step1.setVisibility(View.GONE);
									step2.setVisibility(View.VISIBLE);
									ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(stepContainer, "alpha", 0.0f, 1.0f);
									alphaAnimator.setDuration(500);
									alphaAnimator.start();
								}

								@Override
								public void onAnimationCancel(Animator animation) {
								}
							});
							alphaAnimator.start();
						} else {
							if (wantstoShowHelp) {
								DataManager.MemorizeValue("helpCompleted", true, getApplicationContext());
							}

							ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(stepContainer, "alpha", 1.0f, 0.0f);
							alphaAnimator.setDuration(500);
							alphaAnimator.addListener(new AnimatorListener() {

								@Override
								public void onAnimationStart(Animator animation) {
								}

								@Override
								public void onAnimationRepeat(Animator animation) {
								}

								@Override
								public void onAnimationEnd(Animator animation) {
									step0.setVisibility(View.GONE);
									step1.setVisibility(View.GONE);
									step2.setVisibility(View.GONE);
									stepContainer.setVisibility(View.GONE);
								}

								@Override
								public void onAnimationCancel(Animator animation) {
								}
							});
							alphaAnimator.start();
						}
					}

				});
			}
		} else {
			final View stepContainer = findViewById(R.id.rl_help);
			stepContainer.setVisibility(View.GONE);
		}

	}

	private void showClosedIcon() {
		if (isSlideLock && mDrawerLayout != null)
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		getSupportActionBar().setTitle(actionBarSubtitle);
		if (mDrawerToggle != null)
			mDrawerToggle.syncState();
		currentGame.cleanTouch();

		showMenu = true;
		supportInvalidateOptionsMenu();
	}

	private void showOpenedIcon() {

		if (getSupportActionBar().getTitle().toString().length() > 0) {
			actionBarSubtitle = getSupportActionBar().getTitle().toString();
			getSupportActionBar().setTitle("");
		}

		if (isSlideLock && mDrawerLayout != null)
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

		if (mDrawerToggle != null)
			mDrawerToggle.syncState();
		currentGame.cleanTouch();

		showMenu = false;
		supportInvalidateOptionsMenu();
	}

	private void closeDrawer() {
		if (mDrawerLayout != null)
			mDrawerLayout.closeDrawers();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.clear();

		menuItemSmallTouchEnable = menu.add(0, MENU_QUICK_TOUCH_ENABLE, 0, R.string.s49);
		menuItemSmallTouchEnable.setIcon(R.drawable.ic_action_smalltouch);
		MenuItemCompat.setShowAsAction(menuItemSmallTouchEnable, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);

		menuItemSmallTouchModify = menu.add(0, MENU_QUICK_TOUCH_MODIFY, 0, R.string.s89);
		menuItemSmallTouchRemove = menu.add(0, MENU_QUICK_TOUCH_REMOVE, 0, R.string.s50);
		MenuItemCompat.setShowAsAction(menuItemSmallTouchModify, MenuItemCompat.SHOW_AS_ACTION_NEVER);
		MenuItemCompat.setShowAsAction(menuItemSmallTouchRemove, MenuItemCompat.SHOW_AS_ACTION_NEVER);

		menuItemDeconnection = menu.add(0, MENU_DECONNECT, 0, R.string.s41);
		menuItemDeconnection.setVisible((mDrawerLayout == null || mDrawerLayout.isDrawerOpen(leftDrawer)) && mGoogleApiClient != null && mGoogleApiClient.isConnected());

		openRightDrawer = menu.add(0, MENU_RIGHT_DRAWER, 1, R.string.s70).setIcon(R.drawable.ic_action_communication_clear_all);
		MenuItemCompat.setShowAsAction(openRightDrawer, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		openRightDrawer.setVisible(isSlideLock);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(MENU_QUICK_TOUCH_MODIFY).setVisible(showMenu && (currentGame != null && currentGame.getQt() != null));
		menu.findItem(MENU_QUICK_TOUCH_REMOVE).setVisible(showMenu && (currentGame != null && currentGame.getQt() != null));
		menu.findItem(MENU_QUICK_TOUCH_ENABLE).setVisible(showMenu && (currentGame == null || currentGame.getQt() == null));

		menu.findItem(MENU_RIGHT_DRAWER).setVisible(showMenu && isSlideLock);
		menu.findItem(MENU_DECONNECT).setVisible((mDrawerLayout == null || mDrawerLayout.isDrawerOpen(leftDrawer)) && mGoogleApiClient != null && mGoogleApiClient.isConnected());

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			if (mDrawerLayout.isDrawerOpen(rightDrawer))
				mDrawerLayout.closeDrawer(rightDrawer);
			else
				mDrawerToggle.onOptionsItemSelected(item);

			return true;
		case MENU_RIGHT_DRAWER:
			if (mDrawerLayout != null) {
				mDrawerLayout.openDrawer(rightDrawer);
			}
			break;
		case MENU_QUICK_TOUCH_ENABLE:
			currentGame.activateQuickTouchMode();

			menuItemSmallTouchEnable.setVisible(false);

			initHelpPopup(true);

			return true;
		case MENU_QUICK_TOUCH_MODIFY:
			currentGame.enterEditMode();
			break;
		case MENU_QUICK_TOUCH_REMOVE:
			currentGame.activateQuickTouchMode();

			menuItemSmallTouchRemove.setVisible(false);
			menuItemSmallTouchModify.setVisible(false);
			menuItemSmallTouchEnable.setVisible(true);
			break;
		case MENU_DECONNECT:
			if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
				Games.signOut(mGoogleApiClient);
				mGoogleApiClient.disconnect();
				mGoogleApiClient = null;
			}
			UpdateLoginPanel(false);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (mDrawerToggle != null)
			mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mDrawerToggle != null)
			mDrawerToggle.onConfigurationChanged(newConfig);

		float dpWidth = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().density;
		if (dpWidth > 400)
			findViewById(R.id.rl_help).getLayoutParams().width = Tools.convertDpToPixel(300);
		else
			findViewById(R.id.rl_help).getLayoutParams().width = -1;
	}

	@Override
	protected void onPause() {
		allGames.set(currentGameIndex, currentGame.saveGameAsString());
		DataManager.SaveAllGames(allGames, getApplicationContext());
		super.onPause();
	}

	@SuppressLint("InlinedApi")
	@Override
	protected void onResume() {

		allGames = DataManager.GetAllSavedGames(getApplicationContext());
		adapter.notifyDataSetChanged();

		if (allGames.size() == 0) {
			currentGame.createNewGame();
			allGames.add(0, currentGame.saveGameAsString());
		} else {
			currentGame.launchGame(allGames.get(0));
		}

		// change the default blue scroll glow
		int glowDrawableId = getApplicationContext().getResources().getIdentifier("overscroll_glow", "drawable", "android");
		if (glowDrawableId > 0) {
			Drawable androidGlow = getApplicationContext().getResources().getDrawable(glowDrawableId);
			androidGlow.setColorFilter(Color.parseColor(getString(R.color.primary_color)), PorterDuff.Mode.SRC_ATOP);
		}

		// change the default blue scroll edge
		int edgeDrawableId = getApplicationContext().getResources().getIdentifier("overscroll_edge", "drawable", "android");
		if (edgeDrawableId > 0) {
			Drawable androidEdge = getApplicationContext().getResources().getDrawable(edgeDrawableId);
			androidEdge.setColorFilter(Color.parseColor(getString(R.color.primary_color)), PorterDuff.Mode.SRC_ATOP);
		}

		super.onResume();
	}

	@Override
	protected void onActivityResult(int request, int response, Intent data) {

		switch (request) {
		case RC_SIGN_IN:
			mSignInClicked = false;
			mResolvingConnectionFailure = false;
			if (response == RESULT_OK) {
				mGoogleApiClient.connect();
			} else {
				BaseGameUtils.showActivityResultError(this, request, response, R.string.sign_in_failed);
			}

			break;
		case RESULT_CONGRATS:
			restartGame();
			break;
		case RESULT_ACHIVEMENTS:
		case RESULT_LEADERBOARDS:
			if (allGames.size() == 0) {
				currentGame.createNewGame();
				allGames.add(0, currentGame.saveGameAsString());
			} else {
				currentGame.launchGame(allGames.get(0));
			}
			break;
		case RESULT_TUTORIAL:
			if (tutorialShownConnectionAfterResume) {
				if (mGoogleApiClient != null && !mGoogleApiClient.isConnecting())
					mGoogleApiClient.connect();
				tutorialShownConnectionAfterResume = false;
			}
			break;
		default:
			break;
		}

		super.onActivityResult(request, response, data);
	}

	static Handler handler = new Handler();

	@Override
	public void turnPlayed(final int nbCoup, int nbTotal) {
		actionBarSubtitle = nbCoup + " / " + nbTotal;
		getSupportActionBar().setTitle(actionBarSubtitle);
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
			if (2147483647 == nbTotal) {
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_are_you_crazy_));
			}
		}
	}

	@Override
	public void gameFinished(int nbCoup, int nbTotal, long miliseconds) {
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_beginner), (int) (Math.log(nbCoup + 1) / Math.log(2)));
			Games.Achievements.increment(mGoogleApiClient, getString(R.string.achievement_expert), (int) (Math.log(nbCoup + 1) / Math.log(2)));
			if (nbTotal == 1) {
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_level_1));
			} else if (nbTotal == Math.pow(2, 2) - 1) {
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_level_2));
			} else if (nbTotal == Math.pow(2, 3) - 1) {
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_level_3));
			} else if (nbTotal == Math.pow(2, 4) - 1) {
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_level_4));
			} else if (nbTotal == Math.pow(2, 5) - 1) {
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_level_5));
				if (nbCoup == Math.pow(2, 5) - 1)
					Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_5));
				Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_fastest_for_5_disks), miliseconds);
			} else if (nbTotal == Math.pow(2, 6) - 1) {
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_level_6));
				if (nbCoup == Math.pow(2, 6) - 1)
					Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_6));
				Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_fastest_for_6_disks), miliseconds);
			} else if (nbTotal == Math.pow(2, 7) - 1) {
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_level_7));
				if (nbCoup == Math.pow(2, 7) - 1)
					Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_7));
				Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_fastest_for_7_disks), miliseconds);
			} else if (nbTotal == Math.pow(2, 8) - 1) {
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_level_8));
				if (nbCoup == Math.pow(2, 8) - 1)
					Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_8));
				Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_fastest_for_8_disks), miliseconds);
			} else if (nbTotal == Math.pow(2, 9) - 1) {
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_level_9));
				if (nbCoup == Math.pow(2, 9) - 1)
					Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_9));
				Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_fastest_for_9_disks), miliseconds);
			} else if (nbTotal == Math.pow(2, 10) - 1) {
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_level_10));
				if (nbCoup == Math.pow(2, 10) - 1)
					Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_10));
				Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_fastest_for_10_disks), miliseconds);
			} else if (nbTotal == Math.pow(2, 11) - 1) {
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_level_11));
				if (nbCoup == Math.pow(2, 11) - 1)
					Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_11));
				Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_fastest_for_11_disks), miliseconds);
			} else if (nbTotal == Math.pow(2, 12) - 1) {
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_level_12));
				if (nbCoup == Math.pow(2, 12) - 1)
					Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_12));
				Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_fastest_for_12_disks), miliseconds);
			} else if (nbTotal == Math.pow(2, 13) - 1) {
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_level_13));
				if (nbCoup == Math.pow(2, 13) - 1)
					Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_13));
				Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_fastest_for_13_disks), miliseconds);
			} else if (nbTotal == Math.pow(2, 14) - 1) {
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_level_14));
				if (nbCoup == Math.pow(2, 14) - 1)
					Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_14));
				Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_fastest_for_14_disks), miliseconds);
			} else if (nbTotal == Math.pow(2, 15) - 1) {
				Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_level_15));
				if (nbCoup == Math.pow(2, 15) - 1)
					Games.Achievements.unlock(mGoogleApiClient, getString(R.string.achievement_perfect_15));
				Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_fastest_for_15_disks), miliseconds);
			}

			if (nbTotal == nbCoup) {
				Games.Leaderboards.submitScore(mGoogleApiClient, getString(R.string.leaderboard_perfect_score), (int) (Math.log(nbCoup + 1) / Math.log(2)));
			}
		}

		Intent i = new Intent(this, CongratsActivity.class);
		Bundle b = new Bundle();
		b.putInt("userMovements", nbCoup);
		b.putInt("totalMovements", nbTotal);
		b.putLong("time", miliseconds);
		i.putExtras(b);
		startActivityForResult(i, RESULT_CONGRATS);
	}

	@SuppressLint("NewApi")
	public void restartGame() {
		boolean oldGameIsContinuable = !(currentGame.isJustStarted() || currentGame.isFinished());
		String sOldGame = currentGame.saveGameAsString();
		allGames.set(currentGameIndex, sOldGame);
		currentGame.createNewGame();
		String s2 = currentGame.saveGameAsString();
		allGames.add(0, s2);
		DataManager.SaveAllGames(allGames, getApplicationContext());
		allGames = DataManager.GetAllSavedGames(getApplicationContext());

		adapter.notifyDataSetChanged();

		if (oldGameIsContinuable && !DataManager.GetMemorizedValueBoolean("shownRightDrawerAfterFirstNewGame", getApplicationContext())) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.s85).setMessage(R.string.s86).setCancelable(false).setPositiveButton(R.string.s87, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					mDrawerLayout.openDrawer(rightDrawer);
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}

		animateGame.setVisibility(View.VISIBLE);
		animateGame.launchGame(sOldGame);
		animateGame.setQt(currentGame.getQt());

		ObjectAnimator anim = ObjectAnimator.ofFloat(currentGame, "alpha", 0.0f, 1.0f);
		anim.setDuration(300);
		anim.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				animateGame.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}
		});
		anim.start();
	}

	public void showTutorial(boolean animate) {
		Intent i = new Intent(MainActivity.this, TutoActivity.class);
		i.putExtra("connectClient", animate && mGoogleApiClient != null && mGoogleApiClient.isConnected());
		startActivityForResult(i, RESULT_TUTORIAL);
		if (!animate)
			overridePendingTransition(0, 0);
		closeDrawer();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

		if (mResolvingConnectionFailure) {
			return;
		}

		if (mSignInClicked || mAutoStartSignInFlow) {
			mAutoStartSignInFlow = false;
			mSignInClicked = false;
			mResolvingConnectionFailure = true;

			if (!BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient, connectionResult, RC_SIGN_IN, getString(R.string.sign_in_failed))) {
				mResolvingConnectionFailure = false;
			}
		}

		UpdateLoginPanel(false);
	}

	@Override
	public void onConnected(Bundle arg0) {
		UpdateLoginPanel(true);
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void UpdateLoginPanel(boolean showConnectedLayout) {
		if (showConnectedLayout) {
			findViewById(R.id.sign_in_panel).setVisibility(View.GONE);
			findViewById(R.id.sign_in_panel).setClickable(false);
			findViewById(R.id.sign_in_panel).setFocusable(false);
			if (menuItemDeconnection != null)
				menuItemDeconnection.setVisible((mDrawerLayout == null || mDrawerLayout.isDrawerOpen(leftDrawer)) && mGoogleApiClient != null && mGoogleApiClient.isConnected());

			LinearLayout c = (LinearLayout) findViewById(R.id.container_play);
			c.setBackgroundColor(Color.parseColor("#FFFFFF"));
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				c.setAlpha(1);
			}
		} else {
			findViewById(R.id.sign_in_panel).setVisibility(View.VISIBLE);
			findViewById(R.id.sign_in_panel).setClickable(true);
			findViewById(R.id.sign_in_panel).setFocusable(true);
			if (menuItemDeconnection != null)
				menuItemDeconnection.setVisible(false);
			LinearLayout c = (LinearLayout) findViewById(R.id.container_play);
			c.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#80000000")));
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				c.setAlpha(0.7f);
			}
		}
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		if (!tutorialShownConnectionAfterResume && !mGoogleApiClient.isConnecting())
			mGoogleApiClient.connect();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!tutorialShownConnectionAfterResume && mGoogleApiClient != null && !mGoogleApiClient.isConnecting())
			mGoogleApiClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mGoogleApiClient != null)
			mGoogleApiClient.disconnect();
	}

	private void deleteGameBySwypeOrMenu(int position) {
		allGames.remove(position);

		if (allGames.size() <= GameAdapter.NB_CLOSE_GAME_COUNT + 1) {
			adapter.showAllSavedGames = false;
		}

		adapter.notifyItemRemoved(position);
		adapter.notifyItemChanged(0);
		adapter.notifyItemChanged(1);
	}

	@Override
	public void quickTouchConstructed() {
		menuItemSmallTouchRemove.setVisible(true);
		menuItemSmallTouchModify.setVisible(true);
	}

	class GameVH extends RecyclerView.ViewHolder {
		View v;

		public GameVH(View itemView) {
			super(itemView);
			v = itemView;
		}
	}

	class GameAdapter extends RecyclerView.Adapter<GameVH> {
		static final int NB_CLOSE_GAME_COUNT = 2;

		LayoutInflater inflater;
		boolean showAllSavedGames;

		public GameAdapter(LayoutInflater inflater) {
			this.inflater = inflater;
			this.showAllSavedGames = false;
		}

		@Override
		public int getItemCount() {
			if (showAllSavedGames)
				return allGames.size();
			else
				return Math.min(allGames.size(), NB_CLOSE_GAME_COUNT + 1);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void onBindViewHolder(GameVH holder, int pos) {
			View v = holder.v;

			GameView gv = (GameView) v.findViewById(R.id.gv);
			View vClickableArea = v.findViewById(R.id.clickableArea);
			CheckBox cbSeeAll = (CheckBox) v.findViewById(R.id.cb_seeAll);

			gv.launchGame(allGames.get(pos));
			gv.setDisabled(true);

			v.findViewById(R.id.iv_playing).setVisibility(pos == currentGameIndex ? View.VISIBLE : View.INVISIBLE);

			if (pos == currentGameIndex) {
				vClickableArea.setBackgroundDrawable(null);
			} else {
				vClickableArea.setBackgroundResource(R.drawable.selectable_background_hanoi);
			}

			TextView tvSectionTitle = (TextView) v.findViewById(R.id.tv_sectionTitle);
			if (pos == 0) {
				tvSectionTitle.setText(R.string.s72);
				tvSectionTitle.setVisibility(View.VISIBLE);
				gv.setBackgroundColor(Color.LTGRAY);
				cbSeeAll.setVisibility(View.GONE);
			} else if (pos == 1) {
				tvSectionTitle.setText(R.string.s73);
				tvSectionTitle.setVisibility(View.VISIBLE);
				gv.setBackgroundColor(Color.WHITE);

				if (allGames.size() > NB_CLOSE_GAME_COUNT + 1)
					cbSeeAll.setVisibility(View.VISIBLE);
				else
					cbSeeAll.setVisibility(View.GONE);
			} else {
				tvSectionTitle.setVisibility(View.GONE);
				gv.setBackgroundColor(Color.WHITE);
				cbSeeAll.setVisibility(View.GONE);
			}

			final String gameAtCurrentPosition = allGames.get(pos);

			vClickableArea.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					int index = allGames.indexOf(gameAtCurrentPosition);

					if (index != currentGameIndex) {
						pendingDrawerActionData = Integer.valueOf(index);
						pendingDrawerAction = DRAWER_RESTORE_GAME;
						closeDrawer();
					}
				}
			});

			cbSeeAll.setOnCheckedChangeListener(null);

			cbSeeAll.setChecked(showAllSavedGames);

			cbSeeAll.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					showAllSavedGames = !showAllSavedGames;
					rvSavedGames.setVerticalScrollBarEnabled(showAllSavedGames);

					if (showAllSavedGames) {
						notifyItemRangeInserted(NB_CLOSE_GAME_COUNT + 1, allGames.size() - (NB_CLOSE_GAME_COUNT + 1) - 1);
					} else {
						notifyItemRangeRemoved(NB_CLOSE_GAME_COUNT + 1, allGames.size() - (NB_CLOSE_GAME_COUNT + 1));
					}
				}
			});

			Toolbar toolbarMenu = (Toolbar) v.findViewById(R.id.t_game);

			if (toolbarMenu.getMenu().size() == 0) {
				toolbarMenu.inflateMenu(R.menu.popup_save);
			}

			toolbarMenu.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

				@Override
				public boolean onMenuItemClick(MenuItem item) {

					if (item.getItemId() == R.id.menu_delete) {
						int index = allGames.indexOf(gameAtCurrentPosition);
						deleteGameBySwypeOrMenu(index);
					}
					return true;
				}
			});

			if (pos == currentGameIndex) {
				toolbarMenu.setVisibility(View.GONE);
			} else {
				toolbarMenu.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public GameVH onCreateViewHolder(ViewGroup parent, int viewType) {
			return new GameVH(inflater.inflate(R.layout.template_game, parent, false));
		}
	}

}
