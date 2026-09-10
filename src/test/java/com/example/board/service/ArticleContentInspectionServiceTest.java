package com.example.board.service;

import com.example.board.client.OpenAiClient;
import com.example.board.exception.ContentInspectionException;
import com.example.board.exception.Errorcode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleContentInspectionServiceTest {

    @Mock
    private OpenAiClient openAiClient;

    @InjectMocks
    private ArticleContentInspectionService articleContentInspectionService;

    @Test
    void NORMAL이면_검사를_통과한다() {
        when(openAiClient.inspect("제목", "내용"))
                .thenReturn(ContentInspectionResult.NORMAL);

        assertDoesNotThrow(() -> articleContentInspectionService.inspect("제목", "내용"));
    }

    @Test
    void ADVERTISEMENT이면_광고성_콘텐츠_예외를_발생시킨다() {
        when(openAiClient.inspect("제목", "내용"))
                .thenReturn(ContentInspectionResult.ADVERTISEMENT);

        assertThatThrownBy(() -> articleContentInspectionService.inspect("제목", "내용"))
                .isInstanceOf(ContentInspectionException.class)
                .extracting("errorCode")
                .isEqualTo(Errorcode.ADVERTISEMENT_CONTENT);
    }

    @Test
    void INAPPROPRIATE이면_부적절한_콘텐츠_예외를_발생시킨다() {
        when(openAiClient.inspect("제목", "내용"))
                .thenReturn(ContentInspectionResult.INAPPROPRIATE);

        assertThatThrownBy(() -> articleContentInspectionService.inspect("제목", "내용"))
                .isInstanceOf(ContentInspectionException.class)
                .extracting("errorCode")
                .isEqualTo(Errorcode.INAPPROPRIATE_CONTENT);
    }

    @Test
    void OpenAI_호출_오류는_그대로_전파한다() {
        when(openAiClient.inspect("제목", "내용"))
                .thenThrow(new ContentInspectionException(Errorcode.OPENAI_SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> articleContentInspectionService.inspect("제목", "내용"))
                .isInstanceOf(ContentInspectionException.class)
                .extracting("errorCode")
                .isEqualTo(Errorcode.OPENAI_SERVICE_UNAVAILABLE);
    }
}
