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
package com.metsci.glimpse.charts.vector.parser.objects;

import java.io.DataInput;
import java.util.HashMap;
import java.io.DataOutputStream;
import java.io.IOException;

public enum ENCObjectType implements GeoFeatureType {

    AdministrationAreaNamed(1),
    AirportAirfield(2),
    AnchorBerth(3),
    AnchorageArea(4),
    Beacon_Cardinal(5),
    Beacon_IsolatedDanger(6),
    Beacon_Lateral(7),
    Beacon_SafeWater(8),
    Beacon_SpecialPurposeGeneral(9),
    Berth(10),
    Bridge(11),
    Building_Single(12),
    BuiltUpArea(13),
    Buoy_Cardinal(14),
    Buoy_Installation(15),
    Buoy_Lateral(17),
    Buoy_IsolatedDanger(16),
    Buoy_SpecialPurposeGeneral(19),
    Buoy_SafeWater(18),
    Cable_Overhead(21),
    CableArea(20),
    Canal(23),
    Cable_Submarine(22),
    CargoTransshipmentArea(25),
    CanalBank(24),
    CautionArea(27),
    Causeway(26),
    CoastguardStation(29),
    Checkpoint(28),
    ContiguousZone(31),
    Coastline(30),
    NauticalPublicationInformation(305),
    Conveyor(34),
    HorizontalDatumShiftParameters(304),
    Crane(35),
    ProductionInformation(307),
    ContinentalShelfArea(32),
    NavigationalSystemOfMarks(306),
    ControlPoint(33),
    SoundingDatum(309),
    Dam(38),
    QualityOfData(308),
    Daymark(39),
    UnitsOfMeasurementOfData(311),
    CurrentNonGravitational(36),
    SurveyReliability(310),
    CustomZone(37),
    DepthArea(42),
    VerticalDatumOfData(312),
    DepthContour(43),
    DeepWaterRouteCenterline(40),
    DeepWaterRoutePart(41),
    DredgedArea(46),
    DryDock(47),
    DistanceMark(44),
    DockArea(45),
    Fairway(51),
    ExclusiveEconomicZone(50),
    Dyke(49),
    DumpingGround(48),
    FishingFacility(55),
    FisheryZone(54),
    FerryRoute(53),
    FenceWall(52),
    FortifiedStructure(59),
    FogSignal(58),
    FloatingDock(57),
    FishingGround(56),
    AccuracyOfData(300),
    HarbourAreaAdministrative(63),
    CompilationScaleOfData(301),
    Gridiron(62),
    Coverage(302),
    Gate(61),
    HorizontalDatumOfData(303),
    FreePortArea(60),
    InshoreTrafficZone(68),
    Lake(69),
    LakeShore(70),
    LandArea(71),
    HarbourFacility(64),
    Hulk(65),
    IceArea(66),
    IncinerationArea(67),
    LightFloat(76),
    LightVessel(77),
    LocalMagneticAnomaly(78),
    LockBasin(79),
    LandElevation(72),
    LandRegion(73),
    Landmark(74),
    Light(75),
    NavigationLine(85),
    MooringWarpingFacility(84),
    OffshorePlatform(87),
    Obstruction(86),
    MagneticVariation(81),
    LogPond(80),
    MilitaryPracticeArea(83),
    MarineFarmCulture(82),
    Pipeline_Overhead(93),
    PipelineArea(92),
    Pontoon(95),
    Pipeline_SubmarineOnLand(94),
    OilBarrier(89),
    OffshoreProductionArea(88),
    PilotBoardingPlace(91),
    Pile(90),
    RadarStation(102),
    RadarTransponderBeacon(103),
    RadarRange(100),
    RadarReflector(101),
    PylonBridgeSupport(98),
    RadarLine(99),
    PrecautionaryArea(96),
    ProductionStorageArea(97),
    RecommendedTrafficLanePart(110),
    RescueStation(111),
    RecommendedRouteCenterline(108),
    RecommendedTrack(109),
    Railway(106),
    Rapids(107),
    RadioCallingInPoint(104),
    RadioStation(105),
    SeaAreaNamedWaterArea(119),
    SandWaves(118),
    Runway(117),
    Road(116),
    RiverBank(115),
    River(114),
    RetroReflector(113),
    RestrictedArea(112),
    SlopingGround(127),
    SlopeTopline(126),
    SiloTank(125),
    SignalStation_Warning(124),
    SignalStation_Traffic(123),
    ShorelineConstruction(122),
    SeabedArea(121),
    SeaPlaneLandingArea(120),
    TidalStreamNonHarmonicPrediction(137),
    TidalStreamHarmonicPrediction(136),
    TidalStreamTimeSeries(139),
    TidalStreamPanelData(138),
    TideNonHarmonicPrediction(141),
    TideHarmonicPrediction(140),
    Tideway(143),
    TideTimeSeries(142),
    StackedOnStackedUnder(402),
    Sounding(129),
    SmallCraftFacility(128),
    Aggregation(400),
    Square(131),
    Association(401),
    Spring(130),
    SubmarineTransitLane(133),
    StraightTerritorialSeaBaseline(132),
    TerritorialSeaArea(135),
    SweptArea(134),
    TwoWayRoutePart(152),
    UnderwaterRockAwashRock(153),
    UnsurveyedArea(154),
    Vegetation(155),
    WaterTurbulence(156),
    Waterfall(157),
    WeedKelp(158),
    Wreck(159),
    TopMark(144),
    TrafficSeparationLine(145),
    TrafficSeparationSchemeBoundary(146),
    TrafficSeparationSchemeCrossing(147),
    TrafficSeparationSchemeLanePart(148),
    TrafficSeparationSchemeRoundabout(149),
    TrafficSeparationZone(150),
    Tunnel(151),
    TidalStreamFloodEbb(160),
    Text(504),
    CartographicArea(500),
    CartographicLine(501),
    CartographicSymbol(502),
    Compass(503);

    private final int code;

    private static final ENCObjectType[] encObjectTypes = ENCObjectType.values( );
    private static boolean populate = true;
    private static HashMap<Integer, ENCObjectType> lookupMap = new HashMap<Integer, ENCObjectType>();

    ENCObjectType(int c) {
        code = c;
    }

    public static ENCObjectType getInstance(int c) {
        if (populate) {
            for (ENCObjectType p : encObjectTypes) {
                lookupMap.put(p.code, p);
            }
            populate = false;
        }
        return lookupMap.get(c);
    }

    public static void write(DataOutputStream fout, ENCObjectType obj) throws IOException {
        fout.writeInt(obj.code);
    }

    public static ENCObjectType read(DataInput fin) throws IOException {
        return getInstance(fin.readInt());
    }

    @Override
    public int getNumFeatures() {
        return staticGetNumFeatures();
    }

    public static int staticGetNumFeatures() {
        return encObjectTypes.length;
    }

    @Override
    public String code() {
        return "" + code;
    }

    @Override
    public String asKey() {
        return name();
    }
}

