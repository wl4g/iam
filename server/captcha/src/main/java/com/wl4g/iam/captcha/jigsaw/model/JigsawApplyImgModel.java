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

import javax.validation.constraints.NotBlank;

import com.wl4g.iam.verify.model.BaseVerifyCodeModel;

/**
 * Apply jigsaw image model
 * 
 * @author James Wong
 * @version v1.0 2019年8月30日
 * @since
 */
public class JigsawApplyImgModel extends BaseVerifyCodeModel {
    private static final long serialVersionUID = 4975604164412626949L;

    private int y;

    @NotBlank
    private String primaryImg; // Image base64

    @NotBlank
    private String blockImg;

    @NotBlank
    private String secret;

    public JigsawApplyImgModel() {
        super();
    }

    public JigsawApplyImgModel(String graphToken, String verifyType) {
        setApplyToken(graphToken);
        setVerifyType(verifyType);
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getPrimaryImg() {
        return primaryImg;
    }

    public void setPrimaryImg(String primaryImg) {
        this.primaryImg = primaryImg;
    }

    public String getBlockImg() {
        return blockImg;
    }

    public void setBlockImg(String blockImg) {
        this.blockImg = blockImg;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}