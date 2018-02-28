package com.ardurasolutions.safekiddo.proto.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.ardurasolutions.safekiddo.R;

public class GridPad extends GridView {
	
	public static enum PadItemKey {
		PAD_ITEM_0(0),
		PAD_ITEM_1(1),
		PAD_ITEM_2(2),
		PAD_ITEM_3(3),
		PAD_ITEM_4(4),
		PAD_ITEM_5(5),
		PAD_ITEM_6(6),
		PAD_ITEM_7(7),
		PAD_ITEM_8(8),
		PAD_ITEM_9(9),
		PAD_ITEM_CUSTOM_LEFT(-1),
		PAD_ITEM_CUSTOM_RIGHT(-2);
		
		private int id = 0;
		
		PadItemKey(int idx) {
			this.id = idx;
		}
		
		public int getValue() {
			return this.id;
		}
		
		public static PadItemKey fromInt(int val) {
			PadItemKey res = null;
			for(PadItemKey k : PadItemKey.values()) {
				if (val == k.getValue()) {
					res = k;
					break;
				}
			}
			return res;
		}
	}
	
	public static interface OnPadItemClick {
		public void onPadItemClick(View v, PadItemKey pi);
	}
	
	private GridPadAdapter LA;
	private OnPadItemClick mOnPadItemClick;
	private View.OnClickListener onItemClick;

	public GridPad(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public GridPad(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public GridPad(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		setCacheColorHint(0x00000000);
		
		onItemClick = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PadItemKey key = null;
				Integer i = (Integer) v.getTag();
				if (i != null) {
					key = PadItemKey.fromInt(i);
				}
				
				if (key != null && getOnPadItemClick() != null) {
					getOnPadItemClick().onPadItemClick(v, key);
				}
			}
		};
		
		LA = new GridPadAdapter();
		setAdapter(LA);
		/*setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PadItemKey key = null;
				Integer i = (Integer) view.getTag();
				if (i != null) {
					key = PadItemKey.fromInt(i);
				}
				
				if (key != null && getOnPadItemClick() != null) {
					getOnPadItemClick().onPadItemClick(view, key);
				}
			}
		});*/
	}
	
	public OnPadItemClick getOnPadItemClick() {
		return mOnPadItemClick;
	}

	public void setOnPadItemClick(OnPadItemClick mOnPadItemClick) {
		this.mOnPadItemClick = mOnPadItemClick;
	}

	private class GridPadAdapter extends BaseAdapter {
		
		public GridPadAdapter() { }

		@Override
		public int getCount() {
			return 12;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			convertView = new PinPadButton(getContext());
			convertView.setOnClickListener(onItemClick);
			
			Integer tag = null;
			if (position >= 0 && position < 9) {
				tag = position + 1;
			} else {
				switch (position) {
					case 10: tag = 0; break;
					case 11: tag = -1; break;
					case 9 : tag = -2; break;
				}
			}
			
			convertView.setTag(tag);
			
			if (tag >= 0) {
				if (!isInEditMode())
					convertView.setBackgroundResource(R.drawable.grid_pad_item_bg);
				((PinPadButton) convertView).setText("" + tag);
			} else {
				if (!isInEditMode())
					convertView.setBackgroundResource(R.drawable.trans);
				((PinPadButton) convertView).setText(" ");
			}
			
			return convertView;
		}
		
	}
	
	public int getRealHeight() {
		int w = getMeasuredWidth();
		return ((w / 3) * 4);
	}
	
	private class PinPadButton extends com.ardurasolutions.safekiddo.proto.view.TextViewHv {

		public PinPadButton(Context context) {
			super(context);
			if (!isInEditMode())
				setTextColor(getResources().getColor(R.color.sk_orange));
			setTextSize(TypedValue.COMPLEX_UNIT_SP, 29);
			setGravity(Gravity.CENTER);
		}
		
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int widthSize = MeasureSpec.getSize(widthMeasureSpec);
			setMeasuredDimension(widthSize, widthSize);
		}
		
	}

}
