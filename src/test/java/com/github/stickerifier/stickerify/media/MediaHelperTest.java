package com.github.stickerifier.stickerify.media;

import static com.github.stickerifier.stickerify.ResourceHelper.loadResource;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MATROSKA_FORMAT;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_IMAGE_FILE_SIZE;
import static com.github.stickerifier.stickerify.media.MediaConstraints.MAX_VIDEO_FILE_SIZE;
import static com.github.stickerifier.stickerify.media.MediaConstraints.VP9_CODEC;
import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.github.stickerifier.stickerify.exception.MediaException;
import com.github.stickerifier.stickerify.junit.ClearTempFiles;
import com.github.stickerifier.stickerify.junit.Tags;
import com.github.stickerifier.stickerify.process.PathLocator;
import com.github.stickerifier.stickerify.process.ProcessHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.info.MultimediaInfo;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Tag(Tags.MEDIA)
@ClearTempFiles
class MediaHelperTest {

	@Test
	@Tag(Tags.IMAGE)
	void resizeRectangularImage() throws Exception {
		var jpgImage = loadResource("big.jpg");
		var result = MediaHelper.convert(jpgImage);

		assertImageConsistency(result, 512, 341);
	}

	private static void assertImageConsistency(File result, int expectedWidth, int expectedHeight) throws Exception {
		var size = getFileInfo(result).getVideo().getSize();
		var actualExtension = getExtension(result);

		assertAll("Image validation failed",
				() -> assertThat("image's extension must be webp", actualExtension, is(equalTo(".webp"))),
				() -> assertThat("image's width is not correct", size.getWidth(), is(equalTo(expectedWidth))),
				() -> assertThat("image's height is not correct", size.getHeight(), is(equalTo(expectedHeight))),
				() -> assertThat("image size should not exceed 512 KB", Files.size(result.toPath()), is(lessThanOrEqualTo(MAX_IMAGE_FILE_SIZE)))
		);
	}

	private static MultimediaInfo getFileInfo(File file) throws Exception {
		return new MultimediaObject(file, PathLocator.INSTANCE).getInfo();
	}

	private static String getExtension(File file) {
		return file.getName().substring(file.getName().lastIndexOf('.'));
	}

	@Test
	@Tag(Tags.IMAGE)
	void resizeSmallImage() throws Exception {
		var pngImage = loadResource("small_image.png");
		var result = MediaHelper.convert(pngImage);

		assertImageConsistency(result, 512, 512);
	}

	@Test
	@Tag(Tags.IMAGE)
	void noImageConversionNeeded() throws Exception {
		var pngImage = loadResource("valid.png");
		var result = MediaHelper.convert(pngImage);

		assertThat(result, is(nullValue()));
	}

	@Test
	@Tag(Tags.IMAGE)
	void resizeStaticWebpImage() throws Exception {
		var webpImage = loadResource("static.webp");
		var result = MediaHelper.convert(webpImage);

		assertImageConsistency(result, 256, 512);
	}

	@Test
	@Tag(Tags.IMAGE)
	void resizeFaviconImage() throws Exception {
		var faviconImage = loadResource("favicon.ico");
		var result = MediaHelper.convert(faviconImage);

		assertImageConsistency(result, 512, 512);
	}

	@Test
	@Tag(Tags.IMAGE)
	void resizeTiffImage() throws Exception {
		var tiffImage = loadResource("valid.tiff");
		var result = MediaHelper.convert(tiffImage);

		assertImageConsistency(result, 512, 342);
	}

	@Test
	@Tag(Tags.IMAGE)
	void resizePsdImage() throws Exception {
		var psdImage = loadResource("valid.psd");
		var result = MediaHelper.convert(psdImage);

		assertImageConsistency(result, 512, 384);
	}

	@Test
	@Tag(Tags.IMAGE)
	void resizeDetailedImage() throws Exception {
		var detailedImage = loadResource("detailed.jpg");
		var result = MediaHelper.convert(detailedImage);

		assertImageConsistency(result, 512, 512);
	}

	@Test
	@Tag(Tags.IMAGE)
	void resizeSvgImage() throws Exception {
		assumeSvgSupport();

		var svgImage = loadResource("valid.svg");
		var result = MediaHelper.convert(svgImage);

		assertImageConsistency(result, 512, 512);
	}

	private static void assumeSvgSupport() throws Exception {
		var lines = ProcessHelper.executeCommand("ffmpeg", "-v", "quiet", "-hide_banner", "-decoders");
		var supportsSvg = lines.stream().anyMatch(line -> line.contains("(codec svg)"));
		assumeTrue(supportsSvg, "FFmpeg was not compiled with SVG support");
	}

	@Test
	@Tag(Tags.VIDEO)
	void convertLongMovVideo() throws Exception {
		var movVideo = loadResource("long.mov");
		var result = MediaHelper.convert(movVideo);

		assertVideoConsistency(result, 512, 288, 29.97F, 3_000L);
	}

	private static void assertVideoConsistency(File result, int expectedWidth, int expectedHeight, float expectedFrameRate, long expectedDuration) throws Exception {
		var mediaInfo = getFileInfo(result);
		var videoInfo = mediaInfo.getVideo();
		var videoSize = videoInfo.getSize();

		var actualExtension = getExtension(result);

		assertAll("Video validation failed",
				() -> assertThat("video's extension must be webm", actualExtension, is(equalTo(".webm"))),
				() -> assertThat("video's width is not correct", videoSize.getWidth(), is(equalTo(expectedWidth))),
				() -> assertThat("video's height is not correct", videoSize.getHeight(), is(equalTo(expectedHeight))),
				() -> assertThat("video's frame rate is not correct", videoInfo.getFrameRate(), is(equalTo(expectedFrameRate))),
				() -> assertThat("video must be encoded with the VP9 codec", videoInfo.getDecoder(), startsWith(VP9_CODEC)),
				() -> assertThat("video's duration is not correct", mediaInfo.getDuration(), is(equalTo(expectedDuration))),
				() -> assertThat("video's format must be matroska", mediaInfo.getFormat(), is(equalTo(MATROSKA_FORMAT))),
				() -> assertThat("video must have no audio stream", mediaInfo.getAudio(), is(nullValue())),
				() -> assertThat("video size should not exceed 256 KB", Files.size(result.toPath()), is(lessThanOrEqualTo(MAX_VIDEO_FILE_SIZE)))
		);
	}

	@Test
	@Tag(Tags.VIDEO)
	void convertMp4WithAudio() throws Exception {
		var mp4Video = loadResource("video_with_audio.mp4");
		var result = MediaHelper.convert(mp4Video);

		assertVideoConsistency(result, 512, 288, 29.97F, 3_000L);
	}

	@Test
	@Tag(Tags.VIDEO)
	void convertM4vWithAudio() throws Exception {
		var m4vVideo = loadResource("video_with_audio.m4v");
		var result = MediaHelper.convert(m4vVideo);

		assertVideoConsistency(result, 512, 214, 23.98F, 3_000L);
	}

	@Test
	@Tag(Tags.VIDEO)
	void convertShortAndLowFpsVideo() throws Exception {
		var webmVideo = loadResource("short_low_fps.webm");
		var result = MediaHelper.convert(webmVideo);

		assertVideoConsistency(result, 512, 288, 10F, 1_000L);
	}

	@Test
	@Tag(Tags.VIDEO)
	void resizeSmallWebmVideo() throws Exception {
		var webmVideo = loadResource("small_video_sticker.webm");
		var result = MediaHelper.convert(webmVideo);

		assertVideoConsistency(result, 512, 212, 30F, 2_600L);
	}

	@Test
	@Tag(Tags.VIDEO)
	void convertVerticalWebmVideo() throws Exception {
		var webmVideo = loadResource("vertical_video_sticker.webm");
		var result = MediaHelper.convert(webmVideo);

		assertVideoConsistency(result, 288, 512, 30F, 2_000L);
	}

	@Test
	@Tag(Tags.VIDEO)
	void convertGifVideo() throws Exception {
		var gifVideo = loadResource("valid.gif");
		var result = MediaHelper.convert(gifVideo);

		assertVideoConsistency(result, 512, 274, 10F, 1_000L);
	}

	@Test
	@Tag(Tags.VIDEO)
	void convertAviVideo() throws Exception {
		var aviVideo = loadResource("valid.avi");
		var result = MediaHelper.convert(aviVideo);

		assertVideoConsistency(result, 512, 512, 30F, 3_000L);
	}

	@Test
	@Tag(Tags.VIDEO)
	void noVideoConversionNeeded() throws Exception {
		var webmVideo = loadResource("no_conversion_needed.webm");
		var result = MediaHelper.convert(webmVideo);

		assertThat(result, is(nullValue()));
	}

	@Test
	@Tag(Tags.VIDEO)
	void resizeAnimatedWebpVideo() {
		var webpVideo = loadResource("animated.webp");

		var ex = assertThrows(MediaException.class, () -> MediaHelper.convert(webpVideo));
		assertThat(ex.getMessage(), equalTo("Couldn't read image dimensions"));
	}

	@Test
	@Tag(Tags.ANIMATED_STICKER)
	void noAnimatedStickerConversionNeeded() throws Exception {
		var animatedSticker = loadResource("animated_sticker.tgs");
		var result = MediaHelper.convert(animatedSticker);

		assertThat(result, is(nullValue()));
	}

	@Test
	@Tag(Tags.ANIMATED_STICKER)
	void noLowFpsAnimatedStickerConversionNeeded() throws Exception {
		var animatedSticker = loadResource("low_fps_animated_sticker.tgs");
		var result = MediaHelper.convert(animatedSticker);

		assertThat(result, is(nullValue()));
	}

	@Test
	@Tag(Tags.ANIMATED_STICKER)
	void nonCompliantAnimatedSticker() {
		var animatedSticker = loadResource("non_compliant_animated_sticker.tgs");

		var ex = assertThrows(MediaException.class, () -> MediaHelper.convert(animatedSticker));
		assertThat(ex.getMessage(), equalTo("The file with application/gzip MIME type is not supported"));
	}

	@Test
	@Tag(Tags.ANIMATED_STICKER)
	void unsupportedGzipArchive() {
		var archive = loadResource("unsupported_archive.gz");

		var ex = assertThrows(MediaException.class, () -> MediaHelper.convert(archive));
		assertThat(ex.getMessage(), equalTo("The file with application/gzip MIME type is not supported"));
	}

	@Test
	@Tag(Tags.UNSUPPORTED_FILE)
	void unsupportedFile() {
		var document = loadResource("document.txt");

		var ex = assertThrows(MediaException.class, () -> MediaHelper.convert(document));
		assertThat(ex.getMessage(), equalTo("The file with text/plain MIME type is not supported"));
	}

	@Nested
	@Tag(Tags.CONCURRENT)
	@DisplayName("Concurrently convert")
	@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
	class ConcurrencyTest {

		@Test
		@Tag(Tags.VIDEO)
		@DisplayName("mov videos")
		void concurrentMovVideoConversions() {
			var movVideo = loadResource("long.mov");
			executeConcurrentConversionsOf(movVideo);
		}

		private static void executeConcurrentConversionsOf(File inputFile) {
			final int concurrentRequests = 50;
			var failedConversions = new AtomicInteger(0);
			var failureReasons = ConcurrentHashMap.newKeySet();

			try (var executor = newVirtualThreadPerTaskExecutor()) {
				IntStream.range(0, concurrentRequests).forEach(_ -> executor.execute(() -> {
					try {
						MediaHelper.convert(inputFile);
					} catch (Throwable e) {
						failedConversions.incrementAndGet();
						failureReasons.add(e.getMessage());
					}
				}));
			}

			int failures = failedConversions.get();
			var errorMessage = "Unable to process %d concurrent requests: %d conversions failed: %s".formatted(concurrentRequests, failures, failureReasons);

			assertThat(errorMessage, failures, is(equalTo(0)));
		}

		@Test
		@Tag(Tags.VIDEO)
		@DisplayName("mp4 videos")
		void concurrentMp4VideoConversions() {
			var mp4Video = loadResource("video_with_audio.mp4");
			executeConcurrentConversionsOf(mp4Video);
		}

		@Test
		@Tag(Tags.VIDEO)
		@DisplayName("m4v videos")
		void concurrentM4vVideoConversions() {
			var m4vVideo = loadResource("video_with_audio.m4v");
			executeConcurrentConversionsOf(m4vVideo);
		}

		@Test
		@Tag(Tags.VIDEO)
		@DisplayName("webm videos")
		void concurrentWebmVideoConversions() {
			var webmVideo = loadResource("small_video_sticker.webm");
			executeConcurrentConversionsOf(webmVideo);
		}

		@Test
		@Tag(Tags.VIDEO)
		@DisplayName("avi videos")
		void concurrentAviVideoConversions() {
			var aviVideo = loadResource("valid.avi");
			executeConcurrentConversionsOf(aviVideo);
		}

		@Test
		@Tag(Tags.VIDEO)
		@DisplayName("gif videos")
		void concurrentGifVideoConversions() {
			var gifVideo = loadResource("valid.gif");
			executeConcurrentConversionsOf(gifVideo);
		}

		@Test
		@Tag(Tags.IMAGE)
		@DisplayName("webp images")
		void concurrentWebpImageConversions() {
			var webpImage = loadResource("static.webp");
			executeConcurrentConversionsOf(webpImage);
		}

		@Test
		@Tag(Tags.IMAGE)
		@DisplayName("jpg images")
		void concurrentJpgImageConversions() {
			var jpgImage = loadResource("big.jpg");
			executeConcurrentConversionsOf(jpgImage);
		}

		@Test
		@Tag(Tags.IMAGE)
		@DisplayName("png images")
		void concurrentPngImageConversions() {
			var pngImage = loadResource("big.png");
			executeConcurrentConversionsOf(pngImage);
		}

		@Test
		@Tag(Tags.IMAGE)
		@DisplayName("ico images")
		void concurrentFaviconImageConversions() {
			var faviconImage = loadResource("favicon.ico");
			executeConcurrentConversionsOf(faviconImage);
		}

		@Test
		@Tag(Tags.IMAGE)
		@DisplayName("tiff images")
		void concurrentTiffImageConversions() {
			var tiffImage = loadResource("valid.tiff");
			executeConcurrentConversionsOf(tiffImage);
		}

		@Test
		@Tag(Tags.IMAGE)
		@DisplayName("psd images")
		void concurrentPsdImageConversions() {
			var psdImage = loadResource("valid.psd");
			executeConcurrentConversionsOf(psdImage);
		}

		@Test
		@Tag(Tags.IMAGE)
		@DisplayName("svg images")
		void concurrentSvgImageConversions() throws Exception {
			assumeSvgSupport();

			var svgImage = loadResource("valid.svg");
			executeConcurrentConversionsOf(svgImage);
		}
	}
}
