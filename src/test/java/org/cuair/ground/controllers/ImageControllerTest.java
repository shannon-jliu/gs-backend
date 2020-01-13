package org.cuair.ground.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.mock.web.MockMultipartFile;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.util.List;
import java.util.ArrayList;
import java.io.*;
import org.cuair.ground.models.Image;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ImageController.class)
@AutoConfigureMockMvc
public class ImageControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ImageController controller;

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
        MockMultipartFile firstFile = new MockMultipartFile("data", "test_0.jpg", "image", is);

        mvc.perform(MockMvcRequestBuilders.multipart("/image")
                .file(firstFile)
                .param("jsonString", "{\"timestamp\":10006,\"imgMode\":\"retract\"}")
            )
                .andExpect(status().isOk());
                // .andExpect(content().string(equalTo(list.toString())));
    }
}
