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

import static jpcsp.util.Utilities.sleep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.MemoryMap;
import jpcsp.Processor;
import jpcsp.State;
import jpcsp.Allegrex.CpuState;
import jpcsp.Allegrex.Decoder;
import jpcsp.Allegrex.Instructions;
import jpcsp.Allegrex.Common.Instruction;
import jpcsp.HLE.Modules;
import jpcsp.HLE.PspString;
import jpcsp.HLE.SyscallHandler;
import jpcsp.HLE.kernel.managers.IntrManager;
import jpcsp.HLE.kernel.types.SceKernelThreadInfo;
import jpcsp.HLE.modules.ThreadManForUser;
import jpcsp.HLE.modules.sceDisplay;
import jpcsp.hardware.Interrupts;
import jpcsp.memory.DebuggerMemory;
import jpcsp.memory.FastMemory;
import jpcsp.scheduler.Scheduler;
import jpcsp.settings.AbstractBoolSettingsListener;
import jpcsp.settings.Settings;
import jpcsp.util.CpuDurationStatistics;
import jpcsp.util.DurationStatistics;
import jpcsp.util.Utilities;

import org.apache.log4j.Logger;

/**
 * @author gid15
 *
 */
public class RuntimeContext {
    public  static Logger log = Logger.getLogger("runtime");
	private static boolean compilerEnabled = true;
	public  static float[] fpr;
	public  static float[] vprFloat;
	public  static int[] vprInt;
	public  static int[] memoryInt;
	public  static Processor processor;
	public  static CpuState cpu;
	public  static Memory memory;
	public  static       boolean enableDebugger = true;
	public  static final String debuggerName = "syncDebugger";
	public  static final boolean debugCodeBlockCalls = false;
	public  static final String debugCodeBlockStart = "debugCodeBlockStart";
	public  static final String debugCodeBlockEnd = "debugCodeBlockEnd";
	public  static final boolean debugCodeInstruction = false;
	public  static final String debugCodeInstructionName = "debugCodeInstruction";
	public  static final boolean debugMemoryRead = false;
	public  static final boolean debugMemoryWrite = false;
	public  static final boolean debugMemoryReadWriteNoSP = true;
	public  static final boolean enableInstructionTypeCounting = false;
	public  static final String instructionTypeCount = "instructionTypeCount";
	public  static final String logInfo = "logInfo";
	public  static final String pauseEmuWithStatus = "pauseEmuWithStatus";
	public  static final boolean enableLineNumbers = true;
	public  static final boolean checkCodeModification = false;
	private static final boolean invalidateAllCodeBlocks = false;
	private static final int idleSleepMicros = 1000;
	private static final Map<Integer, CodeBlock> codeBlocks = Collections.synchronizedMap(new HashMap<Integer, CodeBlock>());
	private static int codeBlocksLowestAddress = Integer.MAX_VALUE;
	private static int codeBlocksHighestAddress = Integer.MIN_VALUE;
	// A fast lookup array for executables (to improve the performance of the Allegrex instruction jalr)
	private static IExecutable[] fastExecutableLookup;
	private static final Map<SceKernelThreadInfo, RuntimeThread> threads = Collections.synchronizedMap(new HashMap<SceKernelThreadInfo, RuntimeThread>());
	private static final Map<SceKernelThreadInfo, RuntimeThread> toBeStoppedThreads = Collections.synchronizedMap(new HashMap<SceKernelThreadInfo, RuntimeThread>());
	private static final Map<SceKernelThreadInfo, RuntimeThread> alreadyStoppedThreads = Collections.synchronizedMap(new HashMap<SceKernelThreadInfo, RuntimeThread>());
	private static final List<Thread> alreadySwitchedStoppedThreads = Collections.synchronizedList(new ArrayList<Thread>());
	private static final Map<SceKernelThreadInfo, RuntimeThread> toBeDeletedThreads = Collections.synchronizedMap(new HashMap<SceKernelThreadInfo, RuntimeThread>());
	public  static volatile SceKernelThreadInfo currentThread = null;
	private static volatile RuntimeThread currentRuntimeThread = null;
	private static final Object waitForEnd = new Object();
	private static volatile Emulator emulator;
	private static volatile boolean isIdle = false;
	private static volatile boolean reset = false;
	public  static CpuDurationStatistics idleDuration = new CpuDurationStatistics("Idle Time");
	private static Map<Instruction, Integer> instructionTypeCounts = Collections.synchronizedMap(new HashMap<Instruction, Integer>());
	public  static boolean enableDaemonThreadSync = true;
	public  static final String syncName = "sync";
	public  static volatile boolean wantSync = false;
	private static RuntimeSyncThread runtimeSyncThread = null;
	private static RuntimeThread syscallRuntimeThread;
	private static sceDisplay sceDisplayModule;

	private static class CompilerEnabledSettingsListerner extends AbstractBoolSettingsListener {
		@Override
		protected void settingsValueChanged(boolean value) {
			setCompilerEnabled(value);
		}
	}

	private static void setCompilerEnabled(boolean enabled) {
		compilerEnabled = enabled;
	}

	public static boolean isCompilerEnabled() {
		return compilerEnabled;
	}

	public static void execute(Instruction insn, int opcode) {
		insn.interpret(processor, opcode);
	}

	private static int jumpCall(int address) throws Exception {
        IExecutable executable = getExecutable(address);
        if (executable == null) {
            // TODO Return to interpreter
            log.error("RuntimeContext.jumpCall - Cannot find executable");
            throw new RuntimeException("Cannot find executable");
        }

		int returnValue;
		int sp = cpu._sp;
		RuntimeThread stackThread = currentRuntimeThread;

		if (stackThread != null && stackThread.isStackMaxSize()) {
			if (log.isDebugEnabled()) {
				log.debug(String.format("jumpCall stack already reached maxSize, returning 0x%08X", address));
			}
			throw new StackPopException(address);
		}

		try {
			if (stackThread != null) {
				stackThread.increaseStackSize();
			}
			returnValue = executable.exec();
		} catch (StackOverflowError e) {
			log.error(String.format("StackOverflowError stackSize=%d", stackThread.getStackSize()));
			throw e;
		} finally {
			if (stackThread != null) {
				stackThread.decreaseStackSize();
			}
		}

		if (stackThread != null && stackThread.isStackMaxSize() && cpu._sp > sp) {
			if (log.isDebugEnabled()) {
				log.debug(String.format("jumpCall returning 0x%08X with $sp=0x%08X, start $sp=0x%08X", returnValue, cpu._sp, sp));
			}
			throw new StackPopException(returnValue);
		}

		if (debugCodeBlockCalls && log.isDebugEnabled()) {
        	log.debug(String.format("RuntimeContext.jumpCall returning 0x%08X", returnValue));
        }

        return returnValue;
	}

	public static void jump(int address, int returnAddress) throws Exception {
		if (debugCodeBlockCalls && log.isDebugEnabled()) {
			log.debug(String.format("RuntimeContext.jump starting address=0x%08X, returnAddress=0x%08X, $sp=0x%08X", address, returnAddress, cpu._sp));
		}

		int sp = cpu._sp;
		while (address != returnAddress) {
			try {
				address = jumpCall(address);
			} catch (StackPopException e) {
				if (log.isDebugEnabled()) {
					log.debug(String.format("jumpCall catching StackPopException 0x%08X with $sp=0x%08X, start $sp=0x%08X", e.getRa(), cpu._sp, sp));
				}
				if (e.getRa() != returnAddress) {
					throw e;
				}
				break;
			}
		}

		if (debugCodeBlockCalls && log.isDebugEnabled()) {
			log.debug(String.format("RuntimeContext.jump returning address=0x%08X, returnAddress=0x%08X, $sp=0x%08X", address, returnAddress, cpu._sp));
		}
	}

    public static int call(int address) throws Exception {
		if (debugCodeBlockCalls && log.isDebugEnabled()) {
			log.debug(String.format("RuntimeContext.call address=0x%08X", address));
		}
        int returnValue = jumpCall(address);

        return returnValue;
    }

	public static int executeInterpreter(int address) throws Exception {
		if (debugCodeBlockCalls && log.isDebugEnabled()) {
			log.debug(String.format("RuntimeContext.executeInterpreter address=0x%08X", address));
		}

		boolean interpret = true;
		cpu.pc = address;
		int returnValue = 0;
		while (interpret) {
			int opcode = cpu.fetchOpcode();
			Instruction insn = Decoder.instruction(opcode);
			insn.interpret(processor, opcode);
			if (insn.hasFlags(Instruction.FLAG_STARTS_NEW_BLOCK)) {
				cpu.pc = jumpCall(cpu.pc);
			} else if (insn.hasFlags(Instruction.FLAG_ENDS_BLOCK) && !insn.hasFlags(Instruction.FLAG_IS_CONDITIONAL)) {
				interpret = false;
				returnValue = cpu.pc;
			}
		}

		return returnValue;
	}

	public static void execute(int opcode) {
		Instruction insn = Decoder.instruction(opcode);
		execute(insn, opcode);
	}

	public static void debugCodeBlockStart(int address) {
    	if (log.isDebugEnabled()) {
    		String comment = "";
    		int syscallAddress = address + 4;
    		if (Memory.isAddressGood(syscallAddress)) {
        		int syscallOpcode = memory.read32(syscallAddress);
        		Instruction syscallInstruction = Decoder.instruction(syscallOpcode);
        		if (syscallInstruction == Instructions.SYSCALL) {
            		String syscallDisasm = syscallInstruction.disasm(syscallAddress, syscallOpcode);
        			comment = syscallDisasm.substring(19);
        		}
    		}
    		log.debug(String.format("Starting CodeBlock 0x%08X%s, $ra=0x%08X, $sp=0x%08X", address, comment, cpu._ra, cpu._sp));
    	}
    }

    public static void debugCodeBlockEnd(int address, int returnAddress) {
    	if (log.isDebugEnabled()) {
    		log.debug(String.format("Returning from CodeBlock 0x%08X to 0x%08X, $sp=0x%08X", address, returnAddress, cpu._sp));
    	}
    }

    public static void debugCodeInstruction(int address, int opcode) {
    	if (log.isTraceEnabled()) {
    		cpu.pc = address;
    		Instruction insn = Decoder.instruction(opcode);
    		char compileFlag = insn.hasFlags(Instruction.FLAG_INTERPRETED) ? 'I' : 'C';
    		log.trace(String.format("Executing 0x%08X %c - %s", address, compileFlag, insn.disasm(address, opcode)));
    	}
    }

    private static boolean initialise() {
        if (!compilerEnabled) {
            return false;
        }

        if (enableDaemonThreadSync && runtimeSyncThread == null) {
        	runtimeSyncThread = new RuntimeSyncThread();
        	runtimeSyncThread.setName("Sync Daemon");
        	runtimeSyncThread.setDaemon(true);
        	runtimeSyncThread.start();
        }

        memory = Emulator.getMemory();
		if (memory instanceof FastMemory) {
			memoryInt = ((FastMemory) memory).getAll();
		} else {
		    memoryInt = null;
		}

        if (State.debugger != null || (memory instanceof DebuggerMemory) || debugMemoryRead || debugMemoryWrite) {
        	enableDebugger = true;
        } else {
        	enableDebugger = false;
        }

        Profiler.initialise();

        sceDisplayModule = Modules.sceDisplayModule;

        fastExecutableLookup = new IExecutable[(MemoryMap.END_USERSPACE - MemoryMap.START_USERSPACE + 1) >> 2];

		return true;
    }

    public static boolean canExecuteCallback(SceKernelThreadInfo callbackThread) {
    	if (!compilerEnabled) {
    		return true;
    	}

    	// Can the callback be executed in any thread (e.g. is an interrupt)?
    	if (callbackThread == null) {
    		return true;
    	}

    	if (Modules.ThreadManForUserModule.isIdleThread(callbackThread)) {
    		return true;
    	}

    	Thread currentThread = Thread.currentThread();
    	if (currentThread instanceof RuntimeThread) {
    		RuntimeThread currentRuntimeThread = (RuntimeThread) currentThread;
    		if (callbackThread == currentRuntimeThread.getThreadInfo()) {
    			return true;
    		}
    	}

    	return false;
    }

    private static void checkPendingCallbacks() {
    	if (Modules.ThreadManForUserModule.checkPendingActions()) {
        	// if some action has been executed, the current thread might be changed. Resync.
    		if (log.isDebugEnabled()) {
    			log.debug(String.format("A pending action has been executed for the thread"));
    		}
    		wantSync = true;
    	}

    	Modules.ThreadManForUserModule.checkPendingCallbacks();
    }

    public static void executeCallback() {
    	int pc = cpu.pc;

		if (log.isDebugEnabled()) {
			log.debug(String.format("Start of Callback 0x%08X", pc));
		}

		// Switch to the real active thread, even if it is an idle thread
		switchRealThread(Modules.ThreadManForUserModule.getCurrentThread());

		IExecutable executable = getExecutable(pc);
        int newPc = 0;
        int returnAddress = cpu._ra;
        boolean callbackExited = false;
		try {
			execWithReturnAddress(executable, returnAddress);
			newPc = returnAddress;
		} catch (StopThreadException e) {
			// Ignore exception
		} catch (Exception e) {
			log.error("Catched Throwable in executeCallback:", e);
			callbackExited = true;
		}
    	cpu.pc = newPc;
    	cpu.npc = newPc; // npc is used when context switching

		if (log.isDebugEnabled()) {
			log.debug(String.format("End of Callback 0x%08X", pc));
		}

        if (cpu.pc == ThreadManForUser.CALLBACK_EXIT_HANDLER_ADDRESS || callbackExited) {
            Modules.ThreadManForUserModule.hleKernelExitCallback(Emulator.getProcessor());

            // Re-sync the runtime, the current thread might have been rescheduled
            wantSync = true;
        }

        update();
    }

    private static void updateStaticVariables() {
		emulator = Emulator.getInstance();
		processor = Emulator.getProcessor();
		cpu = processor.cpu;
		if (cpu != null) {
		    fpr = processor.cpu.fpr;
		    vprFloat = processor.cpu.vprFloat;
		    vprInt = processor.cpu.vprInt;
		}
    }

    public static void update() {
        if (!compilerEnabled) {
            return;
        }

        updateStaticVariables();

        ThreadManForUser threadMan = Modules.ThreadManForUserModule;
        if (IntrManager.getInstance().canExecuteInterruptNow()) {
            SceKernelThreadInfo newThread = threadMan.getCurrentThread();
            if (newThread != null && newThread != currentThread) {
                switchThread(newThread);
            }
        }
	}

    private static void switchRealThread(SceKernelThreadInfo threadInfo) {
    	RuntimeThread thread = threads.get(threadInfo);
    	if (thread == null) {
    		thread = new RuntimeThread(threadInfo);
    		threads.put(threadInfo, thread);
    		thread.start();
    	}

    	currentThread = threadInfo;
    	currentRuntimeThread = thread;
        isIdle = false;
    }

    private static void switchThread(SceKernelThreadInfo threadInfo) {
    	if (log.isDebugEnabled()) {
    		String name;
    		if (threadInfo == null) {
    			name = "Idle";
    		} else {
    			name = threadInfo.name;
    		}

    		if (currentThread == null) {
        		log.debug("Switching to Thread " + name);
    		} else {
        		log.debug("Switching from Thread " + currentThread.name + " to " + name);
    		}
    	}

    	if (threadInfo == null || Modules.ThreadManForUserModule.isIdleThread(threadInfo)) {
    	    isIdle = true;
    	    currentThread = null;
    	    currentRuntimeThread = null;
    	} else if (toBeStoppedThreads.containsKey(threadInfo)) {
    		// This thread must stop immediately
    		isIdle = true;
    		currentThread = null;
    		currentRuntimeThread = null;
    	} else {
    		switchRealThread(threadInfo);
    	}
    }

    private static void syncIdle() throws StopThreadException {
        if (isIdle) {
        	ThreadManForUser threadMan = Modules.ThreadManForUserModule;
            Scheduler scheduler = Emulator.getScheduler();

            log.debug("Starting Idle State...");
            idleDuration.start();
            while (isIdle) {
            	checkStoppedThread();
            	{
            		// Do not take the duration of sceDisplay into idleDuration
            		idleDuration.end();
            		syncEmulator(true);
            		idleDuration.start();
            	}
                syncPause();
                checkPendingCallbacks();
                scheduler.step();
                if (threadMan.isIdleThread(threadMan.getCurrentThread())) {
                	threadMan.checkCallbacks();
                	threadMan.hleRescheduleCurrentThread();
                }

                if (isIdle) {
                	long delay = scheduler.getNextActionDelay(idleSleepMicros);
                	if (delay > 0) {
                		int intDelay;
	                	if (delay >= idleSleepMicros) {
	                		intDelay = idleSleepMicros;
	                	} else {
	                		intDelay = (int) delay;
	                	}
                		sleep(intDelay / 1000, intDelay % 1000);
                	}
                }
            }
            idleDuration.end();
            log.debug("Ending Idle State");
        }
    }

    private static void syncThreadImmediately() throws StopThreadException {
        Thread currentThread = Thread.currentThread();
    	if (currentRuntimeThread != null &&
                currentThread != currentRuntimeThread && !alreadySwitchedStoppedThreads.contains(currentThread)) {
    		currentRuntimeThread.continueRuntimeExecution();

    		if (currentThread instanceof RuntimeThread) {
    			RuntimeThread runtimeThread = (RuntimeThread) currentThread;
    			if (!alreadyStoppedThreads.containsValue(runtimeThread)) {
	    			log.debug("Waiting to be scheduled...");
					runtimeThread.suspendRuntimeExecution();
	    			log.debug("Scheduled, restarting...");
	    	        checkStoppedThread();

	    	        updateStaticVariables();
    			} else {
    				alreadySwitchedStoppedThreads.add(currentThread);
    			}
    		}
    	}

    	checkPendingCallbacks();
    }

    private static void syncThread() throws StopThreadException {
        syncIdle();

        if (toBeDeletedThreads.containsValue(getRuntimeThread())) {
        	return;
        }

        Thread currentThread = Thread.currentThread();
    	if (log.isDebugEnabled()) {
    		log.debug("syncThread currentThread=" + currentThread.getName() + ", currentRuntimeThread=" + currentRuntimeThread.getName());
    	}
    	syncThreadImmediately();
    }

    private static RuntimeThread getRuntimeThread() {
    	Thread currentThread = Thread.currentThread();
		if (currentThread instanceof RuntimeThread) {
			return (RuntimeThread) currentThread;
		}

		return null;
    }

    private static boolean isStoppedThread() {
    	if (toBeStoppedThreads.isEmpty()) {
    		return false;
    	}

		RuntimeThread runtimeThread = getRuntimeThread();
		if (runtimeThread != null && toBeStoppedThreads.containsValue(runtimeThread)) {
			if (!alreadyStoppedThreads.containsValue(runtimeThread)) {
				return true;
			}
		}

		return false;
    }

    private static void checkStoppedThread() throws StopThreadException {
    	if (isStoppedThread()) {
			throw new StopThreadException("Stopping Thread " + Thread.currentThread().getName());
		}
    }

    private static void syncPause() throws StopThreadException {
    	if (Emulator.pause) {
	        Emulator.getClock().pause();
	        try {
	            synchronized(emulator) {
	               while (Emulator.pause) {
	                   checkStoppedThread();
	                   emulator.wait();
	               }
	           }
	        } catch (InterruptedException e){
	        	// Ignore Exception
	        } finally {
	        	Emulator.getClock().resume();
	        }
    	}
    }

    public static void syncDebugger(int pc) throws StopThreadException {
		processor.cpu.pc = pc;
    	if (State.debugger != null) {
    		syncDebugger();
    		syncPause();
    	} else if (Emulator.pause) {
    		syncPause();
    	}
    }

    private static void syncDebugger() {
        if (State.debugger != null) {
            State.debugger.step();
        }
    }

    private static void syncEmulator(boolean immediately) {
        if (log.isDebugEnabled()) {
            log.debug("syncEmulator immediately=" + immediately);
        }

        Modules.sceGe_userModule.step();
		Modules.sceDisplayModule.step(immediately);
    }

    private static void syncFast() {
        // Always sync the display to trigger the GE list processing
        Modules.sceDisplayModule.step();
    }

    public static void sync() throws StopThreadException {
    	do {
    		wantSync = false;

	    	if (!IntrManager.getInstance().canExecuteInterruptNow()) {
	    		syncFast();
	    	} else {
		    	syncPause();
				Emulator.getScheduler().step();
				if (Interrupts.isInterruptsEnabled()) {
					Modules.ThreadManForUserModule.hleRescheduleCurrentThread();
				}
				syncThread();
				syncEmulator(false);
		        syncDebugger();
		    	syncPause();
		    	checkStoppedThread();
	        }
    	// Check if a new sync request has been received in the meantime
    	} while (wantSync);
    }

    public static void preSyscall() throws StopThreadException {
    	if (IntrManager.getInstance().canExecuteInterruptNow()) {
	    	syscallRuntimeThread = getRuntimeThread();
	    	if (syscallRuntimeThread != null) {
	    		syscallRuntimeThread.setInSyscall(true);
	    	}
	    	checkStoppedThread();
	    	syncPause();
    	}
    }

    public static void postSyscall() throws StopThreadException {
    	if (!IntrManager.getInstance().canExecuteInterruptNow()) {
    		postSyscallFast();
    	} else {
	    	checkStoppedThread();
	    	sync();
	    	if (syscallRuntimeThread != null) {
	    		syscallRuntimeThread.setInSyscall(false);
	    	}
    	}
    }

    public static void postSyscallFast() {
    	syncFast();
    }

    public static void syscallFast(int code) {
		// Fast syscall: no context switching
    	SyscallHandler.syscall(code);
    	postSyscallFast();
    }

    public static void syscall(int code) throws StopThreadException {
    	preSyscall();
    	SyscallHandler.syscall(code);
    	postSyscall();
    }

    private static void execWithReturnAddress(IExecutable executable, int returnAddress) throws Exception {
    	while (true) {
    		try {
    			int address = executable.exec();
    			if (address != returnAddress) {
    				jump(address, returnAddress);
    			}
    			break;
			} catch (StackPopException e) {
				log.info("Stack exceeded maximum size, shrinking to top level");
				executable = getExecutable(e.getRa());
				if (executable == null) {
					throw e;
				}
			}
		}
    }

    public static void runThread(RuntimeThread thread) {
    	thread.setInSyscall(true);

    	if (isStoppedThread()) {
			// This thread has already been stopped before it is really starting...
    		return;
    	}

		thread.suspendRuntimeExecution();

    	if (isStoppedThread()) {
			// This thread has already been stopped before it is really starting...
    		return;
    	}

        ThreadManForUser threadMan = Modules.ThreadManForUserModule;

        IExecutable executable = getExecutable(processor.cpu.pc);
		thread.setInSyscall(false);
    	try {
    		updateStaticVariables();

    		// Execute any thread event handler for THREAD_EVENT_START
    		// in the thread context, before starting the thread execution.
    		threadMan.checkPendingCallbacks();

    		execWithReturnAddress(executable, ThreadManForUser.THREAD_EXIT_HANDLER_ADDRESS);
            // NOTE: When a thread exits by itself (without calling sceKernelExitThread),
            // it's exitStatus becomes it's return value.
    		threadMan.hleKernelExitThread(processor.cpu._v0);
    	} catch (StopThreadException e) {
    		// Ignore Exception
    	} catch (Throwable e) {
    		// Do not spam exceptions when exiting...
        	if (!Modules.ThreadManForUserModule.exitCalled) {
	    		// Log error in log file and command box
	    		log.error("Catched Throwable in RuntimeThread:", e);
	    		e.printStackTrace();
        	}
		}

		SceKernelThreadInfo threadInfo = thread.getThreadInfo();
    	alreadyStoppedThreads.put(threadInfo, thread);

    	if (log.isDebugEnabled()) {
    		log.debug("End of Thread " + threadInfo.name + " - stopped");
    	}

    	// Tell stopAllThreads that this thread is stopped.
    	thread.setInSyscall(true);

    	threads.remove(threadInfo);
		toBeStoppedThreads.remove(threadInfo);
		toBeDeletedThreads.remove(threadInfo);

		if (!reset) {
			// Switch to the currently active thread
			try {
		    	if (log.isDebugEnabled()) {
		    		log.debug("End of Thread " + threadInfo.name + " - sync");
		    	}

		    	// Be careful to not execute Interrupts or Callbacks by this thread,
		    	// as it is already stopped and the next active thread
		    	// will be resumed immediately.
	    		syncIdle();
	    		syncThreadImmediately();
			} catch (StopThreadException e) {
			}
		}

		alreadyStoppedThreads.remove(threadInfo);
		alreadySwitchedStoppedThreads.remove(thread);

		if (log.isDebugEnabled()) {
			log.debug("End of Thread " + thread.getName());
		}

		synchronized (waitForEnd) {
			waitForEnd.notify();
		}
    }

    private static void computeCodeBlocksRange() {
    	codeBlocksLowestAddress = Integer.MAX_VALUE;
    	codeBlocksHighestAddress = Integer.MIN_VALUE;
    	for (CodeBlock codeBlock : codeBlocks.values()) {
    		if (!codeBlock.isInternal()) {
	    		codeBlocksLowestAddress = Math.min(codeBlocksLowestAddress, codeBlock.getLowestAddress());
	    		codeBlocksHighestAddress = Math.max(codeBlocksHighestAddress, codeBlock.getHighestAddress());
    		}
    	}
    }

    public static void addCodeBlock(int address, CodeBlock codeBlock) {
    	CodeBlock previousCodeBlock = codeBlocks.put(address, codeBlock);

    	if (!codeBlock.isInternal()) {
	    	if (previousCodeBlock != null) {
	    		// One code block has been deleted, recompute the whole code blocks range
	    		computeCodeBlocksRange();

	    		int fastExecutableLoopukIndex = (address - MemoryMap.START_USERSPACE) >> 2;
	    		fastExecutableLookup[fastExecutableLoopukIndex] = null;
	    	} else {
	    		// One new code block has been added, update the code blocks range
	    		codeBlocksLowestAddress = Math.min(codeBlocksLowestAddress, codeBlock.getLowestAddress());
	    		codeBlocksHighestAddress = Math.max(codeBlocksHighestAddress, codeBlock.getHighestAddress());
	    	}
    	}
    }

    public static CodeBlock getCodeBlock(int address) {
	    return codeBlocks.get(address);
	}

    public static boolean hasCodeBlock(int address) {
        return codeBlocks.containsKey(address);
    }

    public static Map<Integer, CodeBlock> getCodeBlocks() {
    	return codeBlocks;
    }

    public static IExecutable getExecutable(int address) {
    	// Check if we have already the executable in the fastExecutableLookup array
		int fastExecutableLoopukIndex = (address - MemoryMap.START_USERSPACE) >> 2;
		IExecutable executable = null;
		try {
			executable = fastExecutableLookup[fastExecutableLoopukIndex];
		} catch (ArrayIndexOutOfBoundsException e) {
			// Ignore exception
		}

		if (executable == null) {
	        CodeBlock codeBlock = getCodeBlock(address);
	        if (codeBlock == null) {
	            executable = Compiler.getInstance().compile(address);
	        } else {
	            executable = codeBlock.getExecutable();
	        }

	        // Store the executable in the fastExecutableLookup array
	        try {
	    		fastExecutableLookup[fastExecutableLoopukIndex] = executable;
	    	} catch (ArrayIndexOutOfBoundsException e) {
	    		// Ignore exception
	    	}
		}

        return executable;
    }

    public static void start() {
    	Settings.getInstance().registerSettingsListener("RuntimeContext", "emu.compiler", new CompilerEnabledSettingsListerner());
    }

    public static void run() {
    	if (Modules.ThreadManForUserModule.exitCalled) {
    		return;
    	}

    	if (!initialise()) {
        	compilerEnabled = false;
        	return;
        }

        log.info("Using Compiler");

        while (!toBeStoppedThreads.isEmpty()) {
        	wakeupToBeStoppedThreads();
        	sleep(idleSleepMicros);
        }

        reset = false;

        if (currentRuntimeThread == null) {
        	try {
				syncIdle();
			} catch (StopThreadException e) {
				// Thread is stopped, return immediately
				return;
			}

        	if (currentRuntimeThread == null) {
        		log.error("RuntimeContext.run: nothing to run!");
        		Emulator.PauseEmuWithStatus(Emulator.EMU_STATUS_UNKNOWN);
        		return;
        	}
        }

        update();

        if (processor.cpu.pc == 0) {
        	Emulator.PauseEmuWithStatus(Emulator.EMU_STATUS_UNKNOWN);
        	return;
        }

        currentRuntimeThread.continueRuntimeExecution();

        while (!threads.isEmpty() && !reset) {
	        synchronized(waitForEnd) {
	        	try {
					waitForEnd.wait();
				} catch (InterruptedException e) {
				}
	        }
        }

        log.debug("End of run");
    }

    private static List<RuntimeThread> wakeupToBeStoppedThreads() {
		List<RuntimeThread> threadList = new LinkedList<RuntimeThread>();
		synchronized (toBeStoppedThreads) {
    		for (Entry<SceKernelThreadInfo, RuntimeThread> entry : toBeStoppedThreads.entrySet()) {
    			threadList.add(entry.getValue());
    		}
		}

		// Trigger the threads to start execution again.
		// Loop on a local list to avoid concurrent modification on toBeStoppedThreads.
		for (RuntimeThread runtimeThread : threadList) {
			Thread.State threadState = runtimeThread.getState();
			log.debug("Thread " + runtimeThread.getName() + ", State=" + threadState);
			if (threadState == Thread.State.TERMINATED) {
				toBeStoppedThreads.remove(runtimeThread.getThreadInfo());
			} else if (threadState == Thread.State.WAITING) {
				runtimeThread.continueRuntimeExecution();
			}
		}

		synchronized (Emulator.getInstance()) {
			Emulator.getInstance().notifyAll();
		}

		return threadList;
    }

    public static void onThreadDeleted(SceKernelThreadInfo thread) {
    	RuntimeThread runtimeThread = threads.get(thread);
    	if (runtimeThread != null) {
    		if (log.isDebugEnabled()) {
    			log.debug("Deleting Thread " + thread.toString());
    		}
    		toBeStoppedThreads.put(thread, runtimeThread);
    		if (runtimeThread.isInSyscall() && Thread.currentThread() != runtimeThread) {
    			toBeDeletedThreads.put(thread, runtimeThread);
    			log.debug("Continue Thread " + runtimeThread.getName());
    			runtimeThread.continueRuntimeExecution();
    		}
    	}
    }

    public static void onThreadExit(SceKernelThreadInfo thread) {
    	RuntimeThread runtimeThread = threads.get(thread);
    	if (runtimeThread != null) {
    		if (log.isDebugEnabled()) {
    			log.debug("Exiting Thread " + thread.toString());
    		}
    		toBeStoppedThreads.put(thread, runtimeThread);
    		threads.remove(thread);
    	}
    }

    public static void onThreadStart(SceKernelThreadInfo thread) {
    	// The thread is starting, if a stop was still pending, cancel the stop.
    	toBeStoppedThreads.remove(thread);
    	toBeDeletedThreads.remove(thread);
    }

    private static void stopAllThreads() {
		synchronized (threads) {
			toBeStoppedThreads.putAll(threads);
    		threads.clear();
		}

		List<RuntimeThread> threadList = wakeupToBeStoppedThreads();

		// Wait for all threads to enter a syscall.
		// When a syscall is entered, the thread will exit
		// automatically by calling checkStoppedThread()
		boolean waitForThreads = true;
		while (waitForThreads) {
			waitForThreads = false;
			for (RuntimeThread runtimeThread : threadList) {
				if (!runtimeThread.isInSyscall()) {
					waitForThreads = true;
					break;
				}
			}

			if (waitForThreads) {
				sleep(idleSleepMicros);
			}
		}
    }

    public static void exit() {
        if (compilerEnabled) {
    		log.debug("RuntimeContext.exit");
        	stopAllThreads();
        	if (DurationStatistics.collectStatistics) {
        		log.info(idleDuration);
        	}

            if (enableInstructionTypeCounting) {
            	long totalCount = 0;
            	for (Instruction insn : instructionTypeCounts.keySet()) {
            		int count = instructionTypeCounts.get(insn);
            		totalCount += count;
            	}

            	while (!instructionTypeCounts.isEmpty()) {
            		Instruction highestCountInsn = null;
            		int highestCount = -1;
                	for (Instruction insn : instructionTypeCounts.keySet()) {
                		int count = instructionTypeCounts.get(insn);
                		if (count > highestCount) {
                			highestCount = count;
                			highestCountInsn = insn;
                		}
                	}
                	instructionTypeCounts.remove(highestCountInsn);
            		log.info(String.format("  %10s %s %d (%2.2f%%)", highestCountInsn.name(), (highestCountInsn.hasFlags(Instruction.FLAG_INTERPRETED) ? "I" : "C"), highestCount, highestCount * 100.0 / totalCount));
            	}
            }
        }
    }

    public static void reset() {
    	if (compilerEnabled) {
    		log.debug("RuntimeContext.reset");
    		Compiler.getInstance().reset();
    		codeBlocks.clear();
    		if (fastExecutableLookup != null) {
    			Arrays.fill(fastExecutableLookup, null);
    		}
    		currentThread = null;
    		currentRuntimeThread = null;
    		reset = true;
    		stopAllThreads();
    		synchronized (waitForEnd) {
				waitForEnd.notify();
			}
    	}
    }

    public static void invalidateAll() {
        if (compilerEnabled) {
    		if (invalidateAllCodeBlocks) {
    			// Simple method: invalidate all the code blocks,
    			// independently if their were modified or not.
        		log.debug("RuntimeContext.invalidateAll simple");
                codeBlocks.clear();
        		Arrays.fill(fastExecutableLookup, null);
                Compiler.getInstance().invalidateAll();
    		} else {
    			// Advanced method: check all the code blocks for a modification
    			// of their opcodes and invalidate only those code blocks that
    			// have been modified.
        		log.debug("RuntimeContext.invalidateAll advanced");
        		Compiler compiler = Compiler.getInstance();
	    		for (CodeBlock codeBlock : codeBlocks.values()) {
	    			if (log.isDebugEnabled()) {
	    				log.debug(String.format("invalidateAll %s: opcodes changed %b", codeBlock, codeBlock.areOpcodesChanged()));
	    			}

	    			if (codeBlock.areOpcodesChanged()) {
	    				compiler.invalidateCodeBlock(codeBlock);
	    			}
	    		}
    		}
    	}
    }

    public static void invalidateRange(int addr, int size) {
        if (compilerEnabled) {
        	addr &= Memory.addressMask;

        	if (log.isDebugEnabled()) {
        		log.debug(String.format("RuntimeContext.invalidateRange(addr=0x%08X, size=%d)", addr, size));
        	}

        	// Fast check: if the address range is outside the largest code blocks range,
        	// there is noting to do.
        	if (addr + size < codeBlocksLowestAddress || addr > codeBlocksHighestAddress) {
        		return;
        	}

        	// Check if the code blocks located in the given range have to be invalidated
    		Compiler compiler = Compiler.getInstance();
        	for (CodeBlock codeBlock : codeBlocks.values()) {
    			if (size == 0x4000 && codeBlock.getHighestAddress() >= addr) {
	    			// Some applications do not clear more than 16KB as this is the size of the complete Instruction Cache.
	    			// Be conservative in this case and check any code block above the given address.
    				compiler.checkCodeBlockValidity(codeBlock);
    			} else if (codeBlock.isOverlappingWithAddressRange(addr, size)) {
    				compiler.checkCodeBlockValidity(codeBlock);
        		}
        	}
    	}
    }

    public static void instructionTypeCount(Instruction insn, int opcode) {
    	int count = 0;
    	if (instructionTypeCounts.containsKey(insn)) {
    		count = instructionTypeCounts.get(insn);
    	}
    	count++;
    	instructionTypeCounts.put(insn, count);
    }

    public static void pauseEmuWithStatus(int status) throws StopThreadException {
    	Emulator.PauseEmuWithStatus(status);
    	syncPause();
    }

    public static void logInfo(String message) {
    	log.info(message);
    }

    public static boolean checkMemoryPointer(int address) {
        if (!Memory.isAddressGood(address)) {
            if (!Memory.isRawAddressGood(Memory.normalizeAddress(address))) {
                return false;
            }
        }

        return true;
    }

    public static String readStringNZ(int address, int maxLength) {
    	return Utilities.readStringNZ(address, maxLength);
    }

    public static PspString readPspStringNZ(int address, int maxLength, boolean canBeNull) {
    	return new PspString(address, maxLength, canBeNull);
    }

    public static int checkMemoryRead32(int address, int pc) throws StopThreadException {
        int rawAddress = address & Memory.addressMask;
        if (!Memory.isRawAddressGood(rawAddress)) {
        	if (memory.read32AllowedInvalidAddress(rawAddress)) {
        		rawAddress = 0;
        	} else {
                int normalizedAddress = Memory.normalizeAddress(address);
                if (Memory.isRawAddressGood(normalizedAddress)) {
                    rawAddress = normalizedAddress;
                } else {
		            processor.cpu.pc = pc;
		            memory.invalidMemoryAddress(address, "read32", Emulator.EMU_STATUS_MEM_READ);
		            syncPause();
		            rawAddress = 0;
                }
        	}
        }

        return rawAddress;
    }

    public static int checkMemoryRead16(int address, int pc) throws StopThreadException {
        int rawAddress = address & Memory.addressMask;
        if (!Memory.isRawAddressGood(rawAddress)) {
            int normalizedAddress = Memory.normalizeAddress(address);
            if (Memory.isRawAddressGood(normalizedAddress)) {
                rawAddress = normalizedAddress;
            } else {
	            processor.cpu.pc = pc;
	            memory.invalidMemoryAddress(address, "read16", Emulator.EMU_STATUS_MEM_READ);
	            syncPause();
	            rawAddress = 0;
            }
        }

        return rawAddress;
    }

    public static int checkMemoryRead8(int address, int pc) throws StopThreadException {
        int rawAddress = address & Memory.addressMask;
        if (!Memory.isRawAddressGood(rawAddress)) {
            int normalizedAddress = Memory.normalizeAddress(address);
            if (Memory.isRawAddressGood(normalizedAddress)) {
                rawAddress = normalizedAddress;
            } else {
	            processor.cpu.pc = pc;
	            memory.invalidMemoryAddress(address, "read8", Emulator.EMU_STATUS_MEM_READ);
	            syncPause();
	            rawAddress = 0;
            }
        }

        return rawAddress;
    }

    public static int checkMemoryWrite32(int address, int pc) throws StopThreadException {
        int rawAddress = address & Memory.addressMask;
        if (!Memory.isRawAddressGood(rawAddress)) {
            int normalizedAddress = Memory.normalizeAddress(address);
            if (Memory.isRawAddressGood(normalizedAddress)) {
                rawAddress = normalizedAddress;
            } else {
	            processor.cpu.pc = pc;
	            memory.invalidMemoryAddress(address, "write32", Emulator.EMU_STATUS_MEM_WRITE);
	            syncPause();
	            rawAddress = 0;
            }
        }

        sceDisplayModule.write32(rawAddress);

        return rawAddress;
    }

    public static int checkMemoryWrite16(int address, int pc) throws StopThreadException {
        int rawAddress = address & Memory.addressMask;
        if (!Memory.isRawAddressGood(rawAddress)) {
            int normalizedAddress = Memory.normalizeAddress(address);
            if (Memory.isRawAddressGood(normalizedAddress)) {
                rawAddress = normalizedAddress;
            } else {
	            processor.cpu.pc = pc;
	            memory.invalidMemoryAddress(address, "write16", Emulator.EMU_STATUS_MEM_WRITE);
	            syncPause();
	            rawAddress = 0;
            }
        }

        sceDisplayModule.write16(rawAddress);

        return rawAddress;
    }

    public static int checkMemoryWrite8(int address, int pc) throws StopThreadException {
        int rawAddress = address & Memory.addressMask;
        if (!Memory.isRawAddressGood(rawAddress)) {
            int normalizedAddress = Memory.normalizeAddress(address);
            if (Memory.isRawAddressGood(normalizedAddress)) {
                rawAddress = normalizedAddress;
            } else {
	            processor.cpu.pc = pc;
	            memory.invalidMemoryAddress(address, "write8", Emulator.EMU_STATUS_MEM_WRITE);
	            syncPause();
	            rawAddress = 0;
            }
        }

        sceDisplayModule.write8(rawAddress);

        return rawAddress;
    }

    public static void debugMemoryReadWrite(int address, int value, int pc, boolean isRead, int width) {
    	if (log.isTraceEnabled()) {
	    	StringBuilder message = new StringBuilder();
	    	message.append(String.format("0x%08X - ", pc));
	    	if (isRead) {
	    		message.append(String.format("read%d(0x%08X)=0x", width, address));
	    		if (width == 8) {
	    			message.append(String.format("%02X", memory.read8(address)));
	    		} else if (width == 16) {
	    			message.append(String.format("%04X", memory.read16(address)));
	    		} else if (width == 32) {
	    			message.append(String.format("%08X (%f)", memory.read32(address), Float.intBitsToFloat(memory.read32(address))));
	    		}
	    	} else {
	    		message.append(String.format("write%d(0x%08X, 0x", width, address));
	    		if (width == 8) {
	    			message.append(String.format("%02X", value));
	    		} else if (width == 16) {
	    			message.append(String.format("%04X", value));
	    		} else if (width == 32) {
	    			message.append(String.format("%08X (%f)", value, Float.intBitsToFloat(value)));
	    		}
	    		message.append(")");
	    	}
	    	log.trace(message.toString());
    	}
    }

    public static void onNextScheduleModified() {
    	checkSync(false);
    }

    private static void checkSync(boolean sleep) {
    	long delay = Emulator.getScheduler().getNextActionDelay(idleSleepMicros);
    	if (delay > 0) {
    		if (sleep) {
	    		int intDelay = (int) delay;
	    		sleep(intDelay / 1000, intDelay % 1000);
    		}
    	} else if (wantSync) {
    		if (sleep) {
    			sleep(idleSleepMicros);
    		}
    	} else {
    		wantSync = true;
    	}
    }

    public static boolean syncDaemonStep() {
    	checkSync(true);

    	return enableDaemonThreadSync;
    }

    public static void exitSyncDaemon() {
    	runtimeSyncThread = null;
    }

    public static void setIsHomebrew(boolean isHomebrew) {
    	// Currently, nothing special to do for Homebrew's
    }

    public static void onCodeModification(int pc, int opcode) {
    	cpu.pc = pc;
    	log.error(String.format("Code instruction at 0x%08X has been modified, expected 0x%08X, current 0x%08X", pc, opcode, memory.read32(pc)));
    	Emulator.PauseEmuWithStatus(Emulator.EMU_STATUS_MEM_WRITE);
    }
}