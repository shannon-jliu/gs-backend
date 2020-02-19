package org.cuair.ground.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import org.cuair.ground.clients.*;
import org.cuair.ground.models.plane.target.*;
import org.cuair.ground.models.*;

@RestController
public class TestController {

    @RequestMapping(value = "/")
    public String index() {
      return "test";
    }

    @RequestMapping(value = "/testinteroptarget", method = RequestMethod.GET)
    public void testinterop() {
    	InteropClient interopClient = ClientFactory.getInteropClient();
    	AlphanumTarget alphaTarget = new AlphanumTarget(ClientType.MDLC, Shape.CIRCLE, Color.BLACK, "A", Color.WHITE, false, null, 1L, 1L);
    	interopClient.attemptSend(alphaTarget);
    }

    @RequestMapping(value = "/testinteropimage", method = RequestMethod.GET)
    public void testImage() {
    	InteropClient interopClient = ClientFactory.getInteropClient();
    	AlphanumTarget alphaTarget = new AlphanumTarget(ClientType.MDLC, Shape.CIRCLE, Color.BLACK, "A", Color.WHITE, false, null, 1L, 1L);
    	interopClient.attemptSend(alphaTarget);
    }

}
