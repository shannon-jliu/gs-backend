package org.cuair.ground.lifecycle;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import org.cuair.ground.clients.ClientFactory;
import org.cuair.ground.daos.ClientCreatableDatabaseAccessor;
import org.cuair.ground.daos.DAOFactory;
import org.cuair.ground.daos.AlphanumTargetDatabaseAccessor;
import org.cuair.ground.models.plane.target.AlphanumTarget;
import org.cuair.ground.models.plane.target.EmergentTarget;
import org.cuair.ground.models.ClientType;
import org.cuair.ground.clients.InteropClient;
import org.cuair.ground.util.Flags;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class Lifecycle
{

    private static final Logger logger = LoggerFactory.getLogger(Lifecycle.class);

    private static InteropClient interopClient = ClientFactory.getInteropClient();

    private static boolean CUAIR_INTEROP_REQUESTS = Flags.CUAIR_INTEROP_REQUESTS;

    private static AlphanumTargetDatabaseAccessor<AlphanumTarget> alphaTargetDao = (AlphanumTargetDatabaseAccessor<AlphanumTarget>)DAOFactory.getDAO(
                DAOFactory.ModelDAOType.ALPHANUM_TARGET_DATABASE_ACCESSOR, AlphanumTarget.class);

    private static ClientCreatableDatabaseAccessor<EmergentTarget> emergentTargetDao = (ClientCreatableDatabaseAccessor<EmergentTarget>)DAOFactory.getDAO(
                DAOFactory.ModelDAOType.CLIENT_CREATABLE_DATABASE_ACCESSOR, EmergentTarget.class);

    private static String DEFAULT_EMERGENT_TARGET_DESC = Flags.DEFAULT_EMERGENT_TARGET_DESC;

    @PostConstruct
    public void startUp()
    {
        initializeOffaxisTargetDatabase();
        initializeEmergentTargetDatabase();
        try
        {
            interopClient.startInteropSequence();
        }
        catch (Exception e)
        {
            logger.error("Startup exception: " + e.getMessage());
        }
    }

    private static void initializeOffaxisTargetDatabase()
    {
        AlphanumTarget offaxisTarget = alphaTargetDao.getOffaxisTarget();
        if (offaxisTarget != null && offaxisTarget.getJudgeTargetId() != null) return;
        if (offaxisTarget == null)
        {
            offaxisTarget =
                new AlphanumTarget(ClientType.MDLC, null, null, null, null, true, null, null, 0L);
            alphaTargetDao.create(offaxisTarget);
        }
    }

    private static void initializeEmergentTargetDatabase()
    {
        List<EmergentTarget> emergentTargets = emergentTargetDao.getAll();
        EmergentTarget emergentTarget = emergentTargets.isEmpty() ? null : emergentTargets.get(0);
        if (emergentTarget != null && emergentTarget.getJudgeTargetId() != null) return;
        if (emergentTarget == null)
        {
            emergentTarget =
                new EmergentTarget(ClientType.MDLC, null, DEFAULT_EMERGENT_TARGET_DESC, null, 0L);
            emergentTargetDao.create(emergentTarget);
        }
    }

    @PreDestroy
    public void shutDown()
    {

    }
}
