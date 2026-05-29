package org.sopt.makers.internal.community.repository.category;

import org.sopt.makers.internal.community.domain.category.Category;
import org.sopt.makers.internal.community.domain.enums.CommunityCategoryCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCodeAndIsActiveTrue(CommunityCategoryCode code);

    List<Category> findAllByCodeInAndIsActiveTrue(List<CommunityCategoryCode> codes);

	@Query("""
        select category
        from Category category
        left join fetch category.parent
        where category.isActive = true
        order by category.displayOrder asc, category.id asc
    """)
	List<Category> findAllActiveWithParentOrderByDisplayOrderAsc();
}
