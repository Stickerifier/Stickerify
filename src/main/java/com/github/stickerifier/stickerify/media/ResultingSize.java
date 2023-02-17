package com.github.stickerifier.stickerify.media;

import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_SIZE;

import ws.schild.jave.info.VideoSize;

class ResultingSize {

    private static final int PRESERVE_ASPECT_RATIO = -2;

    private final int width;
    private final int height;

    ResultingSize(VideoSize videoSize) {
        boolean isWidthBigger = videoSize.getWidth() >= videoSize.getHeight();

        this.width = isWidthBigger ? MAX_SIZE : PRESERVE_ASPECT_RATIO;
        this.height = isWidthBigger ? PRESERVE_ASPECT_RATIO : MAX_SIZE;
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }
}
