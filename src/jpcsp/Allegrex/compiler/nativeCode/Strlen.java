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
package jpcsp.Allegrex.compiler.nativeCode;

import static jpcsp.Allegrex.Common._a0;
import static jpcsp.Allegrex.Common._a1;
import static jpcsp.Allegrex.Common._a2;

/**
 * @author gid15
 *
 */
public class Strlen extends AbstractNativeCodeSequence {
	static public void call(int lastUpdatedRegister) {
		int srcAddr = getGprA0();

		int srcLength = getStrlen(srcAddr);
		setGprV0(srcLength);

		// Some games are also assuming that the other registers
		// have been modified... dirty programming
		if (lastUpdatedRegister >= _a0) {
			getCpu()._a0 = srcAddr + srcLength;
			if (lastUpdatedRegister >= _a1) {
				getCpu()._a1 = srcAddr;
				if (lastUpdatedRegister >= _a2) {
					getCpu()._a2 = 0;
				}
			}
		}
	}
}
