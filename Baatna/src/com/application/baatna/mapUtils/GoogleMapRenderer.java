package com.application.baatna.mapUtils;

import static com.application.baatna.mapUtils.NonHierarchicalDistanceBasedAlgorithm.MAX_DISTANCE_AT_ZOOM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.view.animation.DecelerateInterpolator;

import com.application.baatna.utils.CommonLib;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapRenderer implements ClusterRenderer<SimpleRestaurantPin> {

	private static final boolean SHOULD_ANIMATE = true;
	private final GoogleMap mMap;
	private final ClusterManager mClusterManager;
	private final String TAG = "GoogleMapRenderer";  

	private Set<MarkerWithPosition> mMarkers = Collections.newSetFromMap(
			new ConcurrentHashMap<MarkerWithPosition, Boolean>());

	private MarkerCache<SimpleRestaurantPin> mMarkerCache = new MarkerCache<SimpleRestaurantPin>();

	private static final int MIN_CLUSTER_SIZE = 2;

	private Set<? extends Cluster<SimpleRestaurantPin>> mClusters;
	private Map<Marker, Cluster<SimpleRestaurantPin>> mMarkerToCluster = new HashMap<Marker, Cluster<SimpleRestaurantPin>>();
	private Map<Cluster<SimpleRestaurantPin>, Marker> mClusterToMarker = new HashMap<Cluster<SimpleRestaurantPin>, Marker>();

	private float mZoom;

	private final ViewModifier mViewModifier = new ViewModifier();

	private ClusterManager.OnClusterClickListener<SimpleRestaurantPin> mClickListener;
	private ClusterManager.OnClusterInfoWindowClickListener<SimpleRestaurantPin> mInfoWindowClickListener;
	private ClusterManager.OnClusterItemClickListener<SimpleRestaurantPin> mItemClickListener;
	private ClusterManager.OnClusterItemInfoWindowClickListener<SimpleRestaurantPin> mItemInfoWindowClickListener;

	public GoogleMapRenderer(GoogleMap map, ClusterManager clusterManager) {
		mMap = map;
		mClusterManager = clusterManager;
	}

	@Override
	public void onAdd() {
		mClusterManager.getMarkerCollection().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				CommonLib.ZLog(TAG, "onMarkerClick");
				return mItemClickListener != null && mItemClickListener.onClusterItemClick(mMarkerCache.get(marker));
			}
		});

		mClusterManager.getMarkerCollection().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				if (mItemInfoWindowClickListener != null) {
					mItemInfoWindowClickListener.onClusterItemInfoWindowClick(mMarkerCache.get(marker));
				}
			}
		});

		mClusterManager.getClusterMarkerCollection().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				CommonLib.ZLog(TAG, "onCLusterMarkerClick");
				return mClickListener != null && mClickListener.onClusterClick(mMarkerToCluster.get(marker));
			}
		});

		mClusterManager.getClusterMarkerCollection().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				if (mInfoWindowClickListener != null) {
					mInfoWindowClickListener.onClusterInfoWindowClick(mMarkerToCluster.get(marker));
				}
			}
		});
	}

	@Override
	public void onRemove() {
		mClusterManager.getMarkerCollection().setOnMarkerClickListener(null);
		mClusterManager.getClusterMarkerCollection().setOnMarkerClickListener(null);
	}

	/**
	 * ViewModifier ensures only one re-rendering of the view occurs at a time, and schedules
	 * re-rendering, which is performed by the RenderTask.
	 */
	@SuppressLint("HandlerLeak")
	private class ViewModifier extends Handler {
		private static final int RUN_TASK = 0;
		private static final int TASK_FINISHED = 1;
		private boolean mViewModificationInProgress = false;
		private RenderTask mNextClusters = null;

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == TASK_FINISHED) {
				mViewModificationInProgress = false;
				if (mNextClusters != null) {
					// Run the task that was queued up.
					sendEmptyMessage(RUN_TASK);
				}
				return;
			}
			removeMessages(RUN_TASK);

			if (mViewModificationInProgress) {
				// Busy - wait for the callback.
				return;
			}

			if (mNextClusters == null) {
				// Nothing to do.
				return;
			}

			RenderTask renderTask;
			synchronized (this) {
				renderTask = mNextClusters;
				mNextClusters = null;
				mViewModificationInProgress = true;
			}

			renderTask.setCallback(new Runnable() {
				@Override
				public void run() {
					sendEmptyMessage(TASK_FINISHED);
				}
			});
			renderTask.setProjection(mMap.getProjection());
			renderTask.setMapZoom(mMap.getCameraPosition().zoom);
			new Thread(renderTask).start();
		}

		public void queue(Set<? extends Cluster<SimpleRestaurantPin>> clusters) {
			synchronized (this) {
				// Overwrite any pending cluster tasks - we don't care about intermediate states.
				mNextClusters = new RenderTask(clusters);
			}
			sendEmptyMessage(RUN_TASK);
		}
	}

	protected boolean shouldRenderAsCluster(Cluster<SimpleRestaurantPin> cluster) {
		return cluster.getSize() >= MIN_CLUSTER_SIZE;
	}
	
	
	
	private class RenderTask implements Runnable {
        final Set<? extends Cluster<SimpleRestaurantPin>> clusters;
        private Runnable mCallback;
        private Projection mProjection;
        private SphericalMercatorProjection mSphericalMercatorProjection;
        private float mMapZoom;

        private RenderTask(Set<? extends Cluster<SimpleRestaurantPin>> clusters) {
            this.clusters = clusters;
        }

        /**
         * A callback to be run when all work has been completed.
         *
         * @param callback
         */
        public void setCallback(Runnable callback) {
            mCallback = callback;
        }

        public void setProjection(Projection projection) {
            this.mProjection = projection;
        }

        public void setMapZoom(float zoom) {
            this.mMapZoom = zoom;
            this.mSphericalMercatorProjection = new SphericalMercatorProjection(256 * Math.pow(2, Math.min(zoom, mZoom)));
        }

        public void run() {
            if (clusters.equals(GoogleMapRenderer.this.mClusters)) {
                mCallback.run();
                return;
            }

            final MarkerModifier markerModifier = new MarkerModifier();

            final float zoom = mMapZoom;
            final boolean zoomingIn = zoom > mZoom;
            final float zoomDelta = zoom - mZoom;

            final Set<MarkerWithPosition> markersToRemove = mMarkers;
            final LatLngBounds visibleBounds = mProjection.getVisibleRegion().latLngBounds;
            // TODO: Add some padding, so that markers can animate in from off-screen.

            // Find all of the existing clusters that are on-screen. These are candidates for
            // markers to animate from.
            List<Point> existingClustersOnScreen = null;
            if (GoogleMapRenderer.this.mClusters != null && SHOULD_ANIMATE) {
                existingClustersOnScreen = new ArrayList<Point>();
                for (Cluster<SimpleRestaurantPin> c : GoogleMapRenderer.this.mClusters) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.getPosition())) {
                        Point point = mSphericalMercatorProjection.toPoint(c.getPosition());
                        existingClustersOnScreen.add(point);
                    }
                }
            }

            // Create the new markers and animate them to their new positions.
            final Set<MarkerWithPosition> newMarkers = Collections.newSetFromMap(
                    new ConcurrentHashMap<MarkerWithPosition, Boolean>());
            for (Cluster<SimpleRestaurantPin> c : clusters) {
                boolean onScreen = visibleBounds.contains(c.getPosition());
                if (zoomingIn && onScreen && SHOULD_ANIMATE) {
                    Point point = mSphericalMercatorProjection.toPoint(c.getPosition());
                    Point closest = findClosestCluster(existingClustersOnScreen, point);
                    if (closest != null) {
                        LatLng animateTo = mSphericalMercatorProjection.toLatLng(closest);
                        markerModifier.add(true, new CreateMarkerTask(c, newMarkers, animateTo));
                    } else {
                        markerModifier.add(true, new CreateMarkerTask(c, newMarkers, null));
                    }
                } else {
                    markerModifier.add(onScreen, new CreateMarkerTask(c, newMarkers, null));
                }
            }

            // Wait for all markers to be added.
            markerModifier.waitUntilFree();

            // Don't remove any markers that were just added. This is basically anything that had
            // a hit in the MarkerCache.
            markersToRemove.removeAll(newMarkers);

            // Find all of the new clusters that were added on-screen. These are candidates for
            // markers to animate from.
            List<Point> newClustersOnScreen = null;
            if (SHOULD_ANIMATE) {
                newClustersOnScreen = new ArrayList<Point>();
                for (Cluster<SimpleRestaurantPin> c : clusters) {
                    if (shouldRenderAsCluster(c) && visibleBounds.contains(c.getPosition())) {
                        Point p = mSphericalMercatorProjection.toPoint(c.getPosition());
                        newClustersOnScreen.add(p);
                    }
                }
            }

            // Remove the old markers, animating them into clusters if zooming out.
            for (final MarkerWithPosition marker : markersToRemove) {
                boolean onScreen = visibleBounds.contains(marker.position);
                // Don't animate when zooming out more than 3 zoom levels.
                // TODO: drop animation based on speed of device & number of markers to animate.
                if (!zoomingIn && zoomDelta > -3 && onScreen && SHOULD_ANIMATE) {
                    final Point point = mSphericalMercatorProjection.toPoint(marker.position);
                    final Point closest = findClosestCluster(newClustersOnScreen, point);
                    if (closest != null) {
                        LatLng animateTo = mSphericalMercatorProjection.toLatLng(closest);
                        markerModifier.animateThenRemove(marker, marker.position, animateTo);
                    } else {
                        markerModifier.remove(true, marker.marker);
                    }
                } else {
                    markerModifier.remove(onScreen, marker.marker);
                }
            }

            markerModifier.waitUntilFree();

            mMarkers = newMarkers;
            GoogleMapRenderer.this.mClusters = clusters;
            mZoom = zoom;

            mCallback.run();
        }
    }

	@Override
	public void onClustersChanged(Set<? extends Cluster<SimpleRestaurantPin>> clusters) {
		mViewModifier.queue(clusters);
	}

	@Override
	public void setOnClusterClickListener(ClusterManager.OnClusterClickListener<SimpleRestaurantPin> listener) {
		mClickListener = listener;
	}

	@Override
	public void setOnClusterInfoWindowClickListener(ClusterManager.OnClusterInfoWindowClickListener<SimpleRestaurantPin> listener) {
		mInfoWindowClickListener = listener;
	}

	@Override
	public void setOnClusterItemClickListener(ClusterManager.OnClusterItemClickListener<SimpleRestaurantPin> listener) {
		mItemClickListener = listener;
	}

	@Override
	public void setOnClusterItemInfoWindowClickListener(ClusterManager.OnClusterItemInfoWindowClickListener<SimpleRestaurantPin> listener) {
		mItemInfoWindowClickListener = listener;
	}

	private static double distanceSquared(Point a, Point b) {
		return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
	}

	private static Point findClosestCluster(List<Point> markers, Point point) {
		if (markers == null || markers.isEmpty()) return null;

		// TODO: make this configurable.
		double minDistSquared = MAX_DISTANCE_AT_ZOOM * MAX_DISTANCE_AT_ZOOM;
		Point closest = null;
		for (Point candidate : markers) {
			double dist = distanceSquared(candidate, point);
			if (dist < minDistSquared) {
				closest = candidate;
				minDistSquared = dist;
			}
		}
		return closest;
	}

	@SuppressLint("HandlerLeak")
	private class MarkerModifier extends Handler implements MessageQueue.IdleHandler {
        private static final int BLANK = 0;

        private final Lock lock = new ReentrantLock();
        private final Condition busyCondition = lock.newCondition();

        private Queue<CreateMarkerTask> mCreateMarkerTasks = new LinkedList<CreateMarkerTask>();
        private Queue<CreateMarkerTask> mOnScreenCreateMarkerTasks = new LinkedList<CreateMarkerTask>();
        private Queue<Marker> mRemoveMarkerTasks = new LinkedList<Marker>();
        private Queue<Marker> mOnScreenRemoveMarkerTasks = new LinkedList<Marker>();
        private Queue<AnimationTask> mAnimationTasks = new LinkedList<AnimationTask>();

        private boolean mListenerAdded;

        private MarkerModifier() {
            super(Looper.getMainLooper());
        }

        public void add(boolean priority, CreateMarkerTask c) {
            lock.lock();
            sendEmptyMessage(BLANK);
            if (priority) {
                mOnScreenCreateMarkerTasks.add(c);
            } else {
                mCreateMarkerTasks.add(c);
            }
            lock.unlock();
        }

        public void remove(boolean priority, Marker m) {
            lock.lock();
            sendEmptyMessage(BLANK);
            if (priority) {
                mOnScreenRemoveMarkerTasks.add(m);
            } else {
                mRemoveMarkerTasks.add(m);
            }
            lock.unlock();
        }

        public void animate(MarkerWithPosition marker, LatLng from, LatLng to) {
            lock.lock();
            mAnimationTasks.add(new AnimationTask(marker, from, to));
            lock.unlock();
        }

        public void animateThenRemove(MarkerWithPosition marker, LatLng from, LatLng to) {
            lock.lock();
            AnimationTask animationTask = new AnimationTask(marker, from, to);
            animationTask.removeOnAnimationComplete(mClusterManager.getMarkerManager());
            mAnimationTasks.add(animationTask);
            lock.unlock();
        }

        @Override
        public void handleMessage(Message msg) {
            if (!mListenerAdded) {
                Looper.myQueue().addIdleHandler(this);
                mListenerAdded = true;
            }
            removeMessages(BLANK);

            lock.lock();
            try {

                // Perform up to 10 tasks at once.
                // Consider only performing 10 remove tasks, not adds and animations.
                // Removes are relatively slow and are much better when batched.
                for (int i = 0; i < 5; i++) {
                    performNextTask();
                }

                if (!isBusy()) {
                    mListenerAdded = false;
                    Looper.myQueue().removeIdleHandler(this);
                    // Signal any other threads that are waiting.
                    busyCondition.signalAll();
                } else {
                    // Sometimes the idle queue may not be called - schedule up some work regardless
                    // of whether the UI thread is busy or not.
                    // TODO: try to remove this.
                    sendEmptyMessageDelayed(BLANK, 10);
                }
            } finally {
                lock.unlock();
            }
        }

        /**
         * Perform the next task. Prioritise any on-screen work.
         */
        private void performNextTask() {
            if (!mOnScreenRemoveMarkerTasks.isEmpty()) {
                removeMarker(mOnScreenRemoveMarkerTasks.poll());
            } else if (!mAnimationTasks.isEmpty()) {
                mAnimationTasks.poll().perform();
            } else if (!mOnScreenCreateMarkerTasks.isEmpty()) {
                mOnScreenCreateMarkerTasks.poll().perform(this);
            } else if (!mCreateMarkerTasks.isEmpty()) {
                mCreateMarkerTasks.poll().perform(this);
            } else if (!mRemoveMarkerTasks.isEmpty()) {
                removeMarker(mRemoveMarkerTasks.poll());
            }
        }

        private void removeMarker(Marker m) {
            Cluster<SimpleRestaurantPin> cluster = mMarkerToCluster.get(m);
            mClusterToMarker.remove(cluster);
            mMarkerCache.remove(m);
            mMarkerToCluster.remove(m);
            mClusterManager.getMarkerManager().remove(m);
        }

        /**
         * @return true if there is still work to be processed.
         */
        public boolean isBusy() {
            try {
                lock.lock();
                return !(mCreateMarkerTasks.isEmpty() && mOnScreenCreateMarkerTasks.isEmpty() &&
                        mOnScreenRemoveMarkerTasks.isEmpty() && mRemoveMarkerTasks.isEmpty() &&
                        mAnimationTasks.isEmpty()
                );
            } finally {
                lock.unlock();
            }
        }

        /**
         * Blocks the calling thread until all work has been processed.
         */
        public void waitUntilFree() {
            while (isBusy()) {
                // Sometimes the idle queue may not be called - schedule up some work regardless
                // of whether the UI thread is busy or not.
                // TODO: try to remove this.
                sendEmptyMessage(BLANK);
                lock.lock();
                try {
                    if (isBusy()) {
                        busyCondition.await();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    lock.unlock();
                }
            }
        }

        @Override
        public boolean queueIdle() {
            // When the UI is not busy, schedule some work.
            sendEmptyMessage(BLANK);
            return true;
        }
    }

	private static class MarkerCache<SimpleRestaurantPin> {
		private Map<SimpleRestaurantPin, Marker> mCache = new HashMap<SimpleRestaurantPin, Marker>();
		private Map<Marker, SimpleRestaurantPin> mCacheReverse = new HashMap<Marker, SimpleRestaurantPin>();

		public Marker get(SimpleRestaurantPin item) {
			return mCache.get(item);
		}

		public SimpleRestaurantPin get(Marker m) {
			return mCacheReverse.get(m);
		}

		public void put(SimpleRestaurantPin item, Marker m) {
			mCache.put(item, m);
			mCacheReverse.put(m, item);
		}

		public void remove(Marker m) {
			SimpleRestaurantPin item = mCacheReverse.get(m);
			mCacheReverse.remove(m);
			mCache.remove(item);
		}
	}

	protected void onBeforeClusterItemRendered(SimpleRestaurantPin item, MarkerOptions markerOptions) {
	}

	protected void onBeforeClusterRendered(Cluster<SimpleRestaurantPin> cluster, MarkerOptions markerOptions) {
	}

	protected void onClusterRendered(Cluster<SimpleRestaurantPin> cluster, Marker marker) {
	}

	protected void onClusterItemRendered(SimpleRestaurantPin clusterItem, Marker marker) {
	}

	public Marker getMarker(SimpleRestaurantPin  clusterItem) {
		return mMarkerCache.get(clusterItem);
	}

	public SimpleRestaurantPin getClusterItem(Marker marker) {
		return mMarkerCache.get(marker);
	}

	public Marker getMarker(Cluster<SimpleRestaurantPin>  cluster) {
		return mClusterToMarker.get(cluster);
	}

	public Cluster<SimpleRestaurantPin> getCluster(Marker marker) {
		return mMarkerToCluster.get(marker);
	}

	private class CreateMarkerTask {
		private final Cluster<SimpleRestaurantPin> cluster;
		private final Set<MarkerWithPosition> newMarkers;
		private final LatLng animateFrom;


		public CreateMarkerTask(Cluster<SimpleRestaurantPin> c, Set<MarkerWithPosition> markersAdded, LatLng animateFrom) {
			this.cluster = c;
			this.newMarkers = markersAdded;
			this.animateFrom = animateFrom;
		}

		private void perform(MarkerModifier markerModifier) {

			// Don't show small clusters. Render the markers inside, instead.
			if (!shouldRenderAsCluster(cluster)) {
				for (SimpleRestaurantPin item : cluster.getItems()) {
					Marker marker = mMarkerCache.get(item);
					MarkerWithPosition markerWithPosition;
					if (marker == null) {
						MarkerOptions markerOptions = new MarkerOptions();
						if (animateFrom != null) {
							markerOptions.position(animateFrom);
						} else {
							markerOptions.position(item.getPosition());
						}

						onBeforeClusterItemRendered(item, markerOptions);
						marker = mClusterManager.getMarkerCollection().addMarker(markerOptions);
						markerWithPosition = new MarkerWithPosition(marker);
						mMarkerCache.put(item, marker);
						if (animateFrom != null) {
							markerModifier.animate(markerWithPosition, animateFrom, item.getPosition());
						}
						onClusterItemRendered(item, marker);
					} else {
						markerWithPosition = new MarkerWithPosition(marker);
					}
					onClusterItemRendered(item, marker);
					newMarkers.add(markerWithPosition);
					
				}
				return;
			}

			Marker marker = mClusterToMarker.get(cluster);
			MarkerWithPosition markerWithPosition;
			if (marker == null){
				MarkerOptions markerOptions = new MarkerOptions().
						//position(cluster.getPosition());
						position(animateFrom == null ? cluster.getPosition() : animateFrom);
				onBeforeClusterRendered(cluster, markerOptions);

				marker = mClusterManager.getClusterMarkerCollection().addMarker(markerOptions);
				mMarkerToCluster.put(marker, cluster);
				mClusterToMarker.put(cluster, marker);
				markerWithPosition = new MarkerWithPosition(marker);
				if (animateFrom != null) {
					markerModifier.animate(markerWithPosition, animateFrom, cluster.getPosition());
				}
				onClusterRendered(cluster, marker);
			} else {
				markerWithPosition = new MarkerWithPosition(marker);
			}
			onClusterRendered(cluster, marker);
			newMarkers.add(markerWithPosition);
		}
	}

	/**
	 * A Marker and its position. Marker.getPosition() must be called from the UI thread, so this
	 * object allows lookup from other threads.
	 */
	private static class MarkerWithPosition {
		private final Marker marker;
		private LatLng position;

		private MarkerWithPosition(Marker marker) {
			this.marker = marker;
			position = marker.getPosition();
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof MarkerWithPosition) {
				return marker.equals(((MarkerWithPosition) other).marker);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return marker.hashCode();
		}
	}

	private static final TimeInterpolator ANIMATION_INTERP = new DecelerateInterpolator();

	/**
	 * Animates a markerWithPosition from one position to another. TODO: improve performance for
	 * slow devices (e.g. Nexus S).
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private class AnimationTask extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {
		private final MarkerWithPosition markerWithPosition;
		private final Marker marker;
		private final LatLng from;
		private final LatLng to;
		private boolean mRemoveOnComplete;
		private GoogleMarkerManager mMarkerManager;

		private AnimationTask(MarkerWithPosition markerWithPosition, LatLng from, LatLng to) {
			this.markerWithPosition = markerWithPosition;
			this.marker = markerWithPosition.marker;
			this.from = from;
			this.to = to;
		}

		public void perform() {
			ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
			valueAnimator.setInterpolator(ANIMATION_INTERP);
			valueAnimator.addUpdateListener(this);
			valueAnimator.addListener(this);
			valueAnimator.start();
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			if (mRemoveOnComplete) {
				Cluster<SimpleRestaurantPin> cluster = mMarkerToCluster.get(marker);
				mClusterToMarker.remove(cluster);
				mMarkerCache.remove(marker);
				mMarkerToCluster.remove(marker);
				mMarkerManager.remove(marker);
			}
			markerWithPosition.position = to;
		}

		public void removeOnAnimationComplete(GoogleMarkerManager markerManager) {
			mMarkerManager = markerManager;
			mRemoveOnComplete = true;
		}

		@Override
		public void onAnimationUpdate(ValueAnimator valueAnimator) {
			float fraction = valueAnimator.getAnimatedFraction();
			double lat = (to.latitude - from.latitude) * fraction + from.latitude;
			double lngDelta = to.longitude - from.longitude;

			// Take the shortest path across the 180th meridian.
			if (Math.abs(lngDelta) > 180) {
				lngDelta -= Math.signum(lngDelta) * 360;
			}
			double lng = lngDelta * fraction + from.longitude;
			LatLng position = new LatLng(lat, lng);
			marker.setPosition(position);
		}
	}
}
