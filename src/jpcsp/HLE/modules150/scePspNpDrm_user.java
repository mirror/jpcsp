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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import jpcsp.GeneralJpcspException;
import jpcsp.HLE.CanBeNull;
import jpcsp.HLE.HLEFunction;
import jpcsp.HLE.HLELogging;
import jpcsp.HLE.HLEUnimplemented;
import jpcsp.HLE.PspString;
import jpcsp.HLE.TPointer;
import jpcsp.Emulator;
import jpcsp.Loader;
import jpcsp.connector.DLCConnector;
import jpcsp.crypto.CryptoEngine;
import jpcsp.filesystems.SeekableDataInput;
import jpcsp.filesystems.SeekableRandomFile;
import jpcsp.HLE.kernel.types.SceKernelErrors;
import jpcsp.HLE.kernel.types.SceKernelLMOption;
import jpcsp.HLE.kernel.types.SceModule;
import jpcsp.HLE.Modules;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.HLE.modules150.IoFileMgrForUser.IoInfo;
import jpcsp.settings.AbstractBoolSettingsListener;

import org.apache.log4j.Logger;

@HLELogging
public class scePspNpDrm_user extends HLEModule {

    public static Logger log = Modules.getLogger("scePspNpDrm_user");

    @Override
    public String getName() {
        return "scePspNpDrm_user";
    }
    
    @Override
    public void start() {
        setSettingsListener("emu.disableDLC", new DisableDLCSettingsListerner());
        super.start();
    }
    
    public static final int PSP_NPDRM_KEY_LENGHT = 0x10;
    private byte npDrmKey[] = new byte[PSP_NPDRM_KEY_LENGHT];
    private boolean isNpDrmKeySet = false;
    private DLCConnector dlcConnector;
    private boolean disableDLCDecryption;
    
    public void setDisableDLCStatus(boolean status) {
        disableDLCDecryption = status;
    }

    public boolean getDisableDLCStatus() {
        return disableDLCDecryption;
    }

    protected void setNpDrmKeyStatus(boolean status) {
        isNpDrmKeySet = status;
    }

    protected boolean getNpDrmKeyStatus() {
        return isNpDrmKeySet;
    }
    
    private class DisableDLCSettingsListerner extends AbstractBoolSettingsListener {
        @Override
        protected void settingsValueChanged(boolean value) {
            setDisableDLCStatus(value);
        }
    }

    @HLEFunction(nid = 0xA1336091, version = 150, checkInsideInterrupt = true)
    public int sceNpDrmSetLicenseeKey(TPointer npDrmKeyAddr) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < PSP_NPDRM_KEY_LENGHT; i++) {
            npDrmKey[i] = (byte) npDrmKeyAddr.getValue8(i);
            key.append(String.format("%02X", npDrmKey[i] & 0xFF));
        }
        setNpDrmKeyStatus(true);
        log.info(String.format("NPDRM Encryption key detected: 0x%s", key.toString()));

        // Start the DLC connector.
        if (dlcConnector == null) {
            dlcConnector = new DLCConnector();
        }

        return 0;
    }

    @HLEFunction(nid = 0x9B745542, version = 150, checkInsideInterrupt = true)
    public int sceNpDrmClearLicenseeKey() {
        Arrays.fill(npDrmKey, (byte) 0);
        setNpDrmKeyStatus(false);

        return 0;
    }

    @HLEFunction(nid = 0x275987D1, version = 150, checkInsideInterrupt = true)
    public int sceNpDrmRenameCheck(PspString fileName) {
        CryptoEngine crypto = new CryptoEngine();
        int result = 0;

        if (!getNpDrmKeyStatus()) {
            result = SceKernelErrors.ERROR_NPDRM_NO_K_LICENSEE_SET;
        } else {
            try {
                String pcfilename = Modules.IoFileMgrForUserModule.getDeviceFilePath(fileName.getString());
                SeekableRandomFile file = new SeekableRandomFile(pcfilename, "r");

                String[] name = pcfilename.split("/");
                String fName = "";
                for (int i = 0; i < name.length; i++) {
                    if (name[i].contains("EDAT")) {
                        fName = name[i];
                    }
                }

                // The file must contain a valid PSPEDAT header.
                if (file.length() < 0x80) {
                    // Test if we're using already decrypted DLC.
                    // Discard the error in this situatuion.
                    if (!getDisableDLCStatus()) {
                        log.warn("sceNpDrmRenameCheck: invalid file size");
                        result = SceKernelErrors.ERROR_NPDRM_INVALID_FILE;
                    }
                    file.close();
                } else {
                    // Setup the buffers.
                    byte[] inBuf = new byte[0x80];
                    byte[] srcData = new byte[0x30];
                    byte[] srcHash = new byte[0x10];

                    // Read the header.
                    file.readFully(inBuf);
                    file.close();

                    // The data seed is stored at offset 0x10 of the PSPEDAT header.
                    System.arraycopy(inBuf, 0x10, srcData, 0, 0x30);

                    // The hash to compare is stored at offset 0x40 of the PSPEDAT header.
                    System.arraycopy(inBuf, 0x40, srcHash, 0, 0x10);

                    // If the CryptoEngine fails to find a match, then the file has been renamed.
                    if (!crypto.getPGDEngine().CheckEDATRenameKey(fName.getBytes(), srcHash, srcData)) {
                        if (!getDisableDLCStatus()) {
                            result = SceKernelErrors.ERROR_NPDRM_NO_FILENAME_MATCH;
                            log.warn("sceNpDrmRenameCheck: the file has been renamed");
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                result = SceKernelErrors.ERROR_NPDRM_INVALID_FILE;
                if (log.isDebugEnabled()) {
                    log.debug(String.format("sceNpDrmRenameCheck: file '%s' not found: %s", fileName.getString(), e.toString()));
                }
            } catch (Exception e) {
                log.error("sceNpDrmRenameCheck", e);
            }
        }

        return result;
    }

    @HLELogging(level = "info")
    @HLEFunction(nid = 0x08D98894, version = 150, checkInsideInterrupt = true)
    public int sceNpDrmEdataSetupKey(int edataFd) {
        // Return an error if the key has not been set.
        // Note: An empty key is valid, as long as it was set with sceNpDrmSetLicenseeKey.
        if (!getNpDrmKeyStatus()) {
            return SceKernelErrors.ERROR_NPDRM_NO_K_LICENSEE_SET;
        }

        IoInfo info = Modules.IoFileMgrForUserModule.getFileIoInfo(edataFd);
        if (info == null) {
            return 0;
        }

        CryptoEngine crypto = new CryptoEngine();
        int result = 0;

        // Generate the necessary directories and file paths.
        String dlcPath = dlcConnector.getDLCPath(info.filename);
        String decFileName = dlcConnector.getDecryptedDLCPath(info.filename);

        // Check we're using already decrypted DLC.
        if (!getDisableDLCStatus()) {
            // Try to decrypt the data with the Crypto Engine.
            try {
                // PGD header size.
                int pgdHeaderSize = 0x90;

                // PGD data header offset.
                int pgdHeaderDataOffset = 0x30;

                // PGD data offset in EDAT file.
                int edatPGDOffset = 0x90;

                // Setup the buffers.
                byte[] fileBuf = new byte[(int) info.readOnlyFile.length()];
                byte[] encHeaderBuf = new byte[pgdHeaderSize];

                // Read the encrypted file.
                long startPosition = info.readOnlyFile.getFilePointer();
                info.readOnlyFile.readFully(fileBuf);
                info.readOnlyFile.seek(startPosition);

                // Check if the "PSPEDAT" header is present
                if (fileBuf[0] != 0 || fileBuf[1] != 'P' || fileBuf[2] != 'S' || fileBuf[3] != 'P'
                        || fileBuf[4] != 'E' || fileBuf[5] != 'D' || fileBuf[6] != 'A' || fileBuf[7] != 'T') {
                    // No "EDAT" found in the header,
                    // abort the decryption and leave the file unchanged
                    log.warn("PSPEDAT header not found!");
                    return 0;
                }

                // Extract the encrypted PGD header.
                System.arraycopy(fileBuf, edatPGDOffset, encHeaderBuf, 0, pgdHeaderSize);

                // Get the decryption key from the PGD header and then decrypt the header.
                byte[] decKey = crypto.getPGDEngine().GetEDATPGDKey(encHeaderBuf, pgdHeaderSize);
                byte[] decHeader = crypto.getPGDEngine().DecryptEDATPGDHeader(encHeaderBuf, pgdHeaderSize, decKey);

                // Copy back the decrypted header.
                System.arraycopy(decHeader, 0, fileBuf, edatPGDOffset + pgdHeaderDataOffset, decHeader.length);

                // Extract the decrypting parameters.
                int dataSize = (decHeader[0x14] & 0xFF) | ((decHeader[0x15] & 0xFF) << 8)
                        | ((decHeader[0x16] & 0xFF) << 16) | ((decHeader[0x17] & 0xFF) << 24);
                int chunkSize = (decHeader[0x18] & 0xFF) | ((decHeader[0x19] & 0xFF) << 8)
                        | ((decHeader[0x1A] & 0xFF) << 16) | ((decHeader[0x1B] & 0xFF) << 24);
                int hashOffset = (decHeader[0x1C] & 0xFF) | ((decHeader[0x1D] & 0xFF) << 8)
                        | ((decHeader[0x1E] & 0xFF) << 16) | ((decHeader[0x1F] & 0xFF) << 24);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("PGD dataSize=0x%08x, chunkSize=0x%08x, hashOffset=0x%08x", dataSize, chunkSize, hashOffset));
                }

                byte[] inBuf = new byte[fileBuf.length - edatPGDOffset];
                System.arraycopy(fileBuf, edatPGDOffset, inBuf, 0, inBuf.length);

                byte[] outBuf = crypto.getPGDEngine().DecryptEDATPGD(inBuf, dataSize, hashOffset, chunkSize, decKey);

                if (hashOffset < 0 || dataSize < 0) {
                    log.warn(String.format("Incorrect PGD header: dataSize=%d, chunkSize=%d, hashOffset=%d", dataSize, chunkSize, hashOffset));
                    result = SceKernelErrors.ERROR_PGD_INVALID_HEADER;
                } else {
                    // Create a new file for decryption.
                    new File(dlcPath).mkdirs();
                    SeekableRandomFile decFile = new SeekableRandomFile(decFileName, "rw");

                    decFile.write(outBuf);
                    decFile.close();
                }
            } catch (Exception e) {
                // Ignore.
            }

            try {
                info.readOnlyFile.seek(info.position);
            } catch (Exception e) {
                // Ignore.
            }

            // Load the manually decrypted file generated just now.
            info.readOnlyFile = dlcConnector.loadDecryptedDLCFile(decFileName);
        }

        return result;
    }

    @HLEFunction(nid = 0x219EF5CC, version = 150, checkInsideInterrupt = true)
    public int sceNpDrmEdataGetDataSize(int edataFd) {
        IoInfo info = Modules.IoFileMgrForUserModule.getFileIoInfo(edataFd);
        int size = 0;
        if (info != null) {
            if (info.readOnlyFile != null) {
                try {
                    size = (int) info.readOnlyFile.length();
                } catch (IOException e) {
                    log.error("sceNpDrmEdataGetDataSize", e);
                }
            }
        }

        return size;
    }

    @HLEUnimplemented
    @HLEFunction(nid = 0x2BAA4294, version = 150, checkInsideInterrupt = true)
    public int sceNpDrmOpen(PspString name, int flags, int permissions) {
        if (!getNpDrmKeyStatus()) {
            return SceKernelErrors.ERROR_NPDRM_NO_K_LICENSEE_SET;
        }
        // Open the file with flags ORed with PSP_O_FGAMEDATA and send it to the IoFileMgr.
        int fd = Modules.IoFileMgrForUserModule.hleIoOpen(name, flags | 0x40000000, permissions, true);
        return sceNpDrmEdataSetupKey(fd);
    }

    @HLEFunction(nid = 0xC618D0B1, version = 150, checkInsideInterrupt = true)
    public int sceKernelLoadModuleNpDrm(PspString path, int flags, @CanBeNull TPointer optionAddr) {
        SceKernelLMOption lmOption;
        if (optionAddr.isNotNull()) {
            lmOption = new SceKernelLMOption();
            lmOption.read(optionAddr);
            if (log.isInfoEnabled()) {
                log.info(String.format("sceKernelLoadModuleNpDrm partition=%d, position=%d", lmOption.mpidText, lmOption.position));
            }
        }

        // SPRX modules can't be decrypted yet.
        if (!getDisableDLCStatus()) {
            log.warn(String.format("sceKernelLoadModuleNpDrm detected encrypted DLC module: %s", path.getString()));
            return SceKernelErrors.ERROR_NPDRM_INVALID_PERM;
        } else {
            return Modules.ModuleMgrForUserModule.hleKernelLoadModule(path.getString(), flags, 0, false);
        }
    }

    @HLEFunction(nid = 0xAA5FC85B, version = 150, checkInsideInterrupt = true)
    public int sceKernelLoadExecNpDrm(PspString fileName, @CanBeNull TPointer optionAddr) {
        // Flush system memory to mimic a real PSP reset.
        Modules.SysMemUserForUserModule.reset();

        if (optionAddr.isNotNull()) {
            int optSize = optionAddr.getValue32(0);  // Size of the option struct.
            int argSize = optionAddr.getValue32(4);  // Number of args (strings).
            int argAddr = optionAddr.getValue32(8);  // Pointer to a list of strings.
            int keyAddr = optionAddr.getValue32(12); // Pointer to an encryption key (may not be used).

            if (log.isDebugEnabled()) {
                log.debug(String.format("sceKernelLoadExecNpDrm (params: optSize=%d, argSize=%d, argAddr=0x%08X, keyAddr=0x%08X)", optSize, argSize, argAddr, keyAddr));
            }
        }

        // SPRX modules can't be decrypted yet.
        if (!getDisableDLCStatus()) {
            log.warn(String.format("sceKernelLoadModuleNpDrm detected encrypted DLC module: %s", fileName.getString()));
            return SceKernelErrors.ERROR_NPDRM_INVALID_PERM;
        } else {
            int result;
            try {
                SeekableDataInput moduleInput = Modules.IoFileMgrForUserModule.getFile(fileName.getString(), IoFileMgrForUser.PSP_O_RDONLY);
                if (moduleInput != null) {
                    byte[] moduleBytes = new byte[(int) moduleInput.length()];
                    moduleInput.readFully(moduleBytes);
                    moduleInput.close();
                    ByteBuffer moduleBuffer = ByteBuffer.wrap(moduleBytes);

                    SceModule module = Emulator.getInstance().load(fileName.getString(), moduleBuffer, true);
                    Emulator.getClock().resume();

                    if ((module.fileFormat & Loader.FORMAT_ELF) == Loader.FORMAT_ELF) {
                        result = 0;
                    } else {
                        log.warn("sceKernelLoadExecNpDrm - failed, target is not an ELF");
                        result = SceKernelErrors.ERROR_KERNEL_ILLEGAL_LOADEXEC_FILENAME;
                    }
                } else {
                    result = SceKernelErrors.ERROR_KERNEL_PROHIBIT_LOADEXEC_DEVICE;
                }
            } catch (GeneralJpcspException e) {
                log.error("sceKernelLoadExecNpDrm", e);
                result = SceKernelErrors.ERROR_KERNEL_PROHIBIT_LOADEXEC_DEVICE;
            } catch (IOException e) {
                log.error(String.format("sceKernelLoadExecNpDrm - Error while loading module '%s'", fileName), e);
                result = SceKernelErrors.ERROR_KERNEL_PROHIBIT_LOADEXEC_DEVICE;
            }

            return result;
        }
    }
}
