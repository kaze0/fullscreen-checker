package com.mikedg.android.fullscreentest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class FullscreenChecker {
	private View mViewFullScreenChangeWatcher;
	private View mViewFullScreenSizeChecker;
	Context mContext;
	WindowManager mManager;
	private boolean mFirstGoRoundHack = false; //Used to help handle situation where it's restarted whlie in a fullscreen state

	private FullScreenChangeListener mFullscreenChangeListener;
	private boolean usePreSystemUITrack;

	private static final int LAST_PRE_SYSTEM_UI_BUILD = Build.VERSION_CODES.GINGERBREAD_MR1;
	private static final boolean DEBUG = false;

	public FullscreenChecker(Context context, WindowManager manager) {
		usePreSystemUITrack = (Build.VERSION.SDK_INT <= LAST_PRE_SYSTEM_UI_BUILD);

		this.mManager = manager;
		this.mContext = context;

		mViewFullScreenSizeChecker = new View(mContext) {
			private int mLastScreenHeight;
			private int viewLastHeight;

			@Override
			public void onFinishTemporaryDetach() {
				// TODO Auto-generated method stub
				super.onFinishTemporaryDetach();
			}

			@Override
			protected void onDisplayHint(int hint) {
				// TODO Auto-generated method stub
				super.onDisplayHint(hint);
			}

			@Override
			protected void onSizeChanged(int w, int h, int oldw, int oldh) {
				// TODO Auto-generated method stub
				super.onSizeChanged(w, h, oldw, oldh);
			}

			@Override
			protected void onDraw(Canvas canvas) {
				// TODO Auto-generated method stub
				super.onDraw(canvas);
			}

			@Override
			protected void onAttachedToWindow() {
				// TODO Auto-generated method stub
				super.onAttachedToWindow();
			}

			@Override
			protected void onDetachedFromWindow() {
				// TODO Auto-generated method stub
				super.onDetachedFromWindow();
			}

			@Override
			protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
				Log.d("loc", "Size Checker: onLayout: bottom - top: " + (bottom - top)); // Appears
																							// to
																							// give
																							// correct
																							// size

				DisplayMetrics m = new DisplayMetrics();
				((WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(m);

				Log.d("loc", "Size Checker: displayHeight: " + m.heightPixels);

				if (usePreSystemUITrack) {
					/*
					 * We do this check so we can bypass some of the extra stuff
					 * required to handle the bottom bar... I suppose we can do
					 * this for a device that doesn't show the bottom bar? But
					 * for now we just use the API check since devices with
					 * older OSs are likely to be a hell of a lot slow
					 * 
					 * just compare heights to screen height
					 */
					// check last screen type too?
					// track last fullscreen or mauybe just orientation change?
//					if (mLastScreenHeight == m.heightPixels) { //Actually don't think this matters does it? or is it called twice?
					if (viewLastHeight != (bottom - top)) {
						if (mFullscreenChangeListener != null) {
							if (m.heightPixels == (bottom - top)) {
								/* screenheight = layoutheight */
								mFullscreenChangeListener.onFullscreen(); // Is
																				// full
																				// screen
							} else {
								mFullscreenChangeListener.onNotFullscreen(); // is
																					// not
																					// full
																					// screen
							}
						}
					} else {
						mLastScreenHeight = m.heightPixels;
					}
				} else {
					// This is honeycomb up, because of the SYSTEM_UI_BAR other
					// way is likely more efficient and generally older devies
					int thisHeight = bottom - top;

					if ((viewLastHeight != thisHeight || mFirstGoRoundHack ) && mViewFullScreenChangeWatcher != null) { // make
																								// sure
																								// the
																								// height
						// actually changed
						mFirstGoRoundHack = false;
						int otherHeight = mViewFullScreenChangeWatcher.getHeight();
						if (thisHeight == otherHeight) {
							mFullscreenChangeListener.onFullscreen(); // Is
																			// full
																			// screen
						} else if (otherHeight == 0) {
							//This case is to handle situations where the other layout has not been laid out yet
							/* 
							 * 1/28/12 - Dual view mode - We had an issue reported where if the service was restarted while
							 * you were in a fullscreen app, it did not trigger fullscreen mode. However, single view mode 
							 * for older devices worked fine
							 * http://code.google.com/p/birdbar/issues/detail?id=1
							 */
							mFirstGoRoundHack = true; 
							mFullscreenChangeListener.onNotFullscreen(); //Now disable it, and the hack will check again next time
							//This was due to a reported bug where we were not disabling on start from within the app
						} else {
							mFullscreenChangeListener.onNotFullscreen(); // Is
																				// not
																				// full
																				// screen
						}
						viewLastHeight = thisHeight;
					}
				}
				super.onLayout(changed, left, top, right, bottom);

				Log.d("loc", "Size Checker: onLayout: thisHeight: " + mViewFullScreenSizeChecker.getHeight());
				// Need to check orientation? can we just check
				// fullscreeenedness? or should we just compare heights?
				// Don't think we can compare fullscreenedness
				// compare heights but keep in mind that damn bottom system bar
				// :(
				// can i do anything with comparing this height vs the other
				// height?
				// Need to test if the other thing fills the screen to undeeerr
				// the system ui bar... or just up until
				// we either check that both of these views are the same size or
				// that the sizing view == the screen width/height
				// Won't work too great with the GNEX will it?cause of the
				// status bar :(
			}

			@Override
			protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
				Log.d("loc", "Size Checker: onMeasure: heightMeasureSpec: " + heightMeasureSpec);
				super.onMeasure(widthMeasureSpec, heightMeasureSpec);
				Log.d("loc", "onMeasure other size" + mViewFullScreenSizeChecker.getHeight());
			}

		};
		if (!usePreSystemUITrack) {
			mViewFullScreenChangeWatcher = new View(mContext) {
				// @Override
				// protected void onMeasure(int widthMeasureSpec, int
				// heightMeasureSpec) {
				// Log.d("loc", "height spec: " + heightMeasureSpec);
				// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
				// Log.d("loc", "onMeasure other size" +
				// mViewFullScreenSizeChecker.getHeight());
				// }

				@Override
				protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//					Log.d("loc", "Change Watcher: onLayout: bottom - top: " + (bottom - top)); // Appears
																								// to
																								// give
																								// correct
																								// size

					super.onLayout(changed, left, top, right, bottom);
//					DisplayMetrics m = new DisplayMetrics();
//					((WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(m);

					
//					Log.d("loc", "Size Checker: displayHeight: " + m.heightPixels);
//
//					Log.d("loc", "Change Watcher: onLayout: other size" + mViewFullScreenSizeChecker.getHeight());
					
					
					
					// Need to check orientation? can we just check
					// fullscreeenedness? or should we just compare heights?
					// Don't think we can compare fullscreenedness
					// compare heights but keep in mind that damn bottom system
					// bar :(
					// can i do anything with comparing this height vs the other
					// height?
					// Need to test if the other thing fills the screen to
					// undeeerr the system ui bar... or just up until
					// we either check that both of these views are the same
					// size or that the sizing view == the screen width/height
					// Won't work too great with the GNEX will it?cause of the
					// status bar :(
					if (mFirstGoRoundHack) {
						mFirstGoRoundHack = false;
					
						//TODO: refactor this so it's not duplicated from above onLayout
						int thisHeight = bottom - top;
						int otherHeight = mViewFullScreenSizeChecker.getHeight();
						
						if (thisHeight == otherHeight) {
							mFullscreenChangeListener.onFullscreen(); // Is
																			// full
																			// screen
						} else if (otherHeight == 0) {
							//This case is to handle situations where the other layout has not been laid out yet
							/* 
							 * 1/28/12 - Dual view mode - We had an issue reported where if the service was restarted while
							 * you were in a fullscreen app, it did not trigger fullscreen mode. However, single view mode 
							 * for older devices worked fine
							 * http://code.google.com/p/birdbar/issues/detail?id=1
							 */
							mFirstGoRoundHack = true; 
							mFullscreenChangeListener.onNotFullscreen(); //Now disable it, and the hack will check again next time
							//This was due to a reported bug where we were not disabling on start from within the app
						} else {
							mFullscreenChangeListener.onNotFullscreen(); // Is
																				// not
																				// full
																				// screen
						}
					}
				}
			};
//			mViewFullScreenChangeWatcher.setVisibility(View.INVISIBLE); //Probably helps rendering a slight bit?, but I think this actually stops the layouts from happening :(
		}
		
//		mViewFullScreenSizeChecker.setVisibility(View.INVISIBLE); //Probably helps rendering a slight bit?, but I think this actually stops the layouts from happening :(
		if(DEBUG) {
			mViewFullScreenSizeChecker.setBackgroundColor(0x99ff0000); //FIXME: put this to see how tings resize when switching types
		}
	}

	public void addView() {
		mManager.addView(mViewFullScreenSizeChecker, mViewFullScreenSizeCheckerParams);
		if (!usePreSystemUITrack) {
			mManager.addView(mViewFullScreenChangeWatcher, mViewFullScreenChangeWatcherParams);
		}
	}

	public void removeView() {
		//FIXME: investigate to see if this throws an exception if the views are not added
//		Need to check that these are attached...FullScreenChangeListener or not allow removal when not attache
		if (!usePreSystemUITrack) {
			mManager.removeView(mViewFullScreenChangeWatcher);
		}
		mManager.removeView(mViewFullScreenSizeChecker);
	}

	WindowManager.LayoutParams mViewFullScreenChangeWatcherParams = new WindowManager.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
			WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
			// onMeasure
			// will
			// get
			// called
			// on
			// fullscreen
			// change
			// if
			// this
			// is
			// set
			// :/
			// maybe
			// link
			// 2
			// views
			// for
			// this?
			// one
			// to
			// have
			// location
			// checked...
			// other
			// for
			// notifier?
			PixelFormat.TRANSLUCENT);

	WindowManager.LayoutParams mViewFullScreenSizeCheckerParams = new WindowManager.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 
			WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
			WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | 
			WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | // ,
					// |
					WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR, // onmeasure
																		// does
																		// not
																		// get
																		// called
																		// on
																		// screen
																		// change
			PixelFormat.TRANSLUCENT);

	public void setFullScreenChangeListener(FullScreenChangeListener listener) {
		mFullscreenChangeListener = listener;
	}

	public interface FullScreenChangeListener {
		public void onFullscreen();
		public void onNotFullscreen();
	}
}
