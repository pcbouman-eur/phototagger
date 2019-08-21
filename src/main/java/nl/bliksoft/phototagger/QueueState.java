package nl.bliksoft.phototagger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QueueState {

	private int current, max;
	private String path;
	
	private Map<Integer,String> tagKeys;
	private Map<String,Set<String>> tags;
	
	@JsonCreator
	public QueueState(@JsonProperty("current") int current,
					  @JsonProperty("max") int max,
			          @JsonProperty("path") String path,
			          @JsonProperty("tagKeys") Map<Integer,String> tagKeys,
			          @JsonProperty("tags") Map<String,Set<String>> tags) {
		this(path, max);
		this.current = current;
		this.tagKeys.putAll(tagKeys);
		tags.entrySet()
		    .forEach(e -> this.tags.put(e.getKey(), new TreeSet<>(e.getValue())));
	}
	
	public QueueState(String path, int max) {
		this.path = path;
		this.max = max;
		this.current = 0;
		this.tagKeys = new TreeMap<>();
		this.tags = new TreeMap<>();
	}
	
	
	public void setTag(Integer i, String tag) {
		if (tag == null) {
			tagKeys.remove(i);
		}
		else {
			tagKeys.put(i, tag);
		}
	}
	
	public void toggleTag(String filename, Integer key) {
		String tag = tagKeys.get(key);
		if (tag != null) {
			Set<String> tagSet = tags.get(filename);
			if (tagSet == null) {
				tagSet = new TreeSet<>();
				tagSet.add(tag);
				tags.put(filename, tagSet);
			}
			else {
				if (tagSet.contains(tag)) {
					tagSet.remove(tag);
				}
				else {
					tagSet.add(tag);
				}
			}
		}
	}
	
	public void incrementCurrent() {
		if (current < max-1) {
			current++;
		}
		else if (current == max-1) {
			current = 0;
		}
	}
	
	public void decrementCurrent() {
		if (current >= 1) {
			current--;
		}
		else if(current == 0) {
			current = max-1;
		}
	}
	
	public void setCurrent(int cur) {
		this.current = Math.max(0, Math.min(cur, max-1));
	}
	
	public List<String> getTags(String filename) {
		return new ArrayList<>(tags.getOrDefault(filename, Collections.emptySet()));
	}
	public int getCurrent() {
		return current;
	}
	public String getPath() {
		return path;
	}
	public Map<Integer, String> getTagKeys() {
		return Collections.unmodifiableMap(tagKeys);
	}
	public Map<String, Set<String>> getTags() {
		return Collections.unmodifiableMap(tags);
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
		if (current >= max) {
			current = 0;
		}
	}
	
	public List<Integer> getCachedIndices(int cacheBefore, int cacheAfter) {
		int start = current - cacheBefore;
		while (start < 0) {
			start += max;
		}
		start = start % max;
		List<Integer> result = new ArrayList<>();
		for (int t=0; t <= cacheBefore + cacheAfter; t++) {
			result.add((start + t) % max);
		}
		return result;
	}
	
	
	public static Optional<QueueState> forDirectory(File f) {
		if (f.isDirectory()) {
			File stateFile = new File(f, "state.json");
			if (stateFile.exists()) {
				ObjectMapper om = new ObjectMapper();
				try {
					return Optional.of(om.readValue(stateFile, QueueState.class));
				}
				catch (IOException ex) {}
			}
		}
		return Optional.empty();
		
	}
}
