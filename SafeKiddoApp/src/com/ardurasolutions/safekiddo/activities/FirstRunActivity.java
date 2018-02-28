package com.ardurasolutions.safekiddo.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.auth.AuthLogin;
import com.ardurasolutions.safekiddo.helpers.Constants;
import com.ardurasolutions.safekiddo.launcher.views.DesktopPager;
import com.ardurasolutions.safekiddo.launcher.views.DesktopPagerIndicator;

public class FirstRunActivity extends Activity {
	
	private static FirstRunActivity sFirstRunActivity;
	
	private DesktopPager pager;
	private DesktopPagerIndicator mDesktopPagerIndicator;
	
	public static FirstRunActivity getInstance() {
		return sFirstRunActivity;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_run);
		sFirstRunActivity = this;
		
		pager = (DesktopPager) findViewById(R.id.pager);
		mDesktopPagerIndicator = (DesktopPagerIndicator) findViewById(R.id.bottomBoxIndicator);
		
		pager.setAdapter(new SlidesAdapter(this));
		pager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override public void onPageScrollStateChanged(int state) { }
			@Override public void onPageScrolled(int arg0, float arg1, int arg2) { }
			@Override public void onPageSelected(int arg0) {
				mDesktopPagerIndicator.setSelectedPoint(pager.getCurrentItem());
			}
		});
		pager.setOffscreenPageLimit(4);
		pager.post(new Runnable() {
			@Override
			public void run() {
				mDesktopPagerIndicator.setPoints(pager.getAdapter().getCount());
				mDesktopPagerIndicator.setSelectedPoint(pager.getCurrentItem());
			}
		});
	}
	
	public void handleLogin(View v) {
		startActivity(new Intent(this, AuthLogin.class));
	}
	
	public void handleRegister(View v) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.getRegisterUrl())));
	}
	
	@Override
	protected void onDestroy() {
		sFirstRunActivity = null;
		super.onDestroy();
	}
	
	private class SlidesAdapter extends PagerAdapter {
		
		private LayoutInflater la;
		
		public SlidesAdapter(Context ctx) {
			la = LayoutInflater.from(ctx);
		}

		@Override
		public int getCount() {
			return 4;
		}
		
		@Override
		public Object instantiateItem(View collection, final int position) {
			View view = position == 0 ? la.inflate(R.layout.item_first_run_slide1, (ViewPager) collection, false) : la.inflate(R.layout.item_first_run_slide2, (ViewPager) collection, false);
			
			if (position > 0) {
				TextView slideTitle = (TextView) view.findViewById(R.id.slideTitle);
				TextView slideText = (TextView) view.findViewById(R.id.slideText);
				ImageView slideIcon = (ImageView) view.findViewById(R.id.slideIcon);
				
				switch(position) {
					case 1:
						slideTitle.setText(R.string.label_slogan1);
						slideText.setText(R.string.label_slogan1_desc);
						slideIcon.setImageResource(R.drawable.intro_icon_1);
					break;
					case 2:
						slideTitle.setText(R.string.label_slogan2);
						slideText.setText(R.string.label_slogan2_desc);
						slideIcon.setImageResource(R.drawable.intro_icon_2);
					break;
					case 3:
						slideTitle.setText(R.string.label_slogan3);
						slideText.setText(R.string.label_slogan3_desc);
						slideIcon.setImageResource(R.drawable.intro_icon_3);
					break;
				}
				
			}
			
			((ViewPager) collection).addView(view, 0);
			return view;
		}
		
		@Override
		public void destroyItem(View collection, int position, Object view) {
			((ViewPager) collection).removeView((View) view);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view==((View)object);
		}
		
	}

}
