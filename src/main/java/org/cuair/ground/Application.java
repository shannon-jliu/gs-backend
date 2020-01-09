package org.cuair.ground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.annotation.Transactional;

import org.cuair.ground.models.plane.settings.CameraGimbalSettings;
import org.cuair.ground.models.plane.settings.CameraGimbalSettings.CameraGimbalMode;

@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
        insertAndDeleteInsideTransaction();
	}

    @Transactional
    public static void insertAndDeleteInsideTransaction() {
        CameraGimbalSettings camGimSetting = new CameraGimbalSettings(CameraGimbalMode.IDLE);
        EbeanServer server = Ebean.getDefaultServer();
        server.save(camGimSetting);

        CameraGimbalSettings foundcamGimSetting = server.find(CameraGimbalSettings.class, camGimSetting.getId());

        System.out.println("ff a;sldkjf");
        System.out.println(Long.toString(camGimSetting.getId()));
        System.out.println(Long.toString(foundcamGimSetting.getId()));
        System.out.println("ff a;sldkjf");
        // Customer foundC1 = server.find(Customer.class, c1.getId());
        // server.delete(foundC1);
    }
}
