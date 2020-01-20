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

import java.sql.Timestamp;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.TimestampDatabaseAccessor;
import org.cuair.ground.daos.DatabaseAccessor;
import org.cuair.ground.models.Image;
import org.cuair.ground.models.Image.ImgMode;
import org.cuair.ground.models.geotag.Telemetry;
import org.cuair.ground.models.geotag.GpsLocation;
import org.cuair.ground.models.geotag.GimbalOrientation;
import org.cuair.ground.models.exceptions.InvalidGpsLocationException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import io.ebean.Ebean;
import org.apache.commons.io.FileUtils;
import org.springframework.http.*;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ImageController.class)
@AutoConfigureMockMvc
public class ImageControllerTest {

    private static final String INPUT_DIRECTORY = "src/test/resources/";
    private static final String TEST_FILE = "test.jpg";

    @Value("${plane.image.dir}") private String PLANE_IMAGE_DIR;

    private TimestampDatabaseAccessor<Image> imageDao;
    private DatabaseAccessor<Telemetry> telemetryDao;

    /** A logger */
    private static final Logger logger = LoggerFactory.getLogger(ImageController.class);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ImageController controller;

    /** Before each test, initialize models and empty tables */
    @Before
    public void setup() {
        imageDao = (TimestampDatabaseAccessor) DAOFactory.getDAO(DAOFactory.ModelDAOType.TIMESTAMP_DATABASE_ACCESSOR, Image.class);
        telemetryDao = (DatabaseAccessor) DAOFactory.getDAO(DAOFactory.ModelDAOType.DATABASE_ACCESSOR, Telemetry.class);

        File imgDir = new File(PLANE_IMAGE_DIR);
        if (!imgDir.exists()) {
            if (!imgDir.mkdirs()) {
                logger.error("Unable to create image directory: " + PLANE_IMAGE_DIR + "\n");
            }
        }
    }

    // TODO: Implement client code
    // @After
    // public void clearQueue() {
    //     try {
    //         Field field = ImageClient.class.getDeclaredField("gimbalViewRequests");
    //         field.setAccessible(true);
    //         Queue<Image> queue = (Queue<Image>) field.get(imageClient);
    //         queue.clear();

    //         field = ImageClient.class.getDeclaredField("autopilotRequests");
    //         field.setAccessible(true);
    //         queue = (Queue<Image>) field.get(imageClient);
    //         queue.clear();
    //     } catch (NoSuchFieldException | IllegalAccessException e) {
    //         e.printStackTrace();
    //     }
    // }

    public void cleanDb() {
        List<Image> images = Ebean.find(Image.class).findList();
        Ebean.beginTransaction();
        Ebean.deleteAll(images);
        Ebean.commitTransaction();

        try {
            FileUtils.cleanDirectory(FileUtils.getFile(PLANE_IMAGE_DIR));
        } catch (IOException e) {
            logger.error(PLANE_IMAGE_DIR + " could not be cleared\n");
        }
    }

    /** Tests the controller exists */
    @Test
    public void loads() {
        assertThat(controller).isNotNull();
    }

    /** Tests the GET all call for an empty database */
    @Test
    public void testGetAllEmpty() throws Exception {
        ArrayList<Image> list = new ArrayList();
        mvc.perform(MockMvcRequestBuilders.get("/image").accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(list.toString())));
    }

    /** Tests that geotagging for four corners of an image works */
    @Test
    public void testGeotagFourCorners() throws Exception {
        // instantiate models
        GpsLocation gpsLoc = null;
        try {
            gpsLoc = new GpsLocation(42.4475428000000008, -76.6122976999999992);
        } catch (InvalidGpsLocationException e) {
            logger.error("Invalid GpsLocation\n");
        }

        Telemetry expectedTelemetry = new Telemetry(
                    gpsLoc,
                    221.555125199999992,
                    45.0,
                    new GimbalOrientation(-30.0, 0.0)
                );
        telemetryDao.create(expectedTelemetry);

        Image img = new Image("/some/local/file/url", expectedTelemetry, ImgMode.TRACKING);
        img.setTimestamp(new Timestamp(1234L));
        imageDao.create(img);

        // make request and tests
        mvc.perform(MockMvcRequestBuilders.get("/image/geotag/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(
                        "{\"topLeft\":\"{\\\"latitude\\\":42.44783148593772,"
                            + "\\\"longitude\\\":-76.61235338482004}\",\"topRight\":"
                            + "\"{\\\"latitude\\\":42.447501559151746,\\\"longitude\\\":-76.61190790625975}\","
                            + "\"bottomLeft\":\"{\\\"latitude\\\":42.447584040848255,"
                            + "\\\"longitude\\\":-76.61268749374025}\",\"bottomRight\":\"{"
                            + "\\\"latitude\\\":42.44725411406228,\\\"longitude\\\":-76.61224201517996}\","
                            + "\"orientation\":\"0.7853981633974483\",\"url\":\"/some/local/file/url\"}"
                    )));

        // // instantiate models
        // TelemetryData expectedTelemetry2 = new TelemetryData(null, null, null);
        // telemetryDataDao.create(expectedTelemetry2);

        // Image img2 = new Image("/some/local/file/url2", expectedTelemetry2, null);
        // img2.setTimestamp(new Timestamp(1234L));
        // imageDao.create(img2);

        // // make request
        // Result result2 = route(org.cuair.ground.controllers.routes.ImageController.getGeotagCoordinates(2));
        // // tests
        // assertEquals(NO_CONTENT, result2.status());
        // clear database
        cleanDb();
    }

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
