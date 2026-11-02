package com.common.backoffice.util.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 동영상 썸네일/재생정보 추출 서비스 (서버에 설치된 ffmpeg/ffprobe 명령 호출).
 *
 * - 실행 파일 경로는 Common.ffmpegPath / Common.ffprobePath로 지정 가능하고,
 *   설정이 없으면 PATH에 등록된 ffmpeg/ffprobe를 사용한다.
 * - ffmpeg 미설치·지원하지 않는 코덱 등으로 실패해도 예외를 던지지 않고 해당 필드를 null로 반환한다
 *   (업로드 자체는 계속 진행되어야 하므로).
 */
@Slf4j
@Service
public class VideoMediaService {

	/** 명령 하나당 최대 대기 시간(초) — 깨진 파일 등으로 ffmpeg가 멈추는 경우 대비 */
	private static final long PROCESS_TIMEOUT_SEC = 30;

	/** 썸네일 가로 크기(px), 세로는 비율 유지 */
	private static final int THUMBNAIL_WIDTH = 320;

	@Value("${Common.ffmpegPath:ffmpeg}")
	private String ffmpegPath;

	@Value("${Common.ffprobePath:ffprobe}")
	private String ffprobePath;

	/** 썸네일 파일명 + 재생시간(초) + 해상도. 추출 실패한 항목은 null */
	public record VideoInfo(String thumbnailName, String playTime, String width, String height) {}

	/** ffprobe 결과(재생시간 초 단위 실수, 가로, 세로) */
	private record ProbeResult(Double duration, String width, String height) {}

	/**
	 * 동영상 정보를 읽고 썸네일({파일ID}_thumb.jpg)을 같은 폴더에 생성한다.
	 *
	 * @param dirPath   동영상이 저장된 실제 폴더
	 * @param savedName 저장된 동영상 파일명
	 */
	public VideoInfo createThumbnailAndInfo(String dirPath, String savedName) {
		Path videoPath = Paths.get(dirPath, savedName);
		int dot = savedName.lastIndexOf('.');
		String thumbName = ((dot != -1) ? savedName.substring(0, dot) : savedName) + "_thumb.jpg";

		ProbeResult probe = probe(videoPath);
		String playTime = (probe.duration() != null) ? String.valueOf((int) Math.floor(probe.duration())) : null;

		// 첫 프레임은 검은 화면인 경우가 많아 1초 지점을 캡처(1초 미만 영상은 0초)
		double seekSec = (probe.duration() != null && probe.duration() > 1) ? 1.0 : 0.0;
		boolean created = createThumbnail(videoPath, Paths.get(dirPath, thumbName), seekSec);

		return new VideoInfo(created ? thumbName : null, playTime, probe.width(), probe.height());
	}

	private ProbeResult probe(Path videoPath) {
		List<String> command = List.of(
				ffprobePath, "-v", "error",
				"-select_streams", "v:0",
				"-show_entries", "stream=width,height:format=duration",
				"-of", "default=noprint_wrappers=1",
				videoPath.toString());

		String output = run(command);
		if (output == null) {
			return new ProbeResult(null, null, null);
		}

		// 출력 예: width=1920 / height=1080 / duration=29.033333 (한 줄에 하나씩)
		Map<String, String> values = new HashMap<>();
		for (String line : output.split("\\R")) {
			int eq = line.indexOf('=');
			if (eq > 0) {
				values.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
			}
		}

		Double duration = null;
		try {
			duration = Double.valueOf(values.get("duration"));
		} catch (NumberFormatException | NullPointerException e) {
			log.warn("동영상 재생시간 확인 불가: {}", videoPath.getFileName());
		}
		return new ProbeResult(duration, values.get("width"), values.get("height"));
	}

	private boolean createThumbnail(Path videoPath, Path thumbPath, double seekSec) {
		List<String> command = List.of(
				ffmpegPath, "-y", "-v", "error",
				"-ss", String.valueOf(seekSec),
				"-i", videoPath.toString(),
				"-frames:v", "1",
				"-vf", "scale=" + THUMBNAIL_WIDTH + ":-2",
				"-q:v", "3",
				thumbPath.toString());

		return run(command) != null && Files.isRegularFile(thumbPath);
	}

	/**
	 * 외부 명령 실행. 정상 종료(exit 0)면 표준출력(+에러출력)을, 실패·타임아웃이면 null을 반환한다.
	 * 인자는 리스트로 넘기므로 셸을 거치지 않아 파일명에 공백/특수문자가 있어도 안전하다.
	 */
	private String run(List<String> command) {
		Process process = null;
		try {
			process = new ProcessBuilder(command).redirectErrorStream(true).start();
			// -v error 옵션으로 출력이 몇 줄뿐이라 종료 대기 후 읽어도 파이프가 막히지 않는다
			if (!process.waitFor(PROCESS_TIMEOUT_SEC, TimeUnit.SECONDS)) {
				log.warn("명령 실행 시간 초과({}초): {}", PROCESS_TIMEOUT_SEC, command.get(0));
				return null;
			}
			String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			if (process.exitValue() != 0) {
				log.warn("명령 실행 실패(exit {}): {} - {}", process.exitValue(), command.get(0), output.trim());
				return null;
			}
			return output;
		} catch (IOException e) {
			// ffmpeg/ffprobe가 설치되지 않았거나 경로가 틀린 경우
			log.warn("명령 실행 불가(설치/경로 확인 필요): {} - {}", command.get(0), e.toString());
			return null;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		} finally {
			if (process != null && process.isAlive()) {
				process.destroyForcibly();
			}
		}
	}
}
