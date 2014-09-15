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
package jpcsp.HLE.modules620;

import jpcsp.HLE.HLEFunction;
import jpcsp.HLE.HLELogging;
import jpcsp.HLE.TPointer;
import jpcsp.HLE.TPointer32;
import jpcsp.HLE.kernel.types.SceKernelErrors;

@HLELogging
public class sceMp3 extends jpcsp.HLE.modules150.sceMp3 {
	@HLEFunction(nid = 0x1B839B83 , version = 620)
    public int sceMp3LowLevelInit(Mp3Info mp3Stream, int unknown) {
    	// Always output in stereo, even if the input is mono
		mp3Stream.getCodec().init(0, 2, 2, 0);

		return 0;
	}

	@HLEFunction(nid = 0xE3EE2C81, version = 620)
    public int sceMp3LowLevelDecode(Mp3Info mp3Stream, TPointer sourceAddr, TPointer32 sourceBytesConsumedAddr, TPointer samplesAddr, TPointer32 sampleBytesAddr) {
		int result = mp3Stream.getCodec().decode(sourceAddr.getAddress(), 10000, samplesAddr.getAddress());
		if (log.isDebugEnabled()) {
			log.debug(String.format("sceMp3LowLevelDecode result=0x%08X, samples=0x%X", result, mp3Stream.getCodec().getNumberOfSamples()));
		}
		if (result < 0) {
			return SceKernelErrors.ERROR_MP3_LOW_LEVEL_DECODING_ERROR;
		}

		sourceBytesConsumedAddr.setValue(result);
		sampleBytesAddr.setValue(mp3Stream.getCodec().getNumberOfSamples() * 4);

		return 0;
	}
}
