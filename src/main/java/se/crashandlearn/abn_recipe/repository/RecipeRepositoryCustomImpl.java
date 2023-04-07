package se.crashandlearn.abn_recipe.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import se.crashandlearn.abn_recipe.model.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class RecipeRepositoryCustomImpl implements RecipeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Recipe> findFiltered(Optional<Boolean> vegetarian,
                                     Optional<Integer> servings,
                                     Optional<List<String>> includeIngredients,
                                     Optional<List<String>> excludeIngredient,
                                     Optional<List<String>> instructionKeyword) {


        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Recipe> query = cb.createQuery(Recipe.class);
        Root<Recipe> recipe = query.from(Recipe.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.greaterThanOrEqualTo(recipe.get("servings"), servings.orElse(0)));

        vegetarian.ifPresent(veg -> predicates.add(cb.equal(recipe.get("vegetarian"), veg)));

        instructionKeyword.ifPresent(keywords -> keywords
                .forEach(keyword -> predicates.add(cb.like(recipe.get("instruction"), "%"+keyword+"%"))));

        includeIngredients.ifPresent(_ingredients -> _ingredients
                .forEach(ingredient -> predicates.add(cb.isMember(ingredient, recipe.<Set<String>>get("ingredients")))));

        excludeIngredient.ifPresent(_ingredients -> _ingredients
                .forEach(ingredient -> predicates.add(cb.isNotMember(ingredient, recipe.<Set<String>>get("ingredients")))));

        query.select(recipe).where(cb.and(predicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }
}
