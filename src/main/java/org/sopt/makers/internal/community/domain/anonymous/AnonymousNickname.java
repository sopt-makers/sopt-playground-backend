package org.sopt.makers.internal.community.domain.anonymous;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AnonymousNickname {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "anonymous_nickname_id")
	private Long id;

	@Column(nullable = false)
	String nickname;
}
