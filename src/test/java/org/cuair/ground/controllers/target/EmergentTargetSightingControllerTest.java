package org.cuair.ground.controllers.target;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.util.ArrayList;
import java.util.List;

import io.ebean.Ebean;
import org.cuair.ground.daos.AssignmentDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.DatabaseAccessor;
import org.cuair.ground.daos.ODLCUserDatabaseAccessor;
import org.cuair.ground.models.*;
import org.cuair.ground.models.geotag.*;
import org.cuair.ground.models.plane.target.*;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = EmergentTargetSightingController.class)
@AutoConfigureMockMvc
public class EmergentTargetSightingControllerTest {

  private static final Logger logger = LoggerFactory.getLogger(EmergentTargetSightingController.class);

  @Autowired
  private MockMvc mvc;

  @Autowired
  private EmergentTargetSightingController controller;

  private ObjectMapper mapper = new ObjectMapper();
  // odlcUser is always used when a target or ts needs an ODLCUser. Right now this is fine, but if we do more stuff with
  // ODLCUser later this may be something to look at.
  private ODLCUser odlcUser;
  private DatabaseAccessor<EmergentTargetSighting> emergentTargetSightingDao =
      DAOFactory.getDAO(DAOFactory.ModelDAOType.DATABASE_ACCESSOR, EmergentTargetSighting.class);
  private DatabaseAccessor<AlphanumTargetSighting> alphanumTargetSightingDao =
      DAOFactory.getDAO(DAOFactory.ModelDAOType.DATABASE_ACCESSOR, AlphanumTargetSighting.class);
  private DatabaseAccessor<EmergentTarget> targetDao =
      DAOFactory.getDAO(DAOFactory.ModelDAOType.DATABASE_ACCESSOR, EmergentTarget.class);
  private AssignmentDatabaseAccessor assignmentDao =
      (AssignmentDatabaseAccessor) DAOFactory.getDAO(DAOFactory.ModellessDAOType.ASSIGNMENT_DATABASE_ACCESSOR);
  private DatabaseAccessor<Geotag> geotagDao =
      DAOFactory.getDAO(DAOFactory.ModelDAOType.DATABASE_ACCESSOR, Geotag.class);
  private ODLCUserDatabaseAccessor userDao =
      (ODLCUserDatabaseAccessor) DAOFactory.getDAO(DAOFactory.ModellessDAOType.ODLCUSER_DATABASE_ACCESSOR);
  private EmergentTarget emergentTarget;

  /** Initializes the emergent target */
  private void createEmergentTarget() {
    emergentTarget = new EmergentTarget(odlcUser, null, "", 0L, 0L);
    targetDao.create(emergentTarget);
  }

  /** Before each test, initially populate tables */
  @Before
  public void setup() {
    odlcUser = new ODLCUser("testUser", "testAddr", ODLCUser.UserType.MDLCOPERATOR);
    userDao.create(odlcUser);
    createEmergentTarget();

    // TODO add interop tests when ready
  }

  /** Drop all tables */
  @After
  public void cleanDb() {
    List<EmergentTargetSighting> e_ts = Ebean.find(EmergentTargetSighting.class).findList();
    List<AlphanumTargetSighting> a_ts = Ebean.find(AlphanumTargetSighting.class).findList();
    List<EmergentTarget> targets = Ebean.find(EmergentTarget.class).findList();
    List<Assignment> asss = Ebean.find(Assignment.class).findList();
    List<ODLCUser> users = Ebean.find(ODLCUser.class).findList();
    Ebean.beginTransaction();
    Ebean.deleteAll(e_ts);
    Ebean.deleteAll(a_ts);
    Ebean.deleteAll(targets);
    Ebean.deleteAll(asss);
    Ebean.deleteAll(users);
    Ebean.commitTransaction();
  }

  /** Asserts that sending a GET request to emergent target sighting route {@code route} yields {@code expected} */
  private void assertListGetRequestYields(String route, List<EmergentTargetSighting> expected) throws Exception {
    ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get(route).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    MvcResult result = resultActions.andReturn();
    List<EmergentTargetSighting> actual = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
        TypeFactory.defaultInstance().constructCollectionType(List.class, EmergentTargetSighting.class));

    assertEquals(expected, actual);
  }

  /** Asserts that calling getAll yields {@code expected} */
  private void assertGetAllYields(List<EmergentTargetSighting> expected) throws Exception {
    assertListGetRequestYields("/emergent_target_sighting", expected);
  }

  /** Tests the get all call for an empty database */
  @Test
  public void testGetAllEmpty() throws Exception {
    assertGetAllYields(new ArrayList<>());
  }

  /** Tests the get all call for a database containing emergent ts */
  @Test
  public void testGetAll() throws Exception {
    List<EmergentTargetSighting> expected = new ArrayList<>();
    EmergentTargetSighting t1 =
        new EmergentTargetSighting(
            odlcUser,
            3,
            3,
            100,
            100,
            null,
            null,
            "fireman",
            Math.PI / 2,
            null,
            Confidence.LOW,
            null);
    emergentTargetSightingDao.create(t1);
    expected.add(t1);

    EmergentTargetSighting t2 =
        new EmergentTargetSighting(
            odlcUser,
            3,
            4,
            100,
            100,
            null,
            null,
            "policeman",
            Math.PI / 2,
            null,
            Confidence.HIGH,
            null);
    emergentTargetSightingDao.create(t2);
    expected.add(t2);

    assertGetAllYields(expected);
  }

  /** Tests the get all call when there are alphanum ts */
  @Test
  public void testGetAllWithAlphanumTargetSightings() throws Exception {
    alphanumTargetSightingDao.create(new AlphanumTargetSighting(
        odlcUser,
        Shape.CIRCLE,
        Color.BLACK,
        "a",
        Color.BLUE,
        false,
        30,
        50,
        27,
        27,
        null,
        null,
        Math.PI / 4,
        null,
        0.9,
        0.9,
        0.9,
        0.9,
        0.9,
        Confidence.HIGH
    ));

    assertGetAllYields(new ArrayList<>());

    List<EmergentTargetSighting> expected = new ArrayList<>();
    EmergentTargetSighting t1 =
        new EmergentTargetSighting(
            odlcUser,
            3,
            3,
            100,
            100,
            null,
            null,
            "fireman",
            Math.PI / 2,
            null,
            Confidence.LOW,
            null);
    emergentTargetSightingDao.create(t1);
    expected.add(t1);

    EmergentTargetSighting t2 =
        new EmergentTargetSighting(
            odlcUser,
            3,
            4,
            100,
            100,
            null,
            null,
            "policeman",
            Math.PI / 2,
            null,
            Confidence.HIGH,
            null);
    emergentTargetSightingDao.create(t2);
    expected.add(t2);

    assertGetAllYields(expected);
  }

  /** tests creating an emergent ts for an assignment that doesn't exist fails */
  @Test
  public void testCreateEmergentTargetSightingNoAssignment() throws Exception {
    EmergentTargetSighting t1 =
        new EmergentTargetSighting(
            odlcUser,
            1,
            2,
            3,
            4,
            null,
            null,
            "swag (im just copying what maria wrote dont blame me)",
            null,
            null,
            Confidence.HIGH,
            null);

    ObjectMapper mapper = new ObjectMapper();
    ResultActions resultActions = mvc
        .perform(MockMvcRequestBuilders.post("/emergent_target_sighting/assignment/1")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(mapper.writeValueAsString(t1)))
        .andExpect(status().isNotFound());

    MockHttpServletResponse response = resultActions.andReturn().getResponse();
    assertEquals("", response.getContentAsString());
    assertEquals(response.getErrorMessage(), "Assignment with id 1 not found");
    assertEquals(0, emergentTargetSightingDao.getAllIds().size());
  }

  /** tests successfully creating an emergent ts works as expected */
  @Test
  public void testCreateEmergentTargetSighting() throws Exception {
    Assignment assignment = new Assignment(null, odlcUser);
    assignmentDao.create(assignment);
    EmergentTargetSighting t1 =
        new EmergentTargetSighting(
            odlcUser,
            1,
            2,
            3,
            4,
            null,
            null,
            "swag (im just copying what maria wrote dont blame me)",
            null,
            null,
            Confidence.HIGH,
            null);

    ObjectMapper mapper = new ObjectMapper();
    MvcResult result = mvc
        .perform(MockMvcRequestBuilders.post(String.format("/emergent_target_sighting/assignment/%d", assignment.getId()))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(mapper.writeValueAsString(t1)))
        .andExpect(status().isOk())
        .andReturn();

    EmergentTargetSighting actual =
        new ObjectMapper().readValue(result.getResponse().getContentAsString(), EmergentTargetSighting.class);

    // judge target id excluded from json so added back
    actual.getTarget().setJudgeTargetId_TESTS_ONLY(emergentTarget.getJudgeTargetId());

    t1.setTarget(emergentTarget);
    t1.setAssignment(assignment);

    assertEquals(t1, actual);

    assertEquals(1, emergentTargetSightingDao.getAllIds().size());
    EmergentTargetSighting tsInDB = emergentTargetSightingDao.get(actual.getId());
    assertEquals(tsInDB, actual);
  }

  /** tests a creating a ts with a user that doesn't match its assignment's user fails */
  @Test
  public void testCreateEmergentTargetSightingWrongUser() throws Exception {
    ODLCUser odlcUser2 = new ODLCUser("testUser2", "testAddr2", ODLCUser.UserType.MDLCOPERATOR);
    userDao.create(odlcUser2);

    Assignment assignment = new Assignment(null, odlcUser2);
    assignmentDao.create(assignment);
    EmergentTargetSighting t1 =
        new EmergentTargetSighting(
            odlcUser,
            1,
            2,
            3,
            4,
            null,
            null,
            "swag (im just copying what maria wrote dont blame me)",
            null,
            null,
            Confidence.HIGH,
            null);

    ObjectMapper mapper = new ObjectMapper();
    ResultActions resultActions = mvc
        .perform(MockMvcRequestBuilders.post("/emergent_target_sighting/assignment/1")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(mapper.writeValueAsString(t1)))
        .andExpect(status().isBadRequest());

    MockHttpServletResponse response = resultActions.andReturn().getResponse();
    assertEquals("", response.getContentAsString());
    assertEquals(response.getErrorMessage(), "Creator ODLCUser does not match ODLCUser of assignment");
    assertEquals(0, emergentTargetSightingDao.getAllIds().size());
  }

  /* TODO (hi-pri) add back once geotag averaging implemented


  /** sends create e ts request and returns id for ts in response. used by multiple test cases
  private long createAndGetId(EmergentTargetSighting ts, long assId) throws Exception {
    ResultActions resultActions = mvc
        .perform(MockMvcRequestBuilders.post(String.format("/emergent_target_sighting/assignment/%d", assId))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(mapper.writeValueAsString(ts)));
    MvcResult result = resultActions.andReturn();
    EmergentTargetSighting created =
        new ObjectMapper().readValue(result.getResponse().getContentAsString(), EmergentTargetSighting.class);

    return created.getId();
  }

  @Test
  public void testCreateSightingWithAssignmentGeotagCreatesGeotag() throws Exception {
    Telemetry telem = new Telemetry(new GpsLocation(0., 0.),
        0., 0., new GimbalOrientation(0., 0.));
    Image image = new Image(null, telem, Image.ImgMode.FIXED, 0.);

    Assignment ass = new Assignment(image, odlcUser);
    assignmentDao.create(ass);
    EmergentTargetSighting t1 = new EmergentTargetSighting(
        odlcUser,
        0,
        0,
        10,
        10,
        null,
        null,
        "",
        0.0,
        null,
        Confidence.MEDIUM,
        ass
    );
    long id = createAndGetId(t1, ass.getId());

    assertEquals(1, emergentTargetSightingDao.getAllIds().size());
    assertNotNull(emergentTargetSightingDao.get(id).getGeotag());
    assertEquals(
        emergentTargetSightingDao.get(id).getGeotag(),
        emergentTargetSightingDao.get(id).getTarget().getGeotag());
  }

  @Test
  public void testCreateSightingWithAssignmentNoGeotagCreatesNoGeotag() throws Exception {
    Assignment ass = new Assignment(null, odlcUser);
    assignmentDao.create(ass);
    EmergentTargetSighting t1 = new EmergentTargetSighting(
        odlcUser,
        0,
        0,
        10,
        10,
        null,
        null,
        "",
        0.0,
        null,
        Confidence.MEDIUM,
        ass
    );
    ObjectMapper mapper = new ObjectMapper();
    mvc.perform(
        MockMvcRequestBuilders.post("/emergent_target_sighting/assignment/1")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(mapper.writeValueAsString(t1))
    );

    assertEquals(1, emergentTargetSightingDao.getAllIds().size());
    assertNull(emergentTargetSightingDao.getAll().get(0).getGeotag());
  }

  @Test
  public void testCreateSightingWithAssignmentGeotag() throws Exception {
    Telemetry telem = new Telemetry(new GpsLocation(0., 0.),
        0., 0., new GimbalOrientation(0., 0.));
    Image image = new Image(null, telem, Image.ImgMode.FIXED, 0.);

    Assignment ass1 = new Assignment(image, odlcUser);
    assignmentDao.create(ass1);
    Assignment ass2 = new Assignment(image, odlcUser);
    assignmentDao.create(ass2);
    Assignment ass3 = new Assignment(image, odlcUser);
    assignmentDao.create(ass3);


    EmergentTargetSighting t1 = new EmergentTargetSighting(
        odlcUser,
        0,
        0,
        10,
        10,
        null,
        null,
        "",
        0.0,
        null,
        Confidence.MEDIUM,
        ass1
    );
    EmergentTargetSighting t2 = new EmergentTargetSighting(
        odlcUser,
        5,
        5,
        10,
        10,
        null,
        null,
        "",
        0.0,
        null,
        Confidence.HIGH,
        ass2
    );
    EmergentTargetSighting t3 = new EmergentTargetSighting(
        odlcUser,
        10,
        10,
        10,
        10,
        null,
        null,
        "",
        0.0,
        null,
        Confidence.HIGH,
        ass3
    );


    long t1Id = createAndGetId(t1, ass1.getId());
    long t2Id = createAndGetId(t2, ass2.getId());
    long t3Id = createAndGetId(t3, ass3.getId());

    assertEquals(
        (double) 0.00007884499759659827,
        (double)
            emergentTargetSightingDao
                .get(t3Id)
                .getTarget()
                .getGeotag()
                .getGpsLocation()
                .getLatitude(),
        0.000001);

    assertEquals(
        emergentTargetSightingDao.get(t3Id).getGeotag(),
        emergentTargetSightingDao.get(t3Id).getTarget().getGeotag());
    assertEquals(
        emergentTargetSightingDao.get(t3Id).getGeotag(),
        emergentTargetSightingDao.get(t2Id).getGeotag());
    assertEquals(
        emergentTargetSightingDao.get(t2Id).getGeotag(),
        emergentTargetSightingDao.get(t1Id).getGeotag());
  }

  */

  /** tests that an adlc user creating an emergent ts fails */
  @Test
  public void testCreateByADLC() throws Exception {
    ODLCUser adlcUser = new ODLCUser("testAdlc", "testAdlcAddr", ODLCUser.UserType.ADLC);
    userDao.create(adlcUser);
    Assignment assignment = new Assignment(null, adlcUser);
    assignmentDao.create(assignment);
    EmergentTargetSighting t1 = new EmergentTargetSighting(
        adlcUser,
        1,
        2,
        3,
        4,
        null,
        null,
        "a lone airpod",
        null,
        null,
        Confidence.HIGH,
        null
    );

    ObjectMapper mapper = new ObjectMapper();
    MvcResult result = mvc
        .perform(MockMvcRequestBuilders.post(String.format("/emergent_target_sighting/assignment/%d", assignment.getId()))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(mapper.writeValueAsString(t1)))
        .andExpect(status().isBadRequest())
        .andReturn();

    assertEquals("", result.getResponse().getContentAsString());
    assertEquals("Only MDLC should be creating Emergent Target Sightings", result.getResponse().getErrorMessage());
  }

  /** tests successfully updating an emergent ts */
  @Test
  public void testUpdate() throws Exception {
    Assignment assignment = new Assignment(null, odlcUser);
    assignmentDao.create(assignment);
    EmergentTargetSighting original = new EmergentTargetSighting(
        odlcUser,
        4,
        4,
        100,
        100,
        null,
        null,
        "fireman",
        Math.PI / 2,
        null,
        Confidence.MEDIUM,
        assignment
    );
    emergentTargetSightingDao.create(original);

    EmergentTargetSighting update = new EmergentTargetSighting(
        null,
        4,
        4,
        100,
        100,
        null,
        null,
        "policeman",
        Math.PI / 2,
        null,
        Confidence.MEDIUM,
        null
    );

    ObjectMapper mapper = new ObjectMapper();
    MvcResult result = mvc
        .perform(MockMvcRequestBuilders.put(String.format("/emergent_target_sighting/%d", original.getId()))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(mapper.writeValueAsString(update)))
        .andExpect(status().isOk())
        .andReturn();
    EmergentTargetSighting actual =
        new ObjectMapper().readValue(result.getResponse().getContentAsString(), EmergentTargetSighting.class);

    assertEquals(update.getTarget(), actual.getTarget());
    assertEquals(update.getpixelx(), actual.getpixelx());
    assertEquals(update.getpixely(), actual.getpixely());
    assertEquals(update.getGeotag(), actual.getGeotag());
    assertEquals(original.getCreator(), actual.getCreator());
    assertEquals(update.getDescription(), actual.getDescription());
  }

  /** Asserts update to emergent ts with id 1 is invalid */
  private void assertInvalidUpdates(Long id, String expectedError, EmergentTargetSighting invalidUpdate) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    MvcResult result = mvc
        .perform(MockMvcRequestBuilders.put(String.format("/emergent_target_sighting/%d", id))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(mapper.writeValueAsString(invalidUpdate)))
        .andExpect(status().isBadRequest())
        .andReturn();

    assertEquals(expectedError, result.getResponse().getErrorMessage());
  }

  /** tests that updating an emergent ts's target fails */
  @Test
  public void testUpdateWithTarget() throws Exception {
    EmergentTargetSighting original =
        new EmergentTargetSighting(
            odlcUser,
            3,
            3,
            100,
            100,
            null,
            null,
            "fireman",
            Math.PI / 2,
            null,
            Confidence.MEDIUM,
            null);
    emergentTargetSightingDao.create(original);

    EmergentTarget t = new EmergentTarget(odlcUser, null, "fireman", 1L, 0L);
    targetDao.create(t);

    EmergentTargetSighting update =
        new EmergentTargetSighting(
            null,
            null,
            null,
            100,
            100,
            null,
            t,
            "fireman",
            Math.PI / 2,
            null,
            Confidence.HIGH,
            null);

    // tests
    assertInvalidUpdates(original.getId(), "Don't pass targets for emergent target sighting " + "update", update);
  }

  /** Test invalid update, cannot pass id in json */
  @Test
  public void testUpdateWithId() throws Exception {
    EmergentTargetSighting original =
        new EmergentTargetSighting(
            odlcUser,
            3,
            3,
            100,
            100,
            null,
            null,
            "fireman",
            Math.PI / 2,
            null,
            Confidence.HIGH,
            null);
    emergentTargetSightingDao.create(original);

    EmergentTargetSighting update =
        new EmergentTargetSighting(
            null,
            null,
            null,
            100,
            100,
            null,
            null,
            "fireman",
            Math.PI / 2,
            null,
            Confidence.LOW,
            null);
    emergentTargetSightingDao.create(update);

    assertInvalidUpdates(original.getId(), "Don't pass ids for updates", update);
  }

  /** Test invalid update, cannot pass creator in json */
  @Test
  public void testUpdateWithCreator() throws Exception {
    EmergentTargetSighting original =
        new EmergentTargetSighting(
            odlcUser,
            3,
            3,
            100,
            100,
            null,
            null,
            "fireman",
            Math.PI / 2,
            null,
            Confidence.LOW,
            null);
    emergentTargetSightingDao.create(original);

    ODLCUser updatedUser = new ODLCUser("test2", "testAddr2", ODLCUser.UserType.MDLCOPERATOR);
    userDao.create(updatedUser);
    EmergentTargetSighting update =
        new EmergentTargetSighting(
            updatedUser,
            null,
            null,
            100,
            100,
            null,
            null,
            "fireman",
            Math.PI / 2,
            1D,
            Confidence.MEDIUM,
            null);

    assertInvalidUpdates(original.getId(), "Don't pass creator for updates", update);
  }

  /** Test invalid update, cannot pass geotag in json */
  @Test
  public void testUpdateWithGeotag() throws Exception {
    EmergentTargetSighting original =
        new EmergentTargetSighting(
            odlcUser,
            3,
            3,
            100,
            100,
            null,
            null,
            "fireman",
            Math.PI / 2,
            null,
            Confidence.MEDIUM,
            null);
    emergentTargetSightingDao.create(original);

    EmergentTargetSighting update =
        new EmergentTargetSighting(
            null,
            null,
            null,
            100,
            100,
            new Geotag(new GpsLocation(0., 0.), 1.),
            null,
            "fireman",
            Math.PI / 2,
            null,
            Confidence.HIGH,
            null);

    assertInvalidUpdates(original.getId(), "Don't pass geotag for updates", update);
  }

  /** Test invalid update, cannot pass non-matching pixel_x in json */
  @Test
  public void testUpdateWithPixel_X() throws Exception {
    EmergentTargetSighting original =
        new EmergentTargetSighting(
            odlcUser,
            3,
            4,
            100,
            100,
            null,
            null,
            "fireman",
            Math.PI / 2,
            null,
            null,
            null);
    emergentTargetSightingDao.create(original);

    EmergentTargetSighting update =
        new EmergentTargetSighting(
            null,
            4,
            null,
            100,
            100,
            null,
            null,
            "fireman",
            Math.PI / 2,
            null,
            null,
            null);

    assertInvalidUpdates(original.getId(), "Don't change value of pixel_x. Current value is 3", update);
  }

  /** Test invalid update, cannot pass non-matching pixel_y in json */
  @Test
  public void testUpdateWithPixel_Y() throws Exception {
    EmergentTargetSighting original =
        new EmergentTargetSighting(
            odlcUser,
            4,
            3,
            100,
            100,
            null,
            null,
            "fireman",
            Math.PI / 2,
            null,
            null,
            null);
    emergentTargetSightingDao.create(original);

    EmergentTargetSighting update =
        new EmergentTargetSighting(
            null,
            null,
            4,
            100,
            100,
            null,
            null,
            "fireman",
            Math.PI / 2,
            null,
            null,
            null);

    assertInvalidUpdates(original.getId(), "Don't change value of pixel_y. Current value is 3", update);
  }

  /** Test invalid update, cannot pass non-matching width in json */
  @Test
  public void testUpdateWithWidth() throws Exception {
    EmergentTargetSighting original =
        new EmergentTargetSighting(
            odlcUser,
            3,
            3,
            100,
            200,
            null,
            null,
            "fireman",
            Math.PI / 2,
            null,
            null,
            null);
    emergentTargetSightingDao.create(original);

    EmergentTargetSighting update =
        new EmergentTargetSighting(
            null,
            null,
            null,
            200,
            null,
            null,
            null,
            "fireman",
            Math.PI / 2,
            null,
            null,
            null);

    assertInvalidUpdates(original.getId(), "Don't change value of width. Current value is 100", update);
  }

  /** Test invalid update, cannot pass non-matching height in json */
  @Test
  public void testUpdateWithHeight() throws Exception {
    EmergentTargetSighting original =
        new EmergentTargetSighting(
            odlcUser,
            3,
            3,
            200,
            100,
            null,
            null,
            "fireman",
            Math.PI / 2,
            null,
            null,
            null);
    emergentTargetSightingDao.create(original);

    EmergentTargetSighting update =
        new EmergentTargetSighting(
            null,
            null,
            null,
            null,
            200,
            null,
            null,
            "fireman",
            Math.PI / 2,
            null,
            null,
            null);

    assertInvalidUpdates(original.getId(), "Don't change value of height. Current value is 100", update);
  }

  /** Tests update by id call when model with specific id doesn't exist */
  @Test
  public void testUpdateDoesntExist() throws Exception {
    EmergentTargetSighting update =
        new EmergentTargetSighting(
            odlcUser,
            3,
            3,
            100,
            100,
            null,
            null,
            "fireman",
            Math.PI / 2,
            null,
            Confidence.HIGH,
            null);

    ObjectMapper mapper = new ObjectMapper();
    mvc.perform(MockMvcRequestBuilders.put("/emergent_target_sighting/1")
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content(mapper.writeValueAsString(update)))
        .andExpect(status().isNotFound());
  }

  /** Tests delete call */
  @Test
  public void testDelete() throws Exception {
    EmergentTarget t =
        new EmergentTarget(
            odlcUser,
            new Geotag(new GpsLocation(40., 10.), 2.),
            "fireman",
            null,
            0L);
    targetDao.create(t);

    EmergentTargetSighting ts =
        new EmergentTargetSighting(
            odlcUser,
            3,
            3,
            100,
            100,
            new Geotag(new GpsLocation(50., 20.), 1.),
            t,
            "fireman",
            Math.PI / 2,
            null,
            Confidence.HIGH,
            null);
    emergentTargetSightingDao.create(ts);


    mvc.perform(MockMvcRequestBuilders.delete(String.format("/emergent_target_sighting/%d", ts.getId())))
        .andExpect(status().isOk());

    assertNull(emergentTargetSightingDao.get(ts.getId()));
    assertNotNull(targetDao.getAll().get(0));
    assertNotNull(geotagDao.get(t.getGeotag().getId()));
    assertNull(geotagDao.get(ts.getGeotag().getId()));
  }

  /** Tests delete when ts is thumbnail of target */
  @Test
  public void testDeleteWhenThumbnail() throws Exception {
    EmergentTarget t =
        new EmergentTarget(
            odlcUser,
            new Geotag(new GpsLocation(40., 10.), 2.),
            "hiker",
            null,
            0L);
    targetDao.create(t);

    EmergentTargetSighting ts =
        new EmergentTargetSighting(
            odlcUser,
            3,
            3,
            100,
            100,
            new Geotag(new GpsLocation(50., 20.), 1.),
            t,
            "hiker",
            Math.PI / 2,
            null,
            null,
            null);
    emergentTargetSightingDao.create(ts);

    t.setthumbnailTsid(ts.getId());
    targetDao.update(t);

    mvc.perform(MockMvcRequestBuilders.delete(String.format("/emergent_target_sighting/%d", ts.getId()))).andExpect(status().isOk());

    EmergentTarget newT = targetDao.get(t.getId());
    assertEquals(0L, newT.getthumbnailTsid().longValue());
    assertNull(emergentTargetSightingDao.get(ts.getId()));
  }

  /** Tests delete when model with specific id doesn't exist */
  @Test
  public void testDeleteDoesntExist() throws Exception {
    mvc.perform(MockMvcRequestBuilders.delete("/emergent_target_sighting/1"))
        .andExpect(status().isNotFound());
  }
}
