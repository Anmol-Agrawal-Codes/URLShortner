package com.example.UrlShortner.repository;

import com.example.UrlShortner.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Url findByShortLink(String shortLink);
    @Modifying
    @Query("DELETE FROM Url u WHERE u.shortLink = :shortLink")
    void deleteByShortLink(String shortLink);
}
