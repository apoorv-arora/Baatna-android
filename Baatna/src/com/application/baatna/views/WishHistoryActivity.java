package com.application.baatna.views;

import java.lang.ref.SoftReference;

import com.application.baatna.BaatnaApp;
import com.application.baatna.R;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.TypefaceSpan;
import com.application.baatna.utils.pager.NoSwipeViewPager;
import com.application.baatna.utils.pager.PagerSlidingTabStrip;
import com.application.baatna.utils.pager.ZTabClickCallback;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WishHistoryActivity extends FragmentActivity implements ZTabClickCallback {

	public static final int WISH_OWN = 0;
	public static final int WISH_OFFERED = 1;

	private BaatnaApp zapp;
	private SharedPreferences prefs;
	private int width;
	private NoSwipeViewPager homePager;
	private SparseArray<SoftReference<Fragment>> fragments = new SparseArray<SoftReference<Fragment>>();

	private boolean destroyed = false;

	int currentPageSelected = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wish_history);

		prefs = getSharedPreferences("application_settings", 0);
		zapp = (BaatnaApp) getApplication();
		width = getWindowManager().getDefaultDisplay().getWidth();

		homePager = (NoSwipeViewPager) findViewById(R.id.home_pager);
		homePager.setAdapter(new HomePagerAdapter(getSupportFragmentManager()));
		homePager.setOffscreenPageLimit(1);
		homePager.setCurrentItem(WISH_OWN);
		homePager.setSwipeable(true);
		setupActionBar();
		setUpTabs();
	}

	private void setUpTabs() {
		// tabs
		PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		tabs.setmZomatoHome(true);
		tabs.setAllCaps(false);
		tabs.setForegroundGravity(Gravity.LEFT);
		tabs.setShouldExpand(true);
		tabs.setViewPager(homePager);
		tabs.setDividerColor(getResources().getColor(R.color.zhl_dark));
		tabs.setBackgroundColor(getResources().getColor(R.color.white));
		tabs.setUnderlineColor(getResources().getColor(R.color.zhl_dark));
		tabs.setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Bold), 0);
		tabs.setIndicatorColor(getResources().getColor(R.color.zhl_darker));
		tabs.setIndicatorHeight((int) getResources().getDimension(R.dimen.height3));
		tabs.setTextSize((int) getResources().getDimension(R.dimen.size16));
		tabs.setUnderlineHeight(0);
		tabs.setTabPaddingLeftRight(12);
		tabs.setInterfaceForClick(this);

		final int tabsUnselectedColor = R.color.zhl_darker;
		final int tabsSelectedColor = R.color.zomato_red;

//		final TextView homeSearchHeader = (TextView) ((LinearLayout) tabs.getChildAt(0)).getChildAt(WISH_OWN);
		final TextView homeFeedHeader = (TextView) ((LinearLayout) tabs.getChildAt(0)).getChildAt(WISH_OFFERED);

//		homeSearchHeader.setTextColor(getResources().getColor(tabsSelectedColor));
		homeFeedHeader.setTextColor(getResources().getColor(tabsUnselectedColor));
		setPageChangeListenerOnTabs(tabs, tabsUnselectedColor, tabsSelectedColor, homeFeedHeader);
	}

	private void setupActionBar() {
		ActionBar actionBar = getActionBar();

		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);

		LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View actionBarCustomView = inflator.inflate(R.layout.white_action_bar, null);
		actionBarCustomView.findViewById(R.id.home_icon_container).setVisibility(View.VISIBLE);
		actionBar.setCustomView(actionBarCustomView);

		SpannableString s = new SpannableString(getString(R.string.your_history));
		s.setSpan(
				new TypefaceSpan(getApplicationContext(), CommonLib.BOLD_FONT_FILENAME,
						getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
				0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		TextView title = (TextView) actionBarCustomView.findViewById(R.id.title);

		((RelativeLayout.LayoutParams) actionBarCustomView.findViewById(R.id.back_icon).getLayoutParams())
				.setMargins(width / 40, 0, 0, 0);
		actionBarCustomView.findViewById(R.id.title).setPadding(width / 20, 0, width / 40, 0);
		title.setText(s);
	}

	public void actionBarSelected(View v) {
		switch (v.getId()) {
		case R.id.home_icon_container:
			onBackPressed();
			break;
		default:
			break;
		}
	}

	private class HomePagerAdapter extends FragmentStatePagerAdapter {

		public HomePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			fragments.put(position, null);
			super.destroyItem(container, position, object);
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {

			case WISH_OWN:
				CommonLib.ZLog("HomePage", "Creating new home page fragment");
				WishFragment home = new WishFragment();
				fragments.put(WISH_OWN, new SoftReference<Fragment>(home));
				return home;

			case WISH_OFFERED:
				WishOfferedFragment details = new WishOfferedFragment();
				fragments.put(WISH_OFFERED, new SoftReference<Fragment>(details));
				return details;

			}
			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}

		private String[] ids = { getResources().getString(R.string.your_wishes),
				getResources().getString(R.string.your_offers) };

		public String getPageTitle(int pos) {
			return ids[pos];
		}
	}

	@Override
	public void onTabClick(int position) {
		if (currentPageSelected == position) {

			try {
				switch (position) {
				case WISH_OWN:

					// Search Scroll Top
					if (fragments.get(WISH_OWN) != null) {
						WishFragment srf = (WishFragment) fragments.get(WISH_OWN).get();
						if (srf != null) {
							srf.scrollToTop();
						}

					} else {
						HomePagerAdapter hAdapter = (HomePagerAdapter) homePager.getAdapter();
						if (hAdapter != null) {
							try {
								WishFragment fragMent = (WishFragment) hAdapter.instantiateItem(homePager, WISH_OWN);
								if (fragMent != null)
									fragMent.scrollToTop();
							} catch (Exception e) {
							}
						}
					}

					break;
				case WISH_OFFERED:

					// Home Scroll Top
					if (fragments.get(WISH_OFFERED) != null) {
						WishOfferedFragment hf = (WishOfferedFragment) fragments.get(WISH_OFFERED).get();
						if (hf != null) {
							hf.scrollToTop();
						}
					} else {
						HomePagerAdapter hAdapter = (HomePagerAdapter) homePager.getAdapter();
						if (hAdapter != null) {
							try {
								WishOfferedFragment fragMent = (WishOfferedFragment) hAdapter.instantiateItem(homePager,
										WISH_OFFERED);
								if (fragMent != null)
									fragMent.scrollToTop();
							} catch (Exception e) {
							}
						}
					}
					break;

				}
			} catch (Exception e) {

			}

		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case android.R.id.home:
			onBackPressed();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void setPageChangeListenerOnTabs(PagerSlidingTabStrip tabs, final int tabsUnselectedColor,
			final int tabsSelectedColor, final TextView homeFeedHeader) {
		tabs.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {

				currentPageSelected = arg0;

				// FeedFragment - Wish offered
				if (fragments.get(WISH_OFFERED) != null) {
					WishOfferedFragment upf = (WishOfferedFragment) fragments.get(WISH_OFFERED).get();
					if (upf != null) {
						// upf.stopRefreshing();
					}

				} else {
					HomePagerAdapter hAdapter = (HomePagerAdapter) homePager.getAdapter();
					if (hAdapter != null) {
						try {
							WishOfferedFragment fragMent = (WishOfferedFragment) hAdapter.instantiateItem(homePager,
									WISH_OFFERED);
							if (fragMent != null) {
								// fragMent.stopRefreshing();
							}
						} catch (Exception e) {
						}
					}
				}

				// SearchFragment - Wish Fragment
				if (arg0 == WISH_OWN) {

					if (fragments.get(WISH_OWN) != null) {

						if (fragments.get(WISH_OWN).get() instanceof WishFragment) {
							WishFragment srf = (WishFragment) fragments.get(WISH_OWN).get();
							if (srf != null) {

								// if (!srf.searchCallsInitiatedFromHome)
								// srf.initiateSearchCallFromHome();

							}
						}

					} else {
						HomePagerAdapter hAdapter = (HomePagerAdapter) homePager.getAdapter();
						if (hAdapter != null) {
							try {
								WishFragment fragMent = (WishFragment) hAdapter.instantiateItem(homePager, WISH_OWN);
								if (fragMent != null) {

									// if
									// (!fragMent.searchCallsInitiatedFromHome)
									// fragMent.initiateSearchCallFromHome();

								}
							} catch (Exception e) {
								// Crashlytics.logException(e);
							}
						}
					}

					// homeSearchHeader.setTextColor(getResources().getColor(tabsUnselectedColor));
					homeFeedHeader.setTextColor(getResources().getColor(tabsUnselectedColor));

				} else if (arg0 == WISH_OFFERED) {

					// homeSearchHeader.setTextColor(getResources().getColor(tabsUnselectedColor));
					homeFeedHeader.setTextColor(getResources().getColor(tabsSelectedColor));

					if (fragments != null && fragments.get(WISH_OFFERED) != null) {

						if (fragments.get(WISH_OFFERED).get() instanceof WishOfferedFragment) {
							WishOfferedFragment ff = (WishOfferedFragment) fragments.get(WISH_OFFERED).get();
							if (ff != null) {
								try {
									// if (!ff.feedDisplayed) {
									// ff.feedDisplayed = true;
									// ff.displayFeed();
									// }
								} catch (Exception e) {
									// Crashlytics.logException(e);
								}
							}
						}

					} else {
						HomePagerAdapter hAdapter = (HomePagerAdapter) homePager.getAdapter();
						if (hAdapter != null) {
							try {
								WishOfferedFragment fragMent = (WishOfferedFragment) hAdapter.instantiateItem(homePager,
										WISH_OFFERED);
								if (fragMent != null) {
									// if (!fragMent.feedDisplayed) {
									// fragMent.feedDisplayed = true;
									// fragMent.displayFeed();
									// }
								}
							} catch (Exception e) {
								// Crashlytics.logException(e);
							}
						}
					}

					try {
						((DrawerLayout) findViewById(R.id.drawer_layout))
								.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
					} catch (Exception e) {
					}

				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

				if (position == 0) {

					int alphaValueUnderline = (int) ((((positionOffset - 0) * (255 - 0)) / (1 - 0)) + 0);
					((PagerSlidingTabStrip) findViewById(R.id.tabs))
							.setUnderlineColor(Color.argb(alphaValueUnderline, 228, 228, 228));

				} else if (position > 0) {
					((PagerSlidingTabStrip) findViewById(R.id.tabs)).setUnderlineColor(Color.argb(255, 228, 228, 228));
				}
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

}
