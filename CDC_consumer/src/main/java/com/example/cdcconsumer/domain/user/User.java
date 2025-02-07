package com.example.cdcconsumer.domain.user;

import com.example.cdcconsumer.domain.comment.Comment;
import com.example.cdcconsumer.domain.interaction.model.Interaction;
import com.example.cdcconsumer.domain.post.Post;
import com.example.cdcconsumer.domain.role.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private String name;

    @OneToMany(mappedBy = "user")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Interaction> interactions = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "role_idx")
    private Role role;
}
