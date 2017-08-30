/*
MIT License

Copyright (c) 2017 Josh Simonot

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

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
			
			//translate the vertices to center of frame,
			//and update the image
			int halfwidth = width/2;
			int halfheight = height/2;
			
			for(Vec3 v:vlist) {
				int x = (int) (v.x + halfwidth);
				int y = (int) (v.y + halfheight);	
				
				float c = (v.z + abszmax)/(2*abszmax);
				c = clamp(0f,c,1f);
				
				g.setColor(new Color( c,c,c ));
				g.fillOval(x-2, y-2, 4, 4); 
			}
			g.dispose();
			image = buf;
		}


		private float clamp(float min, float val, float max) {
			return Math.min(max, Math.max(min, val));
		}


		public void paint(Graphics g) {
			g.drawImage(image, 0, 0, null);
		}
		
	}
	
}
