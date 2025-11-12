package com.finance.manager.service;

import com.finance.manager.dto.category.CategoryResponse;
import com.finance.manager.dto.category.CreateCategoryRequest;
import com.finance.manager.dto.category.UpdateCategoryRequest;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.User;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.exception.UnauthorizedException;
import com.finance.manager.exception.ValidationException;
import com.finance.manager.repository.CategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public List<CategoryResponse> getAllCategories() {
        UUID userId = userService.getCurrentUserId();
        return categoryRepository.findAllAvailableForUser(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        User user = userService.getCurrentUser();

        Category category = Category.builder()
                .user(user)
                .name(request.getName())
                .type(request.getType())
                .icon(request.getIcon())
                .color(request.getColor())
                .isSystem(false)
                .build();

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        UUID userId = userService.getCurrentUserId();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (category.getIsSystem()) {
            throw new ValidationException("Cannot update system category");
        }

        if (category.getUser() == null || !category.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own categories");
        }

        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());

        category = categoryRepository.save(category);
        return mapToResponse(category);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        UUID userId = userService.getCurrentUserId();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (category.getIsSystem()) {
            throw new ValidationException("Cannot delete system category");
        }

        if (category.getUser() == null || !category.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own categories");
        }

        if (transactionRepository.existsActiveByCategoryId(id)) {
            throw new ValidationException("Cannot delete category with existing transactions");
        }

        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .icon(category.getIcon())
                .color(category.getColor())
                .isSystem(category.getIsSystem())
                .createdAt(category.getCreatedAt())
                .build();
    }

}
