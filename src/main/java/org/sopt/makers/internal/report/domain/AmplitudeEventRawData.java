package org.sopt.makers.internal.report.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Immutable
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AmplitudeEventRawData {

	@Id
	@Column(name = "\"$insert_id\"")
	private String insertId;
	private String userId;
	private String eventType;
	private String eventTime;
	@Column(name = "\"event_properties_[Amplitude] Page Path\"")
	private String eventPropertiesPagePath;
}

