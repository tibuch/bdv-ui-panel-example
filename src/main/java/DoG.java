import java.util.ArrayList;
import java.util.List;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ops.OpService;
import net.imagej.ops.convert.RealTypeConverter;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

@Plugin(type = Command.class, menuPath = "Experiments>Blob Detection>DoG Pyramid")
public class DoG<T extends RealType<T>> implements Command {

	@Parameter
	private LogService log;

	@Parameter
	private OpService ops;

	@Parameter(type = ItemIO.INPUT)
	private Img<T> image;

	@Parameter(type = ItemIO.INPUT)
	private double sigma = 2;

	@Parameter(type = ItemIO.INPUT)
	private double factor = 1.6;

	@Parameter(type = ItemIO.INPUT)
	private int levels = 5;
	
	@Parameter(type = ItemIO.OUTPUT)
	private RandomAccessibleInterval<T> output;

	@Override
	public void run() {
		final Img<FloatType> converted = ops.convert().float32(image);
		final Img<FloatType> dog_img = ops.create().img(converted);

		// Create a NormalizeScaleRealTypes op
		final RealTypeConverter<FloatType, T> scale_op = (RealTypeConverter<FloatType, T>) ops
				.op("convert.normalizeScale", dog_img.firstElement(), image.firstElement());

		final List<Img<T>> dog = new ArrayList<>();

		double s1 = sigma;
		double s2 = s1 * factor;
		for (int i = 0; i < levels; i++) {
			log.info("Run DoG with sigmas: " + s1 + ", " + s2);

			ops.filter().dog(dog_img, converted, s1, s2);

			final Img<T> out = (Img<T>) ops.create().img(image);
			ops.convert().imageType(out, dog_img, scale_op);
			dog.add(out);

			s1 = s2;
			s2 = s1 * factor;
		}

		output = Views.stack(dog);
	}

}
