package org.sopt.makers.internal.common.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class JPAQueryInspector implements StatementInspector {

	private static final ThreadLocal<QueryManager> queryManagers = new ThreadLocal<>();

	void start() {
		queryManagers.set(new QueryManager(
				new ArrayList<>(),
				System.currentTimeMillis()
		));
	}

	void finish() {
		queryManagers.remove();
	}

	@Override
	public String inspect(String sql) {
		log.info("🚀sql: {}", sql);
		QueryManager queryManager = queryManagers.get();
		if (queryManager != null) {
			queryManager.addQuery(sql);
		}
		return sql;
	}

	public QueryInspectResult inspectResult() {
		QueryManager queryManager = queryManagers.get();
		long queryDurationTime = queryManager.calculateDuration(System.currentTimeMillis());
		checkQueryCountIsOverThanMaxCount(queryManager);
		return new QueryInspectResult(queryManager.getQueryCount(), queryDurationTime);
	}

	private void checkQueryCountIsOverThanMaxCount(@NotNull QueryManager queryManager) {
		if (queryManager.isOverThanMaxQueryCount()) {
			log.warn("🚨쿼리가 10번 이상 실행되었습니다");
			checkIsSusceptibleToNPlusOne(queryManager);
		}
	}

	private void checkIsSusceptibleToNPlusOne(@NotNull QueryManager queryManager) {
		NPlusOneDetector nPlusOneDetector = new NPlusOneDetector(queryManager.extractIndexOfSelectQuery());
		if (nPlusOneDetector.isSelectCountOverThanWarnCount() && nPlusOneDetector.detect()) {
			log.warn("🚨select 문이 연속해서 5회 이상 실행되었습니다. N+1 문제일 수 있습니다");
		}
	}
}
