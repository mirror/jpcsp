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
package jpcsp.HLE.modules200;

import jpcsp.HLE.HLEFunction;
import jpcsp.HLE.HLELogging;
import jpcsp.HLE.TPointer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import jpcsp.Emulator;
import jpcsp.GeneralJpcspException;
import jpcsp.Loader;
import jpcsp.HLE.Modules;
import jpcsp.HLE.kernel.Managers;
import jpcsp.HLE.kernel.types.SceKernelErrors;
import jpcsp.HLE.kernel.types.SceModule;
import jpcsp.HLE.kernel.types.SceUtilityInstallParams;
import jpcsp.HLE.kernel.types.pspUtilityBaseDialog;
import jpcsp.HLE.modules.HLEModuleManager;
import jpcsp.HLE.modules.IoFileMgrForUser;
import jpcsp.HLE.modules.ModuleMgrForUser;
import jpcsp.filesystems.SeekableDataInput;

@HLELogging
public class sceUtility extends jpcsp.HLE.modules150.sceUtility {
    protected static class InstallUtilityDialogState extends UtilityDialogState {
		protected SceUtilityInstallParams installParams;

    	public InstallUtilityDialogState(String name) {
			super(name);
		}

		@Override
		protected boolean executeUpdateVisible() {
			boolean keepVisible = false;

			log.warn(String.format("Partial sceUtilityInstallUpdate %s", installParams.toString()));

			// We only get the game name from the install params. Is the rest fixed?
			String fileName = String.format("ms0:/PSP/GAME/%s/EBOOT.PBP", installParams.gameName);
	        try {
	            SeekableDataInput moduleInput = Modules.IoFileMgrForUserModule.getFile(fileName, IoFileMgrForUser.PSP_O_RDONLY);
	            if (moduleInput != null) {
	                byte[] moduleBytes = new byte[(int) moduleInput.length()];
	                moduleInput.readFully(moduleBytes);
	                ByteBuffer moduleBuffer = ByteBuffer.wrap(moduleBytes);

	    			// TODO How is this module being loaded?
	                // Does it unload the current module? i.e. re-init the PSP
	                SceModule module = Emulator.getInstance().load(name, moduleBuffer, true);
	                Emulator.getClock().resume();

	                if ((module.fileFormat & Loader.FORMAT_ELF) == Loader.FORMAT_ELF) {
	                	installParams.base.result = 0;
	                	keepVisible = false;
	                } else {
	                    log.warn("sceUtilityInstall - failed, target is not an ELF");
	                    installParams.base.result = -1;
	                }
	                moduleInput.close();
	            }
	        } catch (GeneralJpcspException e) {
	            log.error("General Error : " + e.getMessage());
	            Emulator.PauseEmu();
	        } catch (IOException e) {
	            log.error(String.format("sceUtilityInstall - Error while loading module %s: %s", fileName, e.getMessage()));
	            installParams.base.result = -1;
	        }

			return keepVisible;
		}

		@Override
		protected pspUtilityBaseDialog createParams() {
			installParams = new SceUtilityInstallParams();
			return installParams;
		}

        @Override
        protected boolean hasDialog() {
            return false;
        }
    }

    public static final String[] utilityNetModuleNames = new String[] {
        "PSP_NET_MODULE_UNKNOWN(1)",
        "PSP_NET_MODULE_COMMON",
        "PSP_NET_MODULE_ADHOC",
        "PSP_NET_MODULE_INET",
        "PSP_NET_MODULE_PARSEURI",
        "PSP_NET_MODULE_PARSEHTTP",
        "PSP_NET_MODULE_HTTP",
        "PSP_NET_MODULE_SSL",
    };

    public static final int PSP_NET_MODULE_COMMON = 1;
    public static final int PSP_NET_MODULE_ADHOC = 2;
    public static final int PSP_NET_MODULE_INET = 3;
    public static final int PSP_NET_MODULE_PARSEURI = 4;
    public static final int PSP_NET_MODULE_PARSEHTTP = 5;
    public static final int PSP_NET_MODULE_HTTP = 6;
    public static final int PSP_NET_MODULE_SSL = 7;

    protected HashMap<Integer, SceModule> loadedNetModules = new HashMap<Integer, SceModule>();
    protected HashMap<Integer, String> waitingNetModules = new HashMap<Integer, String>();
    protected InstallUtilityDialogState installState;

	@Override
	public void start() {
		super.start();

		installState = new InstallUtilityDialogState("sceUtilityInstall");
	}

	@Override
	public void stop() {
		loadedNetModules.clear();
		waitingNetModules.clear();
		super.stop();
	}

	private String getNetModuleName(int module) {
    	if (module < 0 || module >= utilityNetModuleNames.length) {
    		return "PSP_NET_MODULE_UNKNOWN_" + module;
    	}
    	return utilityNetModuleNames[module];
    }

    protected int hleUtilityLoadNetModule(int module, String moduleName) {
        HLEModuleManager moduleManager = HLEModuleManager.getInstance();
    	if (loadedNetModules.containsKey(module) || waitingNetModules.containsKey(module)) { // Module already loaded.
    		return SceKernelErrors.ERROR_NET_MODULE_ALREADY_LOADED;
    	} else if (!moduleManager.hasFlash0Module(moduleName)) { // Can't load flash0 module.
            waitingNetModules.put(module, moduleName); // Always save a load attempt.
            return SceKernelErrors.ERROR_NET_MODULE_BAD_ID;
    	} else {
            // Load and save it in loadedNetModules.
            int sceModuleId = moduleManager.LoadFlash0Module(moduleName);
            SceModule sceModule = Managers.modules.getModuleByUID(sceModuleId);
            loadedNetModules.put(module, sceModule);
            return 0;
        }
    }

    protected int hleUtilityUnloadNetModule(int module) {
        if (loadedNetModules.containsKey(module)) {
            // Unload the module.
            HLEModuleManager moduleManager = HLEModuleManager.getInstance();
            SceModule sceModule = loadedNetModules.remove(module);
            moduleManager.UnloadFlash0Module(sceModule);
            return 0;
        } else if (waitingNetModules.containsKey(module)) {
            // Simulate a successful unload.
            waitingNetModules.remove(module);
            return 0;
        } else {
            return SceKernelErrors.ERROR_NET_MODULE_NOT_LOADED;
        }
    }

    @HLEFunction(nid = 0x1579A159, version = 200, checkInsideInterrupt = true)
    public int sceUtilityLoadNetModule(int module) {
        String moduleName = getNetModuleName(module);
        int result = hleUtilityLoadNetModule(module, moduleName);
        if (result == SceKernelErrors.ERROR_NET_MODULE_BAD_ID) {
            log.info(String.format("IGNORING: sceUtilityLoadNetModule(module=0x%04X) %s", module, moduleName));
        	Modules.ThreadManForUserModule.hleKernelDelayThread(ModuleMgrForUser.loadHLEModuleDelay, false);
            return 0;
        }

        log.info(String.format("sceUtilityLoadNetModule(module=0x%04X) %s loaded", module, moduleName));

        if (result >= 0) {
        	Modules.ThreadManForUserModule.hleKernelDelayThread(ModuleMgrForUser.loadHLEModuleDelay, false);
        }

        return result;
    }

    @HLEFunction(nid = 0x64D50C56, version = 200, checkInsideInterrupt = true)
    public int sceUtilityUnloadNetModule(int module) {
        String moduleName = getNetModuleName(module);
        log.info(String.format("sceUtilityUnloadNetModule(module=0x%04X) %s unloaded", module, moduleName));

        return hleUtilityUnloadNetModule(module);
    }

    @HLEFunction(nid = 0x1281DA8E, version = 200)
    public int sceUtilityInstallInitStart(TPointer paramsAddr) {
        return installState.executeInitStart(paramsAddr);
    }

    @HLEFunction(nid = 0x5EF1C24A, version = 200)
    public int sceUtilityInstallShutdownStart() {
    	return installState.executeShutdownStart();
    }

    @HLEFunction(nid = 0xA03D29BA, version = 200)
    public int sceUtilityInstallUpdate(int drawSpeed) {
    	return installState.executeUpdate(drawSpeed);
    }

    @HLEFunction(nid = 0xC4700FA3, version = 200)
    public int sceUtilityInstallGetStatus() {
    	return installState.executeGetStatus();
    }
}