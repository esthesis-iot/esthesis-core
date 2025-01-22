package esthesis.core.common.dashboard;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.TimeUnit;

/**
 * A local cache manager, for components to cache which clients are currently viewing a dashboard
 * interested for the data provided by the component. Entries in this cache are set to automatically
 * expire after 1 hour, so dashboards should refresh their subscription to the cache every hour.
 */
@ApplicationScoped
public class DashboardClientCache {

	private Cache<String, String> cache;

	@PostConstruct
	private void setup() {
		cache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).maximumSize(1024).build();
	}

	public void put(String key, String value) {
		cache.put(key, value);
	}

	public void invalidate(String key) {
		cache.invalidate(key);
	}

	public void get(String key) {
		cache.getIfPresent(key);
	}
}
