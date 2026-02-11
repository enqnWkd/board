package com.example.board.service;

import com.example.board.domain.Article;
import com.example.board.domain.Comment;
import com.example.board.domain.User;
import com.example.board.dto.AddCommentRequest;
import com.example.board.dto.CommentResponse;
import com.example.board.exception.AccessDeniedException;
import com.example.board.exception.AuthException;
import com.example.board.exception.Errorcode;
import com.example.board.exception.NotFoundException;
import com.example.board.repository.BlogRepository;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    @Transactional
    public Comment save(AddCommentRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(Errorcode.USER_NOT_FOUND));

        Article article = blogRepository.findById(request.getArticleId())
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .article(article)
                .user(user)
                .build();

        return commentRepository.save(comment);
    }

    public List<CommentResponse> getCommentsByArticle(Long articleId) {
        Article article = blogRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        return commentRepository.findByArticle(article).stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    public void deleteComment(Long commentId, String email) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(Errorcode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException(Errorcode.ACCESS_DENIED);
        }

        commentRepository.deleteById(commentId);
    }
}
