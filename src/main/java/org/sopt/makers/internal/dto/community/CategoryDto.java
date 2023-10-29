package org.sopt.makers.internal.dto.community;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.makers.internal.domain.community.Category;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CategoryDto {

    private Long id;
    private String name;
    private List<CategoryDto> children;

    public static List<CategoryDto> toDtoList(List<Category> categories) {
        CategoryHelper helper = CategoryHelper.newInstance(
                categories,
                c -> new CategoryDto(c.getId(), c.getName(), new ArrayList<>()),
                Category::getParent,
                Category::getId,
                CategoryDto::getChildren);
        return helper.convert();
    }
}