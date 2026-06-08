package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.BaseIntegrationTest;
import com.att.tdp.issueflow.entity.Project;
import com.att.tdp.issueflow.entity.Ticket;
import com.att.tdp.issueflow.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AttachmentControllerTest extends BaseIntegrationTest {

    private byte[] smallContent() {
        return "file content".getBytes();
    }

    @Test
    void uploadPng_succeeds() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        MockMultipartFile file = new MockMultipartFile("file", "photo.png",
                "image/png", smallContent());

        mockMvc.perform(multipart("/tickets/" + ticket.getId() + "/attachments")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.ticketId", is(ticket.getId().intValue())))
                .andExpect(jsonPath("$.filename", is("photo.png")))
                .andExpect(jsonPath("$.contentType", is("image/png")));
    }

    @Test
    void uploadJpeg_succeeds() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg",
                "image/jpeg", smallContent());

        mockMvc.perform(multipart("/tickets/" + ticket.getId() + "/attachments")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentType", is("image/jpeg")));
    }

    @Test
    void uploadPdf_succeeds() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf",
                "application/pdf", smallContent());

        mockMvc.perform(multipart("/tickets/" + ticket.getId() + "/attachments")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentType", is("application/pdf")));
    }

    @Test
    void uploadTextPlain_succeeds() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        MockMultipartFile file = new MockMultipartFile("file", "notes.txt",
                "text/plain", smallContent());

        mockMvc.perform(multipart("/tickets/" + ticket.getId() + "/attachments")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentType", is("text/plain")));
    }

    @Test
    void uploadInvalidContentType_returns400() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        MockMultipartFile file = new MockMultipartFile("file", "archive.zip",
                "application/zip", smallContent());

        mockMvc.perform(multipart("/tickets/" + ticket.getId() + "/attachments")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadEmptyFile_returns400() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        MockMultipartFile file = new MockMultipartFile("file", "empty.png",
                "image/png", new byte[0]);

        mockMvc.perform(multipart("/tickets/" + ticket.getId() + "/attachments")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadToNonExistingTicket_returns404() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");

        MockMultipartFile file = new MockMultipartFile("file", "photo.png",
                "image/png", smallContent());

        mockMvc.perform(multipart("/tickets/99999/attachments")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAttachment_success() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        MockMultipartFile file = new MockMultipartFile("file", "photo.png",
                "image/png", smallContent());

        String uploadResponse = mockMvc.perform(multipart("/tickets/" + ticket.getId() + "/attachments")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();

        Long attachmentId = objectMapper.readTree(uploadResponse).get("id").asLong();

        mockMvc.perform(delete("/tickets/" + ticket.getId() + "/attachments/" + attachmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void deleteNonExistingAttachment_returns404() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        mockMvc.perform(delete("/tickets/" + ticket.getId() + "/attachments/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadResponse_doesNotIncludeBinaryData() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        MockMultipartFile file = new MockMultipartFile("file", "photo.png",
                "image/png", smallContent());

        mockMvc.perform(multipart("/tickets/" + ticket.getId() + "/attachments")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                // Response should only contain metadata, not the file binary
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void uploadExecutableSpoofedAsText_returns400() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        MockMultipartFile file = new MockMultipartFile("file", "malware.exe",
                "text/plain", smallContent());

        mockMvc.perform(multipart("/tickets/" + ticket.getId() + "/attachments")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadScriptSpoofedAsImage_returns400() throws Exception {
        User admin = createAdmin();
        String token = loginAndGetToken("admin", "secret");
        Project project = createProject(admin, "P1");
        Ticket ticket = createTicket(project, admin, null);

        MockMultipartFile file = new MockMultipartFile("file", "script.sh",
                "image/png", smallContent());

        mockMvc.perform(multipart("/tickets/" + ticket.getId() + "/attachments")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
}
