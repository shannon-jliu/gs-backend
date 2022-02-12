package org.cuair.ground.clients;

import org.cuair.ground.models.*;
import org.cuair.ground.models.exceptions.InvalidGpsLocationException;
import org.cuair.ground.models.geotag.Geotag;
import org.cuair.ground.models.geotag.GpsLocation;
import org.cuair.ground.models.plane.target.AlphanumTarget;
import org.cuair.ground.util.RequestUtil;
import org.cuair.ground.models.plane.target.EmergentTargetSighting;
import org.cuair.ground.util.Flags;
import org.cuair.ground.util.RequestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import java.util.concurrent.TimeUnit;

import java.lang.annotation.Target;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class InteropClientTest {

    InteropClient iopClient;

    @Before
    public void setUp() throws Exception {
        iopClient = new InteropClient();
    }

    @Test
    public void getMissionData() throws ExecutionException, InterruptedException {
        System.out.println(iopClient.getMissionData().get().getBody());
    }

    @Test
    public void getSentTargets() throws ExecutionException, InterruptedException {
        System.out.println(iopClient.getSentTargets().get().getBody());
    }

    @Test
    public void sendTargets() throws InvalidGpsLocationException, ExecutionException, InterruptedException {
        ODLCUser odlcUser = new ODLCUser("testUser2", "testAddr2", ODLCUser.UserType.MDLCOPERATOR);

        GpsLocation gps = new GpsLocation(80, 70.2);
        Geotag geotag  =  new Geotag(gps, 1.0);

        ListenableFuture<ResponseEntity<String>> beforeTarget = iopClient.getSentTargets();
        System.out.println("response: " + beforeTarget.get());

        AlphanumTarget original = new AlphanumTarget(
                odlcUser,
                Shape.SQUARE,
                Color.BLUE,
                'A',
                Color.BLACK,
                false,
                geotag,
                null,
                1L
        );

        System.out.println(original.toInteropJson());
//        NOTE: if the database already has NO information comment this out for the test
//        This assumes that the database begins EMPTY
        iopClient.createTarget(original);
        ListenableFuture<ResponseEntity<String>> afterTarget = iopClient.getSentTarget(original.getJudgeTargetId());
        System.out.println("response: " + afterTarget.get().getBody());
        System.out.println("target id: " + original.getJudgeTargetId());


        iopClient.createTarget(original);
//        System.out.println("ERROR: " + k);
//        TimeUnit.SECONDS.sleep(4);


        afterTarget = iopClient.getSentTarget(original.getJudgeTargetId());

        System.out.println("response: " + afterTarget.get().getBody());
        System.out.println("target id: " + original.getJudgeTargetId());


    }

    @Test
    public void testUpdateTargets() throws ExecutionException, InterruptedException, InvalidGpsLocationException {
        ODLCUser odlcUser = new ODLCUser("testUser2", "testAddr2", ODLCUser.UserType.MDLCOPERATOR);

        GpsLocation gps = new GpsLocation(80, 70.2);
        Geotag geotag  =  new Geotag(gps, 1.0);

        ListenableFuture<ResponseEntity<String>> beforeTarget = iopClient.getSentTargets();
        System.out.println("response: " + beforeTarget.get());

        AlphanumTarget original = new AlphanumTarget(
                odlcUser,
                Shape.SQUARE,
                Color.BLUE,
                'A',
                Color.BLACK,
                false,
                geotag,
                null,
                1L
        );

        iopClient.createTarget(original);
        original.setShapeColor(Color.RED);
        iopClient.updateTarget(original);
        System.out.println(iopClient.getSentTarget((original.getJudgeTargetId())).get().getBody());
    }

    @Test
    public void getSentTarget() throws ExecutionException, InterruptedException {
        System.out.println(iopClient.getSentTarget(12).get().getBody());
    }


    @Test
    public void testGetSentTargets() throws ExecutionException, InterruptedException {
        System.out.println("getSentTargets: " + (iopClient.getSentTargets()).get());
    }

    public boolean testTargetCreate(AlphanumTarget t) throws ExecutionException, InterruptedException {
        try{
            iopClient.createTarget(t);
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean testTargetUpdate(AlphanumTarget t){
        try {
            iopClient.updateTarget(t);
        }
        catch (Exception e){
            return false;
        }
        return true;
    }

    @Test
    public void testManyTarget() throws InvalidGpsLocationException, ExecutionException, InterruptedException {
        ODLCUser odlcUser = new ODLCUser("testUser2", "testAddr2", ODLCUser.UserType.MDLCOPERATOR);

        GpsLocation gps = new GpsLocation(80, 70.2);
        Geotag geotag  =  new Geotag(gps, 1.0);

        ListenableFuture<ResponseEntity<String>> beforeTarget = iopClient.getSentTargets();
//        System.out.println("response: " + beforeTarget.get());

        AlphanumTarget original = new AlphanumTarget(
                odlcUser,
                Shape.SQUARE,
                Color.BLUE,
                'A',
                Color.BLACK,
                false,
                geotag,
                12L,
                1L
        );
        assertTrue(testTargetCreate(original));
        original.setAlpha('A');
        assertTrue(testTargetUpdate(original));
    }


    @After
    public void tearDown() throws Exception {
    }
}