package com.myblog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblog.MyBlogBackAppApplication;
import com.myblog.dto.CreatePostRequest;
import com.myblog.dto.UpdatePostRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MyBlogBackAppApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class PostControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        
        jdbcTemplate.execute("DELETE FROM post_images");
        jdbcTemplate.execute("DELETE FROM post_tags");
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM posts");
    }

    @Test
    void testGetPosts() throws Exception {
        mockMvc.perform(get("/posts")
                .param("search", "")
                .param("pageNumber", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.posts").isArray())
            .andExpect(jsonPath("$.hasPrev").exists())
            .andExpect(jsonPath("$.hasNext").exists())
            .andExpect(jsonPath("$.lastPage").exists());
    }

    @Test
    void testCreatePost() throws Exception {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("New Post");
        request.setText("New content");
        request.setTags(Arrays.asList("tag1", "tag2"));

        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title").value("New Post"))
            .andExpect(jsonPath("$.text").value("New content"))
            .andExpect(jsonPath("$.likesCount").value(0));
    }

    @Test
    void testGetPostById() throws Exception {
        // Create a post first
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Test Post");
        request.setText("Test content");
        request.setTags(Arrays.asList("tag1"));

        String response = mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long postId = objectMapper.readTree(response).get("id").asLong();

        // Get the post
        mockMvc.perform(get("/posts/" + postId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(postId))
            .andExpect(jsonPath("$.title").value("Test Post"));
    }

    @Test
    void testUpdatePost() throws Exception {
        // Create a post first
        CreatePostRequest createRequest = new CreatePostRequest();
        createRequest.setTitle("Original Title");
        createRequest.setText("Original content");
        createRequest.setTags(Arrays.asList("tag1"));

        String response = mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long postId = objectMapper.readTree(response).get("id").asLong();

        // Update the post
        UpdatePostRequest updateRequest = new UpdatePostRequest();
        updateRequest.setId(postId);
        updateRequest.setTitle("Updated Title");
        updateRequest.setText("Updated content");
        updateRequest.setTags(Arrays.asList("tag2"));

        mockMvc.perform(put("/posts/" + postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Updated Title"))
            .andExpect(jsonPath("$.text").value("Updated content"));
    }

    @Test
    void testDeletePost() throws Exception {
        // Create a post first
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Post to Delete");
        request.setText("Content");
        request.setTags(Arrays.asList());

        String response = mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long postId = objectMapper.readTree(response).get("id").asLong();

        // Delete the post
        mockMvc.perform(delete("/posts/" + postId))
            .andExpect(status().isOk());

        // Verify it's deleted
        mockMvc.perform(get("/posts/" + postId))
            .andExpect(status().isNotFound());
    }

    @Test
    void testIncrementLikes() throws Exception {
        // Create a post first
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Post with Likes");
        request.setText("Content");
        request.setTags(Arrays.asList());

        String response = mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long postId = objectMapper.readTree(response).get("id").asLong();

        // Increment likes
        mockMvc.perform(post("/posts/" + postId + "/likes"))
            .andExpect(status().isOk())
            .andExpect(content().string("1"));

        // Increment again
        mockMvc.perform(post("/posts/" + postId + "/likes"))
            .andExpect(status().isOk())
            .andExpect(content().string("2"));
    }
}
