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

import java.util.Collections;
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
    private RecipeRepository recipeRepository;
    @Spy
    private RecipeModelAssembler assembler;

    private JacksonTester<Recipe> jsonRecipe;

    @InjectMocks
    private RecipeController controller;

    private MockMvc mvc;

    private final Recipe pumpkinPie = Recipe.builder().title("Pumpkin pie").vegetarian(true).servings(2).ingredients(new HashSet<>(List.of("Flour", "Pumpkin"))).instruction("* Knead Flour and Water to make Dough.\n* Knead Dough to make Raw Pie Crust\n* Remove seeds from pumpkin\n* Chop pumpkin into pieces and add\n* Cook and serve!").build();
    private final Recipe pumpkinPieWithId = Recipe.builder().id(3L).title("Pumpkin pie").vegetarian(true).servings(2).ingredients(new HashSet<>(List.of("Flour", "Pumpkin"))).instruction("* Knead Flour and Water to make Dough.\n* Knead Dough to make Raw Pie Crust\n* Remove seeds from pumpkin\n* Chop pumpkin into pieces\n* Cook and serve!").build();
    private final Recipe applePie = Recipe.builder().title("Apple pie").vegetarian(true).servings(2).ingredients(new HashSet<>(List.of("Flour", "Apple"))).instruction("* Knead Flour and Water to make Dough.\n* Knead Dough to make Raw Pie Crust\n* Chop applies into pieces and add\n* Cook and serve!").build();
    private final Recipe applePieWithId = Recipe.builder().id(3L).title("Apple pie").vegetarian(true).servings(2).ingredients(new HashSet<>(List.of("Flour", "Apple"))).instruction("* Knead Flour and Water to make Dough.\n* Knead Dough to make Raw Pie Crust\n* Chop applies into pieces and add\n* Cook and serve!").build();

    @BeforeEach
    public void setup() {
        JacksonTester.initFields(this, new ObjectMapper());

        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new RecipeControllerAdvice())
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

        System.out.println(response.getContentAsString());
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
    @Test
    public void givenRecipeExists_whenPut_thenUpdateRecipe() throws Exception {
        //given
        given(recipeRepository.findById(3L))
                .willReturn(Optional.of(pumpkinPieWithId));

        //when
        Mockito.when(recipeRepository.save(applePieWithId)).thenReturn(applePieWithId);

        MockHttpServletResponse response = mvc.perform(
                        put("/recipes/3")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRecipe.write(applePie).getJson()))
                .andReturn().getResponse();

        //then
        assertTrue(response.getContentAsString().contains("Apple pie"));

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
    }
    @Test
    public void givenRecipeMissing_whenPut_thenCreateRecipe() throws Exception {
        //given
        given(recipeRepository.findById(10L))
                .willReturn(Optional.empty());

        //when
        Mockito.when(recipeRepository.save(applePie)).thenReturn(applePie);

        MockHttpServletResponse response = mvc.perform(
                        put("/recipes/10")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRecipe.write(applePie).getJson()))
                .andReturn().getResponse();

        //then
        assertTrue(response.getContentAsString().contains("Apple pie"));
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
    }
    @Test
    public void whenDelete_thenReturnOK() throws Exception {

        MockHttpServletResponse response = mvc.perform(
                        delete("/recipes/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRecipe.write(pumpkinPie).getJson()))
                .andReturn().getResponse();

        //then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }


    @Test
    public void whenPassingVegetarianFilterArguments_thenParametersAreSentToQuery() throws Exception {
        // given
        given(recipeRepository.findFiltered(Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
                .willReturn(Collections.emptyList());

        // when
        MockHttpServletResponse response = mvc.perform(
                        get("/recipes?vegetarian=true")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(response.getContentAsString().contains("\"href\":\"http://localhost/recipes?vegetarian=true"));
    }
    @Test
    public void whenPassingServingsFilterArguments_thenParametersAreSentToQuery() throws Exception {
        // given
        given(recipeRepository.findFiltered(Optional.empty(), Optional.of(2), Optional.empty(), Optional.empty(), Optional.empty()))
                .willReturn(Collections.emptyList());

        // when
        MockHttpServletResponse response = mvc.perform(
                        get("/recipes?servings=2")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(response.getContentAsString().contains("\"href\":\"http://localhost/recipes?servings=2"));
    }

    @Test
    public void whenPassingIncludesIngredientFilterArguments_thenParametersAreSentToQuery() throws Exception {
        // given
        given(recipeRepository.findFiltered(Optional.empty(), Optional.empty(), Optional.of(List.of("carrot", "onion")), Optional.empty(), Optional.empty()))
                .willReturn(Collections.emptyList());

        // when
        MockHttpServletResponse response = mvc.perform(
                        get("/recipes?includesIngredients=carrot,onion")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(response.getContentAsString().contains("\"href\":\"http://localhost/recipes?includesIngredients=carrot&includesIngredients=onion"));
    }

    @Test
    public void whenPassingExcludesIngredientFilterArguments_thenParametersAreSentToQuery() throws Exception {
        // given
        given(recipeRepository.findFiltered(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(List.of("carrot", "onion")), Optional.empty()))
                .willReturn(Collections.emptyList());

        // when
        MockHttpServletResponse response = mvc.perform(
                        get("/recipes?excludesIngredients=carrot,onion")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(response.getContentAsString().contains("\"href\":\"http://localhost/recipes?excludesIngredients=carrot&excludesIngredients=onion"));
    }

    @Test
    public void whenPassingInstructionKeywordsFilterArguments_thenParametersAreSentToQuery() throws Exception {
        // given
        given(recipeRepository.findFiltered(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(List.of("oven", "chop"))))
                .willReturn(Collections.emptyList());

        // when
        MockHttpServletResponse response = mvc.perform(
                        get("/recipes?instructionKeywords=oven,chop")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(response.getContentAsString().contains("\"href\":\"http://localhost/recipes?instructionKeywords=oven&instructionKeywords=chop"));
    }

    @Test
    public void whenPassingMultipleFilterArguments_thenParametersAreSentToQuery() throws Exception {
        // given
        given(recipeRepository.findFiltered(Optional.of(true), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(List.of("oven", "chop"))))
                .willReturn(Collections.emptyList());

        // when
        MockHttpServletResponse response = mvc.perform(
                        get("/recipes?vegetarian=true&instructionKeywords=oven,chop")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // then
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(response.getContentAsString().contains("\"href\":\"http://localhost/recipes?vegetarian=true&instructionKeywords=oven&instructionKeywords=chop"));
    }
}
