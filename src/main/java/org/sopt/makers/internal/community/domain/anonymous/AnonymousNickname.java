package org.sopt.makers.internal.community.domain.anonymous;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnonymousNickname {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "anonymous_nickname_id")
	private Long id;

	@Column(nullable = false)
	String nickname;

	@Builder
	private AnonymousNickname(Long id, String nickname) {
		this.id = id;
		this.nickname = nickname;
	}
}
