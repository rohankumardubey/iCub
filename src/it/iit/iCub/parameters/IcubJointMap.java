package it.iit.iCub.parameters;

import static it.iit.iCub.parameters.IcubOrderedJointMap.forcedSideDependentJointNames;
import static it.iit.iCub.parameters.IcubOrderedJointMap.jointNames;
import static it.iit.iCub.parameters.IcubOrderedJointMap.l_ankle_pitch;
import static it.iit.iCub.parameters.IcubOrderedJointMap.l_ankle_roll;
import static it.iit.iCub.parameters.IcubOrderedJointMap.l_elbow;
import static it.iit.iCub.parameters.IcubOrderedJointMap.l_hip_pitch;
import static it.iit.iCub.parameters.IcubOrderedJointMap.l_hip_roll;
import static it.iit.iCub.parameters.IcubOrderedJointMap.l_hip_yaw;
import static it.iit.iCub.parameters.IcubOrderedJointMap.l_knee;
import static it.iit.iCub.parameters.IcubOrderedJointMap.l_shoulder_pitch;
import static it.iit.iCub.parameters.IcubOrderedJointMap.l_shoulder_roll;
import static it.iit.iCub.parameters.IcubOrderedJointMap.l_shoulder_yaw;
import static it.iit.iCub.parameters.IcubOrderedJointMap.l_wrist_pitch;
import static it.iit.iCub.parameters.IcubOrderedJointMap.l_wrist_prosup;
import static it.iit.iCub.parameters.IcubOrderedJointMap.l_wrist_yaw;
import static it.iit.iCub.parameters.IcubOrderedJointMap.neck_pitch;
import static it.iit.iCub.parameters.IcubOrderedJointMap.neck_roll;
import static it.iit.iCub.parameters.IcubOrderedJointMap.neck_yaw;
import static it.iit.iCub.parameters.IcubOrderedJointMap.torso_pitch;
import static it.iit.iCub.parameters.IcubOrderedJointMap.torso_roll;
import static it.iit.iCub.parameters.IcubOrderedJointMap.torso_yaw;
import static us.ihmc.robotics.partNames.ArmJointName.ELBOW_PITCH;
import static us.ihmc.robotics.partNames.ArmJointName.ELBOW_YAW;
import static us.ihmc.robotics.partNames.ArmJointName.FIRST_WRIST_PITCH;
import static us.ihmc.robotics.partNames.ArmJointName.SHOULDER_PITCH;
import static us.ihmc.robotics.partNames.ArmJointName.SHOULDER_ROLL;
import static us.ihmc.robotics.partNames.ArmJointName.SHOULDER_YAW;
import static us.ihmc.robotics.partNames.ArmJointName.WRIST_ROLL;
import static us.ihmc.robotics.partNames.ArmJointName.WRIST_YAW;
import static us.ihmc.robotics.partNames.LegJointName.ANKLE_PITCH;
import static us.ihmc.robotics.partNames.LegJointName.ANKLE_ROLL;
import static us.ihmc.robotics.partNames.LegJointName.HIP_PITCH;
import static us.ihmc.robotics.partNames.LegJointName.HIP_ROLL;
import static us.ihmc.robotics.partNames.LegJointName.HIP_YAW;
import static us.ihmc.robotics.partNames.LegJointName.KNEE_PITCH;
import static us.ihmc.robotics.partNames.NeckJointName.DISTAL_NECK_ROLL;
import static us.ihmc.robotics.partNames.NeckJointName.DISTAL_NECK_YAW;
import static us.ihmc.robotics.partNames.NeckJointName.PROXIMAL_NECK_PITCH;
import static us.ihmc.robotics.partNames.SpineJointName.SPINE_PITCH;
import static us.ihmc.robotics.partNames.SpineJointName.SPINE_ROLL;
import static us.ihmc.robotics.partNames.SpineJointName.SPINE_YAW;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;

import us.ihmc.commons.PrintTools;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.robotics.controllers.pidGains.implementations.YoPDGains;
import us.ihmc.robotics.partNames.ArmJointName;
import us.ihmc.robotics.partNames.JointRole;
import us.ihmc.robotics.partNames.LegJointName;
import us.ihmc.robotics.partNames.LimbName;
import us.ihmc.robotics.partNames.NeckJointName;
import us.ihmc.robotics.partNames.SpineJointName;
import us.ihmc.robotics.robotSide.RobotSide;
import us.ihmc.robotics.robotSide.SideDependentList;
import us.ihmc.wholeBodyController.DRCRobotJointMap;
import us.ihmc.yoVariables.registry.YoVariableRegistry;

public class IcubJointMap implements DRCRobotJointMap
{
   // Enable joint limits
   private static final boolean ENABLE_JOINT_VELOCITY_TORQUE_LIMITS = false;

   static
   {
      if (!ENABLE_JOINT_VELOCITY_TORQUE_LIMITS)
      {
         PrintTools.info(IcubJointMap.class, "Running with torque and velocity limits disabled.");
      }
   }

   public static final String chestName = "chest";
   public static final String pelvisName = "base_link";
   public static final String headName = "head";
   public static final SideDependentList<String> handNames = new SideDependentList<>(getRobotSidePrefix(RobotSide.LEFT) + "hand",
                                                                                     getRobotSidePrefix(RobotSide.RIGHT) + "hand");
   public static final SideDependentList<String> footNames = new SideDependentList<>(getRobotSidePrefix(RobotSide.LEFT) + "ankle_2",
                                                                                     getRobotSidePrefix(RobotSide.RIGHT) + "ankle_2");

   private final IcubPhysicalProperties icubPhysicalProperties;

   private final LegJointName[] legJoints = {HIP_PITCH, HIP_ROLL, HIP_YAW, KNEE_PITCH, ANKLE_PITCH, ANKLE_ROLL};
   private final ArmJointName[] armJoints = {SHOULDER_PITCH, SHOULDER_ROLL, SHOULDER_YAW, ELBOW_PITCH, ELBOW_YAW, FIRST_WRIST_PITCH, WRIST_YAW};
   private final SpineJointName[] spineJoints = {SPINE_PITCH, SPINE_ROLL, SPINE_YAW};
   private final NeckJointName[] neckJoints = {PROXIMAL_NECK_PITCH, DISTAL_NECK_ROLL, DISTAL_NECK_YAW};

   private final LinkedHashMap<String, JointRole> jointRoles = new LinkedHashMap<String, JointRole>();
   private final LinkedHashMap<String, ImmutablePair<RobotSide, LimbName>> limbNames = new LinkedHashMap<String, ImmutablePair<RobotSide, LimbName>>();

   private final LinkedHashMap<String, ImmutablePair<RobotSide, LegJointName>> legJointNames = new LinkedHashMap<String, ImmutablePair<RobotSide, LegJointName>>();
   private final LinkedHashMap<String, ImmutablePair<RobotSide, ArmJointName>> armJointNames = new LinkedHashMap<String, ImmutablePair<RobotSide, ArmJointName>>();
   private final LinkedHashMap<String, SpineJointName> spineJointNames = new LinkedHashMap<String, SpineJointName>();
   private final LinkedHashMap<String, NeckJointName> neckJointNames = new LinkedHashMap<String, NeckJointName>();

   private final SideDependentList<EnumMap<LegJointName, String>> legJointStrings = SideDependentList.createListOfEnumMaps(LegJointName.class);
   private final SideDependentList<EnumMap<ArmJointName, String>> armJointStrings = SideDependentList.createListOfEnumMaps(ArmJointName.class);
   private final EnumMap<SpineJointName, String> spineJointStrings = new EnumMap<>(SpineJointName.class);
   private final EnumMap<NeckJointName, String> neckJointStrings = new EnumMap<>(NeckJointName.class);

   private final SideDependentList<String> nameOfJointsBeforeHands = new SideDependentList<>();
   private final String[] jointNamesBeforeFeet = new String[2];

   public IcubJointMap(IcubPhysicalProperties physicalProperties)
   {
      this.icubPhysicalProperties = physicalProperties;

      for (RobotSide robotSide : RobotSide.values)
      {
         String[] forcedSideJointNames = forcedSideDependentJointNames.get(robotSide);
         legJointNames.put(forcedSideJointNames[l_hip_pitch], new ImmutablePair<RobotSide, LegJointName>(robotSide, HIP_PITCH));
         legJointNames.put(forcedSideJointNames[l_hip_roll], new ImmutablePair<RobotSide, LegJointName>(robotSide, HIP_ROLL));
         legJointNames.put(forcedSideJointNames[l_hip_yaw], new ImmutablePair<RobotSide, LegJointName>(robotSide, HIP_YAW));
         legJointNames.put(forcedSideJointNames[l_knee], new ImmutablePair<RobotSide, LegJointName>(robotSide, KNEE_PITCH));
         legJointNames.put(forcedSideJointNames[l_ankle_pitch], new ImmutablePair<RobotSide, LegJointName>(robotSide, ANKLE_PITCH));
         legJointNames.put(forcedSideJointNames[l_ankle_roll], new ImmutablePair<RobotSide, LegJointName>(robotSide, ANKLE_ROLL));

         armJointNames.put(forcedSideJointNames[l_shoulder_pitch], new ImmutablePair<RobotSide, ArmJointName>(robotSide, SHOULDER_PITCH));
         armJointNames.put(forcedSideJointNames[l_shoulder_roll], new ImmutablePair<RobotSide, ArmJointName>(robotSide, SHOULDER_ROLL));
         armJointNames.put(forcedSideJointNames[l_shoulder_yaw], new ImmutablePair<RobotSide, ArmJointName>(robotSide, SHOULDER_YAW));
         armJointNames.put(forcedSideJointNames[l_elbow], new ImmutablePair<RobotSide, ArmJointName>(robotSide, ELBOW_PITCH));
         armJointNames.put(forcedSideJointNames[l_wrist_prosup], new ImmutablePair<RobotSide, ArmJointName>(robotSide, ELBOW_YAW));
         armJointNames.put(forcedSideJointNames[l_wrist_pitch], new ImmutablePair<RobotSide, ArmJointName>(robotSide, FIRST_WRIST_PITCH));
         armJointNames.put(forcedSideJointNames[l_wrist_yaw], new ImmutablePair<RobotSide, ArmJointName>(robotSide, WRIST_YAW));

         limbNames.put(handNames.get(robotSide), new ImmutablePair<RobotSide, LimbName>(robotSide, LimbName.ARM));
         limbNames.put(footNames.get(robotSide), new ImmutablePair<RobotSide, LimbName>(robotSide, LimbName.LEG));
      }

      spineJointNames.put(jointNames[torso_pitch], SPINE_PITCH);
      spineJointNames.put(jointNames[torso_roll], SPINE_ROLL);
      spineJointNames.put(jointNames[torso_yaw], SPINE_YAW);

      neckJointNames.put(jointNames[neck_pitch], PROXIMAL_NECK_PITCH);
      neckJointNames.put(jointNames[neck_roll], DISTAL_NECK_ROLL);
      neckJointNames.put(jointNames[neck_yaw], DISTAL_NECK_YAW);

      for (String legJointString : legJointNames.keySet())
      {
         RobotSide robotSide = legJointNames.get(legJointString).getLeft();
         LegJointName legJointName = legJointNames.get(legJointString).getRight();
         legJointStrings.get(robotSide).put(legJointName, legJointString);
         jointRoles.put(legJointString, JointRole.LEG);
      }

      for (String armJointString : armJointNames.keySet())
      {
         RobotSide robotSide = armJointNames.get(armJointString).getLeft();
         ArmJointName armJointName = armJointNames.get(armJointString).getRight();
         armJointStrings.get(robotSide).put(armJointName, armJointString);
         jointRoles.put(armJointString, JointRole.ARM);
      }

      for (String spineJointString : spineJointNames.keySet())
      {
         spineJointStrings.put(spineJointNames.get(spineJointString), spineJointString);
         jointRoles.put(spineJointString, JointRole.SPINE);
      }

      for (String neckJointString : neckJointNames.keySet())
      {
         neckJointStrings.put(neckJointNames.get(neckJointString), neckJointString);
         jointRoles.put(neckJointString, JointRole.NECK);
      }

      for (RobotSide robtSide : RobotSide.values)
      {
         nameOfJointsBeforeHands.put(robtSide, armJointStrings.get(robtSide).get(WRIST_ROLL));
      }

      jointNamesBeforeFeet[0] = getJointBeforeFootName(RobotSide.LEFT);
      jointNamesBeforeFeet[1] = getJointBeforeFootName(RobotSide.RIGHT);
   }

   private static String getRobotSidePrefix(RobotSide robotSide)
   {
      return (robotSide == RobotSide.LEFT) ? "l_" : "r_";
   }

   @Override
   public String getModelName()
   {
      return "iCub";
   }

   @Override
   public double getModelScale()
   {
      return 1.0;
   }

   @Override
   public double getMassScalePower()
   {
      return 1.0;
   }

   @Override
   public SideDependentList<String> getNameOfJointBeforeHands()
   {
      return null;
   }

   @Override
   public SideDependentList<String> getNameOfJointBeforeThighs()
   {
      return null;
   }

   @Override
   public String getNameOfJointBeforeChest()
   {
      return null;
   }

   @Override
   public ImmutablePair<RobotSide, LegJointName> getLegJointName(String jointName)
   {
      return legJointNames.get(jointName);
   }

   @Override
   public ImmutablePair<RobotSide, ArmJointName> getArmJointName(String jointName)
   {
      return armJointNames.get(jointName);
   }

   @Override
   public ImmutablePair<RobotSide, LimbName> getLimbName(String limbName)
   {
      return limbNames.get(limbName);
   }

   @Override
   public JointRole getJointRole(String jointName)
   {
      return jointRoles.get(jointName);
   }

   @Override
   public NeckJointName getNeckJointName(String jointName)
   {
      return neckJointNames.get(jointName);
   }

   @Override
   public SpineJointName getSpineJointName(String jointName)
   {
      return spineJointNames.get(jointName);
   }

   @Override
   public String getPelvisName()
   {
      return pelvisName;
   }

   @Override
   public String getChestName()
   {
      return chestName;
   }

   @Override
   public String getHeadName()
   {
      return headName;
   }

   @Override
   public LegJointName[] getLegJointNames()
   {
      return legJoints;
   }

   @Override
   public ArmJointName[] getArmJointNames()
   {
      return armJoints;
   }

   @Override
   public SpineJointName[] getSpineJointNames()
   {
      return spineJoints;
   }

   @Override
   public NeckJointName[] getNeckJointNames()
   {
      return neckJoints;
   }

   @Override
   public String getJointBeforeFootName(RobotSide robotSide)
   {
      return legJointStrings.get(robotSide).get(ANKLE_ROLL);
   }

   @Override
   public boolean isTorqueVelocityLimitsEnabled()
   {
      return ENABLE_JOINT_VELOCITY_TORQUE_LIMITS;
   }

   @Override
   public Set<String> getLastSimulatedJoints()
   {
      return new HashSet<>();
   }

   @Override
   public String[] getOrderedJointNames()
   {
      return jointNames;
   }

   @Override
   public RigidBodyTransform getSoleToAnkleFrameTransform(RobotSide robotSide)
   {
      return icubPhysicalProperties.getSoleToAnkleFrameTransform(robotSide);
   }

   @Override
   public RigidBodyTransform getHandControlFrameToWristTransform(RobotSide robotSide)
   {
      return icubPhysicalProperties.getHandControlFrameToWristTransform(robotSide);
   }

   @Override
   public String getLegJointName(RobotSide robotSide, LegJointName legJointName)
   {
      return legJointStrings.get(robotSide).get(legJointName);
   }

   @Override
   public String getArmJointName(RobotSide robotSide, ArmJointName armJointName)
   {
      return armJointStrings.get(robotSide).get(armJointName);
   }

   @Override
   public String getNeckJointName(NeckJointName neckJointName)
   {
      return neckJointStrings.get(neckJointName);
   }

   @Override
   public String getSpineJointName(SpineJointName spineJointName)
   {
      return spineJointStrings.get(spineJointName);
   }

   @Override
   public String getJointBeforeHandName(RobotSide robotSide)
   {
      return nameOfJointsBeforeHands.get(robotSide);
   }

   @Override
   public String[] getPositionControlledJointsForSimulation()
   {
      return null;
   }

   @Override
   public List<ImmutablePair<String, YoPDGains>> getPassiveJointNameWithGains(YoVariableRegistry registry)
   {
      return null;
   }

   @Override
   public String getUnsanitizedRootJointInSdf()
   {
      return pelvisName;
   }

   public String getHandName(RobotSide robotSide)
   {
      return handNames.get(robotSide);
   }

   @Override
   public String[] getJointNamesBeforeFeet()
   {
      return jointNamesBeforeFeet;
   }

   @Override
   public Enum<?>[] getRobotSegments()
   {
      return RobotSide.values;
   }

   @Override
   public Enum<?> getEndEffectorsRobotSegment(String joineNameBeforeEndEffector)
   {
      for (RobotSide robotSide : RobotSide.values)
      {
         String jointBeforeFootName = getJointBeforeFootName(robotSide);
         if (jointBeforeFootName != null && jointBeforeFootName.equals(joineNameBeforeEndEffector))
         {
            return robotSide;
         }

         String endOfArm = armJointStrings.get(robotSide).get(WRIST_ROLL);
         if (endOfArm != null && endOfArm.equals(joineNameBeforeEndEffector))
         {
            return robotSide;
         }
      }
      throw new IllegalArgumentException(joineNameBeforeEndEffector + " was not listed as an end effector in " + this.getClass().getSimpleName());
   }

   public IcubPhysicalProperties getPhysicalProperties()
   {
      return icubPhysicalProperties;
   }
}
