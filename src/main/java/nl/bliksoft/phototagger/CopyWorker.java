package nl.bliksoft.phototagger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;

public class CopyWorker extends SwingWorker<Void,Void> {

	private QueueState state;
	private File targetDir;
	
	private JTextArea progressArea;
	
	public CopyWorker(QueueState state, File target, JTextArea progress) {
		this.state = state;
		this.targetDir = target;
		this.progressArea = progress;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		this.setProgress(0);
		File originDir = new File(state.getPath());
		Map<String,File> tagDirs = new HashMap<>();
		Map<String, Set<String>> tagMap = state.getTags();
		int tasks = tagMap.values()
				          .stream()
				          .mapToInt(Set::size)
				          .sum();
		report("Copying "+tasks+" files.\n");
		int count = 1;
		for (Entry<String,Set<String>> e : tagMap.entrySet()) {
			File src = new File(originDir, e.getKey());
			for (String tag : e.getValue()) {
				if (!tagDirs.containsKey(tag)) {
					File tagTarget = new File(targetDir, tag);
					if (!tagTarget.exists()) {
						report("Creating directory "+tagTarget+" ...");
						tagTarget.mkdirs();
						report(" Done\n");
					}
					tagDirs.put(tag, tagTarget);
				}
				File targetDir = tagDirs.get(tag);
				File target = new File(targetDir, e.getKey());
				report("["+count+"/"+tasks+"] Copying photo to target "+target.getPath()+" ...");
				FileUtils.copyFile(src, target);
				report(" Done.\n");
				count++;
				int percentage = (int)Math.floor((100d*count)/tasks);
				if (percentage < 100) {
					this.setProgress(percentage);
				}
			}
		}
		report("Finished copying files.\n");
		setProgress(100);
		return null;
	}
	
	private void report(String str) {
		if (progressArea != null) {
			progressArea.append(str);
		}
	}

}
