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
package jpcsp.filesystems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author gigaherz
 */
public class SeekableRandomFile extends RandomAccessFile implements SeekableDataInput {
	private String fileName;
	private String mode;

    public SeekableRandomFile(String fileName, String mode) throws FileNotFoundException {
        super(fileName, mode);
        this.fileName = fileName;
        this.mode = mode;
    }

    public SeekableRandomFile(File name, String mode) throws FileNotFoundException {
        super(name, mode);
        this.fileName = name.toString();
        this.mode = mode;
    }

    public String getFileName() {
    	return fileName;
    }

    public String getMode() {
    	return mode;
    }

    public SeekableRandomFile duplicate() throws IOException {
    	SeekableRandomFile duplicate = new SeekableRandomFile(fileName, mode);
    	duplicate.seek(getFilePointer());

    	return duplicate;
    }

    @Override
	public String toString() {
		return String.format("SeekableRandomFile '%s'", fileName);
	}
}