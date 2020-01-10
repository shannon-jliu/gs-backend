package org.cuair.ground;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.annotation.Transactional;
import io.ebean.config.ServerConfig;

import org.cuair.ground.models.plane.settings.CameraGimbalSettings;
import org.cuair.ground.models.plane.settings.CameraGimbalSettings.CameraGimbalMode;

import org.springframework.boot.test.context.SpringBootTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

import java.util.Properties;
import java.util.List;

@SpringBootTest
public class EbeanTest {

    private EbeanServer server;

    private CameraGimbalSettings camGimSetting;

    /** Before each test, initialize Ebean server */
    @Before
    public void createEbeanServer() {
        // Properties props = new Properties();
        // props.put("ebean.db.ddl.generate", "true");
        // props.put("ebean.db.ddl.run", "true");
        // props.put("datasource.db.username", "postgres");
        // props.put("datasource.db.password", "admin");
        // props.put("datasource.db.databaseUrl", "jdbc:postgresql://postgres_db:5432/groundserver");
        // props.put("datasource.db.databaseDriver", "org.postgresql.Driver");

        ServerConfig config = new ServerConfig();
        config.setName("db");
        config.loadFromProperties();

        server = EbeanServerFactory.create(config);

        camGimSetting = new CameraGimbalSettings(CameraGimbalMode.IDLE);
    }

    @Test
    public void saveTest() throws Exception {
        server.save(camGimSetting);
        CameraGimbalSettings foundcamGimSetting = server.find(CameraGimbalSettings.class, camGimSetting.getId());
        assertEquals(camGimSetting.getId(), foundcamGimSetting.getId());

        List<CameraGimbalSettings> settings = Ebean.find(CameraGimbalSettings.class).findList();
        assertEquals(1, settings.size());
    }
}
