package org.sopt.makers.internal.common.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RequiredArgsConstructor
public class JPAQueryManageInterceptor implements HandlerInterceptor {

	private final JPAQueryInspector jpaQueryInspector;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		jpaQueryInspector.start();
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		QueryInspectResult queryInspectResult = jpaQueryInspector.inspectResult();
		log.info("METHOD: [{}], URI: {}, QUERY_COUNT: {}, QUERY_EXECUTION_TIME: {} ms",
				request.getMethod(),
				request.getRequestURI(),
				queryInspectResult.count(),
				queryInspectResult.time()
		);
		jpaQueryInspector.finish();
	}
}
