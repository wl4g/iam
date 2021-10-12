/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.gateway.server.route;

import com.wl4g.component.common.task.GenericTaskRunner;
import com.wl4g.component.common.task.RunnerProperties;
import com.wl4g.gateway.server.config.RefreshProperties;
import com.wl4g.gateway.server.exception.CurrentlyInRefreshingException;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import static com.wl4g.component.common.lang.Assert2.isTrue;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.ScheduledFuture;

/**
 * Gateway configuration refreshable scheduler coordinator.
 *
 * @author Wangl.sir <983708408@qq.com>
 * @version 2019年6月2日
 * @since v1.0
 */
public class TimingTaskRefresher extends GenericTaskRunner<RunnerProperties> implements ApplicationRunner, DisposableBean {

	@Autowired
	protected RefreshProperties config;

	@Autowired
	protected IRouteCacheRefresh refresher;

	/** Refreshing scheduled future */
	protected ScheduledFuture<?> future;

	public TimingTaskRefresher() {
		super(new RunnerProperties().withConcurrency(1));
	}

	@Override
	public void destroy() throws Exception {
		super.close();
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		super.start();
	}

	/**
	 * Starting configuration refresher schedule.
	 */
	@Override
	public void run() {
		log.info("Starting configuration refresher scheduling...  with refreshDelayMs: {}", config.getRefreshDelayMs());

		// Init scheduling
		createRefreshScheduling();
	}

	/**
	 * Restarting configuration refresher schedule.
	 */
	public void restartRefresher() {
		isTrue(future.cancel(false), CurrentlyInRefreshingException.class,
				"Updating refreshDelayMs failed, because refreshing is currently in progressing");

		// Re-create scheduling
		createRefreshScheduling();
	}

	public IRouteCacheRefresh getRefresher() {
		return refresher;
	}

	/**
	 * Creating refresh scheduling.
	 * 
	 * @throws IllegalStateException
	 */
	private void createRefreshScheduling() throws IllegalStateException {
		if (nonNull(future) && !future.isDone()) {
			throw new IllegalStateException(format("No done last schdule task future for %s", future));
		}

		this.future = getWorker().scheduleWithFixedDelay(() -> {
			try {
				log.info("flushRoutesPermanentToMemery");
				refresher.flushRoutesPermanentToMemery();
			} catch (Exception e) {
				log.error("fresh fail", e);
			}
		}, 1_000L, config.getRefreshDelayMs(), MILLISECONDS);

	}

}