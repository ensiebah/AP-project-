package com.secondhand.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /**
     * A null parent means that this is a top-level category (for example,
     * "Digital"). A non-null parent means that this is a subcategory
     * (for example, "Mobile Phones" under "Digital").
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private Category parent;

    /**
     * This side is not returned as JSON. Lookup endpoints return CategoryDto,
     * so the bidirectional relationship can never cause a JSON recursion.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children = new ArrayList<>();

    /**
     * The text shown next to an advertisement, e.g. "Digital, Mobile Phones".
     * It also works if more than one hierarchy level is added later.
     */
    @Transient
    public String getFullPath() {
        List<String> names = new ArrayList<>();
        Category current = this;

        while (current != null) {
            names.add(current.getName());
            current = current.getParent();
        }

        Collections.reverse(names);
        return String.join(", ", names);
    }
}
