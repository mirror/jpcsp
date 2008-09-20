/* This autogenerated file is part of jpcsp. */
package jpcsp.HLE.modules;

import jpcsp.HLE.pspSysMem;
import jpcsp.Memory;
import jpcsp.Processor;

public class IoFileMgrForUser implements HLEModule {

    @Override
    public final String getName() {
        return "IoFileMgrForUser";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {

        mm.add(sceIoPollAsync, 0x3251EA56);

        mm.add(sceIoWaitAsync, 0xE23EEC33);

        mm.add(sceIoWaitAsyncCB, 0x35DBD746);

        mm.add(sceIoGetAsyncStat, 0xCB05F8D6);

        mm.add(sceIoChangeAsyncPriority, 0xB293727F);

        mm.add(sceIoSetAsyncCallback, 0xA12A0514);

        mm.add(sceIoClose, 0x810C4BC3);

        mm.add(sceIoCloseAsync, 0xFF5940B6);

        mm.add(sceIoOpen, 0x109F50BC);

        mm.add(sceIoOpenAsync, 0x89AA9906);

        mm.add(sceIoRead, 0x6A638D83);

        mm.add(sceIoReadAsync, 0xA0B5A7C2);

        mm.add(sceIoWrite, 0x42EC03AC);

        mm.add(sceIoWriteAsync, 0x0FACAB19);

        mm.add(sceIoLseek, 0x27EB27B8);

        mm.add(sceIoLseekAsync, 0x71B19E77);

        mm.add(sceIoLseek32, 0x68963324);

        mm.add(sceIoLseek32Async, 0x1B385D8F);

        mm.add(sceIoIoctl, 0x63632449);

        mm.add(sceIoIoctlAsync, 0xE95A012B);

        mm.add(sceIoDopen, 0xB29DDF9C);

        mm.add(sceIoDread, 0xE3EB004C);

        mm.add(sceIoDclose, 0xEB092469);

        mm.add(sceIoRemove, 0xF27A9C51);

        mm.add(sceIoMkdir, 0x06A70004);

        mm.add(sceIoRmdir, 0x1117C65F);

        mm.add(sceIoChdir, 0x55F4717D);

        mm.add(sceIoSync, 0xAB96437F);

        mm.add(sceIoGetstat, 0xACE946E8);

        mm.add(sceIoChstat, 0xB8A740F4);

        mm.add(sceIoRename, 0x779103A0);

        mm.add(sceIoDevctl, 0x54F5FB11);

        mm.add(sceIoGetDevType, 0x08BD7374);

        mm.add(sceIoAssign, 0xB2A628C1);

        mm.add(sceIoUnassign, 0x6D08A871);

        mm.add(sceIoCancel, 0xE8BC6571);

        mm.add(sceIoGetFdList, 0x5C2BE2CC);

    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {

        mm.remove(sceIoPollAsync);

        mm.remove(sceIoWaitAsync);

        mm.remove(sceIoWaitAsyncCB);

        mm.remove(sceIoGetAsyncStat);

        mm.remove(sceIoChangeAsyncPriority);

        mm.remove(sceIoSetAsyncCallback);

        mm.remove(sceIoClose);

        mm.remove(sceIoCloseAsync);

        mm.remove(sceIoOpen);

        mm.remove(sceIoOpenAsync);

        mm.remove(sceIoRead);

        mm.remove(sceIoReadAsync);

        mm.remove(sceIoWrite);

        mm.remove(sceIoWriteAsync);

        mm.remove(sceIoLseek);

        mm.remove(sceIoLseekAsync);

        mm.remove(sceIoLseek32);

        mm.remove(sceIoLseek32Async);

        mm.remove(sceIoIoctl);

        mm.remove(sceIoIoctlAsync);

        mm.remove(sceIoDopen);

        mm.remove(sceIoDread);

        mm.remove(sceIoDclose);

        mm.remove(sceIoRemove);

        mm.remove(sceIoMkdir);

        mm.remove(sceIoRmdir);

        mm.remove(sceIoChdir);

        mm.remove(sceIoSync);

        mm.remove(sceIoGetstat);

        mm.remove(sceIoChstat);

        mm.remove(sceIoRename);

        mm.remove(sceIoDevctl);

        mm.remove(sceIoGetDevType);

        mm.remove(sceIoAssign);

        mm.remove(sceIoUnassign);

        mm.remove(sceIoCancel);

        mm.remove(sceIoGetFdList);

    }
    public static final HLEModuleFunction sceIoPollAsync = new HLEModuleFunction("IoFileMgrForUser", "sceIoPollAsync") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoPollAsync [0x3251EA56]");
        }
    };
    public static final HLEModuleFunction sceIoWaitAsync = new HLEModuleFunction("IoFileMgrForUser", "sceIoWaitAsync") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoWaitAsync [0xE23EEC33]");
        }
    };
    public static final HLEModuleFunction sceIoWaitAsyncCB = new HLEModuleFunction("IoFileMgrForUser", "sceIoWaitAsyncCB") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoWaitAsyncCB [0x35DBD746]");
        }
    };
    public static final HLEModuleFunction sceIoGetAsyncStat = new HLEModuleFunction("IoFileMgrForUser", "sceIoGetAsyncStat") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoGetAsyncStat [0xCB05F8D6]");
        }
    };
    public static final HLEModuleFunction sceIoChangeAsyncPriority = new HLEModuleFunction("IoFileMgrForUser", "sceIoChangeAsyncPriority") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoChangeAsyncPriority [0xB293727F]");
        }
    };
    public static final HLEModuleFunction sceIoSetAsyncCallback = new HLEModuleFunction("IoFileMgrForUser", "sceIoSetAsyncCallback") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoSetAsyncCallback [0xA12A0514]");
        }
    };
    public static final HLEModuleFunction sceIoClose = new HLEModuleFunction("IoFileMgrForUser", "sceIoClose") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoClose [0x810C4BC3]");
        }
    };
    public static final HLEModuleFunction sceIoCloseAsync = new HLEModuleFunction("IoFileMgrForUser", "sceIoCloseAsync") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoCloseAsync [0xFF5940B6]");
        }
    };
    public static final HLEModuleFunction sceIoOpen = new HLEModuleFunction("IoFileMgrForUser", "sceIoOpen") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoOpen [0x109F50BC]");
        }
    };
    public static final HLEModuleFunction sceIoOpenAsync = new HLEModuleFunction("IoFileMgrForUser", "sceIoOpenAsync") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoOpenAsync [0x89AA9906]");
        }
    };
    public static final HLEModuleFunction sceIoRead = new HLEModuleFunction("IoFileMgrForUser", "sceIoRead") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoRead [0x6A638D83]");
        }
    };
    public static final HLEModuleFunction sceIoReadAsync = new HLEModuleFunction("IoFileMgrForUser", "sceIoReadAsync") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoReadAsync [0xA0B5A7C2]");
        }
    };
    public static final HLEModuleFunction sceIoWrite = new HLEModuleFunction("IoFileMgrForUser", "sceIoWrite") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoWrite [0x42EC03AC]");
        }
    };
    public static final HLEModuleFunction sceIoWriteAsync = new HLEModuleFunction("IoFileMgrForUser", "sceIoWriteAsync") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoWriteAsync [0x0FACAB19]");
        }
    };
    public static final HLEModuleFunction sceIoLseek = new HLEModuleFunction("IoFileMgrForUser", "sceIoLseek") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoLseek [0x27EB27B8]");
        }
    };
    public static final HLEModuleFunction sceIoLseekAsync = new HLEModuleFunction("IoFileMgrForUser", "sceIoLseekAsync") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoLseekAsync [0x71B19E77]");
        }
    };
    public static final HLEModuleFunction sceIoLseek32 = new HLEModuleFunction("IoFileMgrForUser", "sceIoLseek32") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoLseek32 [0x68963324]");
        }
    };
    public static final HLEModuleFunction sceIoLseek32Async = new HLEModuleFunction("IoFileMgrForUser", "sceIoLseek32Async") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoLseek32Async [0x1B385D8F]");
        }
    };
    public static final HLEModuleFunction sceIoIoctl = new HLEModuleFunction("IoFileMgrForUser", "sceIoIoctl") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoIoctl [0x63632449]");
        }
    };
    public static final HLEModuleFunction sceIoIoctlAsync = new HLEModuleFunction("IoFileMgrForUser", "sceIoIoctlAsync") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoIoctlAsync [0xE95A012B]");
        }
    };
    public static final HLEModuleFunction sceIoDopen = new HLEModuleFunction("IoFileMgrForUser", "sceIoDopen") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoDopen [0xB29DDF9C]");
        }
    };
    public static final HLEModuleFunction sceIoDread = new HLEModuleFunction("IoFileMgrForUser", "sceIoDread") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoDread [0xE3EB004C]");
        }
    };
    public static final HLEModuleFunction sceIoDclose = new HLEModuleFunction("IoFileMgrForUser", "sceIoDclose") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoDclose [0xEB092469]");
        }
    };
    public static final HLEModuleFunction sceIoRemove = new HLEModuleFunction("IoFileMgrForUser", "sceIoRemove") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoRemove [0xF27A9C51]");
        }
    };
    public static final HLEModuleFunction sceIoMkdir = new HLEModuleFunction("IoFileMgrForUser", "sceIoMkdir") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoMkdir [0x06A70004]");
        }
    };
    public static final HLEModuleFunction sceIoRmdir = new HLEModuleFunction("IoFileMgrForUser", "sceIoRmdir") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoRmdir [0x1117C65F]");
        }
    };
    public static final HLEModuleFunction sceIoChdir = new HLEModuleFunction("IoFileMgrForUser", "sceIoChdir") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoChdir [0x55F4717D]");
        }
    };
    public static final HLEModuleFunction sceIoSync = new HLEModuleFunction("IoFileMgrForUser", "sceIoSync") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoSync [0xAB96437F]");
        }
    };
    public static final HLEModuleFunction sceIoGetstat = new HLEModuleFunction("IoFileMgrForUser", "sceIoGetstat") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoGetstat [0xACE946E8]");
        }
    };
    public static final HLEModuleFunction sceIoChstat = new HLEModuleFunction("IoFileMgrForUser", "sceIoChstat") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoChstat [0xB8A740F4]");
        }
    };
    public static final HLEModuleFunction sceIoRename = new HLEModuleFunction("IoFileMgrForUser", "sceIoRename") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoRename [0x779103A0]");
        }
    };
    public static final HLEModuleFunction sceIoDevctl = new HLEModuleFunction("IoFileMgrForUser", "sceIoDevctl") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoDevctl [0x54F5FB11]");
        }
    };
    public static final HLEModuleFunction sceIoGetDevType = new HLEModuleFunction("IoFileMgrForUser", "sceIoGetDevType") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoGetDevType [0x08BD7374]");
        }
    };
    public static final HLEModuleFunction sceIoAssign = new HLEModuleFunction("IoFileMgrForUser", "sceIoAssign") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoAssign [0xB2A628C1]");
        }
    };
    public static final HLEModuleFunction sceIoUnassign = new HLEModuleFunction("IoFileMgrForUser", "sceIoUnassign") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoUnassign [0x6D08A871]");
        }
    };
    public static final HLEModuleFunction sceIoCancel = new HLEModuleFunction("IoFileMgrForUser", "sceIoCancel") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoCancel [0xE8BC6571]");
        }
    };
    public static final HLEModuleFunction sceIoGetFdList = new HLEModuleFunction("IoFileMgrForUser", "sceIoGetFdList") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceIoGetFdList [0x5C2BE2CC]");
        }
    };
};
