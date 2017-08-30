package jpx.terrgen;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Visualize extends JFrame {
	
	public Visualize(List<Vec3> vlist) {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("vertex viz");
		
		VizComponent vizc = new VizComponent(vlist, new Dimension(900,900));
		this.add(vizc);
		
		this.pack();
		this.setVisible(true); 
		
		Thread t = new Thread(()->{
			while(true) {
				vizc.updateImage();
				vizc.repaint();
				try { Thread.sleep(16); } 
				catch (InterruptedException e) {;;} 
			}
		});
		t.setDaemon(true);
		t.start();
	}
	
	
	private class VizComponent extends JPanel {
		
		private float absxymax;
		private List<Vec3> vlist;
		private BufferedImage image;
		private Rotation rotation = new Rotation(new Vec3(0,1,1), 0.005f); 
		
		public VizComponent(List<Vec3> vlist, Dimension dim) { 
			this.setPreferredSize(dim); 
			this.vlist = vlist;
			
			image = new BufferedImage(dim.width,dim.height, BufferedImage.TYPE_INT_ARGB); 
			Graphics g = image.getGraphics();
			g.setColor(Color.BLACK); 
			g.fillRect(0, 0, dim.width, dim.height);
			g.dispose();
			
			//Max XY value is used to scale vertices to fit the screen,
			//This is only calculated once to stop the scale from changing
			//when XY max changes. 
			absxymax = 0;
			for(Vec3 v:vlist) {
				if( absxymax < Math.abs(v.x) ) {
					absxymax = Math.abs(v.x);
				}
				if( absxymax < Math.abs(v.y) ) {
					absxymax = Math.abs(v.y); 
				}
			}
			absxymax+=1f;
		}
		
		
		public void updateImage() {
			
			int width = this.getWidth();
			int height = this.getHeight();
			
			BufferedImage buf = new BufferedImage(width, height, image.getType()); 
			Graphics g = buf.getGraphics();
			
			g.setColor(Color.BLACK); 
			g.fillRect(0, 0, width, height );
			
			//update vertex positions
			for(Vec3 v:vlist) {
				v.set( rotation.transform(v.x, v.y, v.z) );
			}
			
			//sort them by z 
			//(prevents occasional disappearing points that occur when black points overwrite the white ones)
			vlist.sort((a,b)->{
				return Float.compare(a.z, b.z); 
			});
			
			//Max z values from sorted list, used to fade the farthest points into the background
			//Assumes a symmetry on the z-axis (farthest == nearest).
			float abszmax = Math.abs(vlist.get(0).z);
			
			//scale and translate the vertices from world space to screen space,
			//and update the image
			int halfwidth = width/2;
			int halfheight = height/2;
			
			for(Vec3 v:vlist) {
				int x = (int) (v.x + halfwidth);
				int y = (int) (v.y+ halfheight);	
				float c = (v.z + abszmax)/(2*abszmax);
				
				g.setColor(new Color( c,c,c ));
				g.fillOval(x-2, y-2, 4, 4); 
			}
			g.dispose();
			image = buf;
		}


		public void paint(Graphics g) {
			g.drawImage(image, 0, 0, null);
		}
		
	}
	
}
