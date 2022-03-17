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
package com.wl4g.iam.client;

import static com.wl4g.infra.common.lang.Assert2.notEmptyOf;
import static com.wl4g.infra.common.lang.Assert2.notNullOf;

import java.util.LinkedList;
import java.util.List;

import com.wl4g.iam.client.configure.IamConfigurer;
import com.wl4g.iam.client.configure.StandardEnvironmentIamConfigurer;
import com.wl4g.iam.client.core.RequestMappingDispatcher;
import com.wl4g.iam.client.core.ServletFilterMappingDispatcher;
import com.wl4g.iam.client.handler.WebIamHandler;

/**
 * {@link IamClientBuilder}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-09-04
 * @sine v1.0.0
 * @see
 */
public class IamClientBuilder {

	/** {@link IamConfigurer} */
	protected IamConfigurer configurer = new StandardEnvironmentIamConfigurer();

	/** {@link DispatcherFactory} */
	protected DispatcherFactory factory = new DispatcherFactory() {
	};

	/** {@link WebIamHandler} */
	protected final List<WebIamHandler> handlers = new LinkedList<>();

	/**
	 * New {@link IamClientBuilder}
	 * 
	 * @return
	 */
	public static IamClientBuilder newBuilder() {
		return new IamClientBuilder();
	}

	private IamClientBuilder() {
	}

	/**
	 * Sets configurer of {@link IamConfigurer} .
	 * 
	 * @param provider
	 * @return
	 */
	public IamClientBuilder configurer(IamConfigurer configurer) {
		notNullOf(configurer, "configurer");
		this.configurer = configurer;
		return this;
	}

	/**
	 * Sets dispatcher class of {@link RequestMappingDispatcher} .
	 * 
	 * @param provider
	 * @return
	 */
	public IamClientBuilder factory(DispatcherFactory factory) {
		notNullOf(factory, "factory");
		this.factory = factory;
		return this;
	}

	/**
	 * Sets handlers of {@link WebIamHandler}.
	 * 
	 * @param handlers
	 * @return
	 */
	public IamClientBuilder handlers(WebIamHandler... handlers) {
		notEmptyOf(handlers, "handlers");
		for (WebIamHandler handler : handlers) {
			this.handlers.add(handler);
		}
		return this;
	}

	/**
	 * Build of {@link RequestMappingDispatcher}
	 * 
	 * @return
	 */
	public RequestMappingDispatcher build() {
		return factory.newDispatcher(configurer, handlers);
	}

	/**
	 * {@link DispatcherFactory}
	 *
	 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
	 * @version v1.0 2020-09-08
	 * @since
	 */
	public static interface DispatcherFactory {

		/**
		 * New dispatcher
		 * 
		 * @param configurer
		 * @param handlers
		 * @return
		 */
		default RequestMappingDispatcher newDispatcher(IamConfigurer configurer, List<WebIamHandler> handlers) {
			return new ServletFilterMappingDispatcher(configurer, handlers);
		}

	}

}
