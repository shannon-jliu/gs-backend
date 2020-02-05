package org.cuair.ground.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

import io.ebean.Ebean;
import java.sql.Timestamp;

import org.cuair.ground.controllers.target.AlphanumTargetSightingController;

import org.cuair.ground.daos.TargetSightingsDatabaseAccessor;
import org.cuair.ground.daos.AssignmentDatabaseAccessor;
import org.cuair.ground.daos.DatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.TargetSightingsDatabaseAccessor;
import org.cuair.ground.daos.TimestampDatabaseAccessor;
import org.cuair.ground.models.plane.target.AlphanumTargetSighting;
import org.cuair.ground.models.plane.target.AlphanumTarget;
import org.cuair.ground.models.geotag.Geotag;
import org.cuair.ground.models.Image;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AlphanumTargetSightingController.class)
@AutoConfigureMockMvc
public class AlphanumTargetSightingControllerTest {

    private TargetSightingsDatabaseAccessor<AlphanumTargetSighting> alphanumTargetSightingDao;
    private DatabaseAccessor<AlphanumTarget> targetDao;
    private DatabaseAccessor<Geotag> geotagDao;
    private AssignmentDatabaseAccessor assignmentDao;
    private TimestampDatabaseAccessor<Image> imageDao;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AlphanumTargetSightingController controller;

    @Before
    public void setup() {
        alphanumTargetSightingDao = (TargetSightingsDatabaseAccessor<AlphanumTargetSighting>) DAOFactory.getDAO(DAOFactory.ModelDAOType.TARGET_SIGHTINGS_DATABASE_ACCESSOR, AlphanumTargetSighting.class);
        targetDao = DAOFactory.getDAO(DAOFactory.ModelDAOType.DATABASE_ACCESSOR, AlphanumTarget.class);
        geotagDao = DAOFactory.getDAO(DAOFactory.ModelDAOType.DATABASE_ACCESSOR, Geotag.class);
        assignmentDao = (AssignmentDatabaseAccessor) DAOFactory.getDAO(DAOFactory.ModellessDAOType.ASSIGNMENT_DATABASE_ACCESSOR);
        imageDao = (TimestampDatabaseAccessor<Image>) DAOFactory.getDAO(DAOFactory.ModelDAOType.TIMESTAMP_DATABASE_ACCESSOR, Image.class);

        // TODO: Add autopilot client code
    }

    @After
    public void clearDB() {
        List<AlphanumTargetSighting> assignments = Ebean.find(AlphanumTargetSighting.class).findList();
        Ebean.beginTransaction();
        Ebean.deleteAll(assignments);
        Ebean.commitTransaction();
    }

    @Test
    public void loads() {
        assertThat(controller).isNotNull();
    }

    /** Tests the GET all call for an empty database */
    @Test
    public void testGetAllEmpty() throws Exception {
        // make request
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/alphanum_target_sighting").accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("[]")));
    }

    /** Tests the GET all call for a database containing images */
    @Test
    public void testGetAll() throws Exception {
        // instantiate models
        List<AlphanumTargetSighting> expected = new ArrayList<AlphanumTargetSighting>();
        AlphanumTargetSighting t1 =
            new AlphanumTargetSighting(
                ClientType.MDLC,
                Shape.SQUARE,
                Color.RED,
                "b",
                Color.BLUE,
                false,
                3,
                3,
                100,
                100,
                null,
                null,
                new Double(Math.PI / 2),
                null,
                null,
                null,
                null,
                null,
                null,
                Confidence.HIGH);
        alphanumTargetSightingDao.create(t1);
        expected.add(t1);

        AlphanumTargetSighting t2 =
            new AlphanumTargetSighting(
                ClientType.MDLC,
                Shape.SQUARE,
                Color.GREEN,
                "g",
                Color.PURPLE,
                false,
                3,
                4,
                100,
                100,
                null,
                null,
                new Double(Math.PI / 2),
                null,
                null,
                null,
                null,
                null,
                null,
                Confidence.LOW);
        alphanumTargetSightingDao.create(t2);

        expected.add(t2);

        // make request
        Result result =
            route(org.cuair.ground.controllers.target.routes.AlphanumTargetSightingController.getAll());

        // deserialize result
        List<AlphanumTargetSighting> actual = new LinkedList<>();
        ArrayNode jsonArray = (ArrayNode) Json.parse(contentAsString(result));
        for (JsonNode node : jsonArray) {
            actual.add(Json.fromJson(node, AlphanumTargetSighting.class));
        }

        // tests
        assertEquals(OK, result.status());
        assertEquals("application/json", result.contentType().get());
        assertNotNull(actual);
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }









}
