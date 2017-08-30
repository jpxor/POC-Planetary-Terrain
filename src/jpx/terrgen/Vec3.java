package jpx.terrgen;

public class Vec3 {
	
	public float x,y,z;
	
	public Vec3(Vec3 src) {
		this.x = src.x;
		this.y = src.y;
		this.z = src.z;
	}

	public Vec3(float x2, float y2, float z2) { 
		x = x2; y = y2; z = z2;
	}

	public Vec3 normalized() {
		Vec3 norm = new Vec3(this);
		float length = (float) Math.sqrt( x*x + y*y + z*z );
		norm.x /= length;
		norm.y /= length;
		norm.z /= length; 
		return norm;
	}
	
	public Vec3 set(float x2, float y2, float z2) {
		x = x2;
		y = y2;
		z = z2;
		return this;
	}

	public Vec3 set(Vec3 src) {
		x = src.x;
		y = src.y;
		z = src.z;
		return this;
	}
	
	public Vec3 scale(float f) {
		x *= f;
		y *= f;
		z *= f;
		return this;
	}

	public static Vec3 sub(Vec3 a, Vec3 b) { 
		Vec3 ret = new Vec3(a); 
		ret.x -= b.x;
		ret.y -= b.y;
		ret.z -= b.z;
		return ret;
	}

	public static Vec3 add(Vec3 a, Vec3 b) {
		Vec3 ret = new Vec3(a); 
		ret.x += b.x;
		ret.y += b.y;
		ret.z += b.z;
		return ret;
	}

	public static Vec3 mul(Vec3 v, float f) { 
		Vec3 ret = new Vec3(v); 
		ret.x *= f;
		ret.y *= f;
		ret.z *= f;
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		result = prime * result + Float.floatToIntBits(z);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vec3 other = (Vec3) obj;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z))
			return false;
		return true;
	}

	
	
	
}
