package com.example.final_project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "categories")
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

//    @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
//    private List<Question> questions = new ArrayList<>();

    // Per backlog: 1-n relationship with ExamOnline. Uncomment when ExamOnline entity is created.
    /*
    @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<ExamOnline> examOnlines = new ArrayList<>();
    */

    // Per backlog: 1-n relationship with ExamOffline. Uncomment when ExamOffline entity is created.
    /*
    @OneToMany(mappedBy = "category", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<ExamOffline> examOfflines = new ArrayList<>();
    */
}