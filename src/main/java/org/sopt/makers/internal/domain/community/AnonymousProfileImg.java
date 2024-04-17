package org.sopt.makers.internal.domain.community;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

@Getter
public enum AnonymousProfileImg {
	BLUE(1, "profile_blue.svg"),
	ORANGE(2, "profile_orange.svg"),
	PINK(3, "profile_pink.svg"),
	SKYBLUE(4, "profile_skyblue.svg"),
	YELLOW(5, "profile_yellow.svg");

	private int index;
	private String content;

	private static final Map<Integer, AnonymousProfileImg> profileImgMap = new HashMap<>();

	AnonymousProfileImg(int index, String content) {
		this.index = index;
		this.content = content;
	}

	static {
		for (AnonymousProfileImg img : AnonymousProfileImg.values()) {
			profileImgMap.put(img.index, img);
		}
	}


	public static AnonymousProfileImg filtered(List<Integer> excludes) {
		return profileImgMap.keySet().stream()
			.filter(i -> !excludes.contains(i))
			.findFirst()
			.map(profileImgMap::get).orElse(null);
	}
}
