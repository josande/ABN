package se.crashandlearn.abn_recipe.controller;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import se.crashandlearn.abn_recipe.model.Recipe;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Component
public
class RecipeModelAssembler implements RepresentationModelAssembler<Recipe, EntityModel<Recipe>> {

    @Override
    public EntityModel<Recipe> toModel(Recipe recipe) {

        return EntityModel.of(recipe, //
                linkTo(methodOn(RecipeController.class).getRecipe(recipe.getId())).withSelfRel(),
                linkTo(methodOn(RecipeController.class).find(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())).withRel("recipes"));
    }
}
