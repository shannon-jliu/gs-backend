package org.cuair.ground.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.ebean.Ebean;
import java.sql.Timestamp;
import org.cuair.ground.models.Image;
import org.cuair.ground.models.Image.ImgMode;
import org.cuair.ground.models.geotag.GimbalOrientation;
import org.cuair.ground.models.geotag.GpsLocation;
import org.cuair.ground.models.geotag.Telemetry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ImageDatabaseAccessorTest {

  private ImageDatabaseAccessor imageDao;

  private Image i1;

  /** Before each test, initialize models and empty tables */
  @Before
  public void setup() throws Exception {
    imageDao = (ImageDatabaseAccessor) DAOFactory
        .getDAO(DAOFactory.ModellessDAOType.IMAGE_DATABASE_ACCESSOR);

    GpsLocation gpsLoc1 = new GpsLocation(42.4475428000000008, -76.6122976999999992);
    Telemetry expectedTelemetry1 = new Telemetry(
        gpsLoc1,
        221.555125199999992,
        45.0,
        new GimbalOrientation(-30.0, 0.0)
    );
    i1 = new Image("/some/local/file/url", expectedTelemetry1, ImgMode.TRACKING, false, false, 0.0);
    i1.setTimestamp(new Timestamp(2345L));
  }

  /** After each test, drop all tables */
  @After
  public void cleanDb() {
    String sql = "TRUNCATE image RESTART IDENTITY CASCADE";
    Ebean.createSqlUpdate(sql).execute();
  }

  /** Tests that the dao returns the Image instance with the most recent timestamp */
  @Test
  public void testGetRecent() throws Exception {
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
    i2.setTimestamp(new Timestamp(1234L));
    imageDao.create(i2);

    assertEquals(i1, imageDao.getRecent());
  }

  /**
   * Tests that the dao correctly sets the hasMdlcAssignment property of the
   * image instance to true
   */
  @Test
  public void testSetImageHasMDLCAssignment() throws Exception {
    imageDao.create(i1);
    boolean hasMdlcAssignment = imageDao.setImageHasMDLCAssignment(i1);

    assert (hasMdlcAssignment);
    assert (i1.getHasMdlcAssignment());
  }

  /**
   * Tests that the dao correctly sets the hasAdlcAssignment property of the
   * image instance to true
   */
  @Test
  public void testSetImageHasADLCAssignment() throws Exception {
    imageDao.create(i1);
    boolean hasAdlcAssignment = imageDao.setImageHasADLCAssignment(i1);

    assertTrue(hasAdlcAssignment);
    assertTrue(i1.getHasAdlcAssignment());
  }
}
