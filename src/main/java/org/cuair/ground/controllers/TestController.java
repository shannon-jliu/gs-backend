package org.cuair.ground.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class TestController {

    @RequestMapping(value = "/")
    public String index() {
      return "test";
    }

}
