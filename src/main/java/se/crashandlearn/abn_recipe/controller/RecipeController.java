package se.crashandlearn.abn_recipe.controller;

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

    @GetMapping("/recipes")
    CollectionModel<EntityModel<Recipe>> find(
            @RequestParam Optional<Boolean> vegetarian,
            @RequestParam Optional<Integer> servings,
            @RequestParam Optional<List<String>> includesIngredients,
            @RequestParam Optional<List<String>> excludesIngredient,
            @RequestParam Optional<List<String>> instructionKeyword
    ) {

        List<EntityModel<Recipe>> recipes = repository.findFiltered(vegetarian,
                                                                    servings,
                                                                    includesIngredients,
                                                                    excludesIngredient,
                                                                    instructionKeyword)
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(recipes, linkTo(methodOn(RecipeController.class).find(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withSelfRel());
    }
    @PostMapping("/recipes")
    ResponseEntity<?> newRecipe(@RequestBody Recipe newRecipe) {
     //   return repository.save(newRecipe);
        EntityModel<Recipe> entityModel = assembler.toModel(repository.save(newRecipe));

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @GetMapping("/recipes/{id}")
    EntityModel<Recipe> getRecipe(@PathVariable Long id) {

        Recipe recipe = repository.findById(id)
                .orElseThrow(() -> new RecipeNotFoundException(id));

        return assembler.toModel(recipe);

    }
    @PutMapping("/recipes/{id}")
    ResponseEntity<?>  updateRecipe(@RequestBody Recipe newRecipe, @PathVariable Long id) {

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
    @DeleteMapping("/recipes/{id}")
    void deleteRecipe(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
