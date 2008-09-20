/* This autogenerated file is part of jpcsp. */
package jpcsp.HLE.modules;

import jpcsp.HLE.pspSysMem;
import jpcsp.Memory;
import jpcsp.Processor;

public class DmacManForKernel implements HLEModule {

    @Override
    public final String getName() {
        return "DmacManForKernel";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {

        mm.add(sceKernelDmaExit, 0x1C46158A);

        mm.add(sceKernelDmaChExclude, 0xD8BC3120);

        mm.add(sceKernelDmaChReserve, 0x2E3BC333);

        mm.add(sceKernelDmaSoftRequest, 0x7B9634E1);

        mm.add(sceKernelDmaOpAlloc, 0x59615199);

        mm.add(sceKernelDmaOpFree, 0x745E19EF);

        mm.add(sceKernelDmaOpAssign, 0xF64BAB99);

        mm.add(sceKernelDmaOpEnQueue, 0x3BDEA96C);

        mm.add(sceKernelDmaOpDeQueue, 0x92700CCD);

        mm.add(sceKernelDmaOpAllCancel, 0xA84B084B);

        mm.add(sceKernelDmaOpSetCallback, 0xD0358BE9);

        mm.add(sceKernelDmaOpSetupMemcpy, 0x3FAD5844);

        mm.add(sceKernelDmaOpSetupNormal, 0xCE467D9B);

        mm.add(sceKernelDmaOpSetupLink, 0x7D21A2EF);

        mm.add(sceKernelDmaOpSync, 0xDB286D65);

        mm.add(sceKernelDmaOpQuit, 0x5AF32783);

        mm.add(DmacManForKernel_E18A93A5, 0xE18A93A5);

        mm.add(sceKernelDmaOpAssignMultiple, 0x904110FC);

        mm.add(sceKernelDmaOnDebugMode, 0xD3F62265);

        mm.add(sceKernelDmaOpFreeLink, 0xA893DA2C);

    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {

        mm.remove(sceKernelDmaExit);

        mm.remove(sceKernelDmaChExclude);

        mm.remove(sceKernelDmaChReserve);

        mm.remove(sceKernelDmaSoftRequest);

        mm.remove(sceKernelDmaOpAlloc);

        mm.remove(sceKernelDmaOpFree);

        mm.remove(sceKernelDmaOpAssign);

        mm.remove(sceKernelDmaOpEnQueue);

        mm.remove(sceKernelDmaOpDeQueue);

        mm.remove(sceKernelDmaOpAllCancel);

        mm.remove(sceKernelDmaOpSetCallback);

        mm.remove(sceKernelDmaOpSetupMemcpy);

        mm.remove(sceKernelDmaOpSetupNormal);

        mm.remove(sceKernelDmaOpSetupLink);

        mm.remove(sceKernelDmaOpSync);

        mm.remove(sceKernelDmaOpQuit);

        mm.remove(DmacManForKernel_E18A93A5);

        mm.remove(sceKernelDmaOpAssignMultiple);

        mm.remove(sceKernelDmaOnDebugMode);

        mm.remove(sceKernelDmaOpFreeLink);

    }
    public static final HLEModuleFunction sceKernelDmaExit = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaExit") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaExit [0x1C46158A]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaChExclude = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaChExclude") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaChExclude [0xD8BC3120]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaChReserve = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaChReserve") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaChReserve [0x2E3BC333]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaSoftRequest = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaSoftRequest") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaSoftRequest [0x7B9634E1]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaOpAlloc = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaOpAlloc") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaOpAlloc [0x59615199]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaOpFree = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaOpFree") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaOpFree [0x745E19EF]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaOpAssign = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaOpAssign") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaOpAssign [0xF64BAB99]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaOpEnQueue = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaOpEnQueue") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaOpEnQueue [0x3BDEA96C]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaOpDeQueue = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaOpDeQueue") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaOpDeQueue [0x92700CCD]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaOpAllCancel = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaOpAllCancel") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaOpAllCancel [0xA84B084B]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaOpSetCallback = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaOpSetCallback") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaOpSetCallback [0xD0358BE9]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaOpSetupMemcpy = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaOpSetupMemcpy") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaOpSetupMemcpy [0x3FAD5844]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaOpSetupNormal = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaOpSetupNormal") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaOpSetupNormal [0xCE467D9B]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaOpSetupLink = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaOpSetupLink") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaOpSetupLink [0x7D21A2EF]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaOpSync = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaOpSync") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaOpSync [0xDB286D65]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaOpQuit = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaOpQuit") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaOpQuit [0x5AF32783]");
        }
    };
    public static final HLEModuleFunction DmacManForKernel_E18A93A5 = new HLEModuleFunction("DmacManForKernel", "DmacManForKernel_E18A93A5") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function DmacManForKernel_E18A93A5 [0xE18A93A5]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaOpAssignMultiple = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaOpAssignMultiple") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaOpAssignMultiple [0x904110FC]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaOnDebugMode = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaOnDebugMode") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaOnDebugMode [0xD3F62265]");
        }
    };
    public static final HLEModuleFunction sceKernelDmaOpFreeLink = new HLEModuleFunction("DmacManForKernel", "sceKernelDmaOpFreeLink") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelDmaOpFreeLink [0xA893DA2C]");
        }
    };
};
