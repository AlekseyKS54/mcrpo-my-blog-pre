package com.myblog.repository;

import com.myblog.MyBlogBackAppApplication;
import com.myblog.model.Tag;
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
public class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM post_tags");
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM posts");
    }

    @Test
    void testCreateTag() {
        // Given
        String tagName = "java";

        // When
        Tag createdTag = tagRepository.create(tagName);

        // Then
        assertNotNull(createdTag.getId());
        assertEquals("java", createdTag.getName());
    }

    @Test
    void testFindByName() {
        // Given
        Tag tag = tagRepository.create("spring");

        // When
        Optional<Tag> foundTag = tagRepository.findByName("spring");

        // Then
        assertTrue(foundTag.isPresent());
        assertEquals(tag.getId(), foundTag.get().getId());
        assertEquals("spring", foundTag.get().getName());
    }

    @Test
    void testFindByNameNotFound() {
        // When
        Optional<Tag> foundTag = tagRepository.findByName("nonexistent");

        // Then
        assertFalse(foundTag.isPresent());
    }

    @Test
    void testLinkTagToPostAndFindByPostId() {
        // Given
        Tag tag1 = tagRepository.create("java");
        Tag tag2 = tagRepository.create("spring");

        // Create a post manually
        jdbcTemplate.update("INSERT INTO posts (id, title, text, likes_count) VALUES (?, ?, ?, ?)",
                1L, "Test Post", "Content", 0);

        // When
        tagRepository.linkTagToPost(tag1.getId(), 1L);
        tagRepository.linkTagToPost(tag2.getId(), 1L);

        List<Tag> tags = tagRepository.findByPostId(1L);

        // Then
        assertEquals(2, tags.size());
        assertTrue(tags.stream().anyMatch(t -> t.getName().equals("java")));
        assertTrue(tags.stream().anyMatch(t -> t.getName().equals("spring")));
    }

    @Test
    void testUnlinkAllTagsFromPost() {
        // Given
        Tag tag1 = tagRepository.create("java");
        Tag tag2 = tagRepository.create("spring");

        // Create a post manually
        jdbcTemplate.update("INSERT INTO posts (id, title, text, likes_count) VALUES (?, ?, ?, ?)",
                1L, "Test Post", "Content", 0);

        tagRepository.linkTagToPost(tag1.getId(), 1L);
        tagRepository.linkTagToPost(tag2.getId(), 1L);

        // When
        tagRepository.unlinkAllTagsFromPost(1L);

        List<Tag> tags = tagRepository.findByPostId(1L);

        // Then
        assertEquals(0, tags.size());
    }

    @Test
    void testFindByPostIdEmpty() {
        // Given
        jdbcTemplate.update("INSERT INTO posts (id, title, text, likes_count) VALUES (?, ?, ?, ?)",
                1L, "Test Post", "Content", 0);

        // When
        List<Tag> tags = tagRepository.findByPostId(1L);

        // Then
        assertEquals(0, tags.size());
    }
}
