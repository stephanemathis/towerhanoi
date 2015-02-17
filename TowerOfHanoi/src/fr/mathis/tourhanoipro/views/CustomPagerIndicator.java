package fr.mathis.tourhanoipro.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import fr.mathis.tourhanoipro.tools.Tools;

public class CustomPagerIndicator extends View {

	private int _currentPosition = 0;
	private int _maxPosition = 0;
	private float _currentOffsset = 0.0f;

	private int _viewHeight = 0;
	private int _viewWidth = 0;
	private Paint _paint;

	public CustomPagerIndicator(Context context) {
		super(context);
		init();
	}

	public CustomPagerIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CustomPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		_paint.setStyle(Paint.Style.FILL);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int desiredWidth = Tools.convertDpToPixel(50);
		int desiredHeight = Tools.convertDpToPixel(3);

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

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		if (_viewHeight != 0 && _viewWidth != 0 && canvas != null) {

			int spacing = (_viewWidth / (_maxPosition + 1)) / (_maxPosition + 1);

			canvas.drawColor(Color.TRANSPARENT);
			_paint.setColor(Color.argb(255, 170, 102, 204));

			// paint rectangles
			_paint.setAlpha(204);

			for (int i = 0; i < _maxPosition; i++) {
				int startX, endX;
				startX = (_viewWidth / _maxPosition + 1) * i + spacing;
				endX = (_viewWidth / _maxPosition + 1) * (i + 1) - spacing;

				int width = endX - startX;

				if (i == _currentPosition || i == _currentPosition + 1) {

					if (i == _currentPosition) {
						_paint.setAlpha(150);
						canvas.drawRect(new Rect(startX, 0, startX + (int) (width * _currentOffsset), _viewHeight), _paint);
						_paint.setAlpha(255);
						canvas.drawRect(new Rect(startX + (int) (width * _currentOffsset), 0, endX, _viewHeight), _paint);
					} else {
						_paint.setAlpha(255);
						canvas.drawRect(new Rect(startX, 0, startX + (int) (width * _currentOffsset), _viewHeight), _paint);
						_paint.setAlpha(150);
						canvas.drawRect(new Rect(startX + (int) (width * _currentOffsset), 0, endX, _viewHeight), _paint);
					}

				} else {
					_paint.setAlpha(150);
					canvas.drawRect(new Rect(startX, 0, endX, _viewHeight), _paint);
				}

			}

		}
		super.onDraw(canvas);
	}

	public void updateBounds(int position, int max, float offset) {
		_currentPosition = position;
		_maxPosition = max;
		_currentOffsset = offset;
		if (_currentOffsset > 0.8f) {
			_currentPosition++;
			_currentOffsset = 0.0f;
		}
		invalidate();
	}

}
