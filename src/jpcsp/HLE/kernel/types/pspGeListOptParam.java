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
package jpcsp.HLE.kernel.types;

public class pspGeListOptParam extends pspAbstractMemoryMappedStructureVariableLength {
    public int contextAddr;
    public int stackDepth;
    public int stackAddr;

    @Override
    protected void read() {
    	super.read();
        contextAddr = read32();
        stackDepth = read32();
        stackAddr = read32();
    }

    @Override
    protected void write() {
    	super.write();
        write32(contextAddr);
        write32(stackDepth);
        write32(stackAddr);
    }

	@Override
	public String toString() {
    	return String.format("0x%08X(contextAddr=0x%08X, stackDepth=%d, stackAddr=0x%08X)", getBaseAddress(), contextAddr, stackDepth, stackAddr);
	}
}