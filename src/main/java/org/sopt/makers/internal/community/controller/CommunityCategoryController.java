package org.sopt.makers.internal.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.dto.response.CommunityCategoryResponse;
import org.sopt.makers.internal.community.service.category.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/community/category")
@Tag(name = "커뮤니티 카테고리 관련 API", description = "커뮤니티 카테고리 관련 API List")
public class CommunityCategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "커뮤니티 카테고리 전체 조회 API")
    @GetMapping("")
    public ResponseEntity<List<CommunityCategoryResponse>> getAllCategories() {

        return ResponseEntity.status(HttpStatus.OK)
                .body(categoryService.getAllCategoriesWithChildren());
    }
}
