package com.example.board.controller;

import com.example.board.domain.Comment;
import com.example.board.dto.request.AddCommentRequest;
import com.example.board.dto.response.CommentResponse;
import com.example.board.security.CustomUserDetails;
import com.example.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class CommentController {
    private final CommentService commentService;

    //댓글 등록
    @PostMapping("/api/{articleId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long articleId,
            @RequestBody AddCommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        Comment savedComment = commentService.save(articleId, request, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(savedComment));
    }

    //특정 댓글 조회
    @GetMapping("/api/articles/{articleId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long articleId
    ) {
        List<CommentResponse> comments = commentService.getCommentsByArticle(articleId);
        return ResponseEntity.ok(comments);
    }

    //댓글 삭제
    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<CommentResponse> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        commentService.deleteComment(commentId, userDetails.getUserId());

        return ResponseEntity.ok().build();
    }
}
