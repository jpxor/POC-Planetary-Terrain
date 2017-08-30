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

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Visualize extends JFrame {
	
	public Visualize(Terrain terr) {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("vertex viz");
		
		VizTerrain vizc = new VizTerrain(terr, new Dimension(900,900));
		this.add(vizc);
		
		this.pack();
		this.setVisible(true); 
		
		Thread t = new Thread(()->{
			while(true) {
				vizc.update();
				vizc.repaint();
				try { Thread.sleep(16); } 
				catch (InterruptedException e) {;;} 
			}
		});
		t.setDaemon(true);
		t.start();
	}

	private class VizTerrain extends JPanel{

		private BufferedImage image;
		private Rotation tilt = new Rotation(new Vec3(1,0,0), -0.3f); 
		private Rotation rotation = new Rotation(new Vec3(0,1,0), 0.005f); 
		
		private float radius;
		private Terrain terr;
		private Dimension dim;
		
		public VizTerrain(Terrain terrain, Dimension dimension) {
			this.setPreferredSize(dimension); 
			
			this.terr = terrain;
			this.dim = dimension;
			this.image = new BufferedImage(dim.width,dim.height, BufferedImage.TYPE_INT_ARGB);
			
			//tilt the terrain
			for( TerrChunk chunk:terr.chunks ) {
				
				for(int v=0; v<chunk.vertices.length; v+=3) {
					
					float x = chunk.vertices[v+0];
					float y = chunk.vertices[v+1];
					float z = chunk.vertices[v+2];
					
					Vec3 vert = tilt.transform(x, y, z);
					chunk.vertices[v+0] = vert.x;
					chunk.vertices[v+1] = vert.y;
					chunk.vertices[v+2] = vert.z;
				}
				Vec3 c = chunk.centroid;
				chunk.centroid = tilt.transform(c.x, c.y, c.z);
			}
			
			radius = 0;
			for( TerrChunk chunk:terrain.chunks ) {
				for( int i=0; i<chunk.vertices.length; ++i ) {
					if( chunk.vertices[i] > radius ) {
						radius = chunk.vertices[i];
					}
				}
			}
			
			Graphics g = this.image.getGraphics();
			g.setColor(Color.BLACK); 
			g.fillRect(0, 0, dim.width, dim.height);
			g.dispose();
		}

		public void update() {
			
			int width = this.getWidth();
			int height = this.getHeight();
			
			int halfwidth = width/2;
			int halfheight = height/2;
			
			BufferedImage buf = new BufferedImage(width, height, image.getType()); 
			Graphics g = buf.getGraphics();
			
			g.setColor(Color.BLACK); 
			g.fillRect(0, 0, width, height );
			
			//update all vertex positions
			for( TerrChunk chunk:terr.chunks ) {
				
				for(int v=0; v<chunk.vertices.length; v+=3) {
					
					float x = chunk.vertices[v+0];
					float y = chunk.vertices[v+1];
					float z = chunk.vertices[v+2];
					
					Vec3 vert = rotation.transform(x, y, z);
					chunk.vertices[v+0] = vert.x;
					chunk.vertices[v+1] = vert.y;
					chunk.vertices[v+2] = vert.z;
				}
				Vec3 c = chunk.centroid;
				chunk.centroid = rotation.transform(c.x, c.y, c.z);
			}
			
			//render only the chunks facing forwards
			Vec3 forward = new Vec3(0,0,1);
			
			for( TerrChunk chunk:terr.chunks ) {
				
				if( Vec3.dot(chunk.centroid.normalized(), forward ) < 0.5f ) {
					continue;
				}
				for(int v=0; v<chunk.vertices.length; v+=3) {
					int x = (int) chunk.vertices[v+0] + halfwidth;
					int y = (int) chunk.vertices[v+1] + halfheight;
					int z = (int) chunk.vertices[v+2];
					
					float distf = (float) Math.pow( (z+radius)/(2*radius), 2);
					distf = clamp(0.25f, distf, 1);
					
					int hp = (int) (10*distf);
					
					g.setColor(new Color( 1f, 1f, 1f, distf ));
					g.fillOval(x-hp, y-hp, 2*hp, 2*hp); 
				}
				
			}
			
			g.dispose();
			image = buf;
		}
		
		public void paint(Graphics g) {
			g.drawImage(image, 0, 0, null);
		}
		
	}

	private float clamp(float min, float val, float max) {
		return Math.min(max, Math.max(min, val));
	}
	
}
