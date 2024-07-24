package org.sopt.makers.internal.dto.auth;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegistrationPhoneRequest(
	@Schema(required = true)
	@NotBlank(message = "전화번호가 비어 있습니다.")
	@Pattern(regexp = "^010\\d{8}$", message = "잘못된 전화번호 형식입니다. '-'을 제외한 11자의 번호를 입력해주세요.")
	String phone
) { }
