package org.cuair.ground.controllers;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cuair.ground.Application;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.ODLCUserDatabaseAccessor;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.util.Flags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class ODLCUserControllerTest {
  @Autowired
  private MockMvc mockMvc;

  private ODLCUserDatabaseAccessor odlcdao = (ODLCUserDatabaseAccessor) DAOFactory
      .getDAO(DAOFactory.ModellessDAOType.ODLCUSER_DATABASE_ACCESSOR);

  /**
   * Tests basic new user creation
   */
  @Test
  public void newUserCreation() throws Exception {
    ODLCUser expected = new ODLCUser("test", "localhost", ODLCUser.UserType.MDLCTAGGER);
    String username = "test";
    String response = mockMvc.perform(get("/odlcuser/create/mdlc")
        .with(request -> {
          request.setRemoteAddr("localhost");
          return request;
        }).header("Username", username)
    ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

    expected = odlcdao.getODLCUserFromUsername(username);
    ObjectMapper objectMapper = new ObjectMapper();
    String expectedString = objectMapper.writeValueAsString(expected);

    assertEquals(response, expectedString);
    String addr = odlcdao.getAddressFromUsername(username);
    assertEquals(addr, "localhost");
  }

  /**
   * Tests that a user with the same username and address can relogin
   */
  @Test
  public void sameUserRelogin() throws Exception {
    mockMvc.perform(get("/odlcuser/create/mdlc")
        .with(request -> {
          request.setRemoteAddr("localhost");
          return request;
        }).header("Username", "test")
    ).andExpect(status().isOk());

    mockMvc.perform(get("/odlcuser/create/mdlc")
        .with(request -> {
          request.setRemoteAddr("localhost");
          return request;
        }).header("Username", "test")
    ).andExpect(status().isOk());
  }

  /**
   * Tests that different addresses cannot login as the same username
   */
  @Test
  public void errorMultipleUsers() throws Exception {
    mockMvc.perform(get("/odlcuser/create/mdlc")
        .with(request -> {
          request.setRemoteAddr("localhost");
          return request;
        }).header("Username", "test")
    ).andExpect(status().isOk());

    mockMvc.perform(get("/odlcuser/create/mdlc")
        .with(request -> {
          request.setRemoteAddr("notlocalhost");
          return request;
        }).header("Username", "test")
        .content("test")
    ).andExpect(status().isBadRequest());
  }

  /**
   * Tests that one address cannot have multiple usernames
   */
  @Test
  public void errorSameAddress() throws Exception {
    Flags.ENABLE_MULTIPLE_USERS_PER_IP = false;
    mockMvc.perform(get("/odlcuser/create/mdlc")
        .with(request -> {
          request.setRemoteAddr("localhost");
          return request;
        }).header("Username", "test")
    ).andExpect(status().isOk());

    mockMvc.perform(get("/odlcuser/create/mdlc")
        .with(request -> {
          request.setRemoteAddr("localhost");
          return request;
        }).header("Username", "test2")
    ).andExpect(status().isBadRequest());
  }
}
