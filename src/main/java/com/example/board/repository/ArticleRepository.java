package com.example.board.repository;

import com.example.board.domain.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface ArticleRepository extends JpaRepository<Article, Long> {

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

    @Query("""
    select a
    from Article a
    join fetch a.user
    where a.id = :id
""")
    Optional<Article> findByIdWithUser(@Param("id") Long id);

    @Modifying
    @Query("""
           update Article a
           set a.viewCount = :viewCount
           where a.id = :articleId
       """)
    void updateViewCount(Long articleId, Long viewCount);
}