package com.example.blog.service;

import com.example.blog.domain.Article;
import com.example.blog.domain.Comment;
import com.example.blog.domain.User;
import com.example.blog.dto.AddCommentRequest;
import com.example.blog.dto.ArticleResponse;
import com.example.blog.dto.CommentResponse;
import com.example.blog.exception.ArticleNotFoundException;
import com.example.blog.repository.BlogRepository;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
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

    //댓글 등록
    @Transactional
    public Comment save(AddCommentRequest request, String email) {
        // email로 User 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("없는 사용자입니다."));

        //댓글이 달릴 게시글 조회
        Article article = blogRepository.findById(request.getArticleId())
                .orElseThrow(() -> new ArticleNotFoundException("해당 글이 없습니다."));

        //댓글 엔티티 생성 후 저장
        Comment comment = Comment.builder()
                .content(request.getContent())
                .article(article)
                .user(user)
                .build();

        return commentRepository.save(comment);
    }

    //댓글 조회
    public List<CommentResponse> getCommentsByArticle(Long articleId) {
        Article article = blogRepository.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException("해당 게시글이 없습니다."));

        return commentRepository.findByArticle(article).stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    //댓글 삭제
    public void deleteComment(Long commentId, String email) {

        //댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        if (!comment.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("댓글을 삭제할 권한이 없습니다.");
        }

        commentRepository.deleteById(commentId);
    }
}
