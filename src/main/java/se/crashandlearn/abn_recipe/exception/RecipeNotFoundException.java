package se.crashandlearn.abn_recipe.exception;

public class RecipeNotFoundException extends RuntimeException {
    public RecipeNotFoundException(Long id) {
        super("Could not find Recipe " + id);
    }
}
