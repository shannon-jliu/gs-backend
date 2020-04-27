package org.cuair.ground;

import static org.junit.Assert.assertEquals;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import java.util.List;
import org.cuair.ground.models.plane.settings.CameraGimbalSettings;
import org.cuair.ground.models.plane.settings.CameraGimbalSettings.CameraGimbalMode;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EbeanTest {

  private EbeanServer server;

  private CameraGimbalSettings camGimSetting;

  /**
   * Before each test, initialize Ebean server
   */
  @Before
  public void createEbeanServer() {
    ServerConfig config = new ServerConfig();
    config.setName("db");
    config.loadFromProperties();

    server = EbeanServerFactory.create(config);

    camGimSetting = new CameraGimbalSettings(CameraGimbalMode.IDLE);
  }

  @Test
  public void saveTest() throws Exception {
    server.save(camGimSetting);
    CameraGimbalSettings foundcamGimSetting =
        server.find(CameraGimbalSettings.class, camGimSetting.getId());
    assertEquals(camGimSetting.getId(), foundcamGimSetting.getId());

    List<CameraGimbalSettings> settings = Ebean.find(CameraGimbalSettings.class).findList();
    assertEquals(1, settings.size());
  }
}
