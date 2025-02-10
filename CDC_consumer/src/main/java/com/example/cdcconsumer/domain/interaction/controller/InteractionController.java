package com.example.cdcconsumer.domain.interaction.controller;

import com.example.cdcconsumer.global.infra.kafka.out.DataProducer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/interaction")
public class InteractionController {
    private final DataProducer dataProducer;

    @PostMapping("/capture")
    public ResponseEntity<String> capture( ){ //@RequestBody NewInteractionCaptureEvent req

        List<Map<String, Object>> req = new ArrayList<>();

        Map<String, Object> event = new HashMap<>();
        event.put("ROW_ID", "AAATNPAAHAAAALkAEy");
        event.put("OPERATION", "INSERT");
        event.put("SEG_OWNER", "C##DEEP");
        event.put("TABLE_NAME", "INTERACTION");
        event.put("SQL_REDO", "insert into \"C##DEEP\".\"INTERACTION\"(\"IDX\",\"COMMENT_IDX\",\"EMOJI_IDX\",\"USER_IDX\") values ('19835','5','484','3');");

        dataProducer.sendNewInteractionCaptureMessage(req);

        return ResponseEntity.ok("good");
    }
}
