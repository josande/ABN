package se.crashandlearn.abn_recipe.controller;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.transaction.annotation.Transactional;
import se.crashandlearn.abn_recipe.exception.RecipeNotFoundException;
import se.crashandlearn.abn_recipe.model.Recipe;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class RecipeControllerDatabaseTest {
    @Resource
    RecipeController controller;

    private final Recipe smallVeggiePie = Recipe.builder().title("Small veggie pie").vegetarian(true).servings(1).ingredients(new HashSet<>(List.of("Flour", "Carrot", "Broccoli"))).instruction("* Knead Flour and Water to make Dough.\n* Knead Dough to make Raw Pie Crust\n* Add chopped Carrot and Broccoli\n * Cook and serve!").build();
    private final Recipe largeVeggiePie = Recipe.builder().title("Large veggie pie").vegetarian(true).servings(4).ingredients(new HashSet<>(List.of("Flour", "Carrot", "Broccoli"))).instruction("* Knead Flour and Water to make Dough.\n* Knead Dough to make Raw Pie Crust\n* Add chopped Carrot and Broccoli\n * Cook and serve!").build();
    private final Recipe smallMeatPie = Recipe.builder().title("Small meat pie").vegetarian(false).servings(1).ingredients(new HashSet<>(List.of("Flour", "Meat"))).instruction("* Knead Flour and Water to make Dough.\n* Knead Dough to make Raw Pie Crust\n* Add chopped Meat\n * Cook and serve!").build();
    private final Recipe largeMeatPie = Recipe.builder().title("Large meat pie").vegetarian(false).servings(4).ingredients(new HashSet<>(List.of("Flour", "Meat"))).instruction("* Knead Flour and Water to make Dough.\n* Knead Dough to make Raw Pie Crust\n* Add chopped Meat\n * Cook and serve!").build();
    private final Recipe mushroomPie = Recipe.builder().title("Mushroom pie").vegetarian(true).servings(2).ingredients(new HashSet<>(List.of("Flour", "Mushroom"))).instruction("* Knead Flour and Water to make Dough.\n* Knead Dough to make Raw Pie Crust\n* Add chopped mushrooms\n * Cook and serve!").build();

    @Test
    void givenRecipe_whenSave_thenCanFind() {
        assertEquals(0, controller.find(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()).getContent().size());
        controller.newRecipe(largeVeggiePie);
        assertEquals(1, controller.find(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()).getContent().size());
    }
    @Test
    void givenRecipe_whenSave_thenHaveId() {
        Recipe recipe = smallMeatPie;
        assertNull(recipe.getId());
        recipe = (Recipe) ((EntityModel<?>) controller.newRecipe(recipe).getBody()).getContent();
        assertNotNull(recipe.getId());
    }
    @Test
    void givenMultipleRecipes_whenFindAll_thenReturnsAll() {
        controller.newRecipe(smallVeggiePie);
        controller.newRecipe(smallMeatPie);
        controller.newRecipe(largeVeggiePie);
        controller.newRecipe(largeMeatPie);

        assertEquals(4, controller.find(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()).getContent().size());
    }
    @Test
    void givenRecipeExists_whenUpdateIsSent_thenRecipeIsUpdated() {
        Recipe recipe = smallMeatPie;
        recipe = (Recipe) ((EntityModel<?>) controller.newRecipe(recipe).getBody()).getContent();
        long id = recipe.getId();
        Recipe updatedRecipe = (Recipe) ((EntityModel<?>)controller.updateRecipe(largeVeggiePie, id).getBody()).getContent();
        assertEquals(id, recipe.getId());
        assertEquals(id, updatedRecipe.getId());
        assertEquals(4, updatedRecipe.getServings());
    }
    @Test
    void givenRecipeDoesNotExists_whenUpdateIsSent_thenRecipeIsCreated() {
        Recipe updatedRecipe = (Recipe) ((EntityModel<?>)controller.updateRecipe(largeVeggiePie, 123L).getBody()).getContent();
        assertNotNull(updatedRecipe.getId());
        assertEquals(4, updatedRecipe.getServings());
    }

    @Test
    void givenRecipeExists_whenGetRecipe_thenReturnSpecificRecipe() {
        controller.newRecipe(mushroomPie);
        Recipe pumpkinPieRecipe = (Recipe) ((EntityModel<?>) controller.newRecipe(mushroomPie).getBody()).getContent();
        controller.newRecipe(smallVeggiePie);

        Recipe result = controller.getRecipe(pumpkinPieRecipe.getId()).getContent();

        assertEquals(pumpkinPieRecipe, result);
    }
    @Test
    void givenRecipeMissing_whenGetRecipe_thenThrowRecipeNotFoundException() {
        try {
            controller.getRecipe(123L);
            fail();
        } catch (RecipeNotFoundException ex) {
            assertEquals("Could not find Recipe 123", ex.getMessage());
        }
    }
}