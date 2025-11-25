package com.example.final_project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "categories")
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private String createdBy; // Stores the email of the creator (Admin or Teacher)

    @Transient
    private String createdByRole; // transient field for UI: "admin" | "teacher"

    @Transient
    private String createdByName;

   @JsonIgnore
   @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
   private List<Question> questions = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<Exam> exam = new ArrayList<>();


//    @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
//    private List<ExamOnline> examOnlines = new ArrayList<>();
}
