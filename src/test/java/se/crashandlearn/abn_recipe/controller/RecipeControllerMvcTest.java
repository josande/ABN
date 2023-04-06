package se.crashandlearn.abn_recipe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.crashandlearn.abn_recipe.exception.RecipeControllerAdvice;
import se.crashandlearn.abn_recipe.model.Recipe;
import se.crashandlearn.abn_recipe.repository.RecipeRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

//@AutoConfigureJsonTesters
@ExtendWith(MockitoExtension.class)
public class RecipeControllerMvcTest {
    @Mock
    RecipeRepository recipeRepository;
    @Spy
    RecipeModelAssembler assembler;

    private JacksonTester<Recipe> jsonRecipe;

    @InjectMocks
    RecipeController controller;

    private MockMvc mvc;

    private final Recipe pumpkinPie = Recipe.builder().title("Pumpkin pie").vegetarian(true).servings(2).ingredients(new HashSet<>(List.of("Flour", "Pumpkin"))).instruction("* Knead Flour and Water to make Dough.\n* Knead Dough to make Raw Pie Crust\n* Remove seeds from pumpkin\n* Chop pumpkin into pieces\n* Cook and serve!").build();
    private final Recipe pumpkinPieWithId = Recipe.builder().id(3L).title("Pumpkin pie").vegetarian(true).servings(2).ingredients(new HashSet<>(List.of("Flour", "Pumpkin"))).instruction("* Knead Flour and Water to make Dough.\n* Knead Dough to make Raw Pie Crust\n* Remove seeds from pumpkin\n* Chop pumpkin into pieces\n* Cook and serve!").build();

    @BeforeEach
    public void setup() {
        JacksonTester.initFields(this, new ObjectMapper());

        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new RecipeControllerAdvice())
               // .addFilters(new RecipeFilter())
                .build();
    }

    @Test
    public void givenRecipeExists_whenGet_thenReturnObject() throws Exception {
        // given
        given(recipeRepository.findById(3L))
                .willReturn(Optional.of(pumpkinPieWithId));

        // when
        MockHttpServletResponse response = mvc.perform(
                        get("/recipes/3")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(response.getContentAsString().contains(pumpkinPieWithId.getTitle()));
    }
    @Test
    public void givenRecipeExists_whenGet_thenReturnLinks() throws Exception {
        // given
        given(recipeRepository.findById(3L))
                .willReturn(Optional.of(pumpkinPieWithId));

        // when
        MockHttpServletResponse response = mvc.perform(
                        get("/recipes/3")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(response.getContentAsString().contains("\"href\":\"http://localhost/recipes/3\""));
        assertTrue(response.getContentAsString().contains("\"href\":\"http://localhost/recipes\""));
    }
    @Test
    public void givenRecipeMissing_whenGet_thenReturnObjectNotFound() throws Exception {
        // given
        given(recipeRepository.findById(10L))
                .willReturn(Optional.empty());

        // when
        MockHttpServletResponse response = mvc.perform(
                        get("/recipes/10")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

        assertEquals("Could not find Recipe 10", response.getContentAsString());
    }

    @Test
    public void canSaveRecipe() throws Exception {
        //when
        Mockito.when(recipeRepository.save(pumpkinPie)).thenReturn(pumpkinPieWithId);
        MockHttpServletResponse response = mvc.perform(
                                post("/recipes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRecipe.write(pumpkinPie).getJson()))
                .andReturn().getResponse();

        //then
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
    }


}
