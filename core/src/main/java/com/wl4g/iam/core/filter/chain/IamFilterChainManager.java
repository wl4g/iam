/*
 * Copyright 2017 ~ 2025 the original authors James Wong.
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
package com.wl4g.iam.core.filter.chain;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;

import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.NamedFilterList;
import org.apache.shiro.web.filter.mgt.SimpleNamedFilterList;
import org.springframework.util.CollectionUtils;

/**
 * IAM customize filter chain manager.
 *
 * @author James Wong<jamewong1376@gmail.com>
 * @version v1.0 2019年4月25日
 * @since
 */
public class IamFilterChainManager extends DefaultFilterChainManager {

	/**
	 * Premise fixed filters.
	 */
	private ArrayList<Filter> premiseFilters = new ArrayList<>();

	public IamFilterChainManager() {
	}

	public IamFilterChainManager(List<Filter> premiseFilters) {
		if (!CollectionUtils.isEmpty(premiseFilters)) {
			this.premiseFilters.addAll(premiseFilters);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected NamedFilterList ensureChain(String chainName) {
		NamedFilterList chain = getChain(chainName);
		if (chain == null) {
			/*
			 * Cloning is required here, or multiple calls to the new
			 * SimpleNamedFilterList will result in additional premiseFilter
			 * duplication.
			 */
			chain = new SimpleNamedFilterList(chainName, (List<Filter>) premiseFilters.clone());
			getFilterChains().put(chainName, chain);
		}
		return chain;
	}

}