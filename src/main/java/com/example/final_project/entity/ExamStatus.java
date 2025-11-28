package com.example.final_project.entity;

/**
 * Defines the status of an online exam session.
 */
public enum ExamStatus {
    PENDING,     // The exam has been created but not yet started by the teacher.
    IN_PROGRESS, // The exam has been started and is open for students to take.
    FINISHED     // The exam has been closed and is no longer accepting submissions.
}
