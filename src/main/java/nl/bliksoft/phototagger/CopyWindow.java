package nl.bliksoft.phototagger;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

public class CopyWindow extends JFrame {

	private static final long serialVersionUID = 1029901268592424408L;

	private JTextArea progress;
	private JProgressBar progressBar;
	private JTextField target;
	private JButton select, copy;
	
	private QueueState state;
	private CopyWorker worker;
	
	public CopyWindow(QueueState state) {
		super();
		this.state = state;
		setTitle("Fast Photo Tagger and Copier");
		setSize(550,600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		init();
		setVisible(true);
	}
	
	private void init() {
		
		Container pane = this.getContentPane();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
		
		JPanel tmp, tmp2;
		tmp = buildPanel();
		tmp.setBorder(BorderFactory.createTitledBorder("Files per tag category"));
		pane.add(tmp);
		
		tmp = new JPanel(new GridLayout(2,1));
		select = new JButton("Select target directory");
		select.addActionListener(this::selectTarget);
		tmp2 = new JPanel();
		tmp2.add(select);
		tmp.add(tmp2);
		tmp2 = new JPanel();
		target = new JTextField(25);
		target.setText(state.getPath());
		target.setEditable(false);
		tmp2.add(target);
		tmp.add(tmp2);
		tmp.setBorder(BorderFactory.createTitledBorder("Target Directory"));
		pane.add(tmp);
		
		tmp = new JPanel();
		copy = new JButton("Copy files");
		copy.addActionListener(this::copyFiles);
		tmp.add(copy);
		pane.add(tmp);
		
		tmp = new JPanel(new BorderLayout());
		progress = new JTextArea(15,25);
		progress.setEditable(false);
		DefaultCaret caret = (DefaultCaret) progress.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		tmp.add(new JScrollPane(progress), BorderLayout.CENTER);
		pane.add(tmp);
		this.progressBar = new JProgressBar();
		tmp.add(progressBar, BorderLayout.SOUTH);
		pane.add(tmp);	
	}
	
	private void selectTarget(ActionEvent ev) {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("Choose Photo Target Location");
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc.setAcceptAllFileFilterUsed(false);
		int result = jfc.showDialog(this, "Select");
		if (result == JFileChooser.APPROVE_OPTION) {
			File dir = jfc.getSelectedFile();
			target.setText(dir.getPath());
		}
	}
	
	private void copyFiles(ActionEvent ev) {
		copy.setEnabled(false);
		select.setEnabled(false);
		progress.setText("");
		worker = new CopyWorker(state, new File(target.getText()), progress);
		worker.addPropertyChangeListener(this::updateProgress);
		worker.execute();
	}
	
	private void updateProgress(PropertyChangeEvent ev) {
		int value = worker.getProgress();
		this.progressBar.setValue(value);
		if (value == 100) {
			copy.setEnabled(true);
			select.setEnabled(true);
		}
	}
	
	private JPanel buildPanel() {
		Map<String, List<String>> map = buildMap();
		JPanel result = new JPanel(new GridLayout(map.size()+1,2));
		
		int total = map.values()
				       .stream()
				       .mapToInt(List::size)
				       .sum();
		
		map.forEach((tag,files) -> {
			JPanel tmp;
			
			tmp = new JPanel();
			tmp.add(new JLabel("Tag '"+tag+"'"));
			result.add(tmp);
			
			tmp = new JPanel();
			tmp.add(new JLabel(files.size()+" files"));
			result.add(tmp);			
		});
		
		JPanel tmp = new JPanel();
		tmp.add(new JLabel("Total files to be copied"));
		result.add(tmp);
		tmp = new JPanel();
		tmp.add(new JLabel(total+" files"));
		result.add(tmp);
		
		return result;
	}
	
	private Map<String,List<String>> buildMap() {
		Map<String,List<String>> map = new TreeMap<>();
		
		Map<String, Set<String>> tags = state.getTags();
		
		tags.forEach( (fname,ftags) ->
			ftags.forEach(ftag -> {
				if (!map.containsKey(ftag)) {
					map.put(ftag, new ArrayList<>(Arrays.asList(fname)));
				}
				else {
					map.get(ftag).add(fname);
				}
			})
		);
		
		return map;
	}
}
