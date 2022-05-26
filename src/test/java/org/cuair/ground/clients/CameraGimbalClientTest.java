package org.cuair.ground.clients;

import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.ROI;
import org.cuair.ground.models.exceptions.InvalidGpsLocationException;
import org.cuair.ground.models.geotag.GpsLocation;
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
    ODLCUser odlcUser;

    @Before
    public void setUp() throws Exception {
        odlcUser = new ODLCUser("testUser2", "testAddr2", ODLCUser.UserType.MDLCOPERATOR);

    }

    @Test
    public void testSendRois() throws ExecutionException, InterruptedException, InvalidGpsLocationException {
        // Test location
        GpsLocation gps = new GpsLocation(12L, 13L);
        ROI roiTest = new ROI(odlcUser, gps);
        List<ROI> roiList = new ArrayList<>();
        roiList.add(roiTest);
        System.out.println(psClient.roisToJson(roiList));
        ListenableFuture<ResponseEntity<String>> lf = psClient.sendMDLCGroundROIS(roiList);

        // Assert successful upload
        assert(lf.get().getStatusCode().value() == 200);
        System.out.println(lf.get().toString());
    }

    @Test
    public void testGetRois() throws Exception {
        ResponseEntity<String> re = psClient.getRegisteredROIs();
        System.out.println(re.toString());
        // Assert successful download of ROIS from plane system
        assert(re.getStatusCode().value() == 200);
    }

    @After
    public void tearDown() throws Exception {
    }
}