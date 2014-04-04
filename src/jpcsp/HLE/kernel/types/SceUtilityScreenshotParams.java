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

public class SceUtilityScreenshotParams extends pspAbstractMemoryMappedStructure {
    public pspUtilityDialogCommon base;
    public int startupType;
        public final static int PSP_UTILITY_SCREENSHOT_TYPE_GUI = 0;
        public final static int PSP_UTILITY_SCREENSHOT_TYPE_AUTO = 1;
        public final static int PSP_UTILITY_SCREENSHOT_TYPE_LIST_SAVE = 2;
        public final static int PSP_UTILITY_SCREENSHOT_TYPE_LIST_VIEW = 3;
        public final static int PSP_UTILITY_SCREENSHOT_TYPE_CONT_FINISH = 4;
        public final static int PSP_UTILITY_SCREENSHOT_TYPE_CONT_AUTO = 5;
    public int status;
        public final static int PSP_UTILITY_SCREENSHOT_STATUS_BUSY = 0;
        public final static int PSP_UTILITY_SCREENSHOT_STATUS_DONE = 1;
    public int imgFormat;
        public final static int PSP_UTILITY_SCREENSHOT_FORMAT_PNG = 1;
        public final static int PSP_UTILITY_SCREENSHOT_FORMAT_JPEG = 2;
    public int imgQuality;
    public int imgFrameBufAddr;
    public int imgFrameBufWidth;
    public int imgPixelFormat;
    public int screenshotOffsetX;
    public int screenshotOffsetY;
    public int displayWidth;
    public int displayHeigth;
    public String screenshotID;
    public String fileName;
    public int nameRule;
        public final static int PSP_UTILITY_SCREENSHOT_NAMERULE_NONE = 0;
        public final static int PSP_UTILITY_SCREENSHOT_NAMERULE_AUTONUM = 1;
    public String title;
    public int parentalLevel;
    public int pscmFileFlag;
        public final static int PSP_UTILITY_SCREENSHOT_PSCM_CREATE = 0;
        public final static int PSP_UTILITY_SCREENSHOT_PSCM_OVERWRITE = 1;
    public String iconPath;
    public int iconPathSize;
    public int iconFileSize;
    public String backgroundPath;
    public int backgroundPathSize;
    public int backgroundFileSize;
    public int commentFlag;
        public final static int PSP_UTILITY_SCREENSHOT_COMMENT_CREATE = 0;
        public final static int PSP_UTILITY_SCREENSHOT_COMMENT_DONT_CREATE = 1;
    public SceUtilityScreenshotCommentShape commentShape;
    public SceUtilityScreenshotCommentText commentText;

    public static class SceUtilityScreenshotCommentShape extends pspAbstractMemoryMappedStructure {
        public int width;
        public int heigth;
        public int backgroundColor;
        public int alignX;
        public int alignY;
        public int contentAlignY;
        public int marginTop;
        public int marginBottom;
        public int marginLeft;
        public int marginRight;
        public int paddingTop;
        public int paddingBottom;
        public int paddingLeft;
        public int paddingRight;

		@Override
		protected void read() {
            width = read32();
            heigth = read32();
            backgroundColor = read32();
            alignX = read32();
            alignY = read32();
            contentAlignY = read32();
            marginTop = read32();
            marginBottom = read32();
            marginLeft = read32();
            marginRight = read32();
            paddingTop = read32();
            paddingBottom = read32();
            paddingLeft = read32();
            paddingRight = read32();
		}

		@Override
		protected void write() {
            write32(width);
            write32(heigth);
            write32(backgroundColor);
            write32(alignX);
            write32(alignY);
            write32(contentAlignY);
            write32(marginTop);
            write32(marginBottom);
            write32(marginLeft);
            write32(marginRight);
            write32(paddingTop);
            write32(paddingBottom);
            write32(paddingLeft);
            write32(paddingRight);
		}

		@Override
		public int sizeof() {
			return 14 * 4;
		}

		@Override
		public String toString() {
			return String.format("width=%d, height=%d, backgroundColor=0x%08X, align [X=%d, Y=%d], margin [T=%d, B=%d, L=%d, R=%d], padding [T=%d, B=%d, L=%d, R=%d]", width, heigth, backgroundColor, alignX, alignY, marginTop, marginBottom, marginLeft, marginRight, paddingTop, paddingBottom, paddingLeft, paddingRight);
		}
	}

    public static class SceUtilityScreenshotCommentText extends pspAbstractMemoryMappedStructure {
        public int textColor;
        public int shadowType;
            public final static int PSP_UTILITY_SCREENSHOT_COMMENT_SHADOW_DEFAULT = 0;
            public final static int PSP_UTILITY_SCREENSHOT_COMMENT_SHADOW_ON = 1;
            public final static int PSP_UTILITY_SCREENSHOT_COMMENT_SHADOW_OFF = 2;
        public int shadowColor;
        public int fontSize;
            public final static int PSP_UTILITY_SCREENSHOT_COMMENT_FONT_SIZE_DEFAULT = 0;
            public final static int PSP_UTILITY_SCREENSHOT_COMMENT_FONT_SIZE_SMALL = 1;
            public final static int PSP_UTILITY_SCREENSHOT_COMMENT_FONT_SIZE_MEDIUM = 2;
            public final static int PSP_UTILITY_SCREENSHOT_COMMENT_FONT_SIZE_LARGE = 3;
        public int lineSpace;
        public int alignX;
        public String textComment;
        public int textCommentLength;

		@Override
		protected void read() {
            textColor = read32();
            shadowType = read32();
            shadowColor = read32();
            fontSize = read32();
            lineSpace = read32();
            alignX = read32();
            textComment = readStringUTF16NZ(256);
            textCommentLength = read32();
		}

		@Override
		protected void write() {
			write32(textColor);
            write32(shadowType);
            write32(shadowColor);
            write32(fontSize);
            write32(lineSpace);
            write32(alignX);
            textCommentLength = writeStringUTF16Z(256, textComment);
            write32(textCommentLength);
		}

		@Override
		public int sizeof() {
			return 7 * 4 + 256;
		}

		@Override
		public String toString() {
			return String.format("textColor=0x%08X, shadowType=%d, shadowColor=0x%08X, fontSize=%d, lineSpace=%d, alignX=%d, textComment='%s', textCommentLength=%d", textColor, shadowType, shadowColor, fontSize, lineSpace, alignX, textComment, textCommentLength);
		}
	}

    @Override
	protected void read() {
        base = new pspUtilityDialogCommon();
        read(base);
        setMaxSize(base.totalSizeof());

        startupType = read32();
        status = read32();
        imgFormat = read32();
        imgQuality = read32();
        imgFrameBufAddr = read32();
        imgFrameBufWidth = read32();
        imgPixelFormat = read32();
        screenshotOffsetX = read32();
        screenshotOffsetY = read32();
        displayWidth = read32();
        displayHeigth = read32();
        screenshotID = readStringNZ(12);
        fileName = readStringNZ(192);
        nameRule = read32();
        readUnknown(4);
        title = readStringNZ(128);
        parentalLevel = read32();
        pscmFileFlag = read32();
        iconPath = readStringNZ(64);
        iconPathSize = read32();
        iconFileSize = read32();
        backgroundPath = readStringNZ(64);
        backgroundPathSize = read32();
        backgroundFileSize = read32();
        commentFlag = read32();
        commentShape = new SceUtilityScreenshotCommentShape();
        read(commentShape);
        commentText = new SceUtilityScreenshotCommentText();
        read(commentText);
        readUnknown(4);
    }

    @Override
	protected void write() {
        write(base);
        setMaxSize(base.totalSizeof());

        write32(startupType);
        write32(status);
        write32(imgFormat);
        write32(imgQuality);
        write32(imgFrameBufAddr);
        write32(imgFrameBufWidth);
        write32(imgPixelFormat);
        write32(screenshotOffsetX);
        write32(screenshotOffsetY);
        write32(displayWidth);
        write32(displayHeigth);
        writeStringNZ(12, screenshotID);
        writeStringNZ(192, fileName);
        write32(nameRule);
        writeUnknown(4);
        writeStringNZ(128, title);
        write32(parentalLevel);
        write32(pscmFileFlag);
        writeStringNZ(64, iconPath);
        write32(iconPathSize);
        write32(iconFileSize);
        writeStringNZ(64, backgroundPath);
        write32(backgroundPathSize);
        write32(backgroundFileSize);
        write32(commentFlag);
        write(commentShape);
        write(commentText);
        writeUnknown(4);
    }

    public boolean isContModeAuto() {
        return (startupType & 0x7) == PSP_UTILITY_SCREENSHOT_TYPE_CONT_AUTO;
    }

    public boolean isContModeFinish() {
        return (startupType & 0x7) == PSP_UTILITY_SCREENSHOT_TYPE_CONT_FINISH;
    }

    @Override
	public int sizeof() {
        return base.totalSizeof();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("startupType=%d", startupType));
        sb.append(String.format(", status=%d", status));
        sb.append(String.format(", imgFormat=%d", imgFormat));
        sb.append(String.format(", imgQuality=%d", imgQuality));
        sb.append(String.format(", imgFrameBufAddr=0x%08X", imgFrameBufAddr));
        sb.append(String.format(", imgFrameBufWidth=%d", imgFrameBufWidth));
        sb.append(String.format(", imgPixelFormat=%d", imgPixelFormat));
        sb.append(String.format(", screenshotOffsetX=%d", screenshotOffsetX));
        sb.append(String.format(", screenshotOffsetY=%d", screenshotOffsetY));
        sb.append(String.format(", displayWidth=%d", displayWidth));
        sb.append(String.format(", displayHeigth=%d", displayHeigth));
        sb.append(String.format(", screenshotID='%s'", screenshotID));
        sb.append(String.format(", fileName='%s'", fileName));
        sb.append(String.format(", nameRule=%d", nameRule));
        sb.append(String.format(", title='%s'", title));
        sb.append(String.format(", parentalLevel=%d", parentalLevel));
        sb.append(String.format(", pscmFileFlag=%d", pscmFileFlag));
        sb.append(String.format(", iconPath='%s'", iconPath));
        sb.append(String.format(", iconPathSize=%d", iconPathSize));
        sb.append(String.format(", iconFileSize=%d", iconFileSize));
        sb.append(String.format(", backgroundPath='%s'", backgroundPath));
        sb.append(String.format(", backgroundPathSize=%d", backgroundPathSize));
        sb.append(String.format(", backgroundFileSize=%d", backgroundFileSize));
        sb.append(String.format(", commentFlag=%d", commentFlag));
        sb.append(String.format(", commentShape [%s]", commentShape));
        sb.append(String.format(", commentText [%s]", commentText));

        return sb.toString();
    }
}