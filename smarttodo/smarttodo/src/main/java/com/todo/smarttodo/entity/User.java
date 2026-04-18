package com.todo.smarttodo.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
    //The @Enumerated annotation in Spring Boot is a JPA feature that maps Java enum types to database columns.
    // It ensures proper storage and retrieval of enum values in relational databases.
    //EnumType.ORDINAL: Stores the enum values as their ordinal (integer) positions in the enum declaration.
    //EnumType.STRING: Stores the enum values as their string names (e.g., OPEN, CLOSED).
    //Using EnumType.STRING is recommended for better readability and to avoid issues when the order of enum constants changes.
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

}

