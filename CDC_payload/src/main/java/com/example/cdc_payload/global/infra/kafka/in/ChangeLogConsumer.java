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

//        {"ROW_ID":"AAATNPAAHAAAA7MABe",
//                "RS_ID":" 0x00002c.000112ad.0010 ",
//                "OPERATION":"UPDATE",
//                "SEG_OWNER":"C##DEEP",
//                "TABLE_NAME":"INTERACTION",
//                "XIDUSN":10,
//                "XIDSLT":30,
//                "SQL_REDO":"update "C##DEEP"."INTERACTION" set "EMOJI_IDX" = '17' where "EMOJI_IDX" = '16' and ROWID = 'AAATNPAAHAAAA7MABe';"}
//
//        {"ROW_ID":"AAATNPAAHAAABODADq",
//                "RS_ID":" 0x00002c.000112ff.0010 ",
//                "OPERATION":"INSERT",
//                "SEG_OWNER":"C##DEEP",
//                "TABLE_NAME":"INTERACTION",
//                "XIDUSN":10,"XIDSLT":9,
//                "SQL_REDO":"insert into "C##DEEP"."INTERACTION"("IDX","COMMENT_IDX","EMOJI_IDX","USER_IDX") values ('43690','10','6','8');"}
//
//        {"ROW_ID":"AAATNPAAHAAABODADq",
//                "RS_ID":" 0x00002c.0001130f.0010 ",
//                "OPERATION":"DELETE",
//                "SEG_OWNER":"C##DEEP",
//                "TABLE_NAME":"INTERACTION",
//                "XIDUSN":10,
//                "XIDSLT":18,
//                "SQL_REDO":"delete from "C##DEEP"."INTERACTION" where "IDX" = '43690' and "COMMENT_IDX" = '10' and "EMOJI_IDX" = '6' and "USER_IDX" = '8' and ROWID = 'AAATNPAAHAAABODADq';"}

        // delete log 는 idx만 String으로 ex) "1"
        // insert log 는 values뒤에 스트링 그대로
        // update log 는 값 다 가져와서 스트링으로 만들기




        String tableName = event.get("TABLE_NAME").toString();
        String operation = event.get("OPERATION").toString();
        String logToString = "";

        log.info(">>> 이벤트 수신 ...");
        log.info(">>> TABLE_NAME : "+tableName);
        log.info(">>> OPERATION : "+operation);

        if(operation.equals("DELETE")){
            logToString = event.get("SQL_REDO").toString().split("'")[1];
        }else if(operation.equals("INSERT")){
            logToString = event.get("SQL_REDO").toString().split("values \\(")[1]; //'43690','10','6','8');
            logToString.replace("'","").replace(")","").replace(";","");
        }else{
            if(tableName.equals("COMMENTS")){
                Comments comments = commentsRepository.findByRowId(event.get("ROW_ID").toString());
                logToString = comments.logToString();
            }else if(tableName.equals("EMOJI")){
                Emoji emoji = emojiRepository.findByRowId(event.get("ROW_ID").toString());
                logToString = emoji.logToString();
            }else if(tableName.equals("INTERACTION")){
                Interaction interaction = interactionRepository.findByRowId(event.get("ROW_ID").toString());
                logToString = interaction.logToString();
            }else if(tableName.equals("POST")){
                Post post = postRepository.findByRowId(event.get("ROW_ID").toString());
                logToString = post.logToString();
            }else if(tableName.equals("ROLE")){
                Role role = roleRepository.findByRowId(event.get("ROW_ID").toString());
                logToString = role.logToString();
            }else if(tableName.equals("USER")){
                Users users = userRepository.findByRowId(event.get("ROW_ID").toString());
                logToString = users.logToString();
            }
        }

        payloadLogProducer.sendNewPayloadLogCaptureMessage(
                NewPayloadData.builder()
                        .operation(operation)
                        .tableName(tableName)
                        .log(logToString)
                        .build());
    }
}

