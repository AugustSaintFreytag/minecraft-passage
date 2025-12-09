package net.saint.passage.util;

import java.util.HashMap;
import java.util.Map;

public class Profiler {
	private static final Map<String, Profiler> INSTANCES = new HashMap<>();

	private final String name;
	private final Map<String, ProfileSection> sections = new HashMap<>();

	private ProfileSection activeSection;

	private Profiler(String name) {
		this.name = name;
	}

	public static Profiler getProfiler(String name) {
		return INSTANCES.computeIfAbsent(name, Profiler::new);
	}

	public ProfileSection begin(String sectionName) {
		ProfileSection section = new ProfileSection(sectionName);
		sections.put(sectionName, section);
		activeSection = section;
		return section;
	}

	public ProfileSection getSection(String sectionName) {
		return sections.get(sectionName);
	}

	public static class ProfileSection {
		private final String name;
		private final long startTime;

		private long endTime;
		private boolean isActive = true;

		private ProfileSection(String name) {
			this.name = name;
			this.startTime = System.nanoTime();
		}

		public void end() {
			if (isActive) {
				endTime = System.nanoTime();
				isActive = false;
			}
		}

		public double getSeconds() {
			return getNanoSeconds() / 1_000_000_000.0;
		}

		public double getMilliseconds() {
			return getNanoSeconds() / 1_000_000.0;
		}

		public long getMicroseconds() {
			return getNanoSeconds() / 1_000;
		}

		public long getNanoSeconds() {
			if (isActive) {
				return System.nanoTime() - startTime;
			}
			return endTime - startTime;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return String.format("%.4fms", getMilliseconds());
		}

		public boolean isActive() {
			return isActive;
		}
	}
}