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
package com.wl4g.iam.common.bean.ext;

import com.wl4g.infra.common.bean.BaseBean;

import lombok.Getter;
import lombok.Setter;

/**
 * @author vjay
 * @date 2019-11-19 11:33:00
 */
@Getter
@Setter
public class GroupExt extends BaseBean {
    private static final long serialVersionUID = 6302283757380254809L;

    private Long groupId;
    private String displayName;
    private String contact;
    private String contactPhone;
    private String address;

    public static enum GroupType {

        Park(1),

        Company(2),

        Department(3);

        int value;

        GroupType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        /**
         * Converter string to {@link Action}
         *
         * @param action
         * @return
         */
        public static GroupType of(Integer type) {
            GroupType wh = safeOf(type);
            if (wh == null) {
                throw new IllegalArgumentException(String.format("Illegal type '%s'", type));
            }
            return wh;
        }

        /**
         * Safe converter string to {@link Action}
         *
         * @param action
         * @return
         */
        public static GroupType safeOf(Integer type) {
            if (type == null) {
                return null;
            }
            for (GroupType t : values()) {
                if (type.intValue() == t.getValue()) {
                    return t;
                }
            }
            return null;
        }
    }

}