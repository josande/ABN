package se.crashandlearn.abn_recipe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="recipe")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private boolean vegetarian;

    private int servings;

    @ElementCollection
    @CollectionTable(name="ingredient", joinColumns=@JoinColumn(name="recipe_id"))
    @Column(name="ingredient")
    private Set<String> ingredients =  new HashSet<>();

    @Lob
    private String instruction;

}
