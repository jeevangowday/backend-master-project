package com.ecommerce.auth_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true)
    private String name;
}

