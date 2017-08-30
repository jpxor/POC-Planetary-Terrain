package jpx.terrgen;

@FunctionalInterface
public interface NoiseFunc {
	public float apply(float x, float y, float z);
}
