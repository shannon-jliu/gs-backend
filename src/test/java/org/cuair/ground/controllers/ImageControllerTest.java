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
import org.cuair.ground.models.Image;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import io.ebean.Ebean;
import org.apache.commons.io.FileUtils;
import org.springframework.http.*;
import java.sql.Timestamp;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ImageController.class)
@AutoConfigureMockMvc
public class ImageControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ImageController controller;

    @Before
    public void clearDB() {
        List<Image> images = Ebean.find(Image.class).findList();
        Ebean.beginTransaction();
        Ebean.deleteAll(images);
        Ebean.commitTransaction();

        try {
            FileUtils.cleanDirectory(FileUtils.getFile("images/"));
        } catch (IOException e) {
            System.out.println("images/ could not be cleared.");
        }
    }

    @Test
    public void loads() {
        assertThat(controller).isNotNull();
    }

    // TODO: Get full coverage

    // @Test
    // public void getAll() throws Exception {
    //     ArrayList<Image> list = new ArrayList();
    //     mvc.perform(MockMvcRequestBuilders.get("/image").accept(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isOk())
    //             .andExpect(content().string(equalTo(list.toString())));
    // }

    // @Test
    // public void getRecent() throws Exception {
    //     mvc.perform(MockMvcRequestBuilders.get("/image/recent").accept(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isNoContent())
    //             .andExpect(content().string(equalTo("")));
    // }

    // @Test
    // public void get() throws Exception {
    //     mvc.perform(MockMvcRequestBuilders.get("/image/-1").accept(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isNoContent())
    //             .andExpect(content().string(equalTo("")));
    // }

    // @Test
    // public void create() throws Exception {
    //     InputStream is = new BufferedInputStream(new FileInputStream("src/test/java/org/cuair/ground/controllers/test_images/test_0.jpg"));
    //     MockMultipartFile firstFile = new MockMultipartFile("files", "test_0.jpg", "image", is);

    //     String timestamp = "100000000000006";
    //     String imgMode = "retract";

    //     ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
    //                         .file(firstFile)
    //                         .param("jsonString", "{\"timestamp\":"+timestamp+",\"imgMode\":\""+imgMode+"\"}")
    //                     ).andExpect(status().isOk());
    //     MvcResult result = resultAction.andReturn();

    //     Image recent = (Image) controller.getRecent().getBody();
    //     Long expectedId = recent.getId();

    //     ResponseEntity asyncedResponseEntity = (ResponseEntity) result.getAsyncResult();
    //     Image i = (Image) asyncedResponseEntity.getBody();

    //     ObjectMapper mapper = new ObjectMapper();
    //     String imageAsString = mapper.writeValueAsString(i);

    //     assertEquals(imageAsString,
    //             "{\"id\":"+expectedId+",\"timestamp\":"+timestamp+",\"localImageUrl\":\"images/"+timestamp+".jpeg\",\"imageUrl\":\"/api/v1/image/file/"+timestamp+".jpeg\",\"telemetry\":null,\"imgMode\":\""+imgMode+"\"}"
    //         );
    // }

    // @Test
    // public void getGeotagCoordinates() throws Exception {
    //     mvc.perform(MockMvcRequestBuilders.get("/image/geotag/-1").accept(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isNoContent())
    //             .andExpect(content().string(equalTo("")));
    // }

    // @Test
    // public void dummyCreate() throws Exception {
    //     InputStream is = new BufferedInputStream(new FileInputStream("src/test/java/org/cuair/ground/controllers/test_images/test_0.jpg"));
    //     MockMultipartFile firstFile = new MockMultipartFile("files", "test_0.jpg", "image", is);

    //     String timestamp = "100000000000006";
    //     String imgMode = "retract";

    //     ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.multipart("/image/dummy")
    //                         .file(firstFile)
    //                         .param("jsonString", "{\"timestamp\":"+timestamp+",\"imgMode\":\""+imgMode+"\"}")
    //                     ).andExpect(status().isOk());

    //     Image recent = (Image) controller.getRecent().getBody();
    //     Long expectedId = recent.getId();
    //     // TODO: Fix this. For some reason, ResponseEntity.ok(i) in the image controller changes the formatting in an odd way.
    //     String[] timestampParts = recent.getTimestamp().toString().split(" ", 2);
    //     String expectedTimestamp = timestampParts[0] + "T09" + timestampParts[1].substring(2, timestampParts[1].length()) + "+0000";

    //     MvcResult result = resultActions.andReturn();
    //     String contentAsString = result.getResponse().getContentAsString();

    //     assertEquals(contentAsString,
    //             "{\"id\":"+expectedId+",\"timestamp\":\""+expectedTimestamp+"\",\"localImageUrl\":null,\"imageUrl\":null,\"telemetry\":null,\"imgMode\":\""+imgMode+"\"}"
    //         );
    // }
}
