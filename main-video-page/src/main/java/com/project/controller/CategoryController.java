package com.project.controller;

import com.project.common.Category;
import com.project.dto.CategoryDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class CategoryController {

    @Operation(summary = "Справочник категорий", description = "Коды для фильтров, загрузки видео и курсов, специализации мастера")
    @GetMapping("/categories")
    public List<CategoryDto> categories() {
        return Arrays.stream(Category.values()).map(CategoryDto::from).toList();
    }
}
