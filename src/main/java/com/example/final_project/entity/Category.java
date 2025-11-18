package com.example.final_project.entity;

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

   @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
   private List<Question> questions = new ArrayList<>();



//    @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
//    private List<ExamOnline> examOnlines = new ArrayList<>();
//
//
//
//    @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
//    private List<ExamOffline> examOfflines = new ArrayList<>();
}
