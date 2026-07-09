package com.example.board.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ViewCountScheduler {

    private final ViewCountService viewCountService;

    @Scheduled(fixedRate = 60000) //1분마다
    public void syncViewCount() {
        viewCountService.syncToDatabase();
    }
}
