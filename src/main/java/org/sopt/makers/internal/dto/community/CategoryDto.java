package org.sopt.makers.internal.dto.community;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sopt.makers.internal.domain.community.Category;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CategoryDto {

    private Long id;
    private String name;
    private String content;
    private Boolean hasAll;
    private Boolean hasBlind;
    private Boolean hasQuestion;
    private List<CategoryDto> children;

    public static List<CategoryDto> toDtoList(List<Category> categories) {
        CategoryHelper helper = CategoryHelper.newInstance(
                categories,
                c -> new CategoryDto(c.getId(), c.getName(), c.getContent(), c.getHasAll(),
                        c.getHasBlind(), c.getHasQuestion(), new ArrayList<>()),
                Category::getParent,
                Category::getId,
                CategoryDto::getChildren);
        return helper.convert();
    }
}