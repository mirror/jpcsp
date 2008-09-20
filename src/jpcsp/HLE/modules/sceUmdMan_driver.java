/* This autogenerated file is part of jpcsp. */
package jpcsp.HLE.modules;

import jpcsp.HLE.pspSysMem;
import jpcsp.Memory;
import jpcsp.Processor;

public class sceUmdMan_driver implements HLEModule {

    @Override
    public final String getName() {
        return "sceUmdMan_driver";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {

        mm.add(sceUmdManInit, 0xD27D050E);

        mm.add(sceUmdManTerm, 0x0D3EA203);

        mm.add(sceUmdManGetErrorStatus, 0x2C756C37);

        mm.add(sceUmdManActivate, 0xCC80CFC6);

        mm.add(sceUmdManStart, 0x8CFED611);

        mm.add(sceUmdManStop, 0xCAD31025);

        mm.add(sceUmdMan_driver_F6BE91FC, 0xF6BE91FC);

        mm.add(sceUmdManGetUmdDrive, 0x47E2B6D8);

        mm.add(sceUmdManCheckDeviceReady, 0xB2368381);

        mm.add(sceUmdManGetInquiry, 0xE779ECEF);

        mm.add(sceUmdMan_driver_CEA5C857, 0xCEA5C857);

        mm.add(sceUmdMan_driver_8634FFC7, 0x8634FFC7);

        mm.add(sceUmdMan_driver_39704B6E, 0x39704B6E);

        mm.add(sceUmdMan_driver_63ACFD28, 0x63ACFD28);

        mm.add(sceUmdMan_driver_84410A8E, 0x84410A8E);

        mm.add(sceUmdManValidateUMD, 0xFCFEF5FE);

        mm.add(sceUmdExecRead10Cmd, 0x1B1BF9FD);

        mm.add(sceUmdExecPrefetch10Cmd, 0x18DE1880);

        mm.add(sceUmdExecReadUMDStructureCmd, 0x3D44BABF);

        mm.add(sceUmdExecStartStopUnitCmd, 0xE3F448E0);

        mm.add(sceUmdExecInquiryCmd, 0x1B19A313);

        mm.add(sceUmdExecReqSenseCmd, 0x2CBE959B);

        mm.add(sceUmdExecModSenseCmd, 0x2A39569B);

        mm.add(sceUmdExecModSelectCmd, 0xCEE55E3E);

        mm.add(sceUmdExecMechaStatCmd, 0xE5B7EDC5);

        mm.add(sceUmdExecGetEventStatusCmd, 0x65E1B97E);

        mm.add(sceUmdExecReadCapacityCmd, 0x5AA96415);

        mm.add(sceUmdExecSeekCmd, 0x250E6975);

        mm.add(sceUmdExecPreventAllowMediaCmd, 0x2A08FE9A);

        mm.add(sceUmdExecAllocateFromReadCmd, 0x68577709);

        mm.add(sceUmdExecReadMKICmd, 0xF819E17C);

        mm.add(sceUmdExecSetAreaLimitCmd, 0x61C32A52);

        mm.add(sceUmdExecSetAccessLimitCmd, 0x7094E3A7);

        mm.add(sceUmdExecSetLockLengthCmd, 0xD31DAD7E);

        mm.add(sceUmdExecGetMediaInfoCmd, 0x108B2322);

        mm.add(sceUmdExecReportCacheCmd, 0x98345381);

        mm.add(sceUmdExecSetCDSpeedCmd, 0xBF88476F);

        mm.add(sceUmdExecSetStreamingCmd, 0x485D4925);

        mm.add(sceUmdExecClearCacheInfoCmd, 0x73E49F8F);

        mm.add(sceUmdExecTestCmd, 0x14D3381C);

        mm.add(sceUmdExecAdjustDataCmd, 0x92F1CC33);

        mm.add(sceUmdExecGetErrorLogCmd, 0x61EB07A5);

        mm.add(sceUmdMan_driver_77E6C03A, 0x77E6C03A);

        mm.add(sceUmdMan_driver_A869CAB3, 0xA869CAB3);

        mm.add(sceUmdManRegisterImposeCallBack, 0xF65D819F);

        mm.add(sceUmdManUnRegisterImposeCallback, 0x2787078E);

        mm.add(sceUmdMan_driver_1F9AFFF4, 0x1F9AFFF4);

        mm.add(sceUmdManGetDiscInfo, 0x8609D1E4);

        mm.add(sceUmdManGetDiscInfo4VSH, 0xAAA4ED91);

        mm.add(sceUmdManGetUmdDiscInfo, 0xE192C10A);

        mm.add(sceUmdManGetReadyFlag, 0x42D28DD1);

        mm.add(sceUmdManGetIntrStateFlag, 0x4FB913A3);

        mm.add(sceUmdManSetAlarm, 0x7DF2A18D);

        mm.add(sceUmdManCancelAlarm, 0xB0A6DC55);

        mm.add(sceUmdMan_driver_D1D4F296, 0xD1D4F296);

        mm.add(sceUmdManRegisterInsertEjectUMDCallBack, 0xBF8AED79);

        mm.add(sceUmdManUnRegisterInsertEjectUMDCallBack, 0x4217E7F5);

        mm.add(sceUmdManGetPowerStat, 0x4581E306);

        mm.add(sceUmdManChangePowerMode, 0xCA68F200);

        mm.add(sceUmdMan_driver_F4AFF62D, 0xF4AFF62D);

        mm.add(sceUmdManSPKGetMKI, 0x7C3D307C);

        mm.add(sceUmdMan_driver_B511F821, 0xB511F821);

        mm.add(sceUmdMan_driver_736AE133, 0x736AE133);

        mm.add(sceUmdMan_driver_BD13A488, 0xBD13A488);

        mm.add(sceUmdMan_driver_9814DCF3, 0x9814DCF3);

        mm.add(sceUmdMan_driver_3DCFAA71, 0x3DCFAA71);

        mm.add(sceUmdManWaitSema, 0x0DC8D26D);

        mm.add(sceUmdManPollSema, 0x9F106F73);

        mm.add(sceUmdManSignalSema, 0xB0A43DA7);

    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {

        mm.remove(sceUmdManInit);

        mm.remove(sceUmdManTerm);

        mm.remove(sceUmdManGetErrorStatus);

        mm.remove(sceUmdManActivate);

        mm.remove(sceUmdManStart);

        mm.remove(sceUmdManStop);

        mm.remove(sceUmdMan_driver_F6BE91FC);

        mm.remove(sceUmdManGetUmdDrive);

        mm.remove(sceUmdManCheckDeviceReady);

        mm.remove(sceUmdManGetInquiry);

        mm.remove(sceUmdMan_driver_CEA5C857);

        mm.remove(sceUmdMan_driver_8634FFC7);

        mm.remove(sceUmdMan_driver_39704B6E);

        mm.remove(sceUmdMan_driver_63ACFD28);

        mm.remove(sceUmdMan_driver_84410A8E);

        mm.remove(sceUmdManValidateUMD);

        mm.remove(sceUmdExecRead10Cmd);

        mm.remove(sceUmdExecPrefetch10Cmd);

        mm.remove(sceUmdExecReadUMDStructureCmd);

        mm.remove(sceUmdExecStartStopUnitCmd);

        mm.remove(sceUmdExecInquiryCmd);

        mm.remove(sceUmdExecReqSenseCmd);

        mm.remove(sceUmdExecModSenseCmd);

        mm.remove(sceUmdExecModSelectCmd);

        mm.remove(sceUmdExecMechaStatCmd);

        mm.remove(sceUmdExecGetEventStatusCmd);

        mm.remove(sceUmdExecReadCapacityCmd);

        mm.remove(sceUmdExecSeekCmd);

        mm.remove(sceUmdExecPreventAllowMediaCmd);

        mm.remove(sceUmdExecAllocateFromReadCmd);

        mm.remove(sceUmdExecReadMKICmd);

        mm.remove(sceUmdExecSetAreaLimitCmd);

        mm.remove(sceUmdExecSetAccessLimitCmd);

        mm.remove(sceUmdExecSetLockLengthCmd);

        mm.remove(sceUmdExecGetMediaInfoCmd);

        mm.remove(sceUmdExecReportCacheCmd);

        mm.remove(sceUmdExecSetCDSpeedCmd);

        mm.remove(sceUmdExecSetStreamingCmd);

        mm.remove(sceUmdExecClearCacheInfoCmd);

        mm.remove(sceUmdExecTestCmd);

        mm.remove(sceUmdExecAdjustDataCmd);

        mm.remove(sceUmdExecGetErrorLogCmd);

        mm.remove(sceUmdMan_driver_77E6C03A);

        mm.remove(sceUmdMan_driver_A869CAB3);

        mm.remove(sceUmdManRegisterImposeCallBack);

        mm.remove(sceUmdManUnRegisterImposeCallback);

        mm.remove(sceUmdMan_driver_1F9AFFF4);

        mm.remove(sceUmdManGetDiscInfo);

        mm.remove(sceUmdManGetDiscInfo4VSH);

        mm.remove(sceUmdManGetUmdDiscInfo);

        mm.remove(sceUmdManGetReadyFlag);

        mm.remove(sceUmdManGetIntrStateFlag);

        mm.remove(sceUmdManSetAlarm);

        mm.remove(sceUmdManCancelAlarm);

        mm.remove(sceUmdMan_driver_D1D4F296);

        mm.remove(sceUmdManRegisterInsertEjectUMDCallBack);

        mm.remove(sceUmdManUnRegisterInsertEjectUMDCallBack);

        mm.remove(sceUmdManGetPowerStat);

        mm.remove(sceUmdManChangePowerMode);

        mm.remove(sceUmdMan_driver_F4AFF62D);

        mm.remove(sceUmdManSPKGetMKI);

        mm.remove(sceUmdMan_driver_B511F821);

        mm.remove(sceUmdMan_driver_736AE133);

        mm.remove(sceUmdMan_driver_BD13A488);

        mm.remove(sceUmdMan_driver_9814DCF3);

        mm.remove(sceUmdMan_driver_3DCFAA71);

        mm.remove(sceUmdManWaitSema);

        mm.remove(sceUmdManPollSema);

        mm.remove(sceUmdManSignalSema);

    }
    public static final HLEModuleFunction sceUmdManInit = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManInit") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManInit [0xD27D050E]");
        }
    };
    public static final HLEModuleFunction sceUmdManTerm = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManTerm") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManTerm [0x0D3EA203]");
        }
    };
    public static final HLEModuleFunction sceUmdManGetErrorStatus = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManGetErrorStatus") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManGetErrorStatus [0x2C756C37]");
        }
    };
    public static final HLEModuleFunction sceUmdManActivate = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManActivate") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManActivate [0xCC80CFC6]");
        }
    };
    public static final HLEModuleFunction sceUmdManStart = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManStart") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManStart [0x8CFED611]");
        }
    };
    public static final HLEModuleFunction sceUmdManStop = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManStop") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManStop [0xCAD31025]");
        }
    };
    public static final HLEModuleFunction sceUmdMan_driver_F6BE91FC = new HLEModuleFunction("sceUmdMan_driver", "sceUmdMan_driver_F6BE91FC") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdMan_driver_F6BE91FC [0xF6BE91FC]");
        }
    };
    public static final HLEModuleFunction sceUmdManGetUmdDrive = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManGetUmdDrive") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManGetUmdDrive [0x47E2B6D8]");
        }
    };
    public static final HLEModuleFunction sceUmdManCheckDeviceReady = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManCheckDeviceReady") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManCheckDeviceReady [0xB2368381]");
        }
    };
    public static final HLEModuleFunction sceUmdManGetInquiry = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManGetInquiry") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManGetInquiry [0xE779ECEF]");
        }
    };
    public static final HLEModuleFunction sceUmdMan_driver_CEA5C857 = new HLEModuleFunction("sceUmdMan_driver", "sceUmdMan_driver_CEA5C857") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdMan_driver_CEA5C857 [0xCEA5C857]");
        }
    };
    public static final HLEModuleFunction sceUmdMan_driver_8634FFC7 = new HLEModuleFunction("sceUmdMan_driver", "sceUmdMan_driver_8634FFC7") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdMan_driver_8634FFC7 [0x8634FFC7]");
        }
    };
    public static final HLEModuleFunction sceUmdMan_driver_39704B6E = new HLEModuleFunction("sceUmdMan_driver", "sceUmdMan_driver_39704B6E") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdMan_driver_39704B6E [0x39704B6E]");
        }
    };
    public static final HLEModuleFunction sceUmdMan_driver_63ACFD28 = new HLEModuleFunction("sceUmdMan_driver", "sceUmdMan_driver_63ACFD28") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdMan_driver_63ACFD28 [0x63ACFD28]");
        }
    };
    public static final HLEModuleFunction sceUmdMan_driver_84410A8E = new HLEModuleFunction("sceUmdMan_driver", "sceUmdMan_driver_84410A8E") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdMan_driver_84410A8E [0x84410A8E]");
        }
    };
    public static final HLEModuleFunction sceUmdManValidateUMD = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManValidateUMD") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManValidateUMD [0xFCFEF5FE]");
        }
    };
    public static final HLEModuleFunction sceUmdExecRead10Cmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecRead10Cmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecRead10Cmd [0x1B1BF9FD]");
        }
    };
    public static final HLEModuleFunction sceUmdExecPrefetch10Cmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecPrefetch10Cmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecPrefetch10Cmd [0x18DE1880]");
        }
    };
    public static final HLEModuleFunction sceUmdExecReadUMDStructureCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecReadUMDStructureCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecReadUMDStructureCmd [0x3D44BABF]");
        }
    };
    public static final HLEModuleFunction sceUmdExecStartStopUnitCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecStartStopUnitCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecStartStopUnitCmd [0xE3F448E0]");
        }
    };
    public static final HLEModuleFunction sceUmdExecInquiryCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecInquiryCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecInquiryCmd [0x1B19A313]");
        }
    };
    public static final HLEModuleFunction sceUmdExecReqSenseCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecReqSenseCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecReqSenseCmd [0x2CBE959B]");
        }
    };
    public static final HLEModuleFunction sceUmdExecModSenseCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecModSenseCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecModSenseCmd [0x2A39569B]");
        }
    };
    public static final HLEModuleFunction sceUmdExecModSelectCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecModSelectCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecModSelectCmd [0xCEE55E3E]");
        }
    };
    public static final HLEModuleFunction sceUmdExecMechaStatCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecMechaStatCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecMechaStatCmd [0xE5B7EDC5]");
        }
    };
    public static final HLEModuleFunction sceUmdExecGetEventStatusCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecGetEventStatusCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecGetEventStatusCmd [0x65E1B97E]");
        }
    };
    public static final HLEModuleFunction sceUmdExecReadCapacityCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecReadCapacityCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecReadCapacityCmd [0x5AA96415]");
        }
    };
    public static final HLEModuleFunction sceUmdExecSeekCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecSeekCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecSeekCmd [0x250E6975]");
        }
    };
    public static final HLEModuleFunction sceUmdExecPreventAllowMediaCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecPreventAllowMediaCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecPreventAllowMediaCmd [0x2A08FE9A]");
        }
    };
    public static final HLEModuleFunction sceUmdExecAllocateFromReadCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecAllocateFromReadCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecAllocateFromReadCmd [0x68577709]");
        }
    };
    public static final HLEModuleFunction sceUmdExecReadMKICmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecReadMKICmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecReadMKICmd [0xF819E17C]");
        }
    };
    public static final HLEModuleFunction sceUmdExecSetAreaLimitCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecSetAreaLimitCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecSetAreaLimitCmd [0x61C32A52]");
        }
    };
    public static final HLEModuleFunction sceUmdExecSetAccessLimitCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecSetAccessLimitCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecSetAccessLimitCmd [0x7094E3A7]");
        }
    };
    public static final HLEModuleFunction sceUmdExecSetLockLengthCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecSetLockLengthCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecSetLockLengthCmd [0xD31DAD7E]");
        }
    };
    public static final HLEModuleFunction sceUmdExecGetMediaInfoCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecGetMediaInfoCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecGetMediaInfoCmd [0x108B2322]");
        }
    };
    public static final HLEModuleFunction sceUmdExecReportCacheCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecReportCacheCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecReportCacheCmd [0x98345381]");
        }
    };
    public static final HLEModuleFunction sceUmdExecSetCDSpeedCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecSetCDSpeedCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecSetCDSpeedCmd [0xBF88476F]");
        }
    };
    public static final HLEModuleFunction sceUmdExecSetStreamingCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecSetStreamingCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecSetStreamingCmd [0x485D4925]");
        }
    };
    public static final HLEModuleFunction sceUmdExecClearCacheInfoCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecClearCacheInfoCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecClearCacheInfoCmd [0x73E49F8F]");
        }
    };
    public static final HLEModuleFunction sceUmdExecTestCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecTestCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecTestCmd [0x14D3381C]");
        }
    };
    public static final HLEModuleFunction sceUmdExecAdjustDataCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecAdjustDataCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecAdjustDataCmd [0x92F1CC33]");
        }
    };
    public static final HLEModuleFunction sceUmdExecGetErrorLogCmd = new HLEModuleFunction("sceUmdMan_driver", "sceUmdExecGetErrorLogCmd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdExecGetErrorLogCmd [0x61EB07A5]");
        }
    };
    public static final HLEModuleFunction sceUmdMan_driver_77E6C03A = new HLEModuleFunction("sceUmdMan_driver", "sceUmdMan_driver_77E6C03A") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdMan_driver_77E6C03A [0x77E6C03A]");
        }
    };
    public static final HLEModuleFunction sceUmdMan_driver_A869CAB3 = new HLEModuleFunction("sceUmdMan_driver", "sceUmdMan_driver_A869CAB3") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdMan_driver_A869CAB3 [0xA869CAB3]");
        }
    };
    public static final HLEModuleFunction sceUmdManRegisterImposeCallBack = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManRegisterImposeCallBack") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManRegisterImposeCallBack [0xF65D819F]");
        }
    };
    public static final HLEModuleFunction sceUmdManUnRegisterImposeCallback = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManUnRegisterImposeCallback") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManUnRegisterImposeCallback [0x2787078E]");
        }
    };
    public static final HLEModuleFunction sceUmdMan_driver_1F9AFFF4 = new HLEModuleFunction("sceUmdMan_driver", "sceUmdMan_driver_1F9AFFF4") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdMan_driver_1F9AFFF4 [0x1F9AFFF4]");
        }
    };
    public static final HLEModuleFunction sceUmdManGetDiscInfo = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManGetDiscInfo") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManGetDiscInfo [0x8609D1E4]");
        }
    };
    public static final HLEModuleFunction sceUmdManGetDiscInfo4VSH = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManGetDiscInfo4VSH") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManGetDiscInfo4VSH [0xAAA4ED91]");
        }
    };
    public static final HLEModuleFunction sceUmdManGetUmdDiscInfo = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManGetUmdDiscInfo") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManGetUmdDiscInfo [0xE192C10A]");
        }
    };
    public static final HLEModuleFunction sceUmdManGetReadyFlag = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManGetReadyFlag") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManGetReadyFlag [0x42D28DD1]");
        }
    };
    public static final HLEModuleFunction sceUmdManGetIntrStateFlag = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManGetIntrStateFlag") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManGetIntrStateFlag [0x4FB913A3]");
        }
    };
    public static final HLEModuleFunction sceUmdManSetAlarm = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManSetAlarm") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManSetAlarm [0x7DF2A18D]");
        }
    };
    public static final HLEModuleFunction sceUmdManCancelAlarm = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManCancelAlarm") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManCancelAlarm [0xB0A6DC55]");
        }
    };
    public static final HLEModuleFunction sceUmdMan_driver_D1D4F296 = new HLEModuleFunction("sceUmdMan_driver", "sceUmdMan_driver_D1D4F296") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdMan_driver_D1D4F296 [0xD1D4F296]");
        }
    };
    public static final HLEModuleFunction sceUmdManRegisterInsertEjectUMDCallBack = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManRegisterInsertEjectUMDCallBack") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManRegisterInsertEjectUMDCallBack [0xBF8AED79]");
        }
    };
    public static final HLEModuleFunction sceUmdManUnRegisterInsertEjectUMDCallBack = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManUnRegisterInsertEjectUMDCallBack") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManUnRegisterInsertEjectUMDCallBack [0x4217E7F5]");
        }
    };
    public static final HLEModuleFunction sceUmdManGetPowerStat = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManGetPowerStat") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManGetPowerStat [0x4581E306]");
        }
    };
    public static final HLEModuleFunction sceUmdManChangePowerMode = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManChangePowerMode") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManChangePowerMode [0xCA68F200]");
        }
    };
    public static final HLEModuleFunction sceUmdMan_driver_F4AFF62D = new HLEModuleFunction("sceUmdMan_driver", "sceUmdMan_driver_F4AFF62D") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdMan_driver_F4AFF62D [0xF4AFF62D]");
        }
    };
    public static final HLEModuleFunction sceUmdManSPKGetMKI = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManSPKGetMKI") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManSPKGetMKI [0x7C3D307C]");
        }
    };
    public static final HLEModuleFunction sceUmdMan_driver_B511F821 = new HLEModuleFunction("sceUmdMan_driver", "sceUmdMan_driver_B511F821") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdMan_driver_B511F821 [0xB511F821]");
        }
    };
    public static final HLEModuleFunction sceUmdMan_driver_736AE133 = new HLEModuleFunction("sceUmdMan_driver", "sceUmdMan_driver_736AE133") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdMan_driver_736AE133 [0x736AE133]");
        }
    };
    public static final HLEModuleFunction sceUmdMan_driver_BD13A488 = new HLEModuleFunction("sceUmdMan_driver", "sceUmdMan_driver_BD13A488") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdMan_driver_BD13A488 [0xBD13A488]");
        }
    };
    public static final HLEModuleFunction sceUmdMan_driver_9814DCF3 = new HLEModuleFunction("sceUmdMan_driver", "sceUmdMan_driver_9814DCF3") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdMan_driver_9814DCF3 [0x9814DCF3]");
        }
    };
    public static final HLEModuleFunction sceUmdMan_driver_3DCFAA71 = new HLEModuleFunction("sceUmdMan_driver", "sceUmdMan_driver_3DCFAA71") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdMan_driver_3DCFAA71 [0x3DCFAA71]");
        }
    };
    public static final HLEModuleFunction sceUmdManWaitSema = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManWaitSema") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManWaitSema [0x0DC8D26D]");
        }
    };
    public static final HLEModuleFunction sceUmdManPollSema = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManPollSema") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManPollSema [0x9F106F73]");
        }
    };
    public static final HLEModuleFunction sceUmdManSignalSema = new HLEModuleFunction("sceUmdMan_driver", "sceUmdManSignalSema") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceUmdManSignalSema [0xB0A43DA7]");
        }
    };
};
