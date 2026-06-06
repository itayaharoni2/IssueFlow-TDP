package com.att.tdp.issueflow;

import com.att.tdp.issueflow.config.JwtAuthFilter;
import com.att.tdp.issueflow.entity.Project;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.User;
import com.att.tdp.issueflow.entity.enums.Role;
import com.att.tdp.issueflow.entity.enums.TicketPriority;
import com.att.tdp.issueflow.entity.enums.TicketStatus;
import com.att.tdp.issueflow.entity.enums.TicketType;
import com.att.tdp.issueflow.repository.AuditLogRepository;
import com.att.tdp.issueflow.repository.CommentMentionRepository;
import com.att.tdp.issueflow.repository.CommentRepository;
import com.att.tdp.issueflow.repository.ProjectRepository;
import com.att.tdp.issueflow.repository.TicketDependencyRepository;
import com.att.tdp.issueflow.repository.TicketRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import com.att.tdp.issueflow.repository.AttachmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for all integration tests.
 * Provides:
 * - MockMvc, ObjectMapper injection
 * - Helper methods to create users, projects, tickets
 * - Helper to log in and extract a Bearer token
 * - DB teardown between tests
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected ProjectRepository projectRepository;
    @Autowired protected TicketRepository ticketRepository;
    @Autowired protected TicketDependencyRepository ticketDependencyRepository;
    @Autowired protected CommentRepository commentRepository;
    @Autowired protected CommentMentionRepository commentMentionRepository;
    @Autowired protected AuditLogRepository auditLogRepository;
    @Autowired protected AttachmentRepository attachmentRepository;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired protected JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void baseTeardown() {
        // Delete in dependency order
        attachmentRepository.deleteAll();
        commentMentionRepository.deleteAll();
        commentRepository.deleteAll();
        ticketDependencyRepository.deleteAll();
        ticketRepository.deleteAll();
        projectRepository.deleteAll();
        auditLogRepository.deleteAll();
        userRepository.deleteAll();
        jwtAuthFilter.clearDenyList();
    }

    // ─── User helpers ─────────────────────────────────────────────────────────

    protected User createUser(String username, String email, Role role) {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setFullName("Full " + username);
        u.setPassword(passwordEncoder.encode("secret"));
        u.setRole(role);
        return userRepository.save(u);
    }

    protected User createAdmin() {
        return createUser("admin", "admin@test.com", Role.ADMIN);
    }

    protected User createDeveloper(String username) {
        return createUser(username, username + "@test.com", Role.DEVELOPER);
    }

    // ─── Project helpers ──────────────────────────────────────────────────────

    protected Project createProject(User owner, String name) {
        Project p = new Project();
        p.setName(name);
        p.setDescription("Test project " + name);
        p.setOwner(owner);
        return projectRepository.save(p);
    }

    // ─── Ticket helpers ───────────────────────────────────────────────────────

    protected Ticket createTicket(Project project, User reporter, User assignee,
                                  TicketStatus status, TicketPriority priority) {
        Ticket t = new Ticket();
        t.setTitle("Test Ticket");
        t.setDescription("Desc");
        t.setStatus(status);
        t.setPriority(priority);
        t.setType(TicketType.BUG);
        t.setProject(project);
        t.setReporter(reporter);
        t.setAssignee(assignee);
        return ticketRepository.save(t);
    }

    protected Ticket createTicket(Project project, User reporter, User assignee) {
        return createTicket(project, reporter, assignee, TicketStatus.TODO, TicketPriority.LOW);
    }

    protected Ticket createOverdueTicket(Project project, User reporter,
                                          TicketStatus status, TicketPriority priority) {
        Ticket t = new Ticket();
        t.setTitle("Overdue Ticket");
        t.setDescription("Past due");
        t.setStatus(status);
        t.setPriority(priority);
        t.setType(TicketType.BUG);
        t.setProject(project);
        t.setReporter(reporter);
        t.setDueDate(OffsetDateTime.now().minusDays(1));
        return ticketRepository.save(t);
    }

    // ─── Auth helpers ─────────────────────────────────────────────────────────

    protected String loginAndGetToken(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("username", username, "password", password));

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }

    protected String loginAsAdmin() throws Exception {
        createAdmin();
        return loginAndGetToken("admin", "secret");
    }

    protected String loginAsDeveloper(String username) throws Exception {
        createDeveloper(username);
        return loginAndGetToken(username, "secret");
    }

    // ─── Request body helpers ────────────────────────────────────────────────

    protected String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }
}
