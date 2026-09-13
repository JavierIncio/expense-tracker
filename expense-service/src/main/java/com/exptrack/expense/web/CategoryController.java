package com.exptrack.expense.web;

import com.exptrack.expense.domain.TransactionType;
import com.exptrack.expense.dto.*;
import com.exptrack.expense.security.UserPrincipal;
import com.exptrack.expense.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {this.categoryService = categoryService;}

@Operation(summary = "Crea una categoría",
            description = "Crea una categoría etiquetada como ingreso o gasto para el usuario autenticado.",
            security = @SecurityRequirement(name = "userId"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@AuthenticationPrincipal UserPrincipal user,
                                                           @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.create(user.userId(), request));
    }

    @Operation(summary = "Lista categorías",
            description = "Devuelve las categorías del usuario, opcionalmente filtradas por tipo (INCOME o EXPENSE).",
            security = @SecurityRequirement(name = "userId"))
    @ApiResponse(responseCode = "200", description = "Lista de categorías")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> listCategories(@AuthenticationPrincipal UserPrincipal user,
                                                                 @RequestParam(required = false) TransactionType type) {
        return ResponseEntity.ok(categoryService.list(user.userId(), type));
    }

    @Operation(summary = "Obtiene una categoría",
            description = "Devuelve la categoría siempre que pertenezca al usuario autenticado.",
            security = @SecurityRequirement(name = "userId"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada para el usuario")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@AuthenticationPrincipal UserPrincipal user,
                                                        @PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.find(user.userId(), id));
    }

    @Operation(summary = "Actualiza una categoría",
            description = "Modifica el nombre y/o tipo de una categoría existente del usuario.",
            security = @SecurityRequirement(name = "userId"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada para el usuario")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@AuthenticationPrincipal UserPrincipal user,
                                                           @PathVariable UUID id,
                                                           @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(user.userId(), id, request));
    }

    @Operation(summary = "Elimina una categoría",
            description = "Elimina la categoría del usuario. No se puede eliminar si tiene transacciones o presupuestos asociados.",
            security = @SecurityRequirement(name = "userId"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoría eliminada"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada para el usuario"),
            @ApiResponse(responseCode = "409", description = "La categoría está en uso y no puede eliminarse")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@AuthenticationPrincipal UserPrincipal user,
                               @PathVariable UUID id) {
        categoryService.delete(user.userId(), id);
    }
}
