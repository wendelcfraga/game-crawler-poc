package com.wendel.gamecrawler.repositories;

import com.wendel.gamecrawler.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface postRepository extends JpaRepository<Post, Long>{
    
}
