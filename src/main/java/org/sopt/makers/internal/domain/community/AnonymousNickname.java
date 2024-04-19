package org.sopt.makers.internal.domain.community;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AnonymousNickname {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "anonymous_nickname_id")
	private Long id;

	@Column(nullable = false)
	String nickname;
}
