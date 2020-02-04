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

import org.cuair.ground.daos.AssignmentDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.TargetSightingsDatabaseAccessor;
import org.cuair.ground.daos.TimestampDatabaseAccessor;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.Image;
import org.cuair.ground.models.Image.ImgMode;
import org.cuair.ground.models.ClientType;
import org.cuair.ground.models.AuthToken;
import org.cuair.ground.models.plane.target.AlphanumTargetSighting;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {AssignmentController.class, AuthController.class})
@AutoConfigureMockMvc
public class AssignmentControllerTest {

    private AssignmentDatabaseAccessor assignmentDao;
    private TimestampDatabaseAccessor<Image> imageDao;
    private TargetSightingsDatabaseAccessor<AlphanumTargetSighting> targetSightingDao;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AssignmentController controller;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(controller, "AUTH_ENABLED", false);

        assignmentDao = (AssignmentDatabaseAccessor) DAOFactory.getDAO(DAOFactory.ModellessDAOType.ASSIGNMENT_DATABASE_ACCESSOR);
        imageDao = (TimestampDatabaseAccessor) DAOFactory.getDAO(DAOFactory.ModelDAOType.TIMESTAMP_DATABASE_ACCESSOR, Image.class);
        targetSightingDao = (TargetSightingsDatabaseAccessor) DAOFactory.getDAO(DAOFactory.ModelDAOType.ALPHANUM_TARGET_SIGHTINGS_DATABASE_ACCESSOR, AlphanumTargetSighting.class);
    }

    @After
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

    @Test
    public void testGetAllEmpty() throws Exception {
        // make request and test
        ArrayList<Assignment> list = new ArrayList();
        mvc.perform(MockMvcRequestBuilders.get("/assignment").accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo(list.toString())));
    }

    @Test
    public void testGetAll() throws Exception {
        // instantiate models
        Timestamp t1 = new Timestamp(new Date().getTime());
        Image image1 = new Image(String.valueOf(t1.getTime()) + ".jpg", null, ImgMode.TRACKING);
        image1.setTimestamp(t1);
        imageDao.create(image1);
        Assignment assignment1 = new Assignment(image1, ClientType.MDLC);
        assignment1.setTimestamp(new Timestamp(new java.util.Date().getTime()));
        assignmentDao.create(assignment1);

        Timestamp t2 = new Timestamp(new Date().getTime());
        Image image2 = new Image(String.valueOf(t2.getTime()) + ".jpg", null, ImgMode.TRACKING);
        image2.setTimestamp(t2);
        imageDao.create(image2);
        Assignment assignment2 = new Assignment(image1, ClientType.ADLC);
        assignment2.setTimestamp(new Timestamp(new java.util.Date().getTime()));
        assignmentDao.create(assignment2);

        List<Assignment> expected = new ArrayList<>();
        expected.add(assignment1);
        expected.add(assignment2);

        // make request
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/assignment").accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(status().isOk());

        MvcResult result = resultActions.andReturn();

        // deserialize result
        List<Assignment> actual = null;
        actual = new ObjectMapper().readValue(result.getResponse().getContentAsString(),
                        TypeFactory.defaultInstance().constructCollectionType(List.class, Assignment.class));

        // tests
        assertEquals(expected, actual);
    }

    @Test
    public void testGetEmpty() throws Exception {
        // make request and test
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/assignment/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(content().string(equalTo("")));

        assertEquals(null, resultActions.andReturn().getResponse().getContentType());
    }

    @Test
    public void testGet() throws Exception {
        // instantiate models
        Timestamp t1 = new Timestamp(new Date().getTime());
        Image image1 = new Image(String.valueOf(t1.getTime()) + ".jpg", null, ImgMode.TRACKING);
        image1.setTimestamp(t1);
        imageDao.create(image1);
        Assignment assignment1 = new Assignment(image1, ClientType.MDLC);
        assignment1.setTimestamp(new Timestamp(new Date().getTime()));
        assignmentDao.create(assignment1);

        // make request
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/assignment/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(status().isOk());

        MvcResult result = resultActions.andReturn();

        // deserialize result
        Assignment actual = new ObjectMapper().readValue(result.getResponse().getContentAsString(), Assignment.class);

        // tests
        assertEquals(assignment1, actual);
    }

    @Test
    public void testUpdateEmpty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(new Assignment(null, ClientType.MDLC));

        // make request
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.put("/assignment/1").contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .characterEncoding("utf-8"))
                .andExpect(status().isNoContent());

        // tests
        assertEquals(null, resultActions.andReturn().getResponse().getContentType());
    }

    @Test
    public void testUpdateNonMatchingIds() throws Exception {
        // instantiate models
        Timestamp t1 = new Timestamp(new Date().getTime());
        Image image1 = new Image(String.valueOf(t1.getTime()) + ".jpg", null, ImgMode.TRACKING);
        image1.setTimestamp(t1);
        imageDao.create(image1);
        Assignment assignment1 = new Assignment(image1, ClientType.MDLC);
        assignment1.setTimestamp(new Timestamp(new Date().getTime()));
        assignmentDao.create(assignment1);

        Assignment assignment2 = new Assignment(null, ClientType.ADLC);
        assignment2.setTimestamp(new Timestamp(new Date().getTime()));
        assignmentDao.create(assignment2);

        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(assignment1);

        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.put("/assignment/3").contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .characterEncoding("utf-8"))
                .andExpect(content().contentTypeCompatibleWith("text/plain"))
                .andExpect(status().is(400));

        // tests
        assertEquals("Id in URL doesn't match id in object body", resultActions.andReturn().getResponse().getContentAsString());
    }

    @Test
    public void testUpdate() throws Exception {
        // instantiate models
        Timestamp t1 = new Timestamp(new Date().getTime());
        Image image1 = new Image(String.valueOf(t1.getTime()) + ".jpg", null, ImgMode.TRACKING);
        image1.setTimestamp(t1);
        imageDao.create(image1);
        Assignment assignment1 = new Assignment(image1, ClientType.MDLC);
        assignment1.setDone(false);
        assignment1.setTimestamp(new Timestamp(new Date().getTime()));
        assignmentDao.create(assignment1);

        assignment1.setDone(true);

        ObjectMapper mapper = new ObjectMapper();
        String requestJson = mapper.writeValueAsString(assignment1);

        // make request
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.put("/assignment/"+assignment1.getId()).contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
                    .characterEncoding("utf-8"))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(status().isOk());

        // deserialize result
        Assignment actual = new ObjectMapper().readValue(resultActions.andReturn().getResponse().getContentAsString(), Assignment.class);

        // tests
        assertEquals(assignment1.getAssignee(), actual.getAssignee());
        assertEquals(assignment1.getImage(), actual.getImage());
        assertEquals(true, actual.getDone());
    }

    @Test
    public void testGetWorkMdlcNoImages() throws Exception {
        // make request
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post("/assignment/work/MDLC").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // tests
        assertEquals(null, resultActions.andReturn().getResponse().getContentType());
    }

    @Test
    public void testGetWorkAdlcNoImages() throws Exception {
        // make request
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post("/assignment/work/ADLC").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // tests
        assertEquals(null, resultActions.andReturn().getResponse().getContentType());
    }

    @Test
    public void testGetWorkMdlc() throws Exception {
        // instantiate models
        Timestamp t1 = new Timestamp(new Date().getTime());
        Image image1 = new Image(String.valueOf(t1.getTime()) + ".jpg", null, ImgMode.TRACKING);
        image1.setTimestamp(t1);
        imageDao.create(image1);

        // make request
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post("/assignment/work/MDLC").accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(status().isOk());

        // deserialize result
        Assignment actual = new ObjectMapper().readValue(resultActions.andReturn().getResponse().getContentAsString(), Assignment.class);

        // tests
        assertEquals(ClientType.MDLC, actual.getAssignee());
        // TODO: Figure out why this gets the wrong image. Related to needing to shift id's in ImageControllerTest.java
        // assertEquals(image1, actual.getImage());
        assertEquals(false, actual.getDone());
    }

    @Test
    public void testGetWorkAdlc() throws Exception {
        // instantiate models
        Timestamp t1 = new Timestamp(new Date().getTime());
        Image image1 = new Image(String.valueOf(t1.getTime()) + ".jpg", null, ImgMode.TRACKING);
        image1.setTimestamp(t1);
        imageDao.create(image1);

        // make request
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post("/assignment/work/ADLC").accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(status().isOk());

        // deserialize result
        Assignment actual = new ObjectMapper().readValue(resultActions.andReturn().getResponse().getContentAsString(), Assignment.class);

        // tests
        assertEquals(ClientType.ADLC, actual.getAssignee());
        // TODO: Figure out why this gets the wrong image. Related to needing to shift id's in ImageControllerTest.java
        // assertEquals(image1, actual.getImage());
        assertEquals(false, actual.getDone());
    }

    // TODO: Fix this
    // private static final String PASSWORD = "246409e55c39eb701f7ef3950f540bae";
    //
    // @Test
    // public void testUserBasedMdlc() throws Exception {
    //     ReflectionTestUtils.setField(controller, "AUTH_ENABLED", true);
    //     // get the auth token for an admin
    //     ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/auth").accept(MediaType.APPLICATION_JSON)
    //                                     .header("Authorization", PASSWORD)
    //                                     .header("Username", "TestUser"));
    //     AuthToken token = new ObjectMapper().readValue(resultActions.andReturn().getResponse().getContentAsString(), AuthToken.class);

    //     // add image to generate new assignment
    //     Timestamp t1 = new java.sql.Timestamp(new java.util.Date().getTime());
    //     Image image1 = new Image(String.valueOf(t1.getTime()) + ".jpg", null, null);
    //     image1.setTimestamp(t1);
    //     imageDao.create(image1);

    //     // create new assignment
    //     resultActions = mvc.perform(MockMvcRequestBuilders.post("/assignment/work/MDLC").accept(MediaType.APPLICATION_JSON)
    //                                     .header("X-AUTH-TOKEN", token.getToken()))
    //             .andExpect(content().contentTypeCompatibleWith("application/json"))
    //             .andExpect(status().isOk());
    //     Assignment actual = new ObjectMapper().readValue(resultActions.andReturn().getResponse().getContentAsString(), Assignment.class);
    //     assertEquals("TestUser", actual.getUsername());

    //     // assert that there are no more
    //     mvc.perform(MockMvcRequestBuilders.post("/assignment/work/MDLC").accept(MediaType.APPLICATION_JSON))
    //             .andExpect(content().contentTypeCompatibleWith("application/json"))
    //             .andExpect(status().isNoContent());
    // }

    // @Test
    // public void testUserBasedAdlc() throws Exception {
    //     ReflectionTestUtils.setField(controller, "AUTH_ENABLED", true);
    //     // get the auth token for an admin
    //     ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/auth").accept(MediaType.APPLICATION_JSON)
    //                                     .header("Username", "Vision"));
    //     AuthToken token = new ObjectMapper().readValue(resultActions.andReturn().getResponse().getContentAsString(), AuthToken.class);

    //     // add image to generate new assignment
    //     Timestamp t1 = new java.sql.Timestamp(new java.util.Date().getTime());
    //     Image image1 = new Image(String.valueOf(t1.getTime()) + ".jpg", null, null);
    //     image1.setTimestamp(t1);
    //     imageDao.create(image1);

    //     // create new assignment
    //     resultActions = mvc.perform(MockMvcRequestBuilders.post("/assignment/work/MDLC").accept(MediaType.APPLICATION_JSON)
    //                                     .header("X-AUTH-TOKEN", token.getToken()))
    //             .andExpect(content().contentTypeCompatibleWith("application/json"))
    //             .andExpect(status().isOk());
    //     Assignment actual = new ObjectMapper().readValue(resultActions.andReturn().getResponse().getContentAsString(), Assignment.class);
    //     assertEquals("Vision", actual.getUsername());

    //     // assert that there are no more
    //     mvc.perform(MockMvcRequestBuilders.post("/assignment/work/MDLC").accept(MediaType.APPLICATION_JSON))
    //             .andExpect(content().contentTypeCompatibleWith("application/json"))
    //             .andExpect(status().isNoContent());
    // }
    //
    // @Test
    // public void testGetUserNoAssignments() throws Exception {
    //     ReflectionTestUtils.setField(controller, "AUTH_ENABLED", true);

    //     // get the auth token for an admin
    //     ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/auth").accept(MediaType.APPLICATION_JSON)
    //                                     .header("Authorization", PASSWORD)
    //                                     .header("Username", "TestUser"));
    //     AuthToken token = new ObjectMapper().readValue(resultActions.andReturn().getResponse().getContentAsString(), AuthToken.class);

    //     // TestUser should have no assignments
    //     resultActions = mvc.perform(MockMvcRequestBuilders.post("/assignment/user").accept(MediaType.APPLICATION_JSON)
    //                                     .header("X-AUTH-TOKEN", token.getToken()))
    //             .andExpect(content().contentTypeCompatibleWith("application/json"))
    //             .andExpect(status().isOk());

    //     List<Assignment> actual = new ObjectMapper().readValue(resultActions.andReturn().getResponse().getContentAsString(), TypeFactory.defaultInstance().constructCollectionType(List.class, Assignment.class));

    //     assertTrue(actual.isEmpty());
    // }

    // @Test
    // public void testGetUserSomeNoAssignments() throws Exception {
    //     ReflectionTestUtils.setField(controller, "AUTH_ENABLED", true);
    //     // instantiate models
    //     Timestamp t1 = new Timestamp(new Date().getTime());
    //     Image image1 = new Image(String.valueOf(t1.getTime()) + ".jpg", null, null);
    //     image1.setTimestamp(t1);
    //     imageDao.create(image1);

    //     // get the auth token for an admin
    //     ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/auth").accept(MediaType.APPLICATION_JSON)
    //                                     .header("Authorization", PASSWORD)
    //                                     .header("Username", "TestUser"));
    //     AuthToken token = new ObjectMapper().readValue(resultActions.andReturn().getResponse().getContentAsString(), AuthToken.class);

    //     // create 2 new assignments
    //     mvc.perform(MockMvcRequestBuilders.post("/assignment/work/MDLC").accept(MediaType.APPLICATION_JSON)
    //                                     .header("X-AUTH-TOKEN", token.getToken()))
    //             .andExpect(content().contentTypeCompatibleWith("application/json"))
    //             .andExpect(status().isOk());

    //     //  build second user
    //     resultActions = mvc.perform(MockMvcRequestBuilders.get("/auth").accept(MediaType.APPLICATION_JSON)
    //                                     .header("Authorization", PASSWORD)
    //                                     .header("Username", "TestUser"));
    //     AuthToken token2 = new ObjectMapper().readValue(resultActions.andReturn().getResponse().getContentAsString(), AuthToken.class);

    //     // now TestUser will have 1 assignment and TestUser2 will have none
    //     // TestUser should have assignment ids 1 and 2
    //     resultActions = mvc.perform(MockMvcRequestBuilders.post("/assignment/user").accept(MediaType.APPLICATION_JSON)
    //                                     .header("X-AUTH-TOKEN", token.getToken()))
    //             .andExpect(content().contentTypeCompatibleWith("application/json"))
    //             .andExpect(status().isOk());

    //     List<Assignment> actual = new ObjectMapper().readValue(resultActions.andReturn().getResponse().getContentAsString(), TypeFactory.defaultInstance().constructCollectionType(List.class, Assignment.class));
    //     assertEquals(1, actual.size());
    //     assertTrue(1 == actual.get(0).getId());

    //     // TestUser2 should have id 3
    //     resultActions = mvc.perform(MockMvcRequestBuilders.post("/assignment/user").accept(MediaType.APPLICATION_JSON)
    //                                     .header("X-AUTH-TOKEN", token2.getToken()))
    //             .andExpect(content().contentTypeCompatibleWith("application/json"))
    //             .andExpect(status().isOk());

    //     actual = new ObjectMapper().readValue(resultActions.andReturn().getResponse().getContentAsString(), TypeFactory.defaultInstance().constructCollectionType(List.class, Assignment.class));
    //     assertTrue(actual.isEmpty());
    // }

    // @Test
    // public void testGetUserAssignments() throws Exception {
    //     ReflectionTestUtils.setField(controller, "AUTH_ENABLED", true);
    //     // instantiate models
    //     Timestamp t1 = new Timestamp(new Date().getTime());
    //     Image image1 = new Image(String.valueOf(t1.getTime()) + ".jpg", null, null);
    //     image1.setTimestamp(t1);
    //     imageDao.create(image1);

    //     Timestamp t2 = new Timestamp(new Date().getTime());
    //     Image image2 = new Image(String.valueOf(t2.getTime()) + ".jpg", null, null);
    //     image2.setTimestamp(t2);
    //     imageDao.create(image2);

    //     // get the auth token for an admin
    //     ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/auth").accept(MediaType.APPLICATION_JSON)
    //                                     .header("Authorization", PASSWORD)
    //                                     .header("Username", "TestUser"));
    //     AuthToken token = new ObjectMapper().readValue(resultActions.andReturn().getResponse().getContentAsString(), AuthToken.class);

    //     // create 2 new assignments
    //     mvc.perform(MockMvcRequestBuilders.post("/assignment/work/MDLC").accept(MediaType.APPLICATION_JSON)
    //                                     .header("X-AUTH-TOKEN", token.getToken()))
    //             .andExpect(content().contentTypeCompatibleWith("application/json"))
    //             .andExpect(status().isOk());

    //     mvc.perform(MockMvcRequestBuilders.post("/assignment/work/MDLC").accept(MediaType.APPLICATION_JSON)
    //                                     .header("X-AUTH-TOKEN", token.getToken()))
    //             .andExpect(content().contentTypeCompatibleWith("application/json"))
    //             .andExpect(status().isOk());

    //     //  build second user
    //     resultActions = mvc.perform(MockMvcRequestBuilders.get("/auth").accept(MediaType.APPLICATION_JSON)
    //                                     .header("Authorization", PASSWORD)
    //                                     .header("Username", "TestUser2"));
    //     AuthToken token2 = new ObjectMapper().readValue(resultActions.andReturn().getResponse().getContentAsString(), AuthToken.class);

    //     mvc.perform(MockMvcRequestBuilders.post("/assignment/work/MDLC").accept(MediaType.APPLICATION_JSON)
    //                                     .header("X-AUTH-TOKEN", token2.getToken()))
    //             .andExpect(content().contentTypeCompatibleWith("application/json"))
    //             .andExpect(status().isOk());

    //     // now TestUser will have 2 assignments and TestUser2 will only have 1
    //     // TestUser should have assignment ids 1 and 2
    //     resultActions = mvc.perform(MockMvcRequestBuilders.post("/assignment/user").accept(MediaType.APPLICATION_JSON)
    //                                     .header("X-AUTH-TOKEN", token2.getToken()))
    //             .andExpect(content().contentTypeCompatibleWith("application/json"))
    //             .andExpect(status().isOk());

    //     List<Assignment> actual = new ObjectMapper().readValue(resultActions.andReturn().getResponse().getContentAsString(), TypeFactory.defaultInstance().constructCollectionType(List.class, Assignment.class));
    //     assertEquals(2, actual.size());
    //     for (int i = 1; i <= actual.size(); i++) {
    //       assertTrue(i == actual.get(i - 1).getId());
    //     }

    //     // TestUser2 should have id 3
    //     mvc.perform(MockMvcRequestBuilders.post("/assignment/work/MDLC").accept(MediaType.APPLICATION_JSON)
    //                                     .header("X-AUTH-TOKEN", token2.getToken()))
    //             .andExpect(content().contentTypeCompatibleWith("application/json"))
    //             .andExpect(status().isOk());

    //     actual = new ObjectMapper().readValue(resultActions.andReturn().getResponse().getContentAsString(), TypeFactory.defaultInstance().constructCollectionType(List.class, Assignment.class));
    //     assertEquals(1, actual.size());
    //     assertTrue(3 == actual.get(0).getId());
    // }

    @Test
    public void testInvalidUser() {
        ReflectionTestUtils.setField(controller, "AUTH_ENABLED", true);
        mvc.perform(MockMvcRequestBuilders.post("/assignment/work/MDLC").accept(MediaType.APPLICATION_JSON)
                                        .header("X-AUTH-TOKEN", "trash"))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(status().is(400));
    }
}
