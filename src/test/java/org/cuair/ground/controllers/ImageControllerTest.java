package org.cuair.ground.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.ebean.Ebean;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.TimestampDatabaseAccessor;
import org.cuair.ground.models.Image;
import org.cuair.ground.models.Image.ImgMode;
import org.cuair.ground.models.geotag.GimbalOrientation;
import org.cuair.ground.models.geotag.GpsLocation;
import org.cuair.ground.models.geotag.Telemetry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ImageController.class)
@AutoConfigureMockMvc
public class ImageControllerTest {

  private static final String INPUT_DIRECTORY = "src/test/resources/";
  private static final String TEST_FILE = "test.jpg";
  /** A logger */
  private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
  @Value("${plane.image.dir}")
  private String PLANE_IMAGE_DIR;
  private TimestampDatabaseAccessor<Image> imageDao;
  @Autowired
  private MockMvc mvc;

  @Autowired
  private ImageController controller;

  /** Before each test, initialize models and empty tables */
  @Before
  public void setup() {
    imageDao = (TimestampDatabaseAccessor) DAOFactory.getDAO(DAOFactory.ModelDAOType.TIMESTAMP_DATABASE_ACCESSOR, Image.class);

    File imgDir = new File(PLANE_IMAGE_DIR);
    if (!imgDir.exists()) {
      if (!imgDir.mkdirs()) {
        logger.error("Unable to create image directory: " + PLANE_IMAGE_DIR + "\n");
      }
    }
  }

  /** After each test, clean the database */
  @After
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

  /** Tests the GET all call for a database containing images */
  @Test
  public void testGetAll() throws Exception {
    // instantiate models
    List<Image> expected = new ArrayList<>();

    Image i1 = new Image("/some/local/file/url", null, ImgMode.TRACKING);
    i1.setTimestamp(new Timestamp(1234L));
    imageDao.create(i1);
    expected.add(i1);

    Image i2 = new Image("/another/local/file/url", null, ImgMode.FIXED);
    i2.setTimestamp(new Timestamp(2345L));
    imageDao.create(i2);
    expected.add(i2);

    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/image").accept(MediaType.APPLICATION_JSON))
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(status().isOk());

    MvcResult result = resultActions.andReturn();

    // deserialize result
    List<Image> actual = null;
    actual = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
        TypeFactory.defaultInstance().constructCollectionType(List.class, Image.class));

    // tests
    assertEquals(expected, actual);
  }

  /** Tests that geotagging for four corners of an image works */
  @Test
  public void testGeotagFourCorners() throws Exception {
    // instantiate models
    GpsLocation gpsLoc = new GpsLocation(42.4475428000000008, -76.6122976999999992);
    Telemetry expectedTelemetry = new Telemetry(
        gpsLoc,
        221.555125199999992,
        45.0,
        new GimbalOrientation(-30.0, 0.0)
    );
    Image img = new Image("/some/local/file/url", expectedTelemetry, ImgMode.TRACKING);
    img.setTimestamp(new Timestamp(1234L));
    imageDao.create(img);

    // make request and tests
    // TODO: Figure out why this isn't 1
    mvc.perform(MockMvcRequestBuilders.get("/image/geotag/4").accept(MediaType.APPLICATION_JSON))
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
  }

  /** Tests that geotagging for four corners of an image works for all ids */
  @Test
  public void testGeotagFourCornersAllImageIds() throws Exception {
    // instantiate models
    GpsLocation gpsLoc1 = new GpsLocation(42.4475428000000008, -76.6122976999999992);
    Telemetry expectedTelemetry1 = new Telemetry(
        gpsLoc1,
        221.555125199999992,
        45.0,
        new GimbalOrientation(-30.0, 0.0)
    );
    Image img1 = new Image("/some/local/file/url", expectedTelemetry1, ImgMode.TRACKING);
    img1.setTimestamp(new Timestamp(1234L));
    imageDao.create(img1);

    GpsLocation gpsLoc2 = new GpsLocation(42.4475428000000008, -76.6122976999999992);
    Telemetry expectedTelemetry2 = new Telemetry(
        gpsLoc2,
        221.555125199999992,
        45.0,
        new GimbalOrientation(-30.0, 0.0)
    );
    Image img2 = new Image("/some/local/file/url2", expectedTelemetry2, ImgMode.TRACKING);
    img2.setTimestamp(new Timestamp(1234L));
    imageDao.create(img2);

    // make request
    mvc.perform(MockMvcRequestBuilders.get("/image/geotag").accept(MediaType.APPLICATION_JSON))
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(status().isOk())
        .andExpect(content().string(equalTo(
            "{\"1\":{\"topLeft\":\"{\\\"latitude\\\":42.44783148593772,"
                + "\\\"longitude\\\":-76.61235338482004}\",\"topRight\":"
                + "\"{\\\"latitude\\\":42.447501559151746,\\\"longitude\\\":-76.61190790625975}\","
                + "\"bottomLeft\":\"{\\\"latitude\\\":42.447584040848255,"
                + "\\\"longitude\\\":-76.61268749374025}\",\"bottomRight\":\"{"
                + "\\\"latitude\\\":42.44725411406228,\\\"longitude\\\":-76.61224201517996}\","
                + "\"orientation\":\"0.7853981633974483\",\"url\":\"/some/local/file/url\"},"
                + "\"2\":{\"topLeft\":\"{\\\"latitude\\\":42.44783148593772,"
                + "\\\"longitude\\\":-76.61235338482004}\",\"topRight\":"
                + "\"{\\\"latitude\\\":42.447501559151746,\\\"longitude\\\":-76.61190790625975}\","
                + "\"bottomLeft\":\"{\\\"latitude\\\":42.447584040848255,"
                + "\\\"longitude\\\":-76.61268749374025}\",\"bottomRight\":\"{"
                + "\\\"latitude\\\":42.44725411406228,\\\"longitude\\\":-76.61224201517996}\","
                + "\"orientation\":\"0.7853981633974483\",\"url\":\"/some/local/file/url2\"}}"
        )));
  }

  /** Tests the GET by id call */
  @Test
  public void testGet() throws Exception {
    // instantiate models
    GpsLocation gpsLoc = new GpsLocation(42.4475428000000008, -76.6122976999999992);
    Telemetry expectedTelemetry = new Telemetry(
        gpsLoc,
        221.555125199999992,
        45.0,
        new GimbalOrientation(-30.0, 0.0)
    );
    Image expected = new Image("/some/local/file/url", expectedTelemetry, ImgMode.TRACKING);
    expected.setTimestamp(new Timestamp(1234L));
    imageDao.create(expected);

    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/image/3").accept(MediaType.APPLICATION_JSON))
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(status().isOk());

    MvcResult result = resultActions.andReturn();

    // deserialize result
    Image actual = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Image.class);

    // test
    assertEquals(expected, actual);
  }

  /** Tests GET by id call when model with specific id doesn't exist */
  @Test
  public void testGetDoesntExist() throws Exception {
    // make request
    // TODO: Figure out if there is a way to check for no content type set
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/image/1").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent())
        .andExpect(content().string(equalTo("")));

    assertEquals(null, resultActions.andReturn().getResponse().getContentType());
  }

  /** Tests GET most recent call */
  @Test
  public void testGetRecent() throws Exception {
    // instantiate models
    GpsLocation gpsLoc1 = new GpsLocation(42.4475428000000008, -76.6122976999999992);
    Telemetry expectedTelemetry1 = new Telemetry(
        gpsLoc1,
        221.555125199999992,
        45.0,
        new GimbalOrientation(-30.0, 0.0)
    );
    Image i1 = new Image("/some/local/file/url", expectedTelemetry1, ImgMode.TRACKING);
    i1.setTimestamp(new Timestamp(1234L));
    imageDao.create(i1);

    GpsLocation gpsLoc2 = new GpsLocation(42.4475428000000008, -76.6122976999999992);
    Telemetry expectedTelemetry2 = new Telemetry(
        gpsLoc2,
        221.555125199999992,
        45.0,
        new GimbalOrientation(-30.0, 0.0)
    );
    Image i2 = new Image("/another/local/file/url", expectedTelemetry2, ImgMode.TRACKING);
    i2.setTimestamp(new Timestamp(2345L));
    imageDao.create(i2);

    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/image/recent").accept(MediaType.APPLICATION_JSON))
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(status().isOk());

    MvcResult result = resultActions.andReturn();

    // deserialize result
    Image actual = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Image.class);

    // test
    assertEquals(i2, actual);
  }

  /** Tests GET most recent call when no models are in table */
  @Test
  public void testGetRecentDoesntExist() throws Exception {
    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/image/recent").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent())
        .andExpect(content().string(equalTo("")));

    assertEquals(null, resultActions.andReturn().getResponse().getContentType());
  }

  /** Tests POST call to validate its response */
  @Test
  public void testCreate() throws Exception {
    String imageUrl = "src/test/java/org/cuair/ground/controllers/test_images/test_0.jpg";
    InputStream is = new BufferedInputStream(new FileInputStream(imageUrl));
    MockMultipartFile firstFile = new MockMultipartFile("files", "test_0.jpg", "image", is);

    String timestamp = "100000000000006";
    String imgMode = "retract";
    Image expectedImg = new Image("/api/v1/image/file/" + timestamp + ".jpeg", null, ImgMode.FIXED);

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("jsonString", "{\"timestamp\":" + timestamp + ",\"imgMode\":\"" + imgMode + "\"}")
    );
    MvcResult result = resultAction.andReturn();

    Image recent = (Image) controller.getRecent().getBody();

    ResponseEntity asyncedResponseEntity = (ResponseEntity) result.getAsyncResult();
    Image response = (Image) asyncedResponseEntity.getBody();

    ObjectMapper mapper = new ObjectMapper();

    // TODO: Should the content type be non-null?
    assertEquals(null, result.getResponse().getContentType());
    assertEquals(HttpStatus.OK, asyncedResponseEntity.getStatusCode());
    assertEquals(expectedImg, response);
    assertEquals(expectedImg, recent);
  }

  // TODO: add testCreateTooManyParts() when fix json.size() condition

  /** Tests POST call when json part is not valid json. */
  @Test
  public void testCreateWithInvalidJson() throws Exception {
    String imageUrl = "src/test/java/org/cuair/ground/controllers/test_images/test_0.jpg";
    InputStream is = new BufferedInputStream(new FileInputStream(imageUrl));
    MockMultipartFile firstFile = new MockMultipartFile("files", "test_0.jpg", "image", is);

    String timestamp = "100000000000006";
    String imgMode = "retract";

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("jsonString", "\"imgMode\":\"" + imgMode + "\"}")
    );
    MvcResult result = resultAction.andReturn();

    ResponseEntity asyncedResponseEntity = (ResponseEntity) result.getAsyncResult();
    String response = (String) asyncedResponseEntity.getBody();

    assertEquals(null, result.getResponse().getContentType());
    assertEquals(HttpStatus.BAD_REQUEST, asyncedResponseEntity.getStatusCode());
    assert (response.contains("Json part invalid"));
    assertEquals(0, imageDao.getAllIds().size());
  }

  /** Tests POST call when json part does not contain timestamp. */
  @Test
  public void testCreateWithNoTimestamp() throws Exception {
    String imageUrl = "src/test/java/org/cuair/ground/controllers/test_images/test_0.jpg";
    InputStream is = new BufferedInputStream(new FileInputStream(imageUrl));
    MockMultipartFile firstFile = new MockMultipartFile("files", "test_0.jpg", "image", is);

    String timestamp = "100000000000006";
    String imgMode = "retract";

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("jsonString", "{\"imgMode\":\"" + imgMode + "\"}")
    );
    MvcResult result = resultAction.andReturn();

    ResponseEntity asyncedResponseEntity = (ResponseEntity) result.getAsyncResult();
    String response = (String) asyncedResponseEntity.getBody();

    assertEquals(null, result.getResponse().getContentType());
    assertEquals(HttpStatus.BAD_REQUEST, asyncedResponseEntity.getStatusCode());
    assertEquals("Json part must include timestamp field", response);
    assertEquals(0, imageDao.getAllIds().size());
  }

  /** Tests POST call when a custom field name is added to the request body. */
  @Test
  public void testCreateWithCustomFieldName() throws Exception {
    String imageUrl = "src/test/java/org/cuair/ground/controllers/test_images/test_0.jpg";
    InputStream is = new BufferedInputStream(new FileInputStream(imageUrl));
    MockMultipartFile firstFile = new MockMultipartFile("files", "test_0.jpg", "image", is);

    String timestamp = "100000000000006";
    String imgMode = "retract";
    Image expectedImg = new Image("/api/v1/image/file/" + timestamp + ".jpeg", null, ImgMode.FIXED);

    String fieldName = "name";
    String name = "CUAir";
    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("jsonString", "{\"timestamp\":" + timestamp + ",\"imgMode\":\"" + imgMode + "\",\"" + fieldName + "\":\"" + name + "\"}")
    );
    MvcResult result = resultAction.andReturn();

    Image recent = (Image) controller.getRecent().getBody();

    ResponseEntity asyncedResponseEntity = (ResponseEntity) result.getAsyncResult();
    String response = (String) asyncedResponseEntity.getBody();

    assertEquals(null, result.getResponse().getContentType());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, asyncedResponseEntity.getStatusCode());
    assert (response.contains("Unrecognized field \"" + fieldName + "\""));
  }

  /**
   * Tests POST call when image left out of request Also tests if new image that is POSTed is not
   * retrieved from the database
   */
  @Test
  public void testCreateWhenNoImageSent() throws Exception {
    String timestamp = "100000000000006";
    String imgMode = "retract";
    Image expectedImg = new Image("/api/v1/image/file/" + timestamp + ".jpeg", null, ImgMode.FIXED);

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .param("jsonString", "{\"timestamp\":" + timestamp + ",\"imgMode\":\"" + imgMode + "\"}")
    );
    MvcResult result = resultAction.andReturn();

    Image recent = (Image) controller.getRecent().getBody();

    ResponseEntity asyncedResponseEntity = (ResponseEntity) result.getAsyncResult();
    String response = (String) asyncedResponseEntity.getBody();

    assertEquals(null, result.getResponse().getContentType());
    assertEquals(HttpStatus.BAD_REQUEST, asyncedResponseEntity.getStatusCode());
    assertEquals("Missing image file in image POST request", response);
    assertEquals(0, imageDao.getAllIds().size());
  }

  /**
   * Tests POST call when json and image left out of request. Also tests if new image that is POSTed
   * is not retrieved from the database
   */
  @Test
  public void testCreateWithNoParts() throws Exception {
    String imageUrl = "src/test/java/org/cuair/ground/controllers/test_images/test_0.jpg";
    InputStream is = new BufferedInputStream(new FileInputStream(imageUrl));
    MockMultipartFile firstFile = new MockMultipartFile("files", "test_0.jpg", "image", is);

    String timestamp = "100000000000006";
    String imgMode = "retract";
    Image expectedImg = new Image("/api/v1/image/file/" + timestamp + ".jpeg", null, ImgMode.FIXED);

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
    );
    MvcResult result = resultAction.andReturn();

    Image recent = (Image) controller.getRecent().getBody();

    ResponseEntity asyncedResponseEntity = (ResponseEntity) result.getAsyncResult();
    String response = (String) asyncedResponseEntity.getBody();

    assertEquals(null, result.getResponse().getContentType());
    assertEquals(HttpStatus.BAD_REQUEST, asyncedResponseEntity.getStatusCode());
    assertEquals("Missing json in image POST request", response);
    assertEquals(0, imageDao.getAllIds().size());
  }

  /**
   * Tests POST call when json contains id. Also tests if new image that is POSTed is not retrieved
   * from the database
   */
  @Test
  public void testCreateWithId() throws Exception {
    String imageUrl = "src/test/java/org/cuair/ground/controllers/test_images/test_0.jpg";
    InputStream is = new BufferedInputStream(new FileInputStream(imageUrl));
    MockMultipartFile firstFile = new MockMultipartFile("files", "test_0.jpg", "image", is);

    String timestamp = "100000000000006";
    String imgMode = "retract";
    Image expectedImg = new Image("/api/v1/image/file/" + timestamp + ".jpeg", null, ImgMode.FIXED);

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("jsonString", "{\"timestamp\":" + timestamp + ",\"id\":1}")
    );
    MvcResult result = resultAction.andReturn();

    Image recent = (Image) controller.getRecent().getBody();

    ResponseEntity asyncedResponseEntity = (ResponseEntity) result.getAsyncResult();
    String response = (String) asyncedResponseEntity.getBody();

    assertEquals(null, result.getResponse().getContentType());
    assertEquals(HttpStatus.BAD_REQUEST, asyncedResponseEntity.getStatusCode());
    assertEquals("Don't put id in json of image POST request", response);
    assertEquals(0, imageDao.getAllIds().size());
  }

  // TODO: Add reconnection tests w/ clients? Need client code first

  /*
   * Tests POST call when json contains id. Also tests if new image that is POSTed is not retrieved
   * from the database
   */
  @Test
  public void testDummyCreate() throws Exception {
    String imageUrl = "src/test/java/org/cuair/ground/controllers/test_images/test_0.jpg";
    InputStream is = new BufferedInputStream(new FileInputStream(imageUrl));
    MockMultipartFile firstFile = new MockMultipartFile("files", "test_0.jpg", "image", is);

    String timestamp = "100000000000006";
    String imgMode = "retract";
    Image expectedImg = new Image(null, null, ImgMode.FIXED);

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image/dummy")
        .file(firstFile)
        .param("jsonString", "{\"timestamp\":" + timestamp + ",\"imgMode\":\"" + imgMode + "\"}")
    )
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(status().isOk());

    MvcResult result = resultAction.andReturn();
    Image response = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Image.class);

    Image recent = (Image) controller.getRecent().getBody();

    assertEquals(expectedImg, response);
    assertEquals(expectedImg, recent);
  }

  /*
   * Tests POST call when json contains id. Also tests if new image that is POSTed is not retrieved
   * from the database
   */
  @Test
  public void testDummyCreateWithId() throws Exception {
    String imageUrl = "src/test/java/org/cuair/ground/controllers/test_images/test_0.jpg";
    InputStream is = new BufferedInputStream(new FileInputStream(imageUrl));
    MockMultipartFile firstFile = new MockMultipartFile("files", "test_0.jpg", "image", is);

    String timestamp = "100000000000006";
    String imgMode = "retract";
    Image expectedImg = new Image("/api/v1/image/file/" + timestamp + ".jpeg", null, ImgMode.FIXED);

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image/dummy")
        .file(firstFile)
        .param("jsonString", "{\"timestamp\":" + timestamp + ",\"id\":1}")
    )
        .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
        .andExpect(status().is(400));

    MvcResult result = resultAction.andReturn();

    // deserialize result
    String response = result.getResponse().getContentAsString();

    assertEquals("Don't put id in json of image POST request", response);
    assertEquals(0, imageDao.getAllIds().size());
  }
}
