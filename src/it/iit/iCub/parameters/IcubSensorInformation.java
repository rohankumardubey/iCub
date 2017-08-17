package it.iit.iCub.parameters;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import us.ihmc.euclid.referenceFrame.ReferenceFrame;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.robotics.robotSide.RobotSide;
import us.ihmc.robotics.robotSide.SideDependentList;
import us.ihmc.sensorProcessing.parameters.DRCRobotCameraParameters;
import us.ihmc.sensorProcessing.parameters.DRCRobotLidarParameters;
import us.ihmc.sensorProcessing.parameters.DRCRobotPointCloudParameters;
import us.ihmc.sensorProcessing.parameters.DRCRobotSensorInformation;
import us.ihmc.sensorProcessing.parameters.DRCRobotSensorParameters;

public class IcubSensorInformation implements DRCRobotSensorInformation
{

   /**
    * Force Sensor Parameters
    */
   public static final String[] forceSensorNames = { "l_ankle_roll", "r_ankle_roll" };
   public static final SideDependentList<String> feetForceSensorNames = new SideDependentList<String>("l_ankle_roll", "r_ankle_roll");
   public static final SideDependentList<String> handForceSensorNames = new SideDependentList<String>(); // maybe add the FTs embedded in the arms

   private static final SideDependentList<String> urdfFeetForceSensorNames = new SideDependentList<>("l_foot_ft_sensor", "r_foot_ft_sensor");

   public static final SideDependentList<RigidBodyTransform> transformFromMeasurementToAnkleZUpFrames = new SideDependentList<>();
   static
   {
      RigidBodyTransform leftTransform = new RigidBodyTransform();
//      leftTransform.setEuler(0.0, 1.5708, 0.0); //from URDF,  but our 'UP axis' in 'l/r_ankle_roll' is 'X' and the 'Mes CoP' goes crazy (but robot can walk)
      leftTransform.setTranslation(new Vector3D(-0.0035, 0.0, 0.0685));

      transformFromMeasurementToAnkleZUpFrames.put(RobotSide.LEFT, leftTransform);
      transformFromMeasurementToAnkleZUpFrames.put(RobotSide.RIGHT, new RigidBodyTransform(leftTransform));
   }

   /**
    * PPS Parameters
    */


   /**
    * Camera Parameters
    */
   private final DRCRobotCameraParameters[] cameraParamaters = new DRCRobotCameraParameters[0];


   /**
    * Lidar Parameters
    */
   private final DRCRobotLidarParameters[] lidarParamaters = new DRCRobotLidarParameters[0];


   /**
    * Stereo Parameters
    */
   private final DRCRobotPointCloudParameters[] pointCloudParamaters = new DRCRobotPointCloudParameters[0];


   /**
    * IMU
    */
   private static final String bodyIMUSensor = "head_imu_sensor";
   private static final String[] imuSensorsToUse = { bodyIMUSensor };


   public IcubSensorInformation()
   {
   }

   @Override
   public String[] getIMUSensorsToUseInStateEstimator()
   {
      return imuSensorsToUse;
   }

   @Override
	public String[] getForceSensorNames()
	{
		return forceSensorNames;
	}

	@Override
	public SideDependentList<String> getFeetForceSensorNames()
	{
		return feetForceSensorNames;
	}

	public static String getUrdfFeetForceSensorName(RobotSide side)
   {
      return urdfFeetForceSensorNames.get(side);
   }

	@Override
	public SideDependentList<String> getWristForceSensorNames()
	{
		return handForceSensorNames;
	}

	@Override
	public String getPrimaryBodyImu()
	{
		return bodyIMUSensor;
	}

	@Override
	public DRCRobotCameraParameters[] getCameraParameters()
	{
		return cameraParamaters;
	}

	@Override
	public DRCRobotLidarParameters[] getLidarParameters()
	{
		return lidarParamaters;
	}

	@Override
	public DRCRobotPointCloudParameters[] getPointCloudParameters()
	{
		return pointCloudParamaters;
	}

	@Override
	public DRCRobotCameraParameters getCameraParameters(int cameraId)
	{
		return null;
	}

	@Override
	public DRCRobotLidarParameters getLidarParameters(int lidarId)
	{
		return null;
	}

	@Override
	public DRCRobotPointCloudParameters getPointCloudParameters(int pointCloudSensorId)
	{
		return null;
	}

   @Override
   public ReferenceFrame getHeadIMUFrameWhenLevel() {
      return null;
   }

   @Override
	public String[] getSensorFramesToTrack()
	{
		ArrayList<String> sensorFramesToTrack = new ArrayList<String>();
		sensorFramesToTrack(cameraParamaters, sensorFramesToTrack);
		sensorFramesToTrack(lidarParamaters, sensorFramesToTrack);
		sensorFramesToTrack(pointCloudParamaters, sensorFramesToTrack);
		String[] sensorFramesToTrackAsPrimitive = new String[sensorFramesToTrack.size()];
		sensorFramesToTrack.toArray(sensorFramesToTrackAsPrimitive);
		return sensorFramesToTrackAsPrimitive;
	}

	private void sensorFramesToTrack(DRCRobotSensorParameters[] sensorParams, ArrayList<String> holder)
	   {
	      for(int i = 0; i < sensorParams.length; i++)
	      {
	         if(sensorParams[i].getPoseFrameForSdf() != null)
	         {
	            holder.add(sensorParams[i].getPoseFrameForSdf());
	         }
	      }
	   }

	@Override
	public boolean setupROSLocationService()
	{
		return false;
	}

	@Override
	public boolean setupROSParameterSetters()
	{
		return false;
	}

	@Override
	public boolean isMultisenseHead()
	{
		return false;
	}

   @Override
   public SideDependentList<String> getFeetContactSensorNames()
   {
      return new SideDependentList<String>();
   }

   @Override
   public ArrayList<ImmutableTriple<String, String, RigidBodyTransform>> getStaticTransformsForRos()
   {
      return null;
   }
}
