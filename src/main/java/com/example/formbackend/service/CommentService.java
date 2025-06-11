package com.example.formbackend.service;

import com.example.formbackend.dto.CommentDTO;
import com.example.formbackend.model.Comment;
import com.example.formbackend.model.Ticket;
import com.example.formbackend.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public List<Comment> getCommentsByTicket(Ticket ticket) {
        return commentRepository.findByTicket(ticket);
    }

    public Comment addComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    public Comment updateComment(Comment existingComment) {
        return commentRepository.save(existingComment);
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    // public List<CommentDTO> getAllCommentDTOs() {
    //     return commentRepository.findAll().stream()
    //             .map(CommentDTO::new)
    //             .toList();
    // }
}
