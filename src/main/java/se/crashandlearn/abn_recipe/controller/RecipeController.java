package se.crashandlearn.abn_recipe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.crashandlearn.abn_recipe.exception.RecipeNotFoundException;
import se.crashandlearn.abn_recipe.model.Recipe;
import se.crashandlearn.abn_recipe.repository.RecipeRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class RecipeController {

    private final RecipeRepository repository;

    private final RecipeModelAssembler assembler;


    RecipeController(RecipeRepository recipeRepository, RecipeModelAssembler assembler) {
        this.repository =  recipeRepository;
        this.assembler = assembler;
    }

    @Operation(summary = "Returns all recipes that fits filters",
               description = """
    This methods supports 5 types of optional filters that can all be omitted individually.
     - vegetarian is a boolean filters dish strictly.
     - servings returns all dishes that provides at least this amount of servings
     - includesIngredients takes a list of strings that must all be present in the Ingredients section
     - excludesIngredient takes a list of strings that must all be missing from the Ingredients section
     - instructionKeywords takes a list of strings that must be present in the Instruction part of the recipe.
     """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the recipe",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Recipe.class)) })
                })
    @GetMapping("/recipes")
    CollectionModel<EntityModel<Recipe>> find(
            @RequestParam Optional<Boolean> vegetarian,
            @RequestParam Optional<Integer> servings,
            @RequestParam Optional<List<String>> includesIngredients,
            @RequestParam Optional<List<String>> excludesIngredients,
            @RequestParam Optional<List<String>> instructionKeywords
    ) {

        List<EntityModel<Recipe>> recipes = repository.findFiltered(vegetarian,
                                                                    servings,
                                                                    includesIngredients,
                                                                    excludesIngredients,
                                                                    instructionKeywords)
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(recipes, linkTo(methodOn(RecipeController.class)
                    .find(Optional.empty(),
                          Optional.empty(),
                          Optional.empty(),
                          Optional.empty(),
                          Optional.empty()))
                    .withSelfRel());
    }
    @PostMapping("/recipes")
    ResponseEntity<?> newRecipe(@RequestBody Recipe newRecipe) {

        EntityModel<Recipe> entityModel = assembler.toModel(repository.save(newRecipe));

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @Operation(summary = "Get a recipe by its id",
               description = "Returns a specific recipe, or an error message if the recipe was not found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the recipe",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Recipe.class)) }),
            @ApiResponse(responseCode = "404", description = "Recipe not found",
                    content = @Content) })
    @GetMapping("/recipes/{id}")
    EntityModel<Recipe> getRecipeById(@PathVariable Long id) {

        Recipe recipe = repository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));

        return assembler.toModel(recipe);

    }
    @Operation(summary = "Updates a recipe by its id",
               description = "Updates recipe with given ID, or creates new recipe if id not found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Recipe updated or created",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Recipe.class)) })
                    })
    // It might be more accurate to return 200 OK here, not sure.
    // 201 returns Location headers correctly out of the box.
    @PutMapping("/recipes/{id}")
    ResponseEntity<?> updateRecipe(@RequestBody Recipe newRecipe, @PathVariable Long id) {

        Recipe updatedRecipe = repository.findById(id)
                .map(recipe -> {
                    recipe.setTitle(newRecipe.getTitle());
                    recipe.setVegetarian(newRecipe.isVegetarian());
                    recipe.setServings(newRecipe.getServings());
                    recipe.setIngredients(newRecipe.getIngredients());
                    recipe.setInstruction(newRecipe.getInstruction());
                    recipe.setTitle(newRecipe.getTitle());
                    return repository.save(recipe);
                })
                .orElseGet(() -> repository.save(newRecipe));

        EntityModel<Recipe> entityModel = assembler.toModel(updatedRecipe);

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .body(entityModel);

    }
    @Operation(summary = "Delete a recipe by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recipe removed if found")})
    @DeleteMapping("/recipes/{id}")
    void deleteRecipe(@PathVariable Long id) {

        repository.deleteById(id);
    }
}
