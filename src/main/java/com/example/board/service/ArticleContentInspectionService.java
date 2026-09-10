package com.example.board.service;

import com.example.board.client.OpenAiClient;
import com.example.board.exception.ContentInspectionException;
import com.example.board.exception.Errorcode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleContentInspectionService {

    private final OpenAiClient openAiClient;

    public void inspect(String title, String content) {
        ContentInspectionResult result = openAiClient.inspect(title, content);

        if (result == ContentInspectionResult.ADVERTISEMENT) {
            throw new ContentInspectionException(Errorcode.ADVERTISEMENT_CONTENT);
        }

        if (result == ContentInspectionResult.INAPPROPRIATE) {
            throw new ContentInspectionException(Errorcode.INAPPROPRIATE_CONTENT);
        }
    }
}
