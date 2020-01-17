package org.cuair.ground.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Value;

@RestController
public class TestController {

    // Unused, temporarily for display purposes
    @Value("${cuair.plane.camera_gimbal}") public String sampleVar;

    @RequestMapping(value = "/")
    public String index() {
        return "test";
    }

}
