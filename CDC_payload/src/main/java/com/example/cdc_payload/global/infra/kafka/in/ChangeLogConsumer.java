package com.example.cdc_payload.global.infra.kafka.in;

import com.example.cdc_payload.domain.EventEntity;
import com.example.cdc_payload.domain.comment.model.Comments;
import com.example.cdc_payload.domain.comment.model.NewCommentsPayloadData;
import com.example.cdc_payload.domain.comment.repository.CommentsRepository;
import com.example.cdc_payload.domain.emoji.model.Emoji;
import com.example.cdc_payload.domain.emoji.model.NewEmojiPayloadData;
import com.example.cdc_payload.domain.emoji.repository.EmojiRepository;
import com.example.cdc_payload.domain.interaction.model.Interaction;
import com.example.cdc_payload.domain.interaction.model.NewInteractionPayloadData;
import com.example.cdc_payload.domain.interaction.repository.InteractionRepository;
import com.example.cdc_payload.domain.post.model.NewPostPayloadData;
import com.example.cdc_payload.domain.post.model.Post;
import com.example.cdc_payload.domain.post.repository.PostRepository;
import com.example.cdc_payload.domain.role.model.NewRolePayloadData;
import com.example.cdc_payload.domain.role.model.Role;
import com.example.cdc_payload.domain.role.repository.RoleRepository;
import com.example.cdc_payload.domain.user.model.NewUsersPayloadData;
import com.example.cdc_payload.domain.user.model.Users;
import com.example.cdc_payload.domain.user.repository.UserRepository;
import com.example.cdc_payload.global.infra.kafka.out.NewPayloadData;
import com.example.cdc_payload.global.infra.kafka.out.PayloadLogProducer;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangeLogConsumer {
    private final CommentsRepository commentsRepository;
    private final EmojiRepository emojiRepository;
    private final InteractionRepository interactionRepository;
    private final PostRepository postRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PayloadLogProducer payloadLogProducer;

    @KafkaListener(topics = "change_data_log", groupId = "change_log_group")
    public void newCaptureEvent(Map<String, Object> event){

//       {RS_ID=AAATNPAAHAAAALkAEy,
//        OPERATION=INSERT,
//        SEG_OWNER=C##DEEP,
//        TABLE_NAME=INTERACTION,
//        SQL_REDO=insert into "C##DEEP"."INTERACTION"("IDX","COMMENT_IDX","EMOJI_IDX","USER_IDX") values ('19835','5','484','3');}

//        {ROW_ID=AAATNPAAHAAABODACE,
//        RS_ID= 0x00002b.00050030.0048 ,
//        OPERATION=INSERT,
//        SEG_OWNER=C##DEEP,
//        TABLE_NAME=INTERACTION,
//        XIDUSN=10,
//        XIDSLT=30,
//        SQL_REDO=insert into "C##DEEP"."INTERACTION"("IDX","COMMENT_IDX","EMOJI_IDX","USER_IDX") values ('43489','75','185','5');}



        String tableName = event.get("TABLE_NAME").toString();
        String operation = event.get("OPERATION").toString();
        log.info(">>> 이벤트 수신 ...");
        log.info(">>> TABLE_NAME : "+tableName);
        log.info(">>> OPERATION : "+operation);

        if(tableName.equals("COMMENTS")){
            String topic = "comment_payload_log";
            if(operation.equals("DELETE")){
                Long idx = Long.parseLong(event.get("SQL_REDO").toString().split("'")[1]);
                payloadLogProducer.sendNewCommentsPayloadLogCaptureMessage(
                        NewCommentsPayloadData.builder()
                                .operation(operation)
                                .commentsIdx(idx)
                                .build(),
                        topic);
            }else{
                Comments comments = commentsRepository.findByRowId(event.get("ROW_ID").toString());
                payloadLogProducer.sendNewCommentsPayloadLogCaptureMessage(comments.toDto(operation), topic);
            }
//            Comments comments = commentsRepository.findByRowId( event.get("ROW_ID").toString());
//            payloadLogProducer.sendNewCommentsPayloadLogCaptureMessage(comments.toDto(event.get("OPERATION").toString()), "comment_payload_log");
        }else if(tableName.equals("EMOJI")){
            String topic = "emoji_payload_log";
            if(operation.equals("DELETE")){
                Long idx = Long.parseLong(event.get("SQL_REDO").toString().split("'")[1]);
                payloadLogProducer.sendNewEmojiPayloadLogCaptureMessage(
                        NewEmojiPayloadData.builder()
                                .operation(operation)
                                .emojiIdx(idx)
                                .build(),
                        topic);
            }else{
                Emoji emoji = emojiRepository.findByRowId(event.get("ROW_ID").toString());
                payloadLogProducer.sendNewEmojiPayloadLogCaptureMessage(emoji.toDto(operation), topic);
            }
//            Emoji emoji = emojiRepository.findByRowId( event.get("ROW_ID").toString());
//            payloadLogProducer.sendNewEmojiPayloadLogCaptureMessage(emoji.toDto(event.get("OPERATION").toString()), "emoji_payload_log");
        }else if(tableName.equals("INTERACTION")){
            String topic = "interaction_payload_log";
            if(operation.equals("DELETE")){
                Long idx = Long.parseLong(event.get("SQL_REDO").toString().split("'")[1]);
                payloadLogProducer.sendNewInteractionPayloadLogCaptureMessage(
                        NewInteractionPayloadData.builder()
                                .operation(event.get("OPERATION").toString())
                                .interactionIdx(idx)
                                .build(),
                        topic);
            }else{
                Interaction interaction = interactionRepository.findByRowId(event.get("ROW_ID").toString());
                payloadLogProducer.sendNewInteractionPayloadLogCaptureMessage(interaction.toDto(operation), topic);
            }
//            if(event.get("OPERATION").toString().equals("UPDATE")){
//                interaction = interactionRepository.findByRowId( event.get("ROW_ID").toString());
//                payloadLogProducer.sendNewInteractionPayloadLogCaptureMessage(interaction.toDto(event.get("OPERATION").toString()), "interaction_payload_log");
//            }else if(event.get("OPERATION").toString().equals("INSERT")){
//                interaction = interactionRepository.findById(Long.parseLong(event.get("SQL_REDO").toString().split("'")[1])).orElseThrow(()-> new IllegalArgumentException("dfdfdfdfdf"));
//                payloadLogProducer.sendNewInteractionPayloadLogCaptureMessage(interaction.toDto(event.get("OPERATION").toString()), "interaction_payload_log");
//            }else{
//                System.out.println(Long.parseLong(event.get("SQL_REDO").toString().split("'")[1]));
//                interaction = Interaction.builder().idx(Long.parseLong(event.get("SQL_REDO").toString().split("'")[1])).build();
//                payloadLogProducer.sendNewInteractionPayloadLogCaptureMessage(NewInteractionPayloadData.builder()
//                        .operation(event.get("OPERATION").toString()).interactionIdx(interaction.getIdx()).build(), "interaction_payload_log");
//            }
//            Interaction interaction = interactionRepository.findByRowId( event.get("ROW_ID").toString());
//            System.out.println(event.get("ROW_ID").toString());
//            payloadLogProducer.sendNewInteractionPayloadLogCaptureMessage(interaction.toDto(event.get("OPERATION").toString()), "interaction_payload_log");
        }else if(tableName.equals("POST")){
            String topic = "post_payload_log";
            if(operation.equals("DELETE")){
                Long idx = Long.parseLong(event.get("SQL_REDO").toString().split("'")[1]);
                payloadLogProducer.sendNewPostPayloadLogCaptureMessage(
                        NewPostPayloadData.builder()
                                .operation(operation)
                                .postIdx(idx)
                                .build(),
                        topic);
            }else{
                Post post = postRepository.findByRowId(event.get("ROW_ID").toString());
                payloadLogProducer.sendNewPostPayloadLogCaptureMessage(post.toDto(operation), topic);
            }
//            Post post = postRepository.findByRowId( event.get("ROW_ID").toString());
//            payloadLogProducer.sendNewPostPayloadLogCaptureMessage(post.toDto(event.get("OPERATION").toString()), "post_payload_log");
        }else if(tableName.equals("ROLE")){
            String topic = "role_payload_log";
            if(operation.equals("DELETE")){
                Long idx = Long.parseLong(event.get("SQL_REDO").toString().split("'")[1]);
                payloadLogProducer.sendNewRolePayloadLogCaptureMessage(
                        NewRolePayloadData.builder()
                                .operation(operation)
                                .roleIdx(idx)
                                .build(),
                        topic);
            }else{
                Role role = roleRepository.findByRowId(event.get("ROW_ID").toString());
                payloadLogProducer.sendNewRolePayloadLogCaptureMessage(role.toDto(operation), topic);
            }
//            Role role = roleRepository.findByRowId( event.get("ROW_ID").toString());
//            payloadLogProducer.sendNewRolePayloadLogCaptureMessage(role.toDto(event.get("OPERATION").toString()), "role_payload_log");
        }else if(tableName.equals("USER")){
            String topic = "user_payload_log";
            if(operation.equals("DELETE")){
                Long idx = Long.parseLong(event.get("SQL_REDO").toString().split("'")[1]);
                payloadLogProducer.sendNewUserPayloadLogCaptureMessage(
                        NewUsersPayloadData.builder()
                                .operation(event.get("OPERATION").toString())
                                .userIdx(idx)
                                .build(),
                        topic);
            }else{
                Users user = userRepository.findByRowId(event.get("ROW_ID").toString());
                payloadLogProducer.sendNewUserPayloadLogCaptureMessage(user.toDto(operation), topic);
            }
//            Users user = userRepository.findByRowId( event.get("ROW_ID").toString());
//            payloadLogProducer.sendNewUserPayloadLogCaptureMessage(user.toDto(event.get("OPERATION").toString()), "user_payload_log");
        }


    }

    private void produceEvent(Map<String, Object> event, EventEntity entity, String topic){
        try {

            payloadLogProducer.sendNewPayloadLogCaptureMessage(entity.toDto(event.get("OPERATION").toString()), topic);

        } catch (Exception e) {
            // 예외 처리 및 로그 출력
            System.err.println("이벤트 처리 중 오류 발생: " + e.getMessage());
            System.out.println(event.get("ROW_ID"));
            e.printStackTrace();
        }
    }

//    private void produceInteractionEvent(Map<String, Object> event){
//        Interaction interaction = null;
//        try {
//            System.out.println(event.get("ROW_ID"));
//            interaction = interactionRepository.findByRowId( event.get("ROW_ID").toString());
//
//            payloadLogProducer.sendNewPayloadLogCaptureMessage(NewPayloadData.builder()
//                    .operation(event.get("OPERATION").toString())
//                    .tableName(event.get("TABLE_NAME").toString())
//                    .interactionIdx(interaction.getIdx())
//                    .userIdx(interaction.getUSERS().getIdx())
//                    .commentIdx(interaction.getCOMMENTS().getIdx())
//                    .emojiIdx(interaction.getEmoji().getIdx())
//                    .build());
//
//        } catch (Exception e) {
//            System.err.println("이벤트 처리 중 오류 발생: " + e.getMessage());
//            System.out.println(event.get("ROW_ID"));
//            System.out.println(interaction.getIdx());
//            e.printStackTrace();
//        }
//    }
}

