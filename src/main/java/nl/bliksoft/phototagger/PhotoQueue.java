package nl.bliksoft.phototagger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PhotoQueue {

	//private LinkedList<Photo> prev = new LinkedList<>();
	//private LinkedList<Photo> next = new LinkedList<>();
	
	private ExecutorService executor;
	
	private Set<Photo> buffered = new HashSet<>();
	private File stateFile;
	private ArrayList<Photo> list = new ArrayList<>();
	
	//private int cur = 0;
	private QueueState state;
	
	private int bufferBefore = 5;
	private int bufferAfter = 10;
	
	private int width = 1920, height = 1080;
	
	private boolean active = true;
	
	
	public PhotoQueue(File dir, Map<Integer, String> tags) {
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("Expected a directory as a argument.");
		}
		for (File f : dir.listFiles()) {
			if (f.isFile() && f.getName().toLowerCase().endsWith(".jpg")) {
				Photo p = new Photo(f,this);
				list.add(p);
			}
		}
		//list.sort((p1,p2) -> Long.compare(p1.lastModified(), p2.lastModified()));
		//list.sort((p1,p2) -> p1.getCreationDate().compareTo(p2.getCreationDate()));
		list.sort((p1,p2) -> p1.getName().compareTo(p2.getName()));
		
		this.stateFile = new File(dir, "state.json");
		if (stateFile.exists()) {
			try {
				ObjectMapper om = new ObjectMapper();
				this.state = om.readValue(this.stateFile, QueueState.class);
			}
			catch (IOException ex) {
				// TODO: handle more gracefully
				ex.printStackTrace();
			}
		}
		if (this.state == null) {
			this.state = new QueueState(dir.getPath(), list.size());
		}
		if (this.state.getMax() != list.size()) {
			this.state.setMax(list.size());	
		}
		tags.forEach(this.state::setTag);
		
		
		executor = Executors.newWorkStealingPool();
		Thread t = new Thread(this::bufferThread);
		t.setDaemon(true);
		t.start();

	}
	
	public synchronized Optional<Photo> getCurrent() {
		if (list.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(list.get(state.getCurrent()));
	}
	
	public synchronized List<String> getCurrentTags() {
		return getCurrent().map(p -> state.getTags(p.getName()))
				           .orElseGet(Collections::emptyList);
	}
	
	public synchronized void next() {
		if (list.isEmpty()) {
			return;
		}
		state.incrementCurrent();
		saveStateToFile();
		notifyAll();
	}
	
	public synchronized void prev() {
		if (list.isEmpty()) {
			return;
		}
		state.decrementCurrent();
		saveStateToFile();
		notifyAll();
	}

	private synchronized void saveStateToFile() {
		ObjectMapper om = new ObjectMapper();
		try {
			om.writeValue(stateFile, state);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void unbuffered(Photo photo) {
		synchronized(buffered) {
			buffered.remove(photo);
		}
	}

	public void buffered(Photo photo) {
		synchronized(buffered) {
			buffered.add(photo);
		}
	}
	
	public void stop() {
		// TODO: implement this!
		
		this.active = false;
		executor.shutdownNow();
		
		// TODO: unbuffer everything?
	}
	
	private void bufferThread() {
		while (active) {
			Set<Photo> remove;
			synchronized(buffered) {
				remove = new LinkedHashSet<>(buffered);
			}
			synchronized(this) {
				for (Integer t : state.getCachedIndices(bufferBefore, bufferAfter)) {
					Photo p = list.get(t);
					remove.remove(p);
					executor.execute(() -> p.bufferUnsafe(width,height));
				}
			}
			for (Photo p : remove) {
				executor.execute(p::unbuffer);
			}
			synchronized(this) {
				try {
					wait();
				}
				catch (InterruptedException ex) {
					ex.printStackTrace();
					return;
				}
			}
		}
	}

	public void setSize(int w, int h) {
		this.width = w;
		this.height = h;
	}

	public void toggleCurrentTag(int num) {
		getCurrent().ifPresent(
				p -> state.toggleTag(p.getName(), num));
		saveStateToFile();
	}

	public Map<Integer,String> getTagKeys() {
		return state.getTagKeys();
	}

	public String getPositionString() {
		return (1+state.getCurrent()) + " / " + state.getMax() + getCurrent().map(p -> " ("+p.getName()+")").orElse("");
	}

	public synchronized QueueState getState() {
		return state;
	}

}
