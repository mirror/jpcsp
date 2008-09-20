/* This autogenerated file is part of jpcsp. */
package jpcsp.HLE.modules;

import jpcsp.HLE.pspSysMem;
import jpcsp.Memory;
import jpcsp.Processor;

public class sceNand_driver implements HLEModule {

    @Override
    public final String getName() {
        return "sceNand_driver";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {

        mm.add(sceNandInit, 0xA513BB12);

        mm.add(sceNandEnd, 0xD305870E);

        mm.add(sceNandSuspend, 0x73A68408);

        mm.add(sceNandResume, 0x0F9BBBBD);

        mm.add(sceNandSetWriteProtect, 0x84EE5D76);

        mm.add(sceNandLock, 0xAE4438C7);

        mm.add(sceNandUnlock, 0x41FFA822);

        mm.add(sceNandReadStatus, 0xE41A11DE);

        mm.add(sceNandReset, 0x7AF7B77A);

        mm.add(sceNandReadId, 0xFCDF7610);

        mm.add(sceNandReadPages, 0x89BDCA08);

        mm.add(sceNandWritePages, 0x8AF0AB9F);

        mm.add(sceNandReadPagesRawExtra, 0xE05AE88D);

        mm.add(sceNandWritePagesRawExtra, 0x8932166A);

        mm.add(sceNandReadPagesRawAll, 0xC478C1DE);

        mm.add(sceNandWritePagesRawAll, 0xBADD5D46);

        mm.add(sceNandReadAccess, 0x766756EF);

        mm.add(sceNandWriteAccess, 0x0ADC8686);

        mm.add(sceNandEraseBlock, 0xEB0A0022);

        mm.add(sceNandReadExtraOnly, 0x5182C394);

        mm.add(sceNandCalcEcc, 0xEF55F193);

        mm.add(sceNandVerifyEcc, 0x18B78661);

        mm.add(sceNandCollectEcc, 0xB795D2ED);

        mm.add(sceNandDetectChip, 0xD897C343);

        mm.add(sceNandGetPageSize, 0xCE9843E6);

        mm.add(sceNandGetPagesPerBlock, 0xB07C41D4);

        mm.add(sceNandGetTotalBlocks, 0xC1376222);

        mm.add(sceNandWriteBlock, 0x716CD2B2);

        mm.add(sceNandWriteBlockWithVerify, 0xB2B021E5);

        mm.add(sceNandReadBlockWithRetry, 0xC32EA051);

        mm.add(sceNandVerifyBlockWithRetry, 0x5AC02755);

        mm.add(sceNandEraseBlockWithRetry, 0x8933B2E0);

        mm.add(sceNandIsBadBlock, 0x01F09203);

        mm.add(sceNandDoMarkAsBadBlock, 0xC29DA136);

        mm.add(sceNandDumpWearBBMSize, 0x3F76BC21);

        mm.add(sceNandCountChipMakersBBM, 0xEBA0E6C6);

        mm.add(sceNandDetectChipMakersBBM, 0x2FF6081B);

        mm.add(sceNandEraseAllBlock, 0x2674CFFE);

        mm.add(sceNandTestBlock, 0x9B2AC433);

    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {

        mm.remove(sceNandInit);

        mm.remove(sceNandEnd);

        mm.remove(sceNandSuspend);

        mm.remove(sceNandResume);

        mm.remove(sceNandSetWriteProtect);

        mm.remove(sceNandLock);

        mm.remove(sceNandUnlock);

        mm.remove(sceNandReadStatus);

        mm.remove(sceNandReset);

        mm.remove(sceNandReadId);

        mm.remove(sceNandReadPages);

        mm.remove(sceNandWritePages);

        mm.remove(sceNandReadPagesRawExtra);

        mm.remove(sceNandWritePagesRawExtra);

        mm.remove(sceNandReadPagesRawAll);

        mm.remove(sceNandWritePagesRawAll);

        mm.remove(sceNandReadAccess);

        mm.remove(sceNandWriteAccess);

        mm.remove(sceNandEraseBlock);

        mm.remove(sceNandReadExtraOnly);

        mm.remove(sceNandCalcEcc);

        mm.remove(sceNandVerifyEcc);

        mm.remove(sceNandCollectEcc);

        mm.remove(sceNandDetectChip);

        mm.remove(sceNandGetPageSize);

        mm.remove(sceNandGetPagesPerBlock);

        mm.remove(sceNandGetTotalBlocks);

        mm.remove(sceNandWriteBlock);

        mm.remove(sceNandWriteBlockWithVerify);

        mm.remove(sceNandReadBlockWithRetry);

        mm.remove(sceNandVerifyBlockWithRetry);

        mm.remove(sceNandEraseBlockWithRetry);

        mm.remove(sceNandIsBadBlock);

        mm.remove(sceNandDoMarkAsBadBlock);

        mm.remove(sceNandDumpWearBBMSize);

        mm.remove(sceNandCountChipMakersBBM);

        mm.remove(sceNandDetectChipMakersBBM);

        mm.remove(sceNandEraseAllBlock);

        mm.remove(sceNandTestBlock);

    }
    public static final HLEModuleFunction sceNandInit = new HLEModuleFunction("sceNand_driver", "sceNandInit") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandInit [0xA513BB12]");
        }
    };
    public static final HLEModuleFunction sceNandEnd = new HLEModuleFunction("sceNand_driver", "sceNandEnd") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandEnd [0xD305870E]");
        }
    };
    public static final HLEModuleFunction sceNandSuspend = new HLEModuleFunction("sceNand_driver", "sceNandSuspend") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandSuspend [0x73A68408]");
        }
    };
    public static final HLEModuleFunction sceNandResume = new HLEModuleFunction("sceNand_driver", "sceNandResume") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandResume [0x0F9BBBBD]");
        }
    };
    public static final HLEModuleFunction sceNandSetWriteProtect = new HLEModuleFunction("sceNand_driver", "sceNandSetWriteProtect") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandSetWriteProtect [0x84EE5D76]");
        }
    };
    public static final HLEModuleFunction sceNandLock = new HLEModuleFunction("sceNand_driver", "sceNandLock") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandLock [0xAE4438C7]");
        }
    };
    public static final HLEModuleFunction sceNandUnlock = new HLEModuleFunction("sceNand_driver", "sceNandUnlock") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandUnlock [0x41FFA822]");
        }
    };
    public static final HLEModuleFunction sceNandReadStatus = new HLEModuleFunction("sceNand_driver", "sceNandReadStatus") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandReadStatus [0xE41A11DE]");
        }
    };
    public static final HLEModuleFunction sceNandReset = new HLEModuleFunction("sceNand_driver", "sceNandReset") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandReset [0x7AF7B77A]");
        }
    };
    public static final HLEModuleFunction sceNandReadId = new HLEModuleFunction("sceNand_driver", "sceNandReadId") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandReadId [0xFCDF7610]");
        }
    };
    public static final HLEModuleFunction sceNandReadPages = new HLEModuleFunction("sceNand_driver", "sceNandReadPages") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandReadPages [0x89BDCA08]");
        }
    };
    public static final HLEModuleFunction sceNandWritePages = new HLEModuleFunction("sceNand_driver", "sceNandWritePages") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandWritePages [0x8AF0AB9F]");
        }
    };
    public static final HLEModuleFunction sceNandReadPagesRawExtra = new HLEModuleFunction("sceNand_driver", "sceNandReadPagesRawExtra") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandReadPagesRawExtra [0xE05AE88D]");
        }
    };
    public static final HLEModuleFunction sceNandWritePagesRawExtra = new HLEModuleFunction("sceNand_driver", "sceNandWritePagesRawExtra") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandWritePagesRawExtra [0x8932166A]");
        }
    };
    public static final HLEModuleFunction sceNandReadPagesRawAll = new HLEModuleFunction("sceNand_driver", "sceNandReadPagesRawAll") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandReadPagesRawAll [0xC478C1DE]");
        }
    };
    public static final HLEModuleFunction sceNandWritePagesRawAll = new HLEModuleFunction("sceNand_driver", "sceNandWritePagesRawAll") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandWritePagesRawAll [0xBADD5D46]");
        }
    };
    public static final HLEModuleFunction sceNandReadAccess = new HLEModuleFunction("sceNand_driver", "sceNandReadAccess") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandReadAccess [0x766756EF]");
        }
    };
    public static final HLEModuleFunction sceNandWriteAccess = new HLEModuleFunction("sceNand_driver", "sceNandWriteAccess") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandWriteAccess [0x0ADC8686]");
        }
    };
    public static final HLEModuleFunction sceNandEraseBlock = new HLEModuleFunction("sceNand_driver", "sceNandEraseBlock") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandEraseBlock [0xEB0A0022]");
        }
    };
    public static final HLEModuleFunction sceNandReadExtraOnly = new HLEModuleFunction("sceNand_driver", "sceNandReadExtraOnly") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandReadExtraOnly [0x5182C394]");
        }
    };
    public static final HLEModuleFunction sceNandCalcEcc = new HLEModuleFunction("sceNand_driver", "sceNandCalcEcc") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandCalcEcc [0xEF55F193]");
        }
    };
    public static final HLEModuleFunction sceNandVerifyEcc = new HLEModuleFunction("sceNand_driver", "sceNandVerifyEcc") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandVerifyEcc [0x18B78661]");
        }
    };
    public static final HLEModuleFunction sceNandCollectEcc = new HLEModuleFunction("sceNand_driver", "sceNandCollectEcc") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandCollectEcc [0xB795D2ED]");
        }
    };
    public static final HLEModuleFunction sceNandDetectChip = new HLEModuleFunction("sceNand_driver", "sceNandDetectChip") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandDetectChip [0xD897C343]");
        }
    };
    public static final HLEModuleFunction sceNandGetPageSize = new HLEModuleFunction("sceNand_driver", "sceNandGetPageSize") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandGetPageSize [0xCE9843E6]");
        }
    };
    public static final HLEModuleFunction sceNandGetPagesPerBlock = new HLEModuleFunction("sceNand_driver", "sceNandGetPagesPerBlock") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandGetPagesPerBlock [0xB07C41D4]");
        }
    };
    public static final HLEModuleFunction sceNandGetTotalBlocks = new HLEModuleFunction("sceNand_driver", "sceNandGetTotalBlocks") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandGetTotalBlocks [0xC1376222]");
        }
    };
    public static final HLEModuleFunction sceNandWriteBlock = new HLEModuleFunction("sceNand_driver", "sceNandWriteBlock") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandWriteBlock [0x716CD2B2]");
        }
    };
    public static final HLEModuleFunction sceNandWriteBlockWithVerify = new HLEModuleFunction("sceNand_driver", "sceNandWriteBlockWithVerify") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandWriteBlockWithVerify [0xB2B021E5]");
        }
    };
    public static final HLEModuleFunction sceNandReadBlockWithRetry = new HLEModuleFunction("sceNand_driver", "sceNandReadBlockWithRetry") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandReadBlockWithRetry [0xC32EA051]");
        }
    };
    public static final HLEModuleFunction sceNandVerifyBlockWithRetry = new HLEModuleFunction("sceNand_driver", "sceNandVerifyBlockWithRetry") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandVerifyBlockWithRetry [0x5AC02755]");
        }
    };
    public static final HLEModuleFunction sceNandEraseBlockWithRetry = new HLEModuleFunction("sceNand_driver", "sceNandEraseBlockWithRetry") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandEraseBlockWithRetry [0x8933B2E0]");
        }
    };
    public static final HLEModuleFunction sceNandIsBadBlock = new HLEModuleFunction("sceNand_driver", "sceNandIsBadBlock") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandIsBadBlock [0x01F09203]");
        }
    };
    public static final HLEModuleFunction sceNandDoMarkAsBadBlock = new HLEModuleFunction("sceNand_driver", "sceNandDoMarkAsBadBlock") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandDoMarkAsBadBlock [0xC29DA136]");
        }
    };
    public static final HLEModuleFunction sceNandDumpWearBBMSize = new HLEModuleFunction("sceNand_driver", "sceNandDumpWearBBMSize") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandDumpWearBBMSize [0x3F76BC21]");
        }
    };
    public static final HLEModuleFunction sceNandCountChipMakersBBM = new HLEModuleFunction("sceNand_driver", "sceNandCountChipMakersBBM") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandCountChipMakersBBM [0xEBA0E6C6]");
        }
    };
    public static final HLEModuleFunction sceNandDetectChipMakersBBM = new HLEModuleFunction("sceNand_driver", "sceNandDetectChipMakersBBM") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandDetectChipMakersBBM [0x2FF6081B]");
        }
    };
    public static final HLEModuleFunction sceNandEraseAllBlock = new HLEModuleFunction("sceNand_driver", "sceNandEraseAllBlock") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandEraseAllBlock [0x2674CFFE]");
        }
    };
    public static final HLEModuleFunction sceNandTestBlock = new HLEModuleFunction("sceNand_driver", "sceNandTestBlock") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceNandTestBlock [0x9B2AC433]");
        }
    };
};
