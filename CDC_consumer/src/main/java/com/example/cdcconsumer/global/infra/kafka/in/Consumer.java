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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Consumer {
    private final InteractionRepository interactionRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EmojiRepository emojiRepository;
    private final PostRepository postRepository;
    private final RoleRepository roleRepository;

    @KafkaListener(topics = "payload_log", groupId = "payload_group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(NewPayloadData newPayloadData) {
        String operation = newPayloadData.getOperation();
        String tableName = newPayloadData.getTableName();
        String logInfo = newPayloadData.getLog();

        log.info(">>> 이벤트 수신 ...");
        log.info(">>> TABLE_NAME : "+tableName);
        log.info(">>> OPERATION : "+operation);

        try {
            if(tableName.equals("COMMENTS")){
                consumeComment(operation, logInfo);
            }else if(tableName.equals("EMOJI")){
                consumeEmoji(operation,logInfo);
            }else if(tableName.equals("INTERACTION")){
                consumeInteraction(operation,logInfo);
            }else if(tableName.equals("POST")){
                consumePost(operation,logInfo);
            }else if(tableName.equals("ROLE")){
                consumeRole(operation,logInfo);
            }else if(tableName.equals("USER")){
                consumeUser(operation,logInfo);
            }

        } catch (Exception e) {
            System.out.println("예외 발생");
            e.printStackTrace();
        }

    }

    public void consumeComment(String operation, String log) {
        if (operation.equals("DELETE")) {
            commentRepository.deleteById(Long.parseLong(log));
        } else {
            String[] logs = log.split(",");
            Comment comment = new Comment(logs);
            commentRepository.save(comment);
        }
    }

    public void consumeEmoji(String operation, String log) {
        if (operation.equals("DELETE")) {
            emojiRepository.deleteById(Long.parseLong(log));
        } else {
            String[] logs = log.split(",");
            Emoji emoji = new Emoji(logs);
            emojiRepository.save(emoji);
        }
    }

    public void consumeInteraction(String operation, String log) {
        if (operation.equals("DELETE")) {
            interactionRepository.deleteById(Long.parseLong(log));
        } else {
            String[] logs = log.split(",");
            Interaction interaction = new Interaction(logs);
            interactionRepository.save(interaction);
        }
    }

    public void consumePost(String operation, String log) {
        if (operation.equals("DELETE")) {
            postRepository.deleteById(Long.parseLong(log));
        } else {
            String[] logs = log.split(",");
            Post post = new Post(logs);
            postRepository.save(post);
        }
    }

    public void consumeRole(String operation, String log) {
        if (operation.equals("DELETE")) {
            roleRepository.deleteById(Long.parseLong(log));
        } else {
            String[] logs = log.split(",");
            Role role = new Role(logs);
            roleRepository.save(role);
        }
    }

    public void consumeUser(String operation, String log) {
        if (operation.equals("DELETE")) {
            userRepository.deleteById(Long.parseLong(log));
        } else {
            String[] logs = log.split(",");
            User user = new User(logs);
            userRepository.save(user);
        }
    }
}