package org.cuair.ground.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
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
import org.cuair.ground.daos.ImageDatabaseAccessor;
import org.cuair.ground.models.Image;
import org.cuair.ground.models.Image.ImgMode;
import org.cuair.ground.models.geotag.GimbalOrientation;
import org.cuair.ground.models.geotag.GpsLocation;
import org.cuair.ground.models.geotag.Telemetry;
import org.cuair.ground.util.Flags;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
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
  private static final Logger logger = LoggerFactory.getLogger(ImageController.class);
  private String TEST_IMAGE_DIR = Flags.TEST_IMAGE_DIR;
  private String PLANE_IMAGE_DIR = Flags.PLANE_IMAGE_DIR;
  private ImageDatabaseAccessor imageDao;
  @Autowired
  private MockMvc mvc;

  @Autowired
  private ImageController controller;

  private String imageUrl;
  private InputStream is;
  private MockMultipartFile firstFile;
  private String timestamp;
  private String imgMode;
  private double latitude;
  private double longitude;
  private GpsLocation gpsLoc;
  private double altitude;
  private double planeYaw;
  private double pitch;
  private double roll;
  private Telemetry expectedTelemetry;
  private double[] fov;
  private Image expectedImg;
  private JSONObject gpsObj;
  private JSONObject gimOrtObj;
  private JSONObject telemObj;
  private JSONObject jsonObj;

  /** Before each test, initialize models and empty tables */
  @Before
  public void setup() throws Exception {
    imageDao = (ImageDatabaseAccessor) DAOFactory
        .getDAO(DAOFactory.ModellessDAOType.IMAGE_DATABASE_ACCESSOR);

    File imgDir = new File(PLANE_IMAGE_DIR);
    if (!imgDir.exists()) {
      if (!imgDir.mkdirs()) {
        logger.error("Unable to create image directory: " + PLANE_IMAGE_DIR + "\n");
      }
    }

    imgDir = new File(TEST_IMAGE_DIR);
    if (!imgDir.exists()) {
      if (!imgDir.mkdirs()) {
        logger.error("Unable to create test image directory: " + TEST_IMAGE_DIR + "\n");
      }
    }

    imageUrl = "src/test/java/org/cuair/ground/controllers/test_images/test_0.jpg";
    is = new BufferedInputStream(new FileInputStream(imageUrl));
    firstFile = new MockMultipartFile("files", "test_0.jpg", "image", is);

    // instantiate models
    timestamp = "100000000000006";
    imgMode = "fixed";
    latitude = 42.4475428000000008;
    longitude = -76.6122976999999992;
    gpsLoc = new GpsLocation(latitude, longitude);
    altitude = 221.555125199999992;
    planeYaw = 45.0;
    pitch = -30.0;
    roll = 0.0;
    expectedTelemetry = new Telemetry(
        gpsLoc,
        altitude,
        planeYaw,
        new GimbalOrientation(pitch, roll)
    );
    fov = new double[] {60.0, 60.0};
    expectedImg =
        new Image("/api/v1/image/file/" + timestamp + ".jpeg", expectedTelemetry, ImgMode.FIXED,
            false, false, fov);

    // Create the json param
    // gps object
    gpsObj = new JSONObject();
    gpsObj.put("latitude", latitude);
    gpsObj.put("longitude", longitude);
    // gimbal orientation object
    gimOrtObj = new JSONObject();
    gimOrtObj.put("roll", roll);
    gimOrtObj.put("pitch", pitch);
    // telemetry object
    telemObj = new JSONObject();
    telemObj.put("altitude", altitude);
    telemObj.put("planeYaw", planeYaw);
    telemObj.put("gps", gpsObj);
    telemObj.put("gimOrt", gimOrtObj);
    // main json object
    jsonObj = new JSONObject();
    jsonObj.put("timestamp", timestamp);
    jsonObj.put("imgMode", imgMode);
    jsonObj.put("fov", fov);
    jsonObj.put("telemetry", telemObj);
  }

  /** After each test, clean the database */
  @After
  public void cleanDb() {
    String sql = "TRUNCATE image RESTART IDENTITY CASCADE";
    Ebean.createSqlUpdate(sql).execute();

    // Clear image directory
    try {
      FileUtils.cleanDirectory(FileUtils.getFile(PLANE_IMAGE_DIR));
    } catch (IOException e) {
      logger.error(PLANE_IMAGE_DIR + " could not be cleared\n");
    }
  }

  /** Tests the GET all after id call for an empty database */
  @Test
  public void testGetAllAfterIdEmpty() throws Exception {
    ArrayList<Image> list = new ArrayList();
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/image/all/0")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(404))
        .andExpect(content().string(equalTo("")));
    assertNull(resultActions.andReturn().getResponse().getContentType());
  }

  /** Tests the GET all after id call for a database containing images */
  @Test
  public void testGetAllAfterId() throws Exception {
    // instantiate models
    List<Image> expected = new ArrayList<>();

    GpsLocation gpsLoc1 = new GpsLocation(42.4475428000000008, -76.6122976999999992);
    Telemetry expectedTelemetry1 = new Telemetry(
        gpsLoc1,
        221.555125199999992,
        45.0,
        new GimbalOrientation(-30.0, 0.0)
    );
    Image i1 =
        new Image("/some/local/file/url", expectedTelemetry1, ImgMode.TRACKING, false, false, 0.0);
    i1.setTimestamp(new Timestamp(1234L));
    imageDao.create(i1);
    expected.add(i1);

    Telemetry expectedTelemetry2 = new Telemetry(
        gpsLoc1,
        221.555125199999992,
        46.0,
        new GimbalOrientation(-30.0, 0.0)
    );
    Image i2 =
        new Image("/another/local/file/url", expectedTelemetry2, ImgMode.FIXED, false, false, 0.0);
    i2.setTimestamp(new Timestamp(2345L));
    imageDao.create(i2);
    expected.add(i2);

    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/image/all/0")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(status().isOk());

    MvcResult result = resultActions.andReturn();

    // deserialize result
    List<Image> actual = null;
    actual = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
        TypeFactory.defaultInstance().constructCollectionType(List.class, Image.class));

    assertEquals(expected, actual);
  }

  /** Tests the GET all after the first id call for a database containing images */
  @Test
  public void testGetAllAfterIdFirst() throws Exception {
    // instantiate models
    List<Image> expected = new ArrayList<>();

    GpsLocation gpsLoc1 = new GpsLocation(42.4475428000000008, -76.6122976999999992);
    Telemetry expectedTelemetry1 = new Telemetry(
        gpsLoc1,
        221.555125199999992,
        45.0,
        new GimbalOrientation(-30.0, 0.0)
    );
    Image i1 =
        new Image("/some/local/file/url", expectedTelemetry1, ImgMode.TRACKING, false, false, 0.0);
    i1.setTimestamp(new Timestamp(1234L));
    imageDao.create(i1);

    Telemetry expectedTelemetry2 = new Telemetry(
        gpsLoc1,
        221.555125199999992,
        46.0,
        new GimbalOrientation(-30.0, 0.0)
    );
    Image i2 =
        new Image("/another/local/file/url", expectedTelemetry2, ImgMode.FIXED, false, false, 0.0);
    i2.setTimestamp(new Timestamp(2345L));
    imageDao.create(i2);
    expected.add(i2);

    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/image/all/1")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(status().isOk());

    MvcResult result = resultActions.andReturn();

    // deserialize result
    List<Image> actual = null;
    actual = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
        TypeFactory.defaultInstance().constructCollectionType(List.class, Image.class));

    assertEquals(expected, actual);
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
    Image expected =
        new Image("/some/local/file/url", expectedTelemetry, ImgMode.TRACKING, false, false, 0.0);
    expected.setTimestamp(new Timestamp(1234L));
    boolean created = imageDao.create(expected);
    assertTrue(created);

    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/image/1")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(status().isOk());
    MvcResult result = resultActions.andReturn();

    // deserialize result
    Image actual =
        new ObjectMapper().readValue(result.getResponse().getContentAsString(), Image.class);

    assertEquals(expected, actual);
  }

  /** Tests GET by id call when model with specific id doesn't exist */
  @Test
  public void testGetDoesntExist() throws Exception {
    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/image/1")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(404))
        .andExpect(content().string(equalTo("")));

    assertNull(resultActions.andReturn().getResponse().getContentType());
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
    Image i1 =
        new Image("/some/local/file/url", expectedTelemetry1, ImgMode.TRACKING, false, false, 0.0);
    i1.setTimestamp(new Timestamp(1234L));
    imageDao.create(i1);

    GpsLocation gpsLoc2 = new GpsLocation(42.4475428000000008, -76.6122976999999992);
    Telemetry expectedTelemetry2 = new Telemetry(
        gpsLoc2,
        221.555125199999992,
        45.0,
        new GimbalOrientation(-30.0, 0.0)
    );
    Image i2 =
        new Image("/another/local/file/url", expectedTelemetry2, ImgMode.TRACKING, false, false,
            0.0);
    i2.setTimestamp(new Timestamp(2345L));
    imageDao.create(i2);

    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/image/recent")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(status().isOk());

    MvcResult result = resultActions.andReturn();

    // deserialize result
    Image actual =
        new ObjectMapper().readValue(result.getResponse().getContentAsString(), Image.class);

    assertEquals(i2, actual);
  }

  /** Tests GET most recent call when no models are in table */
  @Test
  public void testGetRecentDoesntExist() throws Exception {
    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/image/recent")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().is(404))
        .andExpect(content().string(equalTo("")));

    assertNull(resultActions.andReturn().getResponse().getContentType());
  }

  /** Tests POST call to validate its response */
  @Test
  public void testCreate() throws Exception {
    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("json", jsonObj.toString()));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();
    Image responseImage =
        new ObjectMapper().readValue(result.getResponse().getContentAsString(), Image.class);
    Image recent = (Image) controller.getRecent().getBody();

    assertEquals("application/json", response.getContentType());
    assertEquals(200, response.getStatus());
    assertEquals(expectedImg, responseImage);
    assertEquals(expectedImg, recent);
  }

  /** Tests POST call when json part is not valid json. */
  @Test
  public void testCreateWithInvalidJson() throws Exception {
    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("json", "\"imgMode\":\"" + imgMode + "\"}"));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    assertEquals("text/plain;charset=UTF-8", response.getContentType());
    assertEquals(400, response.getStatus());
    assertTrue(response.getContentAsString().contains("Json part invalid"));
    assertEquals(0, imageDao.getAllIds().size());
  }

  /** Tests POST call when there are too many json parts. */
  @Test
  public void testCreateTooManyParts() throws Exception {
    // instantiate models
    String extraField = "hello there";

    // Edit json fields
    jsonObj.put("extraField", extraField);

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("json", jsonObj.toString()));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    assertEquals("text/plain;charset=UTF-8", response.getContentType());
    assertEquals(400, response.getStatus());
    assertTrue(response.getContentAsString().contains("Json part contains invalid field"));
    assertEquals(0, imageDao.getAllIds().size());
  }

  /** Tests POST call when there are too many json parts. */
  @Test
  public void testCreateWithId() throws Exception {
    // instantiate models
    int id = 1;

    // Edit json fields
    jsonObj.put("id", id);

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("json", jsonObj.toString()));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    assertEquals("text/plain;charset=UTF-8", response.getContentType());
    assertEquals(400, response.getStatus());
    assertTrue(
        response.getContentAsString().contains("Don't put id in json of image POST request"));
    assertEquals(0, imageDao.getAllIds().size());
  }

  /** Tests POST call when json part does not contain timestamp. */
  @Test
  public void testCreateWithNoTimestamp() throws Exception {
    // Edit json fields
    jsonObj.remove("timestamp");

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("json", jsonObj.toString()));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    assertEquals("text/plain;charset=UTF-8", response.getContentType());
    assertEquals(400, response.getStatus());
    assertTrue(response.getContentAsString().contains("Json part must include timestamp field"));
    assertEquals(0, imageDao.getAllIds().size());
  }

  /** Tests POST call when json part does not contain fov. */
  @Test
  public void testCreateWithNoFov() throws Exception {
    // Edit json fields
    jsonObj.remove("fov");

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("json", jsonObj.toString()));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    assertEquals("text/plain;charset=UTF-8", response.getContentType());
    assertEquals(400, response.getStatus());
    assertTrue(response.getContentAsString().contains("Json part must include fov field"));
    assertEquals(0, imageDao.getAllIds().size());
  }

  /** Tests POST call when json part does not contain imgMode. */
  @Test
  public void testCreateWithNoImgMode() throws Exception {
    // Edit json fields
    jsonObj.remove("imgMode");

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("json", jsonObj.toString()));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    assertEquals("text/plain;charset=UTF-8", response.getContentType());
    assertEquals(400, response.getStatus());
    assertTrue(response.getContentAsString().contains("Json part must include imgMode"));
    assertEquals(0, imageDao.getAllIds().size());
  }

  /** Tests POST call when json part does not contain telemetry. */
  @Test
  public void testCreateWithNoTelemetry() throws Exception {
    // Edit json fields
    jsonObj.remove("telemetry");

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("json", jsonObj.toString()));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    assertEquals("text/plain;charset=UTF-8", response.getContentType());
    assertEquals(400, response.getStatus());
    assertTrue(response.getContentAsString().contains("Json part must include telemetry"));
    assertEquals(0, imageDao.getAllIds().size());
  }

  /** Tests POST call when json part does not contain gps within telemetry. */
  @Test
  public void testCreateWithNoGps() throws Exception {
    // Edit json fields
    telemObj.remove("gps");

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("json", jsonObj.toString()));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    assertEquals("text/plain;charset=UTF-8", response.getContentType());
    assertEquals(400, response.getStatus());
    assertTrue(
        response.getContentAsString().contains("Json part must include gps within telemetry"));
    assertEquals(0, imageDao.getAllIds().size());
  }

  /** Tests POST call when json part does not contain latitude within gps within telemetry. */
  @Test
  public void testCreateWithNoLatitude() throws Exception {
    // Edit json fields
    gpsObj.remove("latitude");

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("json", jsonObj.toString()));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    assertEquals("text/plain;charset=UTF-8", response.getContentType());
    assertEquals(400, response.getStatus());
    assertTrue(
        response.getContentAsString().contains("Json part must include latitude within gps"));
    assertEquals(0, imageDao.getAllIds().size());
  }

  /** Tests POST call when json part does not contain longitude within gps within telemetry. */
  @Test
  public void testCreateWithNoLongitude() throws Exception {
    // Edit json fields
    gpsObj.remove("longitude");

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("json", jsonObj.toString()));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    assertEquals("text/plain;charset=UTF-8", response.getContentType());
    assertEquals(400, response.getStatus());
    assertTrue(
        response.getContentAsString().contains("Json part must include longitude within gps"));
    assertEquals(0, imageDao.getAllIds().size());
  }

  /** Tests POST call when json part does not contain gimOrt within telemetry. */
  @Test
  public void testCreateWithNoGimOrt() throws Exception {
    // Edit json fields
    telemObj.remove("gimOrt");

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("json", jsonObj.toString()));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    assertEquals("text/plain;charset=UTF-8", response.getContentType());
    assertEquals(400, response.getStatus());
    assertTrue(
        response.getContentAsString().contains("Json part must include gimOrt within telemetry"));
    assertEquals(0, imageDao.getAllIds().size());
  }

  /** Tests POST call when json part does not contain pitch within gimOrt within telemetry. */
  @Test
  public void testCreateWithNoPitch() throws Exception {
    // Edit json fields
    gimOrtObj.remove("pitch");

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("json", jsonObj.toString()));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    assertEquals("text/plain;charset=UTF-8", response.getContentType());
    assertEquals(400, response.getStatus());
    assertTrue(
        response.getContentAsString().contains("Json part must include pitch within gimOrt"));
    assertEquals(0, imageDao.getAllIds().size());
  }

  /** Tests POST call when json part does not contain roll within gimOrt within telemetry. */
  @Test
  public void testCreateWithNoRoll() throws Exception {
    // Edit json fields
    gimOrtObj.remove("roll");

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("json", jsonObj.toString()));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    assertEquals("text/plain;charset=UTF-8", response.getContentType());
    assertEquals(400, response.getStatus());
    assertTrue(response.getContentAsString().contains("Json part must include roll within gimOrt"));
    assertEquals(0, imageDao.getAllIds().size());
  }

  /** Tests POST call when json part does not contain altitude within telemetry. */
  @Test
  public void testCreateWithNoAltitude() throws Exception {
    // Edit json fields
    telemObj.remove("altitude");

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("json", jsonObj.toString()));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    assertEquals("text/plain;charset=UTF-8", response.getContentType());
    assertEquals(400, response.getStatus());
    assertTrue(
        response.getContentAsString().contains("Json part must include altitude within telemetry"));
    assertEquals(0, imageDao.getAllIds().size());
  }

  /** Tests POST call when json part does not contain planeYaw within telemetry. */
  @Test
  public void testCreateWithNoPlaneYaw() throws Exception {
    // Edit json fields
    telemObj.remove("planeYaw");

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile)
        .param("json", jsonObj.toString()));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    assertEquals("text/plain;charset=UTF-8", response.getContentType());
    assertEquals(400, response.getStatus());
    assertTrue(
        response.getContentAsString().contains("Json part must include planeYaw within telemetry"));
    assertEquals(0, imageDao.getAllIds().size());
  }

  /**
   * Tests POST call when image left out of request. Also tests if new image that is POSTed is not
   * retrieved from the database
   */
  @Test
  public void testCreateWhenNoImageSent() throws Exception {
    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .param("json", jsonObj.toString()));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    Image recent = (Image) controller.getRecent().getBody();

    assertNull(response.getContentType());
    assertEquals(400, response.getStatus());
    assertEquals("Required request part 'files' is not present", response.getErrorMessage());
    assertEquals(0, imageDao.getAllIds().size());
  }

  /**
   * Tests POST call when json left out of request. Also tests if new image that is POSTed
   * is not retrieved from the database
   */
  @Test
  public void testCreateWithNoParts() throws Exception {
    String imageUrl = "src/test/java/org/cuair/ground/controllers/test_images/test_0.jpg";
    InputStream is = new BufferedInputStream(new FileInputStream(imageUrl));
    MockMultipartFile firstFile = new MockMultipartFile("files", "test_0.jpg", "image", is);

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.multipart("/image")
        .file(firstFile));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    assertNull(response.getContentType());
    assertEquals(400, response.getStatus());
    assertEquals("Required String parameter 'json' is not present", response.getErrorMessage());
    assertEquals(0, imageDao.getAllIds().size());
  }

  // TODO: Add reconnection tests w/ clients? Need client code first

  /*
   * Tests POST call when json contains id. Also tests if new image that is POSTed is not retrieved
   * from the database
   */
  @Test
  public void testDummyCreate() throws Exception {
    // instantiate models
    String timestamp = "100000000000006";
    String imgMode = "fixed";
    Double latitude = null;
    Double longitude = null;
    GpsLocation gpsLoc = new GpsLocation();
    double altitude = 221.555125199999992;
    double planeYaw = 45.0;
    Double pitch = null;
    Double roll = null;
    Telemetry expectedTelemetry = new Telemetry(
        gpsLoc,
        altitude,
        planeYaw,
        new GimbalOrientation()
    );
    double[] fov = new double[] {60.0, 60.0};
    Image expectedImg =
        new Image("/api/v1/image/dummy", expectedTelemetry, ImgMode.FIXED, false, false, fov);
    String bodyString = new ObjectMapper().writeValueAsString(expectedImg);

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.post("/image/dummy")
        .contentType(MediaType.APPLICATION_JSON)
        .content(bodyString));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    Image responseImage = new ObjectMapper().readValue(response.getContentAsString(), Image.class);
    Image recent = (Image) controller.getRecent().getBody();

    assertEquals("application/json", response.getContentType());
    assertEquals(200, response.getStatus());
    assertEquals(expectedImg, responseImage);
    assertEquals(expectedImg, recent);
  }

  /*
   * Tests POST call when json contains id. Also tests if new image that is POSTed is not retrieved
   * from the database
   */
  @Test
  public void testDummyCreateWithId() throws Exception {
    // instantiate models
    String timestamp = "100000000000006";
    String imgMode = "fixed";
    Double latitude = null;
    Double longitude = null;
    GpsLocation gpsLoc = new GpsLocation();
    double altitude = 221.555125199999992;
    double planeYaw = 45.0;
    Double pitch = null;
    Double roll = null;
    Telemetry expectedTelemetry = new Telemetry(
        gpsLoc,
        altitude,
        planeYaw,
        new GimbalOrientation()
    );
    double[] fov = new double[] {60.0, 60.0};
    Image expectedImg =
        new Image("/api/v1/image/dummy", expectedTelemetry, ImgMode.FIXED, false, false, fov);

    // To add an id to the image
    imageDao.create(expectedImg);
    // To clear the image table so assertEquals(0, imageDao.getAllIds().size()) makes sense/is correct
    String sql = "TRUNCATE image RESTART IDENTITY CASCADE";
    Ebean.createSqlUpdate(sql).execute();

    String bodyString = new ObjectMapper().writeValueAsString(expectedImg);

    ResultActions resultAction = mvc.perform(MockMvcRequestBuilders.post("/image/dummy")
        .contentType(MediaType.APPLICATION_JSON)
        .content(bodyString));
    MvcResult result = resultAction.andReturn();
    MockHttpServletResponse response = result.getResponse();

    assertEquals("text/plain;charset=UTF-8", response.getContentType());
    assertEquals(400, response.getStatus());
    assertEquals("Don't put id in json of image POST request", response.getContentAsString());
    assertEquals(0, imageDao.getAllIds().size());
  }
}
