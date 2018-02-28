package com.ardurasolutions.safekiddo.proto.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.helpers.Config;
import com.ardurasolutions.safekiddo.proto.view.GridPad.OnPadItemClick;
import com.ardurasolutions.safekiddo.proto.view.GridPad.PadItemKey;

public class PinView extends RelativeLayout {
	
	public static interface OnInputValidPin {
		public void onInputValidPin();
	}
	
	private int prevW = 0, prevH = 0;
	private GridPad pinPad;
	private CheckableLinearLayout[] pins;
	private String savedPin = "", userPIN = "";
	private OnInputValidPin mOnInputValidPin;
	private View content = null;

	public PinView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//init(context);
	}
	
	public PinView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//init(context);
	}
	
	public PinView(Context context) {
		super(context);
		//init(context);
	}
	
	private void init(final Context context) {
		
		userPIN = Config.getInstance(context).load(Config.KeyNames.USER_PIN, (String) null);
		
		content = LayoutInflater.from(context).inflate(R.layout.activity_pin_new, this, false);
		
		pinPad = (GridPad) content.findViewById(R.id.pinPad);
		pins = new CheckableLinearLayout[]{
			(CheckableLinearLayout) content.findViewById(R.id.pin1),
			(CheckableLinearLayout) content.findViewById(R.id.pin2),
			(CheckableLinearLayout) content.findViewById(R.id.pin3),
			(CheckableLinearLayout) content.findViewById(R.id.pin4)
		};
		
		pinPad.setOnPadItemClick(new OnPadItemClick() {
			@Override
			public void onPadItemClick(View v, PadItemKey pi) {
				if (pi == PadItemKey.PAD_ITEM_CUSTOM_LEFT || pi == PadItemKey.PAD_ITEM_CUSTOM_RIGHT) return;
				if (savedPin.length() >= userPIN.length()) return;
				
				savedPin += Integer.toString(pi.getValue());
				updatePinPoints();
				
				if (userPIN.length() == savedPin.length() && savedPin.length() > 0) {
					if (savedPin.equals(userPIN)) {
						if (getOnInputValidPin() != null) {
							getOnInputValidPin().onInputValidPin();
						}
					} else {
						for(int i=0; i<pins.length; i++)
							pins[i].setEnabled(false);
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								for(int i=0; i<pins.length; i++)
									pins[i].setEnabled(true);
								savedPin = "";
								updatePinPoints();
							}
						}, 700);
					}
				}
			}
		});
		
		content.findViewById(R.id.imageView1a).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (savedPin.length() <= 0) {
					return;
				}
				savedPin = savedPin.substring(0, savedPin.length()-1);
				updatePinPoints();
			}
		});
		
		if (context.getResources().getDisplayMetrics().heightPixels <= 480) {
			content.findViewById(R.id.textHeadBottom).setVisibility(View.GONE);
		}
		
		post(new Runnable() {
			@Override
			public void run() {
				onUpdateOrientation(getMeasuredWidth(), getMeasuredHeight());
				if (!isLandscapeMode(getMeasuredWidth(), getMeasuredHeight()) && getResources().getDisplayMetrics().widthPixels <= 480) {
					RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams((int) (220f * getResources().getDisplayMetrics().density), RelativeLayout.LayoutParams.WRAP_CONTENT);
					lp.addRule(RelativeLayout.BELOW, R.id.topPadd);
					content.findViewById(R.id.allBox).setLayoutParams(lp);
				}
			}
		});
	}
	
	private boolean isLandscapeMode(int w, int h) {
		return w > h;
	}
	
	private void updatePinPoints() {
		for(int i=0; i<pins.length; i++)
			pins[i].setChecked(false);
		
		if (savedPin.length() > 0) {
			for(int i=0; i<savedPin.length(); i++)
				pins[i].setChecked(true);
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		onUpdateOrientation(w, h);
	}
	
	private void onUpdateOrientation(int w, int h) {
		if (w != prevW || h != prevH) {
			prevW = w;
			prevH = h;
			
			removeAllViews();
			init(getContext());
			addView(content);
			post(new Runnable() {
				@Override
				public void run() {
					content.requestLayout();
				}
			});
		}
	}

	public OnInputValidPin getOnInputValidPin() {
		return mOnInputValidPin;
	}

	public void setOnInputValidPin(OnInputValidPin mOnInputValidPin) {
		this.mOnInputValidPin = mOnInputValidPin;
	}

}
