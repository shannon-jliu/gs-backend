package org.cuair.ground.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.reflect.Whitebox.setInternalState;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.ebean.DB;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.cuair.ground.daos.AssignmentDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.ImageDatabaseAccessor;
import org.cuair.ground.daos.ODLCUserDatabaseAccessor;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.Image;
import org.cuair.ground.models.Image.ImgMode;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.exceptions.InvalidGpsLocationException;
import org.cuair.ground.models.geotag.GimbalOrientation;
import org.cuair.ground.models.geotag.GpsLocation;
import org.cuair.ground.models.geotag.Telemetry;
import org.cuair.ground.util.Flags;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AssignmentController.class)
@AutoConfigureMockMvc
public class AssignmentControllerTest {
  private AssignmentDatabaseAccessor assignmentDao;
  private ImageDatabaseAccessor imageDao;
  private ODLCUserDatabaseAccessor odlcUserDao;
  @Autowired
  private MockMvc mvc;

  @Autowired
  private AssignmentController controller;

  private ODLCUser user1;
  private ODLCUser user2;
  private Image image1;
  private Image image2;
  private Assignment assignment1;
  private Assignment assignment2;
  private List<Assignment> expected1;
  private List<Assignment> expected2;

  /** Disables auth flag in AssignmentController */
  private void setUsersEnabled(boolean flag) {
    setInternalState(Flags.class, "USERS_ENABLED", flag);
    setInternalState(AssignmentController.class, "USERS_ENABLED", flag);
  }

  /** Before each test, initialize models and empty tables */
  @Before
  public void setup() throws InvalidGpsLocationException {
    setUsersEnabled(true);
    assignmentDao = (AssignmentDatabaseAccessor) DAOFactory
        .getDAO(DAOFactory.ModellessDAOType.ASSIGNMENT_DATABASE_ACCESSOR);
    imageDao = (ImageDatabaseAccessor) DAOFactory
        .getDAO(DAOFactory.ModellessDAOType.IMAGE_DATABASE_ACCESSOR);
    odlcUserDao = (ODLCUserDatabaseAccessor) DAOFactory
        .getDAO(DAOFactory.ModellessDAOType.ODLCUSER_DATABASE_ACCESSOR);

    // Create models
    user1 = new ODLCUser("Obi-Wan", "localhost", ODLCUser.UserType.MDLCTAGGER);
    odlcUserDao.create(user1);
    user2 = new ODLCUser("Obi-Wan 2.0", "localhost", ODLCUser.UserType.MDLCTAGGER);
    odlcUserDao.create(user2);
    GpsLocation gpsLoc = new GpsLocation(42.4475428000000008, -76.6122976999999992);
    Telemetry telem1 = new Telemetry(
        gpsLoc,
        221.555125199999992,
        45.0,
        new GimbalOrientation(-30.0, 0.0)
    );
    Telemetry telem2 = new Telemetry(
        gpsLoc,
        221.555125199999992,
        45.0,
        new GimbalOrientation(-30.0, 0.0)
    );
    Timestamp t2 = new java.sql.Timestamp(new java.util.Date().getTime());
    Timestamp t1 = new java.sql.Timestamp(new java.util.Date().getTime());
    image1 = new Image(t1.getTime() + ".jpg", telem1, ImgMode.TRACKING, false, false, 0.0);
    image1.setTimestamp(t1);
    imageDao.create(image1);
    image2 = new Image(t2.getTime() + ".jpg", telem2, ImgMode.TRACKING, false, false, 0.0);
    image2.setTimestamp(t2);
    imageDao.create(image2);
    assignment1 = new Assignment(image1, user1);
    assignment1.setTimestamp(new Timestamp(new Date().getTime()));
    assignment2 = new Assignment(image2, user2);
    assignment2.setTimestamp(new Timestamp(new Date().getTime()));
    expected1 = new ArrayList<>();
    expected2 = new ArrayList<>();
  }

  /** After each test, clean the database */
  @After
  public void cleanDb() {
    String[] tables = {"assignment", "image", "odlcuser"};
    for (String table : tables) {
      String sql = "TRUNCATE " + table + " RESTART IDENTITY CASCADE";
      DB.createSqlUpdate(sql).execute();
    }
  }

  /** Tests the controller exists */
  @Test
  public void loads() {
    assertNotNull(controller);
  }

  /** Tests nothing is returned */
  @Test
  public void testGetEmpty() throws Exception {
    // make request and test
    ResultActions resultActions =
        mvc.perform(MockMvcRequestBuilders.get("/assignment/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is(404));
    assertNull(resultActions.andReturn().getResponse().getContentType());
  }

  /** Tests the correct assignment is returned for a provided id */
  @Test
  public void testGet() throws Exception {
    // instantiate models
    assignmentDao.create(assignment1);

    // make request
    ResultActions resultActions =
        mvc.perform(MockMvcRequestBuilders.get("/assignment/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(status().isOk());

    MvcResult result = resultActions.andReturn();

    // deserialize result
    Assignment returned =
        new ObjectMapper().readValue(result.getResponse().getContentAsString(), Assignment.class);

    // tests
    assertEquals(assignment1, returned);
  }

  /** Tests that no assignments are returned when no images are in the db */
  @Test
  public void testCreateWorkUsersNotEnabledNoImages() throws Exception {
    setUsersEnabled(false);

    // clear image table
    DB.createSqlUpdate("TRUNCATE image RESTART IDENTITY CASCADE").execute();

    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders
        .post("/assignment/work")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    MockHttpServletResponse response = resultActions.andReturn().getResponse();

    // tests
    assertNull(response.getContentType());
    assertEquals("", response.getContentAsString());
  }

  /** Tests that no assignments are returned when no images are in the db */
  @Test
  public void testCreateWorkUsersEnabledNoImages() throws Exception {
    // clear image table
    DB.createSqlUpdate("TRUNCATE image RESTART IDENTITY CASCADE").execute();

    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders
        .post("/assignment/work")
        .header("Username", user1.getUsername())
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    MockHttpServletResponse response = resultActions.andReturn().getResponse();

    // tests
    assertNull(response.getContentType());
    assertEquals("", response.getContentAsString());
  }

  /** Tests that the assignment is successfully created and returned when users are not enabled */
  @Test
  public void testCreateWorkUsersNotEnabled() throws Exception {
    setUsersEnabled(false);

    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders
        .post("/assignment/work")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(status().isOk());

    // deserialize result
    Assignment returned = new ObjectMapper()
        .readValue(resultActions.andReturn().getResponse().getContentAsString(), Assignment.class);

    // tests
    assertEquals(odlcUserDao.getDefaltUser(), returned.getAssignee());
    assertEquals(image1, returned.getImage());
    assertFalse(returned.getDone());
  }

  /** Tests that the assignment is successfully created and returned when users are enabled */
  @Test
  public void testCreateWorkUsersEnabled() throws Exception {
    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders
        .post("/assignment/work")
        .header("Username", user1.getUsername())
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(status().isOk());

    // deserialize result
    Assignment returned = new ObjectMapper()
        .readValue(resultActions.andReturn().getResponse().getContentAsString(), Assignment.class);

    // tests
    assertEquals(user1, returned.getAssignee());
    assertEquals(image1, returned.getImage());
    assertFalse(returned.getDone());
  }

  /*
   * Tests that the correct error is returned ("Provided username does not exist. Try logging in.")
   * when an invalid user is provided
   */
  @Test
  public void testCreateWorkUsersEnabledInvalidUser() throws Exception {
    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders
        .post("/assignment/work")
        .header("Username", "Sheev 'Frank' 'The Senate' Palpatine")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(status().is(400));

    // tests
    assertEquals("Provided username does not exist. Try logging in.",
        resultActions.andReturn().getResponse().getContentAsString());
  }

  /** Tests that no assignments are returned when users are not enabled */
  @Test
  public void testGetAllForUserUsersNotEnabledNoAssignments() throws Exception {
    setUsersEnabled(false);

    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders
        .get("/assignment")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    MockHttpServletResponse response = resultActions.andReturn().getResponse();

    // tests
    assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
    assertEquals(expected1.toString(), response.getContentAsString());
  }

  /** Tests that no assignments are returned when users are enabled */
  @Test
  public void testGetAllForUserUsersEnabledNoAssignments() throws Exception {
    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders
        .get("/assignment")
        .header("Username", user1.getUsername())
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    MockHttpServletResponse response = resultActions.andReturn().getResponse();

    // tests
    assertEquals("application/json", response.getContentType());
    assertEquals(expected1.toString(), response.getContentAsString());
  }

  /** Tests that the correct assignments are returned for the default user */
  @Test
  public void testGetAllForUserUsersNotEnabled() throws Exception {
    setUsersEnabled(false);

    // instantiate models
    user1 = odlcUserDao.getDefaltUser();
    assignment1.setAssignee(user1);
    assignmentDao.create(assignment1);
    expected1.add(assignment1);

    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders
        .get("/assignment")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    MockHttpServletResponse response = resultActions.andReturn().getResponse();

    // deserialize result
    List<Assignment> returned = new ObjectMapper().readValue(response.getContentAsString(),
        TypeFactory.defaultInstance().constructCollectionType(List.class, Assignment.class));

    // tests
    assertEquals("application/json", response.getContentType());
    assertEquals(expected1, returned);
    assertEquals(user1, returned.get(0).getAssignee());
  }

  /** Tests that the correct assignments are returned for one user */
  @Test
  public void testGetAllForUserUsersEnabled() throws Exception {
    // instantiate models
    assignmentDao.create(assignment1);
    expected1.add(assignment1);

    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders
        .get("/assignment")
        .header("Username", user1.getUsername())
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    MockHttpServletResponse response = resultActions.andReturn().getResponse();

    // deserialize result
    List<Assignment> returned = new ObjectMapper().readValue(response.getContentAsString(),
        TypeFactory.defaultInstance().constructCollectionType(List.class, Assignment.class));

    // tests
    assertEquals("application/json", response.getContentType());
    assertEquals(expected1, returned);
    assertEquals(user1, returned.get(0).getAssignee());
  }

  /** Tests that the correct assignments are returned when there are multiple users */
  @Test
  public void testGetAllForUserUsersEnabledMultipleUsers() throws Exception {
    // instantiate models
    assignmentDao.create(assignment1);
    expected1.add(assignment1);
    assignmentDao.create(assignment2);
    expected2.add(assignment2);

    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders
        .get("/assignment")
        .header("Username", user1.getUsername())
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    MockHttpServletResponse response = resultActions.andReturn().getResponse();

    // deserialize result
    List<Assignment> returned = new ObjectMapper().readValue(response.getContentAsString(),
        TypeFactory.defaultInstance().constructCollectionType(List.class, Assignment.class));

    // tests
    assertEquals("application/json", response.getContentType());
    assertEquals(expected1, returned);
    assertEquals(user1, returned.get(0).getAssignee());

    // make request
    resultActions = mvc.perform(MockMvcRequestBuilders
        .get("/assignment")
        .header("Username", user2.getUsername())
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    response = resultActions.andReturn().getResponse();

    // deserialize result
    returned = new ObjectMapper().readValue(response.getContentAsString(),
        TypeFactory.defaultInstance().constructCollectionType(List.class, Assignment.class));

    // tests
    assertEquals("application/json", response.getContentType());
    assertEquals(expected2, returned);
    assertEquals(user2, returned.get(0).getAssignee());
  }

  /*
   * Tests that the correct error ("Provided username does not exist. Try logging in.")
   * is returned when an invalid user is provided
   */
  @Test
  public void testGetAllForUserUsersEnabledInvalidUser() throws Exception {
    // make request
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders
        .get("/assignment")
        .header("Username", "Sheev 'Frank' 'The Senate' Palpatine")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(status().is(400));

    // tests
    assertEquals("Provided username does not exist. Try logging in.",
        resultActions.andReturn().getResponse().getContentAsString());
  }

  /**
   * Tests that the correct error (404) is returned when the specified
   * assignment does not exist
   */
  @Test
  public void testUpdateNoAssignments() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    String requestJson = mapper.writeValueAsString(new Assignment(null, user1));

    // make request
    ResultActions resultActions = mvc.perform(
        MockMvcRequestBuilders.put("/assignment/1").contentType(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .characterEncoding("utf-8"))
        .andExpect(status().is(404));

    MockHttpServletResponse response = resultActions.andReturn().getResponse();

    // tests
    assertNull(response.getContentType());
    assertEquals("", response.getContentAsString());
  }

  /**
   * Tests that nothing is updated and correct response is returned when there
   * the given assignment's id does not match the passed-in id
   */
  @Test
  public void testUpdateNonMatchingIds() throws Exception {
    // instantiate models
    assignmentDao.create(assignment1);
    assignment2 = new Assignment(null, user1);
    assignmentDao.create(assignment2);

    ObjectMapper mapper = new ObjectMapper();
    String requestJson = mapper.writeValueAsString(assignment1);

    ResultActions resultActions = mvc.perform(
        MockMvcRequestBuilders.put("/assignment/2").contentType(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .characterEncoding("utf-8"))
        .andExpect(content().contentTypeCompatibleWith("text/plain;charset=UTF-8"))
        .andExpect(status().is(400));

    // tests
    assertEquals("Id in URL doesn't match id in object body",
        resultActions.andReturn().getResponse().getContentAsString());
  }

  /** Tests that the assignment is updated correctly */
  @Test
  public void testUpdate() throws Exception {
    // instantiate models
    assignmentDao.create(assignment1);

    assignment1.setDone(true);

    ObjectMapper mapper = new ObjectMapper();
    String requestJson = mapper.writeValueAsString(assignment1);

    // make request
    ResultActions resultActions = mvc.perform(
        MockMvcRequestBuilders.put("/assignment/" + assignment1.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson)
            .characterEncoding("utf-8"))
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(status().isOk());

    // deserialize result
    Assignment returned = new ObjectMapper()
        .readValue(resultActions.andReturn().getResponse().getContentAsString(), Assignment.class);

    // tests
    assertEquals(assignment1.getAssignee(), returned.getAssignee());
    assertEquals(assignment1.getImage(), returned.getImage());
    assertTrue(returned.getDone());
  }
}
