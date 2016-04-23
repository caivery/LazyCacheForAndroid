/*******************************************************************************
 * Copyright 2011-2014 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package om.robin.lazy.cache.memory.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.lazy.library.cache.LimitedAge;
import com.lazy.library.cache.memory.MemoryCache;
import com.lazy.library.util.logger.LazyLogger;

/**
 * Decorator for {@link MemoryCache}. Provides special feature for cache: if
 * some cached object age exceeds defined value then this object will be removed
 * from cache.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @see MemoryCache
 * @since 1.3.1
 */
public class LimitedAgeMemoryCache implements MemoryCache {

	private final MemoryCache cache;

	private final long maxAge;
	private final Map<String, LimitedAge> loadingDates = Collections
			.synchronizedMap(new HashMap<String, LimitedAge>());

	/**
	 * @param cache
	 *            Wrapped memory cache
	 * @param maxAge
	 *            Max object age <b>(in seconds)</b>. If object age will exceed
	 *            this value then it'll be removed from cache on next treatment
	 *            (and therefore be reloaded).
	 */
	public LimitedAgeMemoryCache(MemoryCache cache, long maxAge) {
		this.cache = cache;
		this.maxAge = maxAge;
	}

	@Override
	public <V> boolean put(String key, V value) {
		if (cache == null) {
			LazyLogger.e(new NullPointerException(), "MemoryCache缓存操作对象为空");
			return false;
		}
		boolean putSuccesfully = cache.put(key, value);
		if (putSuccesfully) {
			loadingDates.put(key, new LimitedAge(System.currentTimeMillis(),
					maxAge));
		}
		return putSuccesfully;
	}

	@Override
	public <V> boolean put(String key, V value, long maxLimitTime) {
		if (cache == null) {
			LazyLogger.e(new NullPointerException(), "MemoryCache缓存操作对象为空");
			return false;
		}
		boolean putSuccesfully = cache.put(key, value);
		if (putSuccesfully) {
			loadingDates.put(key, new LimitedAge(System.currentTimeMillis(),
					maxLimitTime));
		}
		return putSuccesfully;
	}

	@Override
	public <V> V get(String key) {
		if (cache == null) {
			LazyLogger.e(new NullPointerException(), "MemoryCache缓存操作对象为空");
			return null;
		}
		LimitedAge loadingDate = loadingDates.get(key);
		if (loadingDate != null && loadingDate.checkExpire()) {
			cache.remove(key);
			loadingDates.remove(key);
		}

		return cache.get(key);
	}

	@Override
	public boolean remove(String key) {
		if (cache == null) {
			LazyLogger.e(new NullPointerException(), "MemoryCache缓存操作对象为空");
			return false;
		}
		loadingDates.remove(key);
		return cache.remove(key);
	}

	@Override
	public Collection<String> keys() {
		if (cache == null) {
			LazyLogger.e(new NullPointerException(), "MemoryCache缓存操作对象为空");
			return null;
		}
		return cache.keys();
	}

	@Override
	public Map<String, ?> snapshot() {
		if (cache == null) {
			LazyLogger.e(new NullPointerException(), "MemoryCache缓存操作对象为空");
			return null;
		}
		return cache.snapshot();
	}

	@Override
	public void resize(int maxSize) {
		if (cache == null) {
			LazyLogger.e(new NullPointerException(), "MemoryCache缓存操作对象为空");
			return;
		}
		cache.resize(maxSize);
	}

	@Override
	public void clear() {
		if (cache == null) {
			LazyLogger.e(new NullPointerException(), "MemoryCache缓存操作对象为空");
			return;
		}
		loadingDates.clear();
		cache.clear();
	}

	@Override
	public void close() {
		if (cache == null) {
			LazyLogger.e(new NullPointerException(), "MemoryCache缓存操作对象为空");
			return;
		}
		loadingDates.clear();
		cache.close();
	}

}
