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

public class Rotation {
	
	public float m00,m01,m02;
	public float m10,m11,m12;
	public float m20,m21,m22;
	
	public Rotation(Vec3 axis, float angle) {
		axis = axis.normalized();
		float x = axis.x;
		float y = axis.y;
		float z = axis.z;
		float cosA = (float) Math.cos(angle);
		float sinA = (float) Math.sin(angle);
		
		// https://en.wikipedia.org/wiki/Rotation_matrix#General_rotations
		m00 = cosA + x*x*(1-cosA);	m01 = x*y*(1-cosA)-z*sinA;	m02 = x*z*(1-cosA)+y*sinA;
		m10 = y*x*(1-cosA)+z*sinA;	m11 = cosA + y*y*(1-cosA);	m12 = y*z*(1-cosA)-x*sinA;
		m20 = z*x*(1-cosA)-y*sinA;	m21 = z*y*(1-cosA)+x*sinA;	m22 = cosA + z*z*(1-cosA);
	}
	
	// https://en.wikipedia.org/wiki/Matrix_multiplication
	public Vec3 transform(float x, float y, float z) {
		return new Vec3(
			m00*x + m01*y + m02*z,
			m10*x + m11*y + m12*z,
			m20*x + m21*y + m22*z
		);
	}
}
