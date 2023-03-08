/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <jameswong1376@gmail.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: http://wl4g.com
 */
package com.wl4g.iam.data.fallback;

import static com.wl4g.infra.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;
import static java.util.Collections.emptyList;

import java.util.List;

import org.springframework.stereotype.Component;

import com.wl4g.infra.common.log.SmartLogger;
import com.wl4g.iam.common.bean.Contact;
import com.wl4g.iam.data.ContactDao;

import feign.hystrix.FallbackFactory;

/**
 * {@link ContactDaoFallbackFactory}
 * 
 * @author James Wong &lt;jameswong1376@gmail.com&gt;
 * @version v1.0 2021-01-07
 * @sine v1.0
 * @see
 */
@Component
public class ContactDaoFallbackFactory implements FallbackFactory<ContactDao> {
    protected final SmartLogger log = getLogger(getClass());

    @Override
    public ContactDao create(Throwable cause) {
        return new ContactDao() {

            @Override
            public int deleteByPrimaryKey(Long id) {
                log.warn(format("Cannot deleteByPrimaryKey, fallback handling... - {}", id), cause);
                return 0;
            }

            @Override
            public int insert(Contact record) {
                log.warn(format("Cannot insert, fallback handling... - {}", record), cause);
                return 0;
            }

            @Override
            public int insertSelective(Contact record) {
                log.warn(format("Cannot insertSelective, fallback handling... - {}", record), cause);
                return 0;
            }

            @Override
            public Contact selectByPrimaryKey(Long id) {
                log.warn(format("Cannot selectByPrimaryKey, fallback handling... - {}", id), cause);
                return null;
            }

            @Override
            public int updateByPrimaryKeySelective(Contact record) {
                log.warn(format("Cannot updateByPrimaryKeySelective, fallback handling... - {}", record), cause);
                return 0;
            }

            @Override
            public int updateByPrimaryKey(Contact record) {
                log.warn(format("Cannot updateByPrimaryKey, fallback handling... - {}", record), cause);
                return 0;
            }

            @Override
            public List<Contact> list(String name) {
                log.warn(format("Cannot list, fallback handling... - {}", name), cause);
                return emptyList();
            }

            @Override
            public List<Contact> getContactByGroupIds(List<Long> groupIds) {
                log.warn(format("Cannot getContactByGroupIds, fallback handling... - {}", groupIds), cause);
                return null;
            }
        };
    }

}
