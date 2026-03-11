package com.example.board.service;

import com.example.board.domain.Article;
import com.example.board.domain.Comment;
import com.example.board.domain.User;
import com.example.board.dto.request.AddCommentRequest;
import com.example.board.dto.response.CommentResponse;
import com.example.board.exception.AccessDeniedException;
import com.example.board.exception.Errorcode;
import com.example.board.exception.NotFoundException;
import com.example.board.repository.ArticleRepository;
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
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Transactional
    public Comment save(Long articleId, AddCommentRequest request, Long userId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(Errorcode.USER_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .article(article)
                .user(user)
                .build();

        return commentRepository.save(comment);
    }

    public List<CommentResponse> getCommentsByArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        return commentRepository.findByArticle(article).stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(Errorcode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(Errorcode.ACCESS_DENIED);
        }

        commentRepository.deleteById(commentId);
    }
}
