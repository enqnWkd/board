package com.example.board.controller;

import com.example.board.domain.Comment;
import com.example.board.dto.AddCommentRequest;
import com.example.board.dto.CommentResponse;
import com.example.board.service.CommentService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class CommentController {
    private CommentService commentService;

    //댓글 저장
    @PostMapping("/api/comments")
    public ResponseEntity<CommentResponse> addComment(
            @RequestBody AddCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        Comment savedComment = commentService.save(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(savedComment));
//        return ResponseEntity.created(URI.create("/api/articles/" + articleId)).build();
    }

    //댓글 조회
    @GetMapping("/api/articles/{articleId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long articleId
    ) {
        List<CommentResponse> comments = commentService.getCommentsByArticle(articleId);
        return ResponseEntity.ok(comments);
    }

    //댓글 삭제
    @DeleteMapping("/api/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> deleteComment(
            @PathVariable Long articleId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        commentService.deleteComment(commentId, userDetails.getUsername());

        return ResponseEntity.ok().build();
    }
}
