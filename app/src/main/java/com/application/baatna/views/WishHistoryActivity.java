package com.application.baatna.views;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.application.baatna.BaatnaApp;
import com.application.baatna.R;
import com.application.baatna.utils.CommonLib;
import com.application.baatna.utils.pager.NoSwipeViewPager;
import com.application.baatna.utils.pager.PagerSlidingTabStrip;
import com.application.baatna.utils.pager.ZTabClickCallback;

import java.lang.ref.SoftReference;

public class WishHistoryActivity extends ActionBarActivity implements ZTabClickCallback {

	private BaatnaApp zapp;
	private SharedPreferences prefs;
	private int width;

	private NoSwipeViewPager homePager;
	private SparseArray<SoftReference<Fragment>> fragments = new SparseArray<SoftReference<Fragment>>();

	public static DrawerLayout mDrawerLayout;
	LayoutInflater inflater;

	private boolean destroyed = false;

	int currentPageSelected = 0;
	public boolean fromExternalSource = false;

	// Viewpager Fragment Index
	private static final int VIEWPAGER_INDEX_DEAL1_FRAGMENT = 0;
	private static final int VIEWPAGER_INDEX_DEAL2_FRAGMENT = 1;

	public ActionBarDrawerToggle mActionBarDrawerToggle;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		inflater = LayoutInflater.from(this);
		prefs = getSharedPreferences("application_settings", 0);
		zapp = (BaatnaApp) getApplication();
		width = getWindowManager().getDefaultDisplay().getWidth();

		setContentView(R.layout.wish_history);
		// Home tabs
		homePager = (NoSwipeViewPager) findViewById(R.id.home_pager);
		homePager.setAdapter(new HomePagerAdapter(getSupportFragmentManager()));
		homePager.setOffscreenPageLimit(4);
		homePager.setCurrentItem(VIEWPAGER_INDEX_DEAL1_FRAGMENT);
		homePager.setSwipeable(true);

		setUpActionBar();

		// hide the thin line below tabs on androidL
		if (CommonLib.isAndroidL())
			findViewById(R.id.tab_thin_line).setVisibility(View.GONE);

		setUpTabs();
	}

	private void setUpActionBar() {

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(false);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayUseLogoEnabled(true);

		final boolean isAndroidL = Build.VERSION.SDK_INT >= 21; // Build.AndroidL
		if (!isAndroidL)
			actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.baatna_dark_feedback));

		actionBar.setTitle("");
	}

	private void setUpTabs() {
		// tabs
		PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		tabs.setmZomatoHome(false);
		tabs.setAllCaps(false);
		tabs.setForegroundGravity(Gravity.LEFT);
		tabs.setShouldExpand(true);
		tabs.setViewPager(homePager);
		tabs.setDividerColor(getResources().getColor(R.color.transparent1));
		tabs.setBackgroundColor(getResources().getColor(R.color.white));
		tabs.setUnderlineColor(getResources().getColor(R.color.zhl_dark));
		tabs.setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Bold), 0);
		tabs.setIndicatorColor(getResources().getColor(R.color.white));
		tabs.setIndicatorHeight((int) getResources().getDimension(R.dimen.height3));
		tabs.setTextSize((int) getResources().getDimension(R.dimen.size15));
		tabs.setUnderlineHeight(0);
		tabs.setTabPaddingLeftRight(12);
		tabs.setInterfaceForClick(this);

		final int tabsUnselectedColor = R.color.zdhl3;
		final int tabsSelectedColor = R.color.baatna_primary_dark;

		final TextView deal1Header = (TextView) ((LinearLayout) tabs.getChildAt(0))
				.getChildAt(VIEWPAGER_INDEX_DEAL1_FRAGMENT);
		final TextView deal2Header = (TextView) ((LinearLayout) tabs.getChildAt(0))
				.getChildAt(VIEWPAGER_INDEX_DEAL2_FRAGMENT);

		deal1Header.setTextColor(getResources().getColor(tabsSelectedColor));
		deal2Header.setTextColor(getResources().getColor(tabsUnselectedColor));

		setPageChangeListenerOnTabs(tabs, tabsUnselectedColor, tabsSelectedColor, deal1Header, deal2Header);
	}

	@Override
	public void onTabClick(int position) {
		if (currentPageSelected == position) {

			try {
				switch (position) {

					case VIEWPAGER_INDEX_DEAL1_FRAGMENT:

						// Home Scroll Top
						if (fragments.get(VIEWPAGER_INDEX_DEAL1_FRAGMENT) != null) {
							WishFragment hf = (WishFragment) fragments.get(VIEWPAGER_INDEX_DEAL1_FRAGMENT).get();
							if (hf != null) {
								hf.scrollToTop();
							}
						} else {
							HomePagerAdapter hAdapter = (HomePagerAdapter) homePager.getAdapter();
							if (hAdapter != null) {
								try {
									WishFragment fragMent = (WishFragment) hAdapter.instantiateItem(homePager,
											VIEWPAGER_INDEX_DEAL1_FRAGMENT);
									if (fragMent != null)
										fragMent.scrollToTop();
								} catch (Exception e) {
								}
							}
						}
						break;

					case VIEWPAGER_INDEX_DEAL2_FRAGMENT:

						// Search Scroll Top
						if (fragments.get(VIEWPAGER_INDEX_DEAL2_FRAGMENT) != null) {
							WishOfferedFragment srf = (WishOfferedFragment) fragments.get(VIEWPAGER_INDEX_DEAL2_FRAGMENT).get();
							if (srf != null) {
								srf.scrollToTop();
							}

						} else {
							HomePagerAdapter hAdapter = (HomePagerAdapter) homePager.getAdapter();
							if (hAdapter != null) {
								try {
									WishOfferedFragment fragMent = (WishOfferedFragment) hAdapter.instantiateItem(homePager,
											VIEWPAGER_INDEX_DEAL2_FRAGMENT);
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

	private void setPageChangeListenerOnTabs(PagerSlidingTabStrip tabs, final int tabsUnselectedColor,
											 final int tabsSelectedColor, final TextView deal1Header, final TextView deal2Header) {
		tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {

				currentPageSelected = arg0;

				// SearchFragment
				if (arg0 == VIEWPAGER_INDEX_DEAL2_FRAGMENT) {

					if (fragments.get(VIEWPAGER_INDEX_DEAL2_FRAGMENT) != null) {

						if (fragments.get(VIEWPAGER_INDEX_DEAL2_FRAGMENT).get() instanceof WishOfferedFragment) {
							WishOfferedFragment srf = (WishOfferedFragment) fragments.get(VIEWPAGER_INDEX_DEAL2_FRAGMENT).get();
							if (srf != null) {

								// if (!srf.searchCallsInitiatedFromHome)
								// srf.initiateSearchCallFromHome();

							}
						}

					} else {
						HomePagerAdapter hAdapter = (HomePagerAdapter) homePager.getAdapter();
						if (hAdapter != null) {
							try {
								WishOfferedFragment fragMent = (WishOfferedFragment) hAdapter.instantiateItem(homePager,
										VIEWPAGER_INDEX_DEAL2_FRAGMENT);
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

					deal1Header.setTextColor(getResources().getColor(tabsUnselectedColor));
					deal2Header.setTextColor(getResources().getColor(tabsSelectedColor));

				} else if (arg0 == VIEWPAGER_INDEX_DEAL1_FRAGMENT) {

					deal2Header.setTextColor(getResources().getColor(tabsUnselectedColor));
					deal1Header.setTextColor(getResources().getColor(tabsSelectedColor));
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

				case VIEWPAGER_INDEX_DEAL1_FRAGMENT: {
					CommonLib.ZLog("HomePage", "Creating new home page fragment");
					WishFragment home = new WishFragment();
					fragments.put(VIEWPAGER_INDEX_DEAL1_FRAGMENT, new SoftReference<Fragment>(home));
					return home;
				}

				case VIEWPAGER_INDEX_DEAL2_FRAGMENT: {
					WishOfferedFragment details = new WishOfferedFragment();
					fragments.put(VIEWPAGER_INDEX_DEAL2_FRAGMENT, new SoftReference<Fragment>(details));
					return details;
				}

			}
			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}

		private String[] ids = { getResources().getString(R.string.your_wishes), getResources().getString(R.string.your_offers)};

		public String getPageTitle(int pos) {
			return ids[pos];
		}
	}

}