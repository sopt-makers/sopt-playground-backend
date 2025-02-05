package org.sopt.makers.internal.report.repository;

import org.sopt.makers.internal.report.domain.AmplitudeEventRawData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AmplitudeEventRawDataRepository extends JpaRepository<AmplitudeEventRawData, String> {

	int countAllByUserIdAndEventTypeAndEventTimeContains(String userId, String eventType, String eventTime);

	@Query(value = "SELECT COUNT(*) FROM internal_dev.amplitude_event_raw_data " +
			"WHERE user_id = :userId " +
			"AND event_type = :eventType " +
			"AND \"event_properties_[Amplitude] Page Path\" = :eventPropertiesPagePath " +
			"AND event_time LIKE %:eventTime%",
			nativeQuery = true)
	int countByUserIdAndEventTypeAndEventPropertiesPagePathAndEventTime(
			@Param("userId") String userId,
			@Param("eventType") String eventType,
			@Param("eventPropertiesPagePath") String eventPropertiesPagePath,
			@Param("eventTime") String eventTime);
}
