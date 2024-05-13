package org.sopt.makers.internal.dto.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GabiaSMSResponseData {
        private String BEFORE_SMS_QTY;
        private String AFTER_SMS_QTY;
}

