/*
This file is part of jpcsp.

Jpcsp is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Jpcsp is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Jpcsp.  If not, see <http://www.gnu.org/licenses/>.
 */
package jpcsp.Allegrex.compiler;

import static java.lang.Math.min;
import static jpcsp.Allegrex.Common._f0;
import static jpcsp.Allegrex.Common._ra;
import static jpcsp.Allegrex.Common._sp;
import static jpcsp.Allegrex.Common._v0;
import static jpcsp.Allegrex.Common._v1;
import static jpcsp.Allegrex.Common._zr;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.Processor;
import jpcsp.State;
import jpcsp.Allegrex.Common;
import jpcsp.Allegrex.CpuState;
import jpcsp.Allegrex.GprState;
import jpcsp.Allegrex.Instructions;
import jpcsp.Allegrex.VfpuState;
import jpcsp.Allegrex.Common.Instruction;
import jpcsp.Allegrex.FpuState.Fcr31;
import jpcsp.Allegrex.VfpuState.Vcr;
import jpcsp.Allegrex.VfpuState.Vcr.PfxDst;
import jpcsp.Allegrex.VfpuState.Vcr.PfxSrc;
import jpcsp.Allegrex.compiler.nativeCode.NativeCodeInstruction;
import jpcsp.Allegrex.compiler.nativeCode.NativeCodeManager;
import jpcsp.Allegrex.compiler.nativeCode.NativeCodeSequence;
import jpcsp.Allegrex.compiler.nativeCode.Nop;
import jpcsp.HLE.CanBeNull;
import jpcsp.HLE.CheckArgument;
import jpcsp.HLE.HLEUidClass;
import jpcsp.HLE.HLEUidObjectMapping;
import jpcsp.HLE.Modules;
import jpcsp.HLE.PspString;
import jpcsp.HLE.SceKernelErrorException;
import jpcsp.HLE.StringInfo;
import jpcsp.HLE.SyscallHandler;
import jpcsp.HLE.TErrorPointer32;
import jpcsp.HLE.TPointer;
import jpcsp.HLE.TPointer16;
import jpcsp.HLE.TPointer32;
import jpcsp.HLE.TPointer64;
import jpcsp.HLE.kernel.managers.IntrManager;
import jpcsp.HLE.kernel.types.SceKernelErrors;
import jpcsp.HLE.kernel.types.SceKernelThreadInfo;
import jpcsp.HLE.kernel.types.pspAbstractMemoryMappedStructure;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;
import jpcsp.HLE.modules.ThreadManForUser;
import jpcsp.hardware.Interrupts;
import jpcsp.memory.FastMemory;
import jpcsp.memory.SafeFastMemory;
import jpcsp.util.ClassAnalyzer;
import jpcsp.util.DurationStatistics;
import jpcsp.util.ClassAnalyzer.ParameterInfo;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author gid15
 *
 */
public class CompilerContext implements ICompilerContext {
	protected static Logger log = Compiler.log;
	private CompilerClassLoader classLoader;
	private CodeBlock codeBlock;
	private int numberInstructionsToBeSkipped;
	private boolean skipDelaySlot;
	private MethodVisitor mv;
	private CodeInstruction codeInstruction;
	private static final boolean storeCpuLocal = true;
	private static final boolean storeMemoryIntLocal = false;
    private static final int LOCAL_CPU = 0;
    private static final int LOCAL_INSTRUCTION_COUNT = 1;
    private static final int LOCAL_MEMORY_INT = 2;
    private static final int LOCAL_TMP1 = 3;
    private static final int LOCAL_TMP2 = 4;
    private static final int LOCAL_TMP3 = 5;
    private static final int LOCAL_TMP4 = 6;
    private static final int LOCAL_TMP_VD0 = 7;
    private static final int LOCAL_TMP_VD1 = 8;
    private static final int LOCAL_TMP_VD2 = 9;
    private static final int LOCAL_MAX = 10;
    private static final int DEFAULT_MAX_STACK_SIZE = 11;
    private static final int SYSCALL_MAX_STACK_SIZE = 100;
    private static final int LOCAL_ERROR_POINTER = LOCAL_TMP3;
	private boolean enableIntructionCounting = false;
    public Set<Integer> analysedAddresses = new HashSet<Integer>();
    public Stack<Integer> blocksToBeAnalysed = new Stack<Integer>();
    private int currentInstructionCount;
    private int preparedRegisterForStore = -1;
    private boolean memWritePrepared = false;
    private boolean hiloPrepared = false;
    private int methodMaxInstructions;
    private NativeCodeManager nativeCodeManager;
    private final VfpuPfxSrcState vfpuPfxsState = new VfpuPfxSrcState();
    private final VfpuPfxSrcState vfpuPfxtState = new VfpuPfxSrcState();
    private final VfpuPfxDstState vfpuPfxdState = new VfpuPfxDstState();
    private Label interpretPfxLabel = null;
    private boolean pfxVdOverlap = false;
    private static final String runtimeContextInternalName = Type.getInternalName(RuntimeContext.class);
    private static final String processorDescriptor = Type.getDescriptor(Processor.class);
    private static final String cpuDescriptor = Type.getDescriptor(CpuState.class);
    private static final String cpuInternalName = Type.getInternalName(CpuState.class);
    private static final String instructionsInternalName = Type.getInternalName(Instructions.class);
    private static final String instructionInternalName = Type.getInternalName(Instruction.class);
    private static final String instructionDescriptor = Type.getDescriptor(Instruction.class);
    private static final String sceKernalThreadInfoInternalName = Type.getInternalName(SceKernelThreadInfo.class);
    private static final String sceKernalThreadInfoDescriptor = Type.getDescriptor(SceKernelThreadInfo.class);
    private static final String stringDescriptor = Type.getDescriptor(String.class);
    private static final String memoryDescriptor = Type.getDescriptor(Memory.class);
    private static final String memoryInternalName = Type.getInternalName(Memory.class);
    private static final String profilerInternalName = Type.getInternalName(Profiler.class);
	public  static final String executableDescriptor = Type.getDescriptor(IExecutable.class);
	public  static final String executableInternalName = Type.getInternalName(IExecutable.class);
	private static final String arraycopyDescriptor = "(" + Type.getDescriptor(Object.class) + "I" + Type.getDescriptor(Object.class) + "II)V";
	private static Set<Integer> fastSyscalls;
	private int instanceIndex;
	private NativeCodeSequence preparedCallNativeCodeBlock = null;
	private int maxStackSize = DEFAULT_MAX_STACK_SIZE;
	private CompilerTypeManager compilerTypeManager;

	public CompilerContext(CompilerClassLoader classLoader, int instanceIndex) {
    	Compiler compiler = Compiler.getInstance();
        this.classLoader = classLoader;
        this.instanceIndex = instanceIndex;
        nativeCodeManager = compiler.getNativeCodeManager();
        methodMaxInstructions = compiler.getDefaultMethodMaxInstructions();
        compilerTypeManager = compiler.getCompilerTypeManager();

        // Count instructions only when the profile is enabled or
        // when the statistics are enabled
        if (Profiler.isProfilerEnabled() || DurationStatistics.collectStatistics) {
        	enableIntructionCounting = true;
        }

        if (fastSyscalls == null) {
	        fastSyscalls = new TreeSet<Integer>();
	        addFastSyscall(0x3AD58B8C); // sceKernelSuspendDispatchThread
	        addFastSyscall(0x110DEC9A); // sceKernelUSec2SysClock
	        addFastSyscall(0xC8CD158C); // sceKernelUSec2SysClockWide
	        addFastSyscall(0xBA6B92E2); // sceKernelSysClock2USec 
	        addFastSyscall(0xE1619D7C); // sceKernelSysClock2USecWide 
	        addFastSyscall(0xDB738F35); // sceKernelGetSystemTime
	        addFastSyscall(0x82BC5777); // sceKernelGetSystemTimeWide
	        addFastSyscall(0x369ED59D); // sceKernelGetSystemTimeLow
	        addFastSyscall(0xB5F6DC87); // sceMpegRingbufferAvailableSize
	        addFastSyscall(0xE0D68148); // sceGeListUpdateStallAddr
	        addFastSyscall(0x34B9FA9E); // sceKernelDcacheWritebackInvalidateRangeFunction
	        addFastSyscall(0xE47E40E4); // sceGeEdramGetAddrFunction
	        addFastSyscall(0x1F6752AD); // sceGeEdramGetSizeFunction
	        addFastSyscall(0x74AE582A); // __sceSasGetEnvelopeHeight
	        addFastSyscall(0x68A46B95); // __sceSasGetEndFlag
        }
    }

    private void addFastSyscall(int nid) {
        fastSyscalls.add(HLEModuleManager.getInstance().getSyscallFromNid(nid));
    }

    public CompilerClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(CompilerClassLoader classLoader) {
		this.classLoader = classLoader;
	}

    public CodeBlock getCodeBlock() {
        return codeBlock;
    }

    public void setCodeBlock(CodeBlock codeBlock) {
        this.codeBlock = codeBlock;
    }

    public NativeCodeManager getNativeCodeManager() {
    	return nativeCodeManager;
    }

    private void loadCpu() {
    	if (storeCpuLocal) {
    		mv.visitVarInsn(Opcodes.ALOAD, LOCAL_CPU);
    	} else {
    		mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "cpu", cpuDescriptor);
    	}
	}

    private void loadProcessor() {
        mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "processor", processorDescriptor);
    }

    private void loadMemory() {
		mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "memory", memoryDescriptor);
    }

    private void loadModule(String moduleName) {
        mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Modules.class), moduleName + "Module", "Ljpcsp/HLE/modules/" + moduleName + ";");
    }

    private void loadFpr() {
		mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "fpr", "[F");
    }

    private void loadVprFloat() {
		mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "vprFloat", "[F");
    }

    private void loadVprInt() {
		mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "vprInt", "[I");
    }

    @Override
    public void loadRegister(int reg) {
    	if (reg == _zr) {
    		loadImm(0);
    	} else {
	    	loadCpu();
	        mv.visitFieldInsn(Opcodes.GETFIELD, cpuInternalName, getGprFieldName(reg), "I");
    	}
    }

    @Override
    public void loadFRegister(int reg) {
    	loadFpr();
    	loadImm(reg);
        mv.visitInsn(Opcodes.FALOAD);
    }

    private Float getPfxSrcCstValue(VfpuPfxSrcState pfxSrcState, int n) {
    	if (pfxSrcState == null ||
    	    pfxSrcState.isUnknown() ||
    	    !pfxSrcState.pfxSrc.enabled ||
    	    !pfxSrcState.pfxSrc.cst[n]) {
    		return null;
    	}

    	float value = 0.0f;
		switch (pfxSrcState.pfxSrc.swz[n]) {
			case 0:
				value = pfxSrcState.pfxSrc.abs[n] ? 3.0f : 0.0f;
				break;
			case 1:
				value = pfxSrcState.pfxSrc.abs[n] ? (1.0f / 3.0f) : 1.0f;
				break;
			case 2:
				value = pfxSrcState.pfxSrc.abs[n] ? (1.0f / 4.0f) : 2.0f;
				break;
			case 3:
				value = pfxSrcState.pfxSrc.abs[n] ? (1.0f / 6.0f) : 0.5f;
				break;
		}

		if (pfxSrcState.pfxSrc.neg[n]) {
			value = 0.0f - value;
		}

		if (log.isTraceEnabled() && pfxSrcState.isKnown() && pfxSrcState.pfxSrc.enabled) {
			log.trace(String.format("PFX    %08X - getPfxSrcCstValue %d -> %f", getCodeInstruction().getAddress(), n, value));
		}

		return new Float(value);
    }

    private void convertVFloatToInt() {
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Float.class), "floatToRawIntBits", "(F)I");
    }

    private void convertVIntToFloat() {
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Float.class), "intBitsToFloat", "(I)F");
    }

    private void applyPfxSrcPostfix(VfpuPfxSrcState pfxSrcState, int n, boolean isFloat) {
    	if (pfxSrcState == null ||
    	    pfxSrcState.isUnknown() ||
    	    !pfxSrcState.pfxSrc.enabled) {
    		return;
    	}

    	if (pfxSrcState.pfxSrc.abs[n]) {
			if (log.isTraceEnabled() && pfxSrcState.isKnown() && pfxSrcState.pfxSrc.enabled) {
				log.trace(String.format("PFX    %08X - applyPfxSrcPostfix abs(%d)", getCodeInstruction().getAddress(), n));
			}

			if (isFloat) {
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Math.class), "abs", "(F)F");
			} else {
    			loadImm(0x7FFFFFFF);
    			mv.visitInsn(Opcodes.IAND);
			}
    	}
    	if (pfxSrcState.pfxSrc.neg[n]) {
			if (log.isTraceEnabled() && pfxSrcState.isKnown() && pfxSrcState.pfxSrc.enabled) {
				log.trace(String.format("PFX    %08X - applyPfxSrcPostfix neg(%d)", getCodeInstruction().getAddress(), n));
			}

			if (isFloat) {
				mv.visitInsn(Opcodes.FNEG);
			} else {
    			loadImm(0x80000000);
    			mv.visitInsn(Opcodes.IXOR);
			}
    	}
    }

    private int getPfxSrcIndex(VfpuPfxSrcState pfxSrcState, int n) {
    	if (pfxSrcState == null ||
    	    pfxSrcState.isUnknown() ||
    	    !pfxSrcState.pfxSrc.enabled ||
    	    pfxSrcState.pfxSrc.cst[n]) {
    		return n;
    	}

		if (log.isTraceEnabled() && pfxSrcState.isKnown() && pfxSrcState.pfxSrc.enabled) {
			log.trace(String.format("PFX    %08X - getPfxSrcIndex %d -> %d", getCodeInstruction().getAddress(), n, pfxSrcState.pfxSrc.swz[n]));
		}
    	return pfxSrcState.pfxSrc.swz[n];
    }

    private void loadVRegister(int m, int c, int r, boolean isFloat) {
    	int index = VfpuState.getVprIndex(m, c, r);
    	if (isFloat) {
    		loadVprFloat();
    		loadImm(index);
            mv.visitInsn(Opcodes.FALOAD);
    	} else {
    		loadVprInt();
    		loadImm(index);
            mv.visitInsn(Opcodes.IALOAD);
    	}
    }

    private void loadCstValue(Float cstValue, boolean isFloat) {
    	if (isFloat) {
    		mv.visitLdcInsn(cstValue.floatValue());
    	} else {
    		loadImm(Float.floatToRawIntBits(cstValue.floatValue()));
    	}
    }

    private void loadVRegister(int vsize, int reg, int n, VfpuPfxSrcState pfxSrcState, boolean isFloat) {
		if (log.isTraceEnabled() && pfxSrcState != null && pfxSrcState.isKnown() && pfxSrcState.pfxSrc.enabled) {
			log.trace(String.format("PFX    %08X - loadVRegister %d, %d, %d", getCodeInstruction().getAddress(), vsize, reg, n));
		}

		int m = (reg >> 2) & 7;
    	int i = (reg >> 0) & 3;
    	int s;
    	switch (vsize) {
    		case 1: {
    			s = (reg >> 5) & 3;
    			Float cstValue = getPfxSrcCstValue(pfxSrcState, n);
    			if (cstValue != null) {
    				loadCstValue(cstValue, isFloat);
    			} else {
    			    loadVRegister(m, i, s, isFloat);
	    			applyPfxSrcPostfix(pfxSrcState, n, isFloat);
    			}
    			break;
    		}
    		case 2: {
                s = (reg & 64) >> 5;
                Float cstValue = getPfxSrcCstValue(pfxSrcState, n);
                if (cstValue != null) {
    				loadCstValue(cstValue, isFloat);
                } else {
                	int index = getPfxSrcIndex(pfxSrcState, n);
	                if ((reg & 32) != 0) {
	                    loadVRegister(m, s + index, i, isFloat);
	                } else {
	                    loadVRegister(m, i, s + index, isFloat);
	                }
	                applyPfxSrcPostfix(pfxSrcState, n, isFloat);
                }
                break;
    		}
            case 3: {
                s = (reg & 64) >> 6;
                Float cstValue = getPfxSrcCstValue(pfxSrcState, n);
                if (cstValue != null) {
    				loadCstValue(cstValue, isFloat);
                } else {
                	int index = getPfxSrcIndex(pfxSrcState, n);
	                if ((reg & 32) != 0) {
	                    loadVRegister(m, s + index, i, isFloat);
	                } else {
	                    loadVRegister(m, i, s + index, isFloat);
	                }
	                applyPfxSrcPostfix(pfxSrcState, n, isFloat);
                }
                break;
    		}
            case 4: {
            	s = (reg & 64) >> 5;
                Float cstValue = getPfxSrcCstValue(pfxSrcState, n);
                if (cstValue != null) {
    				loadCstValue(cstValue, isFloat);
                } else {
                	int index = getPfxSrcIndex(pfxSrcState, (n + s) & 3);
	                if ((reg & 32) != 0) {
	                    loadVRegister(m, index, i, isFloat);
	                } else {
	                    loadVRegister(m, i, index, isFloat);
	                }
	                applyPfxSrcPostfix(pfxSrcState, n, isFloat);
                }
            	break;
            }
    	}
    }

    public void prepareRegisterForStore(int reg) {
    	if (preparedRegisterForStore < 0) {
        	loadCpu();
    		preparedRegisterForStore = reg;
    	}
    }

    private String getGprFieldName(int reg) {
    	return Common.gprNames[reg].replace('$', '_');
    }

    public void storeRegister(int reg) {
    	if (preparedRegisterForStore == reg) {
	        mv.visitFieldInsn(Opcodes.PUTFIELD, cpuInternalName, getGprFieldName(reg), "I");
	        preparedRegisterForStore = -1;
    	} else {
	    	loadCpu();
	        mv.visitInsn(Opcodes.SWAP);
	        mv.visitFieldInsn(Opcodes.PUTFIELD, cpuInternalName, getGprFieldName(reg), "I");
    	}
    }

    public void storeRegister(int reg, int constantValue) {
    	if (preparedRegisterForStore == reg) {
    		preparedRegisterForStore = -1;
    	} else {
    		loadCpu();
    	}
    	loadImm(constantValue);
        mv.visitFieldInsn(Opcodes.PUTFIELD, cpuInternalName, getGprFieldName(reg), "I");
    }

    public void prepareFRegisterForStore(int reg) {
    	if (preparedRegisterForStore < 0) {
        	loadFpr();
        	loadImm(reg);
    		preparedRegisterForStore = reg;
    	}
    }

    public void storeFRegister(int reg) {
    	if (preparedRegisterForStore == reg) {
	        mv.visitInsn(Opcodes.FASTORE);
	        preparedRegisterForStore = -1;
    	} else {
	    	loadFpr();
	        mv.visitInsn(Opcodes.SWAP);
	        loadImm(reg);
	        mv.visitInsn(Opcodes.SWAP);
	        mv.visitInsn(Opcodes.FASTORE);
    	}
    }

    private boolean isPfxDstMasked(VfpuPfxDstState pfxDstState, int n) {
    	if (pfxDstState == null ||
    		pfxDstState.isUnknown() ||
    		!pfxDstState.pfxDst.enabled) {
    		return false;
    	}

    	return pfxDstState.pfxDst.msk[n];
    }

    private void applyPfxDstPostfix(VfpuPfxDstState pfxDstState, int n, boolean isFloat) {
    	if (pfxDstState == null ||
    		pfxDstState.isUnknown() ||
    	    !pfxDstState.pfxDst.enabled) {
    		return;
    	}

    	switch (pfxDstState.pfxDst.sat[n]) {
    		case 1:
				if (log.isTraceEnabled() && pfxDstState != null && pfxDstState.isKnown() && pfxDstState.pfxDst.enabled) {
					log.trace(String.format("PFX    %08X - applyPfxDstPostfix %d [0:1]", getCodeInstruction().getAddress(), n));
				}
				if (!isFloat) {
					convertVIntToFloat();
				}
    			mv.visitLdcInsn(1.0f);
        		mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Math.class), "min", "(FF)F");
    			mv.visitLdcInsn(0.0f);
        		mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Math.class), "max", "(FF)F");
        		if (!isFloat) {
        			convertVFloatToInt();
        		}
        		break;
    		case 3:
				if (log.isTraceEnabled() && pfxDstState != null && pfxDstState.isKnown() && pfxDstState.pfxDst.enabled) {
					log.trace(String.format("PFX    %08X - applyPfxDstPostfix %d [-1:1]", getCodeInstruction().getAddress(), n));
				}
				if (!isFloat) {
					convertVIntToFloat();
				}
    			mv.visitLdcInsn(1.0f);
        		mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Math.class), "min", "(FF)F");
    			mv.visitLdcInsn(-1.0f);
        		mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Math.class), "max", "(FF)F");
        		if (!isFloat) {
        			convertVFloatToInt();
        		}
        		break;
    	}
    }

    private void prepareVRegisterForStore(int m, int c, int r, boolean isFloat) {
    	int index = VfpuState.getVprIndex(m, c, r);
    	if (isFloat) {
    		// Prepare the array and index for the int value
    		loadVprInt();
    		loadImm(index);

    		// Prepare the array and index for the float value
    		loadVprFloat();
    		loadImm(index);
    	} else {
    		// Prepare the array and index for the float value
    		loadVprFloat();
    		loadImm(index);

    		// Prepare the array and index for the int value
    		loadVprInt();
    		loadImm(index);
    	}
    }

    public void prepareVRegisterForStore(int vsize, int reg, int n, VfpuPfxDstState pfxDstState, boolean isFloat) {
    	if (preparedRegisterForStore < 0) {
            if (!isPfxDstMasked(pfxDstState, n)) {
            	int m = (reg >> 2) & 7;
            	int i = (reg >> 0) & 3;
            	int s;
            	switch (vsize) {
            		case 1: {
                        s = (reg >> 5) & 3;
                        prepareVRegisterForStore(m, i, s, isFloat);
            			break;
            		}
            		case 2: {
                        s = (reg & 64) >> 5;
                        if ((reg & 32) != 0) {
                            prepareVRegisterForStore(m, s + n, i, isFloat);
                        } else {
                            prepareVRegisterForStore(m, i, s + n, isFloat);
                        }
                        break;
            		}
                    case 3: {
                        s = (reg & 64) >> 6;
                        if ((reg & 32) != 0) {
                            prepareVRegisterForStore(m, s + n, i, isFloat);
                        } else {
                            prepareVRegisterForStore(m, i, s + n, isFloat);
                        }
                        break;
            		}
                    case 4: {
                    	s = (reg & 64) >> 5;
                        if ((reg & 32) != 0) {
                            prepareVRegisterForStore(m, (n + s) & 3, i, isFloat);
                        } else {
                            prepareVRegisterForStore(m, i, (n + s) & 3, isFloat);
                        }
                    	break;
                    }
            	}
            }
    		preparedRegisterForStore = reg;
    	}
    }

    private void storeVRegister(int vsize, int reg, int n, VfpuPfxDstState pfxDstState, boolean isFloat) {
		if (log.isTraceEnabled() && pfxDstState != null && pfxDstState.isKnown() && pfxDstState.pfxDst.enabled) {
			log.trace(String.format("PFX    %08X - storeVRegister %d, %d, %d", getCodeInstruction().getAddress(), vsize, reg, n));
		}

    	if (preparedRegisterForStore == reg) {
            if (isPfxDstMasked(pfxDstState, n)) {
				if (log.isTraceEnabled() && pfxDstState != null && pfxDstState.isKnown() && pfxDstState.pfxDst.enabled) {
					log.trace(String.format("PFX    %08X - storeVRegister %d masked", getCodeInstruction().getAddress(), n));
				}

                mv.visitInsn(Opcodes.POP);
            } else {
                applyPfxDstPostfix(pfxDstState, n, isFloat);
                if (isFloat) {
                	// Keep a copy of the value for the int value
                	mv.visitInsn(Opcodes.DUP_X2);
                	mv.visitInsn(Opcodes.FASTORE); // First store the float value
                	convertVFloatToInt();
                	mv.visitInsn(Opcodes.IASTORE); // Second store the int value
                } else {
                	// Keep a copy of the value for the float value
                	mv.visitInsn(Opcodes.DUP_X2);
                	mv.visitInsn(Opcodes.IASTORE); // First store the int value
                	convertVIntToFloat();
                	mv.visitInsn(Opcodes.FASTORE); // Second store the float value
                }
            }
	        preparedRegisterForStore = -1;
    	} else {
    		log.error("storeVRegister with non-prepared register is not supported");
    	}
    }

    public void loadFcr31() {
    	loadCpu();
        mv.visitFieldInsn(Opcodes.GETFIELD, cpuInternalName, "fcr31", Type.getDescriptor(Fcr31.class));
    }

    public void loadVcr() {
    	loadCpu();
        mv.visitFieldInsn(Opcodes.GETFIELD, cpuInternalName, "vcr", Type.getDescriptor(Vcr.class));
    }

	@Override
	public void loadHilo() {
		loadCpu();
        mv.visitFieldInsn(Opcodes.GETFIELD, cpuInternalName, "hilo", Type.getDescriptor(long.class));
	}

	@Override
	public void prepareHiloForStore() {
		loadCpu();
		hiloPrepared = true;
	}

	@Override
	public void storeHilo() {
		if (!hiloPrepared) {
			loadCpu();
			mv.visitInsn(Opcodes.DUP_X2);
        	mv.visitInsn(Opcodes.POP);
		}
        mv.visitFieldInsn(Opcodes.PUTFIELD, cpuInternalName, "hilo", Type.getDescriptor(long.class));

        hiloPrepared = false;
	}

	@Override
	public void loadFcr31c() {
    	loadFcr31();
        mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(Fcr31.class), "c", "Z");
    }

	@Override
	public void prepareFcr31cForStore() {
		loadFcr31();
	}

	@Override
	public void storeFcr31c() {
        mv.visitFieldInsn(Opcodes.PUTFIELD, Type.getInternalName(Fcr31.class), "c", "Z");
	}

	public void loadVcrCc() {
		loadVcrCc((codeInstruction.getOpcode() >> 18) & 7);
	}

	@Override
	public void loadVcrCc(int cc) {
    	loadVcr();
        mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(Vcr.class), "cc", "[Z");
    	loadImm(cc);
    	mv.visitInsn(Opcodes.BALOAD);
    }

    private void loadLocalVar(int localVar) {
        mv.visitVarInsn(Opcodes.ILOAD, localVar);
    }

    private void loadInstruction(Instruction insn) {
    	String classInternalName = instructionsInternalName;

    	if (insn == Common.UNK) {
    		// UNK instruction is in Common class, not Instructions
    		classInternalName = Type.getInternalName(Common.class);
    	}

    	mv.visitFieldInsn(Opcodes.GETSTATIC, classInternalName, insn.name().replace('.', '_').replace(' ', '_'), instructionDescriptor);
    }

    private void storePc() {
    	loadCpu();
    	loadImm(codeInstruction.getAddress());
        mv.visitFieldInsn(Opcodes.PUTFIELD, cpuInternalName, "pc", "I");
    }

    private void visitContinueToAddress(int returnAddress, boolean returnOnUnknownAddress) {
        //      if (x != returnAddress) {
        //          RuntimeContext.jump(x, returnAddress);
        //      }
        Label continueLabel = new Label();
        Label isReturnAddress = new Label();

        mv.visitInsn(Opcodes.DUP);
        loadImm(returnAddress);
        visitJump(Opcodes.IF_ICMPEQ, isReturnAddress);

        if (returnOnUnknownAddress) {
        	visitJump();
        } else {
	        loadImm(returnAddress);
	        mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "jump", "(II)V");
	        mv.visitJumpInsn(Opcodes.GOTO, continueLabel);
        }

        mv.visitLabel(isReturnAddress);
        mv.visitInsn(Opcodes.POP);
        mv.visitLabel(continueLabel);
    }

    public void visitJump() {
    	flushInstructionCount(true, false);
    	checkSync();

    	endMethod();
    	mv.visitInsn(Opcodes.IRETURN);
    }

    public void prepareCall(int address, int returnAddress, int returnRegister) {
    	preparedCallNativeCodeBlock = null;

    	// Do not call native block directly if we are profiling,
        // this would loose profiler information
        if (!Profiler.isProfilerEnabled()) {
        	// Is a native equivalent for this CodeBlock available?
        	preparedCallNativeCodeBlock = nativeCodeManager.getCompiledNativeCodeBlock(address);
        }

        if (preparedCallNativeCodeBlock == null) {
        	if (returnRegister != _zr) {
        		// Load the return register ($ra) with the return address
        		// before the delay slot is executed. The delay slot might overwrite it.
        		// For example:
        		//     addiu      $sp, $sp, -16
        		//     sw         $ra, 0($sp)
        		//     jal        0x0XXXXXXX
        		//     lw         $ra, 0($sp)
        		//     jr         $ra
        		//     addiu      $sp, $sp, 16
	        	prepareRegisterForStore(returnRegister);
	    		loadImm(returnAddress);
	            storeRegister(returnRegister);
        	}
        }
    }

    public void visitCall(int address, int returnAddress, int returnRegister, boolean returnRegisterModified, boolean returnOnUnknownAddress) {
    	flushInstructionCount(false, false);

        if (preparedCallNativeCodeBlock != null) {
    		if (preparedCallNativeCodeBlock.getNativeCodeSequenceClass().equals(Nop.class)) {
        		// NativeCodeSequence Nop means nothing to do!
    		} else {
    			// Call NativeCodeSequence
    			if (log.isDebugEnabled()) {
    				log.debug(String.format("Inlining call at 0x%08X to %s", getCodeInstruction().getAddress(), preparedCallNativeCodeBlock));
    			}

    			visitNativeCodeSequence(preparedCallNativeCodeBlock, address, null);
    		}
    	} else {
	        mv.visitMethodInsn(Opcodes.INVOKESTATIC, getClassName(address, instanceIndex), getStaticExecMethodName(), getStaticExecMethodDesc());
	        visitContinueToAddress(returnAddress, returnOnUnknownAddress);
    	}

        preparedCallNativeCodeBlock = null;
    }

    public void visitCall(int returnAddress, int returnRegister) {
    	flushInstructionCount(false, false);
        if (returnRegister != _zr) {
            storeRegister(returnRegister, returnAddress);
        }
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "call", "(I)I");
        visitContinueToAddress(returnAddress, false);
    }

    public void visitCall(int address, String methodName) {
    	flushInstructionCount(false, false);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, getClassName(address, instanceIndex), methodName, "()V");
    }

    public void visitIntepreterCall(int opcode, Instruction insn) {
    	loadInstruction(insn);
        loadProcessor();
        loadImm(opcode);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, instructionInternalName, "interpret", "(" + processorDescriptor + "I)V");
    }

    private boolean isFastSyscall(int code) {
		return fastSyscalls.contains(code);
	}

    /**
     * Generate the required Java code to load one parameter for
     * the syscall function from the CPU registers.
     *
     * The following code is generated based on the parameter type:
     * Processor: parameterValue = RuntimeContext.processor
     * int:       parameterValue = cpu.gpr[paramIndex++]
     * float:     parameterValue = cpu.fpr[paramFloatIndex++]
     * long:      parameterValue = (cpu.gpr[paramIndex++] & 0xFFFFFFFFL) + ((long) cpu.gpr[paramIndex++]) << 32)
     * boolean:   parameterValue = cpu.gpr[paramIndex++]
     * TPointer,
     * TPointer16,
     * TPointer32,
     * TPointer64,
     * TErrorPointer32:
     *            if (checkMemoryAccess()) {
     *                if (canBeNullParam && address == 0) {
     *                    goto addressGood;
     *                }
     *                if (RuntimeContext.checkMemoryPointer(address)) {
     *                    goto addressGood;
     *                }
     *                cpu.gpr[_v0] = SceKernelErrors.ERROR_INVALID_POINTER;
     *                pop all the parameters already prepared on the stack;
     *                goto afterSyscall;
     *                addressGood:
     *            }
     *            <parameterType> pointer = new <parameterType>(address);
     *            if (parameterType == TErrorPointer32.class) {
     *                parameterReader.setHasErrorPointer(true);
     *                localVar[LOCAL_ERROR_POINTER] = pointer;
     *            }
     *            parameterValue = pointer
     * HLEUidClass defined in annotation:
     *            <parameterType> uidObject = HLEUidObjectMapping.getObject("<parameterType>", uid);
     *            if (uidObject == null) {
     *                cpu.gpr[_v0] = errorValueOnNotFound;
     *                pop all the parameters already prepared on the stack;
     *                goto afterSyscall;
     *            }
     *            parameterValue = uidObject
     *
     * And then common for all the types:
     *            try {
     *                parameterValue = <module>.<methodToCheck>(parameterValue);
     *            } catch (SceKernelErrorException e) {
     *                goto catchSceKernelErrorException;
     *            }
     *            push parameterValue on stack
     *
     * @param parameterReader               the current parameter state
     * @param func                          the syscall function
     * @param parameterType                 the type of the parameter
     * @param afterSyscallLabel             the Label pointing after the call to the syscall function
     * @param catchSceKernelErrorException  the Label pointing to the SceKernelErrorException catch handler
     */
    private void loadParameter(CompilerParameterReader parameterReader, HLEModuleFunction func, Class<?> parameterType, Annotation[] parameterAnnotations, Label afterSyscallLabel, Label catchSceKernelErrorException) {
    	if (parameterType == Processor.class) {
    		loadProcessor();
    		parameterReader.incrementCurrentStackSize();
    	} else if (parameterType == CpuState.class) {
    		loadCpu();
    		parameterReader.incrementCurrentStackSize();
    	} else if (parameterType == int.class) {
    		parameterReader.loadNextInt();
    		parameterReader.incrementCurrentStackSize();
    	} else if (parameterType == float.class) {
    		parameterReader.loadNextFloat();
    		parameterReader.incrementCurrentStackSize();
    	} else if (parameterType == long.class) {
    		parameterReader.loadNextLong();
    		parameterReader.incrementCurrentStackSize(2);
    	} else if (parameterType == boolean.class) {
    		parameterReader.loadNextInt();
    		parameterReader.incrementCurrentStackSize();
    	} else if (parameterType == String.class) {
    		parameterReader.loadNextInt();

    		int maxLength = 16 * 1024;
    		for (Annotation parameterAnnotation : parameterAnnotations) {
    			if (parameterAnnotation instanceof StringInfo) {
    				StringInfo stringInfo = ((StringInfo)parameterAnnotation);
    				maxLength = stringInfo.maxLength();
    				break;
    			}
    		}
    		loadImm(maxLength);
   			mv.visitMethodInsn(
				Opcodes.INVOKESTATIC,
				runtimeContextInternalName,
				"readStringNZ", "(II)" + Type.getDescriptor(String.class)
   			);
    		parameterReader.incrementCurrentStackSize();
    	} else if (parameterType == PspString.class) {
    		parameterReader.loadNextInt();

    		int maxLength = 16 * 1024;
    		boolean canBeNull = false;
    		for (Annotation parameterAnnotation : parameterAnnotations) {
    			if (parameterAnnotation instanceof StringInfo) {
    				StringInfo stringInfo = ((StringInfo)parameterAnnotation);
    				maxLength = stringInfo.maxLength();
    			}
    			if (parameterAnnotation instanceof CanBeNull) {
    				canBeNull = true;
    			}
    		}
    		loadImm(maxLength);
    		loadImm(canBeNull);
   			mv.visitMethodInsn(
				Opcodes.INVOKESTATIC,
				runtimeContextInternalName,
				"readPspStringNZ", "(IIZ)" + Type.getDescriptor(PspString.class)
   			);
    		parameterReader.incrementCurrentStackSize();
    	} else if (parameterType == TPointer.class || parameterType == TPointer16.class || parameterType == TPointer32.class || parameterType == TPointer64.class || parameterType == TErrorPointer32.class) {
    		// if (checkMemoryAccess()) {
    		//     if (canBeNullParam && address == 0) {
    		//         goto addressGood;
    		//     }
    		//     if (RuntimeContext.checkMemoryPointer(address)) {
    		//         goto addressGood;
    		//     }
    		//     cpu.gpr[_v0] = SceKernelErrors.ERROR_INVALID_POINTER;
    		//     pop all the parameters already prepared on the stack;
    		//     goto afterSyscall;
    		//     addressGood:
    		// }
    		// <parameterType> pointer = new <parameterType>(address);
    		// if (parameterType == TErrorPointer32.class) {
    		//     parameterReader.setHasErrorPointer(true);
    		//     localVar[LOCAL_ERROR_POINTER] = pointer;
    		// }
    		mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(parameterType));
    		mv.visitInsn(Opcodes.DUP);
    		loadMemory();
    		parameterReader.loadNextInt();

    		boolean canBeNull = false;
    		for (Annotation parameterAnnotation : parameterAnnotations) {
    			if (parameterAnnotation instanceof CanBeNull) {
    				canBeNull = true;
    				break;
    			}
    		}

    		if (checkMemoryAccess() && afterSyscallLabel != null) {
    			Label addressGood = new Label();
    			if (canBeNull) {
        			mv.visitInsn(Opcodes.DUP);
    				mv.visitJumpInsn(Opcodes.IFEQ, addressGood);
    			}
    			mv.visitInsn(Opcodes.DUP);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "checkMemoryPointer", "(I)Z");
    			mv.visitJumpInsn(Opcodes.IFNE, addressGood);
    			storeRegister(_v0, SceKernelErrors.ERROR_INVALID_POINTER);
    			parameterReader.popAllStack(4);
    			mv.visitJumpInsn(Opcodes.GOTO, afterSyscallLabel);
    			mv.visitLabel(addressGood);
    		}
    		if (parameterType == TPointer16.class || parameterType == TPointer32.class || parameterType == TPointer64.class) {
    			loadImm(canBeNull);
    			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(parameterType), "<init>", "(" + memoryDescriptor + "IZ)V");
    		} else {
    			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(parameterType), "<init>", "(" + memoryDescriptor + "I)V");
    		}
    		if (parameterType == TErrorPointer32.class) {
    			parameterReader.setHasErrorPointer(true);
    			mv.visitInsn(Opcodes.DUP);
    			mv.visitVarInsn(Opcodes.ASTORE, LOCAL_ERROR_POINTER);
    		}
    		parameterReader.incrementCurrentStackSize();
    	} else if (pspAbstractMemoryMappedStructure.class.isAssignableFrom(parameterType)) {
    		parameterReader.loadNextInt();

    		boolean canBeNull = false;
    		for (Annotation parameterAnnotation : parameterAnnotations) {
    			if (parameterAnnotation instanceof CanBeNull) {
    				canBeNull = true;
    				break;
    			}
    		}

    		if (checkMemoryAccess() && afterSyscallLabel != null) {
    			Label addressGood = new Label();
    			if (canBeNull) {
        			mv.visitInsn(Opcodes.DUP);
    				mv.visitJumpInsn(Opcodes.IFEQ, addressGood);
    			}
    			mv.visitInsn(Opcodes.DUP);
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "checkMemoryPointer", "(I)Z");
    			mv.visitJumpInsn(Opcodes.IFNE, addressGood);
    			storeRegister(_v0, SceKernelErrors.ERROR_INVALID_POINTER);
    			parameterReader.popAllStack(1);
    			mv.visitJumpInsn(Opcodes.GOTO, afterSyscallLabel);
    			mv.visitLabel(addressGood);
    		}

    		mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(parameterType));
    		mv.visitInsn(Opcodes.DUP);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(parameterType), "<init>", "()V");
    		mv.visitInsn(Opcodes.DUP_X1);
    		mv.visitInsn(Opcodes.SWAP);
    		loadMemory();
    		mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(parameterType), "read", "(" + memoryDescriptor + "I)V");
    		parameterReader.incrementCurrentStackSize();
    	} else {
			HLEUidClass hleUidClass = parameterType.getAnnotation(HLEUidClass.class);
			if (hleUidClass != null) {
		   		int errorValueOnNotFound = hleUidClass.errorValueOnNotFound();
				
				// <parameterType> uidObject = HLEUidObjectMapping.getObject("<parameterType>", uid);
				// if (uidObject == null) {
				//     cpu.gpr[_v0] = errorValueOnNotFound;
	    		//     pop all the parameters already prepared on the stack;
	    		//     goto afterSyscall;
				// }
				mv.visitLdcInsn(parameterType.getName());
				// Load the UID
				parameterReader.loadNextInt();

				// Load the UID Object
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(HLEUidObjectMapping.class), "getObject", "(" + Type.getDescriptor(String.class) + "I)" + Type.getDescriptor(Object.class));
				if (afterSyscallLabel != null) {
					Label foundUid = new Label();
					mv.visitInsn(Opcodes.DUP);
					mv.visitJumpInsn(Opcodes.IFNONNULL, foundUid);
					storeRegister(_v0, errorValueOnNotFound);
					parameterReader.popAllStack(1);
					mv.visitJumpInsn(Opcodes.GOTO, afterSyscallLabel);
					mv.visitLabel(foundUid);
				}
				mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(parameterType));
	    		parameterReader.incrementCurrentStackSize();
			} else {
				log.error(String.format("Unsupported sycall parameter type '%s'", parameterType.getName()));
				Emulator.PauseEmuWithStatus(Emulator.EMU_STATUS_UNIMPLEMENTED);
			}
    	}

    	Method methodToCheck = null;
    	if (afterSyscallLabel != null) {
			for (Annotation parameterAnnotation : parameterAnnotations) {
				if (parameterAnnotation instanceof CheckArgument) {
					CheckArgument checkArgument = (CheckArgument) parameterAnnotation;
					try {
						methodToCheck = func.getHLEModule().getClass().getMethod(checkArgument.value(), parameterType);
					} catch (Exception e) {
						log.error(String.format("CheckArgument method '%s' not found in %s", checkArgument.value(), func.getModuleName()), e);
					}
					break;
				}
			}
    	}

    	if (methodToCheck != null) {
    		// try {
    		//     parameterValue = <module>.<methodToCheck>(parameterValue);
    		// } catch (SceKernelErrorException e) {
    		//     goto catchSceKernelErrorException;
    		// }
    		loadModule(func.getModuleName());
    		mv.visitInsn(Opcodes.SWAP);

    		Label tryStart = new Label();
        	Label tryEnd = new Label();
        	mv.visitTryCatchBlock(tryStart, tryEnd, catchSceKernelErrorException, Type.getInternalName(SceKernelErrorException.class));

        	mv.visitLabel(tryStart);
        	mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(methodToCheck.getDeclaringClass()), methodToCheck.getName(), "(" + Type.getDescriptor(parameterType) + ")" + Type.getDescriptor(parameterType));
        	mv.visitLabel(tryEnd);
    	}

    	parameterReader.incrementCurrentParameterIndex();
    }

    /**
     * Generate the required Java code to store the return value of
     * the syscall function into the CPU registers.
     *
     * The following code is generated depending on the return type:
     * void:         -
     * int:          cpu.gpr[_v0] = intValue
     * boolean:      cpu.gpr[_v0] = booleanValue
     * long:         cpu.gpr[_v0] = (int) (longValue & 0xFFFFFFFFL)
     *               cpu.gpr[_v1] = (int) (longValue >>> 32)
     * float:        cpu.fpr[_f0] = floatValue
     * HLEUidClass:  if (moduleMethodUidGenerator == "") {
     *                   cpu.gpr[_v0] = HLEUidObjectMapping.createUidForObject("<return type>", returnValue);
     *               } else {
     *                   int uid = <module>.<moduleMethodUidGenerator>();
     *                   cpu.gpr[_v0] = HLEUidObjectMapping.addObjectMap("<return type>", uid, returnValue);
     *               }
     *
     * @param func        the syscall function
     * @param returnType  the type of the return value
     */
    private void storeReturnValue(HLEModuleFunction func, Class<?> returnType) {
    	if (returnType == void.class) {
    		// Nothing to do
    	} else if (returnType == int.class) {
    		// cpu.gpr[_v0] = intValue
    		storeRegister(_v0);
    	} else if (returnType == boolean.class) {
    		// cpu.gpr[_v0] = booleanValue
    		storeRegister(_v0);
    	} else if (returnType == long.class) {
    		// cpu.gpr[_v0] = (int) (longValue & 0xFFFFFFFFL)
    		// cpu.gpr[_v1] = (int) (longValue >>> 32)
    		mv.visitInsn(Opcodes.DUP2);
    		mv.visitLdcInsn(0xFFFFFFFFL);
    		mv.visitInsn(Opcodes.LAND);
    		mv.visitInsn(Opcodes.L2I);
    		storeRegister(_v0);
    		loadImm(32);
    		mv.visitInsn(Opcodes.LSHR);
    		mv.visitInsn(Opcodes.L2I);
    		storeRegister(_v1);
    	} else if (returnType == float.class) {
    		// cpu.fpr[_f0] = floatValue
    		storeFRegister(_f0);
    	} else {
			HLEUidClass hleUidClass = returnType.getAnnotation(HLEUidClass.class);
			if (hleUidClass != null) {
				// if (moduleMethodUidGenerator == "") {
				//     cpu.gpr[_v0] = HLEUidObjectMapping.createUidForObject("<return type>", returnValue);
				// } else {
				//     int uid = <module>.<moduleMethodUidGenerator>();
				//     cpu.gpr[_v0] = HLEUidObjectMapping.addObjectMap("<return type>", uid, returnValue);
				// }
				if (hleUidClass.moduleMethodUidGenerator().length() <= 0) {
					// No UID generator method, use the default one
					mv.visitLdcInsn(returnType.getName());
					mv.visitInsn(Opcodes.SWAP);
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(HLEUidObjectMapping.class), "createUidForObject", "(" + Type.getDescriptor(String.class) + Type.getDescriptor(Object.class) + ")I");
					storeRegister(_v0);
				} else {
					mv.visitLdcInsn(returnType.getName());
					mv.visitInsn(Opcodes.SWAP);
					loadModule(func.getModuleName());
					mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(func.getHLEModuleMethod().getDeclaringClass()), hleUidClass.moduleMethodUidGenerator(), "()I");
					mv.visitInsn(Opcodes.SWAP);
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(HLEUidObjectMapping.class), "addObjectMap", "(" + Type.getDescriptor(String.class) + "I" + Type.getDescriptor(Object.class) + ")I");
					storeRegister(_v0);
				}
			} else {
				log.error(String.format("Unsupported sycall return value type '%s'", returnType.getName()));
			}
    	}
    }

    private void loadModuleLoggger(HLEModuleFunction func, boolean useDirectModuleLogger) {
    	if (useDirectModuleLogger) {
        	mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(func.getHLEModuleMethod().getDeclaringClass()), "log", Type.getDescriptor(Logger.class));
    	} else {
			mv.visitLdcInsn(func.getModuleName());
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Modules.class), "getLogger", "(" + Type.getDescriptor(String.class) + ")" + Type.getDescriptor(Logger.class));
    	}
    }

    private void logSyscall(HLEModuleFunction func, String logPrefix, String logCheckFunction, String logFunction, boolean useDirectModuleLogger) {
		// Modules.getLogger(func.getModuleName()).warn("Unimplemented...");
    	loadModuleLoggger(func, useDirectModuleLogger);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Logger.class), logCheckFunction, "()Z");
		Label loggingDisabled = new Label();
		mv.visitJumpInsn(Opcodes.IFEQ, loggingDisabled);

		loadModuleLoggger(func, useDirectModuleLogger);

		StringBuilder formatString = new StringBuilder();
		if (logPrefix != null) {
			formatString.append(logPrefix);
		}
		formatString.append(func.getFunctionName());
		ParameterInfo[] parameters = new ClassAnalyzer().getParameters(func.getFunctionName(), func.getHLEModuleMethod().getDeclaringClass());
		if (parameters != null) {
			// Log message:
			//    String.format(
			//       "Unimplemented <function name>
			//                 <parameterIntegerName>=0x%X,
			//                 <parameterBooleanName>=%b,
			//                 <parameterLongName>=0x%X,
			//                 <parameterFloatName>=%f,
			//                 <parameterOtherTypeName>=%s",
			//       new Object[] {
			//                 new Integer(parameterValueInteger),
			//                 new Boolean(parameterValueBoolean),
			//                 new Long(parameterValueLong),
			//                 new Float(parameterValueFloat),
			//                 parameterValueOtherTypes
			//       })
			loadImm(parameters.length);
			mv.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(Object.class));
            CompilerParameterReader parameterReader = new CompilerParameterReader(this);
            Annotation[][] paramsAnotations = func.getHLEModuleMethod().getParameterAnnotations();
            int paramIndex = 0;
            int objectArrayIndex = 0;
            for (ParameterInfo parameter : parameters) {
            	Class<?> parameterType = parameter.type;
            	CompilerTypeInformation typeInformation = compilerTypeManager.getCompilerTypeInformation(parameterType);

            	mv.visitInsn(Opcodes.DUP);
            	loadImm(objectArrayIndex);

        		formatString.append(paramIndex > 0 ? ", " : " ");
            	formatString.append(parameter.name);
            	formatString.append("=");
            	formatString.append(typeInformation.formatString);

            	if (typeInformation.boxingTypeInternalName != null) {
            		mv.visitTypeInsn(Opcodes.NEW, typeInformation.boxingTypeInternalName);
            		mv.visitInsn(Opcodes.DUP);
            	}

            	loadParameter(parameterReader, func, parameterType, paramsAnotations[paramIndex], null, null);

            	if (typeInformation.boxingTypeInternalName != null) {
            		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, typeInformation.boxingTypeInternalName, "<init>", typeInformation.boxingMethodDescriptor);
            	}
            	mv.visitInsn(Opcodes.AASTORE);

            	paramIndex++;
            	objectArrayIndex++;
            }
			mv.visitLdcInsn(formatString.toString());
			mv.visitInsn(Opcodes.SWAP);
        	mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(String.class), "format", "(" + Type.getDescriptor(String.class) + "[" + Type.getDescriptor(Object.class) + ")" + Type.getDescriptor(String.class));
		} else {
			mv.visitLdcInsn(formatString.toString());
		}
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Logger.class), logFunction, "(" + Type.getDescriptor(Object.class) + ")V");

		mv.visitLabel(loggingDisabled);
    }

    /**
     * Generate the required Java code to call a syscall function.
     * The code generated much match the Java behavior implemented in
     * jpcsp.HLE.modules.HLEModuleFunctionReflection
     *
     * The following code is generated:
     *     if (!fastSyscall) {
     *         RuntimeContext.preSyscall();
     *     }
     *     if (func.checkInsideInterrupt()) {
     *         if (IntrManager.getInstance.isInsideInterrupt()) {
     *             cpu.gpr[_v0] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
     *             goto afterSyscall;
     *         }
     *     }
     *     if (func.checkDispatchThreadEnabled()) {
     *         if (!Modules.ThreadManForUserModule.isDispatchThreadEnabled()) {
     *             cpu.gpr[_v0] = SceKernelErrors.ERROR_KERNEL_WAIT_CAN_NOT_WAIT;
     *             goto afterSyscall;
     *         }
     *     }
     *     if (func.isUnimplemented()) {
     *         Modules.getLogger(func.getModuleName()).warn("Unimplemented <function name> parameterName1=parameterValue1, parameterName2=parameterValue2, ...");
     *     }
     *     foreach parameter {
     *         loadParameter(parameter);
     *     }
     *     try {
     *         returnValue = <module name>.<function name>(...parameters...);
     *         storeReturnValue();
     *         if (parameterReader.hasErrorPointer()) {
     *             errorPointer.setValue(0);
     *         }
     *     } catch (SceKernelErrorException e) {
     *         errorCode = e.errorCode;
     *         if (Modules.log.isDebugEnabled()) {
     *             Modules.log.debug(String.format("<function name> return errorCode 0x%08X", errorCode));
     *         }
     *         if (parameterReader.hasErrorPointer()) {
     *             errorPointer.setValue(errorCode);
     *             cpu.gpr[_v0] = 0;
     *         } else {
     *             cpu.gpr[_v0] = errorCode;
     *         }
     *         reload cpu.gpr[_ra]; // an exception is always clearing the whole stack
     *     }
     *     afterSyscall:
     *     if (fastSyscall) {
     *         RuntimeContext.postSyscallFast();
     *     } else {
     *         RuntimeContext.postSyscall();
     *     }
     *
     * @param func         the syscall function
     * @param fastSyscall  true if this is a fast syscall (i.e. without context switching)
     *                     false if not (i.e. a syscall where context switching could happen)
     */
    private void visitSyscall(HLEModuleFunction func, boolean fastSyscall) {
    	// The compilation of a syscall requires more stack size than usual
    	maxStackSize = SYSCALL_MAX_STACK_SIZE;

    	if (!fastSyscall) {
    		mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "preSyscall", "()V");
    	}

    	Label afterSyscallLabel = new Label();

    	if (func.checkInsideInterrupt()) {
    		// if (IntrManager.getInstance().isInsideInterrupt()) {
    		//     if (Modules.log.isDebugEnabled()) {
    		//         Modules.log.debug("<function name> return errorCode 0x80020064 (ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT)");
    		//     }
    		//     cpu.gpr[_v0] = SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT;
    		//     goto afterSyscall
    		// }
    		mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(IntrManager.class), "getInstance", "()" + Type.getDescriptor(IntrManager.class));
    		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(IntrManager.class), "isInsideInterrupt", "()Z");
    		Label notInsideInterrupt = new Label();
    		mv.visitJumpInsn(Opcodes.IFEQ, notInsideInterrupt);

    		mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Modules.class), "log", Type.getDescriptor(Logger.class));
        	mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Logger.class), "isDebugEnabled", "()Z");
        	Label notDebug = new Label();
        	mv.visitJumpInsn(Opcodes.IFEQ, notDebug);
        	mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Modules.class), "log", Type.getDescriptor(Logger.class));
        	mv.visitLdcInsn(String.format("%s returning errorCode 0x%08X (ERROR_KERNEL_WAIT_CAN_NOT_WAIT)", func.getFunctionName(), SceKernelErrors.ERROR_KERNEL_WAIT_CAN_NOT_WAIT));
        	mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Logger.class), "debug", "(" + Type.getDescriptor(Object.class) + ")V");
        	mv.visitLabel(notDebug);

        	storeRegister(_v0, SceKernelErrors.ERROR_KERNEL_CANNOT_BE_CALLED_FROM_INTERRUPT);
    		mv.visitJumpInsn(Opcodes.GOTO, afterSyscallLabel);
    		mv.visitLabel(notInsideInterrupt);
    	}

    	if (func.checkDispatchThreadEnabled()) {
    		// if (!Modules.ThreadManForUserModule.isDispatchThreadEnabled() || !Interrupts.isInterruptsEnabled()) {
    		//     if (Modules.log.isDebugEnabled()) {
    		//         Modules.log.debug("<function name> return errorCode 0x800201A7 (ERROR_KERNEL_WAIT_CAN_NOT_WAIT)");
    		//     }
    		//     cpu.gpr[_v0] = SceKernelErrors.ERROR_KERNEL_WAIT_CAN_NOT_WAIT;
    		//     goto afterSyscall
    		// }
    		loadModule("ThreadManForUser");
    		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(ThreadManForUser.class), "isDispatchThreadEnabled", "()Z");
    		Label returnError = new Label();
    		mv.visitJumpInsn(Opcodes.IFEQ, returnError);
    		mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Interrupts.class), "isInterruptsEnabled", "()Z");
    		Label noError = new Label();
    		mv.visitJumpInsn(Opcodes.IFNE, noError);

    		mv.visitLabel(returnError);
        	mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Modules.class), "log", Type.getDescriptor(Logger.class));
        	mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Logger.class), "isDebugEnabled", "()Z");
        	Label notDebug = new Label();
        	mv.visitJumpInsn(Opcodes.IFEQ, notDebug);
        	mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Modules.class), "log", Type.getDescriptor(Logger.class));
        	mv.visitLdcInsn(String.format("%s returning errorCode 0x%08X (ERROR_KERNEL_WAIT_CAN_NOT_WAIT)", func.getFunctionName(), SceKernelErrors.ERROR_KERNEL_WAIT_CAN_NOT_WAIT));
        	mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Logger.class), "debug", "(" + Type.getDescriptor(Object.class) + ")V");
        	mv.visitLabel(notDebug);

        	storeRegister(_v0, SceKernelErrors.ERROR_KERNEL_WAIT_CAN_NOT_WAIT);
    		mv.visitJumpInsn(Opcodes.GOTO, afterSyscallLabel);
    		mv.visitLabel(noError);
    	}

    	if (func.isUnimplemented()) {
    		logSyscall(func, "Unimplemented ", "isInfoEnabled", "warn", false);
    	}

    	if (func.getLoggingLevel() != null) {
    		String logCheckFunction = "isInfoEnabled";
    		if ("trace".equals(func.getLoggingLevel())) {
    			logCheckFunction = "isTraceEnabled";
    		} else if ("debug".equals(func.getLoggingLevel())) {
    			logCheckFunction = "isDebugEnabled";
    		}
    		logSyscall(func, null, logCheckFunction, func.getLoggingLevel(), true);
    	}

    	if (func.hasStackUsage()) {
    		loadMemory();
    		loadRegister(_sp);
    		loadImm(func.getStackUsage());
    		mv.visitInsn(Opcodes.ISUB);
    		loadImm(0);
    		loadImm(func.getStackUsage());
        	mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, memoryInternalName, "memset", "(IBI)V");
    	}

    	// Collecting the parameters and calling the module function...
        CompilerParameterReader parameterReader = new CompilerParameterReader(this);

    	loadModule(func.getModuleName());
    	parameterReader.incrementCurrentStackSize();

    	Label tryStart = new Label();
    	Label tryEnd = new Label();
    	Label catchSceKernelErrorException = new Label();
    	mv.visitTryCatchBlock(tryStart, tryEnd, catchSceKernelErrorException, Type.getInternalName(SceKernelErrorException.class));

        Class<?>[] parameterTypes = func.getHLEModuleMethod().getParameterTypes();
        Class<?> returnType = func.getHLEModuleMethod().getReturnType();
        StringBuilder methodDescriptor = new StringBuilder();
        methodDescriptor.append("(");
        
        Annotation[][] paramsAnotations = func.getHLEModuleMethod().getParameterAnnotations();
        int paramIndex = 0;
        for (Class<?> parameterType : parameterTypes) {
        	methodDescriptor.append(Type.getDescriptor(parameterType));
        	loadParameter(parameterReader, func, parameterType, paramsAnotations[paramIndex], afterSyscallLabel, catchSceKernelErrorException);
        	paramIndex++;
        }
        methodDescriptor.append(")");
        methodDescriptor.append(Type.getDescriptor(returnType));

    	mv.visitLabel(tryStart);

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(func.getHLEModuleMethod().getDeclaringClass()), func.getFunctionName(), methodDescriptor.toString());

        storeReturnValue(func, returnType);

        if (parameterReader.hasErrorPointer()) {
        	// errorPointer.setValue(0);
            mv.visitVarInsn(Opcodes.ALOAD, LOCAL_ERROR_POINTER);
        	loadImm(0);
        	mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(TErrorPointer32.class), "setValue", "(I)V");
    	}

        mv.visitLabel(tryEnd);
        mv.visitJumpInsn(Opcodes.GOTO, afterSyscallLabel);

        // catch (SceKernelErrorException e) {
        //     errorCode = e.errorCode;
        //     if (Modules.log.isDebugEnabled()) {
        //         Modules.log.debug(String.format("<function name> return errorCode 0x%08X", errorCode));
        //     }
        //     if (hasErrorPointer()) {
        //         errorPointer.setValue(errorCode);
        //         cpu.gpr[_v0] = 0;
        //     } else {
        //         cpu.gpr[_v0] = errorCode;
        //     }
        // }
        mv.visitLabel(catchSceKernelErrorException);
        mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(SceKernelErrorException.class), "errorCode", "I");
        {
        	// if (Modules.log.isDebugEnabled()) {
        	//     Modules.log.debug(String.format("<function name> returning errorCode 0x%08X", new Object[1] { new Integer(errorCode) }));
        	// }
        	mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Modules.class), "log", Type.getDescriptor(Logger.class));
        	mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Logger.class), "isDebugEnabled", "()Z");
        	Label notDebug = new Label();
        	mv.visitJumpInsn(Opcodes.IFEQ, notDebug);
        	mv.visitInsn(Opcodes.DUP);
    		mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(Integer.class));
    		mv.visitInsn(Opcodes.DUP_X1);
    		mv.visitInsn(Opcodes.SWAP);
    		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Integer.class), "<init>", "(I)V");
    		loadImm(1);
    		mv.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(Object.class));
        	mv.visitInsn(Opcodes.DUP_X1);
        	mv.visitInsn(Opcodes.SWAP);
    		loadImm(0);
    		mv.visitInsn(Opcodes.SWAP);
    		mv.visitInsn(Opcodes.AASTORE);
        	mv.visitLdcInsn(String.format("%s returning errorCode 0x%%08X", func.getFunctionName()));
        	mv.visitInsn(Opcodes.SWAP);
        	mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(String.class), "format", "(" + Type.getDescriptor(String.class) + "[" + Type.getDescriptor(Object.class) + ")" + Type.getDescriptor(String.class));
        	mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(Modules.class), "log", Type.getDescriptor(Logger.class));
        	mv.visitInsn(Opcodes.SWAP);
        	mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Logger.class), "debug", "(" + Type.getDescriptor(Object.class) + ")V");
        	mv.visitLabel(notDebug);
        }
        if (parameterReader.hasErrorPointer()) {
        	// errorPointer.setValue(errorCode);
        	// cpu.gpr[_v0] = 0;
            mv.visitVarInsn(Opcodes.ALOAD, LOCAL_ERROR_POINTER);
        	mv.visitInsn(Opcodes.SWAP);
        	mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(TErrorPointer32.class), "setValue", "(I)V");
        	storeRegister(_v0, 0);
    	} else {
    		// cpu.gpr[_v0] = errorCode;
    		storeRegister(_v0);
    	}

        // Reload the $ra register, the stack is lost after an exception
        CodeInstruction previousInstruction = codeBlock.getCodeInstruction(codeInstruction.getAddress() - 4);
        if (previousInstruction != null && previousInstruction.getInsn() == Instructions.JR) {
        	int jumpRegister = (previousInstruction.getOpcode() >> 21) & 0x1F;
        	loadRegister(jumpRegister);
        }

    	mv.visitLabel(afterSyscallLabel);

        if (fastSyscall) {
    		mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "postSyscallFast", "()V");
        } else {
    		mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "postSyscall", "()V");
        }
    }

    /**
     * Generate the required Java code to perform a syscall.
     *
     * When the syscall function is an HLEModuleFunctionReflection,
     * generate the code for calling the module function directly, as
     * HLEModuleFunctionReflection.execute() would.
     *
     * Otherwise, generate the code for calling
     *     RuntimeContext.syscall()
     * or
     *     RuntimeContext.syscallFast()
     * 
     * @param opcode    opcode of the instruction
     */
    public void visitSyscall(int opcode) {
    	flushInstructionCount(false, false);

    	int code = (opcode >> 6) & 0x000FFFFF;

    	if (code == SyscallHandler.syscallUnmappedImport) {
    		storePc();
    	}

    	HLEModuleFunction func = HLEModuleManager.getInstance().getFunctionFromSyscallCode(code);
    	boolean fastSyscall = isFastSyscall(code);
    	if (func == null) {
	    	loadImm(code);
	    	if (fastSyscall) {
	    		mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "syscallFast", "(I)V");
	    	} else {
	    		mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "syscall", "(I)V");
	    	}
    	} else {
    		visitSyscall(func, fastSyscall);
    	}
    }

    public void startClass(ClassVisitor cv) {
    	if (RuntimeContext.enableLineNumbers) {
    		cv.visitSource(getCodeBlock().getClassName() + ".java", null);
    	}
    }

    public void startSequenceMethod() {
        if (storeCpuLocal) {
            mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "cpu", cpuDescriptor);
            mv.visitVarInsn(Opcodes.ASTORE, LOCAL_CPU);
        }

        if (storeMemoryIntLocal) {
			mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "memoryInt", "[I");
			mv.visitVarInsn(Opcodes.ASTORE, LOCAL_MEMORY_INT);
        }

        if (enableIntructionCounting) {
            currentInstructionCount = 0;
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, LOCAL_INSTRUCTION_COUNT);
        }

        startNonBranchingCodeSequence();
    }

    public void endSequenceMethod() {
    	flushInstructionCount(false, true);
        mv.visitInsn(Opcodes.RETURN);
    }

    public void checkSync() {
    	if (RuntimeContext.enableDaemonThreadSync) {
    		Label doNotWantSync = new Label();
            mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "wantSync", "Z");
            mv.visitJumpInsn(Opcodes.IFEQ, doNotWantSync);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, RuntimeContext.syncName, "()V");
            mv.visitLabel(doNotWantSync);
    	}
    }

    private void startInternalMethod() {
    	// if (e != null)
    	Label notReplacedLabel = new Label();
    	mv.visitFieldInsn(Opcodes.GETSTATIC, codeBlock.getClassName(), getReplaceFieldName(), executableDescriptor);
    	mv.visitJumpInsn(Opcodes.IFNULL, notReplacedLabel);
    	{
    		// return e.exec(returnAddress, alternativeReturnAddress, isJump);
        	mv.visitFieldInsn(Opcodes.GETSTATIC, codeBlock.getClassName(), getReplaceFieldName(), executableDescriptor);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, executableInternalName, getExecMethodName(), getExecMethodDesc());
            mv.visitInsn(Opcodes.IRETURN);
    	}
    	mv.visitLabel(notReplacedLabel);

    	if (Profiler.isProfilerEnabled()) {
    		loadImm(getCodeBlock().getStartAddress());
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, profilerInternalName, "addCall", "(I)V");
    	}

    	if (RuntimeContext.debugCodeBlockCalls) {
        	loadImm(getCodeBlock().getStartAddress());
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, RuntimeContext.debugCodeBlockStart, "(I)V");
        }
    }

    public void startMethod() {
    	startInternalMethod();
    	startSequenceMethod();
    }

    private void flushInstructionCount(boolean local, boolean last) {
        if (enableIntructionCounting) {
        	if (local) {
        		if (currentInstructionCount > 0) {
        			mv.visitIincInsn(LOCAL_INSTRUCTION_COUNT, currentInstructionCount);
        		}
        	} else {
		        mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "currentThread", sceKernalThreadInfoDescriptor);
		        mv.visitInsn(Opcodes.DUP);
		        mv.visitFieldInsn(Opcodes.GETFIELD, sceKernalThreadInfoInternalName, "runClocks", "J");
		        mv.visitVarInsn(Opcodes.ILOAD, LOCAL_INSTRUCTION_COUNT);
		        if (currentInstructionCount > 0) {
		        	loadImm(currentInstructionCount);
			        mv.visitInsn(Opcodes.IADD);
		        }
		        if (Profiler.isProfilerEnabled()) {
			        mv.visitInsn(Opcodes.DUP);
		    		loadImm(getCodeBlock().getStartAddress());
		            mv.visitMethodInsn(Opcodes.INVOKESTATIC, profilerInternalName, "addInstructionCount", "(II)V");
		        }
		        mv.visitInsn(Opcodes.I2L);
		        mv.visitInsn(Opcodes.LADD);
		        mv.visitFieldInsn(Opcodes.PUTFIELD, sceKernalThreadInfoInternalName, "runClocks", "J");
		        if (!last) {
		        	mv.visitInsn(Opcodes.ICONST_0);
		        	mv.visitVarInsn(Opcodes.ISTORE, LOCAL_INSTRUCTION_COUNT);
		        }
        	}
	        currentInstructionCount = 0;
        }
    }

    private void endInternalMethod() {
        if (RuntimeContext.debugCodeBlockCalls) {
            mv.visitInsn(Opcodes.DUP);
        	loadImm(getCodeBlock().getStartAddress());
            mv.visitInsn(Opcodes.SWAP);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, RuntimeContext.debugCodeBlockEnd, "(II)V");
        }
    }

    public void endMethod() {
    	endInternalMethod();
    	flushInstructionCount(false, true);
    }

    public void beforeInstruction(CodeInstruction codeInstruction) {
	    if (enableIntructionCounting) {
	    	if (codeInstruction.isBranchTarget()) {
	    		flushInstructionCount(true, false);
	    	}
	    	currentInstructionCount++;
	    }

	    if (RuntimeContext.enableLineNumbers) {
	    	// Force the instruction to emit a label
    		codeInstruction.getLabel(false);
    	}
    }

    private void startNonBranchingCodeSequence() {
    	vfpuPfxsState.reset();
    	vfpuPfxtState.reset();
    	vfpuPfxdState.reset();
    }

    private boolean isNonBranchingCodeSequence(CodeInstruction codeInstruction) {
        return !codeInstruction.isBranchTarget() && !codeInstruction.isBranching();
    }

    public void startInstruction(CodeInstruction codeInstruction) {
    	if (RuntimeContext.enableLineNumbers) {
    		int lineNumber = codeInstruction.getAddress() - getCodeBlock().getLowestAddress();
    		// Java line number is unsigned 16bits
    		if (lineNumber >= 0 && lineNumber <= 0xFFFF) {
    			mv.visitLineNumber(lineNumber, codeInstruction.getLabel());
    		}
    	}

    	if (RuntimeContext.debugCodeInstruction) {
        	loadImm(codeInstruction.getAddress());
        	loadImm(codeInstruction.getOpcode());
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, RuntimeContext.debugCodeInstructionName, "(II)V");
	    }

	    if (RuntimeContext.enableInstructionTypeCounting) {
	    	if (codeInstruction.getInsn() != null) {
		    	loadInstruction(codeInstruction.getInsn());
		    	loadImm(codeInstruction.getOpcode());
	            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, RuntimeContext.instructionTypeCount, "(" + instructionDescriptor + "I)V");
	    	}
	    }

	    if (RuntimeContext.enableDebugger) {
	    	loadImm(codeInstruction.getAddress());
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, RuntimeContext.debuggerName, "(I)V");
	    }

	    if (RuntimeContext.checkCodeModification && !(codeInstruction instanceof NativeCodeInstruction)) {
	    	// Generate the following sequence:
	    	//
	    	//     if (memory.read32(pc) != opcode) {
	    	//         RuntimeContext.onCodeModification(pc, opcode);
	    	//     }
	    	//
	    	loadMemory();
	    	loadImm(codeInstruction.getAddress());
	        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, memoryInternalName, "read32", "(I)I");
	        loadImm(codeInstruction.getOpcode());
	        Label codeUnchanged = new Label();
	        mv.visitJumpInsn(Opcodes.IF_ICMPEQ, codeUnchanged);

	        loadImm(codeInstruction.getAddress());
	        loadImm(codeInstruction.getOpcode());
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "onCodeModification", "(II)V");

	        mv.visitLabel(codeUnchanged);
	    }

	    if (!isNonBranchingCodeSequence(codeInstruction)) {
	    	startNonBranchingCodeSequence();
	    }

	    // This instructions consumes the PFXT prefix but does not use it.
	    if (codeInstruction.hasFlags(Instruction.FLAG_CONSUMES_VFPU_PFXT)) {
            disablePfxSrc(vfpuPfxtState);
        }
    }

    private void disablePfxSrc(VfpuPfxSrcState pfxSrcState) {
        pfxSrcState.pfxSrc.enabled = false;
        pfxSrcState.setKnown(true);
    }

    private void disablePfxDst(VfpuPfxDstState pfxDstState) {
        pfxDstState.pfxDst.enabled = false;
        pfxDstState.setKnown(true);
    }

    public void endInstruction() {
        if (codeInstruction != null) {
            if (codeInstruction.hasFlags(Instruction.FLAG_USE_VFPU_PFXS)) {
                disablePfxSrc(vfpuPfxsState);
            }

            if (codeInstruction.hasFlags(Instruction.FLAG_USE_VFPU_PFXT)) {
                disablePfxSrc(vfpuPfxtState);
            }

            if (codeInstruction.hasFlags(Instruction.FLAG_USE_VFPU_PFXD)) {
                disablePfxDst(vfpuPfxdState);
            }
        }
    }

    public void visitJump(int opcode, CodeInstruction target) {
    	// Back branch? i.e probably a loop
        if (target.getAddress() <= getCodeInstruction().getAddress()) {
        	checkSync();

        	if (Profiler.isProfilerEnabled()) {
        		loadImm(getCodeInstruction().getAddress());
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, profilerInternalName, "addBackBranch", "(I)V");
        	}
        }
        visitJump(opcode, target.getLabel());
    }

    public void visitJump(int opcode, Label label) {
    	flushInstructionCount(true, false);
        mv.visitJumpInsn(opcode, label);
    }

    public void visitJump(int opcode, int address) {
        flushInstructionCount(true, false);
        if (opcode == Opcodes.GOTO) {
            loadImm(address);
            visitJump();
        } else {
            log.error("Not implemented: branching to an unknown address");
            if (opcode == Opcodes.IF_ACMPEQ ||
                opcode == Opcodes.IF_ACMPNE ||
                opcode == Opcodes.IF_ICMPEQ ||
                opcode == Opcodes.IF_ICMPNE ||
                opcode == Opcodes.IF_ICMPGE ||
                opcode == Opcodes.IF_ICMPGT ||
                opcode == Opcodes.IF_ICMPLE ||
                opcode == Opcodes.IF_ICMPLT) {
                // 2 Arguments to POP
                mv.visitInsn(Opcodes.POP);
            }
            mv.visitInsn(Opcodes.POP);
            loadImm(address);
            visitJump();
        }
    }

    public static String getClassName(int address, int instanceIndex) {
    	return "_S1_" + instanceIndex + "_" + Integer.toHexString(address).toUpperCase();
    }

    public static int getClassAddress(String name) {
    	String hexAddress = name.substring(name.lastIndexOf("_") + 1);

        return Integer.parseInt(hexAddress, 16);
    }

    public static int getClassInstanceIndex(String name) {
    	int startIndex = name.indexOf("_", 1);
    	int endIndex = name.lastIndexOf("_");
    	String instanceIndex = name.substring(startIndex + 1, endIndex);

    	return Integer.parseInt(instanceIndex);
    }

    public String getExecMethodName() {
        return "exec";
    }

    public String getExecMethodDesc() {
        return "()I";
    }

    public String getReplaceFieldName() {
    	return "e";
    }

    public String getReplaceMethodName() {
    	return "setExecutable";
    }

    public String getReplaceMethodDesc() {
    	return "(" + executableDescriptor + ")V";
    }

    public String getGetMethodName() {
    	return "getExecutable";
    }

    public String getGetMethodDesc() {
    	return "()" + executableDescriptor;
    }

    public String getStaticExecMethodName() {
        return "s";
    }

    public String getStaticExecMethodDesc() {
        return "()I";
    }

    public boolean isAutomaticMaxLocals() {
        return false;
    }

    public int getMaxLocals() {
        return LOCAL_MAX;
    }

    public boolean isAutomaticMaxStack() {
        return false;
    }

    public int getMaxStack() {
        return maxStackSize;
    }

    public void visitPauseEmuWithStatus(MethodVisitor mv, int status) {
    	loadImm(status);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, RuntimeContext.pauseEmuWithStatus, "(I)V");
    }

    public void visitLogInfo(MethodVisitor mv, String message) {
    	mv.visitLdcInsn(message);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, RuntimeContext.logInfo, "(" + stringDescriptor + ")V");
    }

	@Override
	public MethodVisitor getMethodVisitor() {
		return mv;
	}

	public void setMethodVisitor(MethodVisitor mv) {
		this.mv = mv;
	}

	@Override
	public CodeInstruction getCodeInstruction() {
		return codeInstruction;
	}

	@Override
	public CodeInstruction getCodeInstruction(int address) {
		return getCodeBlock().getCodeInstruction(address);
	}

	public void setCodeInstruction(CodeInstruction codeInstruction) {
		this.codeInstruction = codeInstruction;
	}

	@Override
	public int getSaValue() {
		return codeInstruction.getSaValue();
    }

	@Override
	public int getRsRegisterIndex() {
		return codeInstruction.getRsRegisterIndex();
    }

	@Override
    public int getRtRegisterIndex() {
		return codeInstruction.getRtRegisterIndex();
    }

	@Override
    public int getRdRegisterIndex() {
		return codeInstruction.getRdRegisterIndex();
    }

	@Override
    public void loadRs() {
        loadRegister(getRsRegisterIndex());
    }

	@Override
    public void loadRt() {
        loadRegister(getRtRegisterIndex());
    }

	@Override
    public void loadRd() {
        loadRegister(getRdRegisterIndex());
    }

	@Override
    public void loadSaValue() {
        loadImm(getSaValue());
    }

    public void loadRegisterIndex(int registerIndex) {
    	loadImm(registerIndex);
    }

    public void loadRsIndex() {
        loadRegisterIndex(getRsRegisterIndex());
    }

    public void loadRtIndex() {
        loadRegisterIndex(getRtRegisterIndex());
    }

    public void loadRdIndex() {
        loadRegisterIndex(getRdRegisterIndex());
    }

	@Override
    public int getImm16(boolean signedImm) {
		return codeInstruction.getImm16(signedImm);
    }

	@Override
	public int getImm14(boolean signedImm) {
		return codeInstruction.getImm14(signedImm);
	}

	@Override
    public void loadImm16(boolean signedImm) {
    	loadImm(getImm16(signedImm));
    }

	@Override
    public void loadImm(int imm) {
		switch (imm) {
			case -1: mv.visitInsn(Opcodes.ICONST_M1); break;
			case  0: mv.visitInsn(Opcodes.ICONST_0);  break;
			case  1: mv.visitInsn(Opcodes.ICONST_1);  break;
			case  2: mv.visitInsn(Opcodes.ICONST_2);  break;
			case  3: mv.visitInsn(Opcodes.ICONST_3);  break;
			case  4: mv.visitInsn(Opcodes.ICONST_4);  break;
			case  5: mv.visitInsn(Opcodes.ICONST_5);  break;
			default:
				if (Byte.MIN_VALUE <= imm && imm < Byte.MAX_VALUE) {
					mv.visitIntInsn(Opcodes.BIPUSH, imm);
				} else if (Short.MIN_VALUE <= imm && imm < Short.MAX_VALUE) {
					mv.visitIntInsn(Opcodes.SIPUSH, imm);
				} else {
					mv.visitLdcInsn(new Integer(imm));
				}
				break;
		}
    }

	public void loadImm(boolean imm) {
		mv.visitInsn(imm ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
	}

	public void loadPspNaNInt() {
		mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(VfpuState.class), "pspNaNint", "I");
	}

	@Override
	public void compileInterpreterInstruction() {
		visitIntepreterCall(codeInstruction.getOpcode(), codeInstruction.getInsn());
	}

	@Override
	public void compileRTRSIMM(String method, boolean signedImm) {
		loadCpu();
		loadRtIndex();
		loadRsIndex();
		loadImm16(signedImm);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, cpuInternalName, method, "(III)V");
	}

	@Override
	public void compileRDRT(String method) {
		loadCpu();
		loadRdIndex();
		loadRtIndex();
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, cpuInternalName, method, "(II)V");
	}

	@Override
	public void storeRd() {
		storeRegister(getRdRegisterIndex());
	}

	@Override
	public void storeRd(int constantValue) {
		storeRegister(getRdRegisterIndex(), constantValue);
	}

	@Override
	public void storeRt() {
		storeRegister(getRtRegisterIndex());
	}

	@Override
	public void storeRt(int constantValue) {
		storeRegister(getRtRegisterIndex(), constantValue);
	}

	@Override
	public boolean isRdRegister0() {
		return getRdRegisterIndex() == _zr;
	}

	@Override
	public boolean isRtRegister0() {
		return getRtRegisterIndex() == _zr;
	}

	@Override
	public boolean isRsRegister0() {
		return getRsRegisterIndex() == _zr;
	}

	@Override
	public void prepareRdForStore() {
		prepareRegisterForStore(getRdRegisterIndex());
	}

	@Override
	public void prepareRtForStore() {
		prepareRegisterForStore(getRtRegisterIndex());
	}

	private void loadMemoryInt() {
		if (storeMemoryIntLocal) {
			mv.visitVarInsn(Opcodes.ALOAD, LOCAL_MEMORY_INT);
		} else {
			mv.visitFieldInsn(Opcodes.GETSTATIC, runtimeContextInternalName, "memoryInt", "[I");
		}
	}

	@Override
	public void memRead32(int registerIndex, int offset) {
		if (RuntimeContext.memoryInt == null) {
			loadMemory();
		} else {
			loadMemoryInt();
		}

		prepareMemIndex(registerIndex, offset, true, 32);

		if (RuntimeContext.memoryInt == null) {
	        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, memoryInternalName, "read32", "(I)I");
		} else {
			mv.visitInsn(Opcodes.IALOAD);
		}
	}

	@Override
	public void memRead16(int registerIndex, int offset) {
		if (RuntimeContext.memoryInt == null) {
			loadMemory();
		} else {
			loadMemoryInt();
		}

		loadRegister(registerIndex);
		if (offset != 0) {
			loadImm(offset);
			mv.visitInsn(Opcodes.IADD);
		}

		if (RuntimeContext.debugMemoryRead) {
			mv.visitInsn(Opcodes.DUP);
			loadImm(0);
            loadImm(codeInstruction.getAddress());
			loadImm(1);
			loadImm(16);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "debugMemoryReadWrite", "(IIIZI)V");
		}

		if (RuntimeContext.memoryInt == null) {
	        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, memoryInternalName, "read16", "(I)I");
		} else {
            if (checkMemoryAccess()) {
                loadImm(codeInstruction.getAddress());
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "checkMemoryRead16", "(II)I");
                loadImm(1);
                mv.visitInsn(Opcodes.IUSHR);
            } else {
    			// memoryInt[(address & 0x3FFFFFFF) / 4] == memoryInt[(address << 2) >>> 4]
    			loadImm(2);
    			mv.visitInsn(Opcodes.ISHL);
    			loadImm(3);
    			mv.visitInsn(Opcodes.IUSHR);
            }
			mv.visitInsn(Opcodes.DUP);
			loadImm(1);
			mv.visitInsn(Opcodes.IAND);
			loadImm(4);
			mv.visitInsn(Opcodes.ISHL);
			storeTmp1();
			loadImm(1);
			mv.visitInsn(Opcodes.IUSHR);
			mv.visitInsn(Opcodes.IALOAD);
			loadTmp1();
			mv.visitInsn(Opcodes.IUSHR);
			loadImm(0xFFFF);
			mv.visitInsn(Opcodes.IAND);
		}
	}

	@Override
	public void memRead8(int registerIndex, int offset) {
		if (RuntimeContext.memoryInt == null) {
			loadMemory();
		} else {
			loadMemoryInt();
		}

		loadRegister(registerIndex);
		if (offset != 0) {
			loadImm(offset);
			mv.visitInsn(Opcodes.IADD);
		}

		if (RuntimeContext.debugMemoryRead) {
			mv.visitInsn(Opcodes.DUP);
			loadImm(0);
            loadImm(codeInstruction.getAddress());
			loadImm(1);
			loadImm(8);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "debugMemoryReadWrite", "(IIIZI)V");
		}

		if (RuntimeContext.memoryInt == null) {
	        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, memoryInternalName, "read8", "(I)I");
		} else {
            if (checkMemoryAccess()) {
                loadImm(codeInstruction.getAddress());
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "checkMemoryRead8", "(II)I");
            } else {
    			// memoryInt[(address & 0x3FFFFFFF) / 4] == memoryInt[(address << 2) >>> 4]
    			loadImm(2);
    			mv.visitInsn(Opcodes.ISHL);
    			loadImm(2);
    			mv.visitInsn(Opcodes.IUSHR);
            }
			mv.visitInsn(Opcodes.DUP);
			loadImm(3);
			mv.visitInsn(Opcodes.IAND);
			loadImm(3);
			mv.visitInsn(Opcodes.ISHL);
			storeTmp1();
			loadImm(2);
			mv.visitInsn(Opcodes.IUSHR);
			mv.visitInsn(Opcodes.IALOAD);
			loadTmp1();
			mv.visitInsn(Opcodes.IUSHR);
			loadImm(0xFF);
			mv.visitInsn(Opcodes.IAND);
		}
	}

	private void prepareMemIndex(int registerIndex, int offset, boolean isRead, int width) {
		loadRegister(registerIndex);
		if (offset != 0) {
			loadImm(offset);
			mv.visitInsn(Opcodes.IADD);
		}

		if (RuntimeContext.debugMemoryRead && isRead) {
			if (!RuntimeContext.debugMemoryReadWriteNoSP || registerIndex != _sp) {
				mv.visitInsn(Opcodes.DUP);
				loadImm(0);
			    loadImm(codeInstruction.getAddress());
				loadImm(isRead);
				loadImm(width);
			    mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "debugMemoryReadWrite", "(IIIZI)V");
			}
		}

		if (RuntimeContext.memoryInt != null) {
			if (registerIndex == _sp) {
				// No need to check for a valid memory access when referencing the $sp register
				loadImm(2);
    			mv.visitInsn(Opcodes.IUSHR);
			} else if (checkMemoryAccess()) {
	            loadImm(codeInstruction.getAddress());
	            String checkMethodName = String.format("checkMemory%s%d", isRead ? "Read" : "Write", width);
	            mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, checkMethodName, "(II)I");
                loadImm(2);
                mv.visitInsn(Opcodes.IUSHR);
	        } else {
    			// memoryInt[(address & 0x3FFFFFFF) / 4] == memoryInt[(address << 2) >>> 4]
    			loadImm(2);
    			mv.visitInsn(Opcodes.ISHL);
    			loadImm(4);
    			mv.visitInsn(Opcodes.IUSHR);
	        }
		}
	}

	@Override
	public void prepareMemWrite32(int registerIndex, int offset) {
		if (RuntimeContext.memoryInt == null) {
			loadMemory();
		} else {
			loadMemoryInt();
		}

		prepareMemIndex(registerIndex, offset, false, 32);

		memWritePrepared = true;
	}

	@Override
	public void memWrite32(int registerIndex, int offset) {
		if (!memWritePrepared) {
			if (RuntimeContext.memoryInt == null) {
				loadMemory();
			} else {
				loadMemoryInt();
			}
			mv.visitInsn(Opcodes.SWAP);

			loadRegister(registerIndex);
			if (offset != 0) {
				loadImm(offset);
				mv.visitInsn(Opcodes.IADD);
			}
            if (checkMemoryAccess()) {
                loadImm(codeInstruction.getAddress());
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "checkMemoryWrite32", "(II)I");
            }
			mv.visitInsn(Opcodes.SWAP);
		}

		if (RuntimeContext.debugMemoryWrite) {
			if (!RuntimeContext.debugMemoryReadWriteNoSP || registerIndex != _sp) {
				mv.visitInsn(Opcodes.DUP2);
				mv.visitInsn(Opcodes.SWAP);
				loadImm(2);
				mv.visitInsn(Opcodes.ISHL);
				mv.visitInsn(Opcodes.SWAP);
			    loadImm(codeInstruction.getAddress());
				loadImm(0);
				loadImm(32);
			    mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "debugMemoryReadWrite", "(IIIZI)V");
			}
		}

		if (RuntimeContext.memoryInt == null) {
	        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, memoryInternalName, "write32", "(II)V");
		} else {
			mv.visitInsn(Opcodes.IASTORE);
		}

		memWritePrepared = false;
	}

	@Override
	public void prepareMemWrite16(int registerIndex, int offset) {
		if (RuntimeContext.memoryInt == null) {
			loadMemory();
		} else {
			loadMemoryInt();
		}

		loadRegister(registerIndex);
		if (offset != 0) {
			loadImm(offset);
			mv.visitInsn(Opcodes.IADD);
		}

		if (RuntimeContext.memoryInt != null) {
			if (checkMemoryAccess()) {
				loadImm(codeInstruction.getAddress());
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "checkMemoryWrite16", "(II)I");
			}
		}

		memWritePrepared = true;
	}

	@Override
	public void memWrite16(int registerIndex, int offset) {
		if (!memWritePrepared) {
			if (RuntimeContext.memoryInt == null) {
				loadMemory();
			} else {
				loadMemoryInt();
			}
			mv.visitInsn(Opcodes.SWAP);

			loadRegister(registerIndex);
			if (offset != 0) {
				loadImm(offset);
				mv.visitInsn(Opcodes.IADD);
			}

			if (RuntimeContext.memoryInt != null) {
				if (checkMemoryAccess()) {
					loadImm(codeInstruction.getAddress());
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "checkMemoryWrite16", "(II)I");
				}
			}
			mv.visitInsn(Opcodes.SWAP);
		}

		if (RuntimeContext.memoryInt == null) {
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, memoryInternalName, "write16", "(IS)V");
		} else {
			// tmp2 = value & 0xFFFF;
			// tmp1 = (address & 2) << 3;
			// memoryInt[address >> 2] = (memoryInt[address >> 2] & ((0xFFFF << tmp1) ^ 0xFFFFFFFF)) | (tmp2 << tmp1);
			loadImm(0xFFFF);
			mv.visitInsn(Opcodes.IAND);
			storeTmp2();
			mv.visitInsn(Opcodes.DUP);
			loadImm(2);
			mv.visitInsn(Opcodes.IAND);
			loadImm(3);
			mv.visitInsn(Opcodes.ISHL);
			storeTmp1();
			if (checkMemoryAccess()) {
				loadImm(2);
				mv.visitInsn(Opcodes.ISHR);
			} else {
				loadImm(2);
				mv.visitInsn(Opcodes.ISHL);
				loadImm(4);
				mv.visitInsn(Opcodes.IUSHR);
			}
			mv.visitInsn(Opcodes.DUP2);
			mv.visitInsn(Opcodes.IALOAD);
			loadImm(0xFFFF);
			loadTmp1();
			mv.visitInsn(Opcodes.ISHL);
			loadImm(-1);
			mv.visitInsn(Opcodes.IXOR);
			mv.visitInsn(Opcodes.IAND);
			loadTmp2();
			loadTmp1();
			mv.visitInsn(Opcodes.ISHL);
			mv.visitInsn(Opcodes.IOR);
			mv.visitInsn(Opcodes.IASTORE);
		}

		memWritePrepared = false;
	}

	@Override
	public void prepareMemWrite8(int registerIndex, int offset) {
		if (RuntimeContext.memoryInt == null) {
			loadMemory();
		} else {
			loadMemoryInt();
		}

		loadRegister(registerIndex);
		if (offset != 0) {
			loadImm(offset);
			mv.visitInsn(Opcodes.IADD);
		}

		if (RuntimeContext.memoryInt != null) {
			if (checkMemoryAccess()) {
				loadImm(codeInstruction.getAddress());
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "checkMemoryWrite8", "(II)I");
			}
		}

		memWritePrepared = true;
	}

	@Override
	public void memWrite8(int registerIndex, int offset) {
		if (!memWritePrepared) {
			if (RuntimeContext.memoryInt == null) {
				loadMemory();
			} else {
				loadMemoryInt();
			}
			mv.visitInsn(Opcodes.SWAP);

			loadRegister(registerIndex);
			if (offset != 0) {
				loadImm(offset);
				mv.visitInsn(Opcodes.IADD);
			}

			if (RuntimeContext.memoryInt != null) {
				if (checkMemoryAccess()) {
					loadImm(codeInstruction.getAddress());
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "checkMemoryWrite8", "(II)I");
				}
			}
			mv.visitInsn(Opcodes.SWAP);
		}

		if (RuntimeContext.memoryInt == null) {
	        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, memoryInternalName, "write8", "(IB)V");
		} else {
			// tmp2 = value & 0xFF;
			// tmp1 = (address & 3) << 3;
			// memoryInt[address >> 2] = (memoryInt[address >> 2] & ((0xFF << tmp1) ^ 0xFFFFFFFF)) | (tmp2 << tmp1);
			loadImm(0xFF);
			mv.visitInsn(Opcodes.IAND);
			storeTmp2();
			mv.visitInsn(Opcodes.DUP);
			loadImm(3);
			mv.visitInsn(Opcodes.IAND);
			loadImm(3);
			mv.visitInsn(Opcodes.ISHL);
			storeTmp1();
			if (checkMemoryAccess()) {
				loadImm(2);
				mv.visitInsn(Opcodes.ISHR);
			} else {
				loadImm(2);
				mv.visitInsn(Opcodes.ISHL);
				loadImm(4);
				mv.visitInsn(Opcodes.IUSHR);
			}
			mv.visitInsn(Opcodes.DUP2);
			mv.visitInsn(Opcodes.IALOAD);
			loadImm(0xFF);
			loadTmp1();
			mv.visitInsn(Opcodes.ISHL);
			loadImm(-1);
			mv.visitInsn(Opcodes.IXOR);
			mv.visitInsn(Opcodes.IAND);
			loadTmp2();
			loadTmp1();
			mv.visitInsn(Opcodes.ISHL);
			mv.visitInsn(Opcodes.IOR);
			mv.visitInsn(Opcodes.IASTORE);
		}

		memWritePrepared = false;
	}

	@Override
	public void memWriteZero8(int registerIndex, int offset) {
		if (RuntimeContext.memoryInt == null) {
			loadMemory();
		} else {
			loadMemoryInt();
		}

		loadRegister(registerIndex);
		if (offset != 0) {
			loadImm(offset);
			mv.visitInsn(Opcodes.IADD);
		}

		if (RuntimeContext.memoryInt != null) {
			if (checkMemoryAccess()) {
				loadImm(codeInstruction.getAddress());
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "checkMemoryWrite8", "(II)I");
			}
		}

		if (RuntimeContext.memoryInt == null) {
			loadImm(0);
	        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, memoryInternalName, "write8", "(IB)V");
		} else {
			// tmp1 = (address & 3) << 3;
			// memoryInt[address >> 2] = (memoryInt[address >> 2] & ((0xFF << tmp1) ^ 0xFFFFFFFF));
			mv.visitInsn(Opcodes.DUP);
			loadImm(3);
			mv.visitInsn(Opcodes.IAND);
			loadImm(3);
			mv.visitInsn(Opcodes.ISHL);
			storeTmp1();
			if (checkMemoryAccess()) {
				loadImm(2);
				mv.visitInsn(Opcodes.ISHR);
			} else {
				loadImm(2);
				mv.visitInsn(Opcodes.ISHL);
				loadImm(4);
				mv.visitInsn(Opcodes.IUSHR);
			}
			mv.visitInsn(Opcodes.DUP2);
			mv.visitInsn(Opcodes.IALOAD);
			loadImm(0xFF);
			loadTmp1();
			mv.visitInsn(Opcodes.ISHL);
			loadImm(-1);
			mv.visitInsn(Opcodes.IXOR);
			mv.visitInsn(Opcodes.IAND);
			mv.visitInsn(Opcodes.IASTORE);
		}
	}

	@Override
	public void compileSyscall() {
		visitSyscall(codeInstruction.getOpcode());
	}

	@Override
	public void convertUnsignedIntToLong() {
		mv.visitInsn(Opcodes.I2L);
		mv.visitLdcInsn(0xFFFFFFFFL);
		mv.visitInsn(Opcodes.LAND);
	}

    public int getMethodMaxInstructions() {
        return methodMaxInstructions;
    }

    public void setMethodMaxInstructions(int methodMaxInstructions) {
        this.methodMaxInstructions = methodMaxInstructions;
    }

    private boolean checkMemoryAccess() {
        if (RuntimeContext.memoryInt == null) {
            return false;
        }

        if (RuntimeContext.memory instanceof SafeFastMemory) {
            return true;
        }

        return false;
    }

    public void compileDelaySlotAsBranchTarget(CodeInstruction codeInstruction) {
    	if (codeInstruction.getInsn() == Instructions.NOP) {
    		// NOP nothing to do
    		return;
    	}

    	boolean skipDelaySlotInstruction = true;
    	CodeInstruction previousInstruction = getCodeBlock().getCodeInstruction(codeInstruction.getAddress() - 4);
    	if (previousInstruction != null) {
    		if (Compiler.isEndBlockInsn(previousInstruction.getAddress(), previousInstruction.getOpcode(), previousInstruction.getInsn())) {
    			// The previous instruction was a J, JR or unconditional branch
    			// instruction, we do not need to skip the delay slot instruction
    			skipDelaySlotInstruction = false;
    		}
    	}

    	Label afterDelaySlot = null;
    	if (skipDelaySlotInstruction) {
    		afterDelaySlot = new Label();
    		mv.visitJumpInsn(Opcodes.GOTO, afterDelaySlot);
    	}
    	codeInstruction.compile(this, mv);
    	if (skipDelaySlotInstruction) {
    		mv.visitLabel(afterDelaySlot);
    	}
    }

    public void compileExecuteInterpreter(int startAddress) {
    	loadImm(startAddress);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, runtimeContextInternalName, "executeInterpreter", "(I)I");
        endMethod();
        mv.visitInsn(Opcodes.IRETURN);
    }

	private void visitNativeCodeSequence(NativeCodeSequence nativeCodeSequence, int address, NativeCodeInstruction nativeCodeInstruction) {
    	StringBuilder methodSignature = new StringBuilder("(");
    	int numberParameters = nativeCodeSequence.getNumberParameters();
    	for (int i = 0; i < numberParameters; i++) {
    		loadImm(nativeCodeSequence.getParameterValue(i, address));
    		methodSignature.append("I");
    	}
    	methodSignature.append(")V");
	    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(nativeCodeSequence.getNativeCodeSequenceClass()), nativeCodeSequence.getMethodName(), methodSignature.toString());

	    if (nativeCodeInstruction != null && nativeCodeInstruction.isBranching()) {
    		CodeInstruction targetInstruction = getCodeBlock().getCodeInstruction(nativeCodeInstruction.getBranchingTo());
    		if (targetInstruction != null) {
    			visitJump(Opcodes.GOTO, targetInstruction);
    		} else {
    			visitJump(Opcodes.GOTO, nativeCodeInstruction.getBranchingTo());
    		}
	    }
	}

	public void compileNativeCodeSequence(NativeCodeSequence nativeCodeSequence, NativeCodeInstruction nativeCodeInstruction) {
	    visitNativeCodeSequence(nativeCodeSequence, nativeCodeInstruction.getAddress(), nativeCodeInstruction);

	    if (nativeCodeSequence.isReturning()) {
	    	loadRegister(_ra);
	        endInternalMethod();
	        mv.visitInsn(Opcodes.IRETURN);
	    }

	    // Replacing the whole CodeBlock?
	    if (getCodeBlock().getLength() == nativeCodeSequence.getNumOpcodes() && !nativeCodeSequence.hasBranchInstruction()) {
        	nativeCodeManager.setCompiledNativeCodeBlock(getCodeBlock().getStartAddress(), nativeCodeSequence);

        	// Be more verbose when Debug enabled.
        	// Only log "Nop" native code sequence in debug.
        	if (log.isDebugEnabled() || nativeCodeSequence.getNativeCodeSequenceClass().equals(Nop.class)) {
        		if (log.isDebugEnabled()) {
        			log.debug(String.format("Replacing CodeBlock at 0x%08X (%08X-0x%08X, length %d) by %s", getCodeBlock().getStartAddress(), getCodeBlock().getLowestAddress(), codeBlock.getHighestAddress(), codeBlock.getLength(), nativeCodeSequence));
        		}
	        } else if (log.isInfoEnabled()) {
	        	log.info(String.format("Replacing CodeBlock at 0x%08X by Native Code '%s'", getCodeBlock().getStartAddress(), nativeCodeSequence.getName()));
	        }
	    } else {
        	// Be more verbose when Debug enabled
	    	int endAddress = getCodeInstruction().getAddress() + (nativeCodeSequence.getNumOpcodes() - 1) * 4;
	    	if (log.isDebugEnabled()) {
		    	log.debug(String.format("Replacing CodeSequence at 0x%08X-0x%08X by Native Code %s", getCodeInstruction().getAddress(), endAddress, nativeCodeSequence));
	        } else if (log.isInfoEnabled()) {
		    	log.info(String.format("Replacing CodeSequence at 0x%08X-0x%08X by Native Code '%s'", getCodeInstruction().getAddress(), endAddress, nativeCodeSequence.getName()));
	    	}
	    }
	}

	public int getNumberInstructionsToBeSkipped() {
		return numberInstructionsToBeSkipped;
	}

	public boolean isSkipDelaySlot() {
		return skipDelaySlot;
	}

	@Override
	public void skipInstructions(int numberInstructionsToBeSkipped, boolean skipDelaySlot) {
		this.numberInstructionsToBeSkipped = numberInstructionsToBeSkipped;
		this.skipDelaySlot = skipDelaySlot;
	}

	@Override
	public int getFdRegisterIndex() {
		return codeInstruction.getFdRegisterIndex();
	}

	@Override
	public int getFsRegisterIndex() {
		return codeInstruction.getFsRegisterIndex();
	}

	@Override
	public int getFtRegisterIndex() {
		return codeInstruction.getFtRegisterIndex();
	}

	@Override
	public void loadFd() {
		loadFRegister(getFdRegisterIndex());
	}

	@Override
	public void loadFs() {
		loadFRegister(getFsRegisterIndex());
	}

	@Override
	public void loadFt() {
		loadFRegister(getFtRegisterIndex());
	}

	@Override
	public void prepareFdForStore() {
		prepareFRegisterForStore(getFdRegisterIndex());
	}

	@Override
	public void prepareFtForStore() {
		prepareFRegisterForStore(getFtRegisterIndex());
	}

	@Override
	public void storeFd() {
		storeFRegister(getFdRegisterIndex());
	}

	@Override
	public void storeFt() {
		storeFRegister(getFtRegisterIndex());
	}

	@Override
	public void loadFCr() {
		loadFRegister(getCrValue());
	}

	@Override
	public void prepareFCrForStore() {
		prepareFRegisterForStore(getCrValue());
	}

	@Override
	public void prepareVcrCcForStore(int cc) {
    	if (preparedRegisterForStore < 0) {
        	loadVcr();
            mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(Vcr.class), "cc", "[Z");
    		loadImm(cc);
    		preparedRegisterForStore = cc;
    	}
	}

	@Override
	public void storeVcrCc(int cc) {
    	if (preparedRegisterForStore == cc) {
	        mv.visitInsn(Opcodes.BASTORE);
	        preparedRegisterForStore = -1;
    	} else {
        	loadVcr();
            mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(Vcr.class), "cc", "[Z");
	        mv.visitInsn(Opcodes.SWAP);
	        loadImm(cc);
	        mv.visitInsn(Opcodes.SWAP);
	        mv.visitInsn(Opcodes.BASTORE);
    	}
	}

	@Override
	public int getCrValue() {
		return codeInstruction.getCrValue();
	}

	@Override
	public void storeFCr() {
		storeFRegister(getCrValue());
	}

	@Override
	public int getVdRegisterIndex() {
		return codeInstruction.getVdRegisterIndex();
	}

	@Override
	public int getVsRegisterIndex() {
		return codeInstruction.getVsRegisterIndex();
	}

	@Override
	public int getVtRegisterIndex() {
		return codeInstruction.getVtRegisterIndex();
	}

	@Override
	public int getVsize() {
		return codeInstruction.getVsize();
	}

	@Override
	public void loadVs(int n) {
		loadVRegister(getVsize(), getVsRegisterIndex(), n, vfpuPfxsState, true);
	}

	@Override
	public void loadVsInt(int n) {
		loadVRegister(getVsize(), getVsRegisterIndex(), n, vfpuPfxsState, false);
	}

	@Override
	public void loadVt(int n) {
		loadVRegister(getVsize(), getVtRegisterIndex(), n, vfpuPfxtState, true);
	}

	@Override
	public void loadVtInt(int n) {
		loadVRegister(getVsize(), getVtRegisterIndex(), n, vfpuPfxtState, false);
	}

	@Override
	public void loadVt(int vsize, int n) {
		loadVRegister(vsize, getVtRegisterIndex(), n, vfpuPfxtState, true);
	}

	@Override
	public void loadVtInt(int vsize, int n) {
		loadVRegister(vsize, getVtRegisterIndex(), n, vfpuPfxtState, false);
	}

	@Override
	public void loadVt(int vsize, int vt, int n) {
		loadVRegister(vsize, vt, n, vfpuPfxtState, true);
	}

	@Override
	public void loadVtInt(int vsize, int vt, int n) {
		loadVRegister(vsize, vt, n, vfpuPfxtState, false);
	}

	@Override
	public void loadVd(int n) {
		loadVRegister(getVsize(), getVdRegisterIndex(), n, null, true);
	}

	@Override
	public void loadVdInt(int n) {
		loadVRegister(getVsize(), getVdRegisterIndex(), n, null, false);
	}

	@Override
	public void loadVd(int vsize, int n) {
		loadVRegister(vsize, getVdRegisterIndex(), n, null, true);
	}

	@Override
	public void loadVdInt(int vsize, int n) {
		loadVRegister(vsize, getVdRegisterIndex(), n, null, false);
	}

	@Override
	public void loadVd(int vsize, int vd, int n) {
		loadVRegister(vsize, vd, n, null, true);
	}

	@Override
	public void loadVdInt(int vsize, int vd, int n) {
		loadVRegister(vsize, vd, n, null, false);
	}

	@Override
	public void prepareVdForStore(int n) {
		prepareVdForStore(getVsize(), n);
	}

	@Override
	public void prepareVdForStore(int vsize, int n) {
		prepareVdForStore(vsize, getVdRegisterIndex(), n);
	}

	@Override
	public void prepareVdForStore(int vsize, int vd, int n) {
		if (pfxVdOverlap && n < vsize - 1) {
			// Do nothing, value will be store in tmp local variable
		} else {
			prepareVRegisterForStore(vsize, vd, n, vfpuPfxdState, true);
		}
	}

	@Override
	public void prepareVdForStoreInt(int n) {
		prepareVdForStoreInt(getVsize(), n);
	}

	@Override
	public void prepareVdForStoreInt(int vsize, int n) {
		prepareVdForStoreInt(vsize, getVdRegisterIndex(), n);
	}

	@Override
	public void prepareVdForStoreInt(int vsize, int vd, int n) {
		if (pfxVdOverlap && n < vsize - 1) {
			// Do nothing, value will be store in tmp local variable
		} else {
			prepareVRegisterForStore(vsize, vd, n, vfpuPfxdState, false);
		}
	}

	@Override
	public void prepareVtForStore(int n) {
		prepareVRegisterForStore(getVsize(), getVtRegisterIndex(), n, null, true);
	}

	@Override
	public void prepareVtForStore(int vsize, int n) {
		prepareVRegisterForStore(vsize, getVtRegisterIndex(), n, null, true);
	}

	@Override
	public void prepareVtForStoreInt(int n) {
		prepareVRegisterForStore(getVsize(), getVtRegisterIndex(), n, null, false);
	}

	@Override
	public void prepareVtForStoreInt(int vsize, int n) {
		prepareVRegisterForStore(vsize, getVtRegisterIndex(), n, null, false);
	}

	@Override
	public void storeVd(int n) {
		storeVd(getVsize(), n);
	}

	@Override
	public void storeVdInt(int n) {
		storeVdInt(getVsize(), n);
	}

	@Override
	public void storeVd(int vsize, int n) {
		storeVd(vsize, getVdRegisterIndex(), n);
	}

	@Override
	public void storeVdInt(int vsize, int n) {
		storeVdInt(vsize, getVdRegisterIndex(), n);
	}

	@Override
	public void storeVd(int vsize, int vd, int n) {
		if (pfxVdOverlap && n < vsize - 1) {
			storeFTmpVd(n, true);
		} else {
			storeVRegister(vsize, vd, n, vfpuPfxdState, true);
		}
	}

	@Override
	public void storeVdInt(int vsize, int vd, int n) {
		if (pfxVdOverlap && n < vsize - 1) {
			storeFTmpVd(n, false);
		} else {
			storeVRegister(vsize, vd, n, vfpuPfxdState, false);
		}
	}

	@Override
	public void storeVt(int n) {
		storeVRegister(getVsize(), getVtRegisterIndex(), n, null, true);
	}

	@Override
	public void storeVtInt(int n) {
		storeVRegister(getVsize(), getVtRegisterIndex(), n, null, false);
	}

	@Override
	public void storeVt(int vsize, int n) {
		storeVRegister(vsize, getVtRegisterIndex(), n, null, true);
	}

	@Override
	public void storeVtInt(int vsize, int n) {
		storeVRegister(vsize, getVtRegisterIndex(), n, null, false);
	}

	@Override
	public void storeVt(int vsize, int vt, int n) {
		storeVRegister(vsize, vt, n, null, true);
	}

	@Override
	public void storeVtInt(int vsize, int vt, int n) {
		storeVRegister(vsize, vt, n, null, false);
	}

	@Override
	public void prepareVtForStore(int vsize, int vt, int n) {
		prepareVRegisterForStore(vsize, vt, n, null, true);
	}

	@Override
	public void prepareVtForStoreInt(int vsize, int vt, int n) {
		prepareVRegisterForStore(vsize, vt, n, null, false);
	}

	@Override
	public int getImm7() {
		return codeInstruction.getImm7();
	}

	@Override
	public int getImm5() {
		return codeInstruction.getImm5();
	}

	@Override
	public int getImm4() {
		return codeInstruction.getImm4();
	}

	@Override
	public int getImm3() {
		return codeInstruction.getImm3();
	}

	@Override
	public void loadVs(int vsize, int n) {
		loadVRegister(vsize, getVsRegisterIndex(), n, vfpuPfxsState, true);
	}

	@Override
	public void loadVsInt(int vsize, int n) {
		loadVRegister(vsize, getVsRegisterIndex(), n, vfpuPfxsState, false);
	}

	@Override
	public void loadVs(int vsize, int vs, int n) {
		loadVRegister(vsize, vs, n, vfpuPfxsState, true);
	}

	@Override
	public void loadVsInt(int vsize, int vs, int n) {
		loadVRegister(vsize, vs, n, vfpuPfxsState, false);
	}

	@Override
	public void loadTmp1() {
		loadLocalVar(LOCAL_TMP1);
	}

	@Override
	public void loadTmp2() {
		loadLocalVar(LOCAL_TMP2);
	}

	@Override
	public void loadLTmp1() {
		mv.visitVarInsn(Opcodes.LLOAD, LOCAL_TMP1);
	}

	@Override
	public void loadFTmp1() {
        mv.visitVarInsn(Opcodes.FLOAD, LOCAL_TMP1);
	}

	@Override
	public void loadFTmp2() {
        mv.visitVarInsn(Opcodes.FLOAD, LOCAL_TMP2);
	}

	@Override
	public void loadFTmp3() {
        mv.visitVarInsn(Opcodes.FLOAD, LOCAL_TMP3);
	}

	@Override
	public void loadFTmp4() {
        mv.visitVarInsn(Opcodes.FLOAD, LOCAL_TMP4);
	}

	private void loadFTmpVd(int n, boolean isFloat) {
		int opcode = isFloat ? Opcodes.FLOAD : Opcodes.ILOAD;
		if (n == 0) {
	        mv.visitVarInsn(opcode, LOCAL_TMP_VD0);
		} else if (n == 1) {
	        mv.visitVarInsn(opcode, LOCAL_TMP_VD1);
		} else {
	        mv.visitVarInsn(opcode, LOCAL_TMP_VD2);
		}
	}

	@Override
	public void storeTmp1() {
		mv.visitVarInsn(Opcodes.ISTORE, LOCAL_TMP1);
	}

	@Override
	public void storeTmp2() {
		mv.visitVarInsn(Opcodes.ISTORE, LOCAL_TMP2);
	}

	@Override
	public void storeLTmp1() {
		mv.visitVarInsn(Opcodes.LSTORE, LOCAL_TMP1);
	}

	@Override
	public void storeFTmp1() {
		mv.visitVarInsn(Opcodes.FSTORE, LOCAL_TMP1);
	}

	@Override
	public void storeFTmp2() {
		mv.visitVarInsn(Opcodes.FSTORE, LOCAL_TMP2);
	}

	@Override
	public void storeFTmp3() {
		mv.visitVarInsn(Opcodes.FSTORE, LOCAL_TMP3);
	}

	@Override
	public void storeFTmp4() {
		mv.visitVarInsn(Opcodes.FSTORE, LOCAL_TMP4);
	}

	private void storeFTmpVd(int n, boolean isFloat) {
		int opcode = isFloat ? Opcodes.FSTORE : Opcodes.ISTORE;
		if (n == 0) {
			mv.visitVarInsn(opcode, LOCAL_TMP_VD0);
		} else if (n == 1) {
			mv.visitVarInsn(opcode, LOCAL_TMP_VD1);
		} else {
			mv.visitVarInsn(opcode, LOCAL_TMP_VD2);
		}
	}

	@Override
	public VfpuPfxDstState getPfxdState() {
		return vfpuPfxdState;
	}

	@Override
	public VfpuPfxSrcState getPfxsState() {
		return vfpuPfxsState;
	}

	@Override
	public VfpuPfxSrcState getPfxtState() {
		return vfpuPfxtState;
	}

    private void startPfxCompiled(VfpuPfxState vfpuPfxState, String name, String descriptor, String internalName, boolean isFloat) {
        if (vfpuPfxState.isUnknown()) {
            if (interpretPfxLabel == null) {
                interpretPfxLabel = new Label();
            }

            loadVcr();
            mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(Vcr.class), name, descriptor);
            mv.visitFieldInsn(Opcodes.GETFIELD, internalName, "enabled", "Z");
            mv.visitJumpInsn(Opcodes.IFNE, interpretPfxLabel);
        }
    }

    @Override
    public void startPfxCompiled() {
    	startPfxCompiled(true);
    }

    @Override
    public void startPfxCompiled(boolean isFloat) {
        interpretPfxLabel = null;

        if (codeInstruction.hasFlags(Instruction.FLAG_USE_VFPU_PFXS)) {
            startPfxCompiled(vfpuPfxsState, "pfxs", Type.getDescriptor(PfxSrc.class), Type.getInternalName(PfxSrc.class), isFloat);
        }

        if (codeInstruction.hasFlags(Instruction.FLAG_USE_VFPU_PFXT)) {
            startPfxCompiled(vfpuPfxtState, "pfxt", Type.getDescriptor(PfxSrc.class), Type.getInternalName(PfxSrc.class), isFloat);
        }

        if (codeInstruction.hasFlags(Instruction.FLAG_USE_VFPU_PFXD)) {
            startPfxCompiled(vfpuPfxdState, "pfxd", Type.getDescriptor(PfxDst.class), Type.getInternalName(PfxDst.class), isFloat);
        }

        pfxVdOverlap = false;
		if (getCodeInstruction().hasFlags(Instruction.FLAG_USE_VFPU_PFXS | Instruction.FLAG_USE_VFPU_PFXD)) {
			pfxVdOverlap |= isVsVdOverlap();
		}
		if (getCodeInstruction().hasFlags(Instruction.FLAG_USE_VFPU_PFXT | Instruction.FLAG_USE_VFPU_PFXD)) {
			pfxVdOverlap |= isVtVdOverlap();
		}
    }

    @Override
    public void endPfxCompiled() {
    	endPfxCompiled(true);
    }

    @Override
    public void endPfxCompiled(boolean isFloat) {
    	endPfxCompiled(getVsize(), isFloat);
    }

    @Override
    public void endPfxCompiled(int vsize) {
    	endPfxCompiled(vsize, true);
    }

    @Override
    public void endPfxCompiled(int vsize, boolean isFloat) {
    	endPfxCompiled(vsize, isFloat, true);
    }

    @Override
    public void endPfxCompiled(int vsize, boolean isFloat, boolean doFlush) {
    	if (doFlush) {
    		flushPfxCompiled(vsize, getVdRegisterIndex(), isFloat);
    	}

		if (interpretPfxLabel != null) {
            Label continueLabel = new Label();
            mv.visitJumpInsn(Opcodes.GOTO, continueLabel);
            mv.visitLabel(interpretPfxLabel);
            compileInterpreterInstruction();
            mv.visitLabel(continueLabel);

            interpretPfxLabel = null;
        }

        pfxVdOverlap = false;
    }

    @Override
    public void flushPfxCompiled(int vsize, int vd, boolean isFloat) {
		if (pfxVdOverlap) {
			// Write back the temporary overlap variables
			pfxVdOverlap = false;
			for (int n = 0; n < vsize - 1; n++) {
				if (isFloat) {
					prepareVdForStore(vsize, vd, n);
				} else {
					prepareVdForStoreInt(vsize, vd, n);
				}
				loadFTmpVd(n, isFloat);
				if (isFloat) {
					storeVd(vsize, vd, n);
				} else {
					storeVdInt(vsize, vd, n);
				}
			}
			pfxVdOverlap = true;
		}
    }

    @Override
    public boolean isPfxConsumed(int flag) {
        if (log.isTraceEnabled()) {
        	log.trace(String.format("PFX -> %08X: %s", getCodeInstruction().getAddress(), getCodeInstruction().getInsn().disasm(getCodeInstruction().getAddress(), getCodeInstruction().getOpcode())));
        }

        int address = getCodeInstruction().getAddress();
        while (true) {
            address += 4;
            CodeInstruction codeInstruction = getCodeBlock().getCodeInstruction(address);

            if (log.isTraceEnabled()) {
            	log.trace(String.format("PFX    %08X: %s", codeInstruction.getAddress(), codeInstruction.getInsn().disasm(codeInstruction.getAddress(), codeInstruction.getOpcode())));
            }

            if (codeInstruction == null || !isNonBranchingCodeSequence(codeInstruction)) {
                return false;
            }
            if (codeInstruction.hasFlags(flag)) {
            	return codeInstruction.hasFlags(Instruction.FLAG_COMPILED_PFX);
            }
        }
    }

    private boolean isVxVdOverlap(VfpuPfxSrcState pfxSrcState, int registerIndex) {
		if (!pfxSrcState.isKnown()) {
			return false;
		}

		int vsize = getVsize();
		int vd = getVdRegisterIndex();
		// Check if registers are overlapping
		if (registerIndex != vd) {
			if (vsize != 3) {
				// Different register numbers, no overlap possible
				return false;
			}
			// For vsize==3, a possible overlap exist. E.g.
			//    C000.t and C001.t
			// are partially overlapping.
			if ((registerIndex & 63) != (vd & 63)) {
				return false;
			}
		}

		if (!pfxSrcState.pfxSrc.enabled) {
			return true;
		}

		for (int n = 0; n < vsize; n++) {
			if (!pfxSrcState.pfxSrc.cst[n] && pfxSrcState.pfxSrc.swz[n] != n) {
				return true;
			}
		}

		return false;
    }

    @Override
	public boolean isVsVdOverlap() {
    	return isVxVdOverlap(vfpuPfxsState, getVsRegisterIndex());
	}

	@Override
	public boolean isVtVdOverlap() {
    	return isVxVdOverlap(vfpuPfxtState, getVtRegisterIndex());
	}

	private boolean canUseVFPUInt(int vsize) {
		if (vfpuPfxsState.isKnown() && vfpuPfxsState.pfxSrc.enabled) {
			for (int i = 0; i < vsize; i++) {
				// abs, neg and cst source prefixes can be handled as int
				if (vfpuPfxsState.pfxSrc.swz[i] != i) {
					return false;
				}
			}
		}

		if (vfpuPfxdState.isKnown() && vfpuPfxdState.pfxDst.enabled) {
			return false;
		}

		return true;
	}

	@Override
	public void compileVFPUInstr(Object cstBefore, int opcode, String mathFunction) {
		int vsize = getVsize();
		boolean useVt = getCodeInstruction().hasFlags(Instruction.FLAG_USE_VFPU_PFXT);

		if (mathFunction == null &&
		    opcode == Opcodes.NOP &&
		    !useVt &&
		    cstBefore == null &&
		    canUseVFPUInt(vsize)) {
			// VMOV should use int instead of float
			startPfxCompiled(false);

			for (int n = 0; n < vsize; n++) {
				prepareVdForStoreInt(n);
				loadVsInt(n);
				storeVdInt(n);
			}

			endPfxCompiled(vsize, false);
		} else {
			startPfxCompiled(true);

			for (int n = 0; n < vsize; n++) {
				prepareVdForStore(n);
				if (cstBefore != null) {
					mv.visitLdcInsn(cstBefore);
				}

				loadVs(n);
				if (useVt) {
					loadVt(n);
				}
				if (mathFunction != null) {
					if ("abs".equals(mathFunction)) {
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Math.class), mathFunction, "(F)F");
					} else if ("max".equals(mathFunction) || "min".equals(mathFunction)) {
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Math.class), mathFunction, "(FF)F");
					} else {
						mv.visitInsn(Opcodes.F2D);
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Math.class), mathFunction, "(D)D");
						mv.visitInsn(Opcodes.D2F);
					}
				}

				Label doneStore = null;
				if (opcode != Opcodes.NOP) {
					Label doneOpcode = null;

					if (opcode == Opcodes.FDIV && cstBefore == null) {
						// if (value1 == 0f && value2 == 0f) {
						//     result = PSP-NaN | (sign(value1) ^ sign(value2));
						// } else {
						//     result = value1 / value2;
						// }
						doneOpcode = new Label();
						doneStore = new Label();
						Label notZeroByZero = new Label();
						Label notZeroByZeroPop = new Label();
						mv.visitInsn(Opcodes.DUP2);
						mv.visitInsn(Opcodes.FCONST_0);
						mv.visitInsn(Opcodes.FCMPG);
						mv.visitJumpInsn(Opcodes.IFNE, notZeroByZeroPop);
						mv.visitInsn(Opcodes.FCONST_0);
						mv.visitInsn(Opcodes.FCMPG);
						mv.visitJumpInsn(Opcodes.IFNE, notZeroByZero);
						convertVFloatToInt();
						loadImm(0x80000000);
						mv.visitInsn(Opcodes.IAND);
						mv.visitInsn(Opcodes.SWAP);
						convertVFloatToInt();
						loadImm(0x80000000);
						mv.visitInsn(Opcodes.IAND);
						mv.visitInsn(Opcodes.IXOR);
						storeTmp1();
						// Store the NaN value as an "int" to not loose any bit.
						// Storing as float results in 0x7FC00001 instead of 0x7F800001.
						mv.visitInsn(Opcodes.DUP2_X2);
						mv.visitInsn(Opcodes.POP2);
						loadPspNaNInt();
						loadTmp1();
						mv.visitInsn(Opcodes.IOR);
						int preparedRegister = preparedRegisterForStore;
						storeVdInt(n);
						preparedRegisterForStore = preparedRegister;
						mv.visitJumpInsn(Opcodes.GOTO, doneStore);

						mv.visitLabel(notZeroByZeroPop);
						mv.visitInsn(Opcodes.POP);
						mv.visitLabel(notZeroByZero);
					}

					mv.visitInsn(opcode);

					if (doneOpcode != null) {
						mv.visitLabel(doneOpcode);
					}
				}

				storeVd(n);

				if (doneStore != null) {
					mv.visitLabel(doneStore);
				}
			}

			endPfxCompiled(vsize, true);
		}
	}

	public void visitHook(NativeCodeSequence nativeCodeSequence) {
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(nativeCodeSequence.getNativeCodeSequenceClass()), nativeCodeSequence.getMethodName(), "()V");
	}

	@Override
	public boolean compileVFPULoad(int registerIndex, int offset, int vt, int count) {
		if (RuntimeContext.memoryInt == null) {
			// Can only generate an optimized code sequence for memoryInt
			return false;
		}

		if ((vt & 32) != 0) {
		    // Optimization possible only for column access
			return false;
		}

		// Build parameters for
    	//    System.arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
    	// i.e.
    	//    System.arraycopy(RuntimeContext.memoryInt,
    	//                     RuntimeContext.checkMemoryRead32(rs + simm14, pc) >>> 2,
    	//                     RuntimeContext.vprInt,
    	//                     vprIndex,
    	//                     countSequence * 4);
    	loadMemoryInt();

    	loadRegister(registerIndex);
		if (offset != 0) {
			loadImm(offset);
			mv.visitInsn(Opcodes.IADD);
		}
    	if (checkMemoryAccess()) {
    		loadImm(getCodeInstruction().getAddress());
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(RuntimeContext.class), "checkMemoryRead32", "(II)I");
            loadImm(2);
            mv.visitInsn(Opcodes.IUSHR);
    	} else {
    		loadImm(2);
			mv.visitInsn(Opcodes.ISHL);
			loadImm(4);
			mv.visitInsn(Opcodes.IUSHR);
    	}

    	loadVprInt();
    	int vprIndex = VfpuState.getVprIndex((vt >> 2) & 7, vt & 3, (vt & 64) >> 6);
    	loadImm(vprIndex);
    	loadImm(count);
    	mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(System.class), "arraycopy", arraycopyDescriptor);

    	// Set the VPR float values
    	for (int i = 0; i < count; i++) {
    		loadVprFloat();
    		loadImm(vprIndex + i);
    		loadVprInt();
    		loadImm(vprIndex + i);
    		mv.visitInsn(Opcodes.IALOAD);
    		convertVIntToFloat();
    		mv.visitInsn(Opcodes.FASTORE);
    	}

    	return true;
	}

	@Override
	public boolean compileVFPUStore(int registerIndex, int offset, int vt, int count) {
		if (RuntimeContext.memoryInt == null) {
			// Can only generate an optimized code sequence for memoryInt
			return false;
		}

		if ((vt & 32) != 0) {
		    // Optimization possible only for column access
			return false;
		}

		// Build parameters for
    	//    System.arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
    	// i.e.
    	//    System.arraycopy(RuntimeContext.vprInt,
    	//                     vprIndex,
    	//                     RuntimeContext.memoryInt,
    	//                     RuntimeContext.checkMemoryWrite32(rs + simm14, pc) >>> 2,
    	//                     countSequence * 4);
    	loadVprInt();
    	int vprIndex = VfpuState.getVprIndex((vt >> 2) & 7, vt & 3, (vt & 64) >> 6);
    	loadImm(vprIndex);
    	loadMemoryInt();

    	loadRegister(registerIndex);
		if (offset != 0) {
			loadImm(offset);
			mv.visitInsn(Opcodes.IADD);
		}
    	if (checkMemoryAccess()) {
    		loadImm(getCodeInstruction().getAddress());
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(RuntimeContext.class), "checkMemoryWrite32", "(II)I");
            loadImm(2);
            mv.visitInsn(Opcodes.IUSHR);
    	} else {
    		loadImm(2);
			mv.visitInsn(Opcodes.ISHL);
			loadImm(4);
			mv.visitInsn(Opcodes.IUSHR);
    	}

    	loadImm(count);
    	mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(System.class), "arraycopy", arraycopyDescriptor);

    	return true;
	}

	/**
	 * Search for a sequence of instructions saving registers onto the stack
	 * at the beginning of a code block and replace them by a meta
	 * instruction allowing a more efficient compiled code.
	 * 
	 * For example, a typical code block sequence looks like this:
	 *		addiu      $sp, $sp, -32
	 *		sw         $s3, 12($sp)
	 *		addu       $s3, $a0, $zr <=> move $s3, $a0
	 *		lui        $a0, 0x0307 <=> li $a0, 0x03070000
	 *		ori        $a0, $a0, 16
	 *		sw         $s2, 8($sp)
	 *		addu       $s2, $a1, $zr <=> move $s2, $a1
	 *		sw         $s1, 4($sp)
	 *		sw         $s0, 0($sp)
	 *		lui        $s0, 0x0000 <=> li $s0, 0x00000000
	 *		sw         $ra, 16($sp)
	 *		jal        0x0nnnnnnn
	 *		...
	 *
	 * This method will identify the "sw" instructions saving registers onto the
	 * stack and will merge them into a single meta instruction (SequenceSWCodeInstruction).
	 * 
	 * In the above example:
	 * 		addiu      $sp, $sp, -32
	 * 		sw         $s0/$s1/$s2/$s3/$ra, 0/4/8/12/16($sp)
	 * 		addu       $s3, $a0, $zr <=> move $s3, $a0
	 * 		lui        $a0, 0x0307 <=> li $a0, 0x03070000
	 * 		ori        $a0, $a0, 16
	 * 		addu       $s2, $a1, $zr <=> move $s2, $a1
	 * 		lui        $s0, 0x0000 <=> li $s0, 0x00000000
	 * 		jal        0x0nnnnnnn
	 * 		...
	 * 
	 * @param codeInstructions the list of code instruction to be optimized.
	 */
	public void optimizeSequence(List<CodeInstruction> codeInstructions) {
		// Optimization only possible for memoryInt
		if (RuntimeContext.memoryInt == null) {
			return;
		}
		// Disable optimizations when the profiler is enabled.
		if (Profiler.isProfilerEnabled()) {
			return;
		}
		// Disable optimizations when the debugger is open
		if (State.debugger != null) {
			return;
		}

		int decreaseSpInstruction = -1;
		int stackSize = 0;
		int currentInstructionIndex = 0;
		int maxSpOffset = Integer.MAX_VALUE;
		int swSequenceCount = 0;

		int[] storeSpInstructions = null;
		int[] storeSpRegisters = null;
		List<CodeInstruction> storeSpCodeInstructions = null;
		boolean[] modifiedRegisters = new boolean[GprState.NUMBER_REGISTERS];
		Arrays.fill(modifiedRegisters, false);

		for (CodeInstruction codeInstruction : codeInstructions) {
			// Stop optimization when reaching a branch, branch target or delay slot
			if (codeInstruction.isBranching() || codeInstruction.hasFlags(Instruction.FLAG_HAS_DELAY_SLOT)) {
				break;
			}
			if (codeInstruction.isBranchTarget() && codeBlock.getStartAddress() != codeInstruction.getAddress()) {
				break;
			}

			Instruction insn = codeInstruction.getInsn();

			// Check for a "sw" instruction if we have already seen an "addiu $sp, $sp, -nn".
			if (decreaseSpInstruction >= 0) {
				// Check for a "sw" instruction...
				if (insn == Instructions.SW) {
					int rs = codeInstruction.getRsRegisterIndex();
					int rt = codeInstruction.getRtRegisterIndex();
					// ...saving an unmodified register to the stack...
					if (rs == _sp) {
						int simm16 = codeInstruction.getImm16(true);
						if (!modifiedRegisters[rt]) {
							// ...at a valid stack offset...
							if (simm16 >= 0 && simm16 < stackSize && (simm16 & 3) == 0 && simm16 < maxSpOffset) {
								int index = simm16 >> 2;
								// ...at a still ununsed stack offset
								if (storeSpInstructions[index] < 0) {
									storeSpCodeInstructions.add(codeInstruction);
									storeSpInstructions[index] = currentInstructionIndex;
									storeSpRegisters[index] = rt;
									swSequenceCount++;
								}
							}
						} else {
							// The register saved to the stack has already been modified.
							// Do not optimize values above this stack offset.
							maxSpOffset = min(maxSpOffset, simm16);
						}
					}
				}
			}

			// Check for a "addiu $sp, $sp, -nn" instruction
			if (insn == Instructions.ADDI || insn == Instructions.ADDIU) {
				int rs = codeInstruction.getRsRegisterIndex();
				int rt = codeInstruction.getRtRegisterIndex();
				int simm16 = codeInstruction.getImm16(true);
				if (rt == _sp && rs == _sp && simm16 < 0) {
					decreaseSpInstruction = currentInstructionIndex;
					stackSize = -codeInstruction.getImm16(true);
					storeSpInstructions = new int[stackSize >> 2];
					Arrays.fill(storeSpInstructions, -1);
					storeSpRegisters = new int[storeSpInstructions.length];
					Arrays.fill(storeSpRegisters, -1);
					storeSpCodeInstructions = new LinkedList<CodeInstruction>();
				} else if (rs == _sp && simm16 >= 0) {
					// Loading a stack address into a register (e.g. "addiu $xx, $sp, nnn").
					// Do not optimize values above this stack offset (nnn).
					maxSpOffset = min(maxSpOffset, simm16);
				}
			// Check for a "addu $reg, $sp, $reg" instruction
			} else if (insn == Instructions.ADD || insn == Instructions.ADDU) {
				int rs = codeInstruction.getRsRegisterIndex();
				int rt = codeInstruction.getRtRegisterIndex();
				if (rs == _sp || rt == _sp) {
					// Loading the stack address into a register (e.g. "addu $reg, $sp, $zr").
					// The stack could be accessed at any address, stop optimizing.
					break;
				}
			} else if (insn == Instructions.LW || insn == Instructions.SWC1 || insn == Instructions.LWC1) {
				int rs = codeInstruction.getRsRegisterIndex();
				int simm16 = codeInstruction.getImm16(true);
				if (rs == _sp && simm16 >= 0) {
					// Accessing the stack, do not optimize values above this stack offset.
					maxSpOffset = min(maxSpOffset, simm16);
				}
			} else if (insn == Instructions.SVQ || insn == Instructions.LVQ) {
				int rs = codeInstruction.getRsRegisterIndex();
				int simm14 = codeInstruction.getImm14(true);
				if (rs == _sp && simm14 >= 0) {
					// Accessing the stack, do not optimize values above this stack offset.
					maxSpOffset = min(maxSpOffset, simm14);
				}
			}

			if (codeInstruction.hasFlags(Instruction.FLAG_WRITES_RT)) {
				modifiedRegisters[codeInstruction.getRtRegisterIndex()] = true;
			}
			if (codeInstruction.hasFlags(Instruction.FLAG_WRITES_RD)) {
				modifiedRegisters[codeInstruction.getRdRegisterIndex()] = true;
			}

			if (maxSpOffset <= 0) {
				// Nothing more to do if the complete stack should not be optimized
				break;
			}

			currentInstructionIndex++;
		}

		// If we have found more than one "sw" instructions, replace them by a meta code instruction.
		if (swSequenceCount > 1) {
			int[] offsets = new int[swSequenceCount];
			int[] registers = new int[swSequenceCount];

			int index = 0;
			for (int i = 0; i < storeSpInstructions.length && index < swSequenceCount; i++) {
				if (storeSpInstructions[i] >= 0) {
					offsets[index] = i << 2;
					registers[index] = storeSpRegisters[i];
					index++;
				}
			}

			// Remove all the "sw" instructions...
			codeInstructions.removeAll(storeSpCodeInstructions);

			// ... and replace them by a meta code instruction
			SequenceSWCodeInstruction sequenceSWCodeInstruction = new SequenceSWCodeInstruction(_sp, offsets, registers);
			sequenceSWCodeInstruction.setAddress(storeSpCodeInstructions.get(0).getAddress());
			codeInstructions.add(decreaseSpInstruction + 1, sequenceSWCodeInstruction);
		}
	}

	/**
	 * Compile a sequence
	 *     sw  $zr, n($reg)
	 *     sw  $zr, n+4($reg)
	 *     sw  $zr, n+8($reg)
	 *     ...
	 * into
	 *     System.arraycopy(FastMemory.zero, 0, memoryInt, (n + $reg) >> 2, length)
	 * 
	 * @param baseRegister
	 * @param offsets
	 * @param registers
	 * @return true  if the sequence could be compiled
	 *         false if the sequence could not be compiled
	 */
	private boolean compileSWsequenceZR(int baseRegister, int[] offsets, int[] registers) {
		for (int i = 0; i < registers.length; i++) {
			if (registers[i] != _zr) {
				return false;
			}
		}

		for (int i = 1; i < offsets.length; i++) {
			if (offsets[i] != offsets[i - 1] + 4) {
				return false;
			}
		}

		int offset = offsets[0];
		int length = offsets.length;
		do {
	    	int copyLength = Math.min(length, FastMemory.zero.length);
			// Build parameters for
	    	//    System.arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
	    	// i.e.
	    	//    System.arraycopy(FastMemory.zero,
			//                     0,
			//                     RuntimeContext.memoryInt,
	    	//                     RuntimeContext.checkMemoryRead32(baseRegister + offset, pc) >>> 2,
	    	//                     copyLength);
    		mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(FastMemory.class), "zero", "[I");
	    	loadImm(0);
	    	loadMemoryInt();
	    	prepareMemIndex(baseRegister, offset, false, 32);
	    	loadImm(copyLength);
	    	mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(System.class), "arraycopy", arraycopyDescriptor);

	    	length -= copyLength;
	    	offset += copyLength;
		} while (length > 0);

		return true;
	}

	private boolean compileSWLWsequence(int baseRegister, int[] offsets, int[] registers, boolean isLW) {
		// Optimization only possible for memoryInt
		if (RuntimeContext.memoryInt == null) {
			return false;
		}
		// Disable optimizations when the profiler is enabled.
		if (Profiler.isProfilerEnabled()) {
			return false;
		}
		// Disable optimizations when the debugger is open
		if (State.debugger != null) {
			return false;
		}

		if (!isLW) {
			if (compileSWsequenceZR(baseRegister, offsets, registers)) {
				return true;
			}
		}

		int offset = offsets[0];
		prepareMemIndex(baseRegister, offset, isLW, 32);
    	storeTmp1();

    	for (int i = 0; i < offsets.length; i++) {
    		int rt = registers[i];

    		if (offset != offsets[i]) {
        		mv.visitIincInsn(LOCAL_TMP1, (offsets[i] - offset) >> 2);
        		offset = offsets[i];
    		}

    		if (isLW) {
    			if (rt != _zr) {
	    			prepareRegisterForStore(rt);
	        		loadMemoryInt();
	        		loadTmp1();
	        		mv.visitInsn(Opcodes.IALOAD);
	        		storeRegister(rt);
    			}
    		} else {
    			loadMemoryInt();
    			loadTmp1();
    			loadRegister(rt);
    			mv.visitInsn(Opcodes.IASTORE);
    		}
    	}

    	return true;
	}

	@Override
	public boolean compileSWsequence(int baseRegister, int[] offsets, int[] registers) {
		return compileSWLWsequence(baseRegister, offsets, registers, false);
	}

	@Override
	public boolean compileLWsequence(int baseRegister, int[] offsets, int[] registers) {
		return compileSWLWsequence(baseRegister, offsets, registers, true);
	}
}
