package de.elite12.stream.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ManageController {

    private static final Logger logger = LoggerFactory.getLogger(ManageController.class);

    @Autowired
    private SimpMessagingTemplate template;

    @PostMapping("/send")
    @PreAuthorize("hasRole('manager')")
    public void helloAction(@RequestBody String message) {
        logger.info("Message send by User %s: %s".formatted(SecurityContextHolder.getContext().getAuthentication().getName(), message));
        template.convertAndSend("/topic/racecontrol",message);
    }
}
