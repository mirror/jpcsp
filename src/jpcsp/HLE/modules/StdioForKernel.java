/* This autogenerated file is part of jpcsp. */
package jpcsp.HLE.modules;

import jpcsp.HLE.pspSysMem;
import jpcsp.Memory;
import jpcsp.Processor;

public class StdioForKernel implements HLEModule {

    @Override
    public final String getName() {
        return "StdioForKernel";
    }

    @Override
    public void installModule(HLEModuleManager mm, int version) {

        mm.add(fdprintf, 0x2CCF071A);

        mm.add(printf, 0xCAB439DF);

        mm.add(fdputc, 0x4F78930A);

        mm.add(putchar, 0xD768752A);

        mm.add(fdputs, 0x36B23B8B);

        mm.add(puts, 0xD97C8CB9);

        mm.add(fdgetc, 0xD2B2A2A7);

        mm.add(getchar, 0x7E338487);

        mm.add(fdgets, 0x11A5127A);

        mm.add(gets, 0xBFF7E760);

        mm.add(sceKernelStdin, 0x172D316E);

        mm.add(sceKernelStdout, 0xA6BAB2E9);

        mm.add(sceKernelStderr, 0xF78BA90A);

        mm.add(sceKernelStdoutReopen, 0x98220F3E);

        mm.add(sceKernelStderrReopen, 0xFB5380C5);

    }

    @Override
    public void uninstallModule(HLEModuleManager mm, int version) {

        mm.remove(fdprintf);

        mm.remove(printf);

        mm.remove(fdputc);

        mm.remove(putchar);

        mm.remove(fdputs);

        mm.remove(puts);

        mm.remove(fdgetc);

        mm.remove(getchar);

        mm.remove(fdgets);

        mm.remove(gets);

        mm.remove(sceKernelStdin);

        mm.remove(sceKernelStdout);

        mm.remove(sceKernelStderr);

        mm.remove(sceKernelStdoutReopen);

        mm.remove(sceKernelStderrReopen);

    }
    public static final HLEModuleFunction fdprintf = new HLEModuleFunction("StdioForKernel", "fdprintf") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function fdprintf [0x2CCF071A]");
        }
    };
    public static final HLEModuleFunction printf = new HLEModuleFunction("StdioForKernel", "printf") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function printf [0xCAB439DF]");
        }
    };
    public static final HLEModuleFunction fdputc = new HLEModuleFunction("StdioForKernel", "fdputc") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function fdputc [0x4F78930A]");
        }
    };
    public static final HLEModuleFunction putchar = new HLEModuleFunction("StdioForKernel", "putchar") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function putchar [0xD768752A]");
        }
    };
    public static final HLEModuleFunction fdputs = new HLEModuleFunction("StdioForKernel", "fdputs") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function fdputs [0x36B23B8B]");
        }
    };
    public static final HLEModuleFunction puts = new HLEModuleFunction("StdioForKernel", "puts") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function puts [0xD97C8CB9]");
        }
    };
    public static final HLEModuleFunction fdgetc = new HLEModuleFunction("StdioForKernel", "fdgetc") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function fdgetc [0xD2B2A2A7]");
        }
    };
    public static final HLEModuleFunction getchar = new HLEModuleFunction("StdioForKernel", "getchar") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function getchar [0x7E338487]");
        }
    };
    public static final HLEModuleFunction fdgets = new HLEModuleFunction("StdioForKernel", "fdgets") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function fdgets [0x11A5127A]");
        }
    };
    public static final HLEModuleFunction gets = new HLEModuleFunction("StdioForKernel", "gets") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function gets [0xBFF7E760]");
        }
    };
    public static final HLEModuleFunction sceKernelStdin = new HLEModuleFunction("StdioForKernel", "sceKernelStdin") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelStdin [0x172D316E]");
        }
    };
    public static final HLEModuleFunction sceKernelStdout = new HLEModuleFunction("StdioForKernel", "sceKernelStdout") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelStdout [0xA6BAB2E9]");
        }
    };
    public static final HLEModuleFunction sceKernelStderr = new HLEModuleFunction("StdioForKernel", "sceKernelStderr") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelStderr [0xF78BA90A]");
        }
    };
    public static final HLEModuleFunction sceKernelStdoutReopen = new HLEModuleFunction("StdioForKernel", "sceKernelStdoutReopen") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelStdoutReopen [0x98220F3E]");
        }
    };
    public static final HLEModuleFunction sceKernelStderrReopen = new HLEModuleFunction("StdioForKernel", "sceKernelStderrReopen") {

        @Override
        public void execute(Processor cpu, Memory mem) {
            System.out.println("Unimplement function sceKernelStderrReopen [0xFB5380C5]");
        }
    };
};
