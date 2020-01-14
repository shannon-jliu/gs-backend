package org.cuair.ground.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.cuair.ground.util.Flags;

@RestController
public class TestController {

	// Unused, temporarily for display purposes
	private String CUAIR_PLANE_CAMERA_GIMBAL = Flags.CUAIR_PLANE_CAMERA_GIMBAL;

    @RequestMapping(value = "/")
    public String index() {
      return "test";
    }

}
