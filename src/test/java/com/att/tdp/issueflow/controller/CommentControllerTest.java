package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.BaseIntegrationTest;
import com.att.tdp.issueflow.entity.Project;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentControllerTest extends BaseIntegrationTest {

    @Test
    void addComment_success() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        mockMvc.perform(post("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "authorId", admin.getId(),
                                "content", "This is a comment"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.content", is("This is a comment")))
                .andExpect(jsonPath("$.mentionedUsers", is(empty())));
    }

    @Test
    void addComment_nonExistingTicket_returns404() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");

        mockMvc.perform(post("/tickets/99999/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "authorId", admin.getId(),
                                "content", "A comment"
                        ))))
                .andExpect(status().isNotFound());
    }

    @Test
    void addComment_nonExistingAuthor_returns404() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        mockMvc.perform(post("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "authorId", 99999,
                                "content", "A comment"
                        ))))
                .andExpect(status().isNotFound());
    }

    @Test
    void addComment_emptyContent_returns400() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        mockMvc.perform(post("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "authorId", admin.getId(),
                                "content", ""
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getComments_returnsListWithMentionedUsers() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        mockMvc.perform(post("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("authorId", admin.getId(), "content", "Hello"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].mentionedUsers", notNullValue()));
    }

    @Test
    void updateComment_success() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        String createResponse = mockMvc.perform(post("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("authorId", admin.getId(), "content", "Original"))))
                .andReturn().getResponse().getContentAsString();

        Long commentId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(patch("/tickets/" + ticket.getId() + "/comments/" + commentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "Updated"))))
                .andExpect(status().isOk());
    }

    @Test
    void deleteComment_success() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        String createResponse = mockMvc.perform(post("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("authorId", admin.getId(), "content", "To delete"))))
                .andReturn().getResponse().getContentAsString();

        Long commentId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(delete("/tickets/" + ticket.getId() + "/comments/" + commentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    // ─── Mention tests ────────────────────────────────────────────────────────

    @Test
    void comment_withMention_populatesMentionedUsers() throws Exception {
        User admin = createAdmin();
        User dev = createDeveloper("dev1");
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        mockMvc.perform(post("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("authorId", admin.getId(), "content", "Hello @dev1 please check"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mentionedUsers", hasSize(1)))
                .andExpect(jsonPath("$.mentionedUsers[0].username", is("dev1")));
    }

    @Test
    void comment_mentionCaseInsensitive() throws Exception {
        User admin = createAdmin();
        createDeveloper("dev1");
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        mockMvc.perform(post("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("authorId", admin.getId(), "content", "Hey @DEV1 take a look"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mentionedUsers", hasSize(1)));
    }

    @Test
    void comment_mentionNonExistingUser_silentlyIgnored() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        mockMvc.perform(post("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("authorId", admin.getId(), "content", "Hello @ghost_user"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mentionedUsers", is(empty())));
    }

    @Test
    void updateComment_rEvaluatesMentions() throws Exception {
        User admin = createAdmin();
        User dev = createDeveloper("dev1");
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        // Create comment with @dev1 mention
        String createResponse = mockMvc.perform(post("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("authorId", admin.getId(), "content", "@dev1 check this"))))
                .andReturn().getResponse().getContentAsString();
        Long commentId = objectMapper.readTree(createResponse).get("id").asLong();

        // Update comment to remove the mention
        mockMvc.perform(patch("/tickets/" + ticket.getId() + "/comments/" + commentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "No mentions now"))))
                .andExpect(status().isOk());

        // Verify mentions are cleared
        mockMvc.perform(get("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.content[0].mentionedUsers", is(empty())));
    }

    @Test
    void getMentions_returnsCommentsWhereUserMentioned() throws Exception {
        User admin = createAdmin();
        User dev = createDeveloper("dev1");
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        mockMvc.perform(post("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("authorId", admin.getId(), "content", "Hey @dev1"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/" + dev.getId() + "/mentions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void comment_mentionWithPunctuation_matchesCorrectly() throws Exception {
        User admin = createAdmin();
        createDeveloper("dev1");
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        mockMvc.perform(post("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("authorId", admin.getId(), "content", "Hello @dev1, please check this. @dev1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mentionedUsers", hasSize(1)))
                .andExpect(jsonPath("$.mentionedUsers[0].username", is("dev1")));
    }

    @Test
    void comment_mentionMultipleDifferentUsers_matchesAll() throws Exception {
        User admin = createAdmin();
        createDeveloper("dev1");
        createDeveloper("dev2");
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        mockMvc.perform(post("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("authorId", admin.getId(), "content", "Hey @dev1 and @dev2"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mentionedUsers", hasSize(2)));
    }

    @Test
    void comment_mentionEmailAddress_isIgnored() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        mockMvc.perform(post("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("authorId", admin.getId(), "content", "Contact me at test@admin.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mentionedUsers", is(empty())));
    }

    @Test
    void comment_mentionSameUserMultipleTimes_onlyOneMetadataEntry() throws Exception {
        User admin = createAdmin();
        createDeveloper("dev1");
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        mockMvc.perform(post("/tickets/" + ticket.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("authorId", admin.getId(), "content", "Hey @dev1 did you see what @dev1 said?"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mentionedUsers", hasSize(1)));
    }
}
