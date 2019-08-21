package nl.bliksoft.phototagger;

import java.io.File;
import java.util.Map;

import javax.swing.JFrame;

public class PhotoWindow extends JFrame {
	
	private static final long serialVersionUID = 2944612161237628966L;

	public PhotoWindow(File dir, Map<Integer,String> tags) {
		super();
		setTitle("Photo Tagger");
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setUndecorated(true);
		
		getContentPane().add(new PhotoPanel(dir, tags, this::escapePressed));
		
		setVisible(true);
		
	}	
	
	public void escapePressed(QueueState state) {
		this.setVisible(false);
		new CopyWindow(state);
	}
	
}
