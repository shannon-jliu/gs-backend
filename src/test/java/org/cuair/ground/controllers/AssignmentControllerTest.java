package org.cuair.ground.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.*;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.mock.web.MockMultipartFile;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.util.List;
import java.util.ArrayList;
import java.io.*;
import org.cuair.ground.models.Assignment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import io.ebean.Ebean;
import org.apache.commons.io.FileUtils;
import org.springframework.http.*;
import java.sql.Timestamp;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AssignmentController.class)
@AutoConfigureMockMvc
public class AssignmentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AssignmentController controller;

    @Before
    public void clearDB() {
        List<Assignment> assignments = Ebean.find(Assignment.class).findList();
        Ebean.beginTransaction();
        Ebean.deleteAll(assignments);
        Ebean.commitTransaction();
    }

    @Test
    public void loads() {
        assertThat(controller).isNotNull();
    }

    // TODO: Get full coverage

    @Test
    public void getAll() throws Exception {
        ArrayList<Assignment> list = new ArrayList();
        mvc.perform(MockMvcRequestBuilders.get("/assignment").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(list.toString())));
    }

    @Test
    public void get() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/assignment/-1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(equalTo("")));
    }

    @Test
    public void getByUser() throws Exception {
        // Auth is disabled
        mvc.perform(MockMvcRequestBuilders.get("/assignment/user").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(content().string(equalTo("")));
    }

    @Test
    public void createWork() throws Exception {
        // Auth is disabled
        mvc.perform(MockMvcRequestBuilders.post("/assignment/work/MDLC").accept(MediaType.APPLICATION_JSON)
                .param("type", "MDLC")
                .param("jsonString", "{\"token\":fj39wjhs69hdek939cxnb5}"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(equalTo("")));
    }

    @Test
    public void update() throws Exception {
        mvc.perform(MockMvcRequestBuilders.put("/assignment/-1").accept(MediaType.APPLICATION_JSON)
                .param("id", "-1")
                .param("jsonString", "{\"token\":fj39wjhs69hdek939cxnb5}"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(equalTo("")));
    }
}
