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
package com.wl4g.iam.common.model.oidc.v1;

import java.util.Date;

import com.wl4g.iam.common.subject.IamPrincipal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.experimental.Wither;

/**
 * {@link V1MetadataEndpointModel} </br>
 * 
 * <p>
 * https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims
 * 
 * <pre>
 *   Member                 Type         Description
 *   sub                    string       Subject - Identifier for the End-User at the Issuer.
 *   name                   string       End-User's full name in displayable form including all name parts, possibly including titles and suffixes, ordered according to the End-User's locale and preferences.
 *   given_name             string       Given name(s) or first name(s) of the End-User. Note that in some cultures, people can have multiple given names; all can be present, with the names being separated by space characters.
 *   family_name            string       Surname(s) or last name(s) of the End-User. Note that in some cultures, people can have multiple family names or no family name; all can be present, with the names being separated by space characters.
 *   middle_name            string       Middle name(s) of the End-User. Note that in some cultures, people can have multiple middle names; all can be present, with the names being separated by space characters. Also note that in some cultures, middle names are not used.
 *   nickname               string       Casual name of the End-User that may or may not be the same as the given_name. For instance, a nickname value of Mike might be returned alongside a given_name value of Michael.
 *   preferred_username     string       Shorthand name by which the End-User wishes to be referred to at the RP, such as janedoe or j.doe. This value MAY be any valid JSON string including special characters such as @, /, or whitespace. The RP MUST NOT rely upon this value being unique, as discussed in Section 5.7.
 *   profile                string       URL of the End-User's profile page. The contents of this Web page SHOULD be about the End-User.
 *   picture                string       URL of the End-User's profile picture. This URL MUST refer to an image file (for example, a PNG, JPEG, or GIF image file), rather than to a Web page containing an image. Note that this URL SHOULD specifically reference a profile photo of the End-User suitable for displaying when describing the End-User, rather than an arbitrary photo taken by the End-User.
 *   website                string       URL of the End-User's Web page or blog. This Web page SHOULD contain information published by the End-User or an organization that the End-User is affiliated with.
 *   email                  string       End-User's preferred e-mail address. Its value MUST conform to the RFC 5322 [RFC5322] addr-spec syntax. The RP MUST NOT rely upon this value being unique, as discussed in Section 5.7.
 *   email_verified         boolean      True if the End-User's e-mail address has been verified; otherwise false. When this Claim Value is true, this means that the OP took affirmative steps to ensure that this e-mail address was controlled by the End-User at the time the verification was performed. The means by which an e-mail address is verified is context-specific, and dependent upon the trust framework or contractual agreements within which the parties are operating.
 *   gender                 string       End-User's gender. Values defined by this specification are female and male. Other values MAY be used when neither of the defined values are applicable.
 *   birthdate              string       End-User's birthday, represented as an ISO 8601:2004 [ISO8601‑2004] YYYY-MM-DD format. The year MAY be 0000, indicating that it is omitted. To represent only the year, YYYY format is allowed. Note that depending on the underlying platform's date related function, providing just year can result in varying month and day, so the implementers need to take this factor into account to correctly process the dates.
 *   zoneinfo               string       String from zoneinfo [zoneinfo] time zone database representing the End-User's time zone. For example, Europe/Paris or America/Los_Angeles.
 *   locale                 string       End-User's locale, represented as a BCP47 [RFC5646] language tag. This is typically an ISO 639-1 Alpha-2 [ISO639‑1] language code in lowercase and an ISO 3166-1 Alpha-2 [ISO3166‑1] country code in uppercase, separated by a dash. For example, en-US or fr-CA. As a compatibility note, some implementations have used an underscore as the separator rather than a dash, for example, en_US; Relying Parties MAY choose to accept this locale syntax as well.
 *   phone_number           string       End-User's preferred telephone number. E.164 [E.164] is RECOMMENDED as the format of this Claim, for example, +1 (425) 555-1212 or +56 (2) 687 2400. If the phone number contains an extension, it is RECOMMENDED that the extension be represented using the RFC 3966 [RFC3966] extension syntax, for example, +1 (604) 555-1234;ext=5678.
 *   phone_number_verified  boolean      True if the End-User's phone number has been verified; otherwise false. When this Claim Value is true, this means that the OP took affirmative steps to ensure that this phone number was controlled by the End-User at the time the verification was performed. The means by which a phone number is verified is context-specific, and dependent upon the trust framework or contractual agreements within which the parties are operating. When true, the phone_number Claim MUST be in E.164 format and any extensions MUST be represented in RFC 3966 format.
 *   address                JSON-Object  End-User's preferred postal address. The value of the address member is a JSON [RFC4627] structure containing some or all of the members defined in Section 5.1.1.
 *   updated_at             number       Time the End-User's information was last updated. Its value is a JSON number representing the number of seconds from 1970-01-01T0:0:0Z as measured in UTC until the date/time.
 * </pre>
 * </p>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2022-03-17 v1.0.0
 * @since v3.0.0
 */
@Getter
@Setter
@ToString
@Wither
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class V1OidcUserClaims {
    private IamPrincipal principal;
    // profile
    private String sub; // like is user_id
    private String name;
    private String given_name;
    private String family_name;
    private String middleName;
    private String nickname;
    private String preferred_username;
    private String gender;
    private String locale;
    private Date birthdate;
    private String picture;
    private String zoneinfo;
    private Date updated_at;
    // independent scoped attributes info
    private String email;
    private Boolean email_verified;
    private AddressClaim address;
    private String phone_number;
    private Boolean phone_number_verified;

    /**
     * https://openid.net/specs/openid-connect-core-1_0.html#AddressClaim
     */
    @Getter
    @Setter
    @ToString
    @Wither
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressClaim {
        private String formatted;
        private String street_address;
        private String locality;
        private String region;
        private String postal_code;
        private String country;
    }

}
