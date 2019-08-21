package nl.bliksoft.phototagger;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import net.coobird.thumbnailator.Thumbnails;

public class Photo {

	private PhotoQueue queue;
	private BufferedImage buffer;
	private File file;
	
	public Photo(File file, PhotoQueue q) {
		this.file = file;
		this.queue = q;
	}
	
	public boolean isBuffered() {
		return buffer != null;
	}
	
	public synchronized void unbuffer() {
		this.buffer = null;
		queue.unbuffered(this);
	}
	
	public void buffer() throws IOException {
		if (buffer == null) {
			BufferedImage load = Thumbnails.of(file)
	                   .scale(1)
	                   .asBufferedImage();
			
			synchronized(this) {
				buffer = load;
				queue.buffered(this);
				notifyAll();
			}
		}
		/*
		if (buffer == null) {
			buffer = ImageIO.read(file);
		}
		*/
	}
	
	public synchronized void bufferUnsafe(int width, int height) {
		try {
			buffer(width,height);
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void buffer(int width, int height) throws IOException {
		if (buffer == null) {
			BufferedImage load = Thumbnails.of(file)
			                   .size(width, height)
			                   .asBufferedImage();
			
			synchronized(this) {
				buffer = load;
				queue.buffered(this);
				notifyAll();
			}
		}
		/*
		buffer();
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) bi.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.drawImage(buffer, 0, 0, width, height, null);
		buffer = bi;
		*/
	}
	
	public synchronized void waitForBuffer() throws InterruptedException {
		while(buffer == null) {
			wait();
		}
	}
	
	public synchronized BufferedImage getImage() throws IOException {
		buffer();
		return buffer;
	}
	
	public synchronized BufferedImage getImage(int width, int height) throws IOException {
		buffer(width, height);
		return buffer;
	}

	public long lastModified() {
		return file.lastModified();
	}
	
	public FileTime getCreationDate() {
		try {
			Path p = file.toPath();
			BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
			return attr.creationTime();
		}
		catch (IOException ex) {
			throw new IllegalStateException("Photo object should always refer to a valid file!");
		}		
	}

	public String getName() {
		return file.getName();
	}
	
}
