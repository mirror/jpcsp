/* This autogenerated file is part of jpcsp. */
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

package jpcsp.HLE.modules150;

import java.io.IOException;
import java.nio.ByteBuffer;

import jpcsp.HLE.Modules;
import jpcsp.HLE.ThreadMan;
import jpcsp.HLE.pspSysMem;
import jpcsp.HLE.pspiofilemgr;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules.HLEModuleFunction;
import jpcsp.HLE.modules.HLEModuleManager;
import jpcsp.HLE.kernel.types.SceModule;

import jpcsp.Emulator;
import jpcsp.FileManager;
import jpcsp.Memory;
import jpcsp.MemoryMap;
import jpcsp.Processor;
import jpcsp.filesystems.SeekableDataInput;
import jpcsp.util.Utilities;

import jpcsp.Allegrex.CpuState; // New-Style Processor



public class ModuleMgrForUser implements HLEModule {
   // String[] banlist = {};
    enum banlist {
        LIBFONT,  /*ace combat */
        sc_sascore,
        audiocodec,
        libatrac3plus,
        videocodec,
        mpegbase,
        mpeg
    }
	@Override
	public String getName() { return "ModuleMgrForUser"; }

	@Override
	public void installModule(HLEModuleManager mm, int version) {
		if (version >= 150) {

			mm.addFunction(sceKernelLoadModuleByIDFunction, 0xB7F46618);
			mm.addFunction(sceKernelLoadModuleFunction, 0x977DE386);
			mm.addFunction(sceKernelLoadModuleMsFunction, 0x710F61B5);
			mm.addFunction(sceKernelLoadModuleBufferUsbWlanFunction, 0xF9275D98);
			mm.addFunction(sceKernelStartModuleFunction, 0x50F0C1EC);
			mm.addFunction(sceKernelStopModuleFunction, 0xD1FF982A);
			mm.addFunction(sceKernelUnloadModuleFunction, 0x2E0911AA);
			mm.addFunction(sceKernelSelfStopUnloadModuleFunction, 0xD675EBB8);
			mm.addFunction(sceKernelStopUnloadSelfModuleFunction, 0xCC1D3699);
			mm.addFunction(sceKernelGetModuleIdListFunction, 0x644395E2);
			mm.addFunction(sceKernelQueryModuleInfoFunction, 0x748CBED9);
			mm.addFunction(sceKernelGetModuleIdFunction, 0xF0A26395);
			mm.addFunction(sceKernelGetModuleIdByAddressFunction, 0xD8B73127);

		}
	}

	@Override
	public void uninstallModule(HLEModuleManager mm, int version) {
		if (version >= 150) {

			mm.removeFunction(sceKernelLoadModuleByIDFunction);
			mm.removeFunction(sceKernelLoadModuleFunction);
			mm.removeFunction(sceKernelLoadModuleMsFunction);
			mm.removeFunction(sceKernelLoadModuleBufferUsbWlanFunction);
			mm.removeFunction(sceKernelStartModuleFunction);
			mm.removeFunction(sceKernelStopModuleFunction);
			mm.removeFunction(sceKernelUnloadModuleFunction);
			mm.removeFunction(sceKernelSelfStopUnloadModuleFunction);
			mm.removeFunction(sceKernelStopUnloadSelfModuleFunction);
			mm.removeFunction(sceKernelGetModuleIdListFunction);
			mm.removeFunction(sceKernelQueryModuleInfoFunction);
			mm.removeFunction(sceKernelGetModuleIdFunction);
			mm.removeFunction(sceKernelGetModuleIdByAddressFunction);

		}
	}


	public void sceKernelLoadModuleByID(Processor processor) {
		CpuState cpu = processor.cpu; // New-Style Processor
		// Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;

		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceKernelLoadModuleByID [0xB7F46618]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}

	public void sceKernelLoadModule(Processor processor) {
		CpuState cpu = processor.cpu; // New-Style Processor
		Memory mem = Processor.memory;

        int path_addr = cpu.gpr[4] & 0x3fffffff;
        int flags = cpu.gpr[5];
        int option_addr = cpu.gpr[6] & 0x3fffffff;
        String name = Utilities.readStringZ(mem.mainmemory, path_addr - MemoryMap.START_RAM);
        Modules.log.warn("PARTIAL:sceKernelLoadModule(path='" + name
            + "',flags=0x" + Integer.toHexString(flags)
            + ",option=0x" + Integer.toHexString(option_addr) + ")");

        if (name.startsWith("flash0:")) {
        	// Simulate a successful loading
            Modules.log.warn("IGNORED:sceKernelLoadModule(path='" + name + "'): module from flash0 not loaded");
    		cpu.gpr[2] = SceModule.flashModuleUid;
    		return;
        }
        int findprx = name.lastIndexOf("/");        
        int endprx = name.indexOf(".PRX");
        if(endprx==-1) endprx=name.indexOf(".prx");
        String prxname = name.substring(findprx+1,endprx);
        for(banlist a : banlist.values())
        {
          if(a.name().matches(prxname))
          {
              Modules.log.warn("IGNORED:sceKernelLoadModule(path='" + name + "'): module from banlist not loaded");
              cpu.gpr[2] = SceModule.flashModuleUid;
              return;          
          }
        }
        
        cpu.gpr[2] = -1;
        try {
            SeekableDataInput moduleInput = pspiofilemgr.get_instance().getFile(name, flags);
            if (moduleInput != null) {
    	        byte[] moduleBytes = new byte[(int) moduleInput.length()];
    	        moduleInput.readFully(moduleBytes);
    	        ByteBuffer moduleBuffer = ByteBuffer.wrap(moduleBytes);
    	        int loadSize = moduleBytes.length;
    	        int loadBase = pspSysMem.get_instance().malloc(2, pspSysMem.PSP_SMEM_Low, loadSize, 0);
    	        FileManager moduleFileManager = new FileManager(moduleBuffer, loadBase);

    	        // The ELF sections (ShType.NOBITS) might have required additional memory
    	        if (moduleFileManager.getLoadAddressHigh() > loadBase + loadSize) {
    	        	int additionalSize = moduleFileManager.getLoadAddressHigh() - (loadBase + loadSize);
    	        	int additionalBase = pspSysMem.get_instance().malloc(2, pspSysMem.PSP_SMEM_Low, additionalSize, 0);
    	        	if (additionalBase != 0) {
    	        		loadSize += additionalSize;
    	        	}
    	        }
    	        pspSysMem.get_instance().addSysMemInfo(2, name, pspSysMem.PSP_SMEM_Low, loadSize, loadBase);

    	        if (moduleFileManager.getType() == FileManager.FORMAT_ELF) {
                    Emulator.initRamBy(moduleFileManager, moduleFileManager.getElf32());

                    SceModule sceModule = new SceModule();
                    sceModule.setName(name);
                    sceModule.setAttr(moduleFileManager.getPSPModuleInfo().getM_attr());
                    sceModule.setStartAddr((int) (moduleFileManager.getBaseoffset() + moduleFileManager.getElf32().getHeader().getE_entry()));
                    HLEModuleManager.getInstance().addSceModule(sceModule);

                    cpu.gpr[2] = sceModule.getUid();
                } else if (moduleFileManager.getType() == FileManager.FORMAT_PSP) {
                	// Simulate a successful loading
                    Modules.log.warn("IGNORED:sceKernelLoadModule(path='" + name + "'): module in PSP format not loaded");
                	cpu.gpr[2] = SceModule.flashModuleUid;
                } else {
                    pspSysMem.get_instance().free(loadBase);
                    cpu.gpr[2] = -1;
                }

    	        moduleInput.close();
            }
        } catch (IOException e) {
        	Modules.log.error("sceKernelLoadModule - Error while loading module " + name + ": " + e.getMessage());
        }
	}

	public void sceKernelLoadModuleMs(Processor processor) {
		CpuState cpu = processor.cpu; // New-Style Processor
		// Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;

		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceKernelLoadModuleMs [0x710F61B5]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}

	public void sceKernelLoadModuleBufferUsbWlan(Processor processor) {
		CpuState cpu = processor.cpu; // New-Style Processor
		// Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;

		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceKernelLoadModuleBufferUsbWlan [0xF9275D98]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}

	public void sceKernelStartModule(Processor processor) {
		CpuState cpu = processor.cpu; // New-Style Processor
		// Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;

        int uid = cpu.gpr[4];
        int argsize = cpu.gpr[5];
        int argp_addr = cpu.gpr[6];
        int status_addr = cpu.gpr[7];
        int option_addr = cpu.gpr[8]; // SceKernelSMOption

        Modules.log.warn("UNIMPLEMENTED:sceKernelStartModule(uid=0x" + Integer.toHexString(uid)
            + ",argsize=" + argsize
            + ",argp=0x" + Integer.toHexString(argp_addr)
            + ",status=0x" + Integer.toHexString(status_addr)
            + ",option=0x" + Integer.toHexString(option_addr) + ")");

        if (uid == SceModule.flashModuleUid) {
        	// Trying to start a module loaded from flash0:
        	// Do nothing...
    		cpu.gpr[2] = 0;
        	return;
        }
        SceModule sceModule = HLEModuleManager.getInstance().getSceModuleByUid(uid);
        if (sceModule == null) {
            cpu.gpr[2] = -1;
            Modules.log.error("sceKernelStartModule - unknown module UID " + uid);
            return;
        }

        ThreadMan.get_instance().createThread("module" + Integer.toHexString(uid), sceModule.getStartAddr(), 0, 0x4000, sceModule.getAttr(), option_addr, true, argsize, argp_addr);

        cpu.gpr[2] = 0;
	}

	public void sceKernelStopModule(Processor processor) {
		CpuState cpu = processor.cpu; // New-Style Processor
		// Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;

		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceKernelStopModule [0xD1FF982A]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}

	public void sceKernelUnloadModule(Processor processor) {
		CpuState cpu = processor.cpu; // New-Style Processor
		// Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;

		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceKernelUnloadModule [0x2E0911AA]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}

	public void sceKernelSelfStopUnloadModule(Processor processor) {
		CpuState cpu = processor.cpu; // New-Style Processor

        int unknown = cpu.gpr[4];
        int argsize = cpu.gpr[5];
        int argp_addr = cpu.gpr[6];

		Modules.log.debug("sceKernelSelfStopUnloadModule(unknown=0x" + Integer.toHexString(unknown)
            + ",argsize=" + argsize
            + ",argp_addr=0x" + Integer.toHexString(argp_addr) + ")");

        Modules.log.info("Program exit detected (sceKernelSelfStopUnloadModule)");
        Emulator.PauseEmuWithStatus(Emulator.EMU_STATUS_OK);

		cpu.gpr[2] = 0;
	}

	public void sceKernelStopUnloadSelfModule(Processor processor) {
		CpuState cpu = processor.cpu; // New-Style Processor
		// Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;

		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceKernelStopUnloadSelfModule [0xCC1D3699]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}

	public void sceKernelGetModuleIdList(Processor processor) {
		CpuState cpu = processor.cpu; // New-Style Processor
		// Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;

		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceKernelGetModuleIdList [0x644395E2]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}

	public void sceKernelQueryModuleInfo(Processor processor) {
		CpuState cpu = processor.cpu; // New-Style Processor
		// Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;

		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceKernelQueryModuleInfo [0x748CBED9]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}

	public void sceKernelGetModuleId(Processor processor) {
		CpuState cpu = processor.cpu; // New-Style Processor
		// Processor cpu = processor; // Old-Style Processor
		Memory mem = Processor.memory;

		/* put your own code here instead */

		// int a0 = cpu.gpr[4];  int a1 = cpu.gpr[5];  ...  int t3 = cpu.gpr[11];
		// float f12 = cpu.fpr[12];  float f13 = cpu.fpr[13];  ... float f19 = cpu.fpr[19];

		System.out.println("Unimplemented NID function sceKernelGetModuleId [0xF0A26395]");

		cpu.gpr[2] = 0xDEADC0DE;

		// cpu.gpr[2] = (int)(result & 0xffffffff);  cpu.gpr[3] = (int)(result  32); cpu.fpr[0] = result;
	}

	public void sceKernelGetModuleIdByAddress(Processor processor) {
		CpuState cpu = processor.cpu; // New-Style Processor

        int addr = cpu.gpr[4];
        Modules.log.debug("UNIMPLEMENTED:sceKernelGetModuleIdByAddress(addr=0x" + Integer.toHexString(addr) + ")");

        // Just return > 0 so it thinks we found the module
		cpu.gpr[2] = 1;
	}

	public final HLEModuleFunction sceKernelLoadModuleByIDFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelLoadModuleByID") {
		@Override
		public final void execute(Processor processor) {
			sceKernelLoadModuleByID(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelLoadModuleByID(processor);";
		}
	};

	public final HLEModuleFunction sceKernelLoadModuleFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelLoadModule") {
		@Override
		public final void execute(Processor processor) {
			sceKernelLoadModule(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelLoadModule(processor);";
		}
	};

	public final HLEModuleFunction sceKernelLoadModuleMsFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelLoadModuleMs") {
		@Override
		public final void execute(Processor processor) {
			sceKernelLoadModuleMs(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelLoadModuleMs(processor);";
		}
	};

	public final HLEModuleFunction sceKernelLoadModuleBufferUsbWlanFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelLoadModuleBufferUsbWlan") {
		@Override
		public final void execute(Processor processor) {
			sceKernelLoadModuleBufferUsbWlan(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelLoadModuleBufferUsbWlan(processor);";
		}
	};

	public final HLEModuleFunction sceKernelStartModuleFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelStartModule") {
		@Override
		public final void execute(Processor processor) {
			sceKernelStartModule(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelStartModule(processor);";
		}
	};

	public final HLEModuleFunction sceKernelStopModuleFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelStopModule") {
		@Override
		public final void execute(Processor processor) {
			sceKernelStopModule(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelStopModule(processor);";
		}
	};

	public final HLEModuleFunction sceKernelUnloadModuleFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelUnloadModule") {
		@Override
		public final void execute(Processor processor) {
			sceKernelUnloadModule(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelUnloadModule(processor);";
		}
	};

	public final HLEModuleFunction sceKernelSelfStopUnloadModuleFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelSelfStopUnloadModule") {
		@Override
		public final void execute(Processor processor) {
			sceKernelSelfStopUnloadModule(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelSelfStopUnloadModule(processor);";
		}
	};

	public final HLEModuleFunction sceKernelStopUnloadSelfModuleFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelStopUnloadSelfModule") {
		@Override
		public final void execute(Processor processor) {
			sceKernelStopUnloadSelfModule(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelStopUnloadSelfModule(processor);";
		}
	};

	public final HLEModuleFunction sceKernelGetModuleIdListFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelGetModuleIdList") {
		@Override
		public final void execute(Processor processor) {
			sceKernelGetModuleIdList(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelGetModuleIdList(processor);";
		}
	};

	public final HLEModuleFunction sceKernelQueryModuleInfoFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelQueryModuleInfo") {
		@Override
		public final void execute(Processor processor) {
			sceKernelQueryModuleInfo(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelQueryModuleInfo(processor);";
		}
	};

	public final HLEModuleFunction sceKernelGetModuleIdFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelGetModuleId") {
		@Override
		public final void execute(Processor processor) {
			sceKernelGetModuleId(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelGetModuleId(processor);";
		}
	};

	public final HLEModuleFunction sceKernelGetModuleIdByAddressFunction = new HLEModuleFunction("ModuleMgrForUser", "sceKernelGetModuleIdByAddress") {
		@Override
		public final void execute(Processor processor) {
			sceKernelGetModuleIdByAddress(processor);
		}
		@Override
		public final String compiledString() {
			return "jpcsp.HLE.Modules.ModuleMgrForUserModule.sceKernelGetModuleIdByAddress(processor);";
		}
	};

};
