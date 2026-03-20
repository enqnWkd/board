package com.example.board.repository;

import com.example.board.domain.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query(
            value = """
        select a
        from Article a
        join fetch a.user
    """,
            countQuery = """
        select count(a)
        from Article a
    """
    )
    Page<Article> findAllWithUser(Pageable pageable);
}
