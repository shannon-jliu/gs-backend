package org.cuair.ground.controllers;

import static org.hamcrest.Matchers.equalTo;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
// import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.*;
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
// import org.springframework.http.ResponseEntity;
import org.springframework.http.*;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ImageController.class)
@AutoConfigureMockMvc
public class ImageControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ImageController controller;

    private void clearDB(String timestamp) {
        List<Image> images = Ebean.find(Image.class).findList();
        Ebean.beginTransaction();
        Ebean.deleteAll(images);
        Ebean.commitTransaction();

        try {
            FileUtils.forceDelete(FileUtils.getFile("images/" + timestamp + ".jpeg"));
        } catch (IOException e) {
            System.out.println("File " + timestamp + ".jpeg could not be deleted.");
        }
    }

    @Test
    public void loads() throws Exception {
        assertThat(controller).isNotNull();
    }

    @Test
    public void getAll() throws Exception {
        ArrayList<Image> list = new ArrayList();
        mvc.perform(MockMvcRequestBuilders.get("/image").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(list.toString())));
    }

    @Test
    public void getAllIds() throws Exception {
        ArrayList<Image> list = new ArrayList();
        mvc.perform(MockMvcRequestBuilders.get("/image/id").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(list.toString())));
    }

    @Test
    public void getRecent() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/image/recent").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(equalTo("")));
    }

    @Test
    public void get() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/image/0").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(equalTo("")));
    }

    @Test
    public void getFile() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/image/file/1.jpeg").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(equalTo("")));
    }

    @Test
    public void create() throws Exception {
        InputStream is = new BufferedInputStream(new FileInputStream("src/test/java/org/cuair/ground/controllers/test_images/test_0.jpg"));
        MockMultipartFile firstFile = new MockMultipartFile("files", "test_0.jpg", "image", is);

        String timestamp = "100000000000006";
        String imgMode = "retract";

        MvcResult result = mvc.perform(MockMvcRequestBuilders.multipart("/image")
                            .file(firstFile)
                            .param("jsonString", "{\"timestamp\":"+timestamp+",\"imgMode\":\""+imgMode+"\"}")
                        ).andReturn();

        ResponseEntity asyncedResponseEntity = (ResponseEntity) result.getAsyncResult();
        Image i = (Image) asyncedResponseEntity.getBody();

        ObjectMapper mapper = new ObjectMapper();
        String imageAsString = mapper.writeValueAsString(i);

        assertEquals(imageAsString,
                "{\"id\":1,\"timestamp\":"+timestamp+",\"localImageUrl\":\"images/"+timestamp+".jpeg\",\"imageUrl\":\"/api/v1/image/file/"+timestamp+".jpeg\",\"telemetry\":null,\"imgMode\":\""+imgMode+"\"}"
            );

        clearDB(timestamp);
    }
}
