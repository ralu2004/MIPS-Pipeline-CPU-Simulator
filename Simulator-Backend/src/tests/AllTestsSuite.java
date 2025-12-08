package tests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("MIPS Simulator Testing")
@SelectClasses({
    AssemblerTest.class,
    ClockTest.class,
    ControlUnitTest.class,
    CPUStateTest.class,
    DataMemoryTest.class,
    ForwardingUnitTest.class,
    HazardDetectionUnitTest.class,
    InstructionMemoryTest.class,
    InstructionTest.class,
    MIPSTest.class,
    PipelineControllerTest.class,
    PipelineStagesTest.class,
    ProgramCounterTest.class,
    RegisterFileTest.class,
    StallUnitTest.class
})
public class AllTestsSuite {}

