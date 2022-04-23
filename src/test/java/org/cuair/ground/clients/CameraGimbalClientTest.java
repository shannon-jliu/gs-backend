package org.cuair.ground.clients;

import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.ROI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class CameraGimbalClientTest {

    CameraGimbalClient psClient = new CameraGimbalClient();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSendRois() throws ExecutionException, InterruptedException {
        ODLCUser odlcUser = new ODLCUser("testUser2", "testAddr2", ODLCUser.UserType.MDLCOPERATOR);
        Assignment assignment = new Assignment(null, odlcUser);
        ROI roiTest = new ROI(odlcUser, 1, 2, assignment);
        List<ROI> roiList = new ArrayList<ROI>();
        roiList.add(roiTest);
        ListenableFuture<ResponseEntity<String>> lf = psClient.sendMDLCGroundROIS(roiList);
        System.out.println(lf.get().toString());

    }

    @After
    public void tearDown() throws Exception {
    }
}