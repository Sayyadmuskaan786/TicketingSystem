package com.example.formbackend.repository;

import com.example.formbackend.model.Comment;
import com.example.formbackend.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTicket(Ticket ticket);

    @Query("SELECT c FROM Comment c")
    List<Comment> getAllComments();
}
