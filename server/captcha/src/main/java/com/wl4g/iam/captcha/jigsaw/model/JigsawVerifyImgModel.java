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
package com.wl4g.iam.captcha.jigsaw.model;

import javax.validation.constraints.NotNull;

import com.wl4g.iam.verify.model.BaseVerifyCodeModel;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Analyze verify jigsaw image model.
 * 
 * @author James Wong
 * @version v1.0 2019年8月30日
 * @since
 */
public class JigsawVerifyImgModel extends BaseVerifyCodeModel {
    private static final long serialVersionUID = 4975604364412626949L;

    @NotNull
    private String x;

    private List<Trail> trails = new ArrayList<>(); // AI enhanced check

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public List<Trail> getTrails() {
        return Objects.isNull(trails) ? emptyList() : trails;
    }

    public void setTrails(List<Trail> trails) {
        if (Objects.nonNull(trails)) {
            this.trails = trails;
        }
    }

    /**
     * The time coordinate information of mouse pointer on slider can increase
     * CNN machine learning model checking.
     * 
     * @author James Wong
     * @version v1.0.0 2019-09-05
     * @since
     */
    public static class Trail {
        private Long t;
        private Integer x;
        private Integer y;

        public Long getT() {
            return t;
        }

        public void setT(Long timestamp) {
            this.t = timestamp;
        }

        public Integer getX() {
            return x;
        }

        public void setX(Integer x) {
            this.x = x;
        }

        public Integer getY() {
            return y;
        }

        public void setY(Integer y) {
            this.y = y;
        }

        @Override
        public String toString() {
            return "Trail [t=" + t + ", x=" + x + ", y=" + y + "]";
        }

    }

}