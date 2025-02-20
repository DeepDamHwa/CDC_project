package com.example.cdcconsumer.global.infra.kafka.in;

import com.example.cdcconsumer.domain.comment.Comment;
import com.example.cdcconsumer.domain.comment.NewCommentsPayloadData;
import com.example.cdcconsumer.domain.comment.repository.CommentRepository;
import com.example.cdcconsumer.domain.emoji.Emoji;
import com.example.cdcconsumer.domain.emoji.NewEmojiPayloadData;
import com.example.cdcconsumer.domain.emoji.repository.EmojiRepository;
import com.example.cdcconsumer.domain.interaction.model.Interaction;
import com.example.cdcconsumer.domain.interaction.model.NewInteractionPayloadData;
import com.example.cdcconsumer.domain.interaction.repository.InteractionRepository;
import com.example.cdcconsumer.domain.post.NewPostPayloadData;
import com.example.cdcconsumer.domain.post.Post;
import com.example.cdcconsumer.domain.post.repository.PostRepository;
import com.example.cdcconsumer.domain.role.NewRolePayloadData;
import com.example.cdcconsumer.domain.role.Role;
import com.example.cdcconsumer.domain.role.repository.RoleRepository;
import com.example.cdcconsumer.domain.user.NewUsersPayloadData;
import com.example.cdcconsumer.domain.user.User;
import com.example.cdcconsumer.domain.user.repository.UserRepository;
import com.example.cdcconsumer.global.infra.kafka.out.DataProducer;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class Consumer {
    private final InteractionRepository interactionRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EmojiRepository emojiRepository;
    private final PostRepository postRepository;
    private final RoleRepository roleRepository;
    private final DataProducer dataProducer;

    @KafkaListener(topics = "comment_payload_log", groupId = "comment_payload_group", containerFactory = "commentKafkaListenerContainerFactory")
    public void consumeComment(NewCommentsPayloadData newCommentsPayloadData) {
        System.out.println("이벤트 수신...");
        String operation = newCommentsPayloadData.getOperation();
        try {
            System.out.println("Comment");
            System.out.println(operation);
            System.out.println(newCommentsPayloadData.getCommentsIdx());
            if (operation.equals("DELETE")) {
//                Comment comment = commentRepository.findById(newCommentsPayloadData.getCommentsIdx()).orElseThrow();
                commentRepository.deleteById(newCommentsPayloadData.getCommentsIdx());
            } else {
                Comment comment = Comment.builder()
                        .idx(newCommentsPayloadData.getCommentsIdx())
                        .post(Post.builder().idx(newCommentsPayloadData.getPostIdx()).build())
                        .user(User.builder().idx(newCommentsPayloadData.getUserIdx()).build())
                        .parent(Comment.builder().idx(newCommentsPayloadData.getParentIdx()).build())
                        .content(newCommentsPayloadData.getContent())
                        .build();
                commentRepository.save(comment);
            }
            log.info("comment 처리 완료... "+String.valueOf(LocalDateTime.now()));
        } catch (Exception e) {
            System.out.println("예외 발생");
            e.printStackTrace();
            //실패시 Dead Letter에 메시지 발행
            dataProducer.sendCommentDeadLetter(newCommentsPayloadData);
            log.info("comment 처리 실패... "+String.valueOf(LocalDateTime.now()));
        }

    }

    @KafkaListener(topics = "user_payload_log", groupId = "user_payload_group", containerFactory = "userKafkaListenerContainerFactory")
    public void consumeUser(NewUsersPayloadData newUsersPayloadData) {
        System.out.println("이벤트 수신...");
        String operation = newUsersPayloadData.getOperation();
        try {
            System.out.println("User");
            System.out.println(operation);
            System.out.println(newUsersPayloadData.getUserIdx());
            if (operation.equals("DELETE")) {
//                User user = userRepository.findById(newUsersPayloadData.getUserIdx()).orElseThrow();
                userRepository.deleteById(newUsersPayloadData.getUserIdx());
            } else {
                User user = User.builder()
                        .idx(newUsersPayloadData.getUserIdx())
                        .role(Role.builder().idx(newUsersPayloadData.getRoleIdx()).build())
                        .name(newUsersPayloadData.getName())
                        .build();
                userRepository.save(user);
            }
            log.info("user 처리 완료... "+String.valueOf(LocalDateTime.now()));
        } catch (Exception e) {
            System.out.println("예외 발생");
            e.printStackTrace();
            dataProducer.sendUserDeadLetter(newUsersPayloadData);
            log.info("user 처리 실패... "+String.valueOf(LocalDateTime.now()));
        }
    }

    @KafkaListener(topics = "emoji_payload_log", groupId = "emoji_payload_group", containerFactory = "emojiKafkaListenerContainerFactory")
    public void consumeEmoji(NewEmojiPayloadData newEmojiPayloadData) {
        System.out.println("이벤트 수신...");
        String operation = newEmojiPayloadData.getOperation();

        try {
            System.out.println("Emoji");
            System.out.println(operation);
            System.out.println(newEmojiPayloadData.getEmojiIdx());
            if (operation.equals("DELETE")) {
                emojiRepository.deleteById(newEmojiPayloadData.getEmojiIdx());
            } else {
                Emoji emoji = Emoji.builder()
                    .idx(newEmojiPayloadData.getEmojiIdx())
                    .name(newEmojiPayloadData.getName())
                    .build();
                emojiRepository.save(emoji);
            }
            log.info("emoji 처리 완료... "+String.valueOf(LocalDateTime.now()));
        } catch (Exception e) {
            System.out.println("예외 발생");
            e.printStackTrace();
            dataProducer.sendEmojiDeadLetter(newEmojiPayloadData);
            log.info("emoji 처리 실패... "+String.valueOf(LocalDateTime.now()));
        }
    }

    @KafkaListener(topics = "role_payload_log", groupId = "role_payload_group", containerFactory = "roleKafkaListenerContainerFactory")
    public void consumeRole(NewRolePayloadData newRolePayloadData) {
        System.out.println("이벤트 수신...");
        String operation = newRolePayloadData.getOperation();
        try {
            System.out.println("Role");
            System.out.println(operation);
            System.out.println(newRolePayloadData.getRoleIdx());
            if (operation.equals("DELETE")) {
//                Role role = roleRepository.findById(newRolePayloadData.getRoleIdx()).orElseThrow();
                roleRepository.deleteById(newRolePayloadData.getRoleIdx());
            } else {
                Role role = Role.builder().
                        idx(newRolePayloadData.getRoleIdx())
                        .name(newRolePayloadData.getName())
                        .build();
                roleRepository.save(role);
            }
            log.info("role 처리 완료... "+String.valueOf(LocalDateTime.now()));
        } catch (Exception e) {
            System.out.println("예외 발생");
            e.printStackTrace();
            dataProducer.sendRoleDeadLetter(newRolePayloadData);
            log.info("role 처리 실패... "+String.valueOf(LocalDateTime.now()));
        }
    }

    @KafkaListener(topics = "interaction_payload_log", groupId = "interaction_payload_group", containerFactory = "interactionKafkaListenerContainerFactory")
    public void consumeInteraction(NewInteractionPayloadData newInteractionPayloadData) {
        System.out.println("이벤트 수신...");
        String operation = newInteractionPayloadData.getOperation();

        try {
            System.out.println("Interation");
            System.out.println(operation);
            System.out.println(newInteractionPayloadData.getInteractionIdx());
            if (operation.equals("DELETE")) {
//                Interaction interaction = interactionRepository.findById(newInteractionPayloadData.getInteractionIdx()).orElseThrow();
                interactionRepository.deleteById(newInteractionPayloadData.getInteractionIdx());
            } else {
                Interaction interaction = Interaction.builder()
                        .idx(newInteractionPayloadData.getInteractionIdx())
                        .comment(Comment.builder().idx(newInteractionPayloadData.getCommentIdx()).build())
                        .user(User.builder().idx(newInteractionPayloadData.getUserIdx()).build())
                        .emoji(Emoji.builder().idx(newInteractionPayloadData.getEmojiIdx()).build()).build();
                interactionRepository.save(interaction);
            }
            log.info("interaction 처리 완료... "+String.valueOf(LocalDateTime.now()));
        } catch (Exception e) {
            System.out.println("예외 발생");
            e.printStackTrace();
            dataProducer.sendInteractionDeadLetter(newInteractionPayloadData);
            log.info("interaction 처리 실패... "+String.valueOf(LocalDateTime.now()));
        }
    }

    @KafkaListener(topics = "post_payload_log", groupId = "post_payload_group", containerFactory = "postKafkaListenerContainerFactory")
    public void consumePost(NewPostPayloadData newPostPayloadData) {
        System.out.println("이벤트 수신...");
        String operation = newPostPayloadData.getOperation();

        try {
            System.out.println("Post");
            System.out.println(operation);
            System.out.println(newPostPayloadData.getPostIdx());
            if (operation.equals("DELETE")) {
//                Post post = postRepository.findById(newPostPayloadData.getPostIdx()).orElseThrow();
                postRepository.deleteById(newPostPayloadData.getPostIdx());
            } else {
                Post post = Post.builder()
                        .idx(newPostPayloadData.getPostIdx())
                        .user(User.builder().idx(newPostPayloadData.getUserIdx()).build())
                        .build();
                postRepository.save(post);
            }
            log.info("post 처리 완료... "+String.valueOf(LocalDateTime.now()));
        } catch (Exception e) {
            System.out.println("예외 발생");
            e.printStackTrace();
            dataProducer.sendPostDeadLetter(newPostPayloadData);
            log.info("post 처리 실패... "+String.valueOf(LocalDateTime.now()));
        }
    }
}