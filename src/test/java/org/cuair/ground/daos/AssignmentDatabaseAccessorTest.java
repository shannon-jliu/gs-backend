//package org.cuair.ground.daos;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;
//
//import io.ebean.DB;
//import java.sql.Timestamp;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import org.cuair.ground.models.Assignment;
//import org.cuair.ground.models.Image;
//import org.cuair.ground.models.Image.ImgMode;
//import org.cuair.ground.models.ODLCUser;
//import org.cuair.ground.models.geotag.GimbalOrientation;
//import org.cuair.ground.models.geotag.GpsLocation;
//import org.cuair.ground.models.geotag.Telemetry;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class AssignmentDatabaseAccessorTest {
//
//  private AssignmentDatabaseAccessor assignmentDao;
//  private ImageDatabaseAccessor imageDao;
//
//  private ODLCUser user1;
//  private ODLCUser user2;
//  private Image image1;
//  private Image image2;
//  private Assignment assignment1;
//  private Assignment assignment2;
//  private List<Assignment> expected1;
//  private List<Assignment> expected2;
//
//  /** Before each test, initialize models and empty tables */
//  @Before
//  public void setup() throws Exception {
//    assignmentDao = (AssignmentDatabaseAccessor) DAOFactory
//        .getDAO(DAOFactory.ModellessDAOType.ASSIGNMENT_DATABASE_ACCESSOR);
//    imageDao = (ImageDatabaseAccessor) DAOFactory
//        .getDAO(DAOFactory.ModellessDAOType.IMAGE_DATABASE_ACCESSOR);
//
//    // Create models
//    user1 = new ODLCUser("Obi-Wan", "localhost", ODLCUser.UserType.MDLCTAGGER);
//    user2 = new ODLCUser("Obi-Wan 2.0", "localhost", ODLCUser.UserType.MDLCTAGGER);
//    GpsLocation gpsLoc = new GpsLocation(42.4475428000000008, -76.6122976999999992);
//    Telemetry telem1 = new Telemetry(
//        gpsLoc,
//        221.555125199999992,
//        45.0,
//        new GimbalOrientation(-30.0, 0.0)
//    );
//    Telemetry telem2 = new Telemetry(
//        gpsLoc,
//        221.555125199999992,
//        45.0,
//        new GimbalOrientation(-30.0, 0.0)
//    );
//    Timestamp t2 = new java.sql.Timestamp(new java.util.Date().getTime());
//    Timestamp t1 = new java.sql.Timestamp(new java.util.Date().getTime());
//    image1 = new Image(t1.getTime() + ".jpg", telem1, ImgMode.TRACKING, false, false, 0.0);
//    image1.setTimestamp(t1);
//    image2 = new Image(t2.getTime() + ".jpg", telem2, ImgMode.TRACKING, false, false, 0.0);
//    image2.setTimestamp(t2);
//    assignment1 = new Assignment(image1, user1);
//    assignment1.setTimestamp(new Timestamp(new Date().getTime()));
//    assignment2 = new Assignment(image2, user2);
//    assignment2.setTimestamp(new Timestamp(new Date().getTime()));
//    expected1 = new ArrayList<>();
//    expected2 = new ArrayList<>();
//  }
//
//  /** After each test, drop all tables */
//  @After
//  public void cleanDb() {
//    String[] tables = {"assignment", "image", "odlcuser"};
//    for (String table : tables) {
//      String sql = "TRUNCATE " + table + " RESTART IDENTITY CASCADE";
//      DB.createSqlUpdate(sql).execute();
//    }
//  }
//
//  /** Tests that the dao returns null for an MDLC user */
//  @Test
//  public void testGetMdlcWorkNoAssignment() {
//    Assignment a =
//        assignmentDao.getWork(new ODLCUser("Obi-Wan", "localhost", ODLCUser.UserType.MDLCTAGGER));
//
//    assertNull(a);
//  }
//
//  /** Tests that the dao returns null for an ADLC user */
//  @Test
//  public void testGetAdlcWorkNoAssignment() {
//    Assignment a =
//        assignmentDao.getWork(new ODLCUser("Obi-Wan", "localhost", ODLCUser.UserType.ADLC));
//
//    assertNull(a);
//  }
//
//  /** Tests that the dao correctly assigns MDLC work */
//  @Test
//  public void testGetMdlcWorkOneUnassignedImage() throws Exception {
//    GpsLocation gpsLoc = new GpsLocation(42.4475428000000008, -76.6122976999999992);
//    Telemetry telem = new Telemetry(
//        gpsLoc,
//        221.555125199999992,
//        45.0,
//        new GimbalOrientation(-30.0, 0.0)
//    );
//    Timestamp t = new java.sql.Timestamp(new java.util.Date().getTime());
//    Image image = new Image(t.getTime() + ".jpg", telem, ImgMode.TRACKING, false, false, 0.0);
//    image.setTimestamp(t);
//    imageDao.create(image);
//
//    ODLCUser user = new ODLCUser("Obi-Wan", "localhost", ODLCUser.UserType.MDLCTAGGER);
//
//    Timestamp before = new java.sql.Timestamp(new java.util.Date().getTime());
//    sleep();
//    Assignment assignment = assignmentDao.getWork(user);
//    sleep();
//    Timestamp after = new java.sql.Timestamp(new java.util.Date().getTime());
//
//    assertEquals(image, assignment.getImage());
//    assertTrue(assignment.getImage().getHasMdlcAssignment());
//    assertFalse(assignment.getImage().getHasAdlcAssignment());
//    assertEquals(user, assignment.getAssignee());
//    assertFalse(assignment.getDone());
//    assertEquals(1, assignment.getTimestamp().compareTo(before));
//    assertEquals(-1, assignment.getTimestamp().compareTo(after));
//  }
//
//  /** Tests that the dao correctly assigns ADLC work */
//  @Test
//  public void testGetAdlcWorkOneUnassignedImage() throws Exception {
//    imageDao.create(image1);
//    user1 = new ODLCUser("Obi-Wan", "localhost", ODLCUser.UserType.ADLC);
//    Assignment assignment = assignmentDao.getWork(user1);
//
//    assertEquals(image1, assignment.getImage());
//    assertFalse(assignment.getImage().getHasMdlcAssignment());
//    assertTrue(assignment.getImage().getHasAdlcAssignment());
//    assertEquals(user1, assignment.getAssignee());
//    assertFalse(assignment.getDone());
//  }
//
//  /** Check that the dao gets the oldest of all MDLC-unassigned images */
//  @Test
//  public void testGetMdlcWorkTwoUnassignedImages() throws Exception {
//    imageDao.create(image1);
//    imageDao.create(image2);
//    Assignment assignment = assignmentDao.getWork(user1);
//
//    assertEquals(image1, assignment.getImage());
//    assertTrue(assignment.getImage().getHasMdlcAssignment());
//    assertFalse(assignment.getImage().getHasAdlcAssignment());
//    assertEquals(user1, assignment.getAssignee());
//    assertFalse(assignment.getDone());
//  }
//
//  /** Check that the dao gets the oldest of all ADLC-unassigned images */
//  @Test
//  public void testGetAdlcWorkTwoUnassignedImages() throws Exception {
//    imageDao.create(image1);
//    imageDao.create(image2);
//    user1 = new ODLCUser("Obi-Wan", "localhost", ODLCUser.UserType.ADLC);
//    Assignment assignment = assignmentDao.getWork(user1);
//
//    assertEquals(image1, assignment.getImage());
////    assertFalse(assignment.getImage().getHasMdlcAssignment());
////    assertTrue(assignment.getImage().getHasAdlcAssignment());
////    assertEquals(user1, assignment.getAssignee());
//    assertFalse(assignment.getDone());
//  }
//
//  /** Check that the dao gets the MDLC-unassigned image */
//  @Test
//  public void testGetMdlcWorkOneAssignedImage() throws Exception {
//    imageDao.create(image1);
//    assignmentDao.create(assignment1);
//    imageDao.setImageHasMDLCAssignment(image1);
//    imageDao.create(image2);
//    Assignment assignment = assignmentDao.getWork(user2);
//
//    assertEquals(image2, assignment.getImage());
//    assertTrue(assignment.getImage().getHasMdlcAssignment());
//    assertFalse(assignment.getImage().getHasAdlcAssignment());
//    assertEquals(user2, assignment.getAssignee());
//    assertFalse(assignment.getDone());
//  }
//
//  /** Check that the dao gets the ADLC-unassigned image */
//  @Test
//  public void testGetAdlcWorkOneAssignedImage() throws Exception {
//    imageDao.create(image1);
//    user1 = new ODLCUser("Obi-Wan", "localhost", ODLCUser.UserType.ADLC);
//    assignment1.setAssignee(user1);
//
//    assignmentDao.create(assignment1);
//    imageDao.setImageHasADLCAssignment(image1);
//
//    imageDao.create(image2);
//    user2 = new ODLCUser("Obi-Wan 2.0", "localhost", ODLCUser.UserType.ADLC);
//    Assignment assignment = assignmentDao.getWork(user2);
//
//    assertEquals(image2, assignment.getImage());
//    assertFalse(assignment.getImage().getHasMdlcAssignment());
//    assertTrue(assignment.getImage().getHasAdlcAssignment());
//    assertEquals(user2, assignment.getAssignee());
//    assertFalse(assignment.getDone());
//  }
//
//  /** Tests that the dao returns nothing with all images MDLC-assigned */
//  @Test
//  public void testGetMdlcWorkAllAssignedImages() throws Exception {
//    assignmentDao.create(assignment1);
//    imageDao.setImageHasMDLCAssignment(image1);
//    Assignment assignmentReturned = assignmentDao.getWork(user2);
//
//    assertNull(assignmentReturned);
//  }
//
//  /** Tests that the dao returns nothing with all images ADLC-assigned */
//  @Test
//  public void testGetAdlcWorkAllAssignedImages() throws Exception {
//    imageDao.create(image1);
//    user1 = new ODLCUser("Obi-Wan", "localhost", ODLCUser.UserType.ADLC);
//    assignment1.setAssignee(user1);
//
//    assignmentDao.create(assignment1);
//    imageDao.setImageHasADLCAssignment(image1);
//
//    user2 = new ODLCUser("Obi-Wan 2.0", "localhost", ODLCUser.UserType.ADLC);
//    Assignment assignmentReturned = assignmentDao.getWork(user2);
//
//    assertNull(assignmentReturned);
//  }
//
//  /** Tests that the dao returns an MDLC-unassigned image that has been ADLC-assigned */
//  @Test
//  public void testGetMdlcWorkAdlcAssigned() throws Exception {
//    assignmentDao.create(assignment1);
//    imageDao.setImageHasADLCAssignment(image1);
//    Assignment assignmentReturned = assignmentDao.getWork(user2);
//
//    assertEquals(image1, assignmentReturned.getImage());
//    assertTrue(assignmentReturned.getImage().getHasMdlcAssignment());
//    assertTrue(assignmentReturned.getImage().getHasAdlcAssignment());
//    assertEquals(user2, assignmentReturned.getAssignee());
//    assertFalse(assignmentReturned.getDone());
//  }
//
//  /** Tests that the dao returns an ADLC-unassigned image that has been MDLC-assigned */
//  @Test
//  public void testGetAdlcWorkMdlcAssigned() throws Exception {
//    imageDao.create(image1);
//    assignmentDao.create(assignment1);
//    imageDao.setImageHasMDLCAssignment(image1);
//
//    user2 = new ODLCUser("Obi-Wan 2.0", "localhost", ODLCUser.UserType.ADLC);
//    Assignment assignmentReturned = assignmentDao.getWork(user2);
//
//    assertEquals(image1, assignmentReturned.getImage());
//    assertTrue(assignmentReturned.getImage().getHasMdlcAssignment());
//    assertTrue(assignmentReturned.getImage().getHasAdlcAssignment());
//    assertEquals(user2, assignmentReturned.getAssignee());
//    assertFalse(assignmentReturned.getDone());
//  }
//
//  /** Tests that the dao returns nothing with all images MDLC-assigned and assignments done */
//  @Test
//  public void testGetMdlcWorkAllAssignedAndDoneImages() throws Exception {
//    assignmentDao.create(assignment1);
//    imageDao.setImageHasMDLCAssignment(image1);
//    assignment1.setDone(true);
//    Assignment assignmentReturned = assignmentDao.getWork(user2);
//
//    assertNull(assignmentReturned);
//  }
//
//  /** Tests that the dao returns nothing with all images ADLC-assigned and assignments done */
//  @Test
//  public void testGetAdlcWorkAllAssignedAndDoneImages() throws Exception {
//    imageDao.create(image1);
//    user1 = new ODLCUser("Obi-Wan", "localhost", ODLCUser.UserType.ADLC);
//    assignment1.setAssignee(user1);
//
//    assignment1.setDone(true);
//    assignmentDao.create(assignment1);
//    imageDao.setImageHasADLCAssignment(image1);
//
//    user2 = new ODLCUser("Obi-Wan 2.0", "localhost", ODLCUser.UserType.ADLC);
//    Assignment assignmentReturned = assignmentDao.getWork(user2);
//
//    assertNull(assignmentReturned);
//  }
//
//  /**
//   * Tests that the dao returns an MDLC-unassigned image that has been ADLC-assigned and
//   * has a done assignment
//   */
//  @Test
//  public void testGetMdlcWorkAdlcAssignedAndDone() throws Exception {
//    assignment1.setDone(true);
//    assignmentDao.create(assignment1);
//    imageDao.setImageHasADLCAssignment(image1);
//    Assignment assignmentReturned = assignmentDao.getWork(user2);
//
//    assertEquals(image1, assignmentReturned.getImage());
//    assertTrue(assignmentReturned.getImage().getHasMdlcAssignment());
//    assertTrue(assignmentReturned.getImage().getHasAdlcAssignment());
//    assertEquals(user2, assignmentReturned.getAssignee());
//    assertFalse(assignmentReturned.getDone());
//  }
//
//  /**
//   * Tests that the dao returns an ADLC-unassigned image that has been MDLC-assigned and
//   * has a done assignment
//   */
//  @Test
//  public void testGetAdlcWorkMdlcAssignedAndDone() throws Exception {
//    imageDao.create(image1);
//    assignment1.setDone(true);
//    assignmentDao.create(assignment1);
//    imageDao.setImageHasMDLCAssignment(image1);
//
//    user2 = new ODLCUser("Obi-Wan 2.0", "localhost", ODLCUser.UserType.ADLC);
//    Assignment assignmentReturned = assignmentDao.getWork(user2);
//
//    assertEquals(image1, assignmentReturned.getImage());
//    assertTrue(assignmentReturned.getImage().getHasMdlcAssignment());
//    assertTrue(assignmentReturned.getImage().getHasAdlcAssignment());
//    assertEquals(user2, assignmentReturned.getAssignee());
//    assertFalse(assignmentReturned.getDone());
//  }
//
//  /** Tests no MDLC assignments are gotten for the user */
//  @Test
//  public void testGetForUserNoMDLCAssignmentsOneUser() throws Exception {
//    List<Assignment> returned = assignmentDao.getAllForUser(user1);
//    assertEquals(expected1, returned);
//  }
//
//  /** Tests no ADLC assignments are gotten for the user */
//  @Test
//  public void testGetForUserNoADLCAssignmentsOneUser() throws Exception {
//    user1 = new ODLCUser("Obi-Wan", "localhost", ODLCUser.UserType.ADLC);
//
//    List<Assignment> returned = assignmentDao.getAllForUser(user1);
//    assertEquals(expected1, returned);
//  }
//
//  /** Tests the correct MDLC assignments are gotten for multiple users */
//  @Test
//  public void testGetForUserMultipleMDLCAssignmentsMultipleUsers() throws Exception {
//    imageDao.create(image1);
//    Assignment a = assignmentDao.getWork(user1);
//    expected1.add(a);
//
//    imageDao.create(image2);
//    a = assignmentDao.getWork(user1);
//    expected1.add(a);
//
//    GpsLocation gpsLoc3 = new GpsLocation(42.4475428000000008, -76.6122976999999992);
//    Telemetry telem3 = new Telemetry(
//        gpsLoc3,
//        221.555125199999992,
//        45.0,
//        new GimbalOrientation(-30.0, 0.0)
//    );
//    Timestamp t3 = new java.sql.Timestamp(new java.util.Date().getTime());
//    Image image3 = new Image(t3.getTime() + ".jpg", telem3, ImgMode.TRACKING, false, false, 0.0);
//    image3.setTimestamp(t3);
//    imageDao.create(image3);
//    a = assignmentDao.getWork(user2);
//    expected2.add(a);
//
//    GpsLocation gpsLoc4 = new GpsLocation(42.4475428000000008, -76.6122976999999992);
//    Telemetry telem4 = new Telemetry(
//        gpsLoc4,
//        221.555125199999992,
//        45.0,
//        new GimbalOrientation(-30.0, 0.0)
//    );
//    Timestamp t4 = new java.sql.Timestamp(new java.util.Date().getTime());
//    Image image4 = new Image(t4.getTime() + ".jpg", telem4, ImgMode.TRACKING, false, false, 0.0);
//    image4.setTimestamp(t4);
//    imageDao.create(image4);
//    a = assignmentDao.getWork(user2);
//    expected2.add(a);
//
//    List<Assignment> returned1 = assignmentDao.getAllForUser(user1);
//    List<Assignment> returned2 = assignmentDao.getAllForUser(user2);
//    assertEquals(expected1, returned1);
//    assertEquals(expected2, returned2);
//  }
//
//  /** Tests nothing is returned for an image that has yet to be assigned */
//  @Test
//  public void testGetAllForImageIdNotAssigned() throws Exception {
//    List<Assignment> returned = assignmentDao.getAllForImageId(new Long(1));
//    assertEquals(expected1, returned);
//  }
//
//  /** Tests the correct assignment is returned for an image that has been assigned */
//  @Test
//  public void testGetAllForImageIdAssigned() throws Exception {
//    imageDao.create(image1);
//    Assignment a = assignmentDao.getWork(user1);
//    expected1.add(a);
//
//    List<Assignment> returned = assignmentDao.getAllForImageId(image1.getId());
//    assertEquals(expected1, returned);
//  }
//
//  private void sleep() {
//    try {
//      Thread.sleep(1);
//    } catch (InterruptedException e) {
//      fail("Thread interrupted");
//    }
//  }
//}
