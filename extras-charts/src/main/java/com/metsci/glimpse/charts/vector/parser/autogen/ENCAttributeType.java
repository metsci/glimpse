/*
 * Copyright (c) 2012, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.charts.vector.parser.autogen;

import com.metsci.glimpse.charts.vector.parser.objects.GeoAttributeType;

import java.io.DataInput;
import java.util.HashMap;
import java.io.DataOutputStream;
import java.io.IOException;

public enum ENCAttributeType implements GeoAttributeType {
     AgencyResponsibleForProduction (1),
     BeaconShape (2),
     BuildingShape (3),
     BuoyShape (4),
     BuriedDepth (5),
     CallSign (6),
     CategoryOfAirportAirfield (7),
     CategoryOfAnchorage (8),
     CategoryOfBridge (9),
     CategoryOfBuiltUpArea (10),
     CategoryOfCable (11),
     CategoryOfCanal (12),
     CategoryOfCardinalMark (13),
     CategoryOfCheckpoint (14),
     CategoryOfCoastline (15),
     CategoryOfConveyor (17),
     CategoryOfControlPoint (16),
     CategoryOfCrane (19),
     CategoryOfCoverage (18),
     CategoryOfDistanceMark (21),
     CategoryOfDam (20),
     CategoryOfDumpingGround (23),
     CategoryOfDock (22),
     CategoryOfFerry (25),
     CategoryOfFenceWall (24),
     CategoryOfFogSignal (27),
     CategoryOfFishingFacility (26),
     CategoryOfGate (29),
     CategoryOfFortifiedStructure (28),
     CategoryOfHulk (31),
     CategoryOfHarbourFacility (30),
     CategoryOfLandRegion (34),
     CategoryOfLandmark (35),
     CategoryOfIce (32),
     CategoryOfInstallationBuoy (33),
     CategoryOfMarineFarmCulture (38),
     CategoryOfMilitaryPracticeArea (39),
     CategoryOfLateralMark (36),
     CategoryOfLight (37),
     CategoryOfObstruction (42),
     CategoryOfOffshorePlatform (43),
     CategoryOfMooringWarpingFacility (40),
     CategoryOfNavigationLine (41),
     CategoryOfPilotBoardingPlace (46),
     CategoryOfPipelinePipe (47),
     CategoryOfOilBarrier (44),
     CategoryOfPile (45),
     CategoryOfRadarStation (51),
     CategoryOfQualityOfData (50),
     CategoryOfPylon (49),
     CategoryOfProductionArea (48),
     CategoryOfRescueStation (55),
     CategoryOfRecommendedTrack (54),
     CategoryOfRadioStation (53),
     CategoryOfRadarTransponderBeacon (52),
     CategoryOfSeaArea (59),
     CategoryOfRunway (58),
     CategoryOfRoad (57),
     CategoryOfRestrictedArea (56),
     CategoryOfSiloTank (63),
     CategoryOfSignalStationWarning (62),
     CategoryOfSignalStationTraffic (61),
     CategoryOfShorelineConstruction (60),
     CategoryOfVegetation (68),
     CategoryOfWaterTurbulence (69),
     CategoryOfWeedKelp (70),
     CategoryOfWreck (71),
     CategoryOfSlope (64),
     CategoryOfSmallCraftFacility (65),
     CategoryOfSpecialPurposeMark (66),
     CategoryOfTrafficSeparationScheme (67),
     ColourPattern (76),
     CommunicationChannel (77),
     CompassSize (78),
     CompilationDate (79),
     CategoryOfZoneOfConfidenceData (72),
     CharacterSpacing (73),
     CharacterSpecification (74),
     Colour (75),
     DateEnd (85),
     CurrentVelocity (84),
     DepthRangeValue1 (87),
     DateStart (86),
     Condition (81),
     CompilationScale (80),
     ConspicuousVisual (83),
     ConspicuousRadar (82),
     ExpositionOfSounding (93),
     ExhibitionConditionOfLight (92),
     Height (95),
     Function (94),
     DepthUnits (89),
     DepthRangeValue2 (88),
     EstimatedRangeOfTransmission (91),
     Elevation (90),
     Information (102),
     Jurisdiction (103),
     HorizontalWidth (100),
     IceFactor (101),
     HorizontalClearance (98),
     HorizontalLength (99),
     HeightLengthUnits (96),
     HorizontalAccuracy (97),
     MultiplicityOfLights (110),
     Nationality (111),
     LightVisibility (108),
     MarksNavigationalSystemOf (109),
     LiftingCapacity (106),
     LightCharacteristic (107),
     JustificationHorizontal (104),
     JustificationVertical (105),
     PeriodicDateStart (119),
     PeriodicDateEnd (118),
     Orientation (117),
     ObjectName (116),
     NoticeToMarinersDate (115),
     NatureOfSurfaceQualifyingTerms (114),
     NatureOfSurface (113),
     NatureOfConstruction (112),
     Radius (127),
     RadarWaveLength (126),
     QualityOfSoundingMeasurement (125),
     PublicationReference (124),
     Product (123),
     ProducingCountry (122),
     PilotDistrict (121),
     PictorialRepresentation (120),
     SectorLimitTwo (137),
     SectorLimitOne (136),
     SignalFrequency (139),
     ShiftParameters (138),
     SignalGroup (141),
     SignalGeneration (140),
     SignalSequence (143),
     SignalPeriod (142),
     RecordingIndication (129),
     RecordingDate (128),
     Restriction (131),
     ReferenceYearForMagneticVariation (130),
     ScaleMinimum (133),
     ScaleMaximum (132),
     ScaleValueTwo (135),
     ScaleValueOne (134),
     SurveyDateStart (152),
     SurveyType (153),
     SymbolScalingFactor (154),
     SymbolizationCode (155),
     TechniqueOfSoundingMeasurement (156),
     TextString (157),
     TextualDescription (158),
     TidalStreamPanelValues (159),
     SoundingAccuracy (144),
     SoundingDistanceMaximum (145),
     SoundingDistanceMinimum (146),
     SourceDate (147),
     SourceIndication (148),
     Status (149),
     SurveyAuthority (150),
     SurveyDateEnd (151),
     TopmarkDaymarkShape (171),
     Tint (170),
     TimeStart (169),
     TimeEnd (168),
     ValueOfLocalMagneticAnomaly (175),
     ValueOfDepthContour (174),
     ValueOfAnnualChangeInMagneticVariation (173),
     TrafficFlow (172),
     TideMethodOfTidalPrediction (163),
     TideHighAndLowWaterValues (162),
     TideAccuracyOfWaterLevel (161),
     TidalStreamCurrentTimeSeriesValues (160),
     TideValueOfHarmonicConstituents (167),
     TideTimeSeriesValues (166),
     TideCurrentTimeIntervalOfValues (165),
     TideTimeAndHeightDifferences (164),
     VerticalLength (186),
     WaterLevelEffect (187),
     VerticalClearanceSafe (184),
     VerticalDatum (185),
     CategoryOfTidalStream (188),
     PositionalAccuracyUnits (189),
     ValueOfNominalRange (178),
     ValueOfSounding (179),
     ValueOfMagneticVariation (176),
     ValueOfMaximumRange (177),
     VerticalClearanceClosed (182),
     VerticalClearanceOpen (183),
     VerticalAccuracy (180),
     VerticalClearance (181),
     TextualDescriptionInNationalLanguage (304),
     InformationInNationalLanguage (300),
     ObjectNameInNationalLanguage (301),
     PilotDistrictInNationalLanguage (302),
     TextStringInNationalLanguage (303),
     QualityOfPosition (402),
     HorizontalDatum (400),
     PositionalAccuracy (401);

     private int code;
     private static boolean populate = true;
     private static HashMap<Integer, ENCAttributeType> lookupMap = new HashMap<Integer, ENCAttributeType>();

     ENCAttributeType(int c){
         code = c;
     }

     public static ENCAttributeType getInstance(int c){
         if(populate){
             for(ENCAttributeType p : ENCAttributeType.values())
                 lookupMap.put(p.code, p);
             populate = false;
         }
         return lookupMap.get(c);
     }

     public static ENCAttributeType read(DataInput fin) throws IOException{
         return getInstance(fin.readInt());
     }

     public static void write(DataOutputStream fout, ENCAttributeType attrib) throws IOException{
         fout.writeInt(attrib.code);
     }

}

