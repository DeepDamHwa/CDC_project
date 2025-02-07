package com.example.cdcconsumer.domain.role;

import com.example.cdcconsumer.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private String name;

    @OneToMany(mappedBy = "role")
    private List<User> users = new ArrayList<>();
}
