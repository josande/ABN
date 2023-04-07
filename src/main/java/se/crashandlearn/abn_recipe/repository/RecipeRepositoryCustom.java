package se.crashandlearn.abn_recipe.repository;

import org.springframework.stereotype.Repository;
import se.crashandlearn.abn_recipe.model.Recipe;

import java.util.List;
import java.util.Optional;


@Repository
public interface RecipeRepositoryCustom {
    List<Recipe> findFiltered(Optional<Boolean> vegetarian,
                              Optional<Integer> servings,
                              Optional<List<String>> containsIngredient,
                              Optional<List<String>> notContainsIngredient,
                              Optional<List<String>> instructionKeyword);
}
