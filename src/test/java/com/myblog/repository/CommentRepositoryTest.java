package com.myblog.repository;

import com.myblog.MyBlogBackAppApplication;
import com.myblog.model.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = MyBlogBackAppApplication.class)
@Transactional
public class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long testPostId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM posts");

        // Create a test post for comments
        jdbcTemplate.update("INSERT INTO posts (id, title, text, likes_count) VALUES (?, ?, ?, ?)",
                1L, "Test Post", "Content", 0);
        testPostId = 1L;
    }

    @Test
    void testCreateAndFindComment() {
        // Given
        Comment comment = new Comment();
        comment.setText("Test comment");
        comment.setPostId(testPostId);

        // When
        Comment createdComment = commentRepository.create(comment);

        // Then
        assertNotNull(createdComment.getId());
        assertEquals("Test comment", createdComment.getText());
        assertEquals(testPostId, createdComment.getPostId());
        assertNotNull(createdComment.getCreatedAt());

        Optional<Comment> foundComment = commentRepository.findById(createdComment.getId());
        assertTrue(foundComment.isPresent());
        assertEquals(createdComment.getId(), foundComment.get().getId());
    }

    @Test
    void testFindByIdNotFound() {
        // When
        Optional<Comment> foundComment = commentRepository.findById(999L);

        // Then
        assertFalse(foundComment.isPresent());
    }

    @Test
    void testFindByPostId() {
        // Given
        Comment comment1 = new Comment();
        comment1.setText("First comment");
        comment1.setPostId(testPostId);
        commentRepository.create(comment1);

        Comment comment2 = new Comment();
        comment2.setText("Second comment");
        comment2.setPostId(testPostId);
        commentRepository.create(comment2);

        // When
        List<Comment> comments = commentRepository.findByPostId(testPostId);

        // Then
        assertEquals(2, comments.size());
    }

    @Test
    void testFindByPostIdEmpty() {
        // When
        List<Comment> comments = commentRepository.findByPostId(testPostId);

        // Then
        assertEquals(0, comments.size());
    }

    @Test
    void testUpdateComment() {
        // Given
        Comment comment = new Comment();
        comment.setText("Original comment");
        comment.setPostId(testPostId);
        Comment createdComment = commentRepository.create(comment);

        // When
        createdComment.setText("Updated comment");
        Comment updatedComment = commentRepository.update(createdComment);

        // Then
        assertEquals("Updated comment", updatedComment.getText());
        assertNotNull(updatedComment.getUpdatedAt());

        Optional<Comment> foundComment = commentRepository.findById(createdComment.getId());
        assertTrue(foundComment.isPresent());
        assertEquals("Updated comment", foundComment.get().getText());
    }

    @Test
    void testDeleteComment() {
        // Given
        Comment comment = new Comment();
        comment.setText("Comment to delete");
        comment.setPostId(testPostId);
        Comment createdComment = commentRepository.create(comment);

        // When
        commentRepository.delete(createdComment.getId());

        // Then
        Optional<Comment> foundComment = commentRepository.findById(createdComment.getId());
        assertFalse(foundComment.isPresent());
    }

    @Test
    void testCountByPostId() {
        // Given
        Comment comment1 = new Comment();
        comment1.setText("First comment");
        comment1.setPostId(testPostId);
        commentRepository.create(comment1);

        Comment comment2 = new Comment();
        comment2.setText("Second comment");
        comment2.setPostId(testPostId);
        commentRepository.create(comment2);

        Comment comment3 = new Comment();
        comment3.setText("Third comment");
        comment3.setPostId(testPostId);
        commentRepository.create(comment3);

        // When
        int count = commentRepository.countByPostId(testPostId);

        // Then
        assertEquals(3, count);
    }

    @Test
    void testCountByPostIdEmpty() {
        // When
        int count = commentRepository.countByPostId(testPostId);

        // Then
        assertEquals(0, count);
    }
}
