package org.cuair.ground.clients;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class InteropClientTest {

    InteropClient iopClient;

    @Before
    public void setUp() throws Exception {
        iopClient = new InteropClient();
    }

    @Test
    public void something() {
        System.out.println("Test");
        System.out.println("wow");
    }

    @Test
    public void testAttemptLogin() {

    }


    @After
    public void tearDown() throws Exception {
    }
}