package com.example.board.service;

import com.example.board.domain.Article;
import com.example.board.domain.User;
import com.example.board.dto.request.AddArticleRequest;
import com.example.board.dto.response.ArticleResponse;
import com.example.board.dto.request.UpdateArticleRequest;
import com.example.board.exception.*;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public BoardService(BoardRepository boardRepository, UserRepository userRepository) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }

    public ArticleResponse save(AddArticleRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(Errorcode.USER_NOT_FOUND));

        Article article = request.toEntity();
        article.setUser(user);
        boardRepository.save(article);

        return ArticleResponse.from(article);
    }

    public List<Article> findAll() {
        return boardRepository.findAll();
    }

    public ArticleResponse findArticle(Long id) {
        Article article = boardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));
        return new ArticleResponse(article);
    }

    public Article findById(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));
    }

    public void deleteAll() {
        boardRepository.deleteAll();
    }

    public void delete(Long id, String email) {

        Article article = boardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(Errorcode.USER_NOT_FOUND));

        if (!article.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(Errorcode.ACCESS_DENIED);
        }
        boardRepository.deleteById(id);
    }

    public Article update(Long id, UpdateArticleRequest request, String email) {

        Article article = boardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Errorcode.ARTICLE_NOT_FOUND));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(Errorcode.USER_NOT_FOUND));

        if (!article.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException(Errorcode.ACCESS_DENIED);
        }

        article.update(request.getTitle(), request.getContent());
        return article;
    }
}
