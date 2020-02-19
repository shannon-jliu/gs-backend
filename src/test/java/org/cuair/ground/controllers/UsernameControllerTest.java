package org.cuair.ground.controllers;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import org.cuair.ground.Application;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.UsernameDatabaseAccessor;
import org.cuair.ground.models.Username;
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
public class UsernameControllerTest {
  @Autowired
  private MockMvc mockMvc;

  private UsernameDatabaseAccessor usernameDao = (UsernameDatabaseAccessor) DAOFactory.getDAO(DAOFactory.ModellessDAOType.USERNAME_DATABASE_ACCESSOR);

  /**
   * Tests basic new user creation
   */
  @Test
  public void newUserCreation() throws Exception {
    Username expected = new Username("test", "localhost");
    String response = mockMvc.perform(post("/username/create")
        .with(request -> {
          request.setRemoteAddr("localhost");
          return request;
        })
        .content("test")
    ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    assertEquals(response, expected.getUsername());
    String addr = usernameDao.findAddrForUsername("test");
    assertEquals(addr, "localhost");
  }

  /**
   * Tests that a user with the same username and address can relogin
   */
 @Test
 public void sameUserRelogin() throws Exception {
   mockMvc.perform(post("/username/create")
       .with(request -> {
         request.setRemoteAddr("localhost");
         return request;
       })
       .content("test")
   ).andExpect(status().isOk());

   mockMvc.perform(post("/username/create")
       .with(request -> {
         request.setRemoteAddr("localhost");
         return request;
       })
       .content("test")
   ).andExpect(status().isOk());
 }

  /**
   * Tests that different addresses cannot login as the same username
   */
 @Test
 public void errorMultipleUsers() throws Exception {
   mockMvc.perform(post("/username/create")
       .with(request -> {
         request.setRemoteAddr("localhost");
         return request;
       })
       .content("test")
   ).andExpect(status().isOk());

   mockMvc.perform(post("/username/create")
       .with(request -> {
         request.setRemoteAddr("notlocalhost");
         return request;
       })
       .content("test")
   ).andExpect(status().isBadRequest());
 }

  /**
   * Tests that one address cannot have multiple usernames
   */
 @Test
 public void errorSameAddress() throws Exception {
   mockMvc.perform(post("/username/create")
       .with(request -> {
         request.setRemoteAddr("localhost");
         return request;
       })
       .content("test")
   ).andExpect(status().isOk());

   mockMvc.perform(post("/username/create")
       .with(request -> {
         request.setRemoteAddr("localhost");
         return request;
       })
       .content("test2")
   ).andExpect(status().isBadRequest());
  }
}
