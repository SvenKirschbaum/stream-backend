package de.elite12.stream.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Controller
public class TestController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/echo")
    @SendToUser("/queue/echoreply")
    public String echoMessage(String input) {
        return input;
    }

    @Scheduled(fixedRate = 1500)
    public void periodicMessage() {
        simpMessagingTemplate.convertAndSend("/topic/test", "Periodic Message: %s".formatted(DateTimeFormatter.ISO_LOCAL_TIME.format(LocalTime.now())));
    }
}
