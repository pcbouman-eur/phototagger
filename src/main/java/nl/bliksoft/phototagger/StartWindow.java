package nl.bliksoft.phototagger;

import java.awt.Container;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class StartWindow extends JFrame {

	private static final long serialVersionUID = 1059064382616644634L;

	private Map<Integer,JTextField> tags;
	private JTextField location;
	
	public StartWindow() {
		this.setTitle("Fast Photo Tagger and Copier");
		this.setSize(400,525);
		this.init();
		readProperties();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private File getPropFile() {
		File homedir = new File(System.getProperty("user.home"));
		return new File(homedir, ".phototagger.properties");
	}
	
	private void readProperties() {
		File propFile = getPropFile();
		if (propFile.exists()) {
			try (InputStream is = new FileInputStream(propFile)) {
				Properties prop = new Properties();
				prop.loadFromXML(is);
				String loc = prop.getProperty("location", "");
				if (!loc.equals("")) {
					File f = new File(loc);
					if (f.exists() && f.isDirectory()) {
						location.setText(loc);
						updateTags(f);
					}
				}
			}
			catch (IOException ex) {
				// TODO: user message on incorrect file?
				ex.printStackTrace();
			}
		}
	}
	
	private void saveProperties() {
		File propFile = getPropFile();
		Properties props = new Properties();
		props.setProperty("location", location.getText());
		try (OutputStream os = new FileOutputStream(propFile)) {
			props.storeToXML(os, "phototagger last location processed");
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void init() {
		Container container = this.getContentPane();
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
		
		JPanel temp, temp2;
		JButton button;
		temp = new JPanel(new GridLayout(2,1));
		temp2 = new JPanel();
		button = new JButton("Select Photo Collection");
		button.addActionListener(ev -> chooseLocation());
		temp2.add(button);
		temp.add(temp2);
		temp2 = new JPanel();
		location = new JTextField(30);
		location.setEditable(false);
		temp2.add(location);
		temp.add(temp2);
		temp.setBorder(BorderFactory.createTitledBorder("Location"));
		container.add(temp);
		
		temp = buildFields();
		temp.setBorder(BorderFactory.createTitledBorder("Tags"));
		container.add(temp);
		
		temp = new JPanel();
		button = new JButton("Tag Photos");
		button.addActionListener(ev -> this.tagPhotos());
		temp.add(button);
		container.add(temp);
	}
	
	private JPanel buildFields() {
		int count = 10;
		JPanel result = new JPanel();
		result.setLayout(new GridLayout(count,2));
		JPanel temp;
				
		this.tags = new TreeMap<>();
		for (int i=1; i <= count; i++) {
			temp = new JPanel();
			temp.add(new JLabel("Tag "+(i%count)));
			result.add(temp);
			temp = new JPanel();
			JTextField field = new JTextField(15);
			if (i==1) {
				field.setText("social");
			}
			else if (i==2) {
				field.setText("friends");
			}
			else if (i==3) {
				field.setText("personal");
			}			temp.add(field);
			result.add(temp);
			tags.put(i, field);
		}
		
		return result;
	}
	
	private void chooseLocation() {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("Choose Photo Location");
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc.setAcceptAllFileFilterUsed(false);
		int result = jfc.showDialog(this, "Select");
		if (result == JFileChooser.APPROVE_OPTION) {
			File dir = jfc.getSelectedFile();
			location.setText(dir.getPath());
			updateTags(dir);
			saveProperties();
		}
	}
	
	private void updateTags(File dir) {
		Optional<QueueState> state = QueueState.forDirectory(dir);
		state.ifPresent(qs -> {
			qs.getTagKeys()
			  .forEach((i,str) -> tags.getOrDefault(i,new JTextField()).setText(str));
		});
	}
	
	private Map<Integer,String> getTags() {
		return tags.entrySet()
				   .stream()
				   .filter(e -> !e.getValue().getText().trim().isEmpty())
				   .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getText()));
	}
	
	private void tagPhotos() {
		if (!location.getText().equals("")) {
			new PhotoWindow(new File(location.getText()), getTags());
			this.setVisible(false);
		}
		else {
			JOptionPane.showMessageDialog(this, "Please select a location with photos to tag first.");
		}
	}
	
	public static void main(String [] args) {
		new StartWindow();
	}
	
}
