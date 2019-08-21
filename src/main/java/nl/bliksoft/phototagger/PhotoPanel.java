package nl.bliksoft.phototagger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;


public class PhotoPanel extends JPanel {
	
	private static final long serialVersionUID = -8043842010310812884L;

	private PhotoQueue queue; // = new PhotoQueue(new File("G:\\Dropbox\\Photos2019"));
	private Consumer<QueueState> callback;
	
	public PhotoPanel(File dir, Map<Integer,String> tags, Consumer<QueueState> callback) {
		super();
		this.queue = new PhotoQueue(dir, tags);
		this.callback = callback;
		registerKeyboardAction(this::escapePressed, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(this::prev, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(this::next, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		for (int i=0; i < 10; i++) {
			final int j = i;
			registerKeyboardAction(ev -> num(ev,j), KeyStroke.getKeyStroke(""+j), JComponent.WHEN_IN_FOCUSED_WINDOW);	
		}
		addComponentListener(new ResizeListener());
	}

	private void num(ActionEvent ev, int num) {
		queue.toggleCurrentTag(num);
		repaint();
	}
	
	private void next(ActionEvent ev) {
		queue.next();
		repaint();
	}

	private void prev(ActionEvent ev) {
		queue.prev();
		repaint();
	}
	
	private void escapePressed(ActionEvent ev) {
		callback.accept(queue.getState());
	}
	
	@Override
	protected void paintComponent(Graphics gr) {
		Graphics2D g = (Graphics2D) gr;
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setFont(g.getFont().deriveFont(18f));
		g.setStroke(new BasicStroke(2.5f));
		int lineHeight = (int)Math.ceil(1.25*getShape(g, "M").getBounds2D().getHeight());

		super.paintComponent(gr);
		int w = getWidth();
		int h = getHeight();
		queue.getCurrent().ifPresent( p -> {
			if (p.isBuffered()) {
				try {
					BufferedImage img = p.getImage(w,h);
					int iw = img.getWidth();
					int ih = img.getHeight();
					int dx = (w - iw)/2;
					int dy = (h - ih)/2;
					g.drawImage(img, dx, dy, iw, ih, null);
					
					int x = 20;
					int y = 2*lineHeight;
					
					
					// Draw current position
					drawWithOutline(g,queue.getPositionString(),x, y);
					
					List<String> tags = queue.getCurrentTags();
					Map<Integer, String> tagKeys = queue.getTagKeys();
					for (Entry<Integer, String> entry : tagKeys.entrySet()) {
						y += lineHeight;
						if (tags.contains(entry.getValue())) {
							Shape shape = getShape(g, entry.getKey()+". "+entry.getValue());
							drawWithOutline(g, shape, x, y);
						}
					}
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			else {
				drawWithOutline(g,"Buffering...",20,2*lineHeight);
				Thread t = new Thread(() ->  {
					try {
						p.waitForBuffer();
						repaint();
					}
					catch (InterruptedException ex) {}
				});
				t.setDaemon(true);
				t.start();
			}
		});
	}
	
	private Shape getShape(Graphics2D g2d, String text) {
		GlyphVector glyphVector = g2d.getFont().createGlyphVector(g2d.getFontRenderContext(), text);
		Shape textShape = glyphVector.getOutline();
		return textShape;
	}
	
	private Shape drawWithOutline(Graphics2D g2d, String text, int x, int y) {
		return drawWithOutline(g2d, getShape(g2d, text), x, y);
	}
	
	private Shape drawWithOutline(Graphics2D g2d, Shape textShape, int x, int y) {
		AffineTransform originalTransform = g2d.getTransform();
		g2d.translate(x, y);
		g2d.setColor(Color.WHITE);
		g2d.draw(textShape);
		g2d.setColor(Color.BLACK);
		g2d.fill(textShape);
		g2d.setTransform(originalTransform);
		return textShape;
	}
	
	private class ResizeListener extends ComponentAdapter {

		@Override
		public void componentResized(ComponentEvent e) {
			queue.setSize(getWidth(), getHeight());
		}
		
	}
	
}
