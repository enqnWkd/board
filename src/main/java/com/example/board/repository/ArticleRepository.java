package com.example.board.repository;

import com.example.board.domain.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query(
            value = """
        select a
        from Article a
        join fetch a.user
        where 
            lower(a.title) like lower(concat('%', :keyword, '%'))
            or lower(a.content) like lower(concat('%', :keyword, '%'))
            or lower(a.user.email) like lower(concat('%', :keyword, '%'))
    """,
            countQuery = """
        select count(a)
        from Article a
        where 
            lower(a.title) like lower(concat('%', :keyword, '%'))
            or lower(a.content) like lower(concat('%', :keyword, '%'))
            or lower(a.user.email) like lower(concat('%', :keyword, '%'))
    """
    )
    Page<Article> search(@Param("keyword") String keyword, Pageable pageable);
}
