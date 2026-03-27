package org.sopt.makers.internal.common.config;

import com.querydsl.core.types.Ops;
import com.querydsl.jpa.HQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryDslConfig {

    // Hibernate 6 + PostgreSQL 조합에서 LIKE ... ESCAPE '!' 의 따옴표를 누락하는 버그 우회
    private static final HQLTemplates LIKE_WITHOUT_ESCAPE = new HQLTemplates() {
        {
            add(Ops.LIKE, "{0} like {1}");
            add(Ops.LIKE_IC, "lower({0}) like lower({1})");
            add(Ops.STRING_CONTAINS, "{0} like {%1%}");
            add(Ops.STRING_CONTAINS_IC, "lower({0}) like lower({%1%})");
            add(Ops.STARTS_WITH, "{0} like {1%}");
            add(Ops.STARTS_WITH_IC, "lower({0}) like lower({1%})");
            add(Ops.ENDS_WITH, "{0} like {%1}");
            add(Ops.ENDS_WITH_IC, "lower({0}) like lower({%1})");
        }
    };

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(LIKE_WITHOUT_ESCAPE, entityManager);
    }
}