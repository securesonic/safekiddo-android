package com.ardurasolutions.safekiddo.launcher;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.ardurasolutions.safekiddo.R;
import com.ardurasolutions.safekiddo.helpers.WallpaperHelper;
import com.ardurasolutions.safekiddo.proto.view.CircleProgressBar;
import com.bugsense.trace.BugSenseHandler;
import com.hv.console.Console;

public class WallpaperChooser extends FragmentActivity {
	
	private ViewPager pager;
	private String[] walpapers;
	private ScreenSlidePagerAdapter mPagerAdapter;
	private ExecutorService pool;
	private View progressOverlay;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wallpaper_chooser);
		
		walpapers = WallpaperHelper.getWallpapers(this);
		
		pool = Executors.newFixedThreadPool(1);
		pager = (ViewPager) findViewById(R.id.pager);
		progressOverlay = findViewById(R.id.progressOverlay);
		
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		pager.setAdapter(mPagerAdapter);
		pager.setPageTransformer(true, new DepthPageTransformer());
	}
	
	private class SlideFragment extends Fragment {
		
		private ImageView img;
		private CircleProgressBar mCircleProgressBar;
		private String imgSrc = "";
		private Future<?> runningTaskFuture;
		
		public SlideFragment() {}
		
		@Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.item_wallpaper_chooser, container, false);
			img  = (ImageView) v.findViewById(R.id.imageView1);
			mCircleProgressBar = (CircleProgressBar) v.findViewById(R.id.circleProgressBar1);
			return v;
		}
		
		public SlideFragment setImg(String s) {
			imgSrc = s;
			return this;
		}
		
		@Override
		public void onActivityCreated(@Nullable Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			final int size = getResources().getDisplayMetrics().heightPixels;
			
			runningTaskFuture = pool.submit(new Runnable() {
				@Override
				public void run() {
					int outScale = 0;
					try {
						if (Thread.interrupted()) {
							if (Console.isEnabled()) Console.logi("TERMINATED 0");
							return;
						}
						final Bitmap result;
						
						BitmapFactory.Options o = new BitmapFactory.Options();
						o.inJustDecodeBounds = true;
						BitmapFactory.decodeStream(WallpaperHelper.getWallpaperStream(WallpaperChooser.this, imgSrc), null, o);
						
						if (Thread.interrupted()) {
							if (Console.isEnabled()) Console.logi("TERMINATED 1");
							return;
						}
						
						if (o.outHeight > size) {
							int scale = 1;
							while(o.outHeight / scale / 2 >= size)
								scale *= 2;
							
							outScale = scale;
							
							BitmapFactory.Options o2 = new BitmapFactory.Options();
							o2.inSampleSize = scale;
							o2.inPreferredConfig = Bitmap.Config.ARGB_8888;
							o2.inScaled = false;
							o2.inDither = false;
							o2.inJustDecodeBounds = false;
							
							if (Thread.interrupted()) {
								if (Console.isEnabled()) Console.logi("TERMINATED 2");
								return;
							}
							
							Bitmap r = BitmapFactory.decodeStream(WallpaperHelper.getWallpaperStream(WallpaperChooser.this, imgSrc), null, o2);
							Bitmap tryGetBmp = null;
							
							if (Thread.interrupted()) {
								if (Console.isEnabled()) Console.logi("TERMINATED 3");
								return;
							}
							
							/*
							 * obsłużenie wyjątku braku pamięci
							 * na Galaxy Note II pojawiały sie tutaj błędy braku pamięci
							 * teraz w przypadku błedu będziemy wiedzieli coś więcej i aplikacja się nie zawiesi 
							 */
							try {
								tryGetBmp = Bitmap.createScaledBitmap(r, size, size, true);
								
								if (Thread.interrupted()) {
									if (Console.isEnabled()) Console.logi("TERMINATED 4");
									return;
								}
								
							} catch (OutOfMemoryError e) {
								BugSenseHandler.sendExceptionMessage("WallpaperChooser", "OutOfMemoryError", new Exception(e));
								if (Console.isEnabled())
									Console.loge("WallpaperChooser :: GENERATE BITMAP[OutOfMemoryError]", new Exception(e));
							} catch (Exception e) {
								BugSenseHandler.sendExceptionMessage("WallpaperChooser", "Exception", new Exception(e));
								if (Console.isEnabled())
									Console.loge("WallpaperChooser :: GENERATE BITMAP[Exception]", new Exception(e));
							}
							
							result = tryGetBmp == null ? r : tryGetBmp;
						} else {
							
							if (Thread.interrupted()) {
								if (Console.isEnabled()) Console.logi("TERMINATED 5");
								return;
							}
							
							result = BitmapFactory.decodeStream(WallpaperHelper.getWallpaperStream(WallpaperChooser.this, imgSrc));
						}
						
						
						if (Thread.interrupted()) {
							if (Console.isEnabled()) Console.logi("TERMINATED 6");
							return;
						}
						
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								img.startAnimation(AnimationUtils.loadAnimation(WallpaperChooser.this, R.anim.show_layer));
								img.setScaleType(ScaleType.CENTER_CROP);
								img.setImageBitmap(result);
								mCircleProgressBar.setVisibility(View.GONE);
								mCircleProgressBar.animStop();
								runningTaskFuture = null;
							}
						});
					} catch (IOException e) {
						BugSenseHandler.sendExceptionMessage("WallpaperChooser", "scaleBitmap_scale=" + outScale, e);
						if (Console.isEnabled())
							Console.loge("WallpaperChooser :: onActivityCreated :: ExecutorService.execute()[IO] scale=" + outScale, e);
					} catch (Exception e) {
						BugSenseHandler.sendExceptionMessage("WallpaperChooser", "scaleBitmap_scale=" + outScale, e);
						if (Console.isEnabled())
							Console.loge("WallpaperChooser :: onActivityCreated :: ExecutorService.execute()", e);
					}
				}
			});
		}
		
		public Bitmap getBitmap() {
			/*
			 * może się tak stać że tło nie zostało jeszcze wygenerowane (generowane jest w osobnym wątku w tle)
			 */
			Drawable d = img.getDrawable();
			if (d != null)
				return ((BitmapDrawable) d).getBitmap();
			else
				return null;
		}
		
		/**
		 * ma wywołać anulowanie wątku generującego bitmapę
		 * <i>Thread.interrupted()</i> zwróci true i będzie można nie wyświetlać bitmapy (lub jej gnerować)
		 * co skróci czas oczekiwania pomiędzy szybkiem prewijaniem slajdów
		 */
		public void terminate() {
			if (runningTaskFuture != null)
				runningTaskFuture.cancel(true);
		}
	}
	
	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		
		private SparseArray<SlideFragment> registeredFragments = new SparseArray<SlideFragment>();
		
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public Fragment getItem(int position) {
			SlideFragment mSlideFragment = new SlideFragment();
			mSlideFragment.setImg(walpapers[position]);
			registeredFragments.put(position, mSlideFragment);
			return mSlideFragment;
		}
		
		@Override
		public int getCount() {
			return walpapers.length;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			
			SlideFragment sf = registeredFragments.get(position);
			sf.terminate();
			
			registeredFragments.remove(position);
			super.destroyItem(container, position, object);
		}
		
		public Fragment getRegisteredFragment(int position) {
			return registeredFragments.get(position);
		}
    }
	
	@SuppressLint("NewApi")
	public class DepthPageTransformer implements ViewPager.PageTransformer {
	    private static final float MIN_SCALE = 0.75f;

		public void transformPage(View view, float position) {
	        int pageWidth = view.getWidth();

	        if (position < -1) { // [-Infinity,-1)
	            // This page is way off-screen to the left.
	            view.setAlpha(0);

	        } else if (position <= 0) { // [-1,0]
	            // Use the default slide transition when moving to the left page
	            view.setAlpha(1);
	            view.setTranslationX(0);
	            view.setScaleX(1);
	            view.setScaleY(1);

	        } else if (position <= 1) { // (0,1]
	            // Fade the page out.
	            view.setAlpha(1 - position);

	            // Counteract the default slide transition
	            view.setTranslationX(pageWidth * -position);

	            // Scale the page down (between MIN_SCALE and 1)
	            float scaleFactor = MIN_SCALE
	                    + (1 - MIN_SCALE) * (1 - Math.abs(position));
	            view.setScaleX(scaleFactor);
	            view.setScaleY(scaleFactor);

	        } else { // (1,+Infinity]
	            // This page is way off-screen to the right.
	            view.setAlpha(0);
	        }
	    }
	}
	
	public void handleSetWallpaper(View v) {
		final SlideFragment mSlideFragment = (SlideFragment) mPagerAdapter.getRegisteredFragment(pager.getCurrentItem());
		final Bitmap wpBitmap = mSlideFragment != null ? mSlideFragment.getBitmap(): null;
		
		/*
		 * jeżeli bitmapa nie została jeszcze wygenerowana to nie robimy nic
		 */
		if (wpBitmap != null) {
			final WallpaperManager wpm = WallpaperManager.getInstance(this);
			
			progressOverlay.setVisibility(View.VISIBLE);
			progressOverlay.startAnimation(AnimationUtils.loadAnimation(this, R.anim.show_layer));
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						wpm.setBitmap(wpBitmap);
					} catch (IOException e) {
						if (Console.isEnabled())
							Console.loge("WallpaperManager::setBitmap[IO]", e);
					}
					
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							finish();
						}
					});
				}
			}).start();
		}
	}

}
