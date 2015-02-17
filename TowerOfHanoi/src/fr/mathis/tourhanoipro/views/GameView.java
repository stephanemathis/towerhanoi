package fr.mathis.tourhanoipro.views;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import fr.mathis.tourhanoipro.R;
import fr.mathis.tourhanoipro.interfaces.HelpListener;
import fr.mathis.tourhanoipro.interfaces.TurnListener;
import fr.mathis.tourhanoipro.model.ClassCircle;
import fr.mathis.tourhanoipro.model.ClassField;
import fr.mathis.tourhanoipro.model.ClassTower;
import fr.mathis.tourhanoipro.model.QuickTouch;
import fr.mathis.tourhanoipro.tools.Tools;

public class GameView extends View {

	public static int MODE_GOAL = 0;
	public static int MODE_MULTIPLE = 1;
	public static int MODE_SIZE = 2;

	int _viewHeight, _viewWidth;
	Rect npdBounds = new Rect(0, 0, 0, 0);
	Paint myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	PathEffect effect;
	int nb = 0;
	boolean quickZoneTouched = false;
	public boolean isBuildingQuickZone = false;
	Point dBuilding = null;
	Point eBuilding = null;
	public ClassField field;
	DisplayMetrics displaymetrics;
	int id;
	public int nbCircles;
	int nbCoups;
	int nbCoupsRequis;
	Point[] line;
	long gameDuration = 0;
	long lastGameReplayed = -1;
	Context c;
	TurnListener turnListener;
	HelpListener helpListener;
	boolean disabled = false;
	int currentMode = -1;
	Path path;
	boolean helpLine = false;

	public GameView(Context context) {
		super(context);
		c = context;
		init();
	}

	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		c = context;
		init();
	}

	private void init() {
		setFocusable(true);

		createNewGame();

		myPaint.setStyle(Paint.Style.FILL);
		myPaint.setTextSize(Tools.convertDpToPixel(32));
		myPaint.setStrokeWidth(Tools.convertDpToPixel(1.0f));
		myPaint.setDither(true);
		myPaint.setStrokeJoin(Paint.Join.ROUND);
		myPaint.setStrokeCap(Paint.Cap.ROUND);
		myPaint.setAntiAlias(true);
		float[] points = new float[2];
		points[0] = 5.0f;
		points[1] = 5.0f;
		effect = new DashPathEffect(points, 0.0f);
	}

	public void createNewGame() {
		SharedPreferences mgr = PreferenceManager.getDefaultSharedPreferences(c);
		createNewGame(Integer.parseInt(mgr.getString("nbCircles", "5")));
	}

	public void createNewGame(int size) {
		gameDuration = 0;
		lastGameReplayed = -1;

		QuickTouch q = null;
		if (field != null && field.getQt() != null)
			q = field.getQt();
		field = new ClassField();
		field.setQt(q);
		field.setId(0);
		ArrayList<ClassTower> towers = new ArrayList<ClassTower>();
		for (int i = 0; i < 3; i++) {
			ClassTower tower = new ClassTower();
			tower.setId(i);
			tower.setCircles(new ArrayList<ClassCircle>());
			towers.add(tower);
		}

		ArrayList<Integer> colors = new ArrayList<Integer>();
		colors.add(Color.parseColor("#33B5E5"));
		colors.add(Color.parseColor("#99CC00"));
		colors.add(Color.parseColor("#FF4444"));
		colors.add(Color.parseColor("#FFBB33"));
		colors.add(Color.parseColor("#AA66CC"));

		nbCircles = size;
		nbCoups = 0;
		nbCoupsRequis = (int) (Math.pow(2, nbCircles) - 1);
		if (turnListener != null) {
			turnListener.turnPlayed(0, nbCoupsRequis);
		}
		for (int i = nbCircles; i > 0; i--) {
			towers.get(0).getCircles().add(new ClassCircle(i, colors.get(i % 5)));
		}

		field.setTowers(towers);
		this.invalidate();
	}

	public void resetQuickTouchZone() {
		field.setQt(null);
	}

	public QuickTouch getQt() {
		if (field != null)
			return field.getQt();
		else
			return null;
	}

	public void setQt(QuickTouch qt) {
		if (field != null)
			field.setQt(qt);
	}

	public void cleanTouch() {
		line = new Point[2];
		path = new Path();
		quickZoneTouched = false;
		this.invalidate();
	}

	public void launchGame(String value) {

		ArrayList<Integer> colors = new ArrayList<Integer>();
		colors.add(Color.parseColor("#33B5E5"));
		colors.add(Color.parseColor("#99CC00"));
		colors.add(Color.parseColor("#FF4444"));
		colors.add(Color.parseColor("#FFBB33"));
		colors.add(Color.parseColor("#AA66CC"));

		String[] values = value.split(";");

		String v1 = values[0];
		String[] towers = v1.split(":");
		QuickTouch q = null;
		if (field != null && field.getQt() != null)
			q = field.getQt();
		field = new ClassField();
		field.setQt(q);
		field.setTowers(new ArrayList<ClassTower>());
		int i = 0;

		for (String t : towers) {
			if (i != 0) {
				ClassTower tower = new ClassTower();
				tower.setCircles(new ArrayList<ClassCircle>());

				field.getTowers().add(tower);

				if (t != null && t.length() != 0 && t.compareTo("n") != 0) {
					String[] circles = t.split(",");
					for (String c : circles) {
						if (i != 0) {
							if (c != null && c.length() != 0 && c.compareTo("n") != 0) {
								ClassCircle circle = new ClassCircle(Integer.parseInt(c), colors.get(Integer.parseInt(c) % 5));
								tower.getCircles().add(circle);
							}
						}
					}
				}
			}
			i++;
		}

		nbCircles = Integer.parseInt(values[1]);
		nbCoups = Integer.parseInt(values[2]);
		nbCoupsRequis = Integer.parseInt(values[3]);
		if (values.length > 4)
			gameDuration = Long.parseLong(values[4]);
		else
			gameDuration = 0;
		lastGameReplayed = -1;

		if (turnListener != null) {
			turnListener.turnPlayed(nbCoups, nbCoupsRequis);
		}

		this.invalidate();
	}

	public String saveGameAsString() {
		String res = "";

		String valuesString = "";

		for (ClassTower ct : field.getTowers()) {
			valuesString += ":";
			for (ClassCircle cc : ct.getCircles()) {
				valuesString += "," + cc.getId();
			}
			if (ct.getCircles().size() == 0)
				valuesString += "n";
		}

		valuesString += ";" + nbCircles;
		valuesString += ";" + nbCoups;
		valuesString += ";" + nbCoupsRequis;

		valuesString += ";" + (gameDuration + (lastGameReplayed > 0 ? System.currentTimeMillis() - lastGameReplayed : 0));

		res = valuesString;

		return res;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (disabled)
			return false;
		else {

			if (isBuildingQuickZone) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					dBuilding = new Point((int) event.getX(), (int) event.getY());
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					eBuilding = new Point((int) event.getX(), (int) event.getY());
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					if (dBuilding != null && eBuilding != null) {
						isBuildingQuickZone = false;

						int x, y, w, h;
						x = dBuilding.x < eBuilding.x ? dBuilding.x : eBuilding.x;
						y = dBuilding.y < eBuilding.y ? dBuilding.y : eBuilding.y;

						w = Math.abs(dBuilding.x - eBuilding.x);
						h = Math.abs(dBuilding.y - eBuilding.y);

						QuickTouch qt = new QuickTouch();
						if (helpLine) {
							qt.setWidth(Tools.convertDpToPixel(160.0f));
							qt.setHeight(Tools.convertDpToPixel(80.0f));
							qt.setTop(_viewHeight / 2 - Tools.convertDpToPixel(40.0f));
							qt.setLeft(_viewWidth - Tools.convertDpToPixel(168.0f));
						} else {
							qt.setWidth(w);
							qt.setHeight(h);
							qt.setTop(y);
							qt.setLeft(x);
						}

						field.setQt(qt);
						eBuilding = null;
						dBuilding = null;
						helpLine = false;
						if (helpListener != null)
							helpListener.stepPassed(0);
					} else {
						field.setQt(null);
						eBuilding = null;
						dBuilding = null;
						isBuildingQuickZone = false;
						helpLine = false;
					}
				}
				this.invalidate();
			} else {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					line = new Point[2];
					path = new Path();
					line[0] = new Point((int) event.getX(), (int) event.getY());
					path.moveTo(event.getX(), event.getY());
					if (field.getQt() != null) {
						int t, w, h, l;
						Point p = new Point((int) event.getX(), (int) event.getY());
						t = field.getQt().getTop();
						w = field.getQt().getWidth();
						h = field.getQt().getHeight();
						l = field.getQt().getLeft();

						if (p.x < l + w && p.x > l && p.y > t && p.y < t + h) {
							quickZoneTouched = true;
							p.x = p.x - l;

							if (p.x < w / 3)
								line[0] = new Point(_viewWidth / 6, _viewHeight * 2 / 5);
							else if (p.x < w * 2 / 3)
								line[0] = new Point(_viewWidth / 2, _viewHeight * 2 / 5);
							else
								line[0] = new Point(_viewWidth / 2 + _viewWidth / 6 + _viewWidth / 6, _viewHeight * 2 / 5);
						}
					}

				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					Point p = new Point((int) event.getX(), (int) event.getY());

					if (quickZoneTouched) {
						int t, w, h, l;
						t = field.getQt().getTop();
						w = field.getQt().getWidth();
						h = field.getQt().getHeight();
						l = field.getQt().getLeft();

						if (p.x < l)
							p.x = l;
						if (p.x > l + w)
							p.x = l + w;
						if (p.y < t)
							p.y = t;
						if (p.y > t + h)
							p.y = t + h;

						p.x = p.x - l;

						if (p.x < w / 3)
							line[1] = new Point(_viewWidth / 6, _viewHeight * 2 / 5);
						else if (p.x < w * 2 / 3)
							line[1] = new Point(_viewWidth / 2, _viewHeight * 2 / 5);
						else
							line[1] = new Point(_viewWidth / 2 + _viewWidth / 6 + _viewWidth / 6, _viewHeight * 2 / 5);

					} else {
						// line.add(p);
						line[1] = new Point((int) event.getX(), (int) event.getY());
						int historySize = event.getHistorySize();
						for (int i = 0; i < historySize; i++) {
							float historicalX = event.getHistoricalX(i);
							float historicalY = event.getHistoricalY(i);

							path.lineTo(historicalX, historicalY);
						}
						path.lineTo(event.getX(), event.getY());

					}
				} else if (event.getAction() == MotionEvent.ACTION_UP) {

					if (quickZoneTouched)
						if (helpListener != null)
							helpListener.stepPassed(1);

					moveOneCircle(line);
					line = new Point[2];
					quickZoneTouched = false;
					path = new Path();
				}

				this.invalidate();
			}

			return true;
		}
	}

	private void moveOneCircle(Point[] line) {
		if (line[0] != null && line[1] != null) {
			int startX = line[0].x;
			int endX = line[1].x;
			int startTower = 0;
			int endTower = 0;

			if (startX <= _viewWidth * 1 / 3)
				startTower = 0;
			else if (startX < _viewWidth * 2 / 3)
				startTower = 1;
			else
				startTower = 2;

			if (endX <= _viewWidth * 1 / 3)
				endTower = 0;
			else if (endX < _viewWidth * 2 / 3)
				endTower = 1;
			else
				endTower = 2;

			if (startTower != endTower) {

				if (lastGameReplayed == -1)
					lastGameReplayed = System.currentTimeMillis();

				int nbCirclesStartTower = field.getTowers().get(startTower).getCircles().size();
				ClassCircle toMove = null;
				ClassCircle toBeSecond = null;
				if (nbCirclesStartTower > 0) {
					boolean isAllowed = true;
					toMove = field.getTowers().get(startTower).getCircles().get(nbCirclesStartTower - 1);
					if (field.getTowers().get(endTower).getCircles().size() > 0) {
						toBeSecond = field.getTowers().get(endTower).getCircles().get(field.getTowers().get(endTower).getCircles().size() - 1);
					}
					if (toBeSecond != null && toMove.getId() > toBeSecond.getId()) {
						isAllowed = false;
					}
					if (isAllowed) {
						field.getTowers().get(startTower).getCircles().remove(nbCirclesStartTower - 1);
						field.getTowers().get(endTower).getCircles().add(toMove);
						nbCoups++;

						if (turnListener != null) {
							turnListener.turnPlayed(nbCoups, nbCoupsRequis);
						}
						CheckGameWin();

					}
				}
			}
		}
	}

	private void CheckGameWin() {
		ArrayList<ClassCircle> circles = field.getTowers().get(2).getCircles();
		boolean win = true;
		int value = nbCircles;
		if (circles.size() == nbCircles) {
			for (int i = 0; i < circles.size(); i++) {
				if (circles.get(i).getId() != value) {
					win = false;
					break;
				}
				value--;
			}
		}

		if (value != 0)
			win = false;

		if (!win) {
			circles = field.getTowers().get(1).getCircles();
			value = nbCircles;
			if (circles.size() == nbCircles) {
				for (int i = 0; i < circles.size(); i++) {
					if (circles.get(i).getId() != value) {
						win = false;
						break;
					}
					value--;
				}
			}

			if (value != 0)
				win = false;
		}

		if (win) {
			if (turnListener != null) {
				turnListener.gameFinished(nbCoups, nbCoupsRequis, (gameDuration + (System.currentTimeMillis() - lastGameReplayed)));
			}
		}
	}

	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas canvas) {
		if (canvas != null) {

			canvas.drawColor(Color.parseColor("#E8E8E8"));

			ClassCircle selectedCircle = null;
			ClassCircle belowCircle = null;
			// get circle index
			if (line != null && line[0] != null) {
				int startX = line[0].x;
				int endX = line[1] == null ? line[0].x : line[1].x;
				if (startX <= _viewWidth * 1 / 3) {
					selectedCircle = field.getTowers().get(0).getCircles().size() > 0 ? field.getTowers().get(0).getCircles().get(field.getTowers().get(0).getCircles().size() - 1) : null;
				} else if (startX < _viewWidth * 2 / 3) {
					selectedCircle = field.getTowers().get(1).getCircles().size() > 0 ? field.getTowers().get(1).getCircles().get(field.getTowers().get(1).getCircles().size() - 1) : null;
				} else {
					selectedCircle = field.getTowers().get(2).getCircles().size() > 0 ? field.getTowers().get(2).getCircles().get(field.getTowers().get(2).getCircles().size() - 1) : null;
				}

				if (endX <= _viewWidth * 1 / 3) {
					belowCircle = field.getTowers().get(0).getCircles().size() > 0 ? field.getTowers().get(0).getCircles().get(field.getTowers().get(0).getCircles().size() - 1) : null;
				} else if (endX < _viewWidth * 2 / 3) {
					belowCircle = field.getTowers().get(1).getCircles().size() > 0 ? field.getTowers().get(1).getCircles().get(field.getTowers().get(1).getCircles().size() - 1) : null;
				} else {
					belowCircle = field.getTowers().get(2).getCircles().size() > 0 ? field.getTowers().get(2).getCircles().get(field.getTowers().get(2).getCircles().size() - 1) : null;
				}

				if (selectedCircle != null) {
					if (belowCircle == null || selectedCircle.getId() <= belowCircle.getId())
						myPaint.setColor(selectedCircle.getColor());
					else
						myPaint.setColor(Color.DKGRAY);
					myPaint.setAlpha(40);
					int currentX = endX;
					if (currentX <= _viewWidth * 1 / 3)
						canvas.drawRect(0, 0, _viewWidth * 1 / 3, _viewHeight, myPaint);
					else if (currentX < _viewWidth * 2 / 3)
						canvas.drawRect(_viewWidth * 1 / 3, 0, _viewWidth * 2 / 3, _viewHeight, myPaint);
					else
						canvas.drawRect(_viewWidth * 2 / 3, 0, _viewWidth, _viewHeight, myPaint);
				}

				myPaint.setColor(Color.rgb(0, 0, 0));
				myPaint.setAlpha(255);
			}

			if (currentMode == MODE_MULTIPLE) {
				myPaint.setColor(Color.DKGRAY);
				myPaint.setAlpha(40);
				canvas.drawRect(_viewWidth * 2 / 3, 0, _viewWidth, _viewHeight, myPaint);
			}

			if (selectedCircle == null)
				selectedCircle = new ClassCircle(-1, -1);

			int x = 0;
			int y = _viewHeight;
			int i = 1;
			myPaint.setPathEffect(effect);

			if (field != null) {
				for (ClassTower tower : field.getTowers()) {
					x = (_viewWidth * i / 3) - ((_viewWidth * 1 / 3) / 2);
					y = _viewHeight;
					for (ClassCircle cercle : tower.getCircles()) {
						if (selectedCircle.getId() != cercle.getId()) {
							int circleWidth = (_viewWidth * 1 / 3 - 10) * ((cercle.getId()) * 100 / nbCircles) / 100;
							if (circleWidth == 0)
								circleWidth = 2;

							int circleHeight = Tools.convertDpToPixel(10.0f);
							circleHeight = _viewWidth / 3 / 10;

							if (circleHeight * nbCircles > _viewHeight) {
								circleHeight = (int) (_viewHeight * 0.95) / nbCircles;
							}

							myPaint.setColor(cercle.getColor());
							npdBounds.set(x - (circleWidth / 2), y - (circleHeight), x + (circleWidth / 2), y);

							if (currentMode == MODE_GOAL && i == 1) {
								myPaint.setAlpha(150);
							} else {
								myPaint.setAlpha(255);
							}
							canvas.drawRect(npdBounds, myPaint);
							y -= circleHeight;
						}
					}

					if (i != 3) {
						myPaint.setStrokeWidth(1);
						myPaint.setColor(Color.parseColor("#AAAAAA"));
						canvas.drawLine((_viewWidth * i / 3), 0, (_viewWidth * i / 3), _viewHeight, myPaint);
						myPaint.setStrokeWidth(Tools.convertDpToPixel(1.0f));
					}
					i++;
				}
			}
			myPaint.setPathEffect(null);
			int decalageTop = Tools.convertDpToPixel(18);

			// draw line
			if (!quickZoneTouched) {
				myPaint.setColor(selectedCircle.getColor());
				if (path != null) {
					myPaint.setStyle(Paint.Style.STROKE);
					canvas.drawPath(path, myPaint);
					myPaint.setStyle(Paint.Style.FILL);
				}
			}
			// draw selected circle
			if (line != null && line[0] != null && selectedCircle.getId() != -1) {
				int circleWidth = (_viewWidth * 1 / 3 - 10) * ((selectedCircle.getId()) * 100 / nbCircles) / 100;
				if (circleWidth == 0)
					circleWidth = 2;

				int circleHeight = Tools.convertDpToPixel(10.0f);
				circleHeight = _viewWidth / 3 / 10;

				if (circleHeight * nbCircles > _viewHeight) {
					circleHeight = (int) (_viewHeight * 0.95) / nbCircles;
				}

				if (line[1] != null) {
					x = line[1].x;
					y = line[1].y;
				} else {
					x = line[0].x;
					y = line[0].y;
				}
				npdBounds.set(x - (circleWidth / 2), y - (circleHeight / 2), x + (circleWidth / 2), y + (circleHeight / 2));
				myPaint.setColor(selectedCircle.getColor());
				canvas.drawRect(npdBounds, myPaint);

				canvas.drawText(selectedCircle.getId() + "", _viewWidth - myPaint.measureText(selectedCircle.getId() + "") - Tools.convertDpToPixel(16.0f), decalageTop + Tools.convertDpToPixel(16.0f), myPaint);
			}

			if (currentMode == MODE_MULTIPLE) {
				x = _viewWidth / 6 * 5;
				y = _viewHeight / 2;

				ClassCircle circle2 = new ClassCircle(1, Color.parseColor("#99CC00"));
				ClassCircle circle = new ClassCircle(2, Color.parseColor("#FF4444"));

				int circleWidth = (_viewWidth * 1 / 3 - 10) * ((circle.getId()) * 100 / nbCircles) / 100;
				if (circleWidth == 0)
					circleWidth = 2;

				int circleHeight = Tools.convertDpToPixel(10.0f);
				circleHeight = _viewWidth / 3 / 10;

				npdBounds.set(x - (circleWidth / 2), y - (circleHeight / 2), x + (circleWidth / 2), y + (circleHeight / 2));
				myPaint.setColor(circle.getColor());
				canvas.drawRect(npdBounds, myPaint);

				circleWidth = (_viewWidth * 1 / 3 - 10) * ((circle2.getId()) * 100 / nbCircles) / 100;
				if (circleWidth == 0)
					circleWidth = 2;

				npdBounds.set(x - (circleWidth / 2), y - (circleHeight / 2) - circleHeight, x + (circleWidth / 2), y + (circleHeight / 2) - circleHeight);
				myPaint.setColor(circle2.getColor());
				canvas.drawRect(npdBounds, myPaint);
			}

			if (isBuildingQuickZone && eBuilding != null && dBuilding != null) {
				int xx, yy, w, h;
				xx = dBuilding.x < eBuilding.x ? dBuilding.x : eBuilding.x;
				yy = dBuilding.y < eBuilding.y ? dBuilding.y : eBuilding.y;

				w = Math.abs(dBuilding.x - eBuilding.x);
				h = Math.abs(dBuilding.y - eBuilding.y);
				myPaint.setColor(Color.parseColor("#AAAAAA"));
				canvas.drawRect(xx, yy, xx + w, yy + h, myPaint);
			}

			if (helpLine) {

				Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
				mPaint.setColor(Color.parseColor(getContext().getString(R.color.ui_color)));
				mPaint.setStyle(Paint.Style.STROKE);
				mPaint.setStrokeWidth(Tools.convertDpToPixel(2.0f));
				mPaint.setPathEffect(new DashPathEffect(new float[] { Tools.convertDpToPixel(3), Tools.convertDpToPixel(1.4f) }, 0));
				mPaint.setAlpha(150);
				canvas.drawLine(_viewWidth - Tools.convertDpToPixel(160.0f) + Tools.convertDpToPixel(6), _viewHeight / 2 - Tools.convertDpToPixel(40.0f) + Tools.convertDpToPixel(6), _viewWidth - Tools.convertDpToPixel(16.0f) - Tools.convertDpToPixel(6), _viewHeight / 2 + Tools.convertDpToPixel(40.0f) - Tools.convertDpToPixel(6), mPaint);
				mPaint.setAlpha(255);
				canvas.drawCircle(_viewWidth - Tools.convertDpToPixel(160.0f), _viewHeight / 2 - Tools.convertDpToPixel(40.0f), Tools.convertDpToPixel(8), mPaint);
				canvas.drawCircle(_viewWidth - Tools.convertDpToPixel(16.0f), _viewHeight / 2 + Tools.convertDpToPixel(40.0f), Tools.convertDpToPixel(8), mPaint);
			}

			if (field != null && field.getQt() != null) {
				int t, w, h, l;
				t = field.getQt().getTop();
				w = field.getQt().getWidth();
				h = field.getQt().getHeight();
				l = field.getQt().getLeft();

				if (quickZoneTouched && line != null && line[0] != null) {
					if (line[1] != null) {
						x = line[1].x;
						y = line[1].y;
					} else {
						x = line[0].x;
						y = line[0].y;
					}

					if (x < _viewWidth / 3) {
						npdBounds.set(l, t, l + (w / 3), t + h);
					} else if (x < _viewWidth / 3 + _viewWidth / 3) {
						npdBounds.set(l + (w / 3), t, l + (w / 3) + (w / 3), t + h);
					} else {
						npdBounds.set(l + (w / 3) + (w / 3), t, l + (w / 3) + (w / 3) + (w / 3), t + h);
					}
					if (line != null && line[0] != null && selectedCircle.getId() != -1) {
						myPaint.setColor(selectedCircle.getColor());
					} else {
						myPaint.setColor(Color.parseColor("#AAAAAA"));
					}
					myPaint.setAlpha(150);
					canvas.drawRect(npdBounds, myPaint);
					myPaint.setAlpha(255);
				}
				if (line != null && line[0] != null && selectedCircle.getId() != -1 && quickZoneTouched) {
					myPaint.setColor(selectedCircle.getColor());
				} else {
					myPaint.setColor(Color.parseColor("#AAAAAA"));
				}

				myPaint.setPathEffect(effect);
				canvas.drawLine(l + w / 3, t, l + w / 3, t + h, myPaint);
				canvas.drawLine(l + w * 2 / 3, t, l + w * 2 / 3, t + h, myPaint);
				myPaint.setPathEffect(null);

				myPaint.setStyle(Paint.Style.STROKE);
				canvas.drawRect(l, t, l + w, t + h, myPaint);
				myPaint.setStyle(Paint.Style.FILL);

			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int desiredWidth = Tools.convertDpToPixel(100 * 3);
		int desiredHeight = Tools.convertDpToPixel(200 * 3);

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width;
		int height;

		if (widthMode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else if (widthMode == MeasureSpec.AT_MOST) {
			width = Math.min(desiredWidth, widthSize);
		} else {
			width = desiredWidth;
		}

		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else if (heightMode == MeasureSpec.AT_MOST) {
			height = Math.min(desiredHeight, heightSize);
		} else {
			height = desiredHeight;
		}

		_viewHeight = height;
		_viewWidth = width;
		setMeasuredDimension(width, height);
	}

	public TurnListener getTurnListener() {
		return turnListener;
	}

	public void setTurnListener(TurnListener turnListener) {
		this.turnListener = turnListener;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public HelpListener getHelpListener() {
		return helpListener;
	}

	public void setHelpListener(HelpListener helpListener) {
		this.helpListener = helpListener;
	}

	public void setDemoMode(int mode) {
		createNewGame(5);
		currentMode = mode;

		if (mode == MODE_SIZE) {

			field.getTowers().get(0).getCircles().remove(4);
			field.getTowers().get(0).getCircles().remove(3);

			ClassCircle circle = new ClassCircle(1, Color.parseColor("#99CC00"));
			field.getTowers().get(1).getCircles().add(circle);
			circle = new ClassCircle(2, Color.parseColor("#FF4444"));
			field.getTowers().get(1).getCircles().add(circle);
		} else if (mode == MODE_GOAL) {
			for (int i = 0; i < field.getTowers().get(0).getCircles().size(); i++) {
				field.getTowers().get(2).getCircles().add(field.getTowers().get(0).getCircles().get(i));
			}
		} else if (mode == MODE_MULTIPLE) {
			field.getTowers().get(0).getCircles().remove(4);
			field.getTowers().get(0).getCircles().remove(3);
		}

		invalidate();
	}

	public void activateQuickTouchMode() {

		if (isBuildingQuickZone || field.getQt() != null) {
			field.setQt(null);
			isBuildingQuickZone = false;
		} else {
			isBuildingQuickZone = true;
		}
		this.invalidate();
	}

	public void drawHelpLine(boolean b) {
		this.helpLine = b;
		invalidate();
	}
}