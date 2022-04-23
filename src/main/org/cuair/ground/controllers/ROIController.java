package org.cuair.ground.controllers;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.cuair.ground.clients.CameraGimbalClient;
import org.cuair.ground.daos.AssignmentDatabaseAccessor;
import org.cuair.ground.daos.ClientCreatableDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.PlaneSettingsModelDatabaseAccessor;
import org.cuair.ground.models.Assignment;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.ROI;
import org.cuair.ground.models.plane.settings.CameraGimbalSettings;
import org.cuair.ground.util.Clustering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/** Contains all the callbacks for all the public api endpoints for the ROI */
@CrossOrigin
@RestController
@RequestMapping(value = "/roi")
public class ROIController {
  private static final Logger logger = LoggerFactory.getLogger(ROIController.class);
  /** The database accessor object for the ROI table */
  private static ClientCreatableDatabaseAccessor<ROI> roiDao =
      (ClientCreatableDatabaseAccessor<ROI>) DAOFactory
          .getDAO(DAOFactory.ModelDAOType.CLIENT_CREATABLE_DATABASE_ACCESSOR, ROI.class);
  /** The database accessor object for the camera gimbal settings table */
  private static PlaneSettingsModelDatabaseAccessor<CameraGimbalSettings> cgsDao =
      (PlaneSettingsModelDatabaseAccessor<CameraGimbalSettings>) DAOFactory
          .getDAO(DAOFactory.ModelDAOType.PLANE_SETTINGS_MODEL_DATABASE_ACCESSOR,
              CameraGimbalSettings.class);
  /** The database accessor object for the assignment table */
  private AssignmentDatabaseAccessor assignmentDao =
      (AssignmentDatabaseAccessor)
          DAOFactory.getDAO(DAOFactory.ModellessDAOType.ASSIGNMENT_DATABASE_ACCESSOR);

  private static void createAndUpdateAveragedRois(ROI roi) {
    roiDao.create(roi);
    CameraGimbalSettings cgs = cgsDao.getRecentUnfailed();

    if (cgs != null && cgs.getMode() == CameraGimbalSettings.CameraGimbalMode.TRACKING) {
      roiDao.getAll()
          .stream()
          .filter(r -> r.isAveraged())
          .map(r -> roiDao.delete(r.getId()));

      List<ROI> rois =
          roiDao.getAll().stream().filter(r -> !r.isAveraged()).collect(Collectors.toList());
      List<ROI> averagedRois = Clustering.cluster(rois);
      averagedRois.stream().map(r -> roiDao.create(r));
    }
  }

  /**
   * Constructs an HTTP response with all the ROIs
   *
   * @return a list of all ROIs in the db
   */
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity getAll() {
    return ok(roiDao.getAll());
  }

  /**
   * Constructs a HTTP response with the ROI with id `id`
   *
   * @param id Long id for ROI
   * @return the ROI corresponding to the provided id on success, 204
   * when said ROI does not exist
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public ResponseEntity get(@PathVariable Long id) {
    ROI roi = roiDao.get(id);
    return (roi != null) ? ok(roi) : noContent().build();
  }

  /**
   * Creates an ROI on our server given the request body json. Constructs a HTTP response with the
   * json of the ROI that was created.
   *
   * @return the created ROI on success, 204 when the corresponding assignment does not exist, or 400
   * when the provided ROI is formatted improperly
   */
  @RequestMapping(value = "/{assignmentID}", method = RequestMethod.POST)
  public ResponseEntity create(@PathVariable Long assignmentID, @RequestBody ROI roi) {
    Assignment a = assignmentDao.get(assignmentID);
    if (a == null) {
      return notFound().build();
    }

    if (roi.getId() != null) {
      return badRequest().body("Don't pass ids for create");
    }

    if (roi.getCreator() == null) {
      return badRequest().body("Must specify creator for creating ROI");
    }

    if (!roi.getCreator().equals(a.getAssignee())) {
      return badRequest().body("Creator client type does not match client type of assignment");
    }

    // ADLC should be the only one creating averaged ROIs. Averaged ROIs for MDLC is handled by the ground server
    if (roi.isAveraged() && (roi.getCreator().getUserType() == ODLCUser.UserType.MDLCTAGGER
        || roi.getCreator().getUserType() == ODLCUser.UserType.MDLCOPERATOR)) {
      return badRequest().body("MDLC cannot create averaged ROIs");
    }

    roi.setAssignment(a);

    if (roi.getPixelx() == null || roi.getPixely() == null) {
      return badRequest().body("Missing pixel location information for ROI");
    }

    if (roi.getGpsLocation() == null) {
      return badRequest().body("Could not calculate GpsLocation with given input");
    }

    ROIController.createAndUpdateAveragedRois(roi);

    List<ROI> rois = new ArrayList<>();
    rois.add(roi);
    CameraGimbalClient client = new CameraGimbalClient();
    client.sendMDLCGroundROIS(rois);

    return ok(roi);
  }

  /**
   * Constructs an HTTP response after deleting a ROI given an id
   *
   * @param id Long id of ROI
   * @return the deleted ROI corresponding to the provided id on success, 204
   * when said ROI does not exist
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public ResponseEntity delete(@PathVariable Long id) {
    ROI roi = roiDao.get(id);
    if (roi == null) {
      return notFound().build();
    }
    roiDao.delete(id);
    return ok(roi);
  }
}
