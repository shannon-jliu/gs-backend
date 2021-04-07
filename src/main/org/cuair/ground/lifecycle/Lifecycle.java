package org.cuair.ground.lifecycle;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.cuair.ground.clients.InteropClient;
import org.cuair.ground.daos.AlphanumTargetDatabaseAccessor;
import org.cuair.ground.daos.ClientCreatableDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.ODLCUserDatabaseAccessor;
import org.cuair.ground.models.ODLCUser;
import org.cuair.ground.models.plane.target.AlphanumTarget;
import org.cuair.ground.models.plane.target.EmergentTarget;
import org.cuair.ground.util.Flags;
import org.springframework.stereotype.Component;

/* Lifecycle component, contains startup and shutdown logic for server. */
@Component
public class Lifecycle {
  private static AlphanumTargetDatabaseAccessor<AlphanumTarget> alphaTargetDao =
      (AlphanumTargetDatabaseAccessor<AlphanumTarget>)
          DAOFactory.getDAO(
              DAOFactory.ModelDAOType.ALPHANUM_TARGET_DATABASE_ACCESSOR, AlphanumTarget.class);

  private static ClientCreatableDatabaseAccessor<EmergentTarget> emergentTargetDao =
      (ClientCreatableDatabaseAccessor<EmergentTarget>)
          DAOFactory.getDAO(
              DAOFactory.ModelDAOType.CLIENT_CREATABLE_DATABASE_ACCESSOR, EmergentTarget.class);

  private ODLCUserDatabaseAccessor odlcUserDao = (ODLCUserDatabaseAccessor) DAOFactory
      .getDAO(DAOFactory.ModellessDAOType.ODLCUSER_DATABASE_ACCESSOR);

  private static InteropClient interopClient = new InteropClient();

  private static String DEFAULT_EMERGENT_TARGET_DESC = Flags.DEFAULT_EMERGENT_TARGET_DESC;

  /**
   * Creates an initial offaxis target in the db when the backend boots up.
   * We do this because there is only one offaxis target, and we only want
   * to update this offaxis target, not create new offaxis ones.
   */
  private static void initializeOffaxisTargetDatabase() {
    AlphanumTarget offaxisTarget = alphaTargetDao.getOffaxisTarget();
    if (offaxisTarget != null && offaxisTarget.getJudgeTargetId() != null) {
      return;
    }
    if (offaxisTarget == null) {
      offaxisTarget =
          new AlphanumTarget(
              new ODLCUser("interop", "localhost", ODLCUser.UserType.MDLCOPERATOR),
              null,
              null,
              'A',
              null,
              true,
              null,
              null,
              0L);
      alphaTargetDao.create(offaxisTarget);
    }
  }

  /**
   * Creates an initial emergent target in the db when the backend boots up.
   * We do this because there is only one emergent target, and we only want
   * to update this offaxis target, not create new offaxis ones. In addition,
   * on startup, we receive the GPS location of the emergent target from
   * interterop (the judges).
   */
  private static void initializeEmergentTargetDatabase() {
    List<EmergentTarget> emergentTargets = emergentTargetDao.getAll();
    EmergentTarget emergentTarget = emergentTargets.isEmpty() ? null : emergentTargets.get(0);
    if (emergentTarget != null && emergentTarget.getJudgeTargetId() != null) {
      return;
    }
    if (emergentTarget == null) {
      emergentTarget =
          new EmergentTarget(
              new ODLCUser("interop", "localhost", ODLCUser.UserType.ADLC),
              null,
              DEFAULT_EMERGENT_TARGET_DESC,
              null,
              0L);
      emergentTargetDao.create(emergentTarget);
    }
  }

  /**
   * Runs all necessary startup functions
   */
  @PostConstruct
  public void startUp() {
    interopClient.attemptLogin();
    initializeOffaxisTargetDatabase();
    initializeEmergentTargetDatabase();
    if (odlcUserDao.getADLCUser() == null) {
      odlcUserDao.create(new ODLCUser("adlc", "", ODLCUser.UserType.ADLC));
    }
  }

  /**
   * Runs all necessary shutdown functions
   */
  @PreDestroy
  public void shutDown() {

  }
}
