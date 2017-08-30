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

public class Generate {

	
	public static void main(String[] args) {
		
		float radius = 300;
		int chunksPerEdge = 4;
		int verticesPerChunkEdge = 8;
		
		Terrain world = Generate.SphericalTerrain(radius, chunksPerEdge, verticesPerChunkEdge);		
		Terrain plane = Generate.PlanarTerrain(chunksPerEdge, radius/chunksPerEdge, verticesPerChunkEdge, new Vec3(0,0,0), new Vec3(1,0,0), new Vec3(0,0,1));
		
		
		NoiseFunc noiseFunc = (x,y,z)->{
			return (float)(  20*Math.cos(x/20) + 10*Math.sin(y/10) + 5*Math.cos(z/5)  );
		};
		Generate.applyRadialNoise(world, noiseFunc);
		
		Vec3 axis = new Vec3(0,1,0);
		Generate.applyAxialNoise(plane, axis, noiseFunc);
		
		new Visualize(world);
		new Visualize(plane);
	} 


	public static Terrain SphericalTerrain(float radius, int chunksPerEdge, int chunkEdgeVertexCount) {
		Terrain world = new Terrain();
		world.chunks = new TerrChunk[ 6*chunksPerEdge*chunksPerEdge ];
		
		int cp = 0;
		Terrain tmp;
		Vec3 corner, axisH, axisV;
		float chunklength = 2f/chunksPerEdge;
		
		//three faces from first corner
		corner = new Vec3(-1,-1,-1);
		
		// X,Y
		axisH = new Vec3(1,0,0);
		axisV = new Vec3(0,1,0);
		tmp = Generate.PlanarTerrain(chunksPerEdge, chunklength, chunkEdgeVertexCount, corner, axisH, axisV );
		for( TerrChunk chunk: tmp.chunks ) {
			world.chunks[cp++] = chunk;
		}
		
		// Y,Z
		axisH = new Vec3(0,0,1);
		axisV = new Vec3(0,1,0);
		tmp = Generate.PlanarTerrain(chunksPerEdge, chunklength, chunkEdgeVertexCount, corner, axisH, axisV );
		for( TerrChunk chunk: tmp.chunks ) {
			world.chunks[cp++] = chunk;
		}
		
		// X,Z
		axisH = new Vec3(1,0,0);
		axisV = new Vec3(0,0,1);
		tmp = Generate.PlanarTerrain(chunksPerEdge, chunklength, chunkEdgeVertexCount, corner, axisH, axisV );
		for( TerrChunk chunk: tmp.chunks ) {
			world.chunks[cp++] = chunk;
		}
		
		//three faces from opposite corner
		corner = new Vec3(+1,+1,+1);
		
		// X,Y
		axisH = new Vec3(-1,0,0);
		axisV = new Vec3(0,-1,0);
		tmp = Generate.PlanarTerrain(chunksPerEdge, chunklength, chunkEdgeVertexCount, corner, axisH, axisV );
		for( TerrChunk chunk: tmp.chunks ) {
			world.chunks[cp++] = chunk;
		}
		
		// Y,Z
		axisH = new Vec3(0,0,-1);
		axisV = new Vec3(0,-1,0);
		tmp = Generate.PlanarTerrain(chunksPerEdge, chunklength, chunkEdgeVertexCount, corner, axisH, axisV );
		for( TerrChunk chunk: tmp.chunks ) {
			world.chunks[cp++] = chunk;
		}
		
		// X,Z
		axisH = new Vec3(-1,0,0);
		axisV = new Vec3(0,0,-1);
		tmp = Generate.PlanarTerrain(chunksPerEdge, chunklength, chunkEdgeVertexCount, corner, axisH, axisV );
		for( TerrChunk chunk: tmp.chunks ) {
			world.chunks[cp++] = chunk;
		}
		
		//map unit cube to unit sphere and then scale to radius
		//http://mathproofs.blogspot.ca/2005/07/mapping-cube-to-sphere.html
		for(TerrChunk chunk:world.chunks) {
			for( int i=0; i<chunk.vertices.length; i+=3 ) {
				
				float ux = chunk.vertices[i+0];
				float uy = chunk.vertices[i+1];
				float uz = chunk.vertices[i+2];
				
				float x = (float) (ux * Math.sqrt(1 - (uy*uy/2) - (uz*uz/2) + (uy*uy*uz*uz/3)));
				float y = (float) (uy * Math.sqrt(1 - (uz*uz/2) - (ux*ux/2) + (uz*uz*ux*ux/3)));
				float z = (float) (uz * Math.sqrt(1 - (ux*ux/2) - (uy*uy/2) + (ux*ux*uy*uy/3)));
				
				Vec3 vert = new Vec3(x,y,z).scale(radius);
				chunk.vertices[i+0] = vert.x;
				chunk.vertices[i+1] = vert.y;
				chunk.vertices[i+2] = vert.z;
			}
			chunk.centroid.scale(radius);
		}
		
		return world;
	}
	
	public static Terrain PlanarTerrain(int chunksPerEdge, float chunkEdgeLength, int chunkEdgeVertexCount, Vec3 origin, Vec3 axisH, Vec3 axisV) {
		Terrain terrain = new Terrain();
		
		int n = chunksPerEdge;
		terrain.chunks = new TerrChunk[ n*n ];
		
		int cp = 0;
		for(int i=0; i<n; ++i) {
			for(int j=0; j<n; ++j) {
				Vec3 chunkOrigin = Vec3.add(origin, Vec3.add(Vec3.mul(axisH, i*chunkEdgeLength), Vec3.mul(axisV, j*chunkEdgeLength)));
				terrain.chunks[cp++] = GenerateTerrainChunk(
						chunkEdgeLength, 
						chunkEdgeVertexCount, 
						chunkOrigin, axisH, axisV);
			}
		}
		return terrain;
	}
	
	private static TerrChunk GenerateTerrainChunk(float sideLength, int vcount, Vec3 origin, Vec3 axisH, Vec3 axisV) {
		TerrChunk chunk = new TerrChunk();
		
		chunk.sideLength = sideLength;
		chunk.centroid = Vec3.add( origin , Vec3.add( Vec3.mul(axisV, 0.50f*sideLength), Vec3.mul(axisH, 0.50f*sideLength)));
		
		int n = vcount*vcount;
		chunk.vertices = new float[3*n];
		chunk.texcoord = new float[2*n]; 
		chunk.indices = new int[ 3*( 2*(n-1)*(n-1) ) ]; 
		
		float step = sideLength/(vcount-1);
		float invL = 1f/sideLength;
		int vp = 0;
		int tp = 0;
		int ip = 0;
		
		//     vert pos      tex coord        indices   
		//   (0)-----(1)    (0,0)--(1,0)    (a)-----(d)
		//    | \     |      |  \    |       | \     |
		//    |   \   |      |   \   |       |   \   |
		//    |     \ |      |    \  |       |     \ |
		//   (2)-----(3)    (0,1)--(1,1)    (b)-----(c)
		
		//vertex positions
		for(float v=0, i=0; i<vcount; v+=step, ++i) {
			for(float h=0, j=0; j<vcount; h+=step, ++j) {
				Vec3 vert = Vec3.add(origin, Vec3.add( Vec3.mul(axisH,h) , Vec3.mul(axisV,v)) );
				chunk.vertices[vp++] = vert.x;
				chunk.vertices[vp++] = vert.y;
				chunk.vertices[vp++] = vert.z;
			}
		}
		
		//texture coordinates
		for(float v=0, i=0; i<vcount; v+=step, ++i) {
			for(float h=0, j=0; j<vcount; h+=step, ++j) {
				chunk.texcoord[tp++] = h*invL;
				chunk.texcoord[tp++] = v*invL;
			}
		}
		
		//indices
		for(int v = 0; v<vcount-1; ++v) {
			for(int h = 0; h<vcount-1; ++h) {
				int a = v*vcount+h;
				int b = a+vcount;
				int c = b+1;
				int d = a+1;
				
				//triangle 1
				chunk.indices[ip++] = a;
				chunk.indices[ip++] = b;
				chunk.indices[ip++] = c;
				
				//triangle 2
				chunk.indices[ip++] = c;
				chunk.indices[ip++] = d;
				chunk.indices[ip++] = a;
			}
		}
		
		return chunk;
	}	
	
	
	private static void applyRadialNoise(Terrain world, NoiseFunc noiseFunc) {
		for(TerrChunk chunk:world.chunks) {
			for(int i=0; i<chunk.vertices.length; i+=3) {
				
				float x = chunk.vertices[i+0];
				float y = chunk.vertices[i+1];
				float z = chunk.vertices[i+2];
				
				Vec3 vert = new Vec3(x,y,z);	
				Vec3 axis = vert.normalized();
				vert = Vec3.add(vert, Vec3.mul(axis, noiseFunc.apply(x, y, z))); 
				
				chunk.vertices[i+0] = vert.x;
				chunk.vertices[i+1] = vert.y;
				chunk.vertices[i+2] = vert.z;
			}
		}
	}
	
	
	private static void applyAxialNoise(Terrain plane, Vec3 axis, NoiseFunc noiseFunc) {
		axis = axis.normalized();
		for(TerrChunk chunk:plane.chunks) {
			for(int i=0; i<chunk.vertices.length; i+=3) {
				
				float x = chunk.vertices[i+0];
				float y = chunk.vertices[i+1];
				float z = chunk.vertices[i+2];
				
				Vec3 vert = new Vec3(x,y,z);	
				vert = Vec3.add(vert, Vec3.mul(axis, noiseFunc.apply(x, y, z))); 
				
				chunk.vertices[i+0] = vert.x;
				chunk.vertices[i+1] = vert.y;
				chunk.vertices[i+2] = vert.z;
			}
		}
	}
	

}
