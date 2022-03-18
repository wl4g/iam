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
package com.wl4g.iam.common.model;

import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

import javax.validation.constraints.NotNull;

import com.wl4g.iam.common.subject.IamPrincipal;

/**
 * Concrete Implementation of the {@link ServiceTicketValidateModel}.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @Long 2018年11月22日
 * @since
 */
public final class ServiceTicketValidateModel<T extends IamPrincipal> {

    /** The Long from which the assertion is valid(start Long). */
    @NotNull
    private Long validFromTime;

    /** The Long the assertion is valid until(end Long). */
    @NotNull
    private Long validUntilTime;

    /** The principal for which this assertion is valid for. */
    @NotNull
    private T iamPrincipal;

    public ServiceTicketValidateModel() {
        super();
    }

    /**
     * Creates a new Assertion with the supplied principal, Assertion
     * attributes, and start and valid until Longs.
     *
     * @param principal
     *            the Principal to associate with the Assertion.
     * @param validFromTime
     *            when the assertion is valid from.
     * @param validUntilTime
     *            when the assertion is valid to.
     * @param attributes
     *            the key/value pairs for this attribute.
     */
    public ServiceTicketValidateModel(final String principal, final Long validFromTime, final Long validUntilTime,
            final String grantTicket, final T iamPrincipal) {
        hasText(principal, "Authenticate principal cannot be null.");
        notNull(validFromTime, "Authenticate validFromTime cannot be null.");
        notNull(validUntilTime, "Authenticate validUntilTime cannot be null.");
        notNull(iamPrincipal, "Authenticate iamPrincipal cannot be null.");
        setValidFromTime(validFromTime);
        setValidUntilTime(validUntilTime);
        setIamPrincipal(iamPrincipal);
    }

    public final Long getValidFromTime() {
        return validFromTime;
    }

    public final void setValidFromTime(Long validFromTime) {
        if (this.validFromTime == null && validFromTime != null) {
            this.validFromTime = validFromTime;
        }
    }

    public final Long getValidUntilTime() {
        return validUntilTime;
    }

    public final void setValidUntilTime(Long validUntilTime) {
        if (this.validUntilTime == null && validUntilTime != null) {
            this.validUntilTime = validUntilTime;
        }
    }

    @NotNull
    public final T getIamPrincipal() {
        return iamPrincipal;
    }

    public final void setIamPrincipal(T iamPrincipal) {
        if (this.iamPrincipal == null && iamPrincipal != null) {
            this.iamPrincipal = iamPrincipal;
        }
    }

    @Override
    public String toString() {
        return toJSONString(this);
    }

}