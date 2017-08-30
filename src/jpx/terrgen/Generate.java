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

import java.util.ArrayList;
import java.util.List;

public class Generate {

	// set to true to combine cube and sphere vertices into 
	// a single visualization. 
	private static final boolean COMBINE = false; 

	public static void main(String[] args) {
		
		//side length, 
		//and number of vertices per side length
		float side = 600f;
		int vsize = 32;
		
		Vec3 originA = new Vec3( -side/2, -side/2, -side/2 );
		Vec3 originB = new Vec3( +side/2, +side/2, +side/2 );

		// generate vertices: 6 planes of the cube
		// > the three planes touching originA (-1,-1,-1)
		List<Vec3> planeA = GenerateVerticies(side, vsize, originA, new Vec3( 1, 0, 0), new Vec3( 0, 1, 0)); // x.y
		List<Vec3> planeB = GenerateVerticies(side, vsize, originA, new Vec3( 0, 0, 1), new Vec3( 0, 1, 0)); // z.y
		List<Vec3> planeC = GenerateVerticies(side, vsize, originA, new Vec3( 1, 0, 0), new Vec3( 0, 0, 1)); // x.z

		// > the three planes touching originB (+1,+1,+1)
		List<Vec3> planeD = GenerateVerticies(side, vsize, originB, new Vec3(-1, 0, 0), new Vec3( 0,-1, 0)); // -x-y
		List<Vec3> planeE = GenerateVerticies(side, vsize, originB, new Vec3( 0, 0,-1), new Vec3( 0,-1, 0)); // -z-y
		List<Vec3> planeF = GenerateVerticies(side, vsize, originB, new Vec3(-1, 0, 0), new Vec3( 0, 0,-1)); // -x-z
		
		List<Vec3> cubeVerts = new ArrayList<>();
		cubeVerts.addAll(planeA); 
		cubeVerts.addAll(planeB); 
		cubeVerts.addAll(planeC); 
		cubeVerts.addAll(planeD); 
		cubeVerts.addAll(planeE); 
		cubeVerts.addAll(planeF); 
		
		// construct indices array
		List<Tri> tris = GenerateTris(vsize);
		List<Integer> vindices = new ArrayList<>(6*3*tris.size());
		
		//the indices are identical for each plane, but since the plane's vertices 
		//have been appended, the indices need an offset. 
		for( int index_offset = 0, plane=0; plane<6; plane++, index_offset+=planeA.size() ) {
			vindices.addAll( toIndices( tris, index_offset ) );
		}
		
			
		//transform our vertices into a sphere!
		List<Vec3> sphereVerts = MapCubeToSphere(side, cubeVerts);
		
		//take a look!
		if(!COMBINE) {
			
			new Visualize(cubeVerts);
			new Visualize(sphereVerts);
			
		} else {
			
			List<Vec3> allVerts = new ArrayList<>();
			allVerts.addAll(sphereVerts);
			allVerts.addAll(cubeVerts);
			new Visualize(allVerts);
			
		}
		
		System.out.println("index count: " + vindices.size() );
		System.out.println("vertex count: " + cubeVerts.size() );
		System.out.println("triangle count: " + 6*tris.size() );
		
	} 
	

	public static List<Vec3> GenerateVerticies(float sideLength, int vcount, Vec3 origin, Vec3 axisH, Vec3 axisV){
		long start = System.currentTimeMillis();
		
		List<Vec3> vertices = new ArrayList<>();
		float step = sideLength/(vcount-1);
		
		for(float v=0, i=0; i<vcount; v+=step, ++i) {
			for(float h=0, j=0; j<vcount; h+=step, ++j) {
				Vec3 vpointer = Vec3.add(origin, Vec3.add( Vec3.mul(axisH,h) , Vec3.mul(axisV,v)) );
				vertices.add( vpointer );
			}
		}
		
		long dur = System.currentTimeMillis() - start;
		System.out.println("GenerateVerticies: " + ((double)dur)/1000 + "s");
		
		return vertices;
	}
	
	// http://mathproofs.blogspot.ca/2005/07/mapping-cube-to-sphere.html
	public static List<Vec3> MapCubeToSphere(float cubeSideLength, List<Vec3> inVerts){
		long start = System.currentTimeMillis();
		
		List<Vec3> outVerts = new ArrayList<>();
		float toUnit = 2f/cubeSideLength;
		float toFullsize = 1f/toUnit;
		
		for( Vec3 v:inVerts ) {
			
			//scale to unit cube (sides of -1 to +1)
			//for transform to work correctly
			float ux = v.x*toUnit;
			float uy = v.y*toUnit;
			float uz = v.z*toUnit;
			
			float x = (float) (ux * Math.sqrt(1 - (uy*uy/2) - (uz*uz/2) + (uy*uy*uz*uz/3)));
			float y = (float) (uy * Math.sqrt(1 - (uz*uz/2) - (ux*ux/2) + (uz*uz*ux*ux/3)));
			float z = (float) (uz * Math.sqrt(1 - (ux*ux/2) - (uy*uy/2) + (ux*ux*uy*uy/3)));
			
			//scale back up to full size
			outVerts.add(new Vec3(x, y, z).scale(toFullsize));
		}
		
		long dur = System.currentTimeMillis() - start;
		System.out.println("MapCubeToSphere: " + ((double)dur)/1000 + "s");
		
		return outVerts;
	}
	
	
	public static List<Tri> GenerateTris(int vsize) {
		long start = System.currentTimeMillis();
		
		List<Tri> tris = new ArrayList<>();
		//   (a)-----(d)
		//    | \     |
		//    |   \   |
		//    |     \ |
		//   (b)-----(c)
		for(int v = 0; v<vsize-1; ++v) {
			for(int h = 0; h<vsize-1; ++h) {
				int a = v*vsize+h;
				int b = a+vsize;
				int c = b+1;
				int d = a+1;
				tris.add(new Tri(a,b,c));
				tris.add(new Tri(c,d,a));
			}
		}
		long dur = System.currentTimeMillis() - start;
		System.out.println("generate tris: " + ((double)dur)/1000 + "s");
		
		return tris;
	}
	
	
	public static List<Integer> toIndices(List<Tri> tris, int index_offset) { 
		List<Integer> indices = new ArrayList<>(3*tris.size());
		for( Tri t:tris ) {
			indices.add( t.a + index_offset );
			indices.add( t.b + index_offset );
			indices.add( t.c + index_offset );
		}
		return indices;
	}


}
