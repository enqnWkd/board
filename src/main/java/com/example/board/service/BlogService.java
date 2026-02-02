package com.example.board.service;

import com.example.board.domain.Article;
import com.example.board.domain.User;
import com.example.board.dto.AddArticleRequest;
import com.example.board.dto.ArticleResponse;
import com.example.board.dto.UpdateArticleRequest;
import com.example.board.exception.ArticleNotFoundException;
import com.example.board.exception.UnauthorizedAccessException;
import com.example.board.repository.BlogRepository;
import com.example.board.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    public BlogService(BlogRepository blogRepository, UserRepository userRepository) {
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
    }

    public Article save(AddArticleRequest request, String email) {
        // email로 User 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("없는 사용자입니다."));

        Article article = request.toEntity();
        article.setUser(user);
        return blogRepository.save(article);
    }

    //조회 로직
    public List<Article> findAll() {
        return blogRepository.findAll();
    }

    public ArticleResponse findArticle(Long id) {
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException("해당 글이 없습니다."));
        return new ArticleResponse(article);
    }

    public Article findById(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException("해당 글이 없습니다."));
    }


    //삭제 로직
    public void deleteAll() {
        blogRepository.deleteAll();
    }

    public void delete(Long id, String email) {

        // 글 조회
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException("해당 글이 없습니다."));

        // email로 User 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("없는 사용자입니다."));

        //작성자 확인
        if (!article.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException("글 삭제 권한이 없습니다");
        }
        blogRepository.deleteById(id);
    }

    //수정 로직
    @Transactional
    public Article update(Long id, UpdateArticleRequest request, String email) {

        // 글 조회
        Article article = blogRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException("해당 글이 없습니다."));

        // email로 User 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("없는 사용자입니다."));

        //작성자 확인
        if (!article.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("글 수정 권한이 없습니다");
        }

        article.update(request.getTitle(), request.getContent());
        return article;
    }
}
