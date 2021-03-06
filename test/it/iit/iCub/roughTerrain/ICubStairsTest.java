package it.iit.iCub.roughTerrain;

import org.junit.Test;

import it.iit.iCub.testTools.ICubTest;
import it.iit.iCub.testTools.TestingEnvironment;
import us.ihmc.euclid.geometry.BoundingBox3D;
import us.ihmc.euclid.referenceFrame.FramePoint3D;
import us.ihmc.euclid.referenceFrame.FrameQuaternion;
import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.tuple3D.Point3D;
import us.ihmc.euclid.tuple3D.interfaces.Point3DReadOnly;
import us.ihmc.humanoidRobotics.communication.packets.walking.FootstepDataListMessage;
import us.ihmc.humanoidRobotics.communication.packets.walking.FootstepDataMessage;
import us.ihmc.robotModels.FullHumanoidRobotModel;
import us.ihmc.robotics.robotSide.RobotSide;
import us.ihmc.robotics.screwTheory.MovingReferenceFrame;
import us.ihmc.simulationConstructionSetTools.util.environments.CommonAvatarEnvironmentInterface;
import us.ihmc.simulationconstructionset.util.simulationRunner.BlockingSimulationRunner.SimulationExceededMaximumTimeException;

public class ICubStairsTest extends ICubTest
{
   @Test
   public void testStairsWalking() throws SimulationExceededMaximumTimeException
   {
      simulate(0.5);

      FullHumanoidRobotModel fullRobotModel = getTestHelper().getControllerFullRobotModel();
      double stepLength = 0.2;
      double stepWidth = 0.15;
      double distance = StairsEnvironment.end + 0.5;

      double swingTime = 1.2;
      double transferTime = 0.6;

      FootstepDataListMessage message = new FootstepDataListMessage(swingTime, transferTime);
      double lastX = 0.0;
      RobotSide side = RobotSide.LEFT;
      int stepIdx = 0;

      while (lastX < distance)
      {
         side = side.getOppositeSide();
         MovingReferenceFrame stanceSoleFrame = fullRobotModel.getSoleFrame(side.getOppositeSide());

         FramePoint3D position = new FramePoint3D(stanceSoleFrame);
         position.setX(stepLength * (++stepIdx));
         position.setY(side.negateIfRightSide(stepWidth));
         position.changeFrame(ReferenceFrame.getWorldFrame());
         lastX = position.getX();
         double height = StairsEnvironment.getHeight(lastX);
         position.setZ(height);

         FrameQuaternion orientation = new FrameQuaternion(ReferenceFrame.getWorldFrame());
         FootstepDataMessage footstep = new FootstepDataMessage(side, position.getPoint(), orientation.getQuaternion());
         message.add(footstep);
      }

      sendPacket(message);

      double initialTransferTime = getRobotModel().getWalkingControllerParameters().getDefaultInitialTransferTime();
      double walkingTime = initialTransferTime + stepIdx * (swingTime + transferTime);

      simulate(walkingTime + 0.5);
   }

   @Override
   public BoundingBox3D getFinalBoundingBox()
   {
      Point3DReadOnly min = new Point3D(StairsEnvironment.end, -0.5, 0.0);
      Point3DReadOnly max = new Point3D(StairsEnvironment.end + 1.0, 0.5, 1.0);
      return new BoundingBox3D(min, max);
   }

   @Override
   public CommonAvatarEnvironmentInterface getEnvironment()
   {
      return new StairsEnvironment();
   }

   public static class StairsEnvironment extends TestingEnvironment
   {
      public static final double start = 0.4;
      public static final double end = 1.4;
      public static final double stepHeight = 0.05;

      public StairsEnvironment()
      {
         terrain.addBox(-0.5, -0.5, end + 1.0, 0.5, -0.01, 0.0);
         double center = (start + end) / 2.0;
         terrain.addBox(center - 0.6, -0.5, center + 0.6, 0.5, stepHeight * 0.0, stepHeight * 1.0);
         terrain.addBox(center - 0.4, -0.5, center + 0.4, 0.5, stepHeight * 1.0, stepHeight * 2.0);
         terrain.addBox(center - 0.2, -0.5, center + 0.2, 0.5, stepHeight * 2.0, stepHeight * 3.0);
      }

      public static double getHeight(double x)
      {
         double center = (start + end) / 2.0;
         if (x <= center + 0.2 && x > center - 0.2)
         {
            return stepHeight * 3.0;
         }
         else if (x <= center + 0.4 && x > center - 0.4)
         {
            return stepHeight * 2.0;
         }
         else if (x <= center + 0.6 && x > center - 0.6)
         {
            return stepHeight * 1.0;
         }
         return 0.0;
      }
   }
}
