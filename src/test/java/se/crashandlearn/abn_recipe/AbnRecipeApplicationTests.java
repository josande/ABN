package se.crashandlearn.abn_recipe;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import se.crashandlearn.abn_recipe.controller.RecipeController;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
class AbnRecipeApplicationTests {


    @Autowired
    RecipeController recipeController;

    @Test
    void contextLoads() {
        assertNotNull(recipeController);
    }

}
