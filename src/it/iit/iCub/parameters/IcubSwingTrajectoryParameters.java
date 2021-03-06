package it.iit.iCub.parameters;

import us.ihmc.commonWalkingControlModules.configurations.SwingTrajectoryParameters;

public class IcubSwingTrajectoryParameters extends SwingTrajectoryParameters
{
   @Override
   public boolean doToeTouchdownIfPossible()
   {
      return false;
   }

   @Override
   public double getToeTouchdownAngle()
   {
      return Math.toRadians(20.0);
   }

   @Override
   public boolean doHeelTouchdownIfPossible()
   {
      return false;
   }

   @Override
   public double getHeelTouchdownAngle()
   {
      return Math.toRadians(-20.0);
   }

   @Override
   public double getMinMechanicalLegLength()
   {
      return 0.2;
   }

   @Override
   public double getDesiredTouchdownHeightOffset()
   {
      return 0.0;
   }

   @Override
   public double getDesiredTouchdownVelocity()
   {
      return -0.15;
   }

   @Override
   public double getDesiredTouchdownAcceleration()
   {
      return 0.0;
   }

   @Override
   public double[] getSwingWaypointProportions()
   {
      return new double[] {0.15, 0.95};
   }

   @Override
   public double[] getObstacleClearanceProportions()
   {
      return new double[] {0.1, 0.9};
   }

}
